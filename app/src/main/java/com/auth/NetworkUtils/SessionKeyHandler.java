package com.auth.NetworkUtils;

import android.util.Log;

import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

import com.auth.Wrapper.ConvertUtil;
import com.auth.Wrapper.ThreadWrapper;

public class SessionKeyHandler extends AbstractHandler {
    public BigInteger p;
    public BigInteger g;
    private BigInteger mySecret;
    public BigInteger sharedSecret;

    private boolean negotiateCall1() {
        try {
            JSONObject response = HttpUtil.sendPostRequest("/negotiate_key1/", new JSONObject("{}"));
            if (response == null) return false;
            JSONObject responseData = response.getJSONObject("data");
            this.p = new BigInteger(responseData.getString("p"), 16);
            this.g = new BigInteger(responseData.getString("g"), 16);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean negotiateCall2() {
        String message = g.modPow(mySecret, p).toString(16);
        message = ConvertUtil.zeroRPad(message, 64);
        try {
            JSONObject requset = new JSONObject();
            requset.put("data", message);
            JSONObject response = HttpUtil.sendPostRequest("/negotiate_key2/", requset);
            if(response == null) return false;
            sharedSecret = (new BigInteger(response.getString("data"), 16)).modPow(mySecret, p);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    SessionKeyHandler(Consumer<AbstractHandler> successCallBack) {
        handleThread = ThreadWrapper.getTimeoutAyncThread(() ->{
            if (!negotiateCall1()) {
                Log.e("Negotiate Failed", "Negotiate DH key failed at step 1.");
                return;
            }
            this.mySecret = new BigInteger(256, new Random());
            if (!negotiateCall2()) {
                Log.e("Negotiate Failed", "Negotiate DH key failed at step 2.");
                return;
            }
            this.compeleteStatus = true;
            successCallBack.accept(this);
        }, 0);
        handleThread.start();
    }

    public byte[] getSessionSM4Key() {
        byte[] bintArray = sharedSecret.toByteArray();
        if (bintArray[0] == 0) {
            bintArray = Arrays.copyOfRange(bintArray, 1, bintArray.length);
        }
        return ConvertUtil.zeroRPad(bintArray, 16);
    }
}
