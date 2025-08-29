package com.example.mdp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        ImageButton comms_button = findViewById(R.id.button1);
        ImageButton bluetooth_button = findViewById(R.id.button2);
        ImageButton arena_button = findViewById(R.id.button3);


        // On-click listeners
        comms_button.setOnClickListener(v -> openCommsView());
        bluetooth_button.setOnClickListener(v -> openBluetoothView());
        arena_button.setOnClickListener(v -> openArenaView());

        // Use LayoutInflater to get other views (arena)
        LayoutInflater myLayoutInflater = getLayoutInflater();
        View arenaView = myLayoutInflater.inflate(R.layout.arena, null);
    }

    public void openCommsView() {
        Intent intent = new Intent(this, Communication.class);
        startActivity(intent);
    }

    public void openBluetoothView() {
        Intent intent = new Intent(this, Bluetooth.class);
        startActivity(intent);
    }

    public void openArenaView() {
        Intent intent = new Intent(this, Arena.class);
        startActivity(intent);
    }

}