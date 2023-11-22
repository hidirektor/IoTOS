package me.t3sl4.iotos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;

import me.t3sl4.iotos.Util.Adapter.Device;
import me.t3sl4.iotos.Util.Adapter.DeviceAdapter;

public class DeviceListScreen extends AppCompatActivity {
    public static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    public static String EXTRA_INFO = "device_address";
    public static BluetoothAdapter myBluetooth;
    private Set<BluetoothDevice> pairedDevices;


    private ListView deviceList;
    private Toolbar toolbar;
    String info;

    private DeviceAdapter deviceListAdapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_device_list_screen);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        requestBluetoothPermissions();

        if (readFromFileAndUpdateSensorNames().length < 2) {
            initializeConfigFile();
        }

        deviceList = findViewById(R.id.listView);
        toolbar = findViewById(R.id.toolbar_dev_list);

        try {
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(-1);
        } catch (NullPointerException e) {
            Log.e("NO_TOOLBAR_ERROR", "Toolbar bulunamadı !", e);
        }

        if (myBluetooth == null) {
            showToast("Bluetooth is not supported on this device");
            finish(); // or handle the lack of Bluetooth support appropriately
            return;
        } else if (!myBluetooth.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
            return;
        }

        pairedDevicesList();

        deviceList.setOnItemClickListener((adapterView, view, position, id) -> {
            Device selectedDevice = (Device) deviceListAdapter.getItem(position);
            String deviceAddress = selectedDevice.getDeviceAddress();

            Intent intent = new Intent(DeviceListScreen.this, DeviceDetailScreen.class);
            intent.putExtra(EXTRA_INFO, deviceAddress);
            startActivity(intent);
        });
    }

    private void pairedDevicesList() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        pairedDevices = myBluetooth.getBondedDevices();
        if (pairedDevices == null) {
            Toast.makeText(getApplicationContext(), "Eşleştirilen cihaz yok.", Toast.LENGTH_LONG).show();
            return;
        }
        ArrayList<Device> deviceListMap = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceListMap.add(new Device(device.getName(), device.getAddress()));
            }
        } else {
            Toast.makeText(getApplicationContext(), "Eşleştirilen cihaz yok.", Toast.LENGTH_LONG).show();
        }
        if (deviceListMap != null) {
            deviceListAdapter = new DeviceAdapter(getApplicationContext(), deviceListMap);
            if (deviceList != null) { // Null check added here
                deviceList.setAdapter(deviceListAdapter);
            } else {
                Log.e("DeviceListScreen", "deviceList is null.");
            }
        }
    }

    private void requestBluetoothPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
        } else {
            pairedDevicesList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pairedDevicesList();
            } else {
                Toast.makeText(this, "Bluetooth Permission Denied. Cannot Proceed.", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> finish(), 2000);
            }
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != R.id.action_refresh) {
            return super.onOptionsItemSelected(menuItem);
        }
        pairedDevicesList();
        return true;
    }

    private String[] getCleanStrings(String str) {
        return str.split(",");
    }

    private String[] readFromFileAndUpdateSensorNames() {
        String str = "";
        try {
            FileInputStream openFileInput = getApplicationContext().openFileInput("config.txt");
            if (openFileInput != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openFileInput));
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    sb.append(readLine);
                }
                openFileInput.close();
                str = sb.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e2) {
            Log.e("login activity", "Can not read file: " + e2.toString());
        }
        return getCleanStrings(str);
    }

    public void initializeConfigFile() {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("config.txt", 0));
            outputStreamWriter.write("Sensor 1,Sensor 2,Sensor 3,Sensor 4,Sensor 5,Sensor 6,Sensor 7,Sensor 8,Sensor 9,Sensor 10");
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
