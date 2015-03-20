package com.suwonsmartapp.hello.listview;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.suwonsmartapp.hello.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class GridActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private GridView mGridView;
    private ArrayList<String> list;
    private Button mLeftButton;
    private Button mRightButton;
    private TextView mTodayText;

    private Calendar today;
    private int moveMonth;

    private int year, month, day, week;
    private int datePerMonth, startWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        mGridView = (GridView) findViewById(R.id.IDgridview);
        mLeftButton = (Button) findViewById(R.id.IDprevmonth);
        mRightButton = (Button) findViewById(R.id.IDnextmonth);
        mTodayText = (TextView) findViewById(R.id.IDthismonth);

        // for the schedule saving
        mGridView.setOnItemClickListener(this);

        // setting today's calendar
        initialize();

        // backward month
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveMonth = -1;
                changeCalender();
            }
        });

        // forward month
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveMonth = 1;
                changeCalender();
            }
        });
    }

    private void initialize() {
        list = new ArrayList<>();
        list.add("일");
        list.add("월");
        list.add("화");
        list.add("수");
        list.add("목");
        list.add("금");
        list.add("토");
        list.add(" ");
        list.add(" ");
        list.add(" ");
        list.add(" ");
        list.add(" ");
        list.add(" ");

        for (int i = 1; i < 32; i++) {
            list.add(String.valueOf(i));
        }

        mLeftButton.setTextColor(Color.RED);
        mLeftButton.setText(" <- ");

        mRightButton.setTextColor(Color.RED);
        mRightButton.setText(" -> ");

        mTodayText.setTextColor(Color.BLUE);
        mTodayText.setText("   2015년 4월   ");

        moveMonth = 0; // we need to calculate how much month moved to left(-) or right(+)
        today = Calendar.getInstance();
        changeCalender();
    }

    private void changeCalender() {
        today.add((Calendar.MONTH), moveMonth);

        week = today.get(Calendar.DAY_OF_WEEK);
        year = today.get(Calendar.YEAR);
        month = today.get(Calendar.MONTH);
        day = today.get(Calendar.DATE);

        datePerMonth = today.getActualMaximum(Calendar.DATE);

        int actualMonth = month + 1;
        mTodayText.setText("   " + year + "년 " + actualMonth + "월   ");

        // 일요일이면 index = 7,   일월화수목금토 = 1,2,3,4,5,6,7

        for (int i = 7; i < 14; i++) {
            list.set(i, " ");
        }

        // refer the following table
        //          1일   2일   3일   4일   5일   6일   7일   day
        // 일(1)     1     7     6     5     4     3     2
        // 월(2)     2     1     7     6     5     4     3
        // 화(3)     3     2     1     7     6     5     4
        // 수(4)     4     3     2     1     7     6     5
        // 목(5)     5     4     3     2     1     7     6
        // 금(6)     6     5     4     3     2     1     7
        // 토(7)     7     6     5     4     3     2     1
        // week

        int remainder = ((day - 1) % 7) + 1;
        startWeek = week - remainder + 1;
        if (startWeek <= 0) {
            startWeek = startWeek + 7;
        }

        startWeek = startWeek + 6;

        int j = 1;
        for (int i = startWeek; i < datePerMonth + startWeek; i++) {
            list.set(i, String.valueOf(j));
            j++;
        }

        // list data 갯수 = 요일7개 + 공백6개 + 날자31개
        for (int i = datePerMonth + startWeek; i < (7 + 6 + 31); i++) {
            list.set(i, " ");
        }

        // adapter preparation
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
        mGridView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        list.set(position, list.get(position) + "0");
    }
}
