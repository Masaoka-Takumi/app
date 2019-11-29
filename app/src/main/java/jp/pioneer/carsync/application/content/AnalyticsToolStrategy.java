package jp.pioneer.carsync.application.content;

import android.content.Context;

public interface AnalyticsToolStrategy {
    void startSession(Context context);
    AnalyticsEventSubmitter createEventSubmitter(Analytics.AnalyticsEvent event);
}
