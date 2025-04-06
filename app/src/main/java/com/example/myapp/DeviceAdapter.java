// DeviceAdapter.java (New file)
package com.example.myapp;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private ArrayList<BluetoothDevice> devices;

    public DeviceAdapter(ArrayList<BluetoothDevice> devices) {
        this.devices = devices;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.text1.setText(device.getName() != null ? device.getName() : "Unknown Device");
        holder.text2.setText("Address: " + device.getAddress() + " | Connected: Yes");
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}