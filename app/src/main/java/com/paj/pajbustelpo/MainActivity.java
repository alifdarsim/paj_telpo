package com.paj.pajbustelpo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public DatabaseHelper db;
    public LoggerHelper logger;
    private TextView text_lat, text_lng, text_spd, text_deg, text_epo, text_acc, text_bus;
    public TextView text_log;
    public ScrollView scrollView;
    private Button btn_clear_log;
    private ToggleButton log_button;
    RelativeLayout logger_panel, main_panel;
    private double current_Lat, current_Lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //make app go full screen and hide notch if exist
        getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        Helper.hideSystemUI(MainActivity.this);

        //init database sqlite, Logger
        db = new DatabaseHelper(this);
        logger = new LoggerHelper(this);
        initGUI();

        //Location services always run in different so no need to create new threading here
        LocationHelper location = new LocationHelper(this, db);
        location.startRequestLocation(this, (latitude, longitude, speed, bearing, time, accuracy, distance) -> {
            //do something in UI thread if needed
            current_Lat = latitude;
            current_Lng = longitude;
        });


        //run task of reading database every x second in different thread
        Runnable runnable = new TaskIntervalRequest(this, db);
        Thread thread = new Thread(runnable);
        thread.start();

        //run whatever

    }


    private void initGUI(){
        text_log = findViewById(R.id.text_logger);
        scrollView = findViewById(R.id.logger_content);
        btn_clear_log = findViewById(R.id.btn_clear_log);
        btn_clear_log.setOnClickListener(view -> {
            text_log.setText("");
        });

        logger_panel = findViewById(R.id.logger_panel);
        main_panel = findViewById(R.id.main_panel);

        log_button = findViewById(R.id.panel_button);
        log_button.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) logger_panel.bringToFront();
            else main_panel.bringToFront();
        });

        Button simulate_button = findViewById(R.id.btn_simulate_card);
        simulate_button.setOnClickListener(view -> {
            //detect card uuid = dsad8aysd89a
            String mykad_uid = "0418C582F1";
            UserInfo userInfo = db.checkUserInfo(mykad_uid);
            if (!userInfo.isUuidExist()) {
                logger.writeToLogger("Card uid not exist", "red");
                return;
            }
            logger.writeToLogger("Found User | Name: " + userInfo.getUsername() + " | Expired: " + userInfo.getExpired() + " | Active: " + userInfo.getActive(), "green");
            if (Helper.stringToDate(userInfo.getExpired()).before(Calendar.getInstance().getTime())) {
                logger.writeToLogger("This card has expired", "red");
                return;
            }
            if (Objects.equals(userInfo.getActive(), "0")) {
                logger.writeToLogger("This card not active", "red");
                return;
            }

            //this value current latitude and longitude can be wrong no gps called at that particular time
            //but in fused location scenario this value is acceptable
            db.insertRidershipData(userInfo.getUuid(), Helper.getDateTimeString(), "Card (Simulate)", current_Lat, current_Lng);
            logger.writeToLogger("Success storing ridership data | Lat: " + current_Lat + " | Lng: " + current_Lng + " | Time: " + Helper.getDateTimeString(), "green");

        });

    }






}