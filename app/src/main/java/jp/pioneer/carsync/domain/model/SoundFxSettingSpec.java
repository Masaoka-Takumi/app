package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Sound FX設定スペック.
 */
public class SoundFxSettingSpec {
    /** カラオケ設定対応.*/
    public boolean karaokeSettingSupported;
    /** ライブシミュレーション設定対応.*/
    public boolean liveSimulationSettingSupported;
    /** Small Car TA設定対応.*/
    public boolean smallCarTaSettingSupported;
    /** Super轟設定対応.*/
    public boolean superTodorokiSettingSupported;
    /** サポートイコライザー群. */
    public List<SoundFxSettingEqualizerType> supportedEqualizers;

    /**
     * コンストラクタ.
     */
    public SoundFxSettingSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        karaokeSettingSupported = false;
        liveSimulationSettingSupported = false;
        smallCarTaSettingSupported = false;
        superTodorokiSettingSupported = false;
        supportedEqualizers = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("karaokeSettingSupported", karaokeSettingSupported)
                .add("liveSimulationSettingSupported", liveSimulationSettingSupported)
                .add("smallCarTaSettingSupported", smallCarTaSettingSupported)
                .add("superTodorokiSettingSupported", superTodorokiSettingSupported)
                .add("supportedEqualizers", supportedEqualizers)
                .toString();
    }
}
