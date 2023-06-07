package com.paj.pajbustelpo.utils;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.paj.pajbustelpo.Helper;
import com.paj.pajbustelpo.R;
import com.paj.pajbustelpo.activities.MainActivity;

public class ReaderHelper {

    public static void checkCard(MainActivity mainActivity, String str_uid, int tapping_type, boolean isSuccess) {
        if (!isSuccess) {
            String textDisplay = "";
            if (tapping_type == TapType.CODE) textDisplay = str_uid;
            else textDisplay = "KAD TIADA SAH DIGUNAKAN";
            showScanResult(mainActivity, false, textDisplay, Helper.getTimeNowScan());
            return;
        }

        mainActivity.logger.writeToLogger("\uD83D\uDCB3 A card is tapped ID: " + str_uid, "yellow");
        int isUserEnter = mainActivity.userFlowTracker.userTap(str_uid);
        if (isUserEnter != -1) {
            mainActivity.db.insertRidershipData(str_uid, Helper.getDateTimeString(), tapping_type, isUserEnter, mainActivity.current_Lat, mainActivity.current_Lng);
            mainActivity.logger.writeToLogger("Success storing ridership data | Lat: " + mainActivity.current_Lat + " | Lng: " + mainActivity.current_Lng + " | Time: " + Helper.getDateTimeString(), "green");
            String displayText = (isUserEnter == 1) ? "SELAMAT DATANG" : "TERIMA KASIH";
            showScanResult(mainActivity, true, displayText, Helper.getTimeNowScan());
        } else {
            mainActivity.logger.writeToLogger("Repeating user tap. Will ignore this data", "yellow");
            showScanResult(mainActivity, true, "SELAMAT DATANG", Helper.getTimeNowScan() + " (Idle)");
        }
    }

    public static void displayError(MainActivity mainActivity, String text) {
        showScanResult(mainActivity, false, text, Helper.getTimeNowScan());
    }

    public static void showScanResult(MainActivity mainActivity, boolean isSuccess, String textUpper, String textLower) {
        int color = R.color.md_green_500;
        int visibility = View.VISIBLE;
        MediaPlayer sound = mainActivity.correctSound;
        if (!isSuccess) {
            color = R.color.md_red_500;
            visibility = View.GONE;
            sound = mainActivity.wrongSound;
        }
        ShowScreenResult(mainActivity, textUpper, textLower, color, visibility, sound);
        new Handler().postDelayed(() -> {
            ShowScreenResult(mainActivity, "SILA IMBAS\nMYKAD ANDA", textLower, R.color.md_blue_500, View.GONE, null);
        }, 2000);
    }

    public static void ShowScreenResult(MainActivity mainActivity, String textUpper, String textLower, int color, int visibility, MediaPlayer sound) {
        mainActivity.scanBackground.setCardBackgroundColor(ContextCompat.getColor(mainActivity, color));
        mainActivity.footerBackground.setBackgroundColor(ContextCompat.getColor(mainActivity, color));
        mainActivity.textScan.setText(textUpper);
        mainActivity.textScanBelow.setVisibility(visibility);
        mainActivity.textScanBelow.setText(textLower);
        if (sound != null) sound.start();
    }

    public static class TapType {
        public static final int CARD = 2;
        public static final int CODE = 3;
        public static final int SIMULATE = 4;
    }

}
