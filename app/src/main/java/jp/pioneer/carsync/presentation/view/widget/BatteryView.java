package jp.pioneer.carsync.presentation.view.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import jp.pioneer.carsync.R;

/**
 * バッテリー表示ウィジット
 */

public class BatteryView extends RelativeLayout {
    View mBattery;
    ImageView mBatteryCharge;
    float mBatteryPct = 0;
    boolean mIsCharge = false;
    public BatteryView(Context context) {
        this(context, null);
    }

    public BatteryView(Context context, AttributeSet attrs) {
        this(context, attrs, R.layout.element_battery_view);
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View layout = LayoutInflater.from(context).inflate(R.layout.element_battery_view, this);
        mBattery = layout.findViewById(R.id.buttery_image);
        mBatteryCharge = (ImageView) layout.findViewById(R.id.buttery_charge);
    }

    public void setBatteryPct(float batteryPct) {
        mBatteryPct = batteryPct;
        float width = getResources().getDimension(R.dimen.battery_level_width);
        float height = getResources().getDimension(R.dimen.battery_level_height);
        LayoutParams params = new LayoutParams((int)width, (int)(height*mBatteryPct));
        params.bottomMargin = (int)getResources().getDimension(R.dimen.battery_bottom_margin);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mBattery.setLayoutParams(params);
        invalidate();
    }

    public float getBatteryPct() {
        return mBatteryPct;
    }

    public void setCharge(boolean charge) {
        mIsCharge = charge;
        if(mIsCharge){
            mBatteryCharge.setVisibility(View.VISIBLE);
        } else {
            mBatteryCharge.setVisibility(View.INVISIBLE);
        }
        invalidate();
    }

    public boolean isCharge() {
        return mIsCharge;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        getContext().registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int status = intent.getIntExtra("status", 0);
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 0);

                mIsCharge = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
                if(mIsCharge){
                    mBatteryCharge.setVisibility(View.VISIBLE);
                } else {
                    mBatteryCharge.setVisibility(View.INVISIBLE);
                }

                mBatteryPct = level / (float)scale;
                float width = getResources().getDimension(R.dimen.battery_level_width);
                float height = getResources().getDimension(R.dimen.battery_level_height);
                LayoutParams params = new LayoutParams((int)width, (int)(height*mBatteryPct));
                params.bottomMargin = (int)getResources().getDimension(R.dimen.battery_bottom_margin);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                mBattery.setLayoutParams(params);

            }
        }
    };
}