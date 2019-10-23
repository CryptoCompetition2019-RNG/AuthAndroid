package com.auth.NetworkUtils;

import org.junit.Test;

import static org.junit.Assert.*;

public class SessionKeyHandlerTest {
    @Test
    public void getSessionKeyHandlerTest(){
        SessionKeyHandler sessionKeyHandler = new SessionKeyHandler(
                (AbstractHandler caller) -> { assertTrue(caller.checkStatus()); },
                (AbstractHandler caller) -> { fail(); }
        );

        try {
            sessionKeyHandler.handleThread.join();
        } catch (InterruptedException ie) { fail(); }
        assertTrue(sessionKeyHandler.checkStatus());
    }
}