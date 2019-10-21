package jp.pioneer.carsync.presentation.view.service;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;

/**
 * Created by NSW00_008316 on 2017/04/18.
 */

public enum ForegroundReason {
    BLUETOOTH_LISTENING(R.string.ntc_001),
    BLUETOOTH_CONNECTING(R.string.ntc_002),
    USB_CONNECTING(R.string.ntc_003);

    @StringRes private int mMessageResId;

    @StringRes
    public int getMessageResId() {
        return mMessageResId;
    }

    ForegroundReason(@StringRes int messageResId) {
        mMessageResId = messageResId;
    }
}
