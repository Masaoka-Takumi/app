package jp.pioneer.carsync.application.content;

import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Flurryクラス.
 */
public class Flurry {
    /** FlurryのAPI. */
    public static final String FLURRY_API = "92XY6QSWZJ6BBJ52XC9X";
    private static final boolean DBG = false;
    private final static String TAG = Flurry.class.getSimpleName();
    /**
     * セッション開始.
     *
     * @param context Context
     */
    public static void sessionStart(Context context) {
        if(!isSessionStarted()) {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .build(context, FLURRY_API);
        }
    }

    /**
     * セッション開始しているか否か.
     */
    public static boolean isSessionStarted() {
        return FlurryAgent.isSessionActive();
    }
    /**
     * 連携情報イベントの送信
     */
    public static void sendDeviceConnectedEvent(String accessoryId, String deviceName, String deviceDivision){
        Map<String, String> params = new HashMap<String, String>();

        Date now = new Date();
        String timestamp;
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        timestamp = dateFormat.format(now);
        params.put("timestamp", timestamp);
        params.put("accessoryId", accessoryId);
        params.put("deviceName", deviceName);
        params.put("deviceDivision", deviceDivision);

        //up to 10 params can be logged with each event
        FlurryAgent.logEvent("EventUser_DeviceConnected", params);
        if (DBG) Log.d(TAG, " - sendDeviceConnectedEvent() : timestamp = " + timestamp + " accessoryId = "+ accessoryId
                + " deviceName = "+ deviceName + " deviceDivision = "+ deviceDivision);
    }
}
