package com.auth.NetworkUtils;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Random;

import com.auth.CryptoUtils.ConvertUtil;

public class DHKeyNegotiator {
    private BigInteger p;
    private BigInteger g;
    private BigInteger mySecret;
    public BigInteger sharedSecret;

    private boolean negotiateCall1() {
        try {
            JSONObject response = HttpUtil.sendPostRequest("/negotiate_key1/", new JSONObject(""));
            this.p = new BigInteger(response.getString("p"), 16);
            this.g = new BigInteger(response.getString("g"), 16);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean negotiateCall2() {
        String message = g.modPow(mySecret, p).toString(16);
        message = ConvertUtil.zeroRightPadding(message, 64);
        try {
            JSONObject requset = new JSONObject();
            requset.put("data", message);
            JSONObject response = HttpUtil.sendPostRequest("/negotiate_key2/", new JSONObject(""));
            sharedSecret = (new BigInteger(response.getString("data"), 16)).modPow(mySecret, p);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    DHKeyNegotiator() {
        if (!negotiateCall1()) {
            Log.e("Negotiate Failed", "Negotiate DH key failed at step 1.");
            return;
        }
        this.mySecret = new BigInteger(256, new Random());
        if (!negotiateCall2()) {
            Log.e("Negotiate Failed", "Negotiate DH key failed at step 2.");
        }
    }
}
