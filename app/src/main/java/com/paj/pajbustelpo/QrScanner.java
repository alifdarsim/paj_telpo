package com.paj.pajbustelpo;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.ScanMode;
import com.paj.pajbustelpo.activities.MainActivity;
import com.paj.pajbustelpo.model.HttpResponse;
import com.paj.pajbustelpo.utils.HttpUtil;
import com.paj.pajbustelpo.utils.ReaderHelper;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class QrScanner {

    private static final String TAG = "QrScanner";
    DatabaseHelper db;
    MainActivity mainActivity;
    CodeScannerView scannerView;
    private CodeScanner mCodeScanner;
    private boolean scanActive = false;

    public QrScanner(MainActivity mainActivity, CodeScannerView scannerView) {
        this.db = mainActivity.db;
        this.mainActivity = mainActivity;
        this.scannerView = scannerView;
    }

    public void toggle() {
        if (scanActive) {
            stop();
            scanActive = false;
            mainActivity.btn_qr_code.setText("ACTIVATE QR CODE");
        } else {
            start();
            scanActive = true;
            mainActivity.btn_qr_code.setText("DEACTIVE QR CODE");
        }
    }

    public void stop() {
        mCodeScanner.releaseResources();
        scannerView.setVisibility(View.INVISIBLE);
    }

    public void start() {
        mCodeScanner.startPreview();
        scannerView.setVisibility(View.VISIBLE);
        scannerView.bringToFront();
    }

    public void init() {
        mCodeScanner = new CodeScanner(mainActivity, scannerView);

        mCodeScanner.setCamera(CodeScanner.CAMERA_FRONT);
        mCodeScanner.setFormats(CodeScanner.ALL_FORMATS);
        mCodeScanner.setAutoFocusEnabled(true);
        mCodeScanner.setScanMode(ScanMode.SINGLE);

        mCodeScanner.setDecodeCallback(result -> mainActivity.runOnUiThread(() -> {
            String qrCodeText = result.getText();
            mainActivity.logger.writeToLogger("Qr Detected: " + qrCodeText, "yellow");

            try{
                String secretText = SecretDecoder.decodeSecretText(qrCodeText);
                DataComponent data = new DataComponent(secretText);
                String userId = String.valueOf(data.getUserId());
                Log.e(TAG, "secretText: " + secretText);
                Log.e(TAG, "UserId: " + userId);

                Calendar secretDateTime = data.getDateTime();
                Calendar deviceDateTime = Calendar.getInstance();

                long timeDifferenceInMillis = secretDateTime.getTimeInMillis() - deviceDateTime.getTimeInMillis();
                long minutesDifference = timeDifferenceInMillis / (60 * 1000);
                if (minutesDifference >= -5 && minutesDifference <= 5) {
                    //enter the telpo
                    ReaderHelper.checkCard(mainActivity, qrCodeText, ReaderHelper.TapType.CODE, true);

                    //let the server know that the user boarding
                    notifyServer(qrCodeText, userId, Helper.getDateTimeString());

                } else {
                    ReaderHelper.displayError(mainActivity, "KOD TAMAT TEMPOH");
                }
            } catch (Exception e){
                ReaderHelper.displayError(mainActivity, "KOD TIDAK SAH");
            }

            //wait 2 second after scan, then start scan again
            Handler handler = new Handler();
            handler.postDelayed(() -> mCodeScanner.startPreview(), 1000);
        }));
    }

    public void notifyServer(String code, String user_id, String scan_time) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("bus", mainActivity.bus_plate);
        jsonObject.put("user_id", user_id);
        jsonObject.put("scan_time", scan_time);

        mainActivity.logger.writeToLogger("\uD83D\uDFE1 Notify server a user is boarding...", "yellow");
        HttpUtil httpUtil = new HttpUtil("code/boarding", jsonObject);
        httpUtil.post(response -> mainActivity.runOnUiThread(() -> {
            mainActivity.logger.writeToLogger("\uD83D\uDFE2 Notify server success. Response: " + response, "green");
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<HttpResponse> jsonAdapter = moshi.adapter(HttpResponse.class);
            try {
                HttpResponse post = jsonAdapter.fromJson(response);
                assert post != null;
                Log.e("111111", post + "," + response);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("222222", "failleeeddd");
                mainActivity.logger.writeToLogger("\uD83D\uDD34 Notify user boarding fail", "red");
            }

        }));
    }

    public static class SecretDecoder {
        public static String decodeSecretText(String encodedText) {
            // Extract the key components
            int key1 = Integer.parseInt(encodedText.substring(12, 13));
            int key2 = Integer.parseInt(encodedText.substring(9, 10));
            int key3 = Integer.parseInt(encodedText.substring(6, 7));
            int key4 = Integer.parseInt(encodedText.substring(3, 4));
            int key5 = Integer.parseInt(encodedText.substring(0, 1));

            // Extract the date time components
            String encodedMonth = encodedText.substring(1, 3);
            String encodedDate = encodedText.substring(4, 6);
            String encodedHour = encodedText.substring(7, 9);
            String encodedMin = encodedText.substring(10, 12);
            String encodedUserId = encodedText.substring(13);

            // Perform decoding operations on each component
            String month = decodeComponent(encodedMonth, key1);
            String date = decodeComponent(encodedDate, key2);
            String hour = decodeComponent(encodedHour, key3);
            String minute = decodeComponent(encodedMin, key4);
            String userId = decodeComponentUser(encodedUserId, key5);

            // Combine the decoded components to reconstruct the secret text
            String secretText = month + date + hour + minute + userId;

            return secretText;
        }

        private static String decodeComponent(String encodedComponent, int randomNum) {
            int portion = Integer.parseInt(encodedComponent) - randomNum * 4;
            String decodedComponent = String.valueOf(portion);
            if (decodedComponent.length() == 1) {
                decodedComponent = "0" + decodedComponent;
            }
            return decodedComponent;
        }

        private static String decodeComponentUser(String encodedComponent, int randomNum) {
            int portion = Integer.parseInt(encodedComponent) / randomNum;
            String decodedComponent = String.valueOf(portion);
            if (decodedComponent.length() == 1) {
                decodedComponent = "0" + decodedComponent;
            }
            return decodedComponent;
        }
    }

    public static class DataComponent {
        private Calendar dateTime;
        private int userId;

        public DataComponent(String text) {
            setComponents(text);
        }

        public void setComponents(String text) {
            int month = Integer.parseInt(text.substring(0, 2));
            int date = Integer.parseInt(text.substring(2, 4));
            int hour = Integer.parseInt(text.substring(4, 6));
            int minute = Integer.parseInt(text.substring(6, 8));
            int userId = Integer.parseInt(text.substring(8));

            int currentYear = Calendar.getInstance().get(Calendar.YEAR);

            this.dateTime = new GregorianCalendar(currentYear, month - 1, date, hour, minute);
            this.userId = userId;
        }

        public Calendar getDateTime() {
            return dateTime;
        }

        public int getUserId() {
            return userId;
        }

    }

}
