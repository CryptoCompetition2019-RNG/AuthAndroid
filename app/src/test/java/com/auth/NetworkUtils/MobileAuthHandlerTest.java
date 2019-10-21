package com.auth.NetworkUtils;

import org.junit.Test;

import static org.junit.Assert.*;

public class MobileAuthHandlerTest {
    @Test
    public void getMobileAuthHandlerTest(){
        String username = "shesl-meow";
        String password = "shesl-meow";

        MobileAuthHandler mobileAuthHandler = new MobileAuthHandler(username, password);
        assertTrue(mobileAuthHandler.checkStatus());
    }

}