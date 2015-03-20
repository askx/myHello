
package com.suwonsmartapp.hello;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SubListviewActivity extends ActionBarActivity {

    private ArrayList<String> mListviewList;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        mListView = (ListView) findViewById(R.id.IDlistview);

        mListviewList = new ArrayList<>();
        mListviewList.add("ListViewActivity");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, mListviewList);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mListView.setDivider(null);

                switch (position) {
                    case 0:
                        startActivity(new Intent(getApplicationContext(), ListViewActivity.class));

                    default:
                }
            }
        });
    }
}
