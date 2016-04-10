package gp.sportsschedule;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Dave on 2/12/2016.
 */
public class Schedule {

    private static final String TAG = "Schedule";

    /** top level member, list of games */
    public static final String GAMES = "games";

    /** member of game object */
    public static final String TEAMS = "teams";

    /** member of game object, a.k.a. locationName -- zog display name for location */
    public static final String LOCATION = "location";

    /** member of game object, zog uses it as a date (w/o the time) */
    public static final String DATE = "day";

    /** member of the game object - zog uses it as a time only field */
    public static final String START_TIME = "startTime";

    public static final int MAX_GAME_COUNT = 32;


    public static class Game {

        private List<String> teams = new ArrayList<String>();

        private String locationName = null;

        private String locationAddress = null; //we need to figure this out

        private String dateFIXME = null; //should not be a string
        private String startFIXME = null; //should use time field of date

        private DateTime jodaDateTime = null;




        public static DateTimeFormatter createZogDateParser(){
            DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                    .appendDayOfWeekShortText()
                    .appendLiteral(". ")
                    .appendMonthOfYearText()
                    .appendLiteral(" ")
                    .appendDayOfMonth(2)
                    .appendLiteral(".")
                    .appendYear(2,4) //min,max digits
                    .appendLiteral("NOTASGOODASRUBY")
                    //.appendHourOfDay(2)  //apparently this causes it to ignore the AM/PM
                    .appendClockhourOfHalfday(2)
                    .appendLiteral(':')
                    .appendMinuteOfHour(2)
                    .appendLiteral(" ")
                    .appendHalfdayOfDayText()
                    .toFormatter();
            return fmt;
        }

        public Game(JSONObject j, Map<String, String> nameToAddress) throws JSONException {
            JSONArray jteams = j.getJSONArray(TEAMS);
            for(int i = 0; i < jteams.length(); ++i){
                this.teams.add(jteams.getString(i));
            }

            this.locationName = j.getString(LOCATION);

            if(nameToAddress != null){
                this.locationAddress = nameToAddress.get(this.locationName);
            }

            this.dateFIXME = j.getString(DATE);

            this.startFIXME = j.getString(START_TIME);

            //TODO:  need ridiculous sports quotes as event description

            //TODO:
            //-day
            //-start time


            //DateTimeFormat dtf = DateTimeFormat.

            //this got invalid:
            // //LocalDateTime ldt = LocalDateTime.parse(dateFIXME + " " + startFIXME);
            //java.lang.IllegalArgumentException: Invalid format: "Tue. Feb 16 07:30 PM"

            //this also failed:
            //"Tue. Feb 16"
            //LocalDateTime ldt = LocalDateTime.parse(dateFIXME);
            //dateFIXME = ldt.toString("yyyyMMdd'T'hhmmss");

            //so i guess joda is probably pointless


            //TODO:  we don't know the year, have to guess
            //
            //also, w/o fucking stupid ass joda time will assume year 2000
            //AND THEN let the day of week trump day of month.
            //so we need to add the year as part of the damn string

            //String test = "Feb 16";
            String parseMe = dateFIXME + "." + DateTime.now().getYear() + "NOTASGOODASRUBY" + startFIXME;




            DateTimeFormatter fmt = createZogDateParser();

            jodaDateTime = fmt.parseDateTime(parseMe).withZoneRetainFields(DateTimeZone.forID("America/New_York")); //TODO: hardcoded TZ

            DateTimeFormatter isoDateFormat = ISODateTimeFormat.dateTime();
            Log.e(TAG, parseMe + " parsed to " + jodaDateTime.toString() + " in iso8601: " + jodaDateTime.toString(isoDateFormat));





            //dateFIXME = dt.toString();
        }

        /**
         *
         * @param s
         * @return true if one of the teams in the game matches the param
         */
        public boolean hasTeam(String s){
            for(String team : this.teams){
                if(team != null && team.equals(s)){
                    return true;
                }
            }
            return false;
        }

        //TODO: should not be a string
        public String getDate(){
            return dateFIXME;
        }

        //TODO: should not be a string
        public String getStartTime(){
            return startFIXME;
        }

        public String getLocationName(){
            return this.locationName;
        }

        public Collection<String> getTeams(){
            return teams;
        }

        public String toIcalEntry() {
            StringBuilder sb = new StringBuilder();
            toIcalEntry(sb);
            return sb.toString();
        }

        public void toIcalEntry(StringBuilder sb){

            final int GAME_LENGTH_HOURS = 1;

            //TODO:  NYC time zone is hard coded!

            //convert to utc for printing
            DateTime utc = jodaDateTime.withZone(DateTimeZone.UTC);
            DateTime utcEnd = utc.withFieldAdded(DurationFieldType.hours(), GAME_LENGTH_HOURS);

            DateTimeFormatter isoDateFormat = ISODateTimeFormat.dateTime();
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z'");
            //DateTimeFormatter dtf = isoDateFormat;

            //mine type is text/calendar



            sb.append("BEGIN:VEVENT").append("\n");

            //sb.append("DTSTART;TZID=America/New_York:" + utc.toString(dtf)).append("\n");
            //sb.append("DTEND;TZID=America/New_York:" + utcEnd.toString(dtf)).append("\n");

            //have to get rid of the -'s because otherwise google wont read the date correctly
            sb.append("DTSTART:" + utc.toString(dtf).replace("-","")).append("\n");
            sb.append("DTEND:" + utcEnd.toString(dtf).replace("-", "")).append("\n");


            sb.append("DTSTAMP:" + DateTime.now().toString(dtf)).append("\n");
            sb.append("SUMMARY:  Volleyball").append("\n");
            sb.append("DESCRIPTION:VOLLEYBALL!!!").append("\n");
            if(this.locationAddress != null){
                sb.append("LOCATION:" + this.locationAddress).append("\n");
            }else{
                sb.append("LOCATION:" + this.locationName).append("\n");
            }
            sb.append("UID:" + UUID.randomUUID().toString().replace("-","")).append("\n");
            sb.append("END:VEVENT").append("\n");

            //“We must have had 99 percent of the match, it was the other three percent that cost us.”
            //“We’re going to turn this team around 360 degrees.”
            //“Don’t say I don’t get along with my teammates. I just don’t get along with some of the guys on the team.”
            //“Volleyball is 90% mental. The other half is physical.”

        }
    }



    private final JSONObject root;

    private final List<Game> games = new ArrayList<Game>(MAX_GAME_COUNT);

    private final Set<String> teams = new HashSet<String>();

    private Schedule(String json, Map<String, String> nameToAddress) throws JSONException {
        this.root = new JSONObject(json);
        JSONArray jgames = root.getJSONArray("games");
        for(int i = 0; i < jgames.length(); ++i){
            Game g = new Game(jgames.getJSONObject(i), nameToAddress);
            if(g != null){
                this.games.add(g);
                this.teams.addAll(g.getTeams());
            }else{
                //TODO:  throw?
            }
        }
    }

    public Set<String> getTeams(){
        return Collections.unmodifiableSet(teams);
    }

    public List<Game> getAllGamesWithTeam(String team){
        List<Game> list = new ArrayList<Game>();

        for(Game game : games){
            if(game != null && game.hasTeam(team)){
                list.add(game);
            }
        }
        return list;
    }

    public static Schedule fromJson(String json){
        try{
            return new Schedule(json, ZogParser.NAME_TO_ADDRESS);
        }catch(JSONException ex){
            return null;
        }
    }

}
