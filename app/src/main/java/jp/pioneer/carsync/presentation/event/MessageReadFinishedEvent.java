package jp.pioneer.carsync.presentation.event;

import jp.pioneer.carsync.domain.model.Notification;

public class MessageReadFinishedEvent {
    public String packageName;
    public MessageReadFinishedEvent(String packageName) {
        this.packageName = packageName;
    }

}
