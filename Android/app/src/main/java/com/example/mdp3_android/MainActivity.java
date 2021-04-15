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
import com.example.mdp3_android.helper.Constants;
import com.example.mdp3_android.map.Maze;
import com.example.mdp3_android.pagerfragment.MapTabFragment;
import com.example.mdp3_android.pagerfragment.ReconfigureFragment;
import com.google.android.material.tabs.TabLayout;
import com.example.mdp3_android.pagerfragment.MessgaeTabFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.UUID;

//import com.example.mdp3_android.adapters.ViewPagerAdapter;
import com.example.mdp3_android.bluetooth.BluetoothActivity;
import com.example.mdp3_android.bluetooth.BluetoothService;

public class MainActivity extends AppCompatActivity {

    // Declaration Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;

    ReconfigureFragment reconfigureFragment = new ReconfigureFragment();
    private static Maze maze;

    Button reconfigure;
    static TextView robotStatus;
    static TextView xAxis, yAxis, directionAxis;
    static Button f1, f2;
    BluetoothDevice bluetoothDevice;
    ProgressDialog progressDialog;

    private static boolean autoUpdate = false;
    public static boolean manualUpdateRequest = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        maze = new Maze(this);
        xAxis = findViewById(R.id.xCoordTextView);
        yAxis = findViewById(R.id.yCoordTextView);
        maze = findViewById(R.id.mazeView);
        directionAxis = findViewById(R.id.directionTextView);
        robotStatus = findViewById(R.id.robotStatus);
        f1 =  findViewById(R.id.f1Button);
        f2 =  findViewById(R.id.f2Button);
        reconfigure =  findViewById(R.id.configureButton);

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
        editor.putString("direction",Constants.NONE);
        editor.putString("connStatus", "Disconnected");
        editor.commit();

        Button printMDFStringButton = (Button) findViewById(R.id.printMdfBtn);
        printMDFStringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Explored : " + Maze.getMDFExplorationString();
                editor = sharedPreferences.edit();
                editor.putString("message", MessgaeTabFragment.getreceivedMsgTxtView().getText() + "\n" + message);
                editor.commit();
                refreshMessageReceived();
                message = "Obstacle : " + Maze.getMDFObstacleString() + Constants.ZERO;
                editor.putString("message", MessgaeTabFragment.getreceivedMsgTxtView().getText() + "\n" + message);
                editor.commit();
                refreshMessageReceived();
            }
        });

        Switch manualAutoToggleBtn = findViewById(R.id.manualAutoToggleBtn);
        manualAutoToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (manualAutoToggleBtn.getText().equals("MANUAL")) {
                    try {
                        maze.setAutoUpdate(true);
                        autoUpdate = true;
                        maze.toggleCheckedBtn(Constants.NONE);
                        manualAutoToggleBtn.setText("AUTO");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.context, "Auto mode", Toast.LENGTH_SHORT).show();
                }
                else if (manualAutoToggleBtn.getText().equals("AUTO")) {
                    try {
                        maze.setAutoUpdate(false);
                        autoUpdate = false;
                        maze.toggleCheckedBtn(Constants.NONE);
                        manualAutoToggleBtn.setText("MANUAL");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.context, "Manual mode", Toast.LENGTH_SHORT).show();
                }
            }
        });

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Waiting for the other device to reconnect ...");
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        if (sharedPreferences.contains("F1")) {
            f1.setContentDescription(sharedPreferences.getString("F1", ""));
        }
        if (sharedPreferences.contains("F2")) {
            f2.setContentDescription(sharedPreferences.getString("F2", ""));
        }

        f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!f1.getContentDescription().toString().equals("empty"))
                    MainActivity.outputMessage(f1.getContentDescription().toString());
            }
        });

        f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!f2.getContentDescription().toString().equals("empty"))
                    MainActivity.outputMessage(f2.getContentDescription().toString());
            }
        });

        reconfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reconfigureFragment.show(getFragmentManager(), "Reconfigure Fragment");
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

    public static TextView getRobotStatus() {  return robotStatus; }

    public static Maze getMaze() { return maze; }

    public static Button getF1() { return f1; }

    public static Button getF2() { return f2; }

    public static void sharedPreferences() {
        sharedPreferences = MainActivity.getSharedPreferences(MainActivity.context);
        editor = sharedPreferences.edit();
    }

    public static void outputMessage(String msg){
        editor = sharedPreferences.edit();
        if(BluetoothService.connStatusFlag){
            byte[] bytes = msg.getBytes(Charset.defaultCharset());
            BluetoothService.write(bytes);
        }
        editor.putString("message", MessgaeTabFragment.getreceivedMsgTxtView().getText() + "\n" + msg);
        editor.commit();
        refreshMessageReceived();
    }

    public static void outputMessage(String name, int x, int y) throws JSONException {
        sharedPreferences();

        JSONObject jsonObject = new JSONObject();
        String message;
        
        if(name.equals("waypoint")){
            jsonObject.put(name, name);
            jsonObject.put("x", x);
            jsonObject.put("y", y);
            message = name + " (" + x + "," + y + ")";
        }else{
            message = "Unexpected Message";
        }
        
        editor.putString("message", MessgaeTabFragment.getreceivedMsgTxtView().getText() + "\n" + message);
        editor.commit();
        if (BluetoothService.connStatusFlag == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothService.write(bytes);
        }
    }

   
    public void refreshDirection(String direction) {
        maze.setRobotDirection(direction);
        directionAxis.setText(sharedPreferences.getString("direction",""));
        outputMessage("Direction is set to " + direction);
    }

   

    public static void receiveMessage(String message) {
        sharedPreferences();
        editor.putString("message", sharedPreferences.getString("message", "") + "\n" + message);
        editor.commit();
    }


    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences();

            if(status.equals("connected")){
                try {
                    progressDialog.dismiss();
                } catch(NullPointerException e){
                    e.printStackTrace();
                }

                Log.d("Main Activity", "Device is now connected to "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Device is now connected to "+mDevice.getName(), Toast.LENGTH_SHORT).show();
                editor.putString("connStatus", "Device is now Connected to " + mDevice.getName());
            }
            else if(status.equals("disconnected")){
                Log.d("Main Activity", "Device is disconnected from "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Device is disconnected from "+mDevice.getName(), Toast.LENGTH_SHORT).show();
                editor.putString("connStatus", "Disconnected");
                progressDialog.show();
            }
            editor.commit();
        }
    };
    
    public static void refreshLabel() {
        xAxis.setText(String.valueOf(maze.getCurCoord()[0]-1));
        yAxis.setText(String.valueOf(maze.getCurCoord()[1]-1));
        directionAxis.setText(sharedPreferences.getString("direction",""));
    }

    public static void refreshMessageReceived() {
        MessgaeTabFragment.getreceivedMsgTxtView().setText(sharedPreferences.getString("message", ""));
    }
    

    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            try {
                if (message.length() > 7 && message.substring(2,6).equals("grid")) {
                    String resultString = "";
                    String amdString = message.substring(11,message.length()-2);
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
                    amdObject.put("explored", Constants.MDF_ALL_F_STRING);
                    amdObject.put("length", amdString.length()*4);
                    amdObject.put("obstacle", resultString);
                    JSONArray amdArray = new JSONArray();
                    amdArray.put(amdObject);
                    JSONObject amdMessage = new JSONObject();
                    amdMessage.put("map", amdArray);
                    message = String.valueOf(amdMessage);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                if (message.length() > 8 && message.substring(2,7).equals("image")) {
                    JSONObject jsonObject = new JSONObject(message);
                    JSONArray jsonArray = jsonObject.getJSONArray("image");
                    maze.drawImageRec(jsonArray.getInt(0),jsonArray.getInt(1),jsonArray.getInt(2));
                }
            } catch (JSONException e) {
            }

            if (maze.getAutoUpdate() || MapTabFragment.manualReq) {
                try {
                    maze.setReceivedJsonObject(new JSONObject(message));
                    maze.updatemazeInfo();
                    MapTabFragment.manualReq = false;
                } catch (JSONException e) {
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
                    bluetoothDevice = (BluetoothDevice) data.getExtras().getParcelable("mBTDevice");
                }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            IntentFilter filter2 = new IntentFilter("ConnectionStatus");
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter2);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Main Activity", "onSaveInstanceState");
    }
}