
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedWriter;

// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {

    long TJstartTime;
    long TJelapsedTime;
    long TSstartTime;
    long TSelapsedTime;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        TSstartTime = System.nanoTime();

        // browse features
        String genre = request.getParameter("genre");
        String index = request.getParameter("char");

        if (index != null && !index.isEmpty() && !index.equals("null") && !index.equals("*")) {
            index = index + "%";
        }

        // Search Queries
        String title = request.getParameter("search_title");
        String year = request.getParameter("search_year");
        String director = request.getParameter("search_director");
        String star = request.getParameter("search_star");
        String fulltxt = request.getParameter("fulltxt");

        if (title != null && !title.isEmpty() && !title.equals("null") && fulltxt == null) {
            title = "%" + title + "%";
        }
        if (director != null && !director.isEmpty() && !director.equals("null")) {
            director = "%" + director + "%";
        }
        if (star != null && !star.isEmpty() && !star.equals("null")) {
            star = "%" + star + "%";
        }

        // User filter/sort
        String mvct = request.getParameter("mvct");
        String page = request.getParameter("page");
        String sort = request.getParameter("sort");
        String tOrder = request.getParameter("title_order");
        String rOrder = request.getParameter("rating_order");

        if (mvct == null && page == null && sort == null && tOrder == null && rOrder == null) {
            mvct = "10";
            page = "1";
            sort = "Default";
            tOrder = "Default";
            rOrder = "Default";
            fulltxt = "null";
        }

        boolean sorting = false;
        if (!sort.equals("Default")) {
            sorting = true;
            sort = sort.toLowerCase();
        }

        int mvct_num = Integer.parseInt(mvct);
        int page_num = Integer.parseInt(page) - 1;
        int offset_num = mvct_num * page_num;

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the
        // connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            TJstartTime = System.nanoTime();

            String query1 = "SELECT movies.* FROM (SELECT DISTINCT movies.*, ratings.rating FROM movies LEFT OUTER JOIN ratings ON movies.id = ratings.movieId) as movies "
                    + "WHERE (movies.title LIKE ? or ? is null or movies.title REGEXP ?) "
                    + "AND (movies.year LIKE ? or ? is null) " + "AND (movies.director LIKE ? or ? is null)";

            String query = ""; // for full text

            if (!fulltxt.equals("null")) {
                // get the query string from parameter
                String[] queryList = title.split(" ");

                for (String elem : queryList) {
                    query += elem;
                    query += "* ";
                }

                query1 = "SELECT * FROM (SELECT movies.*, ratings.rating FROM movies, ratings WHERE match(title) against (? IN BOOLEAN MODE) AND movies.id = ratings.movieId) as movies";

            }

            if (genre != null && !genre.isEmpty() && !genre.equals("null")) {
                query1 = "SELECT movies.* FROM (" + query1 + ") as movies, genres_in_movies as gim, genres "
                        + "WHERE genres.name = ?" + "AND genres.id = gim.genreId AND gim.movieId = movies.id";
            } else if (star != null && !star.isEmpty() && !star.equals("null")) {
                query1 = "SELECT DISTINCT movies.* FROM (SELECT movies.*, ratings.rating FROM movies LEFT OUTER JOIN ratings ON movies.id = ratings.movieId) as movies, stars_in_movies as sim, stars "
                        + "WHERE (movies.title LIKE ? or ? is null or movies.title REGEXP ?) "
                        + "AND (movies.year LIKE ? or ? is null) " + "AND (movies.director LIKE ? or ? is null) "
                        + "AND stars.name LIKE ? " + "AND stars.id = sim.starId AND sim.movieId = movies.id";
            }

            if (sort != null && !sort.isEmpty() && sort.equals("title")) {
                query1 += " ORDER BY movies.title";
                if (tOrder.equals("desc")) {
                    query1 += " DESC, movies.rating";
                } else {
                    query1 += " ASC, movies.rating";
                }
                if (rOrder.equals("asc")) {
                    query1 += " ASC";
                } else {
                    query1 += " DESC";
                }
            } else if (sort != null && !sort.isEmpty() && sort.equals("rating")) {
                query1 += " ORDER BY movies.rating";
                if (rOrder.equals("asc")) {
                    query1 += " ASC, movies.title";
                } else {
                    query1 += " DESC, movies.title";
                }
                if (tOrder.equals("desc")) {
                    query1 += " DESC";
                } else {
                    query1 += " ASC";
                }
            } else {
                query1 += " ORDER BY movies.id";
            }

            query1 += " LIMIT ? OFFSET ?";

            PreparedStatement statement1 = conn.prepareStatement(query1, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            // count query from https://stackoverflow.com/a/53212180
            String query2 = "SELECT stars.id, stars.name, s.movieId "
                    + "FROM stars, (SELECT sim.*, counter.count FROM (SELECT stars_in_movies.* FROM stars_in_movies, ("
                    + query1 + ") as q " + "WHERE stars_in_movies.movieId = q.id) as sim "
                    + "LEFT JOIN (SELECT starId, COUNT(starId) as count FROM stars_in_movies GROUP BY starId) as counter "
                    + "ON counter.starId = sim.starId) as s " + "WHERE stars.id = s.starId "
                    + "ORDER BY s.movieId, s.count DESC, stars.name ASC";

            String query3 = "SELECT genres.name, g.movieId " + "FROM genres, genres_in_movies as g, " + "(" + query1
                    + ") as q " + "WHERE q.id = g.movieId AND g.genreId = genres.id "
                    + "ORDER BY g.movieId, genres.name ASC";

            if (sorting) {
                query2 = "SELECT s.id, s.name, s.movieId FROM (SELECT stars.*, sim.movieId, sim.count FROM stars, "
                        + "(SELECT stars_in_movies.*, counter.count FROM stars_in_movies LEFT JOIN (SELECT starId, COUNT(starId) as count "
                        + "FROM stars_in_movies GROUP BY starId) as counter ON counter.starId = stars_in_movies.starId) as sim "
                        + "WHERE stars.id = sim.starId) as s INNER JOIN " + "(" + query1 + ") as q "
                        + "ON q.id = s.movieId ";

                query3 = "SELECT g.name, g.movieId FROM (SELECT genres.name, gim.movieId FROM genres, genres_in_movies as gim WHERE gim.genreId = genres.id) as g INNER JOIN "
                        + "(" + query1 + ") as q " + "ON q.id = g.movieId ";

                if (!sort.isEmpty() && sort.equals("title")) {
                    if (sorting) {
                        String orderString = "ORDER BY q.title "; // tOrder, rOrder
                        if (tOrder.equals("desc")) {
                            orderString += "DESC, q.rating ";
                        } else {
                            orderString += "ASC, q.rating ";
                        }
                        if (rOrder.equals("asc")) {
                            orderString += "ASC, ";
                        } else {
                            orderString += "DESC, ";
                        }
                        query2 += orderString + "s.count DESC, stars.name ASC";
                        query3 += orderString + "g.name ASC";
                    } else {
                        query2 += "ORDER BY q.title, q.rating, s.count DESC, stars.name ASC";
                        query3 += "ORDER BY q.title, q.rating, g.name ASC";
                    }
                } else if (!sort.isEmpty() && sort.equals("rating")) {
                    if (sorting) {
                        String orderString = "ORDER BY q.rating ";
                        if (rOrder.equals("asc")) {
                            orderString += "ASC, q.title ";
                        } else {
                            orderString += "DESC, q.title ";
                        }
                        if (tOrder.equals("desc")) {
                            orderString += "DESC, ";
                        } else {
                            orderString += "ASC, ";
                        }
                        query2 += orderString + "s.count DESC, stars.name ASC";
                        query3 += orderString + "g.name ASC";
                    } else {
                        query2 += "ORDER BY q.rating, q.title, s.count DESC, stars.name ASC";
                        query3 += "ORDER BY q.rating, q.title, g.name ASC";
                    }
                } else {
                    query2 += "ORDER BY s.movieId, s.count DESC, stars.name ASC";
                    query3 += "ORDER BY g.movieId, g.name ASC";
                }
            }

            PreparedStatement statement2 = conn.prepareStatement(query2, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            PreparedStatement statement3 = conn.prepareStatement(query3, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            int i = 1;

            if (index != null && !index.isEmpty() && !index.equals("null")) { // browse by index
                if (index.equals("*")) {
                    statement1.setString(i, "^[^0-9A-Za-z]");
                    statement2.setString(i, "^[^0-9A-Za-z]");
                    statement3.setString(i, "^[^0-9A-Za-z]");
                    i++;

                    statement1.setString(i, "^[^0-9A-Za-z]");
                    statement2.setString(i, "^[^0-9A-Za-z]");
                    statement3.setString(i, "^[^0-9A-Za-z]");
                    i++;

                    statement1.setString(i, "^[^0-9A-Za-z]");
                    statement2.setString(i, "^[^0-9A-Za-z]");
                    statement3.setString(i, "^[^0-9A-Za-z]");
                    i++;
                } else {
                    statement1.setString(i, index);
                    statement2.setString(i, index);
                    statement3.setString(i, index);
                    i++;

                    statement1.setString(i, index);
                    statement2.setString(i, index);
                    statement3.setString(i, index);
                    i++;

                    statement1.setString(i, index);
                    statement2.setString(i, index);
                    statement3.setString(i, index);
                    i++;
                }
            } else {
                if (!fulltxt.equals("null")) {
                    statement1.setString(i, query);
                    statement2.setString(i, query);
                    statement3.setString(i, query);
                    i++;
                } else if (title != null && !title.isEmpty() && !title.equals("null") && fulltxt.equals("null")) {
                    statement1.setString(i, title);
                    statement2.setString(i, title);
                    statement3.setString(i, title);
                    i++;

                    statement1.setString(i, title);
                    statement2.setString(i, title);
                    statement3.setString(i, title);
                    i++;

                    statement1.setString(i, title);
                    statement2.setString(i, title);
                    statement3.setString(i, title);
                    i++;
                } else {
                    statement1.setNull(i, java.sql.Types.VARCHAR);
                    statement2.setNull(i, java.sql.Types.VARCHAR);
                    statement3.setNull(i, java.sql.Types.VARCHAR);
                    i++;

                    statement1.setNull(i, java.sql.Types.VARCHAR);
                    statement2.setNull(i, java.sql.Types.VARCHAR);
                    statement3.setNull(i, java.sql.Types.VARCHAR);
                    i++;

                    statement1.setNull(i, java.sql.Types.VARCHAR);
                    statement2.setNull(i, java.sql.Types.VARCHAR);
                    statement3.setNull(i, java.sql.Types.VARCHAR);
                    i++;
                }
            }

            if (year != null && !year.isEmpty() && !year.equals("null")) {
                statement1.setString(i, year);
                statement2.setString(i, year);
                statement3.setString(i, year);
                i++;

                statement1.setString(i, year);
                statement2.setString(i, year);
                statement3.setString(i, year);
                i++;
            } else if (fulltxt.equals("null")) {
                statement1.setNull(i, java.sql.Types.VARCHAR);
                statement2.setNull(i, java.sql.Types.VARCHAR);
                statement3.setNull(i, java.sql.Types.VARCHAR);
                i++;

                statement1.setNull(i, java.sql.Types.VARCHAR);
                statement2.setNull(i, java.sql.Types.VARCHAR);
                statement3.setNull(i, java.sql.Types.VARCHAR);
                i++;
            }

            if (director != null && !director.isEmpty() && !director.equals("null")) {
                statement1.setString(i, director);
                statement2.setString(i, director);
                statement3.setString(i, director);
                i++;

                statement1.setString(i, director);
                statement2.setString(i, director);
                statement3.setString(i, director);
                i++;
            } else if (fulltxt.equals("null")) {
                statement1.setNull(i, java.sql.Types.VARCHAR);
                statement2.setNull(i, java.sql.Types.VARCHAR);
                statement3.setNull(i, java.sql.Types.VARCHAR);
                i++;

                statement1.setNull(i, java.sql.Types.VARCHAR);
                statement2.setNull(i, java.sql.Types.VARCHAR);
                statement3.setNull(i, java.sql.Types.VARCHAR);
                i++;
            }

            if (genre != null && !genre.isEmpty() && !genre.equals("null") && fulltxt.equals("null")) {
                statement1.setString(i, genre);
                statement2.setString(i, genre);
                statement3.setString(i, genre);
                i++;
            } else if (star != null && !star.isEmpty() && !star.equals("null") && fulltxt.equals("null")) {
                statement1.setString(i, star);
                statement2.setString(i, star);
                statement3.setString(i, star);
                i++;
            }

            // Pagination parameters
            statement1.setInt(i, mvct_num);
            statement2.setInt(i, mvct_num);
            statement3.setInt(i, mvct_num);
            i++;

            statement1.setInt(i, offset_num);
            statement2.setInt(i, offset_num);
            statement3.setInt(i, offset_num);

            ResultSet rs_movie = statement1.executeQuery();
            ResultSet rs_stars = statement2.executeQuery();
            ResultSet rs_genres = statement3.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs_movie.next()) {
                String movie_id = rs_movie.getString("id");
                String movie_title = rs_movie.getString("title");
                String movie_year = rs_movie.getString("year");
                String movie_director = rs_movie.getString("director");
                String movie_ratings = rs_movie.getString("rating");
                if (movie_ratings == null) {
                    movie_ratings = "0";
                }

                JsonArray stars = new JsonArray();

                while (rs_stars.next()) {
                    if (!rs_stars.getString("movieId").equals(movie_id)) {
                        rs_stars.previous();
                        break;
                    }

                    JsonObject stars_obj = new JsonObject();
                    String rs_star = rs_stars.getString("name");
                    String rs_starid = rs_stars.getString("id");
                    stars_obj.addProperty("star", rs_star);
                    stars_obj.addProperty("star_id", rs_starid);
                    stars.add(stars_obj);
                }

                JsonArray genres = new JsonArray();

                while (rs_genres.next()) {
                    if (!rs_genres.getString("movieId").equals(movie_id)) {
                        rs_genres.previous();
                        break;
                    }
                    JsonObject genre_obj = new JsonObject();
                    String rs_genre = rs_genres.getString("name");
                    genre_obj.addProperty("name", rs_genre);

                    genres.add(genre_obj);
                }

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_ratings", movie_ratings);
                jsonObject.addProperty("count", mvct);
                jsonObject.add("movie_genres", genres);
                jsonObject.add("movie_stars", stars);

                jsonArray.add(jsonObject);
            }
            rs_movie.close();
            rs_stars.close();
            rs_genres.close();
            statement1.close();
            statement2.close();
            statement3.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

            TJelapsedTime = System.nanoTime() - TJstartTime;

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);

        } finally {
            out.close();
            TSelapsedTime = System.nanoTime() - TSstartTime;
            String path = request.getServletContext().getRealPath("/") + "log.txt";

            try {
                File file = new File(path);
                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fw = new FileWriter(file, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fw);
                // bufferedWriter.write("TS: " + TSelapsedTime + ", TJ: " + TJelapsedTime);
                bufferedWriter.write(TSelapsedTime + "," + TJelapsedTime);
                bufferedWriter.newLine();
                bufferedWriter.close();
                // fw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Always remember to close db connection after usage. Here it's done by
        // try-with-resources

    }
}
