package jp.pioneer.carsync.presentation.util;
import android.text.TextUtils;

/**
 * Created by yangming on 18-9-11.
 */
public class CpuInfo {

    public String maxFreq;

    public String minFreq;

    public String curFreq;

    public CpuInfo(String maxFreq, String minFreq, String curFreq) {
        if (TextUtils.isEmpty(maxFreq)) {
            this.maxFreq = "0";
        } else {
            this.maxFreq = maxFreq;
        }
        if (TextUtils.isEmpty(minFreq)) {
            this.minFreq = "0";
        } else {
            this.minFreq = minFreq;
        }
        if (TextUtils.isEmpty(curFreq)) {
            this.curFreq = "0";
        } else {
            this.curFreq = curFreq;
        }
    }
}
