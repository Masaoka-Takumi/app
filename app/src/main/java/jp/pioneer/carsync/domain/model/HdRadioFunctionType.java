package jp.pioneer.carsync.domain.model;

/**
 * HD RadioソースのFunction種別.
 */
public enum HdRadioFunctionType {
    /**
     * BSM設定.
     * <p>
     * [設定値]<br>
     *  設定値はないため0固定。
     */
    BSM(0x00),
    /**
     * LOCAL設定.
     * <p>
     * [設定値]<br>
     *  {@link TASetting#code}
     */
    LOCAL(0x01),
    /**
     * HD SEEK設定.
     * <p>
     * [設定値]<br>
     *  {@code 0x00}:OFF、{@code 0x01}:ON
     */
    HD_SEEK(0x02),
    /**
     * BLENDING設定.
     * <p>
     * [設定値]<br>
     *  {@code 0x00}:OFF、{@code 0x01}:ON
     */
    BLENDING(0x03),
    /**
     * ACTIVE RADIO設定.
     * <p>
     * [設定値]<br>
     *  {@code 0x00}:OFF、{@code 0x01}:ON
     */
    ACTIVE_RADIO(0x04)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    HdRadioFunctionType(int code) {
        this.code = code;
    }
}
