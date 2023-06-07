package com.paj.pajbustelpo.task;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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

//        Card Type	         ATQA  SAK	UID Length
//        Mifare Classic 1K	00 04	08	4 Bytes
//        Mifare Classic 4K	00 02	18	4 Bytes
//        Mifare Ultralight	00 44	00	7 Bytes

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

//                                byte[] sak = {byte_data[0]};
//                                String _sak = byteArrayToHexString(sak);
//                                Log.e(TAG, "sak: " + _sak);
//
//                                byte[] sak1 = {byte_data[1]};
//                                String _sak1 = byteArrayToHexString(sak1);
//                                Log.e(TAG, "sak: " + _sak1);
//
//                                byte[] sak2 = {byte_data[2]};
//                                String _sak2 = byteArrayToHexString(sak2);
//                                Log.e(TAG, "sak: " + _sak2);
//
//                                byte[] sak3 = {byte_data[3]};
//                                String _sak3 = byteArrayToHexString(sak3);
//                                Log.e(TAG, "sak: " + _sak3);
//
//                                byte[] sak4 = {byte_data[4]};
//                                String _sak4 = byteArrayToHexString(sak4);
//                                Log.e(TAG, "sak: " + _sak4);
//
//                                byte[] sak5 = {byte_data[5]};
//                                String _sak5 = byteArrayToHexString(sak5);
//                                Log.e(TAG, "sak: " + _sak5);

                                //expected to be a mifare classic card
                                byte[] bytes = {byte_data[0], byte_data[1], byte_data[2], byte_data[3], byte_data[4], byte_data[5], byte_data[6], byte_data[7], byte_data[8], byte_data[9], byte_data[10], byte_data[11], byte_data[12], byte_data[13]};
                                String str_uid = byteArrayToHexString(bytes);
                                String extractedString = str_uid.substring(10, 18).replaceAll("\\s+", "");
                                ReaderHelper.checkCard(mainActivity, extractedString, ReaderHelper.TapType.CARD, true);
                            } else {
                                ReaderHelper.checkCard(mainActivity, "NOT_41_CARD", ReaderHelper.TapType.CARD, true);
                            }
                            handler.postDelayed(() -> stopReading = false, 2000);
                        }
                        break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    ReaderHelper.checkCard(mainActivity, "AAAAAAAA", ReaderHelper.TapType.CARD, true);
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
