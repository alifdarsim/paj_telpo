package com.paj.pajbustelpo.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.paj.pajbustelpo.BuildConfig;
import com.paj.pajbustelpo.R;
import com.paj.pajbustelpo.model.HttpResponse;
import com.paj.pajbustelpo.task.LoginTask;
import com.paj.pajbustelpo.utils.HttpUtil;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;


public class SplashActivity extends AppCompatActivity {

    public static final String bus_plate = "";
    public static String TAG = "SplashActivity";
    private SplashActivity splashActivity;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        splashActivity = this;
        setContentView(R.layout.activity_splash);
//        startSystemAlertWindowPermission();
        TextView textView = findViewById(R.id.footer_paj);
        String versionName = BuildConfig.VERSION_NAME;
        textView.setText("Version: " + versionName + "\nHak Cipta 2022 - " + Calendar.getInstance().get(Calendar.YEAR) + " © Perbadanan Pengangkutan Awam Johor");

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String bus_plate = pref.getString("bus_plate", "UTM9999");

        nextActivity(bus_plate);
    }

    private void nextActivity(String bus) {
        new Handler().postDelayed(() -> {
            //proceed to next activity after a delay
            Intent myIntent = new Intent(SplashActivity.this, MainActivity.class);
            myIntent.putExtra("bus_plate", bus);
            SplashActivity.this.startActivity(myIntent);
        }, 1000);
    }

}