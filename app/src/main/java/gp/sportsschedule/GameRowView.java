package gp.sportsschedule;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Dave on 2/12/2016.
 */
public class GameRowView extends LinearLayout implements View.OnClickListener {



    static class GameListAdapter implements SimpleListAdapter.ListItemAdapter<Schedule.Game> {

        private OnIcalExportListener listener = null;

        public GameListAdapter(OnIcalExportListener listener){
            this.listener = listener;
        }

        @Override
        public View getView(Schedule.Game item, Context context, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = new GameRowView(context);
            }

            StringBuilder sb = new StringBuilder();
            for(String s : item.getTeams()){
                sb.append(s).append("\n");
            }

            GameRowView row = (GameRowView)convertView;
            row.setFields(item.getDate(), item.getStartTime(), item.getLocationName(), sb.toString());
            row.setOnIcalExportListener(listener);
            row.setGame(item);

            return row;
        }
    }

    public static interface OnIcalExportListener{
        public void OnIcalExportRequested(Schedule.Game game);
    }

    /** need a reference to this for the click listener to work */
    private Schedule.Game game = null;

    private OnIcalExportListener listener = null;

    public GameRowView(Context context){
        this(context, null);
    }

    public GameRowView(Context context, AttributeSet attr){
        super(context, attr);
        super.setOrientation(LinearLayout.HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_gamerow, this, true);

        findViewById(R.id.button_ical).setOnClickListener(this);
    }

    public void setOnIcalExportListener(OnIcalExportListener listener){
        this.listener = listener;
    }

    public void setGame(Schedule.Game game){
        this.game = game;

    }

    @Override
    public void onClick(View v) {
        if(this.listener != null){
            this.listener.OnIcalExportRequested(game);
        }
    }

    /**
     * TODO
     */
    public void setFields(String date, String starttime, String location, String teams){
        set(R.id.text_date, date);
        set(R.id.text_starttime, starttime);
        set(R.id.text_location, location);
        set(R.id.text_teams, teams);
    }

    void set(int id, String s){
        ((TextView)findViewById(id)).setText(s);
    }
}
