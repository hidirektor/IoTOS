package me.t3sl4.iotos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);

        redirectToMainActivity();
    }

    private void redirectToMainActivity() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, DeviceListScreen.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}