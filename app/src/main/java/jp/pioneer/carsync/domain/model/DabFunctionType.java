package jp.pioneer.carsync.domain.model;

/**
 * DABソースのFunction種別.
 */
public enum DabFunctionType {
    /**
     * TA設定.
     * <p>
     * [設定値]<br>
     *  {@link TASetting#code}
     */
    TA(0x00),
    /**
     * SERVICE FOLLOW ON/OFF設定.
     * <p>
     * [設定値]<br>
     *  {@code 0x00}:OFF、{@code 0x01}:ON
     */
    SERVICE_FOLLOW(0x01),
    /**
     * SOFTLINK設定.
     * <p>
     * [設定値]<br>
     *  {@code 0x00}:OFF、{@code 0x01}:ON
     */
    SOFTLINK(0x02)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    DabFunctionType(int code) {
        this.code = code;
    }
}
