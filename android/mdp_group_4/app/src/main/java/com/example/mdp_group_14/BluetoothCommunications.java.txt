package com.example.mdp_group_14;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONObject;
import org.json.JSONArray;

import java.nio.charset.Charset;

public class BluetoothCommunications extends Fragment {
    private static final String TAG = "BluetoothComms";

    SharedPreferences sharedPreferences;
    private static TextView messageReceivedTextView;
    private static EditText typeBoxEditText;
    StringBuilder messages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
        messages = new StringBuilder();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_communications, container, false);

        ImageButton send;
        send = root.findViewById(R.id.messageButton);

        // Message Box
        messageReceivedTextView = root.findViewById(R.id.messageReceivedTitleTextView);
        messageReceivedTextView.setMovementMethod(new ScrollingMovementMethod());
        typeBoxEditText = root.findViewById(R.id.typeBoxEditText);

        // get shared preferences
        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked sendTextBtn");
                String sentText = "" + typeBoxEditText.getText().toString();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("message", sharedPreferences.getString("message", "") + '\n' + sentText);
                editor.apply();
                messageReceivedTextView.append(sentText+"\n");
                typeBoxEditText.setText("");

                if (BluetoothConnectionService.BluetoothConnectionStatus) {
                    // Use improved JSON detection
                    if (isValidJSON(sentText)) {
                        Log.d(TAG, "Sending as JSON: " + sentText);
                        BluetoothConnectionService.writeJson(sentText);
                    } else {
                        Log.d(TAG, "Sending as plain text: " + sentText);
                        byte[] bytes = sentText.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        BluetoothConnectionService.write(bytes);
                    }
                }
                showLog("Exiting sendTextBtn");
            }
        });

        return root;
    }

    // Improved JSON validation method
    private boolean isValidJSON(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        
        try {
            if (trimmed.startsWith("{")) {
                new JSONObject(trimmed);
                return true;
            } else if (trimmed.startsWith("[")) {
                new JSONArray(trimmed);
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.d(TAG, "JSON validation failed: " + e.getMessage());
            return false;
        }
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    public static TextView getMessageReceivedTextView() {
        return messageReceivedTextView;
    }

    public static EditText getTypeBoxEditText() {return typeBoxEditText;}

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("receivedMessage");
            Log.d(TAG, "Received message via broadcast: " + text);
            if (text != null && !text.trim().isEmpty()) {
                messageReceivedTextView.append(text+"\n");
            } else {
                Log.w(TAG, "Received null or empty message");
            }
        }
    };
}