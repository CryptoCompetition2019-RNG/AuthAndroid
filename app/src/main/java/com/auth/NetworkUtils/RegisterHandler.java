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
import java.util.Random;

public class RegisterHandler extends AbstractHandler {
    private UserModel userModel;
    private SessionKeyHandler sessionKeyHandler;

    private boolean registerCall() {
        BigInteger tempBInt = new BigInteger((userModel.password + userModel.salt).getBytes());
        tempBInt = tempBInt.modPow(userModel.biologic, sessionKeyHandler.p);
        String s = new String(SM3Util.hash(tempBInt.toByteArray()));

        byte[] leftOperate = SM3Util.hash( (userModel.username + s).getBytes() );
        byte[] rightOperate = SM3Util.hash( userModel.password.getBytes() );

        tempBInt = (new BigInteger(leftOperate)).xor(new BigInteger(rightOperate));
        String A_pwd = ConvertUtil.zeroRPad(tempBInt.toString(16), 64);

        BigInteger exponent = new BigInteger(ByteUtils.concatenate(leftOperate, rightOperate));
        tempBInt = sessionKeyHandler.g.modPow(exponent, sessionKeyHandler.p);
        String B_pwd = ConvertUtil.zeroRPad(tempBInt.toString(16), 64);

        String hexHashImei = Hex.encodeHexString(SM3Util.hash(userModel.imei.toByteArray()));

        try {
            byte[] plainData = (userModel.username + userModel.salt + A_pwd + B_pwd + hexHashImei).getBytes();
            byte[] sm4SessionKey = sessionKeyHandler.getSessionSM4Key();
            byte[] cipherData = SM4Util.encrypt_Ecb_NoPadding(sm4SessionKey, plainData);

            System.out.println(String.format("shared %s",
                    Hex.encodeHexString(sessionKeyHandler.sharedSecret.toByteArray()))
            );
            System.out.println(String.format("key: %s", Hex.encodeHexString(sm4SessionKey)));
            System.out.println(String.format("cipher: %s", Hex.encodeHexString(cipherData)));
            JSONObject request = new JSONObject(){{
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
            final String _username_,
            final String _password_,
            final BigInteger _biologic_,
            final BigInteger _imei_
    ) {
        sessionKeyHandler = new SessionKeyHandler((AbstractHandler caller) -> {});
        if (!sessionKeyHandler.checkStatus()) {
            Log.e("Register Failed", "Failed when negotiate session key");
            return;
        }

        userModel = new UserModel( ConvertUtil.zeroRPad(_username_, 64) ) {
            {
                password = ConvertUtil.zeroRPad(_password_, 64);
                salt = ConvertUtil.zeroRPad(
                        (new BigInteger(256, new Random())).toString(16), 64
                );
                biologic = _biologic_;
                imei = _imei_;
            }
        };

        if (!this.registerCall()) {
            Log.e("Register Failed", "Failed when request");
            return;
        }
        userModel.saveToFile();
        this.compeleteStatus = true;
    }
}
