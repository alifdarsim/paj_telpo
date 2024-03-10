package com.paj.pajbustelpo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import androidx.cardview.widget.CardView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.paj.pajbustelpo.BuildConfig;
import com.paj.pajbustelpo.R;
import com.paj.pajbustelpo.activities.MainActivity;
import com.paj.pajbustelpo.task.UpdateTask;

public class UIInitializer {

    public String bus_plate;

    public TextView text_log;
    public double current_Lat, current_Lng;
    public CardView scanBackground;
    public RelativeLayout footerBackground;
    public TextView textScan, textScanBelow;
    public MediaPlayer correctSound, wrongSound;
    public Button btn_qr_code;

    public void initialize(MainActivity mainActivity) {

    }
}
