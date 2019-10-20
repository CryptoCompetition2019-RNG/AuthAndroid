package com.auth.NetworkUtils;

import android.util.Log;

import com.auth.Wrapper.SM4Util;
import com.auth.DataModels.UserModel;

import org.json.JSONObject;

public class PcAuthHandler extends AbstractHandler {
    private UserModel userModel;
    private SessionKeyHandler sessionKeyHandler;

    private boolean pcAuthCall1() {
        SM4Util sm4 = new SM4Util();

        sm4.setSecretKey(userModel.salt);
        String sm4_r1 = sm4.encryptData_ECB(userModel.randomToken);

        sm4.setSecretKey(sessionKeyHandler.getSM4Key());
        String sm4_id = sm4.encryptData_ECB(userModel.username);

        try {
            JSONObject request = new JSONObject();
            request.put("data", sm4_r1 + sm4_id);
            JSONObject response = HttpUtil.sendPostRequest("/pcauth_api1/", request);
            return (response != null) && (response.getInt("data") == 0);
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
