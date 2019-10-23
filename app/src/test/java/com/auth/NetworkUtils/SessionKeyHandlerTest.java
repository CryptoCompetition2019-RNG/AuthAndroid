package com.auth.NetworkUtils;

import org.junit.Test;

import static org.junit.Assert.*;

public class SessionKeyHandlerTest {
    @Test
    public void getSessionKeyHandlerTest(){
        SessionKeyHandler sessionKeyHandler = new SessionKeyHandler((AbstractHandler caller) -> {
            assertTrue(caller.checkStatus());
        });
        try { sessionKeyHandler.handleThread.join(); }
        catch (Exception e) { fail(); }
    }
}