package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.util.AppUtil;
import jp.pioneer.carsync.domain.model.TimeFormatSetting;

public class DimmerTimePicker extends FrameLayout implements NumberPicker.OnValueChangeListener {
    private NumberPicker hour;
    private NumberPicker minute;
    private NumberPicker ampm; // 12時間設定の場合のみオブジェクト有効(24時間設定の場合はnull)

    private String[] AmPmList = {"AM", "PM"};

    private Context mContext;
    public static TimeFormatSetting mTimeFormatSetting;

    public DimmerTimePicker(Context context) {
        super(context);
        init(context);
    }

    public DimmerTimePicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DimmerTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setTimeFormatSetting(TimeFormatSetting setting) {
        mTimeFormatSetting = setting;
    }

    private void init(Context context){
        View view;
        if (mTimeFormatSetting == TimeFormatSetting.TIME_FORMAT_24) {
            // 24時間設定である
            view = LayoutInflater.from(context).inflate(R.layout.element_dimmer_time_picker, this);
        } else {
            // 12時間設定である
            view = LayoutInflater.from(context).inflate(R.layout.element_dimmer_time_picker_12h, this);
            this.ampm = (NumberPicker) view.findViewById(R.id.picker_ampm);
        }
        this.hour = (NumberPicker) view.findViewById(R.id.picker_hour);
        this.hour.setOnValueChangedListener(this);
        this.minute = (NumberPicker) view.findViewById(R.id.picker_minute);
        this.minute.setOnValueChangedListener(this);
        mContext = context;
    }

    public void set(int hourValue, int minuteValue){
        if (ampm == null) {
            // 24時間設定である
            hour.setMinValue(0);
            hour.setMaxValue(23);
            hour.setValue(hourValue);
        } else {
            // 12時間設定である
            int hour12 = hourValue % 12;
            hour.setMinValue(0);
            hour.setMaxValue(11);
            hour.setValue(hour12);

            if (!AppUtil.isZero2ElevenIn12Hour(mContext)) {
                // 日本以外
                hour.setDisplayedValues(new String[]{"12", "1", "2", "3", "4", "5","6", "7", "8", "9", "10", "11"});
            } else {
                // 日本
                hour.setDisplayedValues(new String[]{"0", "1", "2", "3", "4", "5","6", "7", "8", "9", "10", "11"});
            }
        }

        minute.setMinValue(0);
        minute.setMaxValue(11);
        minute.setValue(minuteValue / 10);
        minute.setDisplayedValues(new String[]{"00", "10", "20", "30", "40", "50","00", "10", "20", "30", "40", "50"});

       if (ampm != null) {
           ampm.setMinValue(0);
           ampm.setMaxValue(AmPmList.length-1);
           if (hourValue <= 11) {
               ampm.setValue(0); // AM
           } else {
               ampm.setValue(1); // PM
           }
           ampm.setDisplayedValues(AmPmList);
       }
    }

    public int getHour() {
        NumberPicker picker = hour;

        int hour = picker.getValue();
        if (ampm != null) {
            // 12時間設定である
            hour += ampm.getValue() * 12;
        }
        return hour;
    }

    public int getMinute() {
        NumberPicker picker = minute;
        return (picker.getValue() % 6) * 10;
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (picker == minute) {
            int hourValue = hour.getValue();
            int oldMinute =  (oldVal % 6) * 10;
            int newMinute =  (newVal % 6) * 10;

            int maxHour = 23;
            if (ampm != null) {
                // 12時間設定である
                maxHour = 11;
            }

            if(oldMinute == 50 && newMinute == 0) {
                if (hourValue + 1 > maxHour) {
                    hourValue = 0;
                    if (ampm != null) {
                        // 12時間設定である
                        toggleAmPm();
                    }
                } else {
                    hourValue += 1;
                }
            } else if (oldMinute == 0 && newMinute == 50) {
                if (hourValue - 1 < 0) {
                    hourValue = maxHour;
                    if (ampm != null) {
                        // 12時間設定である
                        toggleAmPm();
                    }
                } else {
                    hourValue--;
                }
            }
            hour.setValue(hourValue);
        } else if (picker == hour) {
            if (ampm != null) {
                // 12時間設定である
                if ((oldVal == 11 && newVal == 0) ||(oldVal == 0 && newVal == 11)) {
                    toggleAmPm();
                }
            }
        }
    }

    // AMとPMを切り替える
    private void toggleAmPm() {
        if (ampm == null) {
            return;
        }
        int value = ampm.getValue();
        if (value == 0) {
            ampm.setValue(1);
        } else {
            ampm.setValue(0);
        }
    }
}
