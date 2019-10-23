package com.auth.NetworkUtils;

import android.util.Log;

import com.auth.Wrapper.ConvertUtil;
import com.auth.DataModels.UserModel;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.json.JSONObject;
import org.zz.gmhelper.SM3Util;
import org.zz.gmhelper.SM4Util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class MobileAuthHandler extends AbstractHandler {
    private UserModel userModel;
    private SessionKeyHandler sessionKeyHandler;

    private String A_pwd = null;
    private String B_pwd = null;

    private boolean mobileAuthCall1() {
        byte[] plainResponse;
        try {
            byte[] sessionSm4Key = sessionKeyHandler.getSessionSM4Key();
            byte[] sm4_id = SM4Util.encrypt_Ecb_NoPadding(
                    sessionSm4Key, userModel.username.getBytes(StandardCharsets.US_ASCII)
            );
            JSONObject request = new JSONObject();
            request.put("data", Hex.encodeHexString(sm4_id));
            JSONObject response = HttpUtil.sendPostRequest("/mobileauth_api1/", request);
            if(response == null) return false;

            byte[] saltSm4Key = userModel.getSaltSm4Key();
            byte[] responseData = Hex.decodeHex(response.getString("data"));
            plainResponse = SM4Util.decrypt_Ecb_NoPadding(saltSm4Key, responseData);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        if (plainResponse.length != 128) return false;

        userModel.randomToken = ByteUtils.subArray(plainResponse, 0 ,64);
        A_pwd = new String(ByteUtils.subArray(plainResponse, 64), StandardCharsets.US_ASCII);
        return true;
    }

    private void calculateBpwd(){
        BigInteger A = new BigInteger(A_pwd, 16);
        BigInteger hashPassword = new BigInteger(
                SM3Util.hash( userModel.password.getBytes(StandardCharsets.US_ASCII) )
        );
        BigInteger exponent = new BigInteger(ByteUtils.concatenate(
                ConvertUtil.zeroRPad(A.xor(hashPassword), 32),
                ConvertUtil.zeroRPad(hashPassword.toByteArray(), 32)
        ));

        B_pwd = ConvertUtil.zeroRPad(
                sessionKeyHandler.g.modPow(exponent, sessionKeyHandler.p).toString(16), 64
        );
    }

    private boolean mobileAuthCall2() {
        try {
            byte[] sm4_rB = SM4Util.encrypt_Ecb_NoPadding(
                    sessionKeyHandler.getSessionSM4Key(),
                    ByteUtils.concatenate(userModel.randomToken, B_pwd.getBytes(StandardCharsets.US_ASCII))
            );
            JSONObject request = new JSONObject();
            request.put("data", Hex.encodeHexString(sm4_rB));
            JSONObject response = HttpUtil.sendPostRequest("/mobileauth_api2/", request);
            return (response != null) && (response.getInt("code") == 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public MobileAuthHandler(
            String _username_,
            String _password_,
            Consumer<AbstractHandler> successCallBack,
            Consumer<AbstractHandler> failedCallBack
    ){
        final String padUsername = ConvertUtil.zeroRPad(_username_, 64);
        final String padPassword = ConvertUtil.zeroRPad(_password_, 64);
        Consumer<AbstractHandler> sessionSuccessCallBack = (AbstractHandler caller) ->
        {
            Log.i("RegisterInfo", "Negotiate session key success");

            userModel = new UserModel(padUsername) {{ password = padPassword; }};
            userModel.loadFromFile();
            if(!userModel.checkLoaded()) {
                Log.e("MobileAuthFailed", "Loaded user by username failed.");
                failedCallBack.accept(this);
                return;
            }

            if (!mobileAuthCall1()) {
                Log.e("MobileAuthFailed", "Failed when mobile auth call step 1");
                failedCallBack.accept(this);
                return;
            }
            this.calculateBpwd();
            if(!mobileAuthCall2()) {
                Log.e("MobileAuthFailed", "Failed when mobile auth call step 2");
                failedCallBack.accept(this);
                return;
            }

            this.compeleteStatus = true;
            successCallBack.accept(this);
        };
        Consumer<AbstractHandler> sesssionFailedCallBack = (AbstractHandler caller) -> {
            Log.e("MobileAuthFailed", "Failed when negotiate session key");
            failedCallBack.accept(this);
        };

        handleThread = new Thread(() -> {
            sessionKeyHandler = new SessionKeyHandler(sessionSuccessCallBack, sesssionFailedCallBack);
            try {
                sessionKeyHandler.handleThread.join();
            } catch (InterruptedException ie) {
                Log.w("MobileAuthWarn", String.format("Interrupt:%s", ie.toString()));
            }
        });
        handleThread.start();
    }
}
