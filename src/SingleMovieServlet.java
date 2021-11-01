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
import java.sql.PreparedStatement;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
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

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the
        // connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query1 = "SELECT movies.*, ratings.rating FROM movies LEFT OUTER JOIN ratings ON movies.id = ratings.movieId WHERE movies.id = ?";

            String query2 = "SELECT stars.id, stars.name, s.movieId "
                    + "FROM stars, (SELECT stars_in_movies.*, counter.count FROM stars_in_movies "
                    + "LEFT JOIN (SELECT starId, COUNT(starId) as count FROM stars_in_movies GROUP BY starId) as counter "
                    + "ON counter.starId = stars_in_movies.starId) as s, " + "(" + query1 + ") as q "
                    + "WHERE q.id = s.movieId AND stars.id = s.starId "
                    + "ORDER BY s.movieId, s.count DESC, stars.name ASC";

            String query3 = "SELECT genres.name, g.movieId " + "FROM genres, genres_in_movies as g, " + "(" + query1
                    + ") as q " + "WHERE q.id = g.movieId AND g.genreId = genres.id "
                    + "ORDER BY g.movieId, genres.name ASC";

            // Declare our statement
            PreparedStatement statement1 = conn.prepareStatement(query1);
            PreparedStatement statement2 = conn.prepareStatement(query2);
            PreparedStatement statement3 = conn.prepareStatement(query3);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement1.setString(1, id);
            statement2.setString(1, id);
            statement3.setString(1, id);

            // Perform the query
            ResultSet rs_movie = statement1.executeQuery();
            ResultSet rs_stars = statement2.executeQuery();
            ResultSet rs_genres = statement3.executeQuery();

            rs_movie.next();
            String movieId = rs_movie.getString("id");
            String movieTitle = rs_movie.getString("title");
            String movieYear = rs_movie.getString("year");
            String movieDirector = rs_movie.getString("director");
            String movieRating = rs_movie.getString("rating");
            if (movieRating == null) {
                movieRating = "0";
            }

            // Iterate through each row of rs
            JsonArray stars = new JsonArray();

            while (rs_stars.next()) {
                JsonObject stars_obj = new JsonObject();
                String rs_star = rs_stars.getString("name");
                String rs_starid = rs_stars.getString("id");
                stars_obj.addProperty("star", rs_star);
                stars_obj.addProperty("star_id", rs_starid);
                stars.add(stars_obj);
            }

            JsonArray genres = new JsonArray();

            while (rs_genres.next()) {
                String rs_genre = rs_genres.getString("name");
                genres.add(rs_genre);
            }

            // Create a JsonObject based on the data we retrieve from rs

            JsonObject movieObj = new JsonObject();
            movieObj.addProperty("movie_id", movieId);
            movieObj.addProperty("movie_title", movieTitle);
            movieObj.addProperty("movie_year", movieYear);
            movieObj.addProperty("movie_director", movieDirector);
            movieObj.addProperty("movie_rating", movieRating);
            movieObj.add("movie_stars", stars);
            movieObj.add("movie_genres", genres);

            rs_movie.close();
            rs_stars.close();
            rs_genres.close();
            statement1.close();
            statement2.close();
            statement3.close();

            // Write JSON string to output
            out.write(movieObj.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by
        // try-with-resources

    }

}
