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

public class MobileAuthHandler extends AbstractHandler {
    private UserModel userModel;
    private SessionKeyHandler sessionKeyHandler;

    private String A_pwd = null;
    private String B_pwd = null;

    private boolean mobileAuthCall1() {
        String plainResponse;
        try {
            byte[] sessionSm4Key = sessionKeyHandler.getBytesSM4Key();
            byte[] sm4_id = SM4Util.encrypt_Ecb_NoPadding(sessionSm4Key, userModel.username.getBytes());
            assert sm4_id.length == 64;
            JSONObject request = new JSONObject();
            request.put("data", Hex.encodeHexString(sm4_id));
            JSONObject response = HttpUtil.sendPostRequest("/mobileauth_api1/", request);
            if(response == null) return false;

            byte[] saltSm4Key = userModel.getSaltSm4Key();
            byte[] responseData = Hex.decodeHex(response.getString("data"));
            plainResponse = new String(SM4Util.decrypt_Ecb_NoPadding(saltSm4Key, responseData));
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        if (plainResponse.length() != 128) return false;

        userModel.randomToken = plainResponse.substring(0, 64);
        A_pwd = plainResponse.substring(64);
        return true;
    }

    private void calculateBpwd(){
        BigInteger A = new BigInteger(A_pwd, 16);
        BigInteger Hpwd = new BigInteger( SM3Util.hash( userModel.password.getBytes() ));
        BigInteger exponent = new BigInteger(
                ByteUtils.concatenate(A.xor(Hpwd).toByteArray(), Hpwd.toByteArray())
        );

        B_pwd = ConvertUtil.zeroRPad(
                sessionKeyHandler.g.modPow(exponent, sessionKeyHandler.p).toString(16), 64
        );
    }

    private boolean mobileAuthCall2() {
        try {
            byte[] sm4_rB = SM4Util.encrypt_Ecb_NoPadding(
                    sessionKeyHandler.getBytesSM4Key(), (userModel.randomToken + B_pwd).getBytes()
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
