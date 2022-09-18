package com.paj.pajbustelpo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //work on above oreo only
        hideSystemUI();
        setContentView(R.layout.activity_splash);

        TextView textView = findViewById(R.id.footer_paj);
        textView.setText("Hak Cipta 2022 © Perbadanan Pengangkutan Awam Johor");

        goToNextActivity();
    }

    private void goToNextActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //proceed to next activity
                Intent myIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(myIntent);
            }
        }, 1000);
    }

    public void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}