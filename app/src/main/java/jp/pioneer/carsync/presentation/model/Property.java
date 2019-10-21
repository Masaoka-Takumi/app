package jp.pioneer.carsync.presentation.model;

/**
 * Created by BP06565 on 2017/02/16.
 */

public class Property<T> {
    private T mValue;
    private boolean mIsDirty;

    public Property() {
    }

    public Property(T value, boolean isDirty) {
        mValue = value;
        mIsDirty = isDirty;
    }

    public Property setValue(T value) {
        if ((value == null && mValue != null)
                || (value != null && !value.equals(mValue))) {
            setDirty();
        }

        mValue = value;
        return this;
    }

    public T getValue() {
        clearDirty();
        return mValue;
    }

    public T peekValue() {
        return mValue;
    }

    public boolean isDirty() {
        return mIsDirty;
    }

    public void setDirty() {
        mIsDirty = true;
    }

    public void clearDirty() {
        mIsDirty = false;
    }
}
