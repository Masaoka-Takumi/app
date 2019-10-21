package jp.pioneer.carsync.domain.model;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * Special EQ設定種別.
 */
//TODO プロトコルに実装されたら更新の必要有
public enum SpecialEqType {
    /**
     * UNKNOWN.
     * <p>
     * SPECIAL EQはプロトコル上定義されているが、
     * 種別及びその定義値については決まっていないためUNKNOWNを設定。
     */
    UNKNOWN(0xFF)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    SpecialEqType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     * <p>
     * プロトコルの定義値に存在しないbyte値が指定された場合はUNKNOWNを返す
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSpecialEqualizerSettingType
     */
    public static SpecialEqType valueOf(byte code) {
        for (SpecialEqType value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        return UNKNOWN;
    }

}
