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

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String query =  "SELECT DISTINCT movies.*, s.name as star, s.id as sId, g.name as genre, ratings.rating\n" +
                            "FROM movies, \n" +
                            "ratings,\n" +
                            "(SELECT stars.*, sim.movieId as smId\n" +
                            "FROM stars, stars_in_movies as sim\n" +
                            "WHERE stars.id = sim.starId) as s,\n" +
                            "(SELECT genres.*, gim.movieId as gmId\n" +
                            "FROM genres, genres_in_movies as gim\n" +
                            "WHERE genres.id = gim.genreId) as g\n" +
                            "WHERE ratings.movieId = movies.id AND smID = movies.id AND gmId = movies.id AND ";

            if(genre != null){
                query += String.format("g.name = '%s' ORDER BY rating DESC ", genre);
            }

            else if (index != null) { // browse by index
                if (index.equals("*")){
                    // REGEX PATTERN FROM:
                    // https://stackoverflow.com/questions/1051583/fetch-rows-where-first-character-is-not-alphanumeric
                    query += "movies.title REGEXP '^[^0-9A-Za-z]' ORDER BY rating DESC";
                }
                else{
                    query += "movies.title LIKE " + String.format("'%s%%' ORDER BY rating DESC", index);
                }
            }

            else{
                int and = 0;

                if(!title.isEmpty())
                {
                    query = queryGenerator(query, title, 1);
                    and++;
                }

                if (!year.isEmpty()){
                    if(and > 0){
                        query += " AND ";
                    }
                    query = queryGenerator(query, year, 2);
                    and++;
                }

                if (!director.isEmpty()){
                    if(and > 0){
                        query += " AND ";
                    }
                    query = queryGenerator(query, director, 3);
                    and++;
                }

                if (!star.isEmpty()){
                    if(and > 0){
                        query += " AND ";
                    }
                    query = queryGenerator(query, star, 4);
                }
            }

//            query += String.format("LIMIT %s", mvct);


            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_genres = rs.getString("genre");
                String movie_stars = rs.getString("star");
                String star_id = rs.getString("sId");
                String movie_ratings = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.addProperty("movie_stars", movie_stars);
                jsonObject.addProperty("star_id", star_id);
                jsonObject.addProperty("movie_ratings", movie_ratings);


                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

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
            case 1:
                tail = "movies.title LIKE " + String.format("'%%%s%%'", search);
                break;
            case 2:
                tail = "movies.year LIKE " + String.format("%s", search);
                break;
            case 3:
                tail = "movies.director LIKE " + String.format("'%%%s%%'", search);
                break;
            case 4:
                tail = "s.name LIKE " + String.format("'%%%s%%'", search);
                break;
        }

        return query + tail;
    }
}
