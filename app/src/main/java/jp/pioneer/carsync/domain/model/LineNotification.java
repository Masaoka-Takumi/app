package jp.pioneer.carsync.domain.model;

import javax.inject.Inject;

/**
 * LINEの通知.
 */
public class LineNotification extends DefaultNotificationImpl {
    /**
     * コンストラクタ
     */
    @Inject
    public LineNotification() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadTarget() {
        if ("LINE".equals(getTitle()) && getNotification().tickerText.toString().matches(".*:.*")) {
            return false;
        }

        return super.isReadTarget();
    }
}
