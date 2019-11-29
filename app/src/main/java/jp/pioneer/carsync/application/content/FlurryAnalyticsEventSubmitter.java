package jp.pioneer.carsync.application.content;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryEventRecordStatus;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class FlurryAnalyticsEventSubmitter implements AnalyticsEventSubmitter {
    private Analytics.AnalyticsEvent event;
    private Map<String, String> params = new HashMap<>();

    public FlurryAnalyticsEventSubmitter(Analytics.AnalyticsEvent event) {
        this.event = event;
    }

    public AnalyticsEventSubmitter with(Analytics.AnalyticsParam param, String value) {
        this.params.put(param.name, value);
        return this;
    }

    public AnalyticsEventSubmitter with(Analytics.AnalyticsParam param, long value) {
        this.params.put(param.name, String.valueOf(value));
        return this;
    }

    public AnalyticsEventSubmitter with(Analytics.AnalyticsParam param, double value) {
        this.params.put(param.name, String.valueOf(value));
        return this;
    }

    public void submit() {
        FlurryEventRecordStatus flurryEventRecordStatus = FlurryAgent.logEvent(this.event.name, this.params);
        Timber.d("Event=" + event.name + ",Param=" + params.toString() + ",evStatus=" + flurryEventRecordStatus);
    }
}