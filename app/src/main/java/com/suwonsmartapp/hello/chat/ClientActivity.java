package com.suwonsmartapp.hello.chat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.suwonsmartapp.hello.R;

public class ClientActivity extends Activity implements View.OnClickListener {

    private static ScrollView sv;
    private static EditText tv;
    private static EditText mchatting_message;
    private Button mbtn_sending;
    private  ChatClient chatClient;

    private boolean flagConnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cilent);

        tv = (EditText) findViewById(R.id.chatting_window);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setMaxLines(10000);
        tv.setVerticalScrollBarEnabled(true);
        tv.setScrollBarStyle(EditText.SCROLLBARS_INSIDE_OVERLAY);

        sv = (ScrollView) findViewById(R.id.chatting_scroll);

        mchatting_message = (EditText) findViewById(R.id.chatting_message);
        mbtn_sending = (Button) findViewById(R.id.btn_sending);

        chatClient = new ChatClient();
        chatClient.receiveHandler(mHandler);
        mbtn_sending.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!flagConnect) {
            mbtn_sending.setText("전송");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    chatClient.connect();
                    flagConnect = true;
                }
            }).start();
        }
        if (flagConnect) {
            chatClient.sendMessage(mchatting_message.getText().toString());
            mchatting_message.setText("");
        }
    }

    public static Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
                String prevMsg = tv.getText().toString();
                tv.setText(prevMsg + msg.obj + "\n");

                sv.post(new Runnable() {
                    public void run() {
                        sv.fullScroll(sv.FOCUS_DOWN);
                        mchatting_message.requestFocus();
                    }
                });
            }
        };
}
