package jp.pioneer.carsync.domain.model;

/**
 * Tuner（Radio）ソースのFunction種別.
 */
public enum TunerFunctionType {
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
     * FM Tuner Setting設定.
     * <p>
     * [設定値]<br>
     *  {@link FMTunerSetting#code}
     */
    FM_TUNER_SETTING(0x02),
    /**
     * REG広域設定.
     * <p>
     * [設定値]<br>
     *  {@code 0x00}:OFF、{@code 0x01}:ON
     */
    REG(0x03),
    /**
     * TA設定.
     * <p>
     * [設定値]<br>
     *  {@link TASetting#code}
     */
    TA(0x04),
    /**
     * AF設定.
     * <p>
     * [設定値]<br>
     *  {@code 0x00}:OFF、{@code 0x01}:ON
     */
    AF(0x05),
    /**
     * NEWS設定.
     * <p>
     * [設定値]<br>
     *  {@code 0x00}:OFF、{@code 0x01}:ON
     */
    NEWS(0x06),
    /**
     * ALARM設定.
     * <p>
     * [設定値]<br>
     *  {@code 0x00}:OFF、{@code 0x01}:ON
     */
    ALARM(0x07),
    /**
     * P.CH / MANUAL設定..
     * <p>
     * [設定値]<br>
     *  {@link PCHManualSetting#code}
     */
    PCH_MANUAL(0x08),
    /**
     * PTY SEARCH設定.
     * <p>
     * [設定値]<br>
     *  {@link PtySearchSetting#code}
     */
    PTY_SEARCH(0x09)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    TunerFunctionType(int code) {
        this.code = code;
    }
}
