
package com.suwonsmartapp.hello;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.suwonsmartapp.hello.activity.ActivityExamActivity;
import com.suwonsmartapp.hello.activity.EditTextActivity;
import com.suwonsmartapp.hello.activity.FirstActivity;
import com.suwonsmartapp.hello.activity.FrameLayoutActivity;
import com.suwonsmartapp.hello.activity.RelativeLayoutExamActivity;
import com.suwonsmartapp.hello.activity.SecondActivity;
import com.suwonsmartapp.hello.activity.TableLayoutActivity;
import com.suwonsmartapp.hello.activity.TargetActivity;

public class SubActivityActivity extends ActionBarActivity {

    private ArrayList<String> mActivityList;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        mListView = (ListView) findViewById(R.id.IDlistview);

        mActivityList = new ArrayList<>();
        mActivityList.add("ActivityExamActivity");
        mActivityList.add("EditTextActivity");
        mActivityList.add("FirstActivity");
        mActivityList.add("FrameLayoutActivity");
        mActivityList.add("RelativeLayoutExamActivity");
        mActivityList.add("SecondActivity");
        mActivityList.add("TableLayoutActivity");
        mActivityList.add("TargetActivity");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, mActivityList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mListView.setDivider(null);

                switch (position) {
                    case 0:
                        startActivity(new Intent(getApplicationContext(), ActivityExamActivity.class));

                    case 1:
                        startActivity(new Intent(getApplicationContext(), EditTextActivity.class));

                    case 2:
                        startActivity(new Intent(getApplicationContext(), FirstActivity.class));

                    case 3:
                        startActivity(new Intent(getApplicationContext(), FrameLayoutActivity.class));

                    case 4:
                        startActivity(new Intent(getApplicationContext(), RelativeLayoutExamActivity.class));

                    case 5:
                        startActivity(new Intent(getApplicationContext(), SecondActivity.class));

                    case 6:
                        startActivity(new Intent(getApplicationContext(), TableLayoutActivity.class));

                    case 7:
                        startActivity(new Intent(getApplicationContext(), TargetActivity.class));

                    default:
                }
            }
        });
    }
}
