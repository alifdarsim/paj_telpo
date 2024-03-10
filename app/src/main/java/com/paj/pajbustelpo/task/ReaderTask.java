package com.paj.pajbustelpo.task;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.paj.pajbustelpo.ScannedCard;
import com.paj.pajbustelpo.activities.MainActivity;
import com.paj.pajbustelpo.utils.DeviceUtils;
import com.paj.pajbustelpo.utils.ReaderHelper;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.nfc.Nfc;

import java.util.Timer;
import java.util.TimerTask;

public class ReaderTask {

    private final String TAG = "ReaderTask";
    private final int CHECK_NFC_TIMEOUT = 1;
    private final int SHOW_NFC_DATA = 2;
    Nfc nfc;
    Thread readThread;
    Handler handler;
    MainActivity mainActivity;
    long time1, time2;
    private boolean stopReading = false;

    public ReaderTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        nfc = new Nfc(mainActivity);
    }

    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString().trim();
    }

    public static boolean scanCard(String fullbyte){
        if (fullbyte.contains("8804")) {
            int startIndex = fullbyte.indexOf("8804") + 4;
            ScannedCard.uuid = fullbyte.substring(startIndex, startIndex + 8);
            ScannedCard.type = ReaderHelper.TapType.MYKAD;
            return true;
        }
        if (fullbyte.contains("0804")) {
            int startIndex = fullbyte.indexOf("0804") + 4;
            ScannedCard.uuid = fullbyte.substring(startIndex, startIndex + 8);
            ScannedCard.type = ReaderHelper.TapType.MYKAD;
            return true;
        }
        else if (fullbyte.contains("44000007")) {
            int startIndex = fullbyte.indexOf("44000007") + 8;
            String uuid = fullbyte.substring(startIndex, startIndex + 14);
            ScannedCard.uuid = uuid;
            ScannedCard.type = ReaderHelper.TapType.KMJ;
            return true;
        } else return false;
    }

    @SuppressLint("HandlerLeak")
    public void start() {

        //if the device not telpo, then ignore reading using nfc
        if (!DeviceUtils.getDeviceInfo(mainActivity, DeviceUtils.DEVICE_HARDWARE_MODEL).contains("TPS530"))
            return;

        try {
            nfc.open();
        } catch (TelpoException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
            if (!stopReading) {
                readThread = new ReadThread();
                readThread.start();
            }
            }
        }, 2000, 500);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                stopReading = true;
                try {
                    switch (msg.what) {
                        case SHOW_NFC_DATA: {
                            byte[] byte_data = (byte[]) msg.obj;
                            if (byte_data[0] == 0x41) {
                                Log.e(TAG, "Full byte: " + byteArrayToHexString(byte_data));
                                byte[] bytes = {byte_data[0], byte_data[1], byte_data[2], byte_data[3], byte_data[4], byte_data[5], byte_data[6], byte_data[7], byte_data[8], byte_data[9], byte_data[10], byte_data[11], byte_data[12], byte_data[13]};
                                String fullbytestring = byteArrayToHexString(bytes);
                                mainActivity.logger.writeToLogger("Full Byte:" + fullbytestring, "green");
                                boolean success = scanCard(fullbytestring);
                                if (success)  ReaderHelper.ScanningCard(mainActivity, ScannedCard.type, ScannedCard.uuid);
                                else ReaderHelper.DisplayError(mainActivity, "KAD TIDAK\nSAH");

                            } else {
                                ReaderHelper.DisplayError(mainActivity, "KAD TIDAK\nSAH");
                            }
                            handler.postDelayed(() -> stopReading = false, 2000);
                        }
                        break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    ReaderHelper.DisplayError(mainActivity, "KAD TIDAK \nDIKENAL PASTI");
                    handler.postDelayed(() -> stopReading = false, 2000);
                }
            }
        };
    }

    private class ReadThread extends Thread {
        byte[] nfcData = null;

        @Override
        public void run() {
            try {
                time1 = System.currentTimeMillis();
                nfcData = nfc.activate(100);
                time2 = System.currentTimeMillis();
                if (null != nfcData) {
                    handler.sendMessage(handler.obtainMessage(SHOW_NFC_DATA, nfcData));
                } else {
                    handler.sendMessage(handler.obtainMessage(CHECK_NFC_TIMEOUT, null));
                }
            } catch (TelpoException e) {
//                Log.e("yw",e.toString());
//                e.printStackTrace();
            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e("yw  changemode",e.toString());
            }
        }
    }

}
