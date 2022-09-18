package com.paj.pajbustelpo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.budiyev.android.codescanner.CodeScannerView;
import com.paj.pajbustelpo.TelpoHelper.Telpo;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.nfc.Nfc;
import com.telpo.tps550.api.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public boolean isLogging = false;
    public DatabaseHelper db;
    public LoggerHelper logger;
    public TextView text_log;
    RelativeLayout logger_panel, main_panel;
    private double current_Lat, current_Lng;
    CardView scanBackground;
    TextView textScan, textScanBelow;
    Nfc nfc = new Nfc(this);
    CodeScannerView scannerView;
    public MediaPlayer correctSound, wrongSound;
    Thread readThread;
    Handler handler;
    boolean isShowOption = false;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //make app go full screen and hide notch if exist
        Helper.hideSystemUI(MainActivity.this);

        //init database sqlite, Logger
        db = new DatabaseHelper(this);
        logger = new LoggerHelper(this);

        //init startup script
        initGUI();
        correctSound = MediaPlayer.create(this, R.raw.correct);
        wrongSound = MediaPlayer.create(this, R.raw.wrong);

        //Function: call API to get latest userinfo
        UserSync userSync = new UserSync(this);
        userSync.startSync(isSuccess -> {
            //if user table success sync with server
            if (isSuccess){

            }
        });

        //Function: start QR Scanner
        //TODO: CHANGE TO OUTSIDE THREAD
        QrScanner codeScanner = new QrScanner(this, scannerView);
        codeScanner.startScan();

        //Location services always run in different so no need to create new threading here
        LocationHelper location = new LocationHelper(this, db);
        location.startRequestLocation(this, (latitude, longitude, speed, bearing, time, accuracy, distance) -> {
            //do something in UI thread if needed
            //this value current latitude and longitude can be wrong no gps called at that particular time
            //but in fused location scenario this value is acceptable
            current_Lat = latitude;
            current_Lng = longitude;
        });

        //run task of reading database every x second in different thread
        Runnable runnable = new TaskIntervalRequest(this);
        Thread thread = new Thread(runnable);
        thread.start();

        //thread: check device health info
        //run: every 1 hour
        Runnable deviceHealthCheck = new DeviceInfoThread(this, db);
        Thread deviceHealthThread = new Thread(deviceHealthCheck);
        deviceHealthThread.start();

        //if device is telpo, then open nfc and try to read card
        if (DeviceUtils.getDeviceInfo(this, DeviceUtils.DEVICE_HARDWARE_MODEL).contains("TPS530")) startReadNFC();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Exit disabled for this app.", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("HandlerLeak")
    private void startReadNFC(){

        try {
            nfc.open();
        } catch (TelpoException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                readThread = new ReadThread();
                readThread.start();
            }
        }, 2000, 500);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CHECK_NFC_TIMEOUT: {

                    }break;
                    case SHOW_NFC_DATA:{
                        byte[] uid_data = (byte[]) msg.obj;
                        if (uid_data[0] == 0x42) {
                            // TYPE B类（暂时只支持cpu卡）
                            byte[] atqb = new byte[uid_data[16]];
                            byte[] pupi = new byte[4];


                            System.arraycopy(uid_data, 17, atqb, 0, uid_data[16]);
                            System.arraycopy(uid_data, 29, pupi, 0, 4);


                        } else if (uid_data[0] == 0x41) {
                            // TYPE A类（CPU, M1）
                            byte[] atqa = new byte[2];
                            byte[] sak = new byte[1];
                            byte[] uid = new byte[uid_data[5]];

                            System.arraycopy(uid_data, 2, atqa, 0, 2);
                            System.arraycopy(uid_data, 4, sak, 0, 1);
                            System.arraycopy(uid_data, 6, uid, 0, uid_data[5]);

                            Log.e("yw"," "+atqa[0]+  "  "+atqa[1] + "  "+sak[0] );

                            String str_uid = StringUtil.toHexString(uid);
                            str_uid = str_uid.trim();
                            if (str_uid.length()>12) str_uid = str_uid.substring(0, 12);
//                            Toast.makeText(MainActivity.this, str_uid,
//                                    Toast.LENGTH_LONG).show();

                            User user = db.checkUserMyKad(str_uid);
                            checkUserInfo(user, Telpo.TapType.CARD);

                        } else if (uid_data[0] == 0x46){
                            //felica
                            byte[] idm=new byte[8];
                            byte [] pmm=new byte[8];
                            System.arraycopy(uid_data,33,idm,0,8);
                            System.arraycopy(uid_data,41,pmm,0,8);
                        }else {
//                            Log.e(TAG, "unknow type card!!");
                        }
                    }break;
                    default:break;
                }
            }
        };
    }

    private void initGUI(){
        scannerView = findViewById(R.id.scanner_view);

        text_log = findViewById(R.id.text_logger);
        text_log.setMovementMethod(new ScrollingMovementMethod());

        Button btn_clear_log = findViewById(R.id.btn_clear_log);
        btn_clear_log.setOnClickListener(view -> {
            logger.textSpan = "";
            text_log.setText("");
        });

        logger_panel = findViewById(R.id.logger_panel);
        main_panel = findViewById(R.id.main_panel);

        ToggleButton log_button = findViewById(R.id.panel_button);
        log_button.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                logger_panel.bringToFront();
                isLogging = true;
            }
            else {
                main_panel.bringToFront();
                logger.textSpan = "";
                isLogging = false;
            }
            scannerView.bringToFront();
        });

        scanBackground = findViewById(R.id.card_scan_background);
        textScan = findViewById(R.id.text_scan);
        textScanBelow = findViewById(R.id.text_scan_footer);
        textScan.setText("Sila Imbas Kad/QR");
        textScanBelow.setVisibility(View.GONE);

        Button simulate_button = findViewById(R.id.btn_simulate_card);
        simulate_button.setOnClickListener(view -> {
            String mykad_uid = "AA11AA11AA";
            User user = db.checkUserMyKad(mykad_uid);
            checkUserInfo(user, Telpo.TapType.SIMULATE);
        });

        Button test_enter = findViewById(R.id.test_enter);
        test_enter.setOnClickListener(view -> {
            String mykad_uid = "AA11AA11AA";
            User user = db.checkUserMyKad(mykad_uid);
            checkUserInfo(user, Telpo.TapType.SIMULATE);
        });

        TextView bus_plate = findViewById(R.id.text_bus);
        bus_plate.setText("JTV6100");

        test_enter.setVisibility(View.INVISIBLE);
        log_button.setVisibility(View.INVISIBLE);
        ImageView paj_logo = findViewById(R.id.paj_logo);
        paj_logo.setOnClickListener(view -> {
            if (isShowOption){
                test_enter.setVisibility(View.INVISIBLE);
                log_button.setVisibility(View.INVISIBLE);
                isShowOption = false;
            }
            else{
                test_enter.setVisibility(View.VISIBLE);
                log_button.setVisibility(View.VISIBLE);
                isShowOption = true;
            }
        });

        TextView day_week = findViewById(R.id.day_of_week);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        String dayOfTheWeek = sdf.format(new Date());
        if (dayOfTheWeek.equals("Monday")) dayOfTheWeek = "ISNIN | ";
        else if (dayOfTheWeek.equals("Tuesday")) dayOfTheWeek = "SELASA | ";
        else if (dayOfTheWeek.equals("Wednesday")) dayOfTheWeek = "RABU | ";
        else if (dayOfTheWeek.equals("Thursday")) dayOfTheWeek = "KHAMIS | ";
        else if (dayOfTheWeek.equals("Friday")) dayOfTheWeek = "JUMAAT | ";
        else if (dayOfTheWeek.equals("Saturday")) dayOfTheWeek = "SABTU | ";
        else if (dayOfTheWeek.equals("Sunday")) dayOfTheWeek = "AHAD | ";
        day_week.setText(dayOfTheWeek);

    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    public void checkUserInfo(User user, int tap_type){
        if (!user.isUuidExist()) {
            logger.writeToLogger("Card uid not exist", "red");
            showScanResult(false, "Tiada data didalam sistem", "");
            return;
        }
        logger.writeToLogger("Found User | Name: " + user.getUsername() + " | Expired: " + user.getExpired() + " | Active: " + user.isBlacklist(), "green");
        if (Helper.stringToDate(user.getExpired()).before(Calendar.getInstance().getTime())) {
            logger.writeToLogger("This card has expired", "red");
            showScanResult(false, "Kad tamat tempoh.\nSila perbaharui card anda", "");
            return;
        }
        if (Objects.equals(user.isBlacklist(), "1")) {
            logger.writeToLogger("This card not active", "red");
            showScanResult(false, "Kad telah disenari hitam", "");
            return;
        }
        if (db.isRidershipOut(user.getUuid(), current_Lat, current_Lng)) {
            logger.writeToLogger("User '" + user.getUuid() + "' is tapping OUT", "green");
            showScanResult(true, "TERIMA KASIH \uD83D\uDE04", user.getUsername() + "\n" + Helper.getTimeNowScan());
            db.insertRidershipData(user.getUuid(), Helper.getDateTimeString(), tap_type, Telpo.Tapping.OUT, current_Lat, current_Lng);
            return;
        }
        showScanResult(true, "SELAMAT DATANG", user.getUsername() + "\n" + Helper.getTimeNowScan());
        db.insertRidershipData(user.getUuid(), Helper.getDateTimeString(), tap_type, Telpo.Tapping.IN, current_Lat, current_Lng);
        logger.writeToLogger("Success storing ridership data | Lat: " + current_Lat + " | Lng: " + current_Lng + " | Time: " + Helper.getDateTimeString(), "green");
    }

    private void showScanResult(boolean isSuccess, String textUpper, String textLower){
        if (isSuccess) {
            scanBackground.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(),  R.color.md_green_500));
            textScan.setText(textUpper);
            textScanBelow.setVisibility(View.VISIBLE);
            textScanBelow.setText(textLower);
            correctSound.start();
        }
        else {
            scanBackground.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(),  R.color.md_red_500));
            textScan.setText(textUpper);
            textScanBelow.setText("");
            textScanBelow.setVisibility(View.GONE);
            wrongSound.start();
        }
        new Handler().postDelayed(() -> {
            scanBackground.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(),  R.color.md_blue_500));
            textScan.setText("Sila Imbas Kad/QR");
            textScanBelow.setText("");
            textScanBelow.setVisibility(View.GONE);
        }, 1750);
    }

    private final int CHECK_NFC_TIMEOUT = 1;
    private final int SHOW_NFC_DATA     = 2;
    long time1,time2;
    private class ReadThread extends Thread {
        byte[] nfcData = null;
        @Override
        public void run() {
            try {
                time1=System.currentTimeMillis();
                nfcData = nfc.activate(100); //
                time2=System.currentTimeMillis();
                Log.e("yw activate",(time2-time1)+"");
                if (null != nfcData) {
                    handler.sendMessage(handler.obtainMessage(SHOW_NFC_DATA, nfcData));
                } else {
                    handler.sendMessage(handler.obtainMessage(CHECK_NFC_TIMEOUT, null));
                }
            } catch (TelpoException e) {
                Log.e("yw",e.toString());
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("yw  changemode",e.toString());
            }
        }
    }



}