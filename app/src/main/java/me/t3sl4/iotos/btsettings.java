package me.t3sl4.iotos;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Set;

public class btsettings extends AppCompatActivity {

    private TextView tv_status;
    private Button btn_enable;
    private Button btn_view_paired;
    private Button btn_scan;
    private ProgressDialog mProgressDlg;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();/*Bluetooth deviceları için arraylist oluşturuyoruz.*/
    private BluetoothAdapter mBluetoothAdapter;/*Bluetooth adapter oluşturuyoruz.*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btsettings);

        tv_status = (TextView) findViewById(R.id.tv_status);
        btn_enable = (Button) findViewById(R.id.btn_enable);
        btn_view_paired = (Button) findViewById(R.id.btn_view_paired);
        btn_scan = (Button) findViewById(R.id.btn_scan);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();/*Adapterımıza cihazdaki default olan adapteri atyoruz.*/
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Taranıyor...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "İptal", (dialog, which) -> {
            dialog.dismiss();
            if (ActivityCompat.checkSelfPermission(btsettings.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mBluetoothAdapter.cancelDiscovery();
        });

        //region  if (mBluetoothAdapter)
        if (mBluetoothAdapter == null) {
            showUnsupported();/*Adapter boş ise desteklenmiyor diye TOAST basıyoruz.*/
        } else {
            btn_view_paired.setOnClickListener(new View.OnClickListener() {/*Eşleşmiş cihazları telefonumuzda arıyor.*/
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(btsettings.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    if (pairedDevices == null || pairedDevices.size() == 0) {
                        showToast("Eşleşmiş Cihaz Bulunamadı");
                    } else {
                        /*Eşleşmiş cihazları listeleye ekliyoruz.*/
                        ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
                        list.addAll(pairedDevices);
                        Intent intent = new Intent(btsettings.this, DeviceListActivity.class);
                        intent.putParcelableArrayListExtra("device.list", list);
                        startActivity(intent);
                    }
                }
            });
            btn_scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    /*Eşleştirme işlemi için cihaz arıyoruz.*/
                    if (ActivityCompat.checkSelfPermission(btsettings.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mBluetoothAdapter.startDiscovery();
                }
            });
            btn_enable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Bluetooth açıp kapatıyoruz.*/
                    if (mBluetoothAdapter.isEnabled()) {
                        if (ActivityCompat.checkSelfPermission(btsettings.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mBluetoothAdapter.disable();
                        showDisabled();
                    } else {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, 1000);
                    }
                }
            });
            if (mBluetoothAdapter.isEnabled()) {
                showEnabled();
            } else {
                showDisabled();
            }
        }
        //endregion

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    /*****************************
     * onCreate END
     *********************/
    @Override
    public void onPause() {
        if (mBluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void showEnabled() {
        tv_status.setText("Bluetooth açık");
        tv_status.setTextColor(Color.BLUE);
        btn_enable.setText("Kapat");
        btn_enable.setEnabled(true);
        btn_view_paired.setEnabled(true);
        btn_scan.setEnabled(true);
    }

    private void showDisabled() {
        tv_status.setText("Bluetooth kapalı");
        //f77f0202
        tv_status.setTextColor(Color.rgb(128, 0, 0));
        btn_enable.setText("Aç");
        btn_enable.setEnabled(true);
        btn_view_paired.setEnabled(false);
        btn_scan.setEnabled(false);
    }

    private void showUnsupported() {
        tv_status.setText("Cihaz bluetooth desteklemiyor");
        btn_enable.setText("Açık");
        btn_enable.setEnabled(false);
        btn_view_paired.setEnabled(false);
        btn_scan.setEnabled(false);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    showToast("Açık");
                    showEnabled();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                /*Tarama işlemi bittiyse listeye eklediğimiz cihazları göstermek için DeviceListActivity sınıfına gönderiyoruz.*/
                mProgressDlg.dismiss();
                Intent newIntent = new Intent(btsettings.this, DeviceListActivity.class);
                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                startActivity(newIntent);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
                if (ActivityCompat.checkSelfPermission(btsettings.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                showToast("Bulunan cihazlar " + device.getName());
            }
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /*Geri tuşuna bastığımızda bu class ta finish ediyoruz ve main classına gidiyoruz.*/
            startActivity(new Intent(btsettings.this, main.class));
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
