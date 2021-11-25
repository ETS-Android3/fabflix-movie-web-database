import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

// server endpoint URL
@WebServlet("/movie-suggestion")
public class MovieSuggestion extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Create a dataSource which registered in web.
	private DataSource dataSource;

	public void init(ServletConfig config) {
		try {
			dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 
	 * Match the query against movies and return a JSON response.
	 * 
	 * For example, if the query is "super": The JSON response look like this: [ {
	 * "value": "Superman", "data": { "heroID": 101 } }, { "value": "Supergirl",
	 * "data": { "heroID": 113 } } ]
	 * 
	 * The format is like this because it can be directly used by the JSON auto
	 * complete library this example is using. So that you don't have to convert the
	 * format.
	 * 
	 * The response contains a list of suggestions. In each suggestion object, the
	 * "value" is the item string shown in the dropdown list, the "data" object can
	 * contain any additional information.
	 * 
	 * 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try (Connection conn = dataSource.getConnection()) {

			response.setContentType("application/json");

			PrintWriter out = response.getWriter();

			// setup the response json arrray
			JsonArray jsonArray = new JsonArray();

			// get the query string from parameter
			String query = request.getParameter("query");
			String[] queryList = query.split(" ");

			query = "";
			for (String elem : queryList) {
				query += elem;
				query += "* ";
			}

			// return the empty json array if query is null or empty
			if (query == null || query.trim().isEmpty()) {
				response.getWriter().write(jsonArray.toString());
				return;
			}

			String searchQ = "SELECT id, title from movies where match (title) against (? IN BOOLEAN MODE) LIMIT 10";

			PreparedStatement statement = conn.prepareStatement(searchQ);

			statement.setString(1, query);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				String id = rs.getString("id");
				String title = rs.getString("title");
				jsonArray.add(generateJsonObject(id, title));

			}

			response.getWriter().write(jsonArray.toString());
			rs.close();
			statement.close();

		} catch (Exception e) {
			System.out.println(e);
			response.sendError(500, e.getMessage());
		}
	}

	/*
	 * Generate the JSON Object from hero to be like this format: { "value":
	 * "Chief Zabu", "data": { "movieId": tt0094859 } }
	 * 
	 */
	private static JsonObject generateJsonObject(String movieId, String title) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", title);

		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("movieID", movieId);

		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}

}