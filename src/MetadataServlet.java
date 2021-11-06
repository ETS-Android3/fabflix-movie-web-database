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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

@WebServlet(name = "MetadataServlet", urlPatterns = "/metadata")
public class MetadataServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String tableType = request.getParameter("type");

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            // TO-DO : get metadata
            DatabaseMetaData dbData = conn.getMetaData();

            ResultSet rs;

            if (tableType != null) {
                rs = dbData.getColumns("moviedb", null, tableType, null);

                JsonArray columns = new JsonArray();
                columns.add(tableType); // first index will hold table_name!
                while (rs.next()) {
                    JsonObject jsonObj = new JsonObject();
                    String column = rs.getString("COLUMN_NAME");
                    String columnType = rs.getString("TYPE_NAME");
                    jsonObj.addProperty("column", column);
                    jsonObj.addProperty("type", columnType);
                    columns.add(jsonObj);
                }

                out.write(columns.toString());
            }

            else {
                rs = dbData.getTables("moviedb", null, null, null);

                JsonArray tables = new JsonArray();
                while (rs.next()) {
                    String table = rs.getString("TABLE_NAME");
                    tables.add(table);
                }

                out.write(tables.toString());
            }

            rs.close();

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            // write to output
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
