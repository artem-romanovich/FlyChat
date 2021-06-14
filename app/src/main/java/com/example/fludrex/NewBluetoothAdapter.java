package com.example.fludrex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class NewBluetoothAdapter extends ArrayAdapter<MyBluetoothDevices>{
    private LayoutInflater inflater;
    private int layout;
    private ArrayList<MyBluetoothDevices> devicesList;

    public NewBluetoothAdapter(Context context, int resource, ArrayList<MyBluetoothDevices> devices) {
        super(context, resource, devices);
        this.devicesList = devices;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if(convertView==null){
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = new ViewHolder(convertView);
        }
        final MyBluetoothDevices devices = devicesList.get(position);
        viewHolder.name_device.setSelected(true);
        viewHolder.name_device.setText(String.format(" %s ", devices.getName()));
        return convertView;
    }

    private class ViewHolder {
        final TextView name_device;
        ViewHolder(View view){
            name_device = view.findViewById(R.id.name_device);
        }
    }
}
