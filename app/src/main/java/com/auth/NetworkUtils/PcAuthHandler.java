package com.auth.NetworkUtils;

import android.util.Log;

import com.auth.DataModels.UserModel;
import com.auth.Wrapper.ConvertUtil;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.json.JSONObject;
import org.zz.gmhelper.SM4Util;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class PcAuthHandler extends AbstractHandler {
    private UserModel userModel;
    private SessionKeyHandler sessionKeyHandler;

    private boolean pcAuthCall1() {
        try {
            byte[] cipher2 = SM4Util.encrypt_Ecb_NoPadding(
                    userModel.getSaltSm4Key(),
                    userModel.randomToken
            );
            byte[] cipher1 = SM4Util.encrypt_Ecb_NoPadding(
                    sessionKeyHandler.getSessionSM4Key(),
                    userModel.username.getBytes(StandardCharsets.US_ASCII)
            );
            String message = ConvertUtil.encodeHexString(ByteUtils.concatenate(cipher1, cipher2));

            JSONObject request = new JSONObject();
            request.put("data", message);
            JSONObject response = HttpUtil.sendPostRequest("/pcauth_api1/", request);
            return (response != null) && (response.getInt("code") == 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param pcMessage PC 认证时，扫二维码得到的字符串信息
     */
    public PcAuthHandler(
            String pcMessage,
            Consumer<AbstractHandler> successCallBack,
            Consumer<AbstractHandler> failedCallBack
    ) {
        if (pcMessage.length() != 128) {
            Log.e("PcAuthFailed", "Please provide a 128 length message.");
            failedCallBack.accept(this);
            return;
        }

        Consumer<AbstractHandler>  sessionSuccessCallBack = (AbstractHandler caller) ->
        {
            Log.i("PcAuthInfo", "Negotiate session key success");

            userModel = new UserModel(pcMessage.substring(0, 64));
            userModel.loadFromFile();
            if(!userModel.checkLoaded()){
                Log.e("PcAuthFailed", "Loaded user by username failed.");
                failedCallBack.accept(this);
                return;
            }
            userModel.randomToken = pcMessage.substring(64).getBytes(StandardCharsets.US_ASCII);

            if (!pcAuthCall1()) {
                Log.e("PcAuthFailed", "Failed when pc auth call step 1.");
                return;
            }
            this.compeleteStatus = true;
            successCallBack.accept(this);
        };
        Consumer<AbstractHandler> sessionFailedCallBack = (AbstractHandler caller) ->
        {
            Log.e("RegisterFailed", "Failed when negotiate session key");
            failedCallBack.accept(this);
        };

        handleThread = new Thread(() -> {
            sessionKeyHandler = new SessionKeyHandler(sessionSuccessCallBack, sessionFailedCallBack);
            try {
                sessionKeyHandler.handleThread.join();
            }catch (InterruptedException ie){
                Log.w("PcAuthWarn", String.format("Interrupt:%s", ie.toString()));
            }
        });
        handleThread.start();
    }
}
