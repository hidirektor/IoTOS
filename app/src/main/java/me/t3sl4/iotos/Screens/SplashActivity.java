package me.t3sl4.iotos.Screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import me.t3sl4.iotos.R;
import me.t3sl4.iotos.Screens.Device.DeviceListScreen;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        redirectToMainActivity();
    }

    private void redirectToMainActivity() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, DeviceListScreen.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}