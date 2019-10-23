package com.auth.NetworkUtils;

import android.util.Log;

import com.auth.Wrapper.ConvertUtil;
import com.auth.DataModels.UserModel;
import com.auth.Wrapper.ThreadWrapper;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.json.JSONObject;
import org.zz.gmhelper.SM3Util;
import org.zz.gmhelper.SM4Util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.function.Consumer;

public class RegisterHandler extends AbstractHandler {
    private UserModel userModel;
    private SessionKeyHandler sessionKeyHandler;

    private boolean registerCall() {
        BigInteger tempBInt = new BigInteger(
                (userModel.password + userModel.salt).getBytes(StandardCharsets.US_ASCII)
        );
        tempBInt = tempBInt.modPow(userModel.biologic, sessionKeyHandler.p);
        String s = new String(SM3Util.hash(tempBInt.toByteArray()));

        byte[] leftOperate = SM3Util.hash((userModel.username + s).getBytes(StandardCharsets.US_ASCII));
        byte[] rightOperate = SM3Util.hash(userModel.password.getBytes(StandardCharsets.US_ASCII));

        tempBInt = (new BigInteger(leftOperate)).xor(new BigInteger(rightOperate));
        String A_pwd = Hex.encodeHexString(ConvertUtil.zeroRPad(tempBInt, 32));
        // info: A_pwd.length() == 64

        BigInteger exponent = new BigInteger(ByteUtils.concatenate(leftOperate, rightOperate));
        tempBInt = sessionKeyHandler.g.modPow(exponent, sessionKeyHandler.p);
        String B_pwd = Hex.encodeHexString(ConvertUtil.zeroRPad(tempBInt, 32));
        //  info: B_pwd.length() == 64

        String hexHashImei = Hex.encodeHexString(SM3Util.hash(userModel.imei.toByteArray()));

        try {
            byte[] plainData = (userModel.username + userModel.salt + A_pwd + B_pwd + hexHashImei).getBytes(StandardCharsets.US_ASCII);
            byte[] sm4SessionKey = sessionKeyHandler.getSessionSM4Key();
            byte[] cipherData = SM4Util.encrypt_Ecb_NoPadding(sm4SessionKey, plainData);

            JSONObject request = new JSONObject() {{
                put("data", Hex.encodeHexString(cipherData));
            }};
            JSONObject response = HttpUtil.sendPostRequest("/register_api/", request);
            return (response != null) && response.getInt("code") == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public RegisterHandler(
            UserModel _usermodel_,
            Consumer<AbstractHandler> successCallBack,
            Consumer<AbstractHandler> failedCallBack
    ) {
        userModel = _usermodel_;
        Consumer<AbstractHandler> sessionSuccessCallBack = (AbstractHandler caller) ->
        {
            Log.i("RegisterInfo", "Negotiate session key success");

            if (!this.registerCall()) {
                Log.e("RegisterFailed", "Failed when request");
                failedCallBack.accept(this);
                return;
            }

            userModel.saveToFile();
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
                Log.w("RegisterWarn", String.format("Interrupt:%s", ie.toString()));
            }
        });
        handleThread.start();
    }
}
