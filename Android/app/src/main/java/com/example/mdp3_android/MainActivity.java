package com.example.mdp3_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp3_android.adapters.SectionsPagerAdapter;
import com.example.mdp3_android.map.GridMap;
import com.example.mdp3_android.pagerfragment.ControlsTabFragment;
import com.example.mdp3_android.pagerfragment.MapTabFragment;
import com.google.android.material.tabs.TabLayout;
import com.example.mdp3_android.pagerfragment.CommsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.UUID;

//import com.example.mdp3_android.adapters.ViewPagerAdapter;
import com.example.mdp3_android.bluetooth.BluetoothActivity;
import com.example.mdp3_android.bluetooth.BluetoothService;
import com.google.android.material.tabs.TabLayout;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    // Declaration Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;
    public static String connectedDevice;

    private static GridMap gridMap;
    static TextView xAxisTextView, yAxisTextView, directionAxisTextView;
    static TextView robotStatusTextView;
    static Button f1, f2;
    Button reconfigure;
    ReconfigureFragment reconfigureFragment = new ReconfigureFragment();


    BluetoothDevice mBTDevice;
    private static UUID myUUID;
    ProgressDialog myDialog;

    private static boolean autoUpdate = false;
    public static boolean manualUpdateRequest = false;

    private static final String TAG = "Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(9999);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        // Set up sharedPreferences
        MainActivity.context = getApplicationContext();
        this.sharedPreferences();
        editor.putString("message", "");
        editor.putString("direction","None");
        editor.putString("connStatus", "Disconnected");
        editor.commit();

        Switch manualAutoToggleBtn = findViewById(R.id.manualAutoToggleBtn);
        manualAutoToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked manualAutoToggleBtn");
                if (manualAutoToggleBtn.getText().equals("MANUAL")) {
                    try {
                        gridMap.setAutoUpdate(true);
                        autoUpdate = true;
                        gridMap.toggleCheckedBtn("None");
                        MapTabFragment.getUpdateButton().setClickable(false);
                        MapTabFragment.getUpdateButton().setTextColor(Color.GRAY);
                        ControlsTabFragment.getCalibrateButton().setClickable(false);
                        ControlsTabFragment.getCalibrateButton().setTextColor(Color.GRAY);
                        manualAutoToggleBtn.setText("AUTO");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.context, "Auto mode", Toast.LENGTH_SHORT).show();
                }
                else if (manualAutoToggleBtn.getText().equals("AUTO")) {
                    try {
                        gridMap.setAutoUpdate(false);
                        autoUpdate = false;
                        gridMap.toggleCheckedBtn("None");
                        MapTabFragment.getUpdateButton().setClickable(true);
                        MapTabFragment.getUpdateButton().setTextColor(Color.WHITE);
                        ControlsTabFragment.getCalibrateButton().setClickable(true);
                        ControlsTabFragment.getCalibrateButton().setTextColor(Color.WHITE);
                        manualAutoToggleBtn.setText("MANUAL");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.context, "Manual mode", Toast.LENGTH_SHORT).show();
                }
                showLog("Exiting manualAutoToggleBtn");
            }
        });

        // Map
        gridMap = new GridMap(this);
        gridMap = findViewById(R.id.mazeView);
        xAxisTextView = findViewById(R.id.xCoordTextView);
        yAxisTextView = findViewById(R.id.yCoordTextView);
        directionAxisTextView = findViewById(R.id.directionTextView);

        // Robot Status
        robotStatusTextView = findViewById(R.id.robotStatusTextView);

        myDialog = new ProgressDialog(MainActivity.this);
        myDialog.setMessage("Waiting for the other device to reconnect ...");
        myDialog.setCancelable(false);
        myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        f1 = (Button) findViewById(R.id.f1Button);
        f2 = (Button) findViewById(R.id.f2Button);
        reconfigure = (Button) findViewById(R.id.configureButton);

        if (sharedPreferences.contains("F1")) {
            f1.setContentDescription(sharedPreferences.getString("F1", ""));
            showLog("set text for f1: " + f1.getContentDescription().toString());
        }
        if (sharedPreferences.contains("F2")) {
            f2.setContentDescription(sharedPreferences.getString("F2", ""));
            showLog("set text for f2: " + f2.getContentDescription().toString());
        }

        f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked f1 Button");
                if (!f1.getContentDescription().toString().equals("empty"))
                    MainActivity.printMessage(f1.getContentDescription().toString());
                showLog("f1 Button value: " + f1.getContentDescription().toString());
                showLog("Exiting f1 Button");
            }
        });

        f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked f2 Button");
                if (!f2.getContentDescription().toString().equals("empty"))
                    MainActivity.printMessage(f2.getContentDescription().toString());
                showLog("f2 Button value: " + f2.getContentDescription().toString());
                showLog("Exiting f2 Button");
            }
        });

        reconfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked reconfigureBtn");
                reconfigureFragment.show(getFragmentManager(), "Reconfigure Fragment");
                showLog("Exiting reconfigureBtn");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.btnBluetooth) {
            startActivity(new Intent(this, BluetoothActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static TextView getRobotStatusTextView() {  return robotStatusTextView; }

    public static GridMap getGridMap() { return gridMap; }

    public static Button getF1() { return f1; }

    public static Button getF2() { return f2; }

    public static void sharedPreferences() {
        sharedPreferences = MainActivity.getSharedPreferences(MainActivity.context);
        editor = sharedPreferences.edit();
    }

    public static void printMessage(String msg){
        showLog("Entering printMessage");
        editor = sharedPreferences.edit();
        if(BluetoothService.BluetoothConnectionStatus){
            byte[] bytes = msg.getBytes(Charset.defaultCharset());
            BluetoothService.write(bytes);
        }
        editor.putString("message", CommsFragment.getMessageReceivedTextView().getText() + "\n" + msg);
        editor.commit();
        refreshMessageReceived();
    }

    public static void printMessage(String name, int x, int y) throws JSONException {
        showLog("Entering printMessage");
        sharedPreferences();

        JSONObject jsonObject = new JSONObject();
        String message;

        switch(name) {
//            case "starting":
            case "waypoint":
                jsonObject.put(name, name);
                jsonObject.put("x", x);
                jsonObject.put("y", y);
                message = name + " (" + x + "," + y + ")";
                break;
            default:
                message = "Unexpected default for printMessage: " + name;
                break;
        }
        editor.putString("message", CommsFragment.getMessageReceivedTextView().getText() + "\n" + message);
        editor.commit();
        if (BluetoothService.BluetoothConnectionStatus == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothService.write(bytes);
        }
        showLog("Exiting printMessage");
    }

    public static void refreshMessageReceived() {
        CommsFragment.getMessageReceivedTextView().setText(sharedPreferences.getString("message", ""));
    }

    public void refreshDirection(String direction) {
        gridMap.setRobotDirection(direction);
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
        printMessage("Direction is set to " + direction);
    }

    public static void refreshLabel() {
        xAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[0]-1));
        yAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[1]-1));
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
    }

    public static void receiveMessage(String message) {
        showLog("Entering receiveMessage");
        sharedPreferences();
        editor.putString("message", sharedPreferences.getString("message", "") + "\n" + message);
        editor.commit();
        showLog("Exiting receiveMessage");
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    private BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
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
                Toast.makeText(MainActivity.this, "Device now connected to "+mDevice.getName(), Toast.LENGTH_SHORT).show();
                editor.putString("connStatus", "Connected to " + mDevice.getName());
//                TextView connStatusTextView = findViewById(R.id.connStatusTextView);
//                connStatusTextView.setText("Connected to " + mDevice.getName());
            }
            else if(status.equals("disconnected")){
                Log.d(TAG, "mBroadcastReceiver5: Disconnected from "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Disconnected from "+mDevice.getName(), Toast.LENGTH_SHORT).show();
//                mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
//                mBluetoothConnection.startAcceptThread();

                editor.putString("connStatus", "Disconnected");
//                TextView connStatusTextView = findViewById(R.id.connStatusTextView);
//                connStatusTextView.setText("Disconnected");

                myDialog.show();
            }
            editor.commit();
        }
    };



    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            showLog("receivedMessage: message --- " + message);
            try {
                if (message.length() > 7 && message.substring(2,6).equals("grid")) {
                    String resultString = "";
                    String amdString = message.substring(11,message.length()-2);
                    showLog("amdString: " + amdString);
                    BigInteger hexBigIntegerExplored = new BigInteger(amdString, 16);
                    String exploredString = hexBigIntegerExplored.toString(2);

                    while (exploredString.length() < 300)
                        exploredString = "0" + exploredString;

                    for (int i=0; i<exploredString.length(); i=i+15) {
                        int j=0;
                        String subString = "";
                        while (j<15) {
                            subString = subString + exploredString.charAt(j+i);
                            j++;
                        }
                        resultString = subString + resultString;
                    }
                    hexBigIntegerExplored = new BigInteger(resultString, 2);
                    resultString = hexBigIntegerExplored.toString(16);

                    JSONObject amdObject = new JSONObject();
                    amdObject.put("explored", "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
                    amdObject.put("length", amdString.length()*4);
                    amdObject.put("obstacle", resultString);
                    JSONArray amdArray = new JSONArray();
                    amdArray.put(amdObject);
                    JSONObject amdMessage = new JSONObject();
                    amdMessage.put("map", amdArray);
                    message = String.valueOf(amdMessage);
                    showLog("Executed for AMD message, message: " + message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                if (message.length() > 8 && message.substring(2,7).equals("image")) {
                    JSONObject jsonObject = new JSONObject(message);
                    JSONArray jsonArray = jsonObject.getJSONArray("image");
                    gridMap.drawImageNumberCell(jsonArray.getInt(0),jsonArray.getInt(1),jsonArray.getInt(2));
                    showLog("Image Added for index: " + jsonArray.getInt(0) + "," +jsonArray.getInt(1));
                }
            } catch (JSONException e) {
                showLog("Adding Image Failed");
            }

            if (gridMap.getAutoUpdate() || MapTabFragment.manualUpdateRequest) {
                try {
                    gridMap.setReceivedJsonObject(new JSONObject(message));
                    gridMap.updateMapInformation();
                    MapTabFragment.manualUpdateRequest = false;
                    showLog("messageReceiver: try decode successful");
                } catch (JSONException e) {
                    showLog("messageReceiver: try decode unsuccessful");
                }
            }
            sharedPreferences();
            String receivedText = sharedPreferences.getString("message", "") + "\n" + message;
            editor.putString("message", receivedText);
            editor.commit();
            refreshMessageReceived();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    mBTDevice = (BluetoothDevice) data.getExtras().getParcelable("mBTDevice");
                    myUUID = (UUID) data.getSerializableExtra("myUUID");
                }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            IntentFilter filter2 = new IntentFilter("ConnectionStatus");
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5, filter2);
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
}