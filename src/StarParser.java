import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StarParser extends DefaultHandler{
    ArrayList<Star> stars;
    ArrayList<String> inconsistencies;

    private String tempVal;

    private Star tempStar;

    public StarParser(){
        stars = new ArrayList<>();
        inconsistencies = new ArrayList<>();
    }

    public ArrayList<Star> getStars(){
        return stars;
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException{
        tempVal = new String(ch, start, length);
        if(tempVal.contains("~") || tempVal.contains("+") || tempVal.contains("[1]")){
            inconsistencies.add(tempVal);
        }
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
        HashMap<Star,String> startable = new HashMap<Star, String>(); // {Star.name : Star.id}
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?allowLoadLocalInfile=true", "mytestuser", "My6$Password");

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


        // SETTING STAR ID FROM MAX ID
        String idQuery = "select max(id) as maxID from stars";
        PreparedStatement starIds = conn.prepareStatement(idQuery, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        String maxId = "0";
        Integer tempId = 0;

        ResultSet rs_ids = starIds.executeQuery();
        while(rs_ids.next()){
            maxId = rs_ids.getString("maxID");
            tempId = (Integer.parseInt(maxId.substring(maxId.length() - 7)) + 1);
        }

        // INSERTING INTO DB
//        String insertStars = "INSERT INTO stars VALUES (?, ?, ?)";
        String insertStars = "LOAD DATA LOCAL INFILE './src/StarsData.txt'\n" +
                "INTO TABLE stars\n" +
                "FIELDS TERMINATED BY '*'\n" +
                "LINES TERMINATED BY '$'\n" +
                "(id, name, @dob)\n" +
                "SET birthYear = nullif(@dob, \"0\");";


        for (Star elem : starparser.getStars()){
            String star_id = "nm";
//                System.out.println(elem.getName() +  " = " +elem.getDob());
            star_id += (tempId);
            // System.out.println(star_id + "\n");
            startable.put(elem, star_id);
            tempId++;

        }

        File stars_f = new File("./src/StarsData.txt");
        File stars_fin = new File("./src/StarsDataInconsistencies.txt");
        FileWriter fw = new FileWriter(stars_f);
        FileWriter fwin = new FileWriter(stars_fin);
        for (Map.Entry<Star, String> elem : startable.entrySet()){
            String txt =  elem.getValue() + "*" + elem.getKey().getName() + "*" +  elem.getKey().getDob();
            txt += "$";
            fw.write(txt);
        }

        fwin.write("String inconsistencies within all fields which had '~', '+', '[1]' were all removed and whitespaces trimmed:\n");
        for (String in : starparser.inconsistencies){
            fwin.write(in + "\n");
        }

        fw.close();
        fwin.close();

        PreparedStatement statement = conn.prepareStatement(insertStars);
        statement.executeQuery();
    }
}