package com.auth.NetworkUtils;

import android.util.Log;

import com.auth.DataModels.UserModel;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.json.JSONObject;
import org.zz.gmhelper.SM4Util;

public class PcAuthHandler extends AbstractHandler {
    private UserModel userModel;
    private SessionKeyHandler sessionKeyHandler;

    private boolean pcAuthCall1() {
        try {
            byte[] cipher2 = SM4Util.encrypt_Ecb_NoPadding(
                    userModel.getSaltSm4Key(), userModel.randomToken.getBytes()
            );
            byte[] cipher1 = SM4Util.encrypt_Ecb_NoPadding(
                    sessionKeyHandler.getSessionSM4Key(), userModel.username.getBytes()
            );
            String message = Hex.encodeHexString(ByteUtils.concatenate(cipher1, cipher2));

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
    public PcAuthHandler(String pcMessage) {
        if (pcMessage.length() != 128) {
            Log.e("PcAuth Failed", "Please provide a 128 length message.");
            return;
        }

        sessionKeyHandler = new SessionKeyHandler();
        if (!sessionKeyHandler.checkStatus()) {
            Log.e("PcAuth Failed", "Failed when negotiate session key.");
            return;
        }

        userModel = new UserModel(pcMessage.substring(0, 64));
        userModel.loadFromFile();
        if(!userModel.checkLoaded()){
            Log.e("PcAuth Failed", "Loaded user by username failed.");
            return;
        }
        userModel.randomToken = pcMessage.substring(64);

        if (!pcAuthCall1()) {
            Log.e("PcAuth Failed", "Failed when pc auth call step 1.");
            return;
        }
        this.compeleteStatus = true;
    }
}
