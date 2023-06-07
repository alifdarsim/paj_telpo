package com.paj.pajbustelpo.utils;

import android.annotation.SuppressLint;
import android.text.Html;

import com.paj.pajbustelpo.Helper;
import com.paj.pajbustelpo.activities.MainActivity;

import java.util.Objects;

public class LoggerHelper {

    MainActivity mainActivity;
    public String textSpan = "";
    public LoggerHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @SuppressLint("SetTextI18n")
    public void writeToLogger(String textToAppend, String colorString){

        if (!mainActivity.isLogging) {
            textSpan = "";
            return;
        }

        if (textSpan.length() > 15000){
            int brPos = textSpan.indexOf("<br>", 0);
            textSpan = textSpan.substring(brPos+4);
        }

        String timeLogTxt = "<span style=\"color:gray;\">" + Helper.getTimeNow()+" </span>";
        textToAppend = "<span style=\"color:"+colorString+";\">"+textToAppend+"</span>";

        String text = "";
        if (Objects.equals(textSpan, "")) text = timeLogTxt + textToAppend;
        else text = textSpan + "<br>" + timeLogTxt + textToAppend;
        text = text.trim();
        textSpan = text;

        String finalText = text;
        mainActivity.runOnUiThread(() -> mainActivity.text_log.setText(Html.fromHtml(finalText,  Html.FROM_HTML_MODE_LEGACY)));

    }

    public String replaceLast(String yourString, String first,String second)
    {
        StringBuilder b = new StringBuilder(yourString);
        b.replace(yourString.lastIndexOf(first), yourString.lastIndexOf(first)+first.length(),second );
        return b.toString();
    }

}
