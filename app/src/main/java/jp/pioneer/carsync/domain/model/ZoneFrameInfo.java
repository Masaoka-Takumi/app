package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * ゾーンフレーム情報.
 */
public class ZoneFrameInfo {

    /** ゾーン2情報. */
    public ZoneColorSpec zone2 = new ZoneColorSpec();

    /** ゾーン3情報. */
    public ZoneColorSpec zone3 = new ZoneColorSpec();

    /** 発光時間(ms) */
    public int duration;

    /**
     * コンストラクタ.
     */
    public ZoneFrameInfo() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        zone2.reset();
        zone3.reset();
        duration = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("zone2", zone2)
                .add("zone3", zone3)
                .add("duration", duration)
                .toString();
    }
}
