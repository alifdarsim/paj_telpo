package com.paj.pajbustelpo;

import android.os.Handler;
import android.util.Log;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.ScanMode;
import com.paj.pajbustelpo.TelpoHelper.Telpo;

public class QrScanner {

    DatabaseHelper db;
    MainActivity mainActivity;
    CodeScannerView scannerView;
    private CodeScanner mCodeScanner;

    public QrScanner(MainActivity mainActivity, CodeScannerView scannerView){
        this.db = mainActivity.db;
        this.mainActivity = mainActivity;
        this.scannerView = scannerView;
    }

    public void startScan(){
        mCodeScanner = new CodeScanner(mainActivity, scannerView);
        mCodeScanner.startPreview();

        mCodeScanner.setCamera(CodeScanner.CAMERA_FRONT);
        mCodeScanner.setFormats(CodeScanner.TWO_DIMENSIONAL_FORMATS);
        mCodeScanner.setAutoFocusEnabled(true);
        mCodeScanner.setScanMode(ScanMode.SINGLE);

        mCodeScanner.setDecodeCallback(result -> mainActivity.runOnUiThread(() -> {
            Log.e("QrCode Detected", result.getText());
            String qrCodeText = result.getText();
            mainActivity.logger.writeToLogger("Qr Detected: " + qrCodeText, "yellow");

            User user = db.checkUserQrCode(qrCodeText);
            mainActivity.checkUserInfo(user, Telpo.TapType.QR_CODE);

            //wait 2 second after scan, then start scan again
            Handler handler = new Handler();
            handler.postDelayed(() -> mCodeScanner.startPreview(), 2000);
        }));
    }


}
