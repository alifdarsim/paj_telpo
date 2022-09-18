package com.paj.pajbustelpo;

import android.annotation.SuppressLint;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import java.util.Objects;
import java.util.function.LongFunction;

public class LoggerHelper {

    MainActivity mainActivity;
    public String textSpan = "";
    public LoggerHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @SuppressLint("SetTextI18n")
    public void writeToLogger(String textToAppend, String color){

        if (!mainActivity.isLogging) {
            textSpan = "";
            return;
        }

        if (textSpan.length() > 15000){
            int brPos = textSpan.indexOf("<br>", 0);
            textSpan = textSpan.substring(brPos+4);
        }

        String timeLogTxt = "<span style=\"color:gray;\">" + Helper.getTimeNow()+" </span>";
        textToAppend = "<span style=\"color:"+color+";\">"+textToAppend+"</span>";

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
