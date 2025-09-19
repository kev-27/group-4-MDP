package com.example.mdp_group_14;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import org.json.JSONObject;
import org.json.JSONArray;

public class Home extends Fragment {

    final Handler handler = new Handler();
    // Declaration Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;
    public static Handler timerHandler = new Handler();

    private static GridMap gridMap;
    static TextView xAxisTextView, yAxisTextView, directionAxisTextView;
    static TextView robotStatusTextView, bluetoothStatus, bluetoothDevice;
    static ImageButton upBtn, downBtn, leftBtn, rightBtn,bleftBtn,brightBtn;

    BluetoothDevice mBTDevice;
    private static UUID myUUID;
    ProgressDialog myDialog;
    Bitmap bm, mapscalable;
    String obstacleID;

    private static final String TAG = "Main Activity";
    public static boolean stopTimerFlag = false;
    public static boolean stopWk9TimerFlag = false;

    public static boolean trackRobot = true;

    private int g_coordX;
    private int g_coordY;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate
        View root = inflater.inflate(R.layout.home, container, false);

        // get shared preferences
        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences",
                Context.MODE_PRIVATE);



        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        sectionsPagerAdapter.addFragment(new MappingFragment(),"MAP CONFIG");
        sectionsPagerAdapter.addFragment(new BluetoothCommunications(),"CHAT");
        sectionsPagerAdapter.addFragment(new ControlFragment(),"CHALLENGE");

        ViewPager viewPager = root.findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);


        TabLayout tabs = root.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);



        LocalBroadcastManager
                .getInstance(getContext())
                .registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        // Set up sharedPreferences
        Home.context = getContext();
        sharedPreferences();
        editor.putString("message", "");
        editor.putString("direction","None");
        editor.putString("connStatus", "Disconnected");
        editor.commit();

        // Toolbar
        ImageButton bluetoothButton = root.findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popup = new Intent(getContext(), BluetoothSetUp.class);
                startActivity(popup);
            }
        });

        // Bluetooth Status
        bluetoothStatus = root.findViewById(R.id.bluetoothStatus);
        bluetoothDevice = root.findViewById(R.id.bluetoothConnectedDevice);

        // Map
        gridMap = new GridMap(getContext());
        gridMap = root.findViewById(R.id.mapView);
        xAxisTextView = root.findViewById(R.id.xAxisTextView);
        yAxisTextView = root.findViewById(R.id.yAxisTextView);
        directionAxisTextView = root.findViewById(R.id.directionAxisTextView);

        // initialize ITEM_LIST and imageBearings strings
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                gridMap.ITEM_LIST.get(i)[j] = "";
                GridMap.imageBearings.get(i)[j] = "";
            }
        }

        // Controller
        upBtn = root.findViewById(R.id.upBtn);
        downBtn = root.findViewById(R.id.downBtn);
        leftBtn = root.findViewById(R.id.leftBtn);
        rightBtn = root.findViewById(R.id.rightBtn);
        brightBtn = root.findViewById(R.id.brightBtn);
        bleftBtn = root.findViewById(R.id.bleftBtn);

        // Robot Status
        robotStatusTextView = root.findViewById(R.id.robotStatus);

        myDialog = new ProgressDialog(getContext());
        myDialog.setMessage("Waiting for other device to reconnect...");
        myDialog.setCancelable(false);
        myDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        PathTranslator pathTranslator = new PathTranslator(gridMap);
//        pathTranslator.translatePath("MOVE,FORWARD,30");
        return root;
    }

    public static GridMap getGridMap() {
        return gridMap;
    }
    public static TextView getRobotStatusTextView() {  return robotStatusTextView; }

    public static ImageButton getUpBtn() { return upBtn; }
    public static ImageButton getDownBtn() { return downBtn; }
    public static ImageButton getLeftBtn() { return leftBtn; }
    public static ImageButton getRightBtn() { return rightBtn; }

    public static ImageButton getbLeftBtn() { return bleftBtn; }
    public static ImageButton getbRightBtn() { return brightBtn; }


    public static TextView getBluetoothStatus() { return bluetoothStatus; }
    public static TextView getConnectedDevice() { return bluetoothDevice; }
    // For week 8 only
    public static boolean getTrackRobot() { return trackRobot; }
    public static void toggleTrackRobot() { trackRobot = !trackRobot; }

    public static void sharedPreferences() {
        sharedPreferences = Home.getSharedPreferences(Home.context);
        editor = sharedPreferences.edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    // Send Coordinates to alg
    public static void printCoords(String message){
        showLog("Displaying Coords untranslated and translated");
        showLog(message);
        String[] strArr = message.split("_",2);

        // Translated ver is sent
        if (BluetoothConnectionService.BluetoothConnectionStatus == true){
            String toSend = strArr[1].endsWith("\n") ? strArr[1] : strArr[1] + "\n";
            byte[] bytes = toSend.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }

        // Display both untranslated and translated coordinates on CHAT (for debugging)
        refreshMessageReceivedNS("Untranslated Coordinates: " + strArr[0] + "\n");
        refreshMessageReceivedNS("Translated Coordinates: "+strArr[1]);
        showLog("Exiting printCoords");
    }

    // Send message to bluetooth (not shown on chat box)
    public static void printMessage(String message) {
        showLog("Entering printMessage");
        editor = sharedPreferences.edit();

        if (BluetoothConnectionService.BluetoothConnectionStatus) {
            // Check if this is an obstacle message and convert to JSON
            if (message.startsWith("OBSTACLE,")) {
                showLog("DEBUG: DETECTED OBSTACLE MESSAGE: '" + message + "'");
                showLog("DEBUG: Message length: " + message.length());
                showLog("DEBUG: Message bytes: " + java.util.Arrays.toString(message.getBytes()));
                
                String jsonMessage = convertObstacleToJSON(message);
                if (jsonMessage != null) {
                    showLog("SUCCESS: Converted obstacle to JSON: " + jsonMessage);
                    showLog("SENDING: Calling BluetoothConnectionService.writeJson()...");
                    BluetoothConnectionService.writeJson(jsonMessage);
                    showLog("DEBUG: writeJson() completed");
                } else {
                    showLog("ERROR: Failed to convert obstacle to JSON, sending as plain text");
                    String toSend = message.endsWith("\n") ? message : message + "\n";
                    byte[] bytes = toSend.getBytes(Charset.defaultCharset());
                    showLog("FALLBACK: Sending plain text: '" + toSend + "'");
                    BluetoothConnectionService.write(bytes);
                }
            }
            // Check if this is a robot message and convert to JSON
            else if (message.startsWith("ROBOT,")) {
                showLog("DEBUG: DETECTED ROBOT MESSAGE: '" + message + "'");
                showLog("DEBUG: Message length: " + message.length());
                showLog("DEBUG: Message bytes: " + java.util.Arrays.toString(message.getBytes()));
                
                String jsonMessage = convertRobotToJSON(message);
                if (jsonMessage != null) {
                    showLog("SUCCESS: Converted robot to JSON: " + jsonMessage);
                    showLog("SENDING: Calling BluetoothConnectionService.writeJson()...");
                    BluetoothConnectionService.writeJson(jsonMessage);
                    showLog("DEBUG: writeJson() completed");
                } else {
                    showLog("ERROR: Failed to convert robot to JSON, sending as plain text");
                    String toSend = message.endsWith("\n") ? message : message + "\n";
                    byte[] bytes = toSend.getBytes(Charset.defaultCharset());
                    showLog("FALLBACK: Sending plain text: '" + toSend + "'");
                    BluetoothConnectionService.write(bytes);
                }
            }
            // Use the same JSON detection logic as BluetoothCommunications
            else if (isValidJSON(message)) {
                showLog("Sending as JSON: " + message);
                BluetoothConnectionService.writeJson(message);  // This adds \n for JSON
            } else {
                showLog("Sending as plain text: " + message);
                // Ensure newline for message framing
                String toSend = message.endsWith("\n") ? message : message + "\n";
                byte[] bytes = toSend.getBytes(Charset.defaultCharset());
                BluetoothConnectionService.write(bytes);
            }
        }
        showLog(message);
        showLog("Exiting printMessage");
    }

    // Send message to bluetooth (not shown on chat box)
    public static void printMessage(JSONArray message) {
        showLog("Entering printMessage");
        editor = sharedPreferences.edit();
        if (BluetoothConnectionService.BluetoothConnectionStatus) {
            String payload = message.toString() + "\n"; // newline-delimited JSON
            byte[] bytes = payload.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
    }

    // Send JSONObject variant (not shown on chat box)
    public static void printMessage(JSONObject message) {
        showLog("Entering printMessage (JSONObject)");
        editor = sharedPreferences.edit();
        if (BluetoothConnectionService.BluetoothConnectionStatus) {
            String payload = message.toString() + "\n"; // newline-delimited JSON
            byte[] bytes = payload.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
    }


//        if (BluetoothConnectionService.BluetoothConnectionStatus) {
////            JSONObject jsonObj = message.getJSONObject("data");
//        JSONObject js=new JSONObject();
//        try {
//            JSONArray ja=new JSONArray();
//            js.put("key", "floor");
//            ja.put(js);
//            BluetoothConnectionService.write(ja);
//
//        }
//        catch (JSONException e) {
//            showLog("lol!");
//
//
//            //BluetoothConnectionService.write({"key":"test","value":"hello"});
//        }
//        //showLog(message);
//        showLog("Exiting printMessage");
//    }

    // Purely to display a message on the chat box - NOT SENT via BT
    public static void refreshMessageReceivedNS(String message){
        BluetoothCommunications.getMessageReceivedTextView().append(message+ "\n");
    }

    public static void refreshMessageReceivedNS(int message){
        BluetoothCommunications.getMessageReceivedTextView().append(message+ "\n");
    }

    public static void refreshDirection(String direction) {
        gridMap.setRobotDirection(direction);
        int x = gridMap.getCurCoord()[0];
        int y = gridMap.getCurCoord()[1];
        String dir;
        String newDir = gridMap.getRobotDirection();
//        newDir = newDir.toUpperCase();
        directionAxisTextView.setText(sharedPreferences.getString("direction","")); //changes the UI direction display as well
        //printMessage("Direction is set to " + direction); //OLD VER

        dir= (newDir.equals("up"))?"NORTH":(newDir.equals("down"))?"SOUTH":(newDir.equals("left"))?"WEST":"EAST";
        if ((x - 1)>=0 && (y - 1)>=0)
        {
//          BluetoothCommunications.getMessageReceivedTextView().append("ROBOT" + "," + (col - 2)*5 + "," + (row - 1)*5 + "," + dir.toUpperCase());
            Home.printMessage("ROBOT" + "," + (x-1) + "," + (y-1) + "," + dir.toUpperCase());
        }
        else{
            showLog("out of grid");
        }
//        printMessage("ROBOT,"+ x + "," + y + "," + dir);
//        BluetoothCommunications.getMessageReceivedTextView().append("ROBOT,"+ (x-1) +"," + (y-1) + "," + dir+"\n"); //for troubleshooting

    }

    public static void refreshLabel() {
        xAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[0]-1));
        yAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[1]-1));
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
    }

    // Helper method to convert obstacle string to JSON format
    private static String convertObstacleToJSON(String obstacleMessage) {
        try {
            showLog("DEBUG: Starting JSON conversion for: '" + obstacleMessage + "'");
            
            // Remove trailing newline if present
            String cleanMessage = obstacleMessage.trim();
            showLog("DEBUG: After trim: '" + cleanMessage + "'");
            
            // Parse: OBSTACLE,<id>,<x>,<y>,<direction>
            String[] parts = cleanMessage.split(",");
            showLog("DEBUG: Split into " + parts.length + " parts: " + java.util.Arrays.toString(parts));
            
            if (parts.length != 5) {
                showLog("ERROR: Expected 5 parts, got " + parts.length + ": " + java.util.Arrays.toString(parts));
                return null;
            }
            
            if (!parts[0].equals("OBSTACLE")) {
                showLog("ERROR: First part should be 'OBSTACLE', got: '" + parts[0] + "'");
                return null;
            }
            
            int obstacleId = Integer.parseInt(parts[1]);
            int x = Integer.parseInt(parts[2]);
            int y = Integer.parseInt(parts[3]);
            String direction = parts[4].toUpperCase();
            
            showLog("DEBUG: Parsed values - ID:" + obstacleId + " X:" + x + " Y:" + y + " DIR:" + direction);
            
            // Convert string direction to numeric direction
            int directionInt = convertDirectionToInt(direction);
            
            // Create JSON structure
            JSONObject obstacle = new JSONObject();
            obstacle.put("x", x);
            obstacle.put("y", y);
            obstacle.put("id", obstacleId);
            obstacle.put("d", directionInt);
            
            JSONArray obstacles = new JSONArray();
            obstacles.put(obstacle);
            
            JSONObject value = new JSONObject();
            value.put("obstacles", obstacles);
            value.put("mode", "0");
            
            JSONObject message = new JSONObject();
            message.put("cat", "obstacles");
            message.put("value", value);
            
            String result = message.toString();
            showLog("DEBUG: Final JSON: " + result);
            return result;
            
        } catch (NumberFormatException e) {
            showLog("ERROR: Number parsing error: " + e.getMessage());
            showLog("ERROR: Raw message was: '" + obstacleMessage + "'");
            return null;
        } catch (Exception e) {
            showLog("ERROR: General error converting obstacle to JSON: " + e.getMessage());
            showLog("ERROR: Error type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return null;
        }
    }

    // Convert string direction to numeric direction for Algorithm API
    private static int convertDirectionToInt(String direction) {
        switch (direction.toUpperCase()) {
            case "NORTH":
            case "UP":
                return 0;
            case "EAST":
            case "RIGHT":
                return 1;
            case "SOUTH":
            case "DOWN":
                return 2;
            case "WEST":
            case "LEFT":
                return 3;
            default:
                showLog("WARNING: Unknown direction '" + direction + "', defaulting to NORTH (0)");
                return 0;
        }
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    /**
     * Convert robot coordinate message to JSON format
     * Input: "ROBOT,25,30,NORTH"  
     * Output: JSON string for robot position
     */
    private static String convertRobotToJSON(String robotMessage) {
        try {
            showLog("DEBUG: Starting robot JSON conversion for: '" + robotMessage + "'");
            
            // Remove trailing newline if present
            String cleanMessage = robotMessage.trim();
            showLog("DEBUG: After trim: '" + cleanMessage + "'");
            
            // Parse: ROBOT,<x>,<y>,<direction>
            String[] parts = cleanMessage.split(",");
            showLog("DEBUG: Split into " + parts.length + " parts: " + java.util.Arrays.toString(parts));
            
            if (parts.length != 4) {
                showLog("ERROR: Expected 4 parts, got " + parts.length + ": " + java.util.Arrays.toString(parts));
                return null;
            }
            
            if (!parts[0].equals("ROBOT")) {
                showLog("ERROR: First part should be 'ROBOT', got: '" + parts[0] + "'");
                return null;
            }
            
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            String direction = parts[3].toUpperCase();
            
            showLog("DEBUG: Parsed robot values - X:" + x + " Y:" + y + " DIR:" + direction);
            
            // Create JSON structure
            JSONObject robotData = new JSONObject();
            robotData.put("x", x);
            robotData.put("y", y);
            robotData.put("d", direction);
            
            JSONObject robotJson = new JSONObject();
            robotJson.put("cat", "robot");
            robotJson.put("value", robotData);
            
            String result = robotJson.toString();
            showLog("DEBUG: Created robot JSON: " + result);
            return result;
            
        } catch (NumberFormatException e) {
            showLog("ERROR: Number parsing error in robot coordinates: " + e.getMessage());
            return null;
        } catch (Exception e) {
            showLog("ERROR: Unexpected error during robot JSON conversion: " + e.getMessage());
            return null;
        }
    }

    // TEST METHOD - Call this to verify obstacle JSON conversion
    public static void testObstacleJSONConversion() {
        showLog("TEST: TESTING OBSTACLE JSON CONVERSION");
        
        // Test the conversion without sending via Bluetooth
        String testObstacle = "OBSTACLE,1,40,100,NORTH";
        String convertedJSON = convertObstacleToJSON(testObstacle);
        
        showLog("TEST: Original: " + testObstacle);
        showLog("TEST: Converted: " + convertedJSON);
        
        if (convertedJSON != null) {
            showLog("SUCCESS: Obstacle JSON conversion working!");
        } else {
            showLog("ERROR: Obstacle JSON conversion failed!");
        }
    }

    // MANUAL TEST - Send a test obstacle message through the full pipeline
    public static void sendTestObstacle() {
        showLog("TEST: SENDING TEST OBSTACLE");
        printMessage("OBSTACLE,99,123,456,EAST");
        showLog("TEST: Test obstacle sent");
    }

    // TEST METHOD - Test JSON location message handling
    public static void testJSONLocationHandling() {
        showLog("TEST: TESTING JSON LOCATION HANDLING");
        
        // Test different location messages
        String[] testMessages = {
            "{\"cat\": \"location\", \"value\": {\"x\": 1, \"y\": 1, \"d\": 0}}",  // North
            "{\"cat\": \"location\", \"value\": {\"x\": 5, \"y\": 3, \"d\": 1}}",  // East
            "{\"cat\": \"location\", \"value\": {\"x\": 10, \"y\": 15, \"d\": 2}}", // South
            "{\"cat\": \"location\", \"value\": {\"x\": 0, \"y\": 0, \"d\": 3}}"   // West
        };
        
        for (String testMessage : testMessages) {
            showLog("TEST: Testing message: " + testMessage);
            
            // Simulate the message processing
            try {
                JSONObject jsonMessage = new JSONObject(testMessage.trim());
                if (jsonMessage.has("cat") && jsonMessage.getString("cat").equals("location")) {
                    JSONObject value = jsonMessage.getJSONObject("value");
                    int x = value.getInt("x");
                    int y = value.getInt("y");
                    int d = value.getInt("d");
                    
                    String direction = "";
                    switch (d) {
                        case 0: direction = "up"; break;
                        case 1: direction = "right"; break;
                        case 2: direction = "down"; break;
                        case 3: direction = "left"; break;
                        default: direction = "up"; break;
                    }
                    
                    int gridX = x + 2;
                    int gridY = 19 - y;
                    
                    showLog("TEST: Parsed - X:" + x + " Y:" + y + " D:" + d + " -> GridX:" + gridX + " GridY:" + gridY + " Direction:" + direction);
                }
            } catch (Exception e) {
                showLog("TEST ERROR: " + e.getMessage());
            }
        }
        
        showLog("TEST: JSON location handling test completed");
    }

    // MANUAL TEST - Send a test JSON location message through Bluetooth
    public static void sendTestJSONLocation() {
        showLog("TEST: SENDING TEST JSON LOCATION MESSAGE");
        String testLocationMessage = "{\"cat\": \"location\", \"value\": {\"x\": 1, \"y\": 1, \"d\": 0}}";
        printMessage(testLocationMessage);
        showLog("TEST: Test JSON location message sent: " + testLocationMessage);
    }

    private static boolean isValidJSON(String text) {
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

    private final BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences();

            if(status.equals("connected")){
                try {
                    myDialog.dismiss();
                } catch(NullPointerException e){
                    e.printStackTrace();
                }

                Log.d(TAG, "mBroadcastReceiver5: Device now connected to "+mDevice.getName());
                updateStatus("Device now connected to "
                        + mDevice.getName());
                editor.putString("connStatus", "Connected to " + mDevice.getName());
            }
            else if(status.equals("disconnected")){
                Log.d(TAG, "mBroadcastReceiver5: Disconnected from "+mDevice.getName());
                updateStatus("Disconnected from "
                        + mDevice.getName());

                editor.putString("connStatus", "Disconnected");

                myDialog.show();
            }
            editor.commit();
        }
    };

    // Message handler (Receiving)
    // RPi relays the EXACT SAME stm commands sent by algo back to android: Starts with "Algo|"
    // RPi sends the image id as "TARGET~<obID>~<ImValue>"
    // Other specific strings are to clear checklist
    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PathTranslator pathTranslator = new PathTranslator(gridMap);    // For real-time updating on displayed gridmap
            String message = intent.getStringExtra("receivedMessage");
            showLog("receivedMessage: message --- " + message);

            String[] cmdd = message.split(",");

//            if (message.contains(" "))
//            {
//                message= Arrays.toString(message.split(" "));
//            }
//            showLog("cmd1 --- " + cmdd[1]);
//            showLog("cmd2 --- " + cmdd[2]);


            int[] global_store = gridMap.getCurCoord();
            g_coordX = global_store[0];
            g_coordY = global_store[1];
            ArrayList<String> mapCoord = new ArrayList<>();

            //STATUS:<input>
            if (message.contains("STATUS")) {
                robotStatusTextView.setText(message.split(":")[1]);
            }
            // Handle JSON location messages: {"cat": "location", "value": {"x": 1, "y": 1, "d": 0}}
            else if (message.trim().startsWith("{") && message.contains("\"cat\":\"location\"")) {
                try {
                    showLog("DEBUG: Processing JSON location message: " + message);
                    JSONObject jsonMessage = new JSONObject(message.trim());
                    
                    if (jsonMessage.has("cat") && jsonMessage.getString("cat").equals("location")) {
                        JSONObject value = jsonMessage.getJSONObject("value");
                        int x = value.getInt("x");
                        int y = value.getInt("y");
                        int d = value.getInt("d");
                        
                        // Convert direction from numeric to string
                        String direction = "";
                        switch (d) {
                            case 0:
                                direction = "up";    // North
                                break;
                            case 1:
                                direction = "right"; // East
                                break;
                            case 2:
                                direction = "down";  // South
                                break;
                            case 3:
                                direction = "left";  // West
                                break;
                            default:
                                direction = "up";
                                break;
                        }
                        
                        showLog("DEBUG: Parsed location - X:" + x + " Y:" + y + " Direction:" + direction + " (d=" + d + ")");
                        
                        // Convert coordinates to grid system (add offset and flip Y if needed)
                        int gridX = x + 2;  // Add offset for grid system
                        int gridY = 19 - y; // Convert Y coordinate (flip and offset)
                        
                        showLog("DEBUG: Converted to grid coordinates - GridX:" + gridX + " GridY:" + gridY);
                        
                        // Update robot position
                        gridMap.setCurCoord(gridX, gridY, direction);
                        showLog("SUCCESS: Updated robot position from JSON location message");
                    }
                } catch (Exception e) {
                    showLog("ERROR: Failed to parse JSON location message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            //ROBOT|5,4,EAST (Early version of updating robot position via comms)
            else if(message.contains("ROBOT")) {
                String[] cmd = message.split("\\|");
                String[] sentCoords = cmd[1].split(",");
                String[] sentDirection = sentCoords[2].split("\\.");
//                BluetoothCommunications.getMessageReceivedTextView().append("\n");
                String direction = "";
                String abc = String.join("", sentDirection);
                if (abc.contains("EAST")) {
                    direction = "right";
                }
                else if (abc.contains("NORTH")) {
                    direction = "up";
                }
                else if (abc.contains("WEST")) {
                    direction = "left";
                }
                else if (abc.contains("SOUTH")) {
                    direction = "down";
                }
                else{
                    direction = "";
                }
                gridMap.setCurCoord(Integer.valueOf(sentCoords[1]) + 2, 19 - Integer.valueOf(sentCoords[0]), direction);
            }
            //image format from RPI is "TARGET~<obID>~<ImValue>" eg TARGET~3~7
            else if(message.contains("TARGET")) {
                try {
                    String[] cmd = message.split(",");
                    String temp2="-1";
                    BluetoothCommunications.getMessageReceivedTextView().append("Obstacle no: " + cmd[1]+ "TARGET ID: " + cmd[2] + "\n");

//                    if (cmd[2].contains("STOP"))
//                    {
//                        String temp=cmd[2];
//                        String[] temp1=temp.split(" ");
//                        temp2=temp1[0];
//
//                    }

                    gridMap.updateIDFromRpi(String.valueOf(Integer.valueOf(cmd[1])-1), cmd[2]);
                    obstacleID = String.valueOf(Integer.valueOf(cmd[1]) - 2);


//                    int ob= Integer.parseInt(obstacleID);

                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if(message.contains("ARROW")){
                String[] cmd = message.split(",");
//                BluetoothCommunications.getMessageReceivedTextView().append("Obstacle no: " + cmd[1]+ "TARGET ID: " + cmd[2] + "\n");

                Home.refreshMessageReceivedNS("TASK2"+"\n");
                Home.refreshMessageReceivedNS("obstacle id: "+cmd[1]+", ARROW: "+cmd[2]);


//                updateStatus(cmd[0]+" "+ cmd[1]+" "+cmd[2]);
            }
            // OLD VER: Expects a syntax of e.g. Algo|f010. Commented out and implemented new version below
/*            if(message.contains("Algo")) {
                // translate the message after Algo|
                if(trackRobot)
                    pathTranslator.translatePath(message.split("\\|")[1]);
//                pathTranslator.altTranslation(message.split("\\|")[1]);   // last min addition - untested
            }*/

            //NEW VER: Expects a syntax of eg. MOVE,<DISTANCE IN CM>,<DIRECTION>.
            //NEW VER: Expects a syntax of eg. TURN,<DIRECTION>.

            //CASE 1 & 2: MoveInstruction or TurnInstruction sent
            else if(message.contains("MOVE") || message.contains("TURN")){
                updateStatus("translation");
                pathTranslator.translatePath(message); //splitting and translation will be done in PathTranslator
            }
            else if(message.contains("STOP"))
            {
                Home.refreshMessageReceivedNS("STOP received");
//                showLog("received Stop");
                Home.stopTimerFlag = true;
                Home.stopWk9TimerFlag=true;
                timerHandler.removeCallbacks(ControlFragment.timerRunnableExplore);
                timerHandler.removeCallbacks(ControlFragment.timerRunnableFastest);
            }
            else{
                BluetoothCommunications.getMessageReceivedTextView().append("unknown message received");
                showLog("unknown message received");
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    mBTDevice = data.getExtras().getParcelable("mBTDevice");
                    myUUID = (UUID) data.getSerializableExtra("myUUID");
                }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try{
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        try{
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        try{
            IntentFilter filter2 = new IntentFilter("ConnectionStatus");
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver5, filter2);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(TAG, "onSaveInstanceState");
        showLog("Exiting onSaveInstanceState");
    }
    private void updateStatus(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP,0, 0);
        toast.show();
    }
}