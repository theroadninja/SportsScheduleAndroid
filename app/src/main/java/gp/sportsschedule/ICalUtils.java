package gp.sportsschedule;

import android.content.Intent;

/**
 * Created by Dave on 2/16/2016.
 */
public class ICalUtils {

    public static final String MIME_TYPE = "text/calendar";

    public static String iCalHeader(){
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR").append("\n");
        sb.append("VERSION:2.0").append("\n");
        sb.append("PRODID:-//bobbin v0.1//NONSGML iCal Writer//EN").append("\n");
        sb.append("CALSCALE:GREGORIAN").append("\n");
        sb.append("METHOD:PUBLISH").append("\n");
        return sb.toString();
    }

    public static String iCalFooter(){
        StringBuilder sb = new StringBuilder();
        sb.append("END:VCALENDAR").append("\n");
        return sb.toString();
    }

}
