package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import java.util.Locale;

import jp.pioneer.carsync.R;
import timber.log.Timber;

public class NumberPickerPreferenceFloat extends DialogPreference {
    private int minValue;
    private boolean wrapSelectorWheel;
    private static final int DEFAULT_value = 0;
    private static final int DEFAULT_maxValue = 0;
    private int currentValue;
    private int maxValue;
    private static final int DEFAULT_minValue = 0;
    public static final boolean DEFAULT_WRAP_SELECTOR_WHEEL = true;
    private final String defaultSummary;
    public NumberPickerPreferenceFloat(Context context, AttributeSet attrs) {
        super(context, attrs);

        defaultSummary = getSummary().toString();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);

        try {
            maxValue = a.getInt(R.styleable.NumberPickerPreference_maxValue, DEFAULT_maxValue);
            minValue = a.getInt(R.styleable.NumberPickerPreference_minValue, DEFAULT_minValue);
            wrapSelectorWheel = a.getBoolean(R.styleable.NumberPickerPreference_wrapSelectorWheel, DEFAULT_WRAP_SELECTOR_WHEEL);
        } finally {
            a.recycle();
        }

        setDialogLayoutResource(R.layout.numberpicker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);

    }

    public int getValue() {
        return currentValue;
    }

    public void setValue(int value) {
        currentValue = value;
        persistInt(currentValue);
    }

    public void setFloatValue(float value) {
        currentValue = (int)(value*1000);
        persistInt(currentValue);
        Timber.d("setFloatValue:currentValue:%d" , currentValue);
        setSummary(String.format(defaultSummary, value));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_value);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(currentValue) : (Integer) defaultValue);
    }

    public static class NumberPickerPreferenceFloatDialogFragmentCompat
            extends PreferenceDialogFragmentCompat {
        private static final String SAVE_STATE_VALUE = "NumberPickerPreferenceFloatDialogFragment.value";
        private NumberPicker picker;
        private int currentValue = 1;
        public NumberPickerPreferenceFloatDialogFragmentCompat() {
        }

        public static NumberPickerPreferenceFloat.NumberPickerPreferenceFloatDialogFragmentCompat newInstance(String key) {
            NumberPickerPreferenceFloat.NumberPickerPreferenceFloatDialogFragmentCompat fragment =
                    new NumberPickerPreferenceFloat.NumberPickerPreferenceFloatDialogFragmentCompat();
            Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            return fragment;
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                currentValue = getNumberPickerPreferenceFloat().getValue();
            } else {
                currentValue = savedInstanceState.getInt(SAVE_STATE_VALUE);
            }
        }

        public void onSaveInstanceState(@NonNull Bundle outState) {
            outState.putInt(SAVE_STATE_VALUE, currentValue);
        }

        private NumberPickerPreferenceFloat getNumberPickerPreferenceFloat() {
            return (NumberPickerPreferenceFloat) this.getPreference();
        }

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            picker = view.findViewById(R.id.numpicker_pref);
            picker.setMinValue(0);
            picker.setMaxValue(getNumberPickerPreferenceFloat().maxValue - getNumberPickerPreferenceFloat().minValue);
            picker.setValue(currentValue - getNumberPickerPreferenceFloat().minValue);
            //API26以前だと表示がおかしい
            /*            picker.setFormatter(new NumberPicker.Formatter() {
                @Override
                public String format(int index) {
                    return String.format(Locale.ENGLISH,"%.3f",(index + getNumberPickerPreferenceFloat().minValue)/1000f);
                }
            });*/
            String[] displayWords= new String[getNumberPickerPreferenceFloat().maxValue - getNumberPickerPreferenceFloat().minValue+1];
            for(int i=0;i<displayWords.length;i++){
                displayWords[i] = String.format(Locale.ENGLISH,"%.3f",(i + getNumberPickerPreferenceFloat().minValue)/1000f);
            }
            picker.setDisplayedValues(displayWords);
            picker.setWrapSelectorWheel(getNumberPickerPreferenceFloat().wrapSelectorWheel);
        }

        @Override
        public void onDialogClosed(boolean b) {
            if (b) {
                int value = picker.getValue()+ getNumberPickerPreferenceFloat().minValue;
                if(getPreference().callChangeListener(value)) {
                    getNumberPickerPreferenceFloat().setValue(value);
                }
            }
        }
    }
}