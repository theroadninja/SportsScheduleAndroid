package gp.sportsschedule;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;


/**
 * Created by Dave on 2/12/2016.
 */
public class MainPage extends LinearLayout {

    public MainPage(Context context, AttributeSet attr){
        super(context, attr);

        super.setOrientation(LinearLayout.VERTICAL);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.page_main, this, true);
    }
}
