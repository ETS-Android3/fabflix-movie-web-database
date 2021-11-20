package edu.uci.ics.fabflixmobile.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
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
import edu.uci.ics.fabflixmobile.databinding.SinglemovieActivityBinding;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

public class SingleMovieActivity extends AppCompatActivity{

    private TextView single_title;
    private TextView single_year;
    private TextView single_director;
    private TextView single_genres;
    private TextView single_stars;

    private final String host = "3.12.241.15";
    private final String port = "8443";
    private final String domain = "cs122b-fall21-project4-team-5";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

//    private final String host = "10.0.2.2";
//    private final String port = "8080";
//    private final String domain = "cs122b-fall21-project4-team-5";
//    private final String baseURL = "http://" + host + ":" + port + "/" + domain;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SinglemovieActivityBinding binding = SinglemovieActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        single_title = binding.singleTitle;
        single_year = binding.singleYear;
        single_director = binding.singleDirector;
        single_genres = binding.singleGenres;
        single_stars = binding.singleStars;

        final Button homeButton = binding.homeButton;

        homeButton.setOnClickListener(view -> {
            finish();
            // initialize the activity(page)/destination
            Intent MovieListPage = new Intent(SingleMovieActivity.this, MovieListActivity.class);
            // activate the list page.
            startActivity(MovieListPage);
        });

        Bundle bundle = getIntent().getExtras();
        String movie_id = bundle.getString("id");

        String movie_url = baseURL + "/api/single-movie?id=" + movie_id;

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest singleMovieRequest = new StringRequest(Request.Method.GET, movie_url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String id = jsonObject.getString("movie_id");
                String title = jsonObject.getString("movie_title");
                String year = jsonObject.getString("movie_year");
                String director = jsonObject.getString("movie_director");

                JSONArray stars = jsonObject.getJSONArray("movie_stars");
                JSONArray genres = jsonObject.getJSONArray("movie_genres");

                Movie movie = new Movie(id, title, year, director);
                for (int j = 0; j < genres.length(); j++) {
                    movie.addGenre(genres.getJSONObject(j).getString("name"));
                }
                for (int k = 0; k < stars.length(); k++) {
                    movie.addStar(stars.getJSONObject(k).getString("star"));
                }

                String stars_list = String.join(", ", movie.getStars());
                String genres_list = String.join(", ", movie.getGenres());

                single_title.setText(title + " (" + year + ")");
                single_director.setText("Director: " + director);
                single_genres.setText("Genres: " + genres_list);
                single_stars.setText("Stars: " + stars_list);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> {
            // error
            Log.d("singleMovie.error", error.toString());
        });

        queue.add(singleMovieRequest);
    }
}
