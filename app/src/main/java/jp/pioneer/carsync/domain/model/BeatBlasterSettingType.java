package jp.pioneer.carsync.domain.model;

import jp.pioneer.mle.pmg.player.data.FilterStatus.BeatBlasterMode;

/**
 * Beat Blaster設定種別.
 */
public enum BeatBlasterSettingType {
    /** OFF. */
    OFF(BeatBlasterMode.OFF, BeatBlasterSetting.OFF),
    /** LOW. */
    LOW(BeatBlasterMode.LOW, BeatBlasterSetting.LOW),
    /** MIDDLE. */
    MID(BeatBlasterMode.DOP_HIGH, BeatBlasterSetting.HIGH),//TODO 不明
    /** HIGH. */
    HIGH(BeatBlasterMode.HIGH, BeatBlasterSetting.HIGH);

    /** Pmg lib beat blaster. */
    public final BeatBlasterMode mode;
    /** Car remote beat blaster. */
    public final BeatBlasterSetting setting;

    /**
     * コンストラクタ.
     *
     * @param mode    Pmg lib beat blaster
     * @param setting Car remote beat blaster
     */
    BeatBlasterSettingType(BeatBlasterMode mode, BeatBlasterSetting setting) {
        this.mode = mode;
        this.setting = setting;
    }

    /**
     * BeatBlasterSettingから取得.
     *
     * @param setting BeatBlasterSetting
     * @return BeatBlasterSettingに該当するBeatBlasterSettingType
     * @throws IllegalArgumentException 該当するものがない
     */
    public static BeatBlasterSettingType valueOf(BeatBlasterSetting setting) {
        for (BeatBlasterSettingType value : values()) {
            if (value.setting == setting) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + setting);
    }
}
