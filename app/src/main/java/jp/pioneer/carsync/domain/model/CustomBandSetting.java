package jp.pioneer.carsync.domain.model;

import android.support.annotation.Size;

import com.google.common.base.MoreObjects;

/**
 * カスタムバンド設定.
 */
public class CustomBandSetting {
    /** カスタムEQ種別. */
    public final CustomEqType type;
    /** 31band設定. */
    @Size(31) public float[] bands;

    /**
     * コンストラクタ.
     *
     * @param type カスタムEQ種別
     */
    public CustomBandSetting(CustomEqType type){
        this.type = type;
        bands = new float[31];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("type", type)
                .add("bands", bands)
                .toString();
    }
}
