
package com.suwonsmartapp.hello;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.suwonsmartapp.hello.R;

public class ListViewActivity extends ActionBarActivity {

    private ArrayList<String> mFolderList;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        mListView = (ListView) findViewById(R.id.IDlistview);

        mFolderList = new ArrayList<>();
        mFolderList.add("activity");
        mFolderList.add("challenge");
        mFolderList.add("event");
        mFolderList.add("listview");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, mFolderList);

        mListView.setDivider(null);

        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        startActivity(new Intent(getApplicationContext(), SubActivityActivity.class));
                        break;

                    case 1:
                        startActivity(new Intent(getApplicationContext(), SubChallengeActivity.class));
                        break;

                    case 2:
                        startActivity(new Intent(getApplicationContext(), SubEventActivity.class));
                        break;

                    case 3:
                        startActivity(new Intent(getApplicationContext(), SubListviewActivity.class));
                        break;

                    default:
                        break;
                }
            }
        });
    }
}
