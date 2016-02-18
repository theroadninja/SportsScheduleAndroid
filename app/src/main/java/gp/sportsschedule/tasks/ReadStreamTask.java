package gp.sportsschedule.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import gp.sportsschedule.R;

/**
 * Created by Dave on 2/12/2016.
 */
public class ReadStreamTask extends AsyncTask<Void, Void, String> {

    public static interface OnPostExecuteListener {
        public void onPostExecute(String s);
    }

    private final InputStream is;

    private ProgressDialog progress = null;

    private OnPostExecuteListener li = null;

    public ReadStreamTask(InputStream is){
        this.is = is;
    }

    public void setOnPostExecuteListener(OnPostExecuteListener li){
        this.li = li;
    }

    /**
     * call from the UI thread
     */
    public void showProgressAndHideLater(Context context){
        progress = new ProgressDialog(context);
        progress.setCancelable(false);
        progress.setMessage("please wait");
        progress.show();
    }

    @Override
    protected String doInBackground(Void... params) {

        try{


            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            final StringBuilder sb = new StringBuilder();
            String line = null;
            while(null != (line = br.readLine())){
                sb.append(line).append("\n");
            }
            //new Handler(Looper.getMainLooper()).post(new Runnable(){public void run(){onScheduleDownloaded.OnSchedule(sb.toString());}});

            return sb.toString();

        }catch(Exception ex){

            //TODO
            throw new RuntimeException(ex);
        }

    }

    @Override
    protected void onPostExecute(String s){

        if(progress != null){
            try{ progress.dismiss(); }catch(Exception ex){}
        }

        if(li != null){
            li.onPostExecute(s);
        }
    }
}
