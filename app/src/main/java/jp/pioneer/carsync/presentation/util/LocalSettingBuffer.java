package jp.pioneer.carsync.presentation.util;

/**
 * Created by hp on 6/27/16.
 */
public class LocalSettingBuffer<T> {
    private T mEntry;

    public LocalSettingBuffer() {
        mEntry = null;
    }

    public void setEntry(T entry) {
        mEntry = entry;
    }

    public T getEntry() {
        return mEntry;
    }

    public void open(T entry) {
        mEntry = entry;
    }

    public void close() {
        mEntry = null;
    }
}