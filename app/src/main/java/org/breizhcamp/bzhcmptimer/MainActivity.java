package org.breizhcamp.bzhcmptimer;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    private static final int refreshInterval = 1000;

    private Handler timerHandler = new Handler();
    private int counter = 60;
    private boolean counting = false;


    Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            TextView timerView = (TextView) findViewById(R.id.timer);
            timerView.setText(String.valueOf(--counter));

            if (counter > 0 && counting) {
                timerHandler.postDelayed(this, refreshInterval);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startStopTimer(View view) {
        counting = !counting;
        if (counting) {
            timerHandler.postDelayed(updateTimer, 0);
        }
        else {
            timerHandler.removeCallbacks(updateTimer);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            counter = 60;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
