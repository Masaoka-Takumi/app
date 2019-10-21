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

import jp.pioneer.carsync.R;

/**
 * A {@link android.preference.Preference} that displays a number picker as a dialog.
 */
public class NumberPickerPreference extends DialogPreference {
    private int minValue;
    private boolean wrapSelectorWheel;
    private static final int DEFAULT_value = 0;
    private static final int DEFAULT_maxValue = 0;
    private int currentValue;
    private int maxValue;
    private static final int DEFAULT_minValue = 0;
    public static final boolean DEFAULT_WRAP_SELECTOR_WHEEL = true;
    private final String defaultSummary;
    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        defaultSummary = getSummary().toString();
        //defaultSummary ="0";
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
        setSummary(String.format(defaultSummary, getValue()));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_value);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(currentValue) : (Integer) defaultValue);
    }

    public static class NumberPickerPreferenceDialogFragmentCompat
            extends PreferenceDialogFragmentCompat {
        private static final String SAVE_STATE_VALUE = "NumberPickerPreferenceDialogFragment.value";
        private NumberPicker picker;
        private int currentValue = 1;
        public NumberPickerPreferenceDialogFragmentCompat() {
        }

        public static NumberPickerPreferenceDialogFragmentCompat newInstance(String key) {
            NumberPickerPreferenceDialogFragmentCompat fragment =
                    new NumberPickerPreferenceDialogFragmentCompat();
            Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            return fragment;
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                currentValue = getNumberPickerPreference().getValue();
            } else {
                currentValue = savedInstanceState.getInt(SAVE_STATE_VALUE);
            }
        }

        public void onSaveInstanceState(@NonNull Bundle outState) {
            outState.putInt(SAVE_STATE_VALUE, currentValue);
        }

        private NumberPickerPreference getNumberPickerPreference() {
            return (NumberPickerPreference) this.getPreference();
        }

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            picker = view.findViewById(R.id.numpicker_pref);
            picker.setMaxValue(getNumberPickerPreference().maxValue);
            picker.setMinValue(getNumberPickerPreference().minValue);
            picker.setWrapSelectorWheel(getNumberPickerPreference().wrapSelectorWheel);
            picker.setValue(currentValue);
        }

        @Override
        public void onDialogClosed(boolean b) {
            if (b) {
                int value = picker.getValue();
                if(getPreference().callChangeListener(value)) {
                    getNumberPickerPreference().setValue(value);
                }
            }
        }
    }
}