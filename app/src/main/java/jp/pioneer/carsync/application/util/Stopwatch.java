package jp.pioneer.carsync.application.util;

public class Stopwatch {
    private long elapsed = 0;//sec
    private boolean isRunning = false;
    private long startTimeStamp = 0;

    public long getElapsed() {
        return elapsed;
    }

    public void start() {
        if (!isRunning) {
            startTimeStamp = System.currentTimeMillis();
            isRunning = true;
        }
    }

    public void stop() {
        if (isRunning) {
            elapsed += (System.currentTimeMillis() - startTimeStamp) / 1000;
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
        startTimeStamp = System.currentTimeMillis();
        isRunning = true;
    }

}
