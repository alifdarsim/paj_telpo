package com.paj.pajbustelpo.utils;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.paj.pajbustelpo.Helper;
import com.paj.pajbustelpo.R;
import com.paj.pajbustelpo.ScannedCard;
import com.paj.pajbustelpo.activities.MainActivity;

import java.util.Objects;

public class ReaderHelper {


    /**
     * Trigger when user is tapping or scanning the card or use the QR code
     */
    public static int ScanningQR(MainActivity mainActivity, int tapping_type, String user_id) {
        String username = mainActivity.db.getUserName("qr", user_id);
        boolean isActive = mainActivity.db.isUserActive("qr", user_id);
        if (!isActive) {
            DisplayError(mainActivity, "PEMILIK DISENARAI \nHITAM");
            return -1;
        }
        else {
            return ProcessScanning(mainActivity, null, user_id, tapping_type, username);
        }
    }

    // selamat datang
    // name:
    // jenis:
    // masa:
    public static void ScanningCard(MainActivity mainActivity, int tapping_type, String card_uuid) {
        if (tapping_type == TapType.KMJ){
            String username = mainActivity.db.getUserName("kmj", card_uuid);
            if (Objects.equals(username, null)) DisplayError(mainActivity, "KAD TIDAK \nBERDAFTAR");
            else {
                boolean isActive = mainActivity.db.isUserActive("kmj", card_uuid);
                if (!isActive) DisplayError(mainActivity, "KAD DISENARAI \nHITAM");
                else {
                    ProcessScanning(mainActivity, card_uuid, null, tapping_type, username);
                }
            }
        }
        else {
            ProcessScanning(mainActivity, card_uuid, null, tapping_type, "");
        }
    }

    public static int ProcessScanning(MainActivity mainActivity, String str_uid, String user_id, int tapping_type, String username){
        TapType.currentCard = tapping_type;
        mainActivity.logger.writeToLogger("\uD83D\uDCB3 A card is tapped ID: " + str_uid, "yellow");
        int isUserEnter;

        if (tapping_type == TapType.CODE) {
            isUserEnter = mainActivity.userFlowTracker.userTap(user_id);
        }
        else {
            isUserEnter = mainActivity.userFlowTracker.userTap(str_uid);
        }

        String card_type;
        if (tapping_type == TapType.MYKAD) card_type = "MYKAD";
        else if (tapping_type == TapType.KMJ) card_type = "KMJ";
        else if (tapping_type == TapType.CODE) card_type = "QR CODE";
        else card_type = "";

        if (isUserEnter == UserFlowTracker.UserTapState.USER_TAP_DOUBLE){
            mainActivity.logger.writeToLogger("Repeating user tap. Will ignore this data", "yellow");
            showScanResult(mainActivity, scanCase.REPEATING, "SILA MASUK", username,  card_type,  Helper.getDateTimeString());
            return scanCase.REPEATING;
        }
        else if (isUserEnter == UserFlowTracker.UserTapState.USER_TAP_IN){
            mainActivity.logger.writeToLogger("User enter the bus", "yellow");
            mainActivity.db.insertRidershipData(str_uid, user_id ,Helper.getDateTimeString(), tapping_type, isUserEnter, mainActivity.current_Lat, mainActivity.current_Lng);
            mainActivity.logger.writeToLogger("Success storing ridership data | Lat: " + mainActivity.current_Lat + " | Lng: " + mainActivity.current_Lng + " | Time: " + Helper.getDateTimeString(), "green");
            ReaderHelper.DisplaySuccess(mainActivity, "SELAMAT\nDATANG", username, card_type, Helper.getDateTimeString());
            return scanCase.SUCCESS;
        }
        else if (isUserEnter == UserFlowTracker.UserTapState.USER_TAP_OUT){
            mainActivity.logger.writeToLogger("User exit the bus", "yellow");
            mainActivity.db.insertRidershipData(str_uid, user_id ,Helper.getDateTimeString(), tapping_type, isUserEnter, mainActivity.current_Lat, mainActivity.current_Lng);
            mainActivity.logger.writeToLogger("Success storing ridership data | Lat: " + mainActivity.current_Lat + " | Lng: " + mainActivity.current_Lng + " | Time: " + Helper.getDateTimeString(), "green");
            ReaderHelper.DisplaySuccess(mainActivity, "TERIMA\nKASIH", username, card_type, Helper.getDateTimeString());
            return scanCase.SUCCESS;
        }
        else{
            mainActivity.logger.writeToLogger("User is not registered", "yellow");
            ReaderHelper.DisplayError(mainActivity, "TIDAK \nBERDAFTAR");
            return scanCase.ERROR;
        }
    }

    public static void DisplayError(MainActivity mainActivity, String textUpper) {
        mainActivity.result_image.setImageResource(R.drawable.error);
        showScanResult(mainActivity, scanCase.ERROR, textUpper, "", "", Helper.getTimeNowScan());
    }

    public static void DisplaySuccess(MainActivity mainActivity, String textUpper, String textLower1, String textLower2, String textLower3) {
        mainActivity.result_image.setImageResource(R.drawable.check);
        showScanResult(mainActivity, scanCase.SUCCESS, textUpper, textLower1, textLower2, textLower3);
    }

    public static void showScanResult(MainActivity mainActivity, int cases, String textUpper, String textLower1, String textLower2, String textLower3) {
        int color;
        int visibility;
        MediaPlayer sound;

        if (cases == scanCase.ERROR ) {
            color = R.color.md_red_500;
            visibility = View.GONE;
            sound = mainActivity.wrongSound;
        }
        else if (cases == scanCase.REPEATING){
            color = R.color.md_green_700;
            visibility = View.VISIBLE;
            sound = mainActivity.correctSound;
        }
        else {
            color = R.color.md_green_500;
            visibility = View.VISIBLE;
            sound = mainActivity.correctSound;
        }

        ShowScreenResult(mainActivity, textUpper, textLower1, textLower2, textLower3, color, visibility, sound);
        mainActivity.scanBackground_result.bringToFront();
        new Handler().postDelayed(()->{
            mainActivity.scanBackground_idle.bringToFront();
            mainActivity.footerBackground.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.md_blue_700));
            mainActivity.scannerView.setVisibility(View.VISIBLE);

        }, 2000);
    }

    @SuppressLint("SetTextI18n")
    public static void ShowScreenResult(MainActivity mainActivity, String textUpper, String textLower, String textLower2, String textLower3, int color, int visibility, MediaPlayer sound) {
        mainActivity.scanBackground_result.setCardBackgroundColor(ContextCompat.getColor(mainActivity, color));
        mainActivity.footerBackground.setBackgroundColor(ContextCompat.getColor(mainActivity, color));
        mainActivity.textScan.setText(textUpper);
        mainActivity.textScanBelow1.setVisibility(visibility);
        if (TapType.currentCard == TapType.MYKAD) mainActivity.textScanBelow1.setVisibility(View.GONE);
        mainActivity.textScanBelow2.setVisibility(visibility);
        mainActivity.textScanBelow1.setText("NAMA: " + textLower.toUpperCase());
        mainActivity.textScanBelow2.setText("JENIS KAD: " + textLower2.toUpperCase());
        mainActivity.textScanBelow3.setText("MASA: " + textLower3.toUpperCase());
        mainActivity.scannerView.setVisibility(View.INVISIBLE);
        if (sound != null) sound.start();
    }

    public static class TapType {
        public static final int NULL = 0;
        public static final int MYKAD = 2;
        public static final int KMJ = 3;
        public static final int CODE = 4;
        public static int currentCard = 0;
    }


    // create class of case type
    public static class scanCase {
        public static final int SUCCESS = 1;
        public static final int ERROR = 2;
        public static final int REPEATING = 3;
    }

}
