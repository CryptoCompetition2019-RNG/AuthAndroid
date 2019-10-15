package com.auth.NetworkUtils;

import android.util.Log;

import com.auth.CryptoUtils.ConvertUtil;
import com.auth.CryptoUtils.SM3Hash;
import com.auth.DataModels.UserModel;

import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Random;

public class RegisterHandler {
    private UserModel userModel;
    private SessionKeyNegotiator sessionKeyNegotiator;

    private boolean registerCall() {
        SM3Hash sm3 = new SM3Hash();

        BigInteger tempBInt = new BigInteger((userModel.password + userModel.salt).getBytes());
        tempBInt = tempBInt.modPow(BigInteger.valueOf(userModel.biologic), sessionKeyNegotiator.p);
        String s = new String(sm3.bytesSM3(tempBInt.toByteArray()));

        BigInteger tempBInt1 = new BigInteger(sm3.bytesSM3( (userModel.username + s).getBytes() ));
        BigInteger tempBInt2 = new BigInteger(sm3.bytesSM3( userModel.password.getBytes() ));
        String A_pwd = ConvertUtil.zeroRPad(tempBInt1.xor(tempBInt2).toString(16), 64);

        tempBInt = sessionKeyNegotiator.g.modPow(tempBInt2, sessionKeyNegotiator.p);
        String B_pwd = ConvertUtil.zeroRPad(tempBInt.toString(16), 64);

        String Hash_IMEI = sm3.stringSM3( userModel.imei );

        try {
            JSONObject request = new JSONObject();
            request.put("data", userModel.username + userModel.salt + A_pwd + B_pwd + Hash_IMEI);

            JSONObject response = HttpUtil.sendPostRequest("/register_api/", request);
            return (response != null) && response.getInt("code") == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    RegisterHandler(
            final String _username_,
            final String _password_,
            final Integer _biologic_,
            final String _imei_
    ) {
        sessionKeyNegotiator = new SessionKeyNegotiator();

        userModel = new UserModel() {
                {
                    username = ConvertUtil.zeroRPad(_username_, 64);
                    password = ConvertUtil.zeroRPad(_password_, 64);
                    salt = ConvertUtil.zeroRPad(
                            (new BigInteger(256, new Random())).toString(16), 64
                    );
                    biologic = _biologic_;
                    imei = _imei_;
                }
        };

        if(! this.registerCall() ) {
            Log.e("Register Failed", "Failed when request");
        }
    }
}
