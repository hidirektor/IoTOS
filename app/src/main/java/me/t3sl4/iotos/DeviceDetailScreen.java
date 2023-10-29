package me.t3sl4.iotos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class DeviceDetailScreen extends AppCompatActivity {

    private TextView inputDataTextView;
    private TextView macTextView;

    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private InputStream mmInputStream;
    private OutputStream mmOutputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        inputDataTextView = findViewById(R.id.inputDataTextView);
        macTextView = findViewById(R.id.macAddressTextView);

        String deviceAddress = this.getIntent().getStringExtra(DeviceListScreen.EXTRA_INFO);

        macTextView.setText("MAC: " + deviceAddress);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mmDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            mmSocket.connect();

            mmInputStream = mmSocket.getInputStream();
            mmOutputStream = mmSocket.getOutputStream();

            beginListenForData();

        } catch (IOException e) {
            Log.e("BluetoothError", "Socket oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void beginListenForData() {
        final Handler handler = new Handler();

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                try {
                    int bytesAvailable = mmInputStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        mmInputStream.read(packetBytes);

                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == '\n') {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, "US-ASCII");
                                readBufferPosition = 0;

                                handler.post(() -> inputDataTextView.setText(data));
                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException ex) {
                    stopWorker = true;
                }
            }
        });

        workerThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            stopWorker = true;
            mmInputStream.close();
            mmOutputStream.close();
            mmSocket.close();
        } catch (IOException e) {
            Log.e("BluetoothError", "Kapatma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}