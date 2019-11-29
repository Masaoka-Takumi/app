package jp.pioneer.carsync.application.content;

public interface AnalyticsEventSubmitter {
    AnalyticsEventSubmitter with(Analytics.AnalyticsParam param, String value);
    AnalyticsEventSubmitter with(Analytics.AnalyticsParam param, long value);
    AnalyticsEventSubmitter with(Analytics.AnalyticsParam param, double value);
    void submit();
}
