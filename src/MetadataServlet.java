import com.google.gson.JsonObject;
import com.mysql.cj.jdbc.DatabaseMetaData;
import com.mysql.cj.x.protobuf.MysqlxPrepare.Prepare;

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
        response.setContentType("text/html");

        try (Connection conn = dataSource.getConnection()) {
            // TO-DO : get metadata
            DatabaseMetaData dbData = conn.getMetaData();

            ResultSet rs = dbData.getTables(null, null, null, new String[] { "TABLE" });

            while (rs.next()) {
                String table = rs.getString("TABLE_NAME");

                JsonObject tableJson = new JsonObject();
                tableJson.addProperty("table_name", table);
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
