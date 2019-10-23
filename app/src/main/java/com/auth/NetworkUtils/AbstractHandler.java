package com.auth.NetworkUtils;

public abstract class AbstractHandler {
    protected boolean compeleteStatus = false;

    public boolean checkStatus() {return this.compeleteStatus;}

    public Thread handleThread;
}
