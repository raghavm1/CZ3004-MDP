package com.example.mdp3_android.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp3_android.R;
import com.example.mdp3_android.adapters.DevicesListAdapter;
import com.example.mdp3_android.helper.Constants;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> pairedDevices;
    ArrayList<BluetoothDevice> availDevices;
    DevicesListAdapter pairedDeviceListAdapter;
    DevicesListAdapter availDeviceListAdapter;

    ListView pairedDeviceListView;
    ListView availDeviceListView;
    TextView connStatusTextView;
    String connStatus;
    ProgressDialog dialog;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    BluetoothService bluetoothConnection;
    public static BluetoothDevice bTDevice;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    boolean retryConnect = false;
    Handler reconnectionHandler = new Handler();

    Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
           try {
               if(BluetoothService.BluetoothConnectionStatus == false){
                   connectDevice(bTDevice, MY_UUID);
                   Toast.makeText(BluetoothActivity.this,
                           "Successfuly reconnected!", Toast.LENGTH_SHORT).show();
               }
               reconnectionHandler.removeCallbacks(reconnectRunnable);
               retryConnect = false;
           } catch (Exception e){
               Toast.makeText(BluetoothActivity.this,
                                "Unable to reconnect, trying in 5 seconds",
                                Toast.LENGTH_SHORT).show();
           }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//
//        int width = dm.widthPixels;
//        int height = dm.heightPixels;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get default adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        pairedDevices = new ArrayList<>();
        availDevices = new ArrayList<>();

        Switch bluetoothSwitch = (Switch) findViewById(R.id.bluetoothSwitch);
        pairedDeviceListView = (ListView) findViewById(R.id.lvPairedDevices);
        availDeviceListView = (ListView) findViewById(R.id.lvAvailDevices);
        connStatusTextView = (TextView) findViewById(R.id.tvDeviceStatus);

        //update connection status
        connStatus = "Disconnected";

//        if(bluetoothAdapter.isEnabled()){
//            bluetoothSwitch.setChecked(true);
//            bluetoothSwitch.setText(Constants.BLUETOOTH_ON);
//            //scanNewDevices();
//
//        }
//
//        if (!bluetoothAdapter.isEnabled()){
//            bluetoothSwitch.setChecked(false);
//            bluetoothSwitch.setText(Constants.BLUETOOTH_OFF);
//        }


        IntentFilter bondfilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bluetoothBondReceiver, bondfilter);

        IntentFilter connectionfilter = new IntentFilter("ConnectionStatus");
        LocalBroadcastManager.getInstance(this).registerReceiver(connectionReceiver, connectionfilter);

        if(bluetoothAdapter == null){
            Toast.makeText(BluetoothActivity.this,
                    "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        } else { //device supports bluetooth
            //device bluetooth is OFF
            if(!bluetoothAdapter.isEnabled()){

                //pop up dialog to allow user to allow bluetooth visibility
                Intent reqDiscoverIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                reqDiscoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                        600);

                startActivity(reqDiscoverIntent);

                IntentFilter stateIntent =
                        new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(bluetoothStateReceiver, stateIntent);

                IntentFilter scanIntent =
                        new IntentFilter((BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
                registerReceiver(bluetoothScanReceiver, scanIntent);

            } else { //device bluetooth is ON

//                bluetoothAdapter.disable();

                IntentFilter stateIntent =
                        new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(bluetoothStateReceiver, stateIntent);

//                        connStatusTextView.setText(connStatus);
                //LocalBroadcastManager.getInstance(BluetoothActivity.this).unregisterReceiver(connectionReceiver);

//                        availDeviceListView.setVisibility(View.GONE);
//                        pairedDeviceListView.setVisibility(View.GONE);

            }
        }

//        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isToggled) {
//                //check if device supports bluetooth
//                if(bluetoothAdapter == null){
//                    Toast.makeText(BluetoothActivity.this,
//                            "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
//                    compoundButton.setChecked(false);
//                } else { //device supports bluetooth
//                    //device bluetooth is OFF
//                    if(!bluetoothAdapter.isEnabled()){
//
//                        //pop up dialog to allow user to allow bluetooth visibility
//                        Intent reqDiscoverIntent =
//                                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                        reqDiscoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
//                                                    600);
//
//                        startActivity(reqDiscoverIntent);
//
//                        //toggle bluetooth switches
//                        compoundButton.setChecked(true);
//                        compoundButton.setText(Constants.BLUETOOTH_ON);
//
//                        IntentFilter stateIntent =
//                                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//                        registerReceiver(bluetoothStateReceiver, stateIntent);
//
//                        IntentFilter scanIntent =
//                                new IntentFilter((BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
//                        registerReceiver(bluetoothScanReceiver, scanIntent);
//
//                    } else { //device bluetooth is ON
//
//                        bluetoothAdapter.disable();
//                        compoundButton.setText(Constants.BLUETOOTH_OFF);
//
//                        IntentFilter stateIntent =
//                                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//                        registerReceiver(bluetoothStateReceiver, stateIntent);
//
////                        connStatusTextView.setText(connStatus);
//                        LocalBroadcastManager.getInstance(BluetoothActivity.this).unregisterReceiver(connectionReceiver);
//
////                        availDeviceListView.setVisibility(View.GONE);
////                        pairedDeviceListView.setVisibility(View.GONE);
//
//                    }
//                }
//            }
//        });

        pairedDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                availDeviceListView.setAdapter(availDeviceListAdapter);

//                String deviceName = pairedDevices.get(i).getName();
//                String deviceAddress = pairedDevices.get(i).getAddress();

                bluetoothConnection = new BluetoothService(BluetoothActivity.this);
                bTDevice = pairedDevices.get(i);

                if(bTDevice != null){
                    activateConnection();
                }
            }
        });

        availDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                pairedDeviceListView.setAdapter(pairedDeviceListAdapter);

                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    availDevices.get(i).createBond();

                    bluetoothConnection = new BluetoothService(BluetoothActivity.this);
                    bTDevice = availDevices.get(i);
                }

                if(bTDevice != null){
                    activateConnection();
                }
            }
        });


        connStatusTextView.setText(connStatus);

        sharedPreferences = getApplicationContext().getSharedPreferences("Shared Preferences",
                Context.MODE_PRIVATE);
        if(sharedPreferences.contains("connStatus")) {
            connStatus = sharedPreferences.getString("connStatus", "");
        }

        connStatusTextView.setText(connStatus);

        dialog = new ProgressDialog(BluetoothActivity.this);
        dialog.setMessage("Waiting for other devices to reconnect");
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bluetooth_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean isSelected = false;

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.btnRefreshDevicesList:
//                    scanNewDevices();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        editor = sharedPreferences.edit();
        editor.putString("connStatus", connStatusTextView.getText().toString());
        editor.commit();
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void scanNewDevices(MenuItem menuItem){
    availDevices.clear();
        if(bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(BluetoothActivity.this,
                        "Please turn on Bluetooth",
                        Toast.LENGTH_SHORT).show();
            }
            //scan devices
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();

                checkPermissions();

                bluetoothAdapter.startDiscovery();
                IntentFilter discoverIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(bluetoothAvailDevicesReceiver, discoverIntent);

            } else if (!bluetoothAdapter.isDiscovering()) {

                checkPermissions();

                bluetoothAdapter.startDiscovery();
                IntentFilter discoverIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(bluetoothAvailDevicesReceiver, discoverIntent);
            }

            pairedDevices.clear();
            //get paired devices
            Set<BluetoothDevice> pairedDevicesList = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevicesList) {
                pairedDevices.add(device);
                pairedDeviceListAdapter = new DevicesListAdapter(this,
                                                        R.layout.lv_devices_list, pairedDevices);
                pairedDeviceListView.setAdapter(pairedDeviceListAdapter);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION")
                        + this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            }

            if(permissionCheck != 0){
                this.requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                                                    1001);
            }
        }
    }

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                                    BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("BluetoothActivity", "bluetoothStateReceiver: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("BluetoothActivity", "bluetoothStateReceiver: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("BluetoothActivity", "bluetoothStateReceiver: STATE ON");

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("BluetoothActivity", "bluetoothStateReceiver: STATE TURNING ON");
                        break;
                }
            }

        }
    };

    private final BroadcastReceiver bluetoothScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)){
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
                                                    BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d("BluetoothActivity", "bluetoothScanReceiver: Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d("BluetoothActivity", "bluetoothScanReceiver: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d("BluetoothActivity", "bluetoothScanReceiver: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d("BluetoothActivity", "bluetoothScanReceiver: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d("BluetoothActivity", "bluetoothScanReceiver: Connected.");
                        break;
                }
            }
        }
    };

     BroadcastReceiver bluetoothAvailDevicesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            // finding devices
            if(BluetoothDevice.ACTION_FOUND.equals(action)){

                //get BluetoothDevice object from Intent
                BluetoothDevice bluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //add name and address to an array adapter
                availDevices.add(bluetoothDevice);
                Log.d("Bluetooth Activity", "onReceive: "+
                        bluetoothDevice.getName() +" : " +
                        bluetoothDevice.getAddress());

                availDeviceListAdapter = new DevicesListAdapter(context,
                        R.layout.lv_devices_list,
                        availDevices);
                availDeviceListView.setAdapter(availDeviceListAdapter);
            }
        }
    };

    private BroadcastReceiver bluetoothBondReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                BluetoothDevice bluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(BluetoothActivity.this, "Successfully paired with "
                            + bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                    bTDevice = bluetoothDevice;
                }
                if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.e("Bluetooth Bond", "BOND_BONDING");
                }
                if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.e("Bluetooth Bond", "BOND_NONE");
                }
            }
        }
    });

    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice btDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences =
                    getApplicationContext().getSharedPreferences("Shared Preferences",
                                                                    Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();

            if(status.equals("connected")){
                try{
                    dialog.dismiss();
                }catch (NullPointerException np){
                    np.printStackTrace();
                }

                Toast.makeText(BluetoothActivity.this, "Device now connected to "
                                + btDevice.getName(), Toast.LENGTH_SHORT).show();
                editor.putString("connStatus", "Connected to " + btDevice.getName());
                connStatusTextView.setText("Connected to " + btDevice.getName());
            } else if(status.equals("disconnected") && retryConnect == false){
                Toast.makeText(BluetoothActivity.this, "Disconnected from "
                                + btDevice.getName(), Toast.LENGTH_SHORT).show();
                bluetoothConnection = new BluetoothService(BluetoothActivity.this);

                sharedPreferences =
                        getApplicationContext().getSharedPreferences("Shared Preferences",
                                                                        Context.MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString("connStatus", "Disconnected");
                TextView connStatusTextView = findViewById(R.id.tvDeviceStatus);
                connStatusTextView.setText("Disconnected");
                editor.commit();

                try {
                    dialog.show();
                } catch (Exception e){
                    Log.d("BluetoothActivity", "Dialog Exception: Failed to show dialog");
                }
                retryConnect = true;
                reconnectionHandler.postDelayed(reconnectRunnable, 5000);
            }
            editor.commit();
        }
    };

    public void activateConnection(){
        connectDevice(bTDevice, MY_UUID);
    }

    public void connectDevice(BluetoothDevice bluetoothDevice, UUID uuid){
        bluetoothConnection.startClientThread(bluetoothDevice, uuid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try{
            unregisterReceiver(bluetoothBondReceiver);
            unregisterReceiver(bluetoothScanReceiver);
            unregisterReceiver(bluetoothStateReceiver);
            unregisterReceiver(bluetoothAvailDevicesReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(connectionReceiver);
        } catch (IllegalArgumentException exception){
            exception.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try{
            unregisterReceiver(bluetoothBondReceiver);
            unregisterReceiver(bluetoothScanReceiver);
            unregisterReceiver(bluetoothStateReceiver);
            unregisterReceiver(bluetoothAvailDevicesReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(connectionReceiver);
        } catch (IllegalArgumentException exception){
            exception.printStackTrace();
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("BTDevice", bTDevice);
        data.putExtra("myUUID", MY_UUID);
        setResult(RESULT_OK, data);
        super.finish();
    }
}