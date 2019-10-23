package com.auth.NetworkUtils;

import android.util.Log;

import com.auth.DataModels.UserModel;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.zz.gmhelper.SM3Util;
import org.zz.gmhelper.SM4Util;

import java.math.BigInteger;

public class DynamicAuthHandler extends AbstractHandler {
    private SessionKeyHandler sessionKeyHandler;
    private UserModel userModel;

    private byte[] qrMessage;

    private boolean dynamicAuthCall2(){
        try {
            userModel.randomToken = new String(SM4Util.decrypt_Ecb_NoPadding(userModel.getSaltSm4Key(), qrMessage));

            String hashImei = Hex.encodeHexString( SM3Util.hash(userModel.imei.toByteArray()) );
            byte[] plainData = (userModel.username + hashImei + userModel.randomToken).getBytes();
            byte[] cipherData = SM4Util.encrypt_Ecb_NoPadding(sessionKeyHandler.getSessionSM4Key(), plainData);

            JSONObject request = new JSONObject();
            request.put("data", Hex.encodeHexString(cipherData));
            JSONObject response = HttpUtil.sendPostRequest("/dynamicauth_api2/", request);
            return (response != null) && (response.getInt("code") == 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param _username_ 用户在输入框内输入的用户名
     * @param _qrMessage_ 扫描二维码得到的字符串结果
     */
    public DynamicAuthHandler(
            String _username_,
            byte[] _qrMessage_,
            BigInteger _imei_
    ){
        sessionKeyHandler = new SessionKeyHandler(
                (AbstractHandler caller) -> {},
                (AbstractHandler caller) -> {}
        );
        if(!sessionKeyHandler.checkStatus()){
            Log.e("DynamicAuth Failed", "Failed when negotiate session key.");
            return;
        }

        userModel = new UserModel(_username_);
        userModel.loadFromFile();
        if(!userModel.checkLoaded()){
            Log.e("DynamicAuth Failed", "Loaded user by username failed.");
            return;
        }
        qrMessage = _qrMessage_;
        userModel.imei = _imei_;

        if(!dynamicAuthCall2()) {
            Log.e("DynamicAuth Failed", "Failed when dynamic auth call step 2");
            return;
        }

        this.compeleteStatus = true;
    }
}
