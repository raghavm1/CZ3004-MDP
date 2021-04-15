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


public class BluetoothService {

    private final BluetoothAdapter bluetoothAdapter;
    Context context;

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private AcceptThread acceptThread;

    private ConnectThread connectThread;
    private BluetoothDevice bTDevice;
    private UUID uuidDevice;

    ProgressDialog progressDialog;
    Intent connectStatus;

    private static ConnectedThread connectedThread;
    public static boolean connStatusFlag = false;

    public BluetoothService(Context context) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        activateAcceptThread();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket bluetoothServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        Constants.APP_NAME,
                        MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            bluetoothServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
//            while (true) {
            try {
                socket = bluetoothServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
//                    break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                connectedSocket(socket, socket.getRemoteDevice());
//                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket bluetoothSocket;

        public ConnectThread(BluetoothDevice bluetoothDevice, UUID uuid) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            bTDevice = bluetoothDevice;
            uuidDevice = uuid;
        }

        public void run() {

            BluetoothSocket bTSocket = null;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                bTSocket = bTDevice.createRfcommSocketToServiceRecord(uuidDevice);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            bluetoothSocket = bTSocket;

            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                bluetoothSocket.connect();

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                connectedSocket(bluetoothSocket, bTDevice);
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    bluetoothSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                try {
                    BluetoothActivity bluetoothActivity = (BluetoothActivity) context;
                    bluetoothActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Failed to connect to the Device.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            try {
                progressDialog.dismiss();
            } catch (NullPointerException np) {
                np.printStackTrace();
            }
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    public synchronized void activateAcceptThread() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (acceptThread != null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public void activateClientThread(BluetoothDevice bTDevice, UUID uuid) {
        try {
            progressDialog = ProgressDialog.show(context,
                    "Connecting to Bluetooth",
                    "Please wait ..", true);
        } catch (Exception e) {
            Log.e("Bluetooth Service", "Client thread dialog failed to display");
        }

        connectThread = new ConnectThread(bTDevice, uuid);
        connectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
//        private byte[] buffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {

            connectStatus = new Intent("ConnectionStatus");
            connectStatus.putExtra("Status", "connected");
            connectStatus.putExtra("Device", bTDevice);
            LocalBroadcastManager.getInstance(context).sendBroadcast(connectStatus);
            connStatusFlag = true;

            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int noBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    noBytes = inputStream.read(buffer);
                    // Send the obtained bytes to the UI activity.
                    String msg = new String (buffer, 0, noBytes);

                    Intent msgIntent = new Intent("incomingMessage");
                    msgIntent.putExtra("receivedMessage", msg);

                    LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent);
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);

                    connectStatus = new Intent("ConnectionStatus");
                    connectStatus.putExtra("Status", "disconnected");
                    connectStatus.putExtra("Device", bTDevice);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(connectStatus);

                    connStatusFlag = false;

                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "Write: Writing to output stream: " + text);
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

    }

    private void connectedSocket(BluetoothSocket bluetoothSocket, BluetoothDevice bluetoothDevice) {

        bTDevice = bluetoothDevice;

        if(acceptThread != null){
            acceptThread.cancel();
            acceptThread = null;
        }

        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();
    }

    public static void write (byte[] out){
        ConnectedThread thread;

        connectedThread.write(out);
    }
}