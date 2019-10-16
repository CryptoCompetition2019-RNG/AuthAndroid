package com.auth.NetworkUtils;

import android.util.Log;

import com.auth.CryptoUtils.ConvertUtil;
import com.auth.CryptoUtils.SM3Hash;
import com.auth.CryptoUtils.SM4Util;
import com.auth.DataModels.UserModel;

import org.json.JSONObject;

import java.math.BigInteger;

public class MobileAuthHandler extends AbstractHandler {
    private UserModel userModel;
    private SessionKeyHandler sessionKeyHandler;

    private String A_pwd = null;
    private String B_pwd = null;

    private boolean mobileAuthCall1() {
        SM4Util sm4 = new SM4Util();

        sm4.setSecretKey(sessionKeyHandler.getSM4Key());
        String sm4_id = sm4.encryptData_ECB(userModel.username);

        String plainResponse;
        try {
            JSONObject request = new JSONObject();
            request.put("data", sm4_id);
            JSONObject response = HttpUtil.sendPostRequest("/mobileauth_api1/", request);
            if(response == null) return false;

            plainResponse = sm4.decryptData_ECB(response.getString("data"));
            sm4.setSecretKey(userModel.salt);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        if (plainResponse.length() == 128) return false;

        userModel.randomToken = plainResponse.substring(0, 64);
        A_pwd = plainResponse.substring(64);
        return true;
    }

    private void calculateBpwd(){
        SM3Hash sm3 = new SM3Hash();

        BigInteger A = new BigInteger(A_pwd, 16);
        BigInteger Hpwd = new BigInteger(sm3.bytesSM3(userModel.password.getBytes()));

        B_pwd = ConvertUtil.zeroRPad(
                sessionKeyHandler.g.modPow(A.xor(Hpwd), sessionKeyHandler.p).toString(16), 64
        );
    }

    private boolean mobileAuthCall2() {
        SM4Util sm4 = new SM4Util();

        sm4.setSecretKey(sessionKeyHandler.getSM4Key());
        String sm4_rB = sm4.encryptData_ECB(userModel.randomToken + B_pwd);

        try {
            JSONObject request = new JSONObject();
            request.put("data", sm4_rB);
            JSONObject response = HttpUtil.sendPostRequest("/mobileauth_api2/", request);
            return (response != null) && (response.getInt("code") == 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public MobileAuthHandler(final String _username_, final String _password_){
        sessionKeyHandler = new SessionKeyHandler();
        if(!sessionKeyHandler.checkStatus()){
            Log.e("MobileAuth Failed", "Failed when negotiate session key.");
            return;
        }

        userModel = new UserModel(_username_) {
            { password = ConvertUtil.zeroRPad(_password_, 64); }
        };
        userModel.loadFromFile();
        if(!userModel.checkLoaded()) {
            Log.e("MobileAuth Failed", "Loaded user by username failed.");
            return;
        }
        if (!mobileAuthCall1()) {
            Log.e("MobileAuth Failed", "Failed when mobile auth call step 1");
            return;
        }
        this.calculateBpwd();
        if(!mobileAuthCall2()) {
            Log.e("MobileAuth Failed", "Failed when mobile auth call step 2");
            return;
        }

        this.compeleteStatus = true;
    }
}
