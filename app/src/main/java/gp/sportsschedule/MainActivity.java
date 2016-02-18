package gp.sportsschedule;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import gp.sportsschedule.tasks.ReadStreamTask;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = "MainActivity";


    private MainPage mainPage = null;
    private SchedulePage schedulePage = null;

    private TextView status = null;
    private WebView webView = null;

    private ZogWebsiteAdapter zog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //todo:  we should get this from a config, and if no config, present
        //button to select NYC

        mainPage = (MainPage)findViewById(R.id.page_main);
        schedulePage = (SchedulePage)findViewById(R.id.page_schedule);

        findViewById(R.id.button_go).setOnClickListener(onGoButton);
        findViewById(R.id.button_scrape).setOnClickListener(onScrapeButton);

        findViewById(R.id.button_use_hardcoded_data).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                ReadStreamTask task = new ReadStreamTask(getResources().openRawResource(R.raw.testschedule));
                task.setOnPostExecuteListener(new ReadStreamTask.OnPostExecuteListener() {
                    public void onPostExecute(String json) {
                        onScheduleDownloaded.OnSchedule(json);
                    }
                });
                task.execute();

            }
        });

        this.status = (TextView) findViewById(R.id.txt_status);

        this.webView = (WebView) findViewById(R.id.webview1);

        this.zog = new ZogWebsiteAdapter(this.webView);

        this.zog.setReadForLoginListener(new Runnable() {
            public void run() {
                findViewById(R.id.button_go).setEnabled(true);
            }
        });

        this.zog.setOnStatusListener(new ZogWebsiteAdapter.OnStatusListener() {

            @Override
            public void OnStatusChange(String newStatus) {
                status.setText(newStatus);
            }
        });

        this.zog.setOnScheduleListener(this.onScheduleDownloaded);

        this.zog.start();


    }

    private View.OnClickListener onGoButton = new View.OnClickListener() {
        public void onClick(View v) {

            String username = ((TextView) findViewById(R.id.text_username)).getText().toString();
            String password = ((TextView) findViewById(R.id.text_password)).getText().toString();

            hideKeyboard();

            zog.loginToZog(username, password);
        }
    };

    private View.OnClickListener onScrapeButton = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                zog.scrapeSchedule();
            } catch (IllegalStateException ex) {
                showAlert("can't scrape now");
            }
        }
    };



    private ZogWebsiteAdapter.OnScheduleListener onScheduleDownloaded = new ZogWebsiteAdapter.OnScheduleListener() {
        @Override
        public void OnSchedule(String s) {
            hideKeyboard();

            webView.setVisibility(View.GONE);
            Log.e(TAG, "got schedule: " + s);

            schedulePage.setRawJson(s);

            ((TextView) findViewById(R.id.text_html)).setText(s);

            Schedule schedule = Schedule.fromJson(s);
            schedulePage.setSchedule(schedule);
            if (schedule != null) {
                showTeamSelectionDialog(schedule);
            }
        }
    };

    private void showTeamSelectionDialog(Schedule schedule) {

        final String[] teams = schedule.getTeams().toArray(new String[]{});

        DialogInterface.OnClickListener onTeamSelected = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                showTeam(teams[which]);
            }
        };

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Select your team");
        b.setItems(teams, onTeamSelected);
        AlertDialog d = b.create();
        d.setCanceledOnTouchOutside(true);
        d.show();

    }


    private void showTeam(String team){
        Toast.makeText(this, team + " selected", Toast.LENGTH_SHORT).show();

        this.mainPage.setVisibility(View.GONE);
        this.schedulePage.setVisibility(View.VISIBLE);
        this.schedulePage.showScheduleForTeam(team);
    }




    private void showAlert(String message) {
        AlertDialog d = new AlertDialog.Builder(this).setMessage(message).create();
        d.setCanceledOnTouchOutside(true);
        d.show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);

    }


}
