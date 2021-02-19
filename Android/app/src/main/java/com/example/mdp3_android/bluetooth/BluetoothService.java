package com.example.mdp3_android.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mdp3_android.helper.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by Gwyn Bong Xiao Min on 29/1/2021.
 */
public class BluetoothService {
    BluetoothActivity mBluetoothPopup;
    private static BluetoothService instance;
    private static final String TAG = "DebuggingTag";

    private static final String appName = "MDP_Group_15";
    public static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;
    Intent connectionStatus;

    public static boolean BluetoothConnectionStatus=false;
    private static ConnectedThread mConnectedThread;

    public BluetoothService(Context context) {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = context;
        startAcceptThread();
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket ServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, myUUID);
                Log.d(TAG, "Accept Thread: Setting up Server using: " + myUUID);
            }catch(IOException e){
                Log.e(TAG, "Accept Thread: IOException: " + e.getMessage());
            }
            ServerSocket = tmp;
        }
        public void run(){
            Log.d(TAG, "run: AcceptThread Running. ");
            BluetoothSocket socket =null;
            try {
                Log.d(TAG, "run: RFCOM server socket start here...");

                socket = ServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection.");
            }catch (IOException e){
                Log.e(TAG, "run: IOException: " + e.getMessage());
            }
            if(socket!=null){
                connected(socket, socket.getRemoteDevice());
            }
            Log.i(TAG, "END AcceptThread");
        }
        public void cancel(){
            Log.d(TAG, "cancel: Cancelling AcceptThread");
            try{
                ServerSocket.close();
            } catch(IOException e){
                Log.e(TAG, "cancel: Failed to close AcceptThread ServerSocket " + e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread{
        private BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice device, UUID u){
            Log.d(TAG, "ConnectThread: started.");
            mDevice = device;
            deviceUUID = u;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.d(TAG, "RUN: mConnectThread");

            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + myUUID);
                tmp = mDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }
            mSocket= tmp;
            mBluetoothAdapter.cancelDiscovery();

            try {
                mSocket.connect();

                Log.d(TAG, "RUN: ConnectThread connected.");

                connected(mSocket,mDevice);

            } catch (IOException e) {
                try {
                    mSocket.close();
                    Log.d(TAG, "RUN: ConnectThread socket closed.");
                } catch (IOException e1) {
                    Log.e(TAG, "RUN: ConnectThread: Unable to close connection in socket."+ e1.getMessage());
                }
                Log.d(TAG, "RUN: ConnectThread: could not connect to UUID."+ myUUID);
                try {
                    BluetoothActivity mBluetoothPopUpActivity = (BluetoothActivity) mContext;
                    mBluetoothPopUpActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Toast.makeText(mContext, "Failed to connect to the Device.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception z) {
                    z.printStackTrace();
                }

            }
            try {
                mProgressDialog.dismiss();
            } catch(NullPointerException e){
                e.printStackTrace();
            }
        }

        public void cancel(){
            Log.d(TAG, "cancel: Closing Client Socket");
            try{
                mSocket.close();
            } catch(IOException e){
                Log.e(TAG, "cancel: Failed to close ConnectThread mSocket " + e.getMessage());
            }
        }
    }

    public synchronized void startAcceptThread(){
        Log.d(TAG, "start");

        if(mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread=null;
        }
        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClientThread(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started.");

        try {
            mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please Wait...", true);
        } catch (Exception e) {
            Log.d(TAG, "StartClientThread Dialog show failure");
        }


        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mSocket;
        private final InputStream inStream;
        private final OutputStream outStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            connectionStatus = new Intent("ConnectionStatus");
            connectionStatus.putExtra("Status", "connected");
            connectionStatus.putExtra("Device", mDevice);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(connectionStatus);
            BluetoothConnectionStatus = true;

            this.mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            while(true){
                try {
                    bytes = inStream.read(buffer);
                    String incomingmessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: "+ incomingmessage);

                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("receivedMessage", incomingmessage);

                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
                } catch (IOException e) {
                    Log.e(TAG, "Error reading input stream. "+e.getMessage());

                    connectionStatus = new Intent("ConnectionStatus");
                    connectionStatus.putExtra("Status", "disconnected");
                    connectionStatus.putExtra("Device", mDevice);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(connectionStatus);
                    BluetoothConnectionStatus = false;

                    break;
                }
            }
        }

        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to output stream: "+text);
            try {
                outStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error writing to output stream. "+e.getMessage());
            }
        }

        public void cancel(){
            Log.d(TAG, "cancel: Closing Client Socket");
            try{
                mSocket.close();
            } catch(IOException e){
                Log.e(TAG, "cancel: Failed to close ConnectThread mSocket " + e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket mSocket, BluetoothDevice device) {
        Log.d(TAG, "connected: Starting.");
        mDevice =  device;
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(mSocket);
        mConnectedThread.start();
    }

    public static void write(byte[] out){
        ConnectedThread tmp;

        Log.d(TAG, "write: Write is called." );
        mConnectedThread.write(out);
    }
}
