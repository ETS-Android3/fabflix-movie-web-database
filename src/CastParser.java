import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CastParser extends DefaultHandler{
    ArrayList<Cast> casts;
    ArrayList<Cast> castInconsistencies;

    private String tempVal;
    private String director;

    private Cast tempCast;

    public CastParser(){
        casts = new ArrayList<>();
        castInconsistencies = new ArrayList<>();
    }

    public ArrayList<Cast> getCast(){
        return casts;
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
        if (qName.equalsIgnoreCase("filmc")) {
            //create a new instance of star
            tempCast = new Cast();
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("filmc")) {
            //add it to the list
            tempCast.setDirector(director);
            if(director.contains("Unknown")){
                castInconsistencies.add(tempCast);
            }
            else{
                casts.add(tempCast);
            }
        }
        else if(qName.equals("is")){
            director = tempVal;
        }
        else if (qName.equalsIgnoreCase("t")) {
            tempCast.setTitle(tempVal);
        }
        else if (qName.equalsIgnoreCase("a")) {
            String regexPattern = "[a-zA-z]+";
            Pattern regex = Pattern.compile(regexPattern);
            Matcher match = regex.matcher(tempVal);

            if (match.find() && tempVal.length() != 0 && !tempVal.equals("s a")){
                tempCast.addStar(tempVal);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        HashMap<String, ArrayList<String>> gim = new HashMap<>();
        HashMap<String, String> startable = new HashMap<>();
        HashMap<String, Movie> movietable = new HashMap<>();
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?allowLoadLocalInfile=true", "mytestuser", "My6$Password");

        SAXParserFactory spf = SAXParserFactory.newInstance();
        CastParser castparser = new CastParser();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("casts124.xml", castparser);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        String movieQuery = "SELECT id, title, director FROM movies";
        PreparedStatement movietablestatement = conn.prepareStatement(movieQuery, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        ResultSet rs_movies = movietablestatement.executeQuery();

        while(rs_movies.next()){
            Movie m = new Movie();
            m.setId(rs_movies.getString("id"));
            m.setDirector(rs_movies.getString("director"));
            m.setTitle(rs_movies.getString("title"));
            movietable.put(m.getTitle(), m);
        }

        String starQuery = "SELECT id, name FROM stars";
        PreparedStatement startablestatement = conn.prepareStatement(starQuery, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        ResultSet rs_stars = startablestatement.executeQuery();

        while(rs_stars.next()){
            startable.put(rs_stars.getString("name"), rs_stars.getString("id"));
        }

        String insertSIM = "LOAD DATA LOCAL INFILE './src/SIMData.txt'\n" +
        "INTO TABLE stars_in_movies\n" +
        "FIELDS TERMINATED BY ','\n" +
        "LINES TERMINATED BY '$'\n" +
        "(starId, movieId)";

        for (Cast cast : castparser.getCast()){
            if(movietable.get(cast.getMovieTitle()) != null){
                Movie m = movietable.get(cast.getMovieTitle());
                if(m.getDirector().equals(cast.getMovieDirector())){
                    ArrayList<String> l = new ArrayList<>();
                    gim.put(m.getId(), l);
                    for(String star : cast.getStars()){
                        if(startable.get(star) != null){
                            l.add(startable.get(star));
                        }
                    }
                }
            }
        }

        File simf = new File("./src/SIMData.txt");
        File simincf = new File("./src/SIMDataInconsistencies.txt");
        FileWriter sfw = new FileWriter(simf);
        FileWriter infw = new FileWriter(simincf);
        for (Map.Entry<String, ArrayList<String>> elem : gim.entrySet()){
            for (String sid : elem.getValue()){
                String txt = elem.getKey() + "," + sid + "$";
                sfw.write(txt);
            }
        }

        String e = "Cast inconsistencies we were related with movies that did not have a director, meaning there was definite identity to a movie ID\n" +
                    "Casts that did not star in a movie not already in the database or stars not in the database were also left out\n" +
                    "Any special characters or delimiters marked by the xml were also replaced or skipped\n\n";

        infw.write(e);
        for (Cast cast : castparser.castInconsistencies){
            infw.write(cast.getMovieTitle() + ", " + cast.getMovieDirector() + "\n");
            for (String star : cast.getStars()){
                infw.write(star + "\n");
            }
            infw.write("\n");
        }

        sfw.close();
        infw.close();

        PreparedStatement simStatement = conn.prepareStatement(insertSIM);
        simStatement.executeQuery();
    }
}