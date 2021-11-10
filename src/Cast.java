import java.util.ArrayList;

public class Cast {
    public ArrayList<String> stars;
    private String movie_id;
    private String movie_title;
    private String movie_director;


    public Cast(){
        stars = new ArrayList<String>();
    }

    public String getMovieTitle() {return movie_title;}
    public String getMovieDirector() {return movie_director;}
    public String getMovieId() {return movie_id;}
    public ArrayList<String> getStars() {return stars;}

    public void setMovieId(String id) {
        this.movie_id = id;
    }

    public void setTitle(String title) {
        this.movie_title = title;
    }

    public void setDirector(String director) {
        this.movie_director = director;
    }

    public void addStar(String star) {
        stars.add(star);
    }
}
