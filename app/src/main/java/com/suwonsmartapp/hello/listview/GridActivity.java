package com.suwonsmartapp.hello.listview;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.suwonsmartapp.hello.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class GridActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private GridView mGridView;
    private ArrayList<String> list;     // calendar data = week + 6 spaces + 31 days
    private Button mLeftButton;
    private Button mRightButton;
    private TextView mTodayText;

    private Calendar today;     // save today information from android calendar (we need to change it to gregorian calendar)
    private int moveMonth;      // 0 = no change, -1 = left month, +1 = right month
    private int howManyMoved;   // counter for how many left/right button pressed

    private int year, month, day, week;
    private int datePerMonth, startWeek;

    private Animation mTranslationAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        // get button ID for attach listener
        mGridView = (GridView) findViewById(R.id.IDgridview);
        mLeftButton = (Button) findViewById(R.id.IDprevmonth);
        mRightButton = (Button) findViewById(R.id.IDnextmonth);
        mTodayText = (TextView) findViewById(R.id.IDthismonth);

        // for the schedule saving
        mGridView.setOnItemClickListener(this);

        // setting today's calendar
        moveMonth = 0;      // we need to calculate how much month moved to left(-) or right(+)
        howManyMoved = 0;   // flag for seeing where we are.
        initialize();       // initialize variables
        changeCalender();   // display calendar

        // backward month
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveMonth = -1;     // left button pressed, go to the previous month
                howManyMoved--;     // flag for seeing where we are.
                changeCalender();   // display calendar
                showAnimationRtoL();
            }
        });

        // forward month
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveMonth = 1;      // right button pressed, go to the next month
                howManyMoved++;     // flag for seeing where we are.
                changeCalender();   // display calendar
                showAnimationLtoR();
            }
        });
    }

    // make calendar format as follows:
    //
    //      일  월  화  수  목  금  토          7개 요일
    //                               1          6개 space
    //       2   3   4   5   6   7   8         31개 날짜
    //       9  10  11  12  13  14  15        ============
    //      16  17  18  19  20  21  22         44개 datum
    //      23  24  25  26  27  28  29
    //      30  31
    private void initialize() {
        list = new ArrayList<>();
        list.add("日");      // calendar header
        list.add("月");
        list.add("火");
        list.add("水");
        list.add("木");
        list.add("金");
        list.add("土");
        list.add(" ");      // The very 1st day of the month can be placed on Saturday
        list.add(" ");      // so that we need 6 spaces maximum.
        list.add(" ");
        list.add(" ");
        list.add(" ");
        list.add(" ");

        for (int i = 1; i < 32; i++) {      // maximum 31 days per a month.
            list.add(String.valueOf(i));    // prepare spaces
        }
    }

    private void changeCalender() {
        adjustToday();       // if today is 29, 30, or 31, we can not go to the prev/next day
        today.add((Calendar.MONTH), moveMonth);

        week = today.get(Calendar.DAY_OF_WEEK);
        year = today.get(Calendar.YEAR);
        month = today.get(Calendar.MONTH);
        day = today.get(Calendar.DATE);

        datePerMonth = today.getActualMaximum(Calendar.DATE);

        showCurrentMonth();         // show current month and prev/next month

        // 일요일이면 index = 7,   일월화수목금토 = 1,2,3,4,5,6,7
        for (int i = 7; i < 14; i++) {
            list.set(i, " ");               // delete first 2 rows for the safety
        }

        // refer the following table (@ = 한달의 첫날요일)
        //   @   1일 2일 3일 4일 5일 6일 7일     |            |            |            |            |            |            |
        // 일(1)  1   7   6   5   4   3   2    7 | ******   7 | *******  7 | *******  7 | *******  7 | *******  7 | *******  7 | *******
        // 월(2)  2   1   7   6   5   4   3    6 | ***** *  6 | ******   6 | *******  6 | *******  6 | *******  6 | *******  6 | *******
        // 화(3)  3   2   1   7   6   5   4    5 | **** **  5 | ***** *  5 | ******   5 | *******  5 | *******  5 | *******  5 | *******
        // 수(4)  4   3   2   1   7   6   5    4 | *** ***  4 | **** **  4 | ***** *  4 | ******   4 | *******  4 | *******  4 | *******
        // 목(5)  5   4   3   2   1   7   6    3 | ** ****  3 | *** ***  3 | **** **  3 | ***** *  3 | ******   3 | *******  3 | *******
        // 금(6)  6   5   4   3   2   1   7    2 | * *****  2 | ** ****  2 | *** ***  2 | **** **  2 | ***** *  2 | ******   2 | *******
        // 토(7)  7   6   5   4   3   2   1    1 |  ******  1 | * *****  1 | ** ****  1 | *** ***  1 | **** **  1 | ***** *  1 | ******
        // week                                0 | *******  0 |  ******  0 | * *****  0 | ** ****  0 | *** ***  0 | **** **  0 | ***** *
        //                                    -1 | ******* -1 | ******* -1 |  ****** -1 | * ***** -1 | ** **** -1 | *** *** -1 | **** **
        //                                    -2 | ******* -2 | ******* -2 | ******* -2 |  ****** -2 | * ***** -2 | ** **** -2 | *** ***
        //                                    -3 | ******* -3 | ******* -3 | ******* -3 | ******* -3 |  ****** -3 | * ***** -3 | ** ****
        //          week - day + 1 graph ==>  -4 | ******* -4 | ******* -4 | ******* -4 | ******* -4 | ******* -4 |  ****** -4 | * *****
        //                                    -5 | ******* -5 | ******* -5 | ******* -5 | ******* -5 | ******* -5 | ******* -5 |  ******
        //                                    -6 | ******* -6 | ******* -6 | ******* -6 | ******* -6 | ******* -6 | ******* -6 | *******
        //                                       |   1일      |   2일      |   3일      |   4일      |   5일      |   6일      |   7일

        int remainder = ((day - 1) % 7) + 1;    // make any day to the 1 < day < 7 by getting remainder of 7 days
        startWeek = week - remainder + 1;       // get the first day's week
        if (startWeek <= 0) {
            startWeek = startWeek + 7;          // 0 = 토(7), -1 = 금(8), -2 = 목(5), -3 = 수(4), -4 = 화(3), -5 = 월(2)
        }                                       // by adding 7 on the result of week - day + 1

        startWeek = startWeek + 6;              // skip calendar head (7 weeks) for indexing

        int j = 1;
        for (int i = startWeek; i < datePerMonth + startWeek; i++) {
            list.set(i, String.valueOf(j));     // make calendar
            j++;
        }

        // list data 갯수 = 요일7개 + 공백6개 + 날자31개
        for (int i = datePerMonth + startWeek; i < (7 + 6 + 31); i++) {
            list.set(i, " ");                   // delete rest part
        }

        // adapter preparation
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
        mGridView.setAdapter(adapter);
    }

    // 만일 3월 31일에서 2월로 넘어갔을 때, 2월은 29,30,31일이 없을 수도 있기 때문에 today가 29,30,31일이면 안됨.
    // 역으로 1월 31일에서 2월로 넘어갔을 경우도 동일하기 때문에 오늘 날짜가 29,30,31일인 경우는 무조건 3일을 보정해 줌.
    // 대신에 달력을 이번달에 처음 표시하는 경우에는 오늘 날짜를 읽어 today를 리셋해 줌.
    private void adjustToday() {
        if (howManyMoved == 0) {
            today = GregorianCalendar.getInstance();     // we moved from prev/next month, thus refresh day again
        } else if (day > 28) {
            today.add((Calendar.DATE), -3);     // make 1 < day < 28
        }
    }

    // 달력의 년월 부분 표시
    private void showCurrentMonth() {
        int actualMonth = month + 1;            // nonth = 0 ~ 11 (1월 ~ 12월)
        mTodayText.setTextColor(Color.BLUE);    // display current year and month
        mTodayText.setText("   " + year + "년 " + actualMonth + "월   ");

        int prevActualMonth = actualMonth - 1;  // get previous month
        if (prevActualMonth == 0) {
            prevActualMonth = 12;
        }
        mLeftButton.setTextColor(Color.RED);    // left button
        mLeftButton.setText(prevActualMonth + "월");

        int nextActualMonth = actualMonth + 1;  // get next month
        if (nextActualMonth == 13) {
            nextActualMonth = 1;
        }
        mRightButton.setTextColor(Color.RED);   // right button
        mRightButton.setText(nextActualMonth + "월");
    }

    public void showAnimationLtoR() {
        mTranslationAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.translationr);
        mGridView.setAnimation(mTranslationAnimation);
    }

    public void showAnimationRtoL() {
        mTranslationAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.translationl);
        mGridView.setAnimation(mTranslationAnimation);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        list.set(position, list.get(position) + "0");
    }
}
