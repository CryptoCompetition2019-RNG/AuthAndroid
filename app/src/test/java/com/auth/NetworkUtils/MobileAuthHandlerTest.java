package com.auth.NetworkUtils;

import org.junit.Test;

import static org.junit.Assert.*;

public class MobileAuthHandlerTest {
    @Test
    public void getMobileAuthHandlerTest(){
        String username = "shesl-meow";
        String password = "shesl-meow";

        MobileAuthHandler mobileAuthHandler = new MobileAuthHandler(
                username,
                password,
                (AbstractHandler caller) -> { assertTrue(caller.checkStatus()); },
                (AbstractHandler caller) -> { fail(); }
        );
        try {
            mobileAuthHandler.handleThread.join();
        } catch (InterruptedException ie){ fail(); }
        assertTrue(mobileAuthHandler.checkStatus());
    }

}