package com.auth.NetworkUtils;

import com.auth.DataModels.UserModel;

import org.junit.Test;
import org.zz.gmhelper.SM4Util;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class DynamicAuthHandlerTest {
    @Test
    public void getDynamicAuthHandlerTest(){
        String username = "shesl-meow";
        UserModel userModel = new UserModel(username);
        userModel.loadFromFile();
        assertTrue(userModel.checkLoaded());

        String randomValue3 = "fda115c369a8bf64400185f5331e5f5d2ffc75279d6a50f1c932e492dda4dbaa";
        byte[] qrMessage = new byte[0];
        try {
            qrMessage = SM4Util.encrypt_Ecb_NoPadding(userModel.getSaltSm4Key(), randomValue3.getBytes());
        } catch (Exception e){ fail(); }
        // 获取 IMEI 码应该写 Activity 里面（需要调用父类方法）：
        // TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        // BigInteger IMEI = BigInteger.valueOf(telephonyManager.getDeviceId());
        BigInteger fake_IMEI = BigInteger.valueOf(0x7fff);

        DynamicAuthHandler dynamicAuthHandler = new DynamicAuthHandler(username, qrMessage, fake_IMEI);
        assertTrue(dynamicAuthHandler.checkStatus());
    }

}