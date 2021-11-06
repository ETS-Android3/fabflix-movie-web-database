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

public class CastParser extends DefaultHandler{
    ArrayList<Cast> casts;

    private String tempVal;

    private Cast tempCast;

    public CastParser(){
        casts = new ArrayList<>();
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
            casts.add(tempCast);
        }
        else if (qName.equalsIgnoreCase("t")) {
            tempCast.setTitle(tempVal);
        }
        else if (qName.equalsIgnoreCase("a")) {
            String regexPattern = "[a-zA-z]+";
            Pattern regex = Pattern.compile(regexPattern);
            Matcher match = regex.matcher(tempVal);

            if (match.find() && tempVal.length() != 0){
                tempCast.addStar(tempVal);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        HashMap<String, String> startable = new HashMap<>();
        HashMap<String, String> movietable = new HashMap<>();
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "root", "Pwner419123@");

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

        String query = "SELECT id, title FROM movies";
        PreparedStatement movietablestatement = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        ResultSet rs_movies = movietablestatement.executeQuery();

        while(rs_movies.next()){
            movietable.put(rs_movies.getString("title"), rs_movies.getString("id"));
        }

        ArrayList<Cast> ml = castparser.getCast();
        Cast m = castparser.getCast().get(1);
        System.out.println(m);

    }
}