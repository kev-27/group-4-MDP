package com.example.mdp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.nio.charset.Charset;

public class Communication extends AppCompatActivity {
    private static final String TAG = "Communication->DEBUG";
    TextView showReceived;
    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            String old = showReceived.getText().toString();
            showReceived.setText(old + "\n[ROBOT]:  " + message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.communication);
        Log.d(TAG, "I'm created!");

        Button sendButton = (Button) this.findViewById(R.id.send_message_btn);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText msgToSend = (EditText) findViewById(R.id.chatbox_tv);
                String message = msgToSend.getText().toString();
                Log.d(TAG, message);

                if (BluetoothService.BluetoothConnectionStatus) {
                    byte[] bytes = message.getBytes(Charset.defaultCharset());
                    BluetoothService.write(bytes);
                    String old = showReceived.getText().toString();
                    showReceived.setText(old + "\n[TABLET]:  " + message);
                } else {
                    Toast.makeText(Communication.this, "Please connect to Bluetooth!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        showReceived = findViewById(R.id.chatlog_tv);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            IntentFilter filter = new IntentFilter("ConnectionStatus");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
