package me.t3sl4.iotos;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeviceDetailScreen extends AppCompatActivity {

    private TextView inputDataTextView;
    private TextView macTextView;


    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "IoT-OS";
    public static Handler handler;
    private final static int ERROR_READ = 0;
    BluetoothDevice arduinoBTModule = null;

    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        inputDataTextView = findViewById(R.id.inputDataTextView);
        macTextView = findViewById(R.id.macAddressTextView);

        String deviceAddress = this.getIntent().getStringExtra(DeviceListScreen.EXTRA_INFO);

        macTextView.setText("MAC: " + deviceAddress);

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        initializeBluetoothDevice(bluetoothAdapter, deviceAddress);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case ERROR_READ:
                        String arduinoMsg = msg.obj.toString();
                        inputDataTextView.setText(arduinoMsg);
                        break;
                }
            }
        };

        final Observable<String> connectToBTObservable = Observable.create(emitter -> {
            Log.d(TAG, "Calling connectThread class");
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, MY_UUID, handler);
            connectThread.run();
            if (connectThread.getMmSocket().isConnected()) {
                Log.d(TAG, "Calling ConnectedThread class");
                ConnectedThread connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.run();
                if(connectedThread.getValueRead()!=null)
                {
                    emitter.onNext(connectedThread.getValueRead());
                }
                connectedThread.cancel();
            }

            connectThread.cancel();
            emitter.onComplete();
        });

        connectBT(connectToBTObservable);
    }

    private void initializeBluetoothDevice(BluetoothAdapter bluetoothAdapter, String deviceAddress) {
        if (bluetoothAdapter == null) {
            Log.d(TAG, "Device doesn't support Bluetooth");
        } else {
            Log.d(TAG, "Device support Bluetooth");
            if (!bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth is disabled");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "We don't BT Permissions");
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Log.d(TAG, "Bluetooth is enabled now");
                } else {
                    Log.d(TAG, "We have BT Permissions");
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Log.d(TAG, "Bluetooth is enabled now");
                }

            } else {
                Log.d(TAG, "Bluetooth is enabled");
            }
            String btDevicesString="";
            Set< BluetoothDevice > pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device: pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.d(TAG, "deviceName:" + deviceName);
                    Log.d(TAG, "deviceHardwareAddress:" + deviceHardwareAddress);
                    btDevicesString=btDevicesString+deviceName+" || "+deviceHardwareAddress+"\n";

                    if (deviceHardwareAddress.equals(deviceAddress)) {
                        Log.d(TAG, "HC-05 found");
                        MY_UUID = device.getUuids()[0].getUuid();
                        arduinoBTModule = device;
                    }
                    macTextView.setText("MAC: " + deviceHardwareAddress);
                }
            }
        }
    }

    private void connectBT(Observable<String> connectToBTObservable) {
        inputDataTextView.setText("");
        if (arduinoBTModule != null) {
            connectToBTObservable.
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribeOn(Schedulers.io()).
                    subscribe(valueRead -> {
                        inputDataTextView.setText(valueRead);
                    });

        }
    }
}