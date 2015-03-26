package com.suwonsmartapp.hello.listview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.suwonsmartapp.hello.R;

import java.util.ArrayList;

/**
 * Created by user on 2015-03-24.
 */
public class CustomDialog extends Dialog {

    private Button mIDsaveSchedule;
    private Button mIDCloseSchedule;
    private EditText mIDscheduleText;
    private EditText mIDscheduleTime;
    private EditText mIDscheduleMinute;

    private String saveHour;
    private String saveMinute;
    private ArrayList<String> saveSchedule;

    public CustomDialog (Context context) {
        super(context);
        saveHour = "";
        saveMinute = "";
        saveSchedule = new ArrayList<>();
    }

    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);

        mIDsaveSchedule = (Button) findViewById(R.id.IDsaveSchedule);
        mIDCloseSchedule = (Button) findViewById(R.id.IDcloseSchedule);
        mIDscheduleText = (EditText) findViewById(R.id.IDscheduleText);
        mIDscheduleTime = (EditText) findViewById(R.id.IDscheduleTime);
        mIDscheduleMinute = (EditText) findViewById(R.id.IDscheduleMinute);

        mIDsaveSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSchedule.add(mIDscheduleText.getText().toString());
                saveHour = mIDscheduleTime.getText().toString();
                saveMinute = mIDscheduleMinute.getText().toString();

                mIDscheduleText.setText("");
                mIDscheduleTime.setText("");
                mIDscheduleMinute.setText("");
            }
        });

        mIDCloseSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public String getSavedHour() {
        return saveHour;
    }

    public String getSavedMinute() {
        return saveMinute;
    }

    public ArrayList getSavedText() {
        return saveSchedule;
    }

//    @Override
//    public void onClick(View v) {
//        if (v == mIDsaveSchedule) {
//            saveSchedule.add(mIDscheduleText.getText().toString());
//            saveHour = mIDscheduleTime.getText().toString();
//            saveMinute = mIDscheduleMinute.getText().toString();
//
//            mIDscheduleText.setText("");
//            mIDscheduleTime.setText("");
//            mIDscheduleMinute.setText("");
//        } else if (v == mIDCloseSchedule) {
//            dismiss();
//        }
//    }
}
