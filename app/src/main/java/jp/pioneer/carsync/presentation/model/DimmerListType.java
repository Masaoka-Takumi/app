package jp.pioneer.carsync.presentation.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.DimmerSetting;

/**
 * Dimmerリスト種別.
 */
public enum DimmerListType {
    /** Dimmer OFF. */
    OFF(DimmerSetting.Dimmer.OFF, R.string.com_001),
    /** MANUAL. */
    MANUAL(DimmerSetting.Dimmer.MANUAL, R.string.set_129),
    /** ILLUMI LINE連動. */
    ILLUMI_LINE(DimmerSetting.Dimmer.ILLUMI_LINE, R.string.set_021),
    /** SYNC CLOCK連動設定. */
    SYNC_CLOCK(DimmerSetting.Dimmer.SYNC_CLOCK, R.string.set_212),
    /** SYNC CLOCK連動設定(開始時間). */
    SYNC_CLOCK_START(DimmerSetting.Dimmer.SYNC_CLOCK, R.string.set_202),
    /** SYNC CLOCK連動設定(終了時間). */
    SYNC_CLOCK_STOP(DimmerSetting.Dimmer.SYNC_CLOCK, R.string.set_205)
    ;

    /** Dimmer設定値 */
    public final DimmerSetting.Dimmer dimmer;

    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    /**
     * コンストラクタ.
     *
     * @param dimmer Dimmer設定値
     * @param label 表示用文字列リソースID
     */
    DimmerListType(DimmerSetting.Dimmer dimmer, @StringRes int label) {
        this.dimmer = dimmer;
        this.label = label;
    }
}
