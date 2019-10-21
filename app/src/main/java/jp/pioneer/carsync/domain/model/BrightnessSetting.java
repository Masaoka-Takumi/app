package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * BRIGHTNESS設定.
 */
public class BrightnessSetting {
    /** 最小値. */
    public int min;
    /** 最大値. */
    public int max;
    /** 現在の設定値. */
    public int current;

    /**
     * コンストラクタ.
     */
    public BrightnessSetting() {
        reset();
    }

    /**
     * 値設定.
     *
     * @param min 最小値
     * @param max 最大値
     * @param current 現在の設定値
     */
    public void setValue(int min, int max, int current) {
        this.max = max;
        this.min = min;
        this.current = current;
    }

    /**
     * リセット.
     */
    public void reset() {
        max = 0;
        min = 0;
        current = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("min", min)
                .add("max", max)
                .add("current", current)
                .toString();
    }
}
