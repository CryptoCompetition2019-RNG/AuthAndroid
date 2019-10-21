package com.auth.NetworkUtils;

import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Random;

import com.auth.Wrapper.ConvertUtil;
import com.orhanobut.logger.Logger;

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

    SessionKeyHandler() {
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
    }

    public byte[] getBytesSM4Key() {
        String sm4Key = ConvertUtil.zeroRPad(sharedSecret.toString(16), 32);
        try {
            return Hex.decodeHex(sm4Key);
        } catch (DecoderException de) {
            return null;
        }
    }

    public String getSM4Key() {
        return ConvertUtil.zeroRPad(sharedSecret.toString(16).substring(64), 64);
    }
}
