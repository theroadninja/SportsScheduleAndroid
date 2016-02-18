package gp.sportsschedule;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.json.JSONException;

/**
 * Created by Dave on 2/7/2016.
 *
 * Adapter to manage talking to zogsports.com through a webview.  Zog Sports TM and (C) is owned by Zog Sports.
 *
 */
public class ZogWebsiteAdapter {
    static final String TAG = "ZogWebsiteAdapter";

    public static final String WEBSITE = "http://www.zogsports.com/nyc/home.aspx";

    /**
     * Prefix for hacky log message used to dump the page source.
     * WebView should have a method for accessing the page source, but since it doesnt, we have
     * to use the insecure java inject or the console log message.  Trying to make the log message
     * one work.
     */
    private static final String SCRAPE_PREFIX = "SCRAPE";

    private final WebView webview;

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    /** fired when the adapter is ready for the user to attempt to login
     * TODO: with a more intelligent workflow the user wouldn't have to wait for the initial page load
     */
    private Runnable readyForLoginListener;

    public static interface OnScheduleListener {

        //TODO:  actually the adapter should own the parsing...just using this to see if this worked
        public void OnSchedule(String s);
    }

    public static interface OnStatusListener {
        public void OnStatusChange(String newStatus);
    }

    private OnScheduleListener onScheduleListener = null;
    private OnStatusListener onStatusListener = null;

    private static final int START_STATE = 0;
    private static final int FIRST_LOAD_COMPLETE = 1;
    private static final int LOGIN_SENT = 2;
    private static final int LOGGED_IN = 3;
    private static final int SCHEDULE_NAV_SENT = 4;
    private static final int ON_SCHEDULE_PAGE = 5;

    private int state = START_STATE;






    public ZogWebsiteAdapter(WebView webview){
        this.webview = webview;

        this.webview.getSettings().setJavaScriptEnabled(true);
        this.webview.zoomOut();

        this.webview.setWebViewClient(new SportWebViewClient());
        this.webview.setWebChromeClient(new SportWebChromeClient());


    }

    public void setReadForLoginListener(Runnable r){
        this.readyForLoginListener = r;
    }

    public void setOnScheduleListener(OnScheduleListener li){
        this.onScheduleListener = li;
    }

    public void setOnStatusListener(OnStatusListener li){
        this.onStatusListener = li;
    }

    private void notifyStatusChange(final String newStatus){
        if(this.onStatusListener != null){
            uiHandler.post(new Runnable(){
                public void run(){ onStatusListener.OnStatusChange(newStatus); }
            });
        }
    }

    public void injectJavascript(String js){
        this.webview.loadUrl("javascript:" + js);
    }



    public void injectJavascript(String js, int delayMillis){
        uiHandler.postDelayed(new JavascriptRunnable(webview, js), 100); //millis
    }

    /**
     * step 1: load zog site
     */
    public void start(){
        this.webview.loadUrl(WEBSITE);
    }

    /**
     *
     * @param username
     * @param password
     */
    public void loginToZog(String username, String password){

        //step 1: start login
        String clickOnLogin = "document.getElementById('ctl00_ctl00_signin').childNodes[0].click();";


        injectJavascript(clickOnLogin);




        //step 2: insert values into login dialog
        StringBuilder sb = new StringBuilder();
        sb.append("document.getElementById('ctl00_ctl00_mainLogin_loginMain_UserName').value = \"").append(username).append("\";");
        sb.append("document.getElementById('ctl00_ctl00_mainLogin_loginMain_Password').value = \"").append(password).append("\";");
        sb.append("document.getElementById('ctl00_ctl00_mainLogin_loginMain_LoginButton').click();");


        state = LOGIN_SENT;
        injectJavascript(sb.toString(), 200);

        notifyStatusChange("logging into zog");
    }

    /**
     * nagivates to first or last link it finds that contains "eventschedule.aspx?activity" or whatever.
     *
     * TODO:  make this work when you are on more than one team.
     */
    public void navToSchedule(){

        StringBuilder sb = new StringBuilder();

        //store an event schedule link on a var attached to window object (because i dont want to bother writing a js->java link)
        sb.append("[].forEach.call(document.getElementsByTagName('a'), function(tag) { if(tag.href.indexOf(\"eventschedule.aspx?activityid\") > -1) { window.tmp_thing = tag.href; } });");

        //navigate there
        sb.append("window.location.href = window.tmp_thing");


        state = SCHEDULE_NAV_SENT;
        injectJavascript(sb.toString());

        notifyStatusChange("navigating to schedule");
    }

    public void scrapeSchedule(){
        if(state != ON_SCHEDULE_PAGE) throw new IllegalStateException();

        Log.e(TAG, "attempting scrape");
        injectJavascript("console.log('" + SCRAPE_PREFIX + "' + document.getElementsByTagName('html')[0].innerHTML);");

        notifyStatusChange("scraping schedule html");
    }

    void onSchedulePageScraped(final String scheduleHtml){

        notifyStatusChange("parsing schedule");

        new ParseTask(scheduleHtml).execute();


    }


    private class ParseTask extends AsyncTask<Void, Void, String> {

        private String data;

        public ParseTask(String data){
            this.data = data;
        }

        @Override
        protected String doInBackground(Void... params) {
            try{
                return ZogParser.parse(this.data);
            }catch(Exception ex){
                return "parsing failed " + ex.getMessage();
            }

        }

        @Override
        protected void onPostExecute(final String result){

            if(onScheduleListener != null){

                onScheduleListener.OnSchedule(result);

            }

            notifyStatusChange("Done");

        }
    }


    private class SportWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url){

            Log.e(TAG, "onPageFinished() - page finished for " + url);
            // - page finished for http://www.zogsports.com/nyc/home.aspx

            //problem:  after login we get:
            // page finished for http://www.zogsports.com/nyc/home.aspx

            if(state == START_STATE) {
                state = FIRST_LOAD_COMPLETE;

                if (readyForLoginListener != null) {
                    readyForLoginListener.run();
                }
            }else if(state == LOGIN_SENT){

                state = LOGGED_IN;
                navToSchedule();
            }else if(state == SCHEDULE_NAV_SENT){

                state = ON_SCHEDULE_PAGE;
                scrapeSchedule();
            }



        }


    }

    private class SportWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage cm){

            if(cm.message().startsWith(SCRAPE_PREFIX)){
                Log.e(TAG, "saw scrape prefix in log");

                onSchedulePageScraped(cm.message().substring(SCRAPE_PREFIX.length()));
                return true;
            }

            if(cm.messageLevel() == ConsoleMessage.MessageLevel.ERROR){
                Log.e(TAG, cm.message());
            }

            return false;
        }
    }

}
