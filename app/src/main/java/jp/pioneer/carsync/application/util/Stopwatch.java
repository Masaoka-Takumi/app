package jp.pioneer.carsync.application.util;

import android.os.SystemClock;

public class Stopwatch {
    private long elapsed = 0;//sec
    private boolean isRunning = false;
    private long startTimeStamp = 0;

    public long getElapsed() {
        return elapsed;
    }

    public void start() {
        if (!isRunning) {
            startTimeStamp = SystemClock.elapsedRealtime();
            isRunning = true;
        }
    }

    public void stop() {
        if (isRunning) {
            elapsed += (SystemClock.elapsedRealtime() - startTimeStamp) / 1000;
            isRunning = false;
        }
    }

    public void reset() {
        elapsed = 0;
        isRunning = false;
        startTimeStamp = 0;
    }

    public void restart() {
        elapsed = 0;
        startTimeStamp = SystemClock.elapsedRealtime();
        isRunning = true;
    }

}
