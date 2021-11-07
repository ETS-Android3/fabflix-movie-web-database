import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MovieParser extends DefaultHandler{
    ArrayList<Movie> movies;
    ArrayList<Movie> moviesInconsistencies;
    HashMap<String, String> genreNameMap;

    private String tempVal;

    private Movie tempMovie;
    private String dirN;

    public MovieParser(){
        movies = new ArrayList<>();
        moviesInconsistencies = new ArrayList<>();
        genreNameMap = new HashMap<>();

        genreNameMap.put("comd", "Comedy");
        genreNameMap.put("epic", "Epic");
        genreNameMap.put("myst", "Mystery");
        genreNameMap.put("susp", "Thriller");
        genreNameMap.put("sxfi", "Sci-Fi");
        genreNameMap.put("axtn", "Comedy");
        genreNameMap.put("scfi", "Sci-Fi");
        genreNameMap.put("dram", "Drama");
        genreNameMap.put("bio", "Biography");
        genreNameMap.put("cnr", "Cops and Robbers");
        genreNameMap.put("west", "Western");
        genreNameMap.put("biop", "Biography");
        genreNameMap.put("docu", "Documentary");
        genreNameMap.put("porn", "Pornography");
        genreNameMap.put("tv", "TV");
        genreNameMap.put("noir", "Noir");
        genreNameMap.put("musc", "Musical");
        genreNameMap.put("horr", "Horror");
        genreNameMap.put("advt", "Adventure");
        genreNameMap.put("romt", "Romance");
        genreNameMap.put("s.f.", "Sci-Fi");
        genreNameMap.put("muscl", "Musical");
        genreNameMap.put("draam", "Drama");
        genreNameMap.put("dramd", "Drama");
    }

    public ArrayList<Movie> getMovies(){
        return movies;
    }

    public ArrayList<Movie> getMovieInconsistencies(){
        return moviesInconsistencies;
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException{
        tempVal = new String(ch, start, length);
        tempVal = tempVal.replace("~", " ");
        tempVal = tempVal.replace("+", "");
        tempVal = tempVal.replace("[1]", "");
        tempVal = tempVal.trim();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of star
            tempMovie = new Movie();
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("film")) {
            //add it to the list
            tempMovie.setDirector(dirN);
            if(tempMovie.genres.size() == 0 || tempMovie.getYear() == 0 || dirN.contains("Unknown")){
                moviesInconsistencies.add(tempMovie);
            }
            else{
                movies.add(tempMovie);
            }
        }
        else if (qName.equalsIgnoreCase("t")) {
            if (tempVal.length() != 0){
                tempMovie.setTitle(tempVal);
            }
        }
        else if (qName.equalsIgnoreCase("year")) {
            String regexPattern = "[0-9]{4}";
            Pattern regex = Pattern.compile(regexPattern);
            Matcher match = regex.matcher(tempVal);
            if (match.find() && tempVal.length() != 0){
                tempMovie.setYear(Integer.parseInt(tempVal));
            }
        }
        else if (qName.equalsIgnoreCase("dirname")) {
            dirN = tempVal;
        }
        else if (qName.equalsIgnoreCase("cat")){
            tempVal = tempVal.toLowerCase();
            String[] g = tempVal.split(" ");
            for(int i=0; i < g.length; ++i){
                if(genreNameMap.get(g[i]) != null){
                    tempMovie.genres.add(genreNameMap.get(g[i]));
                }
            }
        }

    }

    public static void main(String[] args) throws Exception {
        HashMap<String, Integer> genretable = new HashMap<>();
        HashMap<String, HashMap<String, Movie>> moviemap = new HashMap<>();
        ArrayList<Movie> insertedMovies = new ArrayList<>();
        HashMap<Integer, ArrayList<String>> insertedGenresInMovies = new HashMap<>();
        Set<String> genreSet = new HashSet<>();
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?allowLoadLocalInfile=true", "mytestuser", "My6$Password");

        SAXParserFactory spf = SAXParserFactory.newInstance();
        MovieParser movieparser = new MovieParser();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("mains243.xml", movieparser);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        String movieQuery = "SELECT * FROM movies";
        PreparedStatement movietablestatement = conn.prepareStatement(movieQuery, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        ResultSet rs_movies = movietablestatement.executeQuery();

        while(rs_movies.next()){
            Movie m = new Movie();
            m.setId(rs_movies.getString("id"));
            m.setTitle(rs_movies.getString("title"));
            m.setDirector(rs_movies.getString("director"));
            m.setYear(rs_movies.getInt("year"));
            if(moviemap.get(rs_movies.getString("title")) == null){
                HashMap<String, Movie> hm = new HashMap<>();
                hm.put(rs_movies.getString("id"), m);
                moviemap.put(rs_movies.getString("title"), hm);
            }
            else{
                moviemap.get(rs_movies.getString("title")).put(rs_movies.getString("id"), m);
            }
        }

        for(String genre : movieparser.genreNameMap.values()){
            genreSet.add(genre);
        }

        String insertGenres = "LOAD DATA LOCAL INFILE './xml_parsing/data/GenreData.txt'\n" +
        "INTO TABLE genres\n" +
        "FIELDS TERMINATED BY ','\n" +
        "LINES TERMINATED BY '$'\n" +
        "(name);";

        File genre_f = new File("./xml_parsing/data/GenreData.txt");
        FileWriter gfw = new FileWriter(genre_f);
        for(String genre : genreSet){
            String txt = genre + "$";
            gfw.write(txt);
        }

        gfw.close();

        PreparedStatement genreStatement = conn.prepareStatement(insertGenres);
        genreStatement.executeQuery();
        genreStatement = conn.prepareStatement("select * from genres");
        ResultSet g = genreStatement.executeQuery();

        while(g.next()){
            genretable.put(g.getString("name"), g.getInt("id"));
        }

        // SETTING MOVIE ID FROM MAX ID
        String idQuery = "select max(id) as maxID from movies";
        PreparedStatement movieIds = conn.prepareStatement(idQuery, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        String maxId = "0";
        Integer tempId = 0;
        
        ResultSet rs_ids = movieIds.executeQuery();
        while(rs_ids.next()){
            maxId = rs_ids.getString("maxID");
            tempId = (Integer.parseInt(maxId.substring(maxId.length() - 7)) + 1);
        }

        String insertMovies = "LOAD DATA LOCAL INFILE './xml_parsing/data/MovieData.txt'\n" +
        "INTO TABLE movies\n" +
        "FIELDS TERMINATED BY '*'\n" +
        "LINES TERMINATED BY '$'\n" +
        "(id, title, year, director);";

        String insertGenresinMoviesString = "LOAD DATA LOCAL INFILE './xml_parsing/data/GIMData.txt'\n" +
        "INTO TABLE genres_in_movies\n" +
        "FIELDS TERMINATED BY ','\n" +
        "LINES TERMINATED BY '$'\n" +
        "(genreId, movieId);";

        for(Movie mov : movieparser.getMovies()){
            boolean dupe = false;
            if(moviemap.get(mov.getTitle()) != null){
                for (Map.Entry<String, Movie> elem : moviemap.get(mov.getTitle()).entrySet()){
                    Movie m = elem.getValue();
                    if(mov.getYear() == m.getYear() && mov.getDirector().equals(m.getDirector())){
                        dupe = true;
                    }
                }
            }
            if(!dupe){
                String movie_id = "tt";
                movie_id += (tempId);
                mov.setId(movie_id);
                insertedMovies.add(mov);
                tempId++;
    
                // Add genres in movie
                for(String genre : mov.genres){
                    Integer mappedG = genretable.get(genre);
                    if(insertedGenresInMovies.get(mappedG) == null){
                        ArrayList<String> l = new ArrayList<>();
                        l.add(mov.getId());
                        insertedGenresInMovies.put(mappedG, l);
                    }
                    else{
                        insertedGenresInMovies.get(mappedG).add(mov.getId());
                    }
                }
            }
        }

        File movie_f = new File("./xml_parsing/data/MovieData.txt");
        File gim_f = new File("./xml_parsing/data/GIMData.txt");
        FileWriter mfw = new FileWriter(movie_f);
        FileWriter gimfw = new FileWriter(gim_f);
        for (Movie movie : insertedMovies){
            String txt = movie.getId() + "*" + movie.getTitle() + "*" + movie.getYear() + "*" + movie.getDirector();
            txt += "$";
            mfw.write(txt);
        }

        for (Map.Entry<Integer, ArrayList<String>> elem : insertedGenresInMovies.entrySet()){
            for(String mid : elem.getValue()){
                String txt = elem.getKey() + "," + mid + "$";
                gimfw.write(txt);
            }
        }

        File in = new File("./xml_parsing/data/MovieDataInconsistencies.txt");
        FileWriter fw = new FileWriter(in);
        fw.write("Movie inconsistencies were related with directors that were:\nunknown, no publishing year, no title or was not categorized under stanford's category references:\nhttp://infolab.stanford.edu/pub/movies/doc.html#CATS\n\n");
        for(Movie m : movieparser.getMovieInconsistencies()){
            String txt = m.getTitle() + ", " + m.getYear() + ", " + m.getDirector() + "\n";
            fw.write(txt);
        }

        mfw.close();
        gimfw.close();
        fw.close();

        PreparedStatement movieStatement = conn.prepareStatement(insertMovies);
        PreparedStatement gimStatement = conn.prepareStatement(insertGenresinMoviesString);
        movieStatement.executeQuery();
        gimStatement.executeQuery();
    }
}