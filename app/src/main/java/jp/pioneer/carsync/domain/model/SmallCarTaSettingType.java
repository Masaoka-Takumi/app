package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Small Car TA 設定種別.
 */
public enum SmallCarTaSettingType {
    /** OFF. */
    OFF(0x00, R.string.com_001, new int[4], new int[4]),
    /** Compact. */
    COMPACT(0x01, R.string.set_136, new int[]{46, 59, 41, 52}, new int[]{59, 46, 52, 41}),
    /** Standard. */
    STANDARD(0x02, R.string.set_040, new int[]{48, 62, 42, 56}, new int[]{62, 48, 56, 42}),
    /** Intermediate. */
    INTERMEDIATE(0x03, R.string.set_210, new int[]{50, 66, 42, 60}, new int[]{66, 50, 60, 42}),
    /** SUV & Premium. */
    SUV_PREMIUM(0x04, R.string.set_281, new int[]{52, 70, 42, 63}, new int[]{70, 52, 63, 42});

    /** プロトコルでの定義値. */
    public final int code;

    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    /** ポジションがleftの場合のステップ値([0]:FrontLeft [1]:FrontRight [2]:RearLeft [3]:RearRight) */
    public final int[] leftStepValue;

    /** ポジションがrightの場合のステップ値([0]:FrontLeft [1]:FrontRight [2]:RearLeft [3]:RearRight) */
    public final int[] rightStepValue;

    /**
     * コンストラクタ.
     *
     * @param code           プロトコルでの定義値
     * @param leftStepValue  ポジションがleftの場合のステップ値
     * @param rightStepValue ポジションがrightの場合のステップ値
     */
    SmallCarTaSettingType(int code, @StringRes int label, int[] leftStepValue, int[] rightStepValue) {
        this.code = code;
        this.label = label;
        this.leftStepValue = leftStepValue;
        this.rightStepValue = rightStepValue;
    }

    /**
     * プロトコルでの定義値取得.
     *
     * @return プロトコルでの定義値
     */
    public int getCode() {
        return code;
    }

    /**
     * 表示用文字列リソースID取得.
     *
     * @return 表示用文字列リソースID
     */
    @StringRes
    public int getLabel() {
        return label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSmallCarTaSettingType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SmallCarTaSettingType valueOf(byte code) {
        for (SmallCarTaSettingType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
