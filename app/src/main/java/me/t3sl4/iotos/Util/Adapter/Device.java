package me.t3sl4.iotos.Util.Adapter;

import android.os.Parcel;
import android.os.Parcelable;

public class Device implements Parcelable {

    private String deviceName;
    private String deviceAddress;

    public Device(String deviceName, String deviceAddress) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }

    protected Device(Parcel in) {
        deviceName = in.readString();
        deviceAddress = in.readString();
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceName);
        dest.writeString(deviceAddress);
    }
}
