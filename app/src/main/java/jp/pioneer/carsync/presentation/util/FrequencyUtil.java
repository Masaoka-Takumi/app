package jp.pioneer.carsync.presentation.util;

import android.content.Context;

import java.util.Locale;

import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;

/**
 * Created by NSW00_007906 on 2017/06/08.
 */

public class FrequencyUtil {
    public static final int UNIT_KHZ = 1;
    public static final int UNIT_MHZ1 = 2;
    public static final int UNIT_MHZ2 = 3;
    public static final int UNIT_MHZ3 = 4;

    public static String toString(Context context, long freq, TunerFrequencyUnit unit) {
        return toString(context, freq, unit, true);
    }

    public static String toString(Context context, long freq, TunerFrequencyUnit unit, boolean includeUnit) {
        if (unit == null) return null;
        float value = ((float) freq) / unit.divide;
        String format = "%." + unit.fraction + "f%s";
        return String.format(Locale.ENGLISH, format, value, includeUnit ? toString(context, unit) : "");
    }

    public static String format(Context context, long frequency, TunerFrequencyUnit unit, boolean includeUnit) {
        if (unit == null) return null;
        float value = (float) frequency;
        value = value / unit.divide;
        String format = "%." + unit.fraction + "f%s";
        return String.format(Locale.ENGLISH, format, value, includeUnit ? toString(context, unit) : "");
    }

    public static String format(long frequency, int unit, boolean includeUnit) {
        float value = (float) frequency;
        int divider, fraction;
        String label;
        switch (unit) {
            case UNIT_MHZ1:
                divider = 1000;
                fraction = 1;
                label = "MHz";
                break;
            case UNIT_MHZ2:
                divider = 1000;
                fraction = 2;
                label = "MHz";
                break;
            case UNIT_MHZ3:
                divider = 1000;
                fraction = 3;
                label = "MHz";
                break;
            case UNIT_KHZ:
            default:
                divider = 1;
                fraction = 0;
                label = "kHz";
                break;
        }
        value = value / divider;
        String format = "%." + fraction + "f%s";
        return String.format(Locale.ENGLISH, format, value, includeUnit ? label : "");
    }

    public static String toString(Context context, TunerFrequencyUnit unit) {
        return unit != null ? context.getString(unit.label) : null;
    }
}
