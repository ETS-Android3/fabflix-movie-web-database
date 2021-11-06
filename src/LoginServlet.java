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

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

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
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "fail");
            jsonObject.addProperty("message", "reCAPTCHA verification failed");
            request.getServletContext().log("reCAPTCHA failed");
            response.getWriter().write(jsonObject.toString());

            return;
        }

        try (Connection conn = dataSource.getConnection()) {

            // // Declare our statement
            // Statement statement = conn.createStatement();

            String email = request.getParameter("email");
            String pswd = request.getParameter("password");

            String query = "SELECT email, password from customers WHERE email = ? ";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();

            // Login success:
            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                if (new StrongPasswordEncryptor().checkPassword(pswd, encryptedPassword)) {
                    // Create a JsonObject based on the data we retrieve from rs
                    // JsonObject responseJsonObject = new JsonObject();

                    // set this user into the session
                    request.getSession().setAttribute("user", new User(email, pswd));

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");

                    response.getWriter().write(responseJsonObject.toString());
                }

                else { // wrong password
                       // JsonObject responseJsonObject = new JsonObject();
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Login failed");
                    responseJsonObject.addProperty("message", "Password is incorrect.");

                    response.getWriter().write(responseJsonObject.toString());
                }

            } else {
                // Login fail
                // JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", "User (" + email + ") doesn't exist");
                response.getWriter().write(responseJsonObject.toString());
                // sample error messages. in practice, it is not a good idea to tell user which
                // one is incorrect/not exist.
                // if (!email.equals(user_email)) {
                // responseJsonObject.addProperty("message", "user " + email + " doesn't
                // exist");
                // } else {
                // responseJsonObject.addProperty("message", "incorrect password");
                // }
            }

            statement.close();
            rs.close();

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

}