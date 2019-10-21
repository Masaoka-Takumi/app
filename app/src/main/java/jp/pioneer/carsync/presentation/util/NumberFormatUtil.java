package jp.pioneer.carsync.presentation.util;

import java.util.Locale;

/**
 * Created by NSW00_906320 on 2017/06/27.
 */

public class NumberFormatUtil {
    public static String format(int value) {
        if (value > 0) {
            return "+" + value;
        } else {
            return String.valueOf(value);
        }
    }

    public static String formatFrequency(float freq, boolean withUnit) {
        String text, unit;
        if (freq >= 1000) {
            text = String.format(Locale.ENGLISH, "%f", freq / 1000);
            unit = "kHz";
        } else {
            text = String.format(Locale.ENGLISH, "%f", freq);
            unit = "Hz";
        }

        int pos = text.indexOf('.'); // 小数点を探す
        if (pos != -1) {
            int end = pos;
            // 文字列の右側から順に走査して0以外の数字が現れたらそこまでの文字列を切り出す
            for (int i = text.length() - 1; i > pos; i--) {
                if (text.charAt(i) != '0') {
                    // この0は削ってOK
                    // 0以外の数値が来たのでここで終わり
                    end = i + 1;
                    break;
                }
            }
            text = text.substring(0, end);
        }
        return text + (withUnit ? unit : "");
    }
}
