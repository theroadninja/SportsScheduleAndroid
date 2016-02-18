package gp.sportsschedule;

import android.webkit.WebView;

/**
 * Created by Dave on 2/7/2016.
 */
public class JavascriptRunnable implements Runnable {
    private final String js;

    private final WebView wv;

    public JavascriptRunnable(WebView webview, String js) {
        this.wv = webview;
        this.js = js;
    }

    public void run() {
        wv.loadUrl("javascript:" + js);
    }
}
