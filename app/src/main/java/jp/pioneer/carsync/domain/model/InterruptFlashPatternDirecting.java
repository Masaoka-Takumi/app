package jp.pioneer.carsync.domain.model;

/**
 * 割り込み時発光パターン演出.
 */
public enum InterruptFlashPatternDirecting {
    /** OFF. */
    OFF(0x00),
    /** ON. */
    ON(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    InterruptFlashPatternDirecting(int code) {
        this.code = code;
    }
}
