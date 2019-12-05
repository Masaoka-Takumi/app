package jp.pioneer.carsync.presentation.event;

import jp.pioneer.carsync.application.content.Analytics;

public class SourceChangeReasonEvent {
    public final Analytics.SourceChangeReason reason;

    public SourceChangeReasonEvent(Analytics.SourceChangeReason reason) {
        this.reason = reason;
    }
}
