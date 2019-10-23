package com.auth.NetworkUtils;

import com.auth.DataModels.UserModel;
import com.auth.Wrapper.ConvertUtil;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.junit.Test;
import org.zz.gmhelper.SM4Util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class DynamicAuthHandlerTest {
    @Test
    public void getDynamicAuthHandlerTest(){
        String username = "shesl-meow";
        UserModel userModel = new UserModel(username);
        userModel.loadFromFile();
        assertTrue(userModel.checkLoaded());
        // todo: 获取 IMEI 码应该写 Activity 里面（需要调用父类方法）：
        //  TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //  BigInteger IMEI = BigInteger.valueOf(telephonyManager.getDeviceId());
        BigInteger fake_IMEI = BigInteger.valueOf(0x7fff);

        byte[] qrMessage = mockPc();    // todo: 模拟 PC 向后端请求随机数
        assertNotNull(qrMessage);

        DynamicAuthHandler dynamicAuthHandler = new DynamicAuthHandler(
                username, qrMessage, fake_IMEI,
                (AbstractHandler caller) -> { assertTrue(caller.checkStatus()); },
                (AbstractHandler caller) -> { fail(); }
        );
        try {
            dynamicAuthHandler.handleThread.join();
        } catch (InterruptedException ie){ fail(); }
        assertTrue(dynamicAuthHandler.checkStatus());
    }

    private byte[] mockPc(){
        try {
            String username = ConvertUtil.zeroRPad("shesl-meow", 64);
            SessionKeyHandler sessionKeyHandler = new SessionKeyHandler(
                    (AbstractHandler caller) -> { assertTrue(caller.checkStatus()); },
                    (AbstractHandler caller) -> { fail(); }
            );
            sessionKeyHandler.handleThread.join();
            assertTrue(sessionKeyHandler.checkStatus());
            byte[] cipherRequest = SM4Util.encrypt_Ecb_NoPadding(
                    sessionKeyHandler.getSessionSM4Key(), username.getBytes(StandardCharsets.US_ASCII)
            );
            JSONObject request = new JSONObject(){{ put("data", Hex.encodeHexString(cipherRequest)); }};
            JSONObject response = HttpUtil.sendPostRequest("/dynamicauth_api1/", request);
            assertTrue((response != null) && (response.getInt("code") == 0));
            return Hex.decodeHex(response.getString("data"));
        } catch (Exception e) { fail(); }
        return null;
    }

}