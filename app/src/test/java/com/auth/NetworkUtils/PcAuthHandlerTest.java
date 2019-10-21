package com.auth.NetworkUtils;

import com.auth.Wrapper.ConvertUtil;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.*;

public class PcAuthHandlerTest {
    @Test
    public void getPcAuthHandlerTest () {
        String username = ConvertUtil.zeroRPad("shesl-meow", 64);
        String random1 = ConvertUtil.zeroRPad((new BigInteger(256, new Random())).toString(16), 64);
        assertEquals(username.length(), 64);
        assertEquals(random1.length(), 64);

        PcAuthHandler pcAuthHandler = new PcAuthHandler(username + random1);
        assertTrue(pcAuthHandler.checkStatus());
    }
}