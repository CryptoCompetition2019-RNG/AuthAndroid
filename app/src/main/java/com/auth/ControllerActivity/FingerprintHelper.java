package com.auth.ControllerActivity;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;

public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal mCancellationSignal;
    //PURPOSE_ENCRYPT,则表示生成token，否则为取出token
    private static FingerprintHelper instance = null;


    private FingerprintHelper() {
    }

    public static FingerprintHelper getInstance() {
        if (instance == null) {
            synchronized (FingerprintHelper.class) {
                if (instance == null) {
                    instance = new FingerprintHelper();
                }
            }
        }
        return instance;
    }


    public void stopAuthenticate() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

}
