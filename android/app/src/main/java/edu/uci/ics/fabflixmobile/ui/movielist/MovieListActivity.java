package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.SingleMovieActivity;

import java.util.ArrayList;

public class MovieListActivity extends AppCompatActivity {

    private EditText query;
    private int page = 1;
    private boolean lastPage = false;

    private final String host = "3.12.241.15";
    private final String port = "8443";
    private final String domain = "cs122b-fall21-project4-team-5";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

//    private final String host = "10.0.2.2";
//    private final String port = "8080";
//    private final String domain = "cs122b-fall21-project4-team-5";
//    private final String baseURL = "http://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        query = binding.search;
        final Button searchButton = binding.searchButton;
        final Button nextButton = binding.next;
        final Button prevButton = binding.prev;

        // assign a listener to call a function to handle the user request when clicking
        // a button
        searchButton.setOnClickListener(view -> {
            page = 1;
            doMovieList();
        });
        nextButton.setOnClickListener(view -> {
            if(!lastPage){
                page++;
                doMovieList();
            }
        });
        prevButton.setOnClickListener(view -> {
            if(page > 1){
                page--;
                doMovieList();
            }
        });

        doMovieList();
    }

    @SuppressLint("SetTextI18n")
    public void doMovieList() {

        final ArrayList<Movie> movies = new ArrayList<>();

        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movies.get(position);
            Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
            SingleMoviePage.putExtra("id", movie.getId());
            startActivity(SingleMoviePage);
        });
        // TODO: this should be retrieved from the backend server
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        String query_string = query.getText().toString();

        String search_query = "search_title=null&fulltxt=null&";

        if(query_string.equals("")){
            search_query = "search_title=null&fulltxt=null&";
        }
        else{
            search_query = "search_title=" + query_string + "&";
            search_query += "fulltxt=true&";
        }

        search_query += "mvct=20&";
        search_query += "sort=Default&";
        search_query += "page=" + Integer.toString(page);

        final StringRequest movieListRequest = new StringRequest(Request.Method.GET, baseURL + "/api/movies?" + search_query, response -> {
            try{
                JSONArray movieList = new JSONArray(response);
                if(movieList.length() < 20){
                    lastPage = true;
                }
                else{
                    lastPage = false;
                }
                for (int i = 0; i < movieList.length(); i++){
                    JSONObject movieObj = movieList.getJSONObject(i);
                    String id = movieObj.getString("movie_id");
                    String title = movieObj.getString("movie_title");
                    String year = movieObj.getString("movie_year");
                    String director = movieObj.getString("movie_director");
                    JSONArray genres = movieObj.getJSONArray("movie_genres");
                    JSONArray stars = movieObj.getJSONArray("movie_stars");

                    Movie movie = new Movie(id, title, year, director);
                    for (int j=0; j < genres.length(); j++){
                        movie.addGenre(genres.getJSONObject(j).getString("name"));
                    }
                    for (int k=0; k < Math.min(3, stars.length()); k++){
                        movie.addStar(stars.getJSONObject(k).getString("star"));
                    }
                    movies.add(movie);
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }, error -> {
            // error
            Log.d("movieList.error", error.toString());
        });

        queue.add(movieListRequest);

    }
}