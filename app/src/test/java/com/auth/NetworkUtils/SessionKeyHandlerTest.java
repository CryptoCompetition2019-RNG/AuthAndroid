package com.auth.NetworkUtils;

import org.junit.Test;

import static org.junit.Assert.*;

public class SessionKeyHandlerTest {
    @Test
    public void getSessionKeyHandlerTest(){
        SessionKeyHandler sessionKeyHandler = new SessionKeyHandler();
        assertTrue(sessionKeyHandler.checkStatus());
    }
}