package com.suwonsmartapp.hello.listview;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.suwonsmartapp.hello.R;

public class SpinnerActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    String[] items = {"red", "orange", "yellow", "green", "blue", "violet"};
    private Spinner spinner;
    private TextView selectColor;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spinner);

        spinner = (Spinner) findViewById(R.id.IDspinner);
        selectColor = (TextView) findViewById(R.id.IDcolorselect);

//        spinner.setOnItemSelectedListener(this);

        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        selectColor.setText(items[position]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectColor.setText("");
    }
}
