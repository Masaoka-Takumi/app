package jp.pioneer.carsync.presentation.view.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AmPmView extends android.support.v7.widget.AppCompatTextView {

    private boolean mRegistered;

    public AmPmView(Context context) {
        super(context);
    }

    public AmPmView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mRegistered) {
            mRegistered = true;
            registerReceiver();
            onTimeChanged();
        }
    }

    private void registerReceiver() {
        final IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        getContext().registerReceiver(mIntentReceiver, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mRegistered) {
            unregisterReceiver();

            mRegistered = false;
        }
    }

    private void unregisterReceiver() {
        getContext().unregisterReceiver(mIntentReceiver);
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onTimeChanged();
        }
    };

    private void onTimeChanged() {
        SimpleDateFormat sdf = new SimpleDateFormat("H");
        int hour = Integer.parseInt(sdf.format(Calendar.getInstance().getTime()));
        if (hour <= 11) {
            setText("AM");
        } else {
            setText("PM");
        }
    }
}
