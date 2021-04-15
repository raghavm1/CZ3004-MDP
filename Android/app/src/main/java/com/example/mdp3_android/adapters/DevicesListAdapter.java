package com.example.mdp3_android.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mdp3_android.R;

import java.util.ArrayList;

/**
 * Created by Gwyn Bong Xiao Min on 26/1/2021.
 */
public class DevicesListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater layoutInflater;
    private ArrayList<BluetoothDevice> devicesList;
    private int resourceId;

    public DevicesListAdapter(@NonNull Context context, int resource,
                              ArrayList<BluetoothDevice> devicesList) {
        super(context, resource, devicesList);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resourceId = resource;
        this.devicesList = devicesList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        convertView = layoutInflater.inflate(resourceId, null);

        BluetoothDevice bluetoothDevice = devicesList.get(position);

        if(bluetoothDevice != null){
            TextView deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
            TextView deviceAdd = (TextView) convertView.findViewById(R.id.tvDeviceAddress);

            if(deviceName != null){
                deviceName.setText(bluetoothDevice.getName());
                deviceAdd.setText(bluetoothDevice.getAddress());
            }
        }

        return convertView;
    }
}
