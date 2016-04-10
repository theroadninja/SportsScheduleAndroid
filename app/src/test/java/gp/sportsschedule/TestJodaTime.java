package gp.sportsschedule;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Dave on 4/10/2016.
 */
public class TestJodaTime {
    public static final String TAG = "TestJodaTime";


    @Test
    public void testParsing() throws Exception {

        String parseMe = "Mon. May 16.2016NOTASGOODASRUBY08:30 PM"; //Mon. May 16.2016NOTASGOODASRUBY08:30 PM parsed to 2016-05-16T08:30:00.000-04:00 in iso8601: 2016-05-16T08:30:00.000-04:00
        //String parseMe = "Mon. May 16.2016NOTASGOODASRUBY08:30 AM";  //Mon. May 16.2016NOTASGOODASRUBY08:30 AM parsed to 2016-05-16T08:30:00.000-04:00 in iso8601: 2016-05-16T08:30:00.000-04:00


        DateTimeFormatter fmt = Schedule.Game.createZogDateParser();
        DateTime jodaDateTime = fmt.parseDateTime(parseMe).withZoneRetainFields(DateTimeZone.forID("America/New_York")); //TODO: hardcoded TZ

        DateTimeFormatter isoDateFormat = ISODateTimeFormat.dateTime();
        //Log.e(TAG, parseMe + " parsed to " + jodaDateTime.toString() + " in iso8601: " + jodaDateTime.toString(isoDateFormat));

        DateTime result = new DateTime(jodaDateTime.toString(isoDateFormat));
        DateTime expected = new DateTime("2016-05-16T20:30:00.000-04:00");
        Assert.assertEquals(expected, result);
        //throw new Exception(parseMe + " parsed to " + jodaDateTime.toString() + " in iso8601: " + jodaDateTime.toString(isoDateFormat));
    }

}
