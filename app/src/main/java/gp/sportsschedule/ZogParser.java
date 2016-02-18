package gp.sportsschedule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Dave on 2/7/2016.
 *
 * Attempts to parse eventschedule.aspx?activityid=... into something usable.
 */
public class ZogParser {

    public static final Map<String, String> NAME_TO_ADDRESS = new TreeMap<String, String>(){{
      //TODO:  the contents of this map should not be checked into source control



    }};


    public static String parse(String data) throws JSONException {

        //first version of the parsing code was based on finding 'dayValue':
        //  https://github.com/theroadninja/ZSportsWebsiteToIcal/blob/master/parse.rb

        Document doc = Jsoup.parse(data);

        //find all tr elements with class memberGridRow_season OR memberGridRowAlt_season

        //Elements list1 = doc.select("tr.memberGridRow season");
        //Elements list2 = doc.select("tr.memberGridRowAlt season");
        //list1.addAll(list2);


        Elements list1 = doc.select("tr.season");

        JSONObject schedule = new JSONObject();
        JSONArray games = new JSONArray();
        schedule.put(Schedule.GAMES, games);

        StringBuilder sb = new StringBuilder();
        for(Element e : list1){
            sb.append(e.html());
            //sb.append(e.getClass());

            games.put(parseGame(e));
        }


        //return sb.toString();

        return schedule.toString();
    }

    public static JSONObject parseGame(Element tr) throws JSONException {
        /* the ruby code that did this:

        trs = table.css('tr')
  trs.each { |tr|
    day = tr.css('td div#dayValue')
    if day.nil?
      next
    end
    time = tr.css('td div#startDateValue').text.strip
    loc = tr.css('td div#locationNameValue').text.strip
    teams = tr.css('td div#teamsValue').text.strip
         */



        //1) Date:
        //<div id="dayValue">Mon. Feb 08</div>
        Element day = tr.select("div#dayValue").get(0);

        //2) Start
        Element startTime = tr.select("div#startDateValue").get(0);

        //3) Location
        Element location = tr.select("div#locationNameValue").get(0);

        //4) Teams
        Element teams = tr.select("div#teamsValue").get(0);

        JSONArray jteams = new JSONArray();
        for(Element team : teams.select("li")){
            jteams.put(team.text());
        }



        JSONObject j = new JSONObject();
        j.put(Schedule.DATE, day.text());
        j.put(Schedule.START_TIME, startTime.text());
        j.put(Schedule.LOCATION, location.text());
        j.put(Schedule.TEAMS, jteams);

        return j;
    }


}
