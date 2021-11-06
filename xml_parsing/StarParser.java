import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StarParser extends DefaultHandler{
    ArrayList<Star> stars;

    private String tempVal;

    private Star tempStar;

    public StarParser(){
        stars = new ArrayList<>();
    }

    public ArrayList<Star> getStars(){
        return stars;
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
        if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of star
            tempStar = new Star();
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("actor")) {
            //add it to the list
            stars.add(tempStar);

        } else if (qName.equalsIgnoreCase("stagename")) {
            tempStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {

            String regexPattern = "[0-9]{4}";
            Pattern regex = Pattern.compile(regexPattern);
            Matcher match = regex.matcher(tempVal);

            if (tempVal.length() == 0){
                tempStar.setDob(0);
            }

            else if (match.find()){
                tempStar.setDob(Integer.parseInt(tempVal));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Set<String> startable = new HashSet<String>();
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "root", "Pwner419123@");

        SAXParserFactory spf = SAXParserFactory.newInstance();
        StarParser starparser = new StarParser();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("actors63.xml", starparser);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        String query = "SELECT * FROM stars";
        PreparedStatement startablestatement = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        ResultSet rs_stars = startablestatement.executeQuery();

        while(rs_stars.next()){
            startable.add(rs_stars.getString("name"));
        }

        System.out.println(starparser.getStars());
    }
}