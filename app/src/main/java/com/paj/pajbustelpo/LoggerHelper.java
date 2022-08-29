package com.paj.pajbustelpo;

import android.annotation.SuppressLint;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

public class LoggerHelper {

    MainActivity mainActivity;
    public LoggerHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @SuppressLint("SetTextI18n")
    public void writeToLogger(String textToAppend, String color){
        String text = Html.toHtml(new SpannableString(mainActivity.text_log.getText()), Html.FROM_HTML_MODE_LEGACY);
        try{
            text= replaceLast(text,"<p dir=\"ltr\">", "");
            text= replaceLast(text,"</p>", "");
        }catch (Exception e) {}
        String timeLogTxt = Helper.getTimeNow() + "  ";
        textToAppend = "<font color='"+color+"'>"+textToAppend+"</font><br>";
        if (text.length() > 20000){
//          removeLastLine(mainActivity.text_log);
            mainActivity.text_log.setText("");
        }
        mainActivity.text_log.setText(Html.fromHtml(text+ timeLogTxt + textToAppend,  Html.FROM_HTML_MODE_LEGACY));
        mainActivity.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    public void removeLastLine(TextView textView)
    {
        String text = Html.toHtml(new SpannableString(mainActivity.text_log.getText()), Html.FROM_HTML_MODE_LEGACY);
        String temp = textView.getText().toString();
        Log.e("data", text);
        int indexOf = text.indexOf("<br>");
        textView.setText(text.substring(indexOf+1));
    }

    public String replaceLast(String yourString, String first,String second)
    {
        StringBuilder b = new StringBuilder(yourString);
        b.replace(yourString.lastIndexOf(first), yourString.lastIndexOf(first)+first.length(),second );
        return b.toString();
    }
}
