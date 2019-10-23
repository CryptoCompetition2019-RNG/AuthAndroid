package com.auth.NetworkUtils;

import android.util.Log;

import com.auth.DataModels.UserModel;
import com.auth.Wrapper.ConvertUtil;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.*;

public class RegisterHandlerTest {
    @Test
    public void getRegisterHandlerTest() {
        String _username_ = "shesl-meow";
        String _password_ = "shesl-meow";
        BigInteger _biologic_ = BigInteger.valueOf(0x1);
        // 获取 IMEI 码应该写 Activity 里面（需要调用父类方法）：
        // TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        // BigInteger IMEI = BigInteger.valueOf(telephonyManager.getDeviceId());
        BigInteger fake_IMEI = BigInteger.valueOf(0x7fff);

        UserModel userModel = new UserModel(ConvertUtil.zeroRPad(_username_, 64)) {
            {
                password = ConvertUtil.zeroRPad(_password_, 64);
                salt = ConvertUtil.zeroRPad(
                        (new BigInteger(256, new Random())).toString(16), 64
                );
                biologic = _biologic_;
                imei = fake_IMEI;
            }
        };
        RegisterHandler registerHandler = new RegisterHandler(
                userModel,
                (AbstractHandler caller) -> { assertTrue(caller.checkStatus()); },
                (AbstractHandler caller) -> { fail(); }
        );
        try {
            registerHandler.handleThread.join();
        } catch (InterruptedException ie){ fail(); }
        assertTrue(registerHandler.checkStatus());
    }
}