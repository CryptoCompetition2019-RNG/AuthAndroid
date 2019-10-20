package com.auth.NetworkUtils;

import android.util.Log;

import com.auth.Wrapper.SM3Hash;
import com.auth.Wrapper.SM4Util;
import com.auth.DataModels.UserModel;

import org.json.JSONObject;

public class DynamicAuthHandler extends AbstractHandler {
    private SessionKeyHandler sessionKeyHandler;
    private UserModel userModel;

    private String qrMessage;

    private boolean dynamicAuthCall2(){
        SM4Util sm4 = new SM4Util();
        SM3Hash sm3 = new SM3Hash();

        sm4.setSecretKey(userModel.salt);
        userModel.randomToken = sm4.decryptData_ECB(qrMessage);

        sm4.setSecretKey(sessionKeyHandler.getSM4Key());
        String sm4_hir = sm4.encryptData_ECB(sm3.stringSM3(userModel.imei) + userModel.randomToken);

        try {
            JSONObject request = new JSONObject();
            request.put("data", sm4_hir);
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
            String _qrMessage_,
            String _imei_
    ){
        sessionKeyHandler = new SessionKeyHandler();
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
