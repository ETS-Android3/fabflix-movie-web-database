package edu.uci.ics.fabflixmobile.data.model;

import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String id;
    private final String name;
    private final String year;
    private final String director;
    private final ArrayList<String> genres;
    private final ArrayList<String> stars;



    public Movie(String id, String name, String year, String director) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.director = director;
        this.genres = new ArrayList<>();
        this.stars = new ArrayList<>();


    }
    public String getId() { return id; }
    public String getName() {
        return name;
    }
    public String getYear() {
        return year;
    }
    public String getDirector() {
        return director;
    }
    public ArrayList<String> getGenres() { return genres; }
    public ArrayList<String> getStars() { return stars; }
    public void addStar(String star) {stars.add(star);}
    public void addGenre(String genre) {genres.add(genre);}
}