package com.paj.pajbustelpo.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.budiyev.android.codescanner.CodeScannerView;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.paj.pajbustelpo.BuildConfig;
import com.paj.pajbustelpo.DatabaseHelper;
import com.paj.pajbustelpo.Helper;
import com.paj.pajbustelpo.QrScanner;
import com.paj.pajbustelpo.R;
import com.paj.pajbustelpo.task.IntervalTask;
import com.paj.pajbustelpo.task.LocationTask;
import com.paj.pajbustelpo.task.ReaderTask;
import com.paj.pajbustelpo.task.UpdateTask;
import com.paj.pajbustelpo.utils.LoggerHelper;
import com.paj.pajbustelpo.utils.UserFlowTracker;

public class MainActivity extends AppCompatActivity {

    public boolean isLogging = false;
    public DatabaseHelper db;
    public LoggerHelper logger;
    public QrScanner codeScanner;
    public CodeScannerView scannerView;
    public UserFlowTracker userFlowTracker;

    public ImageView result_image, connected_bar;
    public TextView text_log;
    public double current_Lat, current_Lng;
    public String current_bearing, current_speed, current_time;
    public CardView scanBackground_idle, scanBackground_result;
    public RelativeLayout footerBackground;
    public TextView textScan, textScanBelow1, textScanBelow2, textScanBelow3;
    public MediaPlayer startingSound, correctSound, wrongSound;
    public String bus_plate;
    public Button btn_qr_code, btn_reset_bus, btn_volume, btn_close_logger, btn_check_update, btn_clear_log;
    RelativeLayout logger_panel, main_panel;

    @SuppressLint({"HandlerLeak", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup the fullscreen and set fullscreen listener
        Helper.hideSystemUI(MainActivity.this);
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0)
                Helper.hideSystemUI(MainActivity.this);
        });

        //Set mainActivity view
        setContentView(R.layout.activity_main);

        //Get extras from previous activity
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        bus_plate = pref.getString("bus_plate", "UTM9999");

        //Init database sqlite, Logger
        db = new DatabaseHelper(this);
        logger = new LoggerHelper(this);

        //insert into activated table
        db.insertActivated();

        //init startup script
        initUI();
        startingSound.start();

        //Location services always run in different thread, so no need to create new threading here
        LocationTask location = new LocationTask(this, db);
        location.startRequestLocation(this, (latitude, longitude, speed, bearing, time, accuracy, distance) -> {
            //do something in UI thread if needed
            //this value current latitude and longitude can be wrong no gps called at that particular time
            //but in fused location scenario this value is acceptable
            current_Lat = latitude;
            current_Lng = longitude;
            current_speed = String.format("%.0f", speed);
            current_bearing = String.format("%.0f", bearing);
            current_time = Helper.getDateTimeString();
        });

        //Reading unsend data from database every 10 second
        Runnable runnable = new IntervalTask(this);
        Thread thread = new Thread(runnable);
        thread.start();

        //Activate the NFC reading
        ReaderTask readerTask = new ReaderTask(this);
        readerTask.start();

        //Init the QR object
        codeScanner = new QrScanner(this, scannerView);
        codeScanner.init();
        scannerView.setVisibility(View.GONE);
        codeScanner.toggle();

        //Init the user flow tracker
        userFlowTracker = new UserFlowTracker(MainActivity.this);
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        result_image = findViewById(R.id.result_image);
        connected_bar = findViewById(R.id.connected_bar);
        correctSound = MediaPlayer.create(this, R.raw.correct);
        wrongSound = MediaPlayer.create(this, R.raw.wrong);
        startingSound = MediaPlayer.create(this, R.raw.tingtingting);
        scannerView = findViewById(R.id.scanner_view);

        text_log = findViewById(R.id.text_logger);
        text_log.setMovementMethod(new ScrollingMovementMethod());

        btn_clear_log = findViewById(R.id.btn_clear_log);
        btn_clear_log.setOnClickListener(view -> {
            logger.textSpan = "";
            text_log.setText("");
        });

        btn_check_update = findViewById(R.id.btn_check_update);
        btn_check_update.setOnClickListener(view -> {
            UpdateTask updateTask = new UpdateTask(MainActivity.this);
            updateTask.checkUpdate();
        });

        btn_qr_code = findViewById(R.id.btn_qr_code);
        btn_qr_code.setOnClickListener(view -> {
            codeScanner.toggle();
        });

        TextView build_num = findViewById(R.id.build_number);
        build_num.setText("version: " + BuildConfig.VERSION_NAME);

        logger_panel = findViewById(R.id.logger_panel);
        main_panel = findViewById(R.id.main_panel);

        scanBackground_idle = findViewById(R.id.card_scan_background_idle);
        scanBackground_result = findViewById(R.id.card_scan_background_result);
        footerBackground = findViewById(R.id.main_footer);
        textScan = findViewById(R.id.text_scan);
        textScanBelow1 = findViewById(R.id.text_scan_footer1);
        textScanBelow2 = findViewById(R.id.text_scan_footer2);
        textScanBelow3 = findViewById(R.id.text_scan_footer3);
        textScan.setText("SILA IMBAS\nMYKAD ANDA");

        btn_volume = findViewById(R.id.btn_volume);
        btn_volume.setOnClickListener(view -> {
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        });

        btn_reset_bus = findViewById(R.id.btn_reset_bus);
        btn_reset_bus.setOnClickListener(view -> {

            // Inside your method, when you want to show the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_password, null);
            EditText passwordEditText = dialogView.findViewById(R.id.passwordEditText);
            passwordEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

            builder.setView(dialogView)
                    .setTitle("Enter Password")
                    .setPositiveButton("OK", (dialog, which) -> {
                        String enteredPassword = passwordEditText.getText().toString().trim();
                        if (enteredPassword.equals("202320")) {
                            new MaterialDialog.Builder(this)
                                    .title("Input Bus Plate")
                                    .content("Kindly set this bus plate number to continue. (Must be Uppercase)")
                                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
                                    .input("Eg: JTV6100", "", (_dialog, input) -> {
                                        SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();
                                        editor.putString("bus_plate", input.toString());
                                        editor.apply();
                                    })
                                    .onPositive((_dialog, _which) -> {
                                        ProcessPhoenix.triggerRebirth(getApplicationContext());
                                    })
                                    .show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid password", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Dialog canceled, do nothing or handle as needed
                    });

            AlertDialog dialog = builder.create();
            dialog.show();



        });

        TextView bus_plate_txt = findViewById(R.id.text_bus);
        bus_plate_txt.setText(bus_plate);

        ImageView paj_logo = findViewById(R.id.paj_logo);
        paj_logo.setOnLongClickListener(view -> {
            logger_panel.bringToFront();
            logger_panel.setVisibility(View.VISIBLE);
            isLogging = true;
            return true;
        });

        btn_close_logger = findViewById(R.id.btn_close_logger);
        btn_close_logger.setOnClickListener(view -> {
            main_panel.bringToFront();
            scannerView.bringToFront();
            logger_panel.setVisibility(View.GONE);
            logger.textSpan = "";
            isLogging = false;
        });

        logger_panel.setVisibility(View.GONE);
    }

}