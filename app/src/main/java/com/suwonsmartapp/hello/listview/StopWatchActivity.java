package com.suwonsmartapp.hello.listview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.suwonsmartapp.hello.R;
import java.util.Calendar;

public class StopWatchActivity extends ActionBarActivity implements View.OnClickListener{

    private Button mClockStart;
    private Button mClockPause;
    private TextView mTextClock;

    private boolean stopFlag = true;       // true if stop, false if run
    private int progress = 0;
    private long initialTime;
    private long elapsedTime;

    private Handler mTimeHandler = new Handler() {
        public void handleMessage(Message msg) {
            mTextClock.setText(getTimeFormat());
//            mTimeHandler.sendEmptyMessageDelayed(0, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_watch);

        mClockStart = (Button) findViewById(R.id.clockStart);
        mClockPause = (Button) findViewById(R.id.clockPause);
        mTextClock = (TextView) findViewById(R.id.textClock);

        mClockStart.setOnClickListener(this);
        mClockPause.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clockStart:
                stopFlag = false;       // true if stop, false if run
                initialTime = SystemClock.elapsedRealtime();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!stopFlag) {
                            elapsedTime = SystemClock.elapsedRealtime() - initialTime;
                            mTimeHandler.sendEmptyMessage(0);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                thread.start();
                break;

            case R.id.clockPause:
                stopFlag = true;       // true if stop, false if run
                break;
        }
    }

    public String getTimeFormat() {
        Calendar spendCalendar = Calendar.getInstance();
        spendCalendar.setTimeInMillis(elapsedTime);

        int minute = spendCalendar.get(Calendar.MINUTE);
        int second = spendCalendar.get(Calendar.SECOND);
        int mils = spendCalendar.get(Calendar.MILLISECOND);

        String str_minute = Integer.toString(minute);
        String str_second = Integer.toString(second);
        String str_mils = Integer.toString(mils);

        if (minute < 10) {
            str_minute = "0" + minute;
        }
        if (second < 10) {
            str_second = "0" + second;
        }
        if (mils < 10) {
            str_mils = "00" + mils;
        } else if (mils < 100) {
            str_mils = "0" + mils;
        }
        return str_minute + ":" + str_second + ":" + str_mils;
    }
}
