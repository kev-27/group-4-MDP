package com.example.mdp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ObjectAnimator;


import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.media.MediaPlayer;
import android.util.Log;
import java.nio.charset.Charset;

public class Arena extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";
    private static final String TAG = "Arena->DEBUG";
    public static boolean firstStart = true;

    MediaPlayer mp;
    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, message);
        }
    };

    public void savesdata() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat("obs1X", findViewById(R.id.obstacle1).getTranslationX());
        editor.putFloat("obs1Y", findViewById(R.id.obstacle1).getTranslationY());
        editor.putFloat("obs1Rotation", findViewById(R.id.obstacle1).getRotation());

        editor.putFloat("obs2X", findViewById(R.id.obstacle2).getTranslationX());
        editor.putFloat("obs2Y", findViewById(R.id.obstacle2).getTranslationY());
        editor.putFloat("obs2Rotation", findViewById(R.id.obstacle2).getRotation());

        editor.putFloat("obs3X", findViewById(R.id.obstacle3).getTranslationX());
        editor.putFloat("obs3Y", findViewById(R.id.obstacle3).getTranslationY());
        editor.putFloat("obs3Rotation", findViewById(R.id.obstacle3).getRotation());

        editor.putFloat("obs4X", findViewById(R.id.obstacle4).getTranslationX());
        editor.putFloat("obs4Y", findViewById(R.id.obstacle4).getTranslationY());
        editor.putFloat("obs4Rotation", findViewById(R.id.obstacle4).getRotation());

        editor.putFloat("obs5X", findViewById(R.id.obstacle5).getTranslationX());
        editor.putFloat("obs5Y", findViewById(R.id.obstacle5).getTranslationY());
        editor.putFloat("obs5Rotation", findViewById(R.id.obstacle5).getRotation());

        editor.putFloat("obs6X", findViewById(R.id.obstacle6).getTranslationX());
        editor.putFloat("obs6Y", findViewById(R.id.obstacle6).getTranslationY());
        editor.putFloat("obs6Rotation", findViewById(R.id.obstacle6).getRotation());

        editor.putFloat("obs7X", findViewById(R.id.obstacle7).getTranslationX());
        editor.putFloat("obs7Y", findViewById(R.id.obstacle7).getTranslationY());
        editor.putFloat("obs7Rotation", findViewById(R.id.obstacle7).getRotation());

        editor.putFloat("obs8X", findViewById(R.id.obstacle8).getTranslationX());
        editor.putFloat("obs8Y", findViewById(R.id.obstacle8).getTranslationY());
        editor.putFloat("obs8Rotation", findViewById(R.id.obstacle8).getRotation());

        editor.putFloat("carX", findViewById(R.id.car).getTranslationX());
        editor.putFloat("carY", findViewById(R.id.car).getTranslationY());
        editor.putFloat("carRotation", findViewById(R.id.car).getRotation());

        int x = (int) (car.getX() + SNAP_GRID_INTERVAL) / SNAP_GRID_INTERVAL;
        int y = (int) (car.getY() + SNAP_GRID_INTERVAL) / SNAP_GRID_INTERVAL;

        editor.putString("x_tv", String.valueOf(car_x.getText()));
        editor.putString("y_tv", String.valueOf(car_y.getText()));
        editor.putString("car_dir", String.valueOf(car_dir.getText()));
        editor.apply();
    }

    public void loaddata() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        obstacle1.setX(sharedPreferences.getFloat("obs1X", 0.0f));
        obstacle1.setY(sharedPreferences.getFloat("obs1Y", 0.0f));
        obstacle1.setRotation((sharedPreferences.getFloat("obs1Rotation", 0.0f)));

        obstacle2.setX(sharedPreferences.getFloat("obs2X", 0.0f));
        obstacle2.setY(sharedPreferences.getFloat("obs2Y", 0.0f));
        obstacle2.setRotation((sharedPreferences.getFloat("obs2Rotation", 0.0f)));

        obstacle3.setX(sharedPreferences.getFloat("obs3X", 0.0f));
        obstacle3.setY(sharedPreferences.getFloat("obs3Y", 0.0f));
        obstacle3.setRotation((sharedPreferences.getFloat("obs3Rotation", 0.0f)));

        obstacle4.setX(sharedPreferences.getFloat("obs4X", 0.0f));
        obstacle4.setY(sharedPreferences.getFloat("obs4Y", 0.0f));
        obstacle4.setRotation((sharedPreferences.getFloat("obs4Rotation", 0.0f)));

        obstacle5.setX(sharedPreferences.getFloat("obs5X", 0.0f));
        obstacle5.setY(sharedPreferences.getFloat("obs5Y", 0.0f));
        obstacle5.setRotation((sharedPreferences.getFloat("obs5Rotation", 0.0f)));

        obstacle6.setX(sharedPreferences.getFloat("obs6X", 0.0f));
        obstacle6.setY(sharedPreferences.getFloat("obs6Y", 0.0f));
        obstacle6.setRotation((sharedPreferences.getFloat("obs6Rotation", 0.0f)));

        obstacle7.setX(sharedPreferences.getFloat("obs7X", 0.0f));
        obstacle7.setY(sharedPreferences.getFloat("obs7Y", 0.0f));
        obstacle7.setRotation((sharedPreferences.getFloat("obs7Rotation", 0.0f)));

        obstacle8.setX(sharedPreferences.getFloat("obs8X", 0.0f));
        obstacle8.setY(sharedPreferences.getFloat("obs8Y", 0.0f));
        obstacle8.setRotation((sharedPreferences.getFloat("obs8Rotation", 0.0f)));

        car.setX(sharedPreferences.getFloat("carX", 0.0f));
        car.setY(sharedPreferences.getFloat("carY", 0.0f));
        car.setRotation(sharedPreferences.getFloat("carRotation", 0.0f));

        car_x.setText((sharedPreferences.getString("x_tv", "")));
        car_y.setText((sharedPreferences.getString("y_tv", "")));
        car_dir.setText((sharedPreferences.getString("car_dir", "")));
        Log.d(TAG, "obs1X: " + Float.toString(sharedPreferences.getFloat("obs1X", 0.0f)));
    }

    private static final int SNAP_GRID_INTERVAL = 35;
    private static final int ANIMATOR_DURATION = 1000;

    /*
     * start from (1,1)
     * NOTE: remember to invert y
     */
    private final int INITIAL_X = 1 * SNAP_GRID_INTERVAL - SNAP_GRID_INTERVAL;
    private final int INITIAL_Y = 18 * SNAP_GRID_INTERVAL - SNAP_GRID_INTERVAL;

    private boolean cansetobstacles = false;
    private String curMode = "IDLE";

    Button IRButton, SPButton, resetButton, preset1Button, setButton, timerButton, saveButton, sendButton, startButton, stopButton;
    ImageView obstacle1, obstacle2, obstacle3, obstacle4, obstacle5, obstacle6, obstacle7, obstacle8, car;
    TextView statusWindow, car_x, car_y, car_dir;

    Map<Integer, ImageView> obstacles;

    // RecyclerView
    //ArrayList<String> s1 = new ArrayList<String>();
    //ArrayList<Integer> images = new ArrayList<Integer>();
    //RecyclerView recyclerView;

    protected void onPause() {
        super.onPause();
        Log.d("onpause", "OnPause() called");
        savesdata();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Restore saved instance state
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");

        setContentView(R.layout.arena);

        // start listening for incoming messages
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter("incomingMessage"));

        initialiseObstacles();
        initialiseButtons();
        initialiseMovementButtons();

        if (!firstStart) {
            loaddata();
        } else {
            firstStart = false;
            savesdata();
        }
    }

    /**
     * Initializes obstacles and setup listeners
     */
    private void initialiseObstacles() {
        obstacle1 = findViewById(R.id.obstacle1);
        obstacle2 = findViewById(R.id.obstacle2);
        obstacle3 = findViewById(R.id.obstacle3);
        obstacle4 = findViewById(R.id.obstacle4);
        obstacle5 = findViewById(R.id.obstacle5);
        obstacle6 = findViewById(R.id.obstacle6);
        obstacle7 = findViewById(R.id.obstacle7);
        obstacle8 = findViewById(R.id.obstacle8);

        obstacles = new HashMap<Integer, ImageView>() {
            {
                put(1, obstacle1);
                put(2, obstacle2);
                put(3, obstacle3);
                put(4, obstacle4);
                put(5, obstacle5);
                put(6, obstacle6);
                put(7, obstacle7);
                put(8, obstacle8);
            }
        };

        // Setup obstacle click listeners for rotation
        setupObstacleRotationListeners();
        
        // Setup obstacle long press listeners for target face annotation
        setupObstacleTargetFaceListeners();

        // Setup obstacle touch listeners for movement
        setupObstacleTouchListeners();
    }

    /**
     * Setup obstacle rotation on click
     */
    private void setupObstacleRotationListeners() {
        obstacle1.setOnClickListener(view -> {
            obstacle1.setRotation((obstacle1.getRotation() + 90) % 360);
            int orientation = (int) obstacle1.getRotation();
            switch (((orientation / 90) % 4 + 4) % 4) {
                case 0:
                    obstacle1.setImageResource(Helper.resources.get("o1n"));
                    break;
                case 1:
                    obstacle1.setImageResource(Helper.resources.get("o1e"));
                    break;
                case 2:
                    obstacle1.setImageResource(Helper.resources.get("o1s"));
                    break;
                case 3:
                    obstacle1.setImageResource(Helper.resources.get("o1w"));
                    break;
            }
        });

        obstacle2.setOnClickListener(view -> {
            obstacle2.setRotation((obstacle2.getRotation() + 90) % 360);
            int orientation = (int) obstacle2.getRotation();
            switch (((orientation / 90) % 4 + 4) % 4) {
                case 0:
                    obstacle2.setImageResource(Helper.resources.get("o2n"));
                    break;
                case 1:
                    obstacle2.setImageResource(Helper.resources.get("o2e"));
                    break;
                case 2:
                    obstacle2.setImageResource(Helper.resources.get("o2s"));
                    break;
                case 3:
                    obstacle2.setImageResource(Helper.resources.get("o2w"));
                    break;
            }
        });

        obstacle3.setOnClickListener(view -> {
            obstacle3.setRotation((obstacle3.getRotation() + 90) % 360);
            int orientation = (int) obstacle3.getRotation();
            switch (((orientation / 90) % 4 + 4) % 4) {
                case 0:
                    obstacle3.setImageResource(Helper.resources.get("o3n"));
                    break;
                case 1:
                    obstacle3.setImageResource(Helper.resources.get("o3e"));
                    break;
                case 2:
                    obstacle3.setImageResource(Helper.resources.get("o3s"));
                    break;
                case 3:
                    obstacle3.setImageResource(Helper.resources.get("o3w"));
                    break;
            }
        });

        obstacle4.setOnClickListener(view -> {
            obstacle4.setRotation((obstacle4.getRotation() + 90) % 360);
            int orientation = (int) obstacle4.getRotation();
            switch (((orientation / 90) % 4 + 4) % 4) {
                case 0:
                    obstacle4.setImageResource(Helper.resources.get("o4n"));
                    break;
                case 1:
                    obstacle4.setImageResource(Helper.resources.get("o4e"));
                    break;
                case 2:
                    obstacle4.setImageResource(Helper.resources.get("o4s"));
                    break;
                case 3:
                    obstacle4.setImageResource(Helper.resources.get("o4w"));
                    break;
            }
        });

        obstacle5.setOnClickListener(view -> {
            obstacle5.setRotation((obstacle5.getRotation() + 90) % 360);
            int orientation = (int) obstacle5.getRotation();
            switch (((orientation / 90) % 4 + 4) % 4) {
                case 0:
                    obstacle5.setImageResource(Helper.resources.get("o5n"));
                    break;
                case 1:
                    obstacle5.setImageResource(Helper.resources.get("o5e"));
                    break;
                case 2:
                    obstacle5.setImageResource(Helper.resources.get("o5s"));
                    break;
                case 3:
                    obstacle5.setImageResource(Helper.resources.get("o5w"));
                    break;
            }
        });

        obstacle6.setOnClickListener(view -> {
            obstacle6.setRotation((obstacle6.getRotation() + 90) % 360);
            int orientation = (int) obstacle6.getRotation();
            switch (((orientation / 90) % 4 + 4) % 4) {
                case 0:
                    obstacle6.setImageResource(Helper.resources.get("o6n"));
                    break;
                case 1:
                    obstacle6.setImageResource(Helper.resources.get("o6e"));
                    break;
                case 2:
                    obstacle6.setImageResource(Helper.resources.get("o6s"));
                    break;
                case 3:
                    obstacle6.setImageResource(Helper.resources.get("o6w"));
                    break;
            }
        });

        obstacle7.setOnClickListener(view -> {
            obstacle7.setRotation((obstacle7.getRotation() + 90) % 360);
            int orientation = (int) obstacle7.getRotation();
            switch (((orientation / 90) % 4 + 4) % 4) {
                case 0:
                    obstacle7.setImageResource(Helper.resources.get("o7n"));
                    break;
                case 1:
                    obstacle7.setImageResource(Helper.resources.get("o7e"));
                    break;
                case 2:
                    obstacle7.setImageResource(Helper.resources.get("o7s"));
                    break;
                case 3:
                    obstacle7.setImageResource(Helper.resources.get("o7w"));
                    break;
            }
        });

        obstacle8.setOnClickListener(view -> {
            obstacle8.setRotation((obstacle8.getRotation() + 90) % 360);
            int orientation = (int) obstacle8.getRotation();
            switch (((orientation / 90) % 4 + 4) % 4) {
                case 0:
                    obstacle8.setImageResource(Helper.resources.get("o8n"));
                    break;
                case 1:
                    obstacle8.setImageResource(Helper.resources.get("o8e"));
                    break;
                case 2:
                    obstacle8.setImageResource(Helper.resources.get("o8s"));
                    break;
                case 3:
                    obstacle8.setImageResource(Helper.resources.get("o8w"));
                    break;
            }
        });
    }

    /**
     * Setup obstacle long press listeners for target face annotation
     */
    private void setupObstacleTargetFaceListeners() {
        // Long press on obstacle1 to annotate target face
        obstacle1.setOnLongClickListener(view -> {
            showTargetFaceDialog(1);
            return true;
        });

        obstacle2.setOnLongClickListener(view -> {
            showTargetFaceDialog(2);
            return true;
        });

        obstacle3.setOnLongClickListener(view -> {
            showTargetFaceDialog(3);
            return true;
        });

        obstacle4.setOnLongClickListener(view -> {
            showTargetFaceDialog(4);
            return true;
        });

        obstacle5.setOnLongClickListener(view -> {
            showTargetFaceDialog(5);
            return true;
        });

        obstacle6.setOnLongClickListener(view -> {
            showTargetFaceDialog(6);
            return true;
        });

        obstacle7.setOnLongClickListener(view -> {
            showTargetFaceDialog(7);
            return true;
        });

        obstacle8.setOnLongClickListener(view -> {
            showTargetFaceDialog(8);
            return true;
        });
    }

    /**
     * Show dialog to select target face direction
     */
    private void showTargetFaceDialog(int obstacleNumber) {
        String[] directions = {"North (N)", "East (E)", "South (S)", "West (W)"};
        String[] directionCodes = {"N", "E", "S", "W"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Select Target Face for Obstacle " + obstacleNumber);
        builder.setItems(directions, (dialog, which) -> {
            String selectedDirection = directionCodes[which];
            annotateTargetFace(obstacleNumber, selectedDirection);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Annotate target face and send via Bluetooth
     */
    private void annotateTargetFace(int obstacleNumber, String direction) {
        // Update obstacle appearance to show target face
        updateObstacleTargetFace(obstacleNumber, direction);
        
        // Send target face information via Bluetooth
        if (BluetoothService.BluetoothConnectionStatus) {
            String message = "FACE,B" + obstacleNumber + "," + direction;
            byte[] bytes = message.getBytes(java.nio.charset.Charset.defaultCharset());
            BluetoothService.write(bytes);
            Toast.makeText(this, "Target face annotated: " + message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Bluetooth not connected!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update obstacle appearance to show target face
     */
    private void updateObstacleTargetFace(int obstacleNumber, String direction) {
        ImageView obstacle = obstacles.get(obstacleNumber);
        if (obstacle != null) {
            // Add a visual indicator for the target face
            // This could be a border, different color, or overlay
            obstacle.setTag("TARGET_FACE_" + direction);
            
            // You can also add a visual indicator here
            // For example, set a different background or border
            obstacle.setBackgroundResource(R.drawable.target_face_indicator);
            
            Log.d(TAG, "Obstacle " + obstacleNumber + " target face set to " + direction);
        }
    }

    /**
     * Setup obstacle touch listeners for movement
     */
    private void setupObstacleTouchListeners() {
        obstacle1.setOnTouchListener(new View.OnTouchListener() {
            int x = 0;
            int y = 0;
            int dx = 0;
            int dy = 0;
            int orientation;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!cansetobstacles) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getX();
                        y = (int) event.getY();
                        orientation = (int) obstacle1.getRotation();
                        obstacle1.setRotation(0);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        dx = (int) event.getX() - x;
                        dy = (int) event.getY() - y;

                        obstacle1.setX(obstacle1.getX() + dx);
                        obstacle1.setY(obstacle1.getY() + dy);
                        break;
                    case MotionEvent.ACTION_UP:
                        int snapToX = ((int) ((obstacle1.getX() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        int snapToY = ((int) ((obstacle1.getY() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        Log.d(TAG, "obstacle1 is at " + snapToX + "," + snapToY);
                        obstacle1.setX(snapToX);
                        obstacle1.setY(snapToY);
                        obstacle1.setRotation(orientation % 360);
                        //sendObstacles();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        obstacle2.setOnTouchListener(new View.OnTouchListener() {
            int x = 0;
            int y = 0;
            int dx = 0;
            int dy = 0;
            int orientation;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!cansetobstacles) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getX();
                        y = (int) event.getY();
                        orientation = (int) obstacle2.getRotation();
                        obstacle2.setRotation(0);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        dx = (int) event.getX() - x;
                        dy = (int) event.getY() - y;

                        obstacle2.setX(obstacle2.getX() + dx);
                        obstacle2.setY(obstacle2.getY() + dy);
                        break;
                    case MotionEvent.ACTION_UP:
                        int snapToX = ((int) ((obstacle2.getX() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        int snapToY = ((int) ((obstacle2.getY() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        // Log.d(TAG, "obstacle2 is at " + snapToX + "," + snapToY);
                        obstacle2.setX(snapToX);
                        obstacle2.setY(snapToY);
                        obstacle2.setRotation(orientation % 360);
                        //sendObstacles();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        obstacle3.setOnTouchListener(new View.OnTouchListener() {
            int x = 0;
            int y = 0;
            int dx = 0;
            int dy = 0;
            int orientation;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!cansetobstacles) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getX();
                        y = (int) event.getY();
                        orientation = (int) obstacle3.getRotation();
                        obstacle3.setRotation(0);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        dx = (int) event.getX() - x;
                        dy = (int) event.getY() - y;

                        obstacle3.setX(obstacle3.getX() + dx);
                        obstacle3.setY(obstacle3.getY() + dy);
                        break;
                    case MotionEvent.ACTION_UP:
                        int snapToX = ((int) ((obstacle3.getX() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        int snapToY = ((int) ((obstacle3.getY() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        // Log.d(TAG, "obstacle3 is at " + snapToX + "," + snapToY);
                        obstacle3.setX(snapToX);
                        obstacle3.setY(snapToY);
                        obstacle3.setRotation(orientation % 360);
                        //sendObstacles();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        obstacle4.setOnTouchListener(new View.OnTouchListener() {
            int x = 0;
            int y = 0;
            int dx = 0;
            int dy = 0;
            int orientation;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!cansetobstacles) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getX();
                        y = (int) event.getY();
                        orientation = (int) obstacle4.getRotation();
                        obstacle4.setRotation(0);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        dx = (int) event.getX() - x;
                        dy = (int) event.getY() - y;

                        obstacle4.setX(obstacle4.getX() + dx);
                        obstacle4.setY(obstacle4.getY() + dy);
                        break;
                    case MotionEvent.ACTION_UP:
                        int snapToX = ((int) ((obstacle4.getX() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        int snapToY = ((int) ((obstacle4.getY() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        // Log.d(TAG, "obstacle4 is at " + snapToX + "," + snapToY);
                        obstacle4.setX(snapToX);
                        obstacle4.setY(snapToY);
                        obstacle4.setRotation(orientation % 360);
                        //sendObstacles();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        obstacle5.setOnTouchListener(new View.OnTouchListener() {
            int x = 0;
            int y = 0;
            int dx = 0;
            int dy = 0;
            int orientation;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!cansetobstacles) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getX();
                        y = (int) event.getY();
                        orientation = (int) obstacle5.getRotation();
                        obstacle5.setRotation(0);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        dx = (int) event.getX() - x;
                        dy = (int) event.getY() - y;

                        obstacle5.setX(obstacle5.getX() + dx);
                        obstacle5.setY(obstacle5.getY() + dy);
                        break;
                    case MotionEvent.ACTION_UP:
                        int snapToX = ((int) ((obstacle5.getX() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        int snapToY = ((int) ((obstacle5.getY() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        // Log.d(TAG, "obstacle5 is at " + snapToX + "," + snapToY);
                        obstacle5.setX(snapToX);
                        obstacle5.setY(snapToY);
                        obstacle5.setRotation(orientation % 360);
                        //sendObstacles();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        obstacle6.setOnTouchListener(new View.OnTouchListener() {
            int x = 0;
            int y = 0;
            int dx = 0;
            int dy = 0;
            int orientation;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!cansetobstacles) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getX();
                        y = (int) event.getY();
                        orientation = (int) obstacle6.getRotation();
                        obstacle6.setRotation(0);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        dx = (int) event.getX() - x;
                        dy = (int) event.getY() - y;

                        obstacle6.setX(obstacle6.getX() + dx);
                        obstacle6.setY(obstacle6.getY() + dy);
                        break;
                    case MotionEvent.ACTION_UP:
                        int snapToX = ((int) ((obstacle6.getX() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        int snapToY = ((int) ((obstacle6.getY() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        // Log.d(TAG, "obstacle6 is at " + snapToX + "," + snapToY);
                        obstacle6.setX(snapToX);
                        obstacle6.setY(snapToY);
                        obstacle6.setRotation(orientation % 360);
                        //sendObstacles();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        obstacle7.setOnTouchListener(new View.OnTouchListener() {
            int x = 0;
            int y = 0;
            int dx = 0;
            int dy = 0;
            int orientation;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!cansetobstacles) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getX();
                        y = (int) event.getY();
                        orientation = (int) obstacle7.getRotation();
                        obstacle7.setRotation(0);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        dx = (int) event.getX() - x;
                        dy = (int) event.getY() - y;

                        obstacle7.setX(obstacle7.getX() + dx);
                        obstacle7.setY(obstacle7.getY() + dy);
                        break;
                    case MotionEvent.ACTION_UP:
                        int snapToX = ((int) ((obstacle7.getX() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        int snapToY = ((int) ((obstacle7.getY() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        // Log.d(TAG, "obstacle7 is at " + snapToX + "," + snapToY);
                        obstacle7.setX(snapToX);
                        obstacle7.setY(snapToY);
                        obstacle7.setRotation(orientation % 360);
                        //sendObstacles();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        obstacle8.setOnTouchListener(new View.OnTouchListener() {
            int x = 0;
            int y = 0;
            int dx = 0;
            int dy = 0;
            int orientation;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!cansetobstacles) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getX();
                        y = (int) event.getY();
                        orientation = (int) obstacle8.getRotation();
                        obstacle8.setRotation(0);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        dx = (int) event.getX() - x;
                        dy = (int) event.getY() - y;

                        obstacle8.setX(obstacle8.getX() + dx);
                        obstacle8.setY(obstacle8.getY() + dy);
                        break;
                    case MotionEvent.ACTION_UP:
                        int snapToX = ((int) ((obstacle8.getX() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        int snapToY = ((int) ((obstacle8.getY() + SNAP_GRID_INTERVAL / 2) / SNAP_GRID_INTERVAL))
                                * SNAP_GRID_INTERVAL;
                        // Log.d(TAG, "obstacle8 is at " + snapToX + "," + snapToY);
                        obstacle8.setX(snapToX);
                        obstacle8.setY(snapToY);
                        obstacle8.setRotation(orientation % 360);
                        //sendObstacles();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    /*
     * Set obstacle ID images
     */
    private void setObstacleImageID(int obstacleNumber, String image) {
        int orientation = (int) obstacles.get(obstacleNumber).getRotation();
        ImageView iv = obstacles.get(obstacleNumber);
        Log.d("Obstacle ID", String.valueOf(iv.getId()));
        try {
            if (orientation == 0) {
                obstacles.get(obstacleNumber).setImageResource(Helper.resources.get(image + "n"));
            } else if (orientation == 90) {
                obstacles.get(obstacleNumber).setImageResource(Helper.resources.get(image + "e"));
            } else if (orientation == 180) {
                obstacles.get(obstacleNumber).setImageResource(Helper.resources.get(image + "s"));
            } else if (orientation == 270) {
                obstacles.get(obstacleNumber).setImageResource(Helper.resources.get(image + "w"));
            } else {
                obstacles.get(obstacleNumber).setImageResource(Helper.resources.get(image));
                obstacles.get(obstacleNumber).setRotation(0);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /*
     * Initializes the arrow buttons
     */
    private void initialiseMovementButtons() {
        ImageButton forwardButton = (ImageButton) findViewById(R.id.forwardButton);
        forwardButton.setOnClickListener(v -> {
            Log.d(TAG, "forward");

            // Bluetooth message
            if (BluetoothService.BluetoothConnectionStatus) {
                // byte[] bytes = "STM:w100n".getBytes(Charset.defaultCharset());
                byte[] bytes = "STM:n".getBytes(Charset.defaultCharset());
                BluetoothService.write(bytes);
            }

            // Animation
            forwardButton(1);
        });

        ImageButton reverseButton = (ImageButton) findViewById(R.id.reverseButton);
        reverseButton.setOnClickListener(v -> {
            Log.d(TAG, "reverse");

            // Bluetooth message
            if (BluetoothService.BluetoothConnectionStatus) {
                // byte[] bytes = "STM:s100n".getBytes(Charset.defaultCharset());
                byte[] bytes = "STM:s".getBytes(Charset.defaultCharset());
                BluetoothService.write(bytes);
            }

            // Animation
            reverseButton(1);
        });

        ImageButton leftButton = (ImageButton) findViewById(R.id.leftButton);
        leftButton.setOnClickListener(v -> {
            Log.d(TAG, "left");

            if (BluetoothService.BluetoothConnectionStatus) {
                // byte[] bytes = "STM:ln".getBytes(Charset.defaultCharset());
                byte[] bytes = "STM:w".getBytes(Charset.defaultCharset());
                BluetoothService.write(bytes);
            }

            leftCommand();
        });

        ImageButton rightButton = (ImageButton) findViewById(R.id.rightButton);
        rightButton.setOnClickListener(v -> {
            Log.d(TAG, "right");

            if (BluetoothService.BluetoothConnectionStatus) {
                // byte[] bytes = "STM:rn".getBytes(Charset.defaultCharset());
                byte[] bytes = "STM:e".getBytes(Charset.defaultCharset());
                BluetoothService.write(bytes);
            }

            rightCommand();
        });
    }

    /**
     * Initalizes buttons, car and setup listeners
     */
    private void initialiseButtons() {
        // Declarations
        car = findViewById(R.id.car);
        car_x = findViewById(R.id.x_tv);
        car_y = findViewById(R.id.y_tv);
        car_dir = findViewById(R.id.dir_tv);
        IRButton = findViewById(R.id.IRButton);
        SPButton = findViewById(R.id.SPBtn);
        resetButton = findViewById(R.id.resetButton);
        preset1Button = findViewById(R.id.preset1Button);
        setButton = findViewById(R.id.setButton);
        saveButton = findViewById(R.id.saveButton);
        sendButton = findViewById(R.id.sendButton);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        timerButton = findViewById(R.id.timerButton);
        statusWindow = findViewById(R.id.statusWindowText);

        // Events
        IRButton.setOnClickListener(view -> beginIRTask());
        SPButton.setOnClickListener(view -> beginSPTask());
        resetButton.setOnClickListener(view -> setResetButton());
        preset1Button.setOnClickListener(view -> setPreset1Button());
        setButton.setOnClickListener(view -> toggleSetMode());
        saveButton.setOnClickListener(view -> sendObstacles());
        sendButton.setOnClickListener(view -> sendObstaclesData());
        startButton.setOnClickListener(view -> startTask2());
        stopButton.setOnClickListener(view -> stopMusic());
        timerButton.setOnClickListener(view -> stopTimerButton());

        // Initialize car to bottom left
        car.setX(INITIAL_X);
        car.setY(INITIAL_Y);
        updateXYDirText();
    }

    private void startTask2() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("S");
        Log.d(TAG, stringBuilder.toString());
        if (BluetoothService.BluetoothConnectionStatus) {
            byte[] bytes = stringBuilder.toString().getBytes(Charset.defaultCharset());
            BluetoothService.write(bytes);
            Toast.makeText(Arena.this, "Start Task 2", Toast.LENGTH_SHORT).show();

            // Optional: simple audio feedback if resource exists
            try {
                MediaPlayer mediaPlayer = MediaPlayer.create(Arena.this, R.raw.potc);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            } catch (Exception e) {
                Log.d(TAG, "Audio playback not available");
            }
        }
    }

    private void stopMusic() {
        mp.release();
        startButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.INVISIBLE);
    }

    /*
     * Function to wait for certain amount of time
     */
    private void sleepFor(int time) {
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * ======================
     * For movement commands:
     * 0 - North
     * 1 - East
     * 2 - South
     * 3 - West
     * ======================
     */

    private void forwardButton(int noOfGrids) {
        int orientation = (int) car.getRotation();
        int new_x, new_y;
        ObjectAnimator animator;
        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                new_y = (int) car.getY() - noOfGrids * SNAP_GRID_INTERVAL;
                car.setY(new_y);
                animator = ObjectAnimator.ofFloat(car, "y", new_y);
                animator.setDuration(noOfGrids * ANIMATOR_DURATION);
                animator.start();
                updateXYDirText();
                break;
            case 1:
                new_x = (int) car.getX() + noOfGrids * SNAP_GRID_INTERVAL;
                car.setX(new_x);
                animator = ObjectAnimator.ofFloat(car, "x", new_x);
                animator.setDuration(noOfGrids * ANIMATOR_DURATION);
                animator.start();
                updateXYDirText();
                break;
            case 2:
                new_y = (int) car.getY() + noOfGrids * SNAP_GRID_INTERVAL;
                car.setY(new_y);
                animator = ObjectAnimator.ofFloat(car, "y", new_y);
                animator.setDuration(noOfGrids * ANIMATOR_DURATION);
                animator.start();
                updateXYDirText();
                break;
            case 3:
                new_x = (int) car.getX() - noOfGrids * SNAP_GRID_INTERVAL;
                car.setX(new_x);
                animator = ObjectAnimator.ofFloat(car, "x", new_x);
                animator.setDuration(noOfGrids * ANIMATOR_DURATION);
                animator.start();
                updateXYDirText();
                break;
            default:
                // Shouldn't reach this case
                break;
        }
    }

    private void reverseButton(int noOfGrids) {
        int orientation = (int) car.getRotation();
        int new_x, new_y;
        ObjectAnimator animator;
        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                new_y = (int) car.getY() + noOfGrids * SNAP_GRID_INTERVAL;
                car.setY(new_y);
                animator = ObjectAnimator.ofFloat(car, "y", new_y);
                animator.setDuration(noOfGrids * ANIMATOR_DURATION);
                animator.start();
                updateXYDirText();
                break;
            case 1:
                new_x = (int) car.getX() - noOfGrids * SNAP_GRID_INTERVAL;
                car.setX(new_x);
                animator = ObjectAnimator.ofFloat(car, "x", new_x);
                animator.setDuration(noOfGrids * ANIMATOR_DURATION);
                animator.start();
                updateXYDirText();
                break;
            case 2:
                new_y = (int) car.getY() - noOfGrids * SNAP_GRID_INTERVAL;
                car.setY(new_y);
                animator = ObjectAnimator.ofFloat(car, "y", new_y);
                animator.setDuration(noOfGrids * ANIMATOR_DURATION);
                animator.start();
                updateXYDirText();
                break;
            case 3:
                new_x = (int) car.getX() + noOfGrids * SNAP_GRID_INTERVAL;
                car.setX(new_x);
                animator = ObjectAnimator.ofFloat(car, "x", new_x);
                animator.setDuration(noOfGrids * ANIMATOR_DURATION);
                animator.start();
                updateXYDirText();
                break;
            default:
                // Shouldn't reach this case
                break;
        }
    }

    public void leftCommand() {
        int orientation = (int) car.getRotation();
        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                car.setRotation(270);
                break;
            case 1:
                car.setRotation(0);
                break;
            case 2:
                car.setRotation(90);
                break;
            case 3:
                car.setRotation(180);
                break;
            default:
                // Shouldn't reach this case
                break;
        }

        updateXYDirText();
    };

    private void rightCommand() {
        int orientation = (int) car.getRotation();
        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                car.setRotation(90);
                break;
            case 1:
                car.setRotation(180);
                break;
            case 2:
                car.setRotation(270);
                break;
            case 3:
                car.setRotation(0);
                break;
            default:
                // Shouldn't reach this case
                break;
        }

        updateXYDirText();
    }

    /*
     * Slide left - forward
     */
    public void leftSlideCommand() {
        int orientation = (int) car.getRotation();
        int new_x, new_y;
        AnimatorSet animatorSet;
        ObjectAnimator moveStraightAnimator, moveStraightAgainAnimator;

        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                new_y = (int) car.getY() - 4 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 1:
                new_y = (int) car.getY() - SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 4 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 2:
                new_y = (int) car.getY() + 4 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 3:
                new_y = (int) car.getY() + SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 4 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            default:
                // Shouldn't reach this case
                break;
        }
    }

    /*
     * Slide right - forward
     */
    private void rightSlideCommand() {
        int orientation = (int) car.getRotation();
        int new_x, new_y;
        AnimatorSet animatorSet;
        ObjectAnimator moveStraightAnimator, moveStraightAgainAnimator;

        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                new_y = (int) car.getY() - 4 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 1:
                new_y = (int) car.getY() + SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 4 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 2:
                new_y = (int) car.getY() + 4 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 3:
                new_y = (int) car.getY() - SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 4 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            default:
                // Shouldn't reach this case
                break;
        }
        updateXYDirText();
    }

    /*
     * Medium turn left
     */
    public void leftMidButtonCommand() {
        int orientation = (int) car.getRotation();
        int new_x, new_y;
        AnimatorSet animatorSet;
        ObjectAnimator moveStraightAnimator, turnAnimator, moveStraightAgainAnimator;

        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                new_y = (int) car.getY() - 2 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 3 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", -90);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(270);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 1:
                new_y = (int) car.getY() - 3 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 2 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 0);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(0);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 2:
                new_y = (int) car.getY() + 2 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 3 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 90);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(90);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 3:
                new_y = (int) car.getY() + 3 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 2 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 180);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(180);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            default:
                // Shouldn't reach this case
                break;
        }
    }

    /*
     * Medium turn right
     */
    public void rightMidButtonCommand() {
        int orientation = (int) car.getRotation();
        int new_x, new_y;
        AnimatorSet animatorSet;
        ObjectAnimator moveStraightAnimator, turnAnimator, moveStraightAgainAnimator;

        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                new_y = (int) car.getY() - 2 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 3 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 90);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(90);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 1:
                new_y = (int) car.getY() + 3 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 2 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 180);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(180);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 2:
                new_y = (int) car.getY() + 2 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 3 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 270);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(270);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 3:
                new_y = (int) car.getY() - 3 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 2 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 360);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(0);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            default:
                // Shouldn't reach this case
                break;
        }
    }

    /*
     * Slide left - reverse
     */
    public void leftSlideReverseCommand() {
        int orientation = (int) car.getRotation();
        int new_x, new_y;
        AnimatorSet animatorSet;
        ObjectAnimator moveStraightAnimator, moveStraightAgainAnimator;

        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                new_y = (int) car.getY() + 4 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 1:
                new_y = (int) car.getY() - SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 4 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 2:
                new_y = (int) car.getY() - 4 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 3:
                new_y = (int) car.getY() + SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 4 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            default:
                // Shouldn't reach this case
                break;
        }
    }

    /*
     * Slide right - reverse
     */
    private void rightSlideReverseCommand() {
        int orientation = (int) car.getRotation();
        int new_x, new_y;
        AnimatorSet animatorSet;
        ObjectAnimator moveStraightAnimator, moveStraightAgainAnimator;

        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                new_y = (int) car.getY() + 4 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 1:
                new_y = (int) car.getY() + SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 4 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 2:
                new_y = (int) car.getY() - 4 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 3:
                new_y = (int) car.getY() - SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 4 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            default:
                // Shouldn't reach this case
                break;
        }
        updateXYDirText();
    }

    /*
     * reverse left
     */
    public void leftMidReverseButtonCommand() {
        int orientation = (int) car.getRotation();
        int new_x, new_y;
        AnimatorSet animatorSet;
        ObjectAnimator moveStraightAnimator, turnAnimator, moveStraightAgainAnimator;

        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                new_y = (int) car.getY() + 3 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 2 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 90);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(90);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 1:
                new_y = (int) car.getY() - 2 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 3 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 180);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(180);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 2:
                new_y = (int) car.getY() - 3 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 2 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 270);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(270);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 3:
                new_y = (int) car.getY() + 2 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 3 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 360);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(0);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            default:
                // Shouldn't reach this case
                break;
        }
    }

    /*
     * reverse right
     */
    public void rightMidReverseButtonCommand() {
        int orientation = (int) car.getRotation();
        int new_x, new_y;
        AnimatorSet animatorSet;
        ObjectAnimator moveStraightAnimator, turnAnimator, moveStraightAgainAnimator;

        switch (((orientation / 90) % 4 + 4) % 4) {
            case 0:
                new_y = (int) car.getY() + 3 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 2 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", -90);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(270);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 1:
                new_y = (int) car.getY() + 2 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 3 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 0);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(0);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 2:
                new_y = (int) car.getY() - 3 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() - 2 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 90);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(90);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            case 3:
                new_y = (int) car.getY() - 2 * SNAP_GRID_INTERVAL;
                new_x = (int) car.getX() + 3 * SNAP_GRID_INTERVAL;

                moveStraightAnimator = ObjectAnimator.ofFloat(car, "x", new_x);
                moveStraightAnimator.setDuration(ANIMATOR_DURATION);

                turnAnimator = ObjectAnimator.ofFloat(car, "rotation", 180);
                turnAnimator.setDuration(500);

                moveStraightAgainAnimator = ObjectAnimator.ofFloat(car, "y", new_y);
                moveStraightAgainAnimator.setDuration(ANIMATOR_DURATION);

                animatorSet = new AnimatorSet();
                animatorSet.playSequentially(moveStraightAnimator, turnAnimator, moveStraightAgainAnimator);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        car.setY(new_y);
                        car.setX(new_x);
                        car.setRotation(180);
                        updateXYDirText();
                    }
                });
                animatorSet.start();
                break;
            default:
                // Shouldn't reach this case
                break;
        }
    }

    private void stopTimerButton() {
        Chronometer IRTimer = (Chronometer) findViewById(R.id.IRTimer);
        IRTimer.stop();
        updateStatusWindow("Ready");
    }

    private void beginIRTask() {
        String IRstart = "ALG:START";

        if (BluetoothService.BluetoothConnectionStatus) {
            // Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_SHORT).show();
//            byte[] bytes = IRstart.getBytes(Charset.defaultCharset());
//            BluetoothService.write(bytes);
            sendObstacles();
            Toast.makeText(Arena.this, "Obstacles sent", Toast.LENGTH_SHORT).show();
            updateStatusWindow("IR Started");
        } else {
            updateStatusWindow("IR Not Started");
            Toast.makeText(Arena.this, "Please connect to Bluetooth.", Toast.LENGTH_SHORT).show();
            sleepFor(ANIMATOR_DURATION);
            updateStatusWindow("Ready");
            return;
        }

        Chronometer IRTimer = (Chronometer) findViewById(R.id.IRTimer);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        IRTimer.setBase(elapsedRealtime);
        IRTimer.start();
    }

    private void beginSPTask() {
        if (BluetoothService.BluetoothConnectionStatus) {
            byte[] bytes = "STM:sp".getBytes(Charset.defaultCharset());
            BluetoothService.write(bytes);
            Toast.makeText(Arena.this, "Shortest Path Started.", Toast.LENGTH_SHORT).show();
            updateStatusWindow("SP Started");
        } else {
            updateStatusWindow("SP Not Started");
            Toast.makeText(Arena.this, "Please connect to Bluetooth.", Toast.LENGTH_SHORT).show();
            sleepFor(ANIMATOR_DURATION);
            updateStatusWindow("Ready");
            return;
        }

        Chronometer IRTimer = (Chronometer) findViewById(R.id.IRTimer);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        IRTimer.setBase(elapsedRealtime);
        IRTimer.start();
    }

    private void setResetButton() {
        // Reset Timer
        Chronometer IRTimer = (Chronometer) findViewById(R.id.IRTimer);
        IRTimer.setBase(SystemClock.elapsedRealtime());
        IRTimer.stop();
        updateStatusWindow("Ready");

        // Hard coded
        obstacle1.setTranslationX(0);
        obstacle1.setTranslationY(0);

        obstacle2.setTranslationX(0);
        obstacle2.setTranslationY(0);

        obstacle3.setTranslationX(0);
        obstacle3.setTranslationY(0);

        obstacle4.setTranslationX(0);
        obstacle4.setTranslationY(0);

        obstacle5.setTranslationX(0);
        obstacle5.setTranslationY(0);

        obstacle6.setTranslationX(0);
        obstacle6.setTranslationY(0);

        obstacle7.setTranslationX(0);
        obstacle7.setTranslationY(0);

        obstacle8.setTranslationX(0);
        obstacle8.setTranslationY(0);

        car.setX(INITIAL_X);
        car.setY(INITIAL_Y);
        car.setRotation(0);
        updateXYDirText();

        obstacle1.setImageResource(Helper.resources.get("o1n"));
        obstacle1.setTag(Helper.resources.get("o1n"));
        obstacle2.setImageResource(Helper.resources.get("o2n"));
        obstacle2.setTag(Helper.resources.get("o2n"));
        obstacle3.setImageResource(Helper.resources.get("o3n"));
        obstacle3.setTag(Helper.resources.get("o3n"));
        obstacle4.setImageResource(Helper.resources.get("o4n"));
        obstacle4.setTag(Helper.resources.get("o4n"));
        obstacle5.setImageResource(Helper.resources.get("o5n"));
        obstacle5.setTag(Helper.resources.get("o5n"));
        obstacle6.setImageResource(Helper.resources.get("o6n"));
        obstacle6.setTag(Helper.resources.get("o6n"));
        obstacle7.setImageResource(Helper.resources.get("o7n"));
        obstacle7.setTag(Helper.resources.get("o7n"));
        obstacle8.setImageResource(Helper.resources.get("o8n"));
        obstacle1.setTag(Helper.resources.get("o8n"));

        obstacle1.setRotation(0);
        obstacle2.setRotation(0);
        obstacle3.setRotation(0);
        obstacle4.setRotation(0);
        obstacle5.setRotation(0);
        obstacle6.setRotation(0);
        obstacle7.setRotation(0);
        obstacle8.setRotation(0);

        Toast.makeText(this, "Map Reset", Toast.LENGTH_SHORT).show();
    }

    private void setPreset1Button() {
        updateStatusWindow("Ready");

        obstacle1.setX(350);
        obstacle1.setY(70);
        obstacle1.setRotation(180);
        obstacle1.setImageResource(Helper.resources.get("o1s"));

        obstacle2.setX(595);
        obstacle2.setY(70);
        obstacle2.setRotation(270);
        obstacle2.setImageResource(Helper.resources.get("o2w"));

        obstacle3.setX(70);
        obstacle3.setY(105);
        obstacle3.setRotation(180);
        obstacle3.setImageResource(Helper.resources.get("o3s"));

        obstacle4.setX(560);
        obstacle4.setY(525);
        obstacle4.setRotation(180);
        obstacle4.setImageResource(Helper.resources.get("o4s"));

        obstacle5.setX(455);
        obstacle5.setY(630);
        obstacle5.setRotation(270);
        obstacle5.setImageResource(Helper.resources.get("o5w"));

        obstacle6.setX(210);
        obstacle6.setY(455);
        obstacle6.setRotation(0);
        obstacle6.setImageResource(Helper.resources.get("o6n"));

        obstacle7.setX(315);
        obstacle7.setY(280);
        obstacle7.setRotation(270);
        obstacle7.setImageResource(Helper.resources.get("o7w"));

        obstacle8.setX(105);
        obstacle8.setY(560);
        obstacle8.setRotation(90);
        obstacle8.setImageResource(Helper.resources.get("o8e"));

        Toast.makeText(Arena.this, "Preset 1 Applied", Toast.LENGTH_SHORT).show();
    }

    /*
     * TODO: Figure out what I want to do with this shit
     */
    public String[][] savedPreset = {};

    private void setPreset2Button() {
        if (savedPreset.length == 0) {
            Toast.makeText(this, "No saved preset found", Toast.LENGTH_SHORT).show();
        } else {
            String[] obstacle1data = savedPreset[0];
            obstacle1.setX(Integer.parseInt(obstacle1data[0]));
            obstacle1.setY(Integer.parseInt(obstacle1data[1]));
            Log.d(TAG, "Obstacle1 should be at " + obstacle1data[0] + "," + obstacle1data[1]);
            switch (obstacle1data[2]) {
                case ("N"):
                    obstacle1.setRotation(0);
                    obstacle1.setImageResource(Helper.resources.get("o1n"));
                    break;
                case ("E"):
                    obstacle1.setRotation(90);
                    obstacle1.setImageResource(Helper.resources.get("o1e"));
                    break;
                case ("S"):
                    obstacle1.setRotation(180);
                    obstacle1.setImageResource(Helper.resources.get("o1s"));
                    break;
                case ("W"):
                    obstacle1.setRotation(270);
                    obstacle1.setImageResource(Helper.resources.get("o1w"));
                    break;
                default:
                    break;
            }
            String[] obstacle2data = savedPreset[1];
            obstacle2.setX(Integer.parseInt(obstacle2data[0]));
            obstacle2.setY(Integer.parseInt(obstacle2data[1]));
            Log.d(TAG, "obstacle2 should be at " + obstacle2data[0] + "," + obstacle2data[1]);
            switch (obstacle2data[2]) {
                case ("N"):
                    obstacle2.setRotation(0);
                    obstacle2.setImageResource(Helper.resources.get("o2n"));
                    break;
                case ("E"):
                    obstacle2.setRotation(90);
                    obstacle2.setImageResource(Helper.resources.get("o2e"));
                    break;
                case ("S"):
                    obstacle2.setRotation(180);
                    obstacle2.setImageResource(Helper.resources.get("o2s"));
                    break;
                case ("W"):
                    obstacle2.setRotation(270);
                    obstacle2.setImageResource(Helper.resources.get("o2w"));
                    break;
                default:
                    break;
            }
            String[] obstacle3data = savedPreset[2];
            obstacle3.setX(Integer.parseInt(obstacle3data[0]));
            obstacle3.setY(Integer.parseInt(obstacle3data[1]));
            Log.d(TAG, "obstacle3 should be at " + obstacle3data[0] + "," + obstacle3data[1]);
            switch (obstacle3data[2]) {
                case ("N"):
                    obstacle3.setRotation(0);
                    obstacle3.setImageResource(Helper.resources.get("o3n"));
                    break;
                case ("E"):
                    obstacle3.setRotation(90);
                    obstacle3.setImageResource(Helper.resources.get("o3e"));
                    break;
                case ("S"):
                    obstacle3.setRotation(180);
                    obstacle3.setImageResource(Helper.resources.get("o3s"));
                    break;
                case ("W"):
                    obstacle3.setRotation(270);
                    obstacle3.setImageResource(Helper.resources.get("o3w"));
                    break;
                default:
                    break;
            }
            String[] obstacle4data = savedPreset[3];
            obstacle4.setX(Integer.parseInt(obstacle4data[0]));
            obstacle4.setY(Integer.parseInt(obstacle4data[1]));
            Log.d(TAG, "obstacle4 should be at " + obstacle4data[0] + "," + obstacle4data[1]);
            switch (obstacle4data[2]) {
                case ("N"):
                    obstacle4.setRotation(0);
                    obstacle4.setImageResource(Helper.resources.get("o4n"));
                    break;
                case ("E"):
                    obstacle4.setRotation(90);
                    obstacle4.setImageResource(Helper.resources.get("o4e"));
                    break;
                case ("S"):
                    obstacle4.setRotation(180);
                    obstacle4.setImageResource(Helper.resources.get("o4s"));
                    break;
                case ("W"):
                    obstacle4.setRotation(270);
                    obstacle4.setImageResource(Helper.resources.get("o4w"));
                    break;
                default:
                    break;
            }
            String[] obstacle5data = savedPreset[4];
            obstacle5.setX(Integer.parseInt(obstacle5data[0]));
            obstacle5.setY(Integer.parseInt(obstacle5data[1]));
            Log.d(TAG, "obstacle5 should be at " + obstacle5data[0] + "," + obstacle5data[1]);
            switch (obstacle5data[2]) {
                case ("N"):
                    obstacle5.setRotation(0);
                    obstacle5.setImageResource(Helper.resources.get("o5n"));
                    break;
                case ("E"):
                    obstacle5.setRotation(90);
                    obstacle5.setImageResource(Helper.resources.get("o5e"));
                    break;
                case ("S"):
                    obstacle5.setRotation(180);
                    obstacle5.setImageResource(Helper.resources.get("o5s"));
                    break;
                case ("W"):
                    obstacle5.setRotation(270);
                    obstacle5.setImageResource(Helper.resources.get("o5w"));
                    break;
                default:
                    break;
            }
            String[] obstacle6data = savedPreset[5];
            obstacle6.setX(Integer.parseInt(obstacle6data[0]));
            obstacle6.setY(Integer.parseInt(obstacle6data[1]));
            Log.d(TAG, "obstacle6 should be at " + obstacle6data[0] + "," + obstacle6data[1]);
            switch (obstacle6data[2]) {
                case ("N"):
                    obstacle6.setRotation(0);
                    obstacle6.setImageResource(Helper.resources.get("o6n"));
                    break;
                case ("E"):
                    obstacle6.setRotation(90);
                    obstacle6.setImageResource(Helper.resources.get("o6e"));
                    break;
                case ("S"):
                    obstacle6.setRotation(180);
                    obstacle6.setImageResource(Helper.resources.get("o6s"));
                    break;
                case ("W"):
                    obstacle6.setRotation(270);
                    obstacle6.setImageResource(Helper.resources.get("o6w"));
                    break;
                default:
                    break;
            }
            String[] obstacle7data = savedPreset[6];
            obstacle7.setX(Integer.parseInt(obstacle7data[0]));
            obstacle7.setY(Integer.parseInt(obstacle7data[1]));
            Log.d(TAG, "obstacle7 should be at " + obstacle7data[0] + "," + obstacle7data[1]);
            switch (obstacle7data[2]) {
                case ("N"):
                    obstacle7.setRotation(0);
                    obstacle7.setImageResource(Helper.resources.get("o7n"));
                    break;
                case ("E"):
                    obstacle7.setRotation(90);
                    obstacle7.setImageResource(Helper.resources.get("o7e"));
                    break;
                case ("S"):
                    obstacle7.setRotation(180);
                    obstacle7.setImageResource(Helper.resources.get("o7s"));
                    break;
                case ("W"):
                    obstacle7.setRotation(270);
                    obstacle7.setImageResource(Helper.resources.get("o7w"));
                    break;
                default:
                    break;
            }
            String[] obstacle8data = savedPreset[7];
            obstacle8.setX(Integer.parseInt(obstacle8data[0]));
            obstacle8.setY(Integer.parseInt(obstacle8data[1]));
            Log.d(TAG, "obstacle8 should be at " + obstacle8data[0] + "," + obstacle8data[1]);
            switch (obstacle8data[2]) {
                case ("N"):
                    obstacle8.setRotation(0);
                    obstacle8.setImageResource(Helper.resources.get("o8n"));
                    break;
                case ("E"):
                    obstacle8.setRotation(90);
                    obstacle8.setImageResource(Helper.resources.get("o8e"));
                    break;
                case ("S"):
                    obstacle8.setRotation(180);
                    obstacle8.setImageResource(Helper.resources.get("o8s"));
                    break;
                case ("W"):
                    obstacle8.setRotation(270);
                    obstacle8.setImageResource(Helper.resources.get("o8w"));
                    break;
                default:
                    break;
            }

            Toast.makeText(this, "Preset 2 Applied", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleSetMode() {
        cansetobstacles = !cansetobstacles;
        if (curMode.equals("IDLE")) {
            curMode = "SET";
            setButton.setText("Done");
            Toast.makeText(this, "In set mode", Toast.LENGTH_SHORT).show();
        } else if (curMode.equals("SET")) {
            curMode = "IDLE";
            setButton.setText("Set");
            Toast.makeText(this, "Obstacles set", Toast.LENGTH_SHORT).show();
        }
    }

    private void setObstacles(String[] obstaclesPreset) {
        if (obstaclesPreset.length == 0) {
            Toast.makeText(this, "No saved preset found", Toast.LENGTH_SHORT).show();
        } else {
            for (int i = 0; i < obstaclesPreset.length; i++) {
                int obstaclenum = i + 1;
                String[] obsdata = obstaclesPreset[i].split(",");
                Log.d(TAG, "Obstacle preset length is " + obstaclesPreset.length);
                Log.d(TAG, "obstacle " + obstaclenum + " data is " + Arrays.toString(obsdata));

                obstacles.get(obstaclenum).setX(Integer.parseInt(obsdata[0]) * 40);
                obstacles.get(obstaclenum).setY(Integer.parseInt(obsdata[1]) * 40);
                switch (obsdata[2]) {
                    case ("N"):
                        obstacles.get(obstaclenum).setRotation(0);
                        obstacles.get(obstaclenum).setImageResource(Helper.resources.get("o" + obstaclenum + "n"));
                        break;
                    case ("E"):
                        obstacles.get(obstaclenum).setRotation(90);
                        obstacles.get(obstaclenum).setImageResource(Helper.resources.get("o" + obstaclenum + "e"));
                        break;
                    case ("S"):
                        obstacles.get(obstaclenum).setRotation(180);
                        obstacles.get(obstaclenum).setImageResource(Helper.resources.get("o" + obstaclenum + "s"));
                        break;
                    case ("W"):
                        obstacles.get(obstaclenum).setRotation(270);
                        obstacles.get(obstaclenum).setImageResource(Helper.resources.get("o" + obstaclenum + "w"));
                        break;
                    default:
                        break;
                }
            }
        }
    }


    private void sendObstaclesData() {
        Gson gson = new GsonBuilder().create();
        JsonObject message = new JsonObject();
        message.addProperty("command", "INITIALIZE");

        JsonObject data = new JsonObject();
        message.add("data", data);

        // Robot data placeholder
        JsonObject robotData = new JsonObject();
        robotData.addProperty("direction", "NORTH");
        JsonObject robotSouthWest = new JsonObject();
        robotSouthWest.addProperty("x", 0);
        robotSouthWest.addProperty("y", 0);
        robotData.add("south_west", robotSouthWest);
        JsonObject robotNorthEast = new JsonObject();
        robotNorthEast.addProperty("x", 25);
        robotNorthEast.addProperty("y", 25);
        robotData.add("north_east", robotNorthEast);
        data.add("robot_data", robotData);

        data.addProperty("world_size", 200);

        JsonArray obstacleData = new JsonArray();
        data.add("obstacle_data", obstacleData);

        for (int i = 1; i <= 8; i++) {
            ImageView currentObstacle = getObstacle(i); // Implement getObstacle(int id) to return the correct ImageView
            String obstacleString = getObstacleString(currentObstacle);
            if (!obstacleString.isEmpty()) {
                String[] parts = obstacleString.split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                String direction = parts[2];

                JsonObject obstacleObject = new JsonObject(); // Renamed to obstacleObject
                obstacleObject.addProperty("image_id", i);
                obstacleObject.addProperty("direction", getFullDirection(direction)); // Implement getFullDirection(String dir) to convert 'S' to 'SOUTH', etc.

                JsonObject southWest = new JsonObject();
                southWest.addProperty("x", (x * 10) - 10 + 10);
                southWest.addProperty("y", (y * 10) - 10 +10);
                obstacleObject.add("south_west", southWest);

                JsonObject northEast = new JsonObject();
                northEast.addProperty("x", x * 10 + 10);
                northEast.addProperty("y", y * 10 + 10);
                obstacleObject.add("north_east", northEast);

                obstacleData.add(obstacleObject);
            }
        }

        String json = gson.toJson(message);
        Log.d(TAG, json);

        if (BluetoothService.BluetoothConnectionStatus) {
            byte[] bytes = json.getBytes(Charset.defaultCharset());
            BluetoothService.write(bytes);
            Toast.makeText(Arena.this, "Obstacles sent", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to convert direction (e.g., 'S' to 'SOUTH')
    private String getFullDirection(String dir) {
        switch (dir) {
            case "S":
                return "SOUTH";
            case "W":
                return "WEST";
            case "N":
                return "NORTH";
            case "E":
                return "EAST";
            default:
                return "";
        }
    }

    // Helper method to get the correct ImageView based on id
    private ImageView getObstacle(int id) {
        // Implement logic to return the correct ImageView based on id
        switch (id) {
            case 1:
                return obstacle1;
            case 2:
                return obstacle2;
            case 3:
                return obstacle3;
            case 4:
                return obstacle4;
            case 5:
                return obstacle5;
            case 6:
                return obstacle6;
            case 7:
                return obstacle7;
            case 8:
                return obstacle8;
            default:
                return null;
        }
    }

    private void sendObstacles() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("ALG:\n" + "1: ")
                .append(getObstacleString(obstacle1) + "\n2:")
                .append(getObstacleString(obstacle2) + "\n3:")
                .append(getObstacleString(obstacle3) + "\n4:")
                .append(getObstacleString(obstacle4) + "\n5:")
                .append(getObstacleString(obstacle5) + "\n6:")
                .append(getObstacleString(obstacle6) + "\n7:")
                .append(getObstacleString(obstacle7) + "\n8:")
                .append(getObstacleString(obstacle8));
        Log.d(TAG, stringBuilder.toString());

        /*if (BluetoothService.BluetoothConnectionStatus) {
            byte[] bytes = stringBuilder.toString().getBytes(Charset.defaultCharset());
            BluetoothService.write(bytes);
            Toast.makeText(Arena.this, "Obstacles sent", Toast.LENGTH_SHORT).show();
        }*/
    }

    /*
     * Returns 2-D array of obstacles in [x, y, direction] format
     */
    private String[][] savedObstacles() {
        String[][] savedPreset = { getObstacleLocation(obstacle1).split(","), getObstacleLocation(obstacle2).split(","),
                getObstacleLocation(obstacle3).split(","),
                getObstacleLocation(obstacle4).split(","), getObstacleLocation(obstacle5).split(","),
                getObstacleLocation(obstacle6).split(","),
                getObstacleLocation(obstacle7).split(","), getObstacleLocation(obstacle8).split(",") };
        // {1,2,N,2,3,E}
        Log.d(TAG, "Saved obstacle data: " + savedPreset);
        return savedPreset;
    }

    /*
     * Get obstacle location in <x, y, direction> format
     */
    private String getObstacleLocation(ImageView obstacle) {
        return (int) obstacle.getX() + "," + (int) obstacle.getY() + "," + getImageOrientation(obstacle);
    }

    /*
     * Get obstacle location in <x, y, direction> format
     * NOTE: 19 because grid is only 20 x 20
     */
    private String getObstacleString(ImageView obstacle) {
        int x = (int) obstacle.getX() / SNAP_GRID_INTERVAL;
        int y = (int) obstacle.getY() / SNAP_GRID_INTERVAL;
        // (0,0) starts from top left hence invert y
        int new_y = 20 - y - 1;
        Log.d(TAG, "Obstacle at " + x + "," + new_y);

        if (x > 19 || new_y > 19) {
            return "";
        } else {
            return x + "," + new_y
                    + ","
                    + getImageOrientation(obstacle);
        }
    }

    private String getImageOrientation(ImageView obstacle) {
        switch (((int) ((obstacle.getRotation() / 90) % 4 + 4) % 4)) {
            case 0:
                return "N";
            case 1:
                return "E";
            case 2:
                return "S";
            case 3:
                return "W";
            default:
                return "X";
        }
    }

    private void updateStatusWindow(String msg) {
        statusWindow.setText(msg);
        Log.d(TAG, "Status window: " + msg);
    }

    private void updateRobotPosition(int x, int y, int direction) {
        car.setX(x * SNAP_GRID_INTERVAL - SNAP_GRID_INTERVAL);
        car.setY(y * SNAP_GRID_INTERVAL - SNAP_GRID_INTERVAL);
        switch (direction) {
            case 7: // North-west
                car.setRotation(315);
                break;
            case 0: // North
                car.setRotation(0);
                break;
            case 1: // North-east
                car.setRotation(45);
                break;
            case 2: // East
                car.setRotation(90);
                break;
            case 3: // South-east
                car.setRotation(135);
                break;
            case 4: // South
                car.setRotation(180);
                break;
            case 5: // South-west
                car.setRotation(225);
                break;
            case 6: // West
                car.setRotation(270);
                break;
            default:
                // Shouldn't reach this case
                break;
        }

        updateXYDirText();
    }

    private void updateXYDirText() {
        int x = (int) (car.getX() + SNAP_GRID_INTERVAL) / SNAP_GRID_INTERVAL;
        int y = (int) (car.getY() + SNAP_GRID_INTERVAL) / SNAP_GRID_INTERVAL;
        // (0,0) starts from top left hence invert y
        int new_y = 20 - y - 1;
        car_x.setText(String.valueOf(x));
        car_y.setText(String.valueOf(new_y));

        int direction = (int) car.getRotation();

        if (direction == 315)
            car_dir.setText("North-West");
        else if (direction == 0)
            car_dir.setText("North");
        else if (direction == 45)
            car_dir.setText("North-East");
        else if (direction == 90)
            car_dir.setText("East");
        else if (direction == 135)
            car_dir.setText("South-East");
        else if (direction == 180)
            car_dir.setText("South");
        else if (direction == 225)
            car_dir.setText("South-West");
        else if (direction == 270)
            car_dir.setText("West");
        else
            car_dir.setText("None");
    }

    // Broadcast Receiver for incoming messages
    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            String command;
            Log.d(TAG, "Received message: " + message);

            try {
                command = message.substring(0, message.indexOf(','));
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(Arena.this, "Invalid message format!",
                        Toast.LENGTH_SHORT).show();
                return;
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
                return;
            }

            try {
                switch (command) {
                    // move robot
                    case Helper.ROBOT:
                        int startingIndex = message.indexOf("<");
                        int endingIndex = message.indexOf(">");
                        String x = message.substring(startingIndex + 1, endingIndex);

                        startingIndex = message.indexOf("<", endingIndex + 1);
                        endingIndex = message.indexOf(">", endingIndex + 1);
                        String y = message.substring(startingIndex + 1, endingIndex);
                        int adjusted_y = 19 - Integer.parseInt(y);

                        startingIndex = message.indexOf("<", endingIndex + 1);
                        endingIndex = message.indexOf(">", endingIndex + 1);
                        String direction = message.substring(startingIndex + 1, endingIndex);

                        Log.d("ROBOT", "(x: " + x + ") (y: " + adjusted_y + ") (direction: " + direction + ")");

                        int direction_int = 0;
                        switch (direction) {
                            case "N":
                                direction_int = 0;
                                break;
                            case "NE":
                                direction_int = 1;
                                break;
                            case "E":
                                direction_int = 2;
                                break;
                            case "SE":
                                direction_int = 3;
                                break;
                            case "S":
                                direction_int = 4;
                                break;
                            case "SW":
                                direction_int = 5;
                                break;
                            case "W":
                                direction_int = 6;
                                break;
                            case "NW":
                                direction_int = 7;
                                break;
                            default:
                                break;
                        }

                        updateRobotPosition(Integer.parseInt(x), Integer.parseInt(y), direction_int);
                        //updateRobotPosition(Integer.parseInt(x), adjusted_y, direction_int);
                        break;

                    // update obstacle ID (format - TARGET,obstacle_number,target_ID)
                    case Helper.TARGET:
                        int obstacleNumber = Character.getNumericValue(message.charAt(7));
                        String solution = message.substring(9);
                        Log.d(TAG, "Solution value: " + solution);
                        if (Integer.parseInt(solution) == 0) {
                            Toast.makeText(Arena.this, "Image not recognized, trying again", Toast.LENGTH_SHORT).show();
                        } else {
                            // RMB TO PLUS 1 !!
                            setObstacleImageID(obstacleNumber + 1, solution);
                            Toast.makeText(Arena.this,
                                    "Obstacle " + obstacleNumber + " changed to Target ID: " + solution,
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;

                    // update TASK2 commands
                    case Helper.TASK2:
                        if (message.equals("TASK2,DONE")) {
                            MediaPlayer mediaPlayer = MediaPlayer.create(Arena.this, R.raw.potc);
                            if (mediaPlayer != null) {
                                mediaPlayer.start();
                            } else {
                                Log.e(TAG, "Failed to initialize media player");
                            }
                        }
                        break;

                    // update status window
                    case Helper.STATUS:
                        String msg;
                        if (message.contains("\n")) {
                            msg = message.substring(message.indexOf(',') + 1, message.indexOf('\n'));
                        } else {
                            msg = message.substring(message.indexOf(',') + 1);
                        }
                        if (message.contains("STOPPED")) {
                            Chronometer IRTimer = (Chronometer) findViewById(R.id.IRTimer);
                            IRTimer.stop();
                            updateStatusWindow("IR Completed");
                        } else {
                            updateStatusWindow(msg);
                        }
                        break;

                    // plot obstacles
                    case Helper.PLOT:
                        String receivedmsg = message.substring(message.indexOf(",") + 1); // string after PLOT,
                        String[] obstaclesPreset = receivedmsg.split(";"); // create 2d array for obstacle data\
                        Log.d(TAG, "Obstacle data is " + Arrays.toString(obstaclesPreset));
                        setObstacles(obstaclesPreset);
                        break;

                    // commands from RPI
                    case Helper.COMMAND:
                        String moveCommand = message.substring(message.indexOf(',') + 1); //
                        // substring after COMMANDS
                        Log.d(TAG, "Command received: " + moveCommand);

                        String prefix = moveCommand.substring(0, 2);
                        String distance = moveCommand.substring(2);
                        int convertedDistance;
                        Log.d(TAG, prefix + ";" + distance);

                        switch (prefix) {
                            // forward
                            case "SF":
                                convertedDistance = Integer.parseInt(distance) / 10;
                                Log.d(TAG, prefix + ";" + convertedDistance);
                                forwardButton(convertedDistance);
                                break;
                            // reverseA
                            case "SB":
                                convertedDistance = Integer.parseInt(distance) / 10;
                                reverseButton(convertedDistance);
                                break;
                            // right forward
                            case "RF":
                                rightMidButtonCommand();
                                break;
                            // right backward
                            case "RB":
                                rightMidReverseButtonCommand();
                                break;
                            // left forward
                            case "LF":
                                leftMidButtonCommand();
                                break;
                            // left backward
                            case "LB":
                                leftMidReverseButtonCommand();
                                break;
                            // slide right forward
                            case "JF":
                                rightSlideCommand();
                                break;
                            // slide right backward
                            case "JB":
                                rightSlideReverseCommand();
                                break;
                            // slide left forward
                            case "KF":
                                leftSlideCommand();
                                break;
                            // slide left backward
                            case "KB":
                                leftSlideReverseCommand();
                                break;
                        }
                    default:
                        // for out of "ROBOT/TARGET/STATUS/COMMAND" cases
                        break;
                }
            } catch (Exception e) {
                // TODO: handle exception
                Log.d(TAG, "Exception: " + e.getMessage());
                return;
            }
        }
    };
}
