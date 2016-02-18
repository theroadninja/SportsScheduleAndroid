package gp.sportsschedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import gp.sportsschedule.tasks.ShareAttachmentTask;

/**
 * Created by Dave on 2/12/2016.
 */
public class SchedulePage extends LinearLayout {

    private Schedule schedule = null;
    private String rawJson = null;

    private String selectedTeam = null;
    private List<Schedule.Game> gamesList = null;

    public SchedulePage(Context context, AttributeSet attr) {
        super(context, attr);

        super.setOrientation(LinearLayout.VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.page_team, this, true);

        findViewById(R.id.button_share_all).setOnClickListener(exportAllICal);
        findViewById(R.id.button_export_json).setOnClickListener(shareJson);


        //
    }

    public void setRawJson(String s) {
        this.rawJson = s;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;

    }

    public void showScheduleForTeam(String team) {

        this.selectedTeam = team;

        gamesList = schedule.getAllGamesWithTeam(team);

        SimpleListAdapter adapter = new SimpleListAdapter(getContext(), gamesList, new GameRowView.GameListAdapter(onSingleRowExport));

        ((ListView) findViewById(R.id.list_games)).setAdapter(adapter);
    }

    private GameRowView.OnIcalExportListener onSingleRowExport = new GameRowView.OnIcalExportListener() {
        @Override
        public void OnIcalExportRequested(Schedule.Game game) {
            if (game != null) {

                //need the header and footer for a single entry

                StringBuilder sb = new StringBuilder();
                sb.append(ICalUtils.iCalHeader());
                game.toIcalEntry(sb);
                sb.append(ICalUtils.iCalFooter());


                String filename = "game.ical";
                ShareAttachmentTask task = new ShareAttachmentTask(getContext(), sb.toString(), ICalUtils.MIME_TYPE, filename);
                task.setLaunchWhenReady((Activity) getContext(), "Export iCal Entry");
                task.execute();

                //Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_SHORT).show();
            }

        }
    };

    private View.OnClickListener exportAllICal = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if(gamesList == null || gamesList.size() < 1){
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(ICalUtils.iCalHeader());
            for(Schedule.Game game : gamesList){
                game.toIcalEntry(sb);
            }
            sb.append(ICalUtils.iCalFooter());

            String filename = "games.ical"; //different from 'game.ical' used by single export
            ShareAttachmentTask task = new ShareAttachmentTask(getContext(), sb.toString(), ICalUtils.MIME_TYPE, filename);
            task.setLaunchWhenReady((Activity) getContext(), "Export All iCal Entries");
            task.execute();

        }
    };


    private View.OnClickListener shareJson = new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            Intent i = new Intent(android.content.Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Schedule Json");
            i.putExtra(android.content.Intent.EXTRA_TEXT, rawJson);
            ((Activity)getContext()).startActivity(Intent.createChooser(i, "Export raw json via"));
        }
    };


}
