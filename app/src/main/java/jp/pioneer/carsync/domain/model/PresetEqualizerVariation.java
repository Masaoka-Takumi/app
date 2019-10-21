package jp.pioneer.carsync.domain.model;

/**
 * PRESET EQ 仕向け.
 */
public enum PresetEqualizerVariation {
    /** OTHER. */
    OTHER(0),
    /** BRAZIL. */
    BRAZIL(1),
    /** INDIA. */
    INDIA(2)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    PresetEqualizerVariation(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するPresetEqualizerVariation
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static PresetEqualizerVariation valueOf(int code) {
        for (PresetEqualizerVariation value : values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
