import java.util.ArrayList;

public class Cast {
    public ArrayList<String> stars;
    private int movie_id;
    private String movie_title;


    public Cast(){
        stars = new ArrayList<String>();
    }

    public String getMovieTitle() {return movie_title;}
    public int getMovieId() {return movie_id;}
    public ArrayList<String> getStars() {return stars;}

    public void setMovieId(int id) {
        this.movie_id = id;
    }

    public void setTitle(String title) {
        this.movie_title = title;
    }

    public void addStar(String star) {
        stars.add(star);
    }
}
