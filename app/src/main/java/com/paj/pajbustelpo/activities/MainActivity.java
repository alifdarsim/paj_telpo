package com.paj.pajbustelpo.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
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

    public TextView text_log;
    public double current_Lat, current_Lng;
    public CardView scanBackground;
    public RelativeLayout footerBackground;
    public TextView textScan, textScanBelow;
    public MediaPlayer correctSound, wrongSound;
    public String bus_plate;
    public Button btn_qr_code;
    RelativeLayout logger_panel, main_panel;

    @SuppressLint("HandlerLeak")
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
        Bundle extras = getIntent().getExtras();
        bus_plate = extras.getString("bus_plate");

        //Init database sqlite, Logger
        db = new DatabaseHelper(this);
        logger = new LoggerHelper(this);

        //init startup script
        initUI();

        //Location services always run in different thread, so no need to create new threading here
        LocationTask location = new LocationTask(this, db);
        location.startRequestLocation(this, (latitude, longitude, speed, bearing, time, accuracy, distance) -> {
            //do something in UI thread if needed
            //this value current latitude and longitude can be wrong no gps called at that particular time
            //but in fused location scenario this value is acceptable
            current_Lat = latitude;
            current_Lng = longitude;
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
        codeScanner.toggle();

        //Init the user flow tracker
        userFlowTracker = new UserFlowTracker(MainActivity.this);
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        correctSound = MediaPlayer.create(this, R.raw.correct);
        wrongSound = MediaPlayer.create(this, R.raw.wrong);
        scannerView = findViewById(R.id.scanner_view);

        text_log = findViewById(R.id.text_logger);
        text_log.setMovementMethod(new ScrollingMovementMethod());

        Button btn_clear_log = findViewById(R.id.btn_clear_log);
        btn_clear_log.setOnClickListener(view -> {
            logger.textSpan = "";
            text_log.setText("");
        });

        Button btn_check_update = findViewById(R.id.btn_check_update);
        btn_check_update.setOnClickListener(view -> {
            UpdateTask updateTask = new UpdateTask(MainActivity.this);
            updateTask.checkUpdate();
        });

        Button btn_close_logger = findViewById(R.id.btn_close_logger);
        btn_close_logger.setOnClickListener(view -> {
            main_panel.bringToFront();
            scannerView.bringToFront();
            logger.textSpan = "";
            isLogging = false;
        });

        btn_qr_code = findViewById(R.id.btn_qr_code);
        btn_qr_code.setOnClickListener(view -> {
            codeScanner.toggle();
        });

        TextView build_num = findViewById(R.id.build_number);
        build_num.setText("version: " + BuildConfig.VERSION_NAME);

        logger_panel = findViewById(R.id.logger_panel);
        main_panel = findViewById(R.id.main_panel);

        scanBackground = findViewById(R.id.card_scan_background);
        footerBackground = findViewById(R.id.main_footer);
        textScan = findViewById(R.id.text_scan);
        textScanBelow = findViewById(R.id.text_scan_footer);
        textScan.setText("SILA IMBAS\nMYKAD ANDA");
        textScanBelow.setVisibility(View.GONE);

        Button btn_volume = findViewById(R.id.btn_volume);
        btn_volume.setOnClickListener(view -> {
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        });

        Button btn_reset_bus = findViewById(R.id.btn_reset_bus);
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
        paj_logo.setOnClickListener(view -> {
            logger_panel.bringToFront();
            isLogging = true;
        });

    }

}