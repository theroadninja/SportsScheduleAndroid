package gp.sportsschedule.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.UUID;

/**
 * Created by Dave on 2/16/2016.
 *
 * Writes to a file and then creates a share intent to send a file as an attachment.
 *
 * I'm not 100% certain, but based on the old code I'm copying from, Android is retarded
 * and forces you to read from a file on disk if you want to send data as an attachment.
 * So we have to write a fucking temp file first.
 */
public class ShareAttachmentTask extends AsyncTask<Void, Void, Boolean> {

    static final String TAG = "ShareAttachmentTask";

    private final File file;

    private final String mimeType;

    private final String content;

    public static interface OnShareIntentReadyListener {
        public void OnShareIntentReady(Intent shareIntent);
    }

    private OnShareIntentReadyListener listener = null;

    public ShareAttachmentTask(Context context, String content, String mimeType, String filename){
        //String filename = "export." + UUID.randomUUID().toString().replace("-","");
        file = new File(context.getApplicationContext().getExternalCacheDir().getAbsolutePath(), filename);

        this.content = content;
        this.mimeType = mimeType;
    }

    public void setOnShareIntentReadyListener(OnShareIntentReadyListener listener){
        this.listener = listener;
    }

    public void setLaunchWhenReady(final Activity activity, final String exportChooserTitle){
        this.setOnShareIntentReadyListener(new OnShareIntentReadyListener() {
            @Override
            public void OnShareIntentReady(Intent shareIntent) {
                activity.startActivityForResult(Intent.createChooser(shareIntent, exportChooserTitle), 0);
            }
        });
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        BufferedWriter writer = null;
        try{
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

            writer.write(content);
            writer.flush();
            writer.close();

            return Boolean.TRUE;
        }catch(IOException ex){
            Log.e(TAG, ex.getMessage(), ex);
            return Boolean.FALSE;
        }finally{
            try{ if(writer != null){ writer.close(); } }catch(Exception ex){}
        }

    }

    @Override
    protected void onPostExecute(Boolean result){

        if(!Boolean.TRUE.equals(result)){
            return;
        }

        if(listener != null){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType);

            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

            listener.OnShareIntentReady(intent);
        }
    }


}
