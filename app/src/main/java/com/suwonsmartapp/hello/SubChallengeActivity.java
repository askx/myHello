
package com.suwonsmartapp.hello;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.suwonsmartapp.hello.challenge.challenge01.ImageExamActivity;
import com.suwonsmartapp.hello.challenge.challenge02.SMSActivity;
import com.suwonsmartapp.hello.challenge.challenge04.MainActivity;
import com.suwonsmartapp.hello.challenge.challenge04.SubActivity;
import com.suwonsmartapp.hello.challenge.challenge05.Mission05MainActivity;
import com.suwonsmartapp.hello.challenge.challenge06.Mission06MainActivity;

public class SubChallengeActivity extends ActionBarActivity {

    private ArrayList<String> mChallengeList;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        mListView = (ListView) findViewById(R.id.IDlistview);

        mChallengeList = new ArrayList<>();
        mChallengeList.add("ImageExamActivity");
        mChallengeList.add("SMSActivity");
        mChallengeList.add("MainActivity");
        mChallengeList.add("Mission05MainActivity");
        mChallengeList.add("Mission06MainActivity");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, mChallengeList);

                mListView.setDivider(null);

                mListView.setAdapter(adapter);

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Class c = null;
                try {
                    c = Class.forName(BuildConfig.APPLICATION_ID + ".challenge." + mChallengeList.get(position));
                    startActivity(new Intent(getApplicationContext(), c));
                } catch (ClassNotFoundException e) {
                }
            }
        });
    }
}
