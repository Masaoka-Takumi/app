package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * Sound FX設定ステータス.
 */
public class SoundFxSettingStatus extends SerialVersion {
    /** カラオケ設定有効.*/
    public boolean karaokeSettingEnabled;
    /** ライブシミュレーション設定有効.*/
    public boolean liveSimulationSettingEnabled;
    /** Small Car TA設定有効.*/
    public boolean smallCarTaSettingEnabled;
    /** Super轟設定有効.*/
    public boolean superTodorokiSettingEnabled;

    /**
     * コンストラクタ.
     */
    public SoundFxSettingStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        karaokeSettingEnabled = false;
        liveSimulationSettingEnabled = false;
        smallCarTaSettingEnabled = false;
        superTodorokiSettingEnabled = false;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("karaokeSettingEnabled", karaokeSettingEnabled)
                .add("liveSimulationSettingEnabled", liveSimulationSettingEnabled)
                .add("smallCarTaSettingEnabled", smallCarTaSettingEnabled)
                .add("superTodorokiSettingEnabled", superTodorokiSettingEnabled)
                .toString();
    }
}
