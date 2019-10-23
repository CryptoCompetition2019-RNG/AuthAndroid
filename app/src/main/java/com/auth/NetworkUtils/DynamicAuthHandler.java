package com.auth.NetworkUtils;

import android.util.Log;

import com.auth.DataModels.UserModel;
import com.auth.Wrapper.ConvertUtil;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.json.JSONObject;
import org.zz.gmhelper.SM3Util;
import org.zz.gmhelper.SM4Util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class DynamicAuthHandler extends AbstractHandler {
    private SessionKeyHandler sessionKeyHandler;
    private UserModel userModel;

    private byte[] qrMessage;

    private boolean dynamicAuthCall2(){
        try {
            userModel.randomToken = SM4Util.decrypt_Ecb_NoPadding(userModel.getSaltSm4Key(), qrMessage);

            String hashImei = ConvertUtil.encodeHexString( SM3Util.hash(userModel.imei.toByteArray()) );
            byte[] plainData = ByteUtils.concatenate(
                    (userModel.username + hashImei).getBytes(StandardCharsets.US_ASCII), userModel.randomToken
            );
            byte[] cipherData = SM4Util.encrypt_Ecb_NoPadding(sessionKeyHandler.getSessionSM4Key(), plainData);

            JSONObject request = new JSONObject();
            request.put("data", ConvertUtil.encodeHexString(cipherData));
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
            BigInteger _imei_,
            Consumer<AbstractHandler> successCallBack,
            Consumer<AbstractHandler> failedCallBack
    ){
        qrMessage = _qrMessage_;
        Consumer<AbstractHandler> sessionSuccessCallBack = (AbstractHandler caller) ->
        {
            Log.i("DynamicAuthFailed", "Negotiate session key success");

            userModel = new UserModel(_username_);
            userModel.loadFromFile();
            if(!userModel.checkLoaded()){
                Log.e("DynamicAuthFailed", "Loaded user by username failed.");
                failedCallBack.accept(this);
                return;
            }
            userModel.imei = _imei_;

            if(!dynamicAuthCall2()) {
                Log.e("DynamicAuthFailed", "Failed when dynamic auth call step 2");
                failedCallBack.accept(this);
                return;
            }

            this.compeleteStatus = true;
            successCallBack.accept(this);
        };
        Consumer<AbstractHandler> sessionFailedCallBack = (AbstractHandler caller) ->
        {
            Log.e("DynamicAuthFailed", "Failed when negotiate session key");
            failedCallBack.accept(this);
        };

        handleThread = new Thread(() -> {
            sessionKeyHandler = new SessionKeyHandler(sessionSuccessCallBack, sessionFailedCallBack);
            try {
                sessionKeyHandler.handleThread.join();
            }catch (InterruptedException ie){
                Log.w("DynamicAuthWarn", String.format("Interrupt:%s", ie.toString()));
            }
        });
        handleThread.start();
    }
}
