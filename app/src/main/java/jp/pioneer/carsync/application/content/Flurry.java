package jp.pioneer.carsync.application.content;

import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;

/**
 * Flurryクラス.
 */
public class Flurry {
    /** FlurryのAPI. */
    //public static final String FLURRY_API = "92XY6QSWZJ6BBJ52XC9X";
    public static final String FLURRY_API = "QC5CGWCTJZHDPSG27Q69";
    private static final boolean DBG = false;
    private final static String TAG = Flurry.class.getSimpleName();

    /**
     * セッション開始.
     *
     * @param context Context
     */
    public static void sessionStart(Context context) {
        if (!isSessionStarted()) {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .withLogLevel(Log.VERBOSE)
                    .withIncludeBackgroundSessionsInMetrics(true)
                    .build(context, FLURRY_API);
        }
    }

    /**
     * セッション開始しているか否か.
     */
    public static boolean isSessionStarted() {
        return FlurryAgent.isSessionActive();
    }

    public static AnalyticsEventSubmitter createEventSubmitter(Analytics.AnalyticsEvent event) {
        return new FlurryAnalyticsEventSubmitter(event);
    }

}
