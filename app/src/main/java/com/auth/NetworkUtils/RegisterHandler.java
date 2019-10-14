package com.auth.NetworkUtils;

public class RegisterHandler {
    private DHKeyNegotiator dhKeyNegotiator = new DHKeyNegotiator();
    private String registerUsername;
    private String registerPassword;

    public static boolean registerCall() {
        return true;
    }

    RegisterHandler(String username, String password) {
        registerUsername = username;
        registerPassword = password;
    }
}
