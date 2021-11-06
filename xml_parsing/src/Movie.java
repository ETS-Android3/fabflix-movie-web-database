import java.util.ArrayList;

public class Movie {
    private String id;
    private String title;
    private int year;
    private String director;
    public ArrayList<String> genres;

    public Movie(){
        genres = new ArrayList<String>();
    }

    public String getId() {return id;}
    public String getTitle() {return title;}
    public int getYear() {return year;}
    public String getDirector() {return director;}
    public ArrayList<String> getGenres() {return genres;}

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void addGenre(String genre) {
        genres.add(genre);
    }
}
