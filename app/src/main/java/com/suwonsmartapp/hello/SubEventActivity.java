
package com.suwonsmartapp.hello;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.suwonsmartapp.hello.event.TouchEventActivity;

public class SubEventActivity extends ActionBarActivity {

    private ArrayList<String> mEventList;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        mListView = (ListView) findViewById(R.id.IDlistview);

        mEventList = new ArrayList<>();
        mEventList.add("TouchEventActivity");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, mEventList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mListView.setDivider(null);

                switch (position) {
                    case 0:
                        startActivity(new Intent(getApplicationContext(), TouchEventActivity.class));

                    default:
                }
            }
        });
    }
}
