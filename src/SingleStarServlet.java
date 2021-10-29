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

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
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
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query1 = "SELECT movies.* FROM movies, stars, stars_in_movies as sim " +
                            "WHERE stars.id = ? " +
                            "AND stars.id = sim.starId AND sim.movieId = movies.id " +
                            "ORDER BY movies.year DESC, movies.title ASC";

            String query2 = "SELECT * FROM stars WHERE stars.id = ?";
            // Declare our statement
            PreparedStatement statement1 = conn.prepareStatement(query1);
            PreparedStatement statement2 = conn.prepareStatement(query2);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement1.setString(1, id);
            statement2.setString(1, id);

            // Perform the query
            ResultSet rs_movie = statement1.executeQuery();
            ResultSet rs_star = statement2.executeQuery();

            // Iterate through each row of rs
            rs_star.next();
            String starId = rs_star.getString("id");
            String starName = rs_star.getString("name");
            String starDob = rs_star.getString("birthYear");
            if(starDob == null){
                starDob = "N/A";
            }

            JsonArray movies = new JsonArray();

            while (rs_movie.next()) {
                String movieId = rs_movie.getString("id");
                String movieTitle = rs_movie.getString("title");
                String movieYear = rs_movie.getString("year");
                String movieDirector = rs_movie.getString("director");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject movie = new JsonObject();

                movie.addProperty("movie_id", movieId);
                movie.addProperty("movie_title", movieTitle);
                movie.addProperty("movie_year", movieYear);
                movie.addProperty("movie_director", movieDirector);

                movies.add(movie);
            }

            JsonObject star = new JsonObject();

            star.addProperty("star_id", starId);
            star.addProperty("star_name", starName);
            star.addProperty("star_dob", starDob);
            star.add("movies", movies);

            rs_movie.close();
            rs_star.close();
            statement1.close();
            statement2.close();

            // Write JSON string to output
            out.write(star.toString());
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

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
