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
import java.sql.ResultSetMetaData;
// import java.sql.Statement;
import java.sql.PreparedStatement;

// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {

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

        // browse features

        String genre = request.getParameter("genre");
        String index = request.getParameter("char");

        // Search Queries
        String title = request.getParameter("search_title");
        String year = request.getParameter("search_year");
        String director = request.getParameter("search_director");
        String star = request.getParameter("search_star");

        // User filter/sort
        String mvct = request.getParameter("mvct");
        String page = request.getParameter("page");
        String sort = request.getParameter("sort");
        String tOrder = request.getParameter("title_order");
        String rOrder = request.getParameter("rating_order");

        boolean sorting = false;
        if (!sort.equals("Default")) {
            sorting = true;
            sort = sort.toLowerCase();
            tOrder = tOrder.toUpperCase();
            rOrder = rOrder.toUpperCase();
        }

        int mvct_num = Integer.parseInt(mvct);
        int page_num = Integer.parseInt(page) - 1;
        int offset_num = mvct_num * page_num;
        String offset = Integer.toString(offset_num);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the
        // connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            // Statement statement1 =
            // conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            // ResultSet.CONCUR_READ_ONLY);
            // Statement statement2 =
            // conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            // ResultSet.CONCUR_READ_ONLY);
            // Statement statement3 =
            // conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            // ResultSet.CONCUR_READ_ONLY);

            String query1 = "SELECT movies.* FROM (SELECT DISTINCT movies.*, ratings.rating FROM movies LEFT OUTER JOIN ratings ON movies.id = ratings.movieId) as movies";

            if (genre != null && !genre.isEmpty() && !genre.equals("null")) {
                query1 = "SELECT movies.* FROM (" + query1 + ") as movies, genres_in_movies as gim, genres "
                        + "WHERE genres.name = " + String.format("'%s' ", genre)
                        + "AND genres.id = gim.genreId AND gim.movieId = movies.id";
            }

            else if (index != null && !index.isEmpty() && !index.equals("null")) { // browse by index
                if (index.equals("*")) {
                    // REGEX PATTERN FROM:
                    // https://stackoverflow.com/questions/1051583/fetch-rows-where-first-character-is-not-alphanumeric
                    query1 += " WHERE movies.title REGEXP '^[^0-9A-Za-z]'";
                } else {
                    query1 += " WHERE movies.title LIKE " + String.format("'%s%%'", index);
                }
            }

            else {
                boolean and = false;

                if (star != null && !star.isEmpty() && !star.equals("null")) {
                    query1 = queryGenerator(query1, star, 1);
                    and = true;
                }

                if (title != null && !title.isEmpty() && !title.equals("null")) {
                    if (and) {
                        query1 += " AND ";
                    } else {
                        query1 += " WHERE ";
                    }
                    query1 = queryGenerator(query1, title, 2);
                    and = true;
                }

                if (year != null && !year.isEmpty() && !year.equals("null")) {
                    if (and) {
                        query1 += " AND ";
                    } else {
                        query1 += " WHERE ";
                    }
                    query1 = queryGenerator(query1, year, 3);
                    and = true;
                }

                if (director != null && !director.isEmpty() && !director.equals("null")) {
                    if (and) {
                        query1 += " AND ";
                    } else {
                        query1 += " WHERE ";
                    }
                    query1 = queryGenerator(query1, director, 4);
                    and = true;
                }

            }

            if (sort != null && !sort.isEmpty() && sort.equals("title")) {
                query1 += " ORDER BY movies.title " + String.format("%s,", tOrder) + " movies.rating "
                        + String.format("%s", rOrder);
            } else if (sort != null && !sort.isEmpty() && sort.equals("rating")) {
                query1 += " ORDER BY movies.rating " + String.format("%s,", rOrder) + " movies.title "
                        + String.format("%s", tOrder);
            } else {
                query1 += " ORDER BY movies.id";
            }

            query1 += " LIMIT " + String.format("%s", mvct) + " OFFSET " + String.format("%s", offset);

            // count query from https://stackoverflow.com/a/53212180
            // String query2 = "SELECT stars.id, stars.name, s.movieId "
            //         + "FROM stars, (SELECT stars_in_movies.*, counter.count FROM stars_in_movies "
            //         + "LEFT JOIN (SELECT starId, COUNT(starId) as count FROM stars_in_movies GROUP BY starId) as counter "
            //         + "ON counter.starId = stars_in_movies.starId) as s, " + "(" + query1 + ") as q "
            //         + "WHERE q.id = s.movieId AND stars.id = s.starId "
            //         + "ORDER BY s.movieId, s.count DESC, stars.name ASC";
            
            String query2 = "SELECT stars.id, stars.name, s.movieId "
                    + "FROM stars, (SELECT sim.*, counter.count FROM (SELECT stars_in_movies.* FROM stars_in_movies, (" + query1 + ") as q "
                    + "WHERE stars_in_movies.movieId = q.id) as sim "
                    + "LEFT JOIN (SELECT starId, COUNT(starId) as count FROM stars_in_movies GROUP BY starId) as counter "
                    + "ON counter.starId = sim.starId) as s "
                    + "WHERE stars.id = s.starId "
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
                        String orderString = "ORDER BY q.title " + String.format("%s, ", tOrder) + "q.rating "
                                + String.format("%s, ", rOrder);
                        query2 += orderString + "s.count DESC, stars.name ASC";
                        query3 += orderString + "g.name ASC";
                    } else {
                        query2 += "ORDER BY q.title, q.rating, s.count DESC, stars.name ASC";
                        query3 += "ORDER BY q.title, q.rating, g.name ASC";
                    }
                } else if (!sort.isEmpty() && sort.equals("rating")) {
                    if (sorting) {
                        String orderString = " ORDER BY q.rating " + String.format("%s, ", rOrder) + "q.title "
                                + String.format("%s, ", tOrder);
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

            // Perform the query
            // ResultSet rs_movie = statement1.executeQuery(query1);
            // ResultSet rs_stars = statement2.executeQuery(query2);
            // ResultSet rs_genres = statement3.executeQuery(query3);

            PreparedStatement statement1 = conn.prepareStatement(query1, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            PreparedStatement statement2 = conn.prepareStatement(query2, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            PreparedStatement statement3 = conn.prepareStatement(query3, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

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
                    // ResultSetMetaData debugdata = rs_genres.getMetaData();
                    // String debuggggg = debugdata.getColumnName(2);
                    if (!rs_genres.getString("movieId").equals(movie_id)) {
                        rs_genres.previous();
                        break;
                    }

                    String rs_genre = rs_genres.getString("name");
                    genres.add(rs_genre);
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

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by
        // try-with-resources

    }

    private String queryGenerator(String query, String search, int type) {
        String tail = "";
        switch (type) {
            case 1: // "SELECT movies.*, ratings.rating FROM movies, ratings WHERE ratings.movieId =
                    // movies.id";
                String newQuery = "SELECT DISTINCT movies.* FROM (SELECT movies.*, ratings.rating FROM movies LEFT OUTER JOIN ratings ON movies.id = ratings.movieId) as movies, stars_in_movies as sim, stars "
                        + "WHERE stars.name LIKE " + String.format("'%%%s%%' ", search)
                        + "AND stars.id = sim.starId AND sim.movieId = movies.id";

                return newQuery;
            case 2:
                tail = "movies.title LIKE " + String.format("'%%%s%%'", search);
                break;
            case 3:
                tail = "movies.year LIKE " + String.format("%s", search);
                break;
            case 4:
                tail = "movies.director LIKE " + String.format("'%%%s%%'", search);
                break;
        }

        return query + tail;
    }
}