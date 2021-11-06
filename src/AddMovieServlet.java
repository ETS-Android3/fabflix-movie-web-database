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
import java.sql.*;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet extends HttpServlet {

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
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            // // Declare our statement
            // Statement statement = conn.createStatement();

            String movie_title = request.getParameter("movie-title");
            String movie_director = request.getParameter("movie-director");
            String movie_year = request.getParameter("movie-year");
            String movie_star = request.getParameter("mstar_name");
            String movie_genre = request.getParameter("movie-genre");

            String query = "call add_movie(?,?,?,?,?,?)";

            CallableStatement cstm = conn.prepareCall(query);
            cstm.setString(1, movie_title);
            cstm.setInt(2, Integer.parseInt(movie_year));
            cstm.setString(3, movie_director);
            cstm.setString(4, movie_star);
            cstm.setString(5, movie_genre);
            cstm.registerOutParameter(6, Types.VARCHAR);
            cstm.execute();

            String msg = cstm.getString(6);
            JsonObject responseJsonObj = new JsonObject();
            if (!msg.isEmpty() && msg != null) {
                responseJsonObj.addProperty("message", msg);
                out.write(responseJsonObj.toString());
            }


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

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}