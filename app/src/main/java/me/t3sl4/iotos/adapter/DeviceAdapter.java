package me.t3sl4.iotos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import me.t3sl4.iotos.R;

public class DeviceAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Device> deviceList;

    public DeviceAdapter(Context context, ArrayList<Device> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.custom_device_list_item, parent, false);
            holder = new ViewHolder();
            holder.deviceName = convertView.findViewById(R.id.deviceNameTextView);
            holder.deviceAddress = convertView.findViewById(R.id.deviceAddressTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Device device = deviceList.get(position);
        holder.deviceName.setText(device.getDeviceName());
        holder.deviceAddress.setText(device.getDeviceAddress());

        return convertView;
    }

    private static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
