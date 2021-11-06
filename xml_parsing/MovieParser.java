import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

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

    private String tempVal;

    private Movie tempMovie;

    public MovieParser(){
        movies = new ArrayList<>();
    }

    public ArrayList<Movie> getMovies(){
        return movies;
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
            movies.add(tempMovie);
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
        else if (qName.equalsIgnoreCase("dirn")) {
            tempMovie.setDirector(tempVal);
        }
        else if (qName.equalsIgnoreCase("cat")){
            tempMovie.genres.add(tempVal);
        }

    }

    public static void main(String[] args) throws Exception {
        HashMap<String, String> genretable = new HashMap<>();
        HashMap<String, String> movietable = new HashMap<>();
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "root", "Pwner419123@");

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

        String query = "SELECT id, title FROM movies";
        PreparedStatement movietablestatement = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        ResultSet rs_movies = movietablestatement.executeQuery();

        while(rs_movies.next()){
            movietable.put(rs_movies.getString("title"), rs_movies.getString("id"));
        }

        Movie m = movieparser.getMovies().get(33);
        System.out.println(m);
    }
}