package com.auth.Wrapper;

import android.util.Log;

public class ThreadWrapper {

    public static Thread getTimeoutAyncThread(Runnable runnable, int delay) {
        return new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (InterruptedException e) {
                Log.w("Thread Interrupted", e.toString());
            }
        });
    }

    public static Thread setTimeoutAync(Runnable runnable, int delay){
        Thread newThread = new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                Log.w("Thread Interrupted", e.toString());
            }
        });
        newThread.start();
        return newThread;
    }

    public static void setTimeoutSync(Runnable runnable, int delay){
        try {
            Thread.sleep(delay);
            runnable.run();
        } catch (Exception e) {
            Log.w("Thread Interrupted", e.toString());
        }
    }
}
