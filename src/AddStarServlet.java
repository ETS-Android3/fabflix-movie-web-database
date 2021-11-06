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

@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add-star")
public class AddStarServlet extends HttpServlet {

    static int id_count = 1;

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

            String star_name = request.getParameter("star_name");
            String star_year = request.getParameter("new_star_year"); // can be null
            String star_id = "nm"; // random star_id for new stars ; add id count to this

            if (star_year.isEmpty()) {
                star_year = null;
            }

            PreparedStatement idQuery = conn.prepareStatement("select max(id) as maxID from stars");
            ResultSet rs = idQuery.executeQuery();
            String maxId = "0";
            while (rs.next()) {
                maxId = rs.getString("maxID");
            }

            star_id += (Integer.parseInt(maxId.substring(maxId.length() - 7)) + 1);

            String query = "INSERT INTO stars VALUES (?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, star_id);
            statement.setString(2, star_name);
            if (star_year != null) {
                statement.setInt(3, Integer.parseInt(star_year));
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }

            int rows_affected = statement.executeUpdate();

            JsonObject responseJsonObject = new JsonObject();

            if (rows_affected == 1) {
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", String.format("%s was successfully added!", star_name));

            } else {
                responseJsonObject.addProperty("status", "failed");
                responseJsonObject.addProperty("message", String.format("Could not add %s", star_name));

            }

            out.write(responseJsonObject.toString());
            statement.close();

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