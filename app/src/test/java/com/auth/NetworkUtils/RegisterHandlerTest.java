package com.auth.NetworkUtils;

import android.util.Log;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class RegisterHandlerTest {
    @Test
    public void getRegisterHandlerTest() {
        String username = "shesl-meow";
        String password = "shesl-meow";
        BigInteger biologic = BigInteger.valueOf(0x1);
        // 获取 IMEI 码应该写 Activity 里面（需要调用父类方法）：
        // TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        // BigInteger IMEI = BigInteger.valueOf(telephonyManager.getDeviceId());
        BigInteger fake_IMEI = BigInteger.valueOf(0x7fff);

        RegisterHandler registerHandler = new RegisterHandler(username, password, biologic, fake_IMEI);
        assertTrue(registerHandler.checkStatus());
    }
}