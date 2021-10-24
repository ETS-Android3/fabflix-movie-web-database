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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
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

        int mvct_num = Integer.parseInt(mvct);
        int page_num = Integer.parseInt(page) - 1;
        int offset_num = mvct_num * page_num;
        String offset = Integer.toString(offset_num);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement1 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Statement statement2 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Statement statement3 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            String query1 = "SELECT movies.*, ratings.rating FROM movies, ratings WHERE ratings.movieId = movies.id";                   // ONLY MOVIES AND RATING

            if(genre != null && !genre.isEmpty() && !genre.equals("null")){
                query1 =    "SELECT movies.*, ratings.rating " +
                            "FROM movies, ratings, genres_in_movies as gim, genres " +
                            "WHERE genres.name = " +
                            String.format("'%s' ", genre) +
                            "AND genres.id = gim.genreId AND gim.movieId = movies.id AND ratings.movieId = movies.id";
            }

            else if (index != null && !index.isEmpty() && !index.equals("null")) { // browse by index
                if (index.equals("*")){
                    // REGEX PATTERN FROM:
                    // https://stackoverflow.com/questions/1051583/fetch-rows-where-first-character-is-not-alphanumeric
                    query1 +=   " AND movies.title REGEXP '^[^0-9A-Za-z]'";
                }
                else{
                    query1 +=   " AND movies.title LIKE " + String.format("'%s%%'", index);
                }
            }
            
            else{
                
                if (star != null && !star.isEmpty() && !star.equals("null")){
                    query1 = queryGenerator(query1, star, 1);
                }

                if(title != null && !title.isEmpty() && !title.equals("null"))
                {
                    query1 += " AND ";
                    query1 = queryGenerator(query1, title, 2);
                }
                
                if (year != null && !year.isEmpty() && !year.equals("null")){
                    query1 += " AND ";
                    query1 = queryGenerator(query1, year, 3);
                }
                
                if (director != null && !director.isEmpty() && !director.equals("null")){
                    query1 += " AND ";
                    query1 = queryGenerator(query1, director, 4);
                }
                
            }
            query1 +=   " ORDER BY movies.id" +
                        " LIMIT " +
                        String.format("%s", mvct) +
                        " OFFSET " +
                        String.format("%s", offset);

            String query2 =    "SELECT stars.*, sim.* " +
                        "FROM stars, stars_in_movies as sim, " +
                        "(" + query1 + ") as q " +
                        "WHERE q.id = sim.movieId AND stars.id = sim.starId" +
                        " ORDER BY sim.movieId";
            
            String query3 =    "SELECT genres.*, gim.movieId as gmId " +
                        "FROM genres, genres_in_movies as gim, " +
                        "(" + query1 + ") as q " +
                        "WHERE q.id = gim.movieId AND gim.genreId = genres.id" +
                        " ORDER BY gmId, genres.name ASC";
            

            // Perform the query
            ResultSet rs_movie = statement1.executeQuery(query1);
            ResultSet rs_stars = statement2.executeQuery(query2);
            ResultSet rs_genres = statement3.executeQuery(query3);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while(rs_movie.next()){
                String movie_id = rs_movie.getString("id");
                String movie_title = rs_movie.getString("title");
                String movie_year = rs_movie.getString("year");
                String movie_director = rs_movie.getString("director");
                String movie_ratings = rs_movie.getString("rating");
                
                JsonArray stars = new JsonArray();
                
                while(rs_stars.next()){
                    if(!rs_stars.getString("sim.movieId").equals(movie_id)){
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

                while(rs_genres.next()){
                    if(!rs_genres.getString("gmId").equals(movie_id)){
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
                jsonObject.add("movie_genres", genres);
                jsonObject.add("movie_stars", stars);
                jsonObject.addProperty("movie_ratings", movie_ratings);
                jsonObject.addProperty("count",mvct);
                
                
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

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

    private String queryGenerator(String query, String search, int type){
        String tail = "";
        switch(type){
            case 1:                     // "SELECT movies.*, ratings.rating FROM movies, ratings WHERE ratings.movieId = movies.id"; 
                String newQuery =   "SELECT movies.*, ratings.rating " +
                                    "FROM movies, ratings, stars_in_movies as sim, stars " +
                                    "WHERE stars.name LIKE " +
                                    String.format("'%%%s%%' ", search) +
                                    "AND stars.id = sim.starId AND sim.movieId = movies.id AND ratings.movieId = movies.id";
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
