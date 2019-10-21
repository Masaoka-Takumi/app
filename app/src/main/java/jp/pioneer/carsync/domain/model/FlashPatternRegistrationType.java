package jp.pioneer.carsync.domain.model;

/**
 * 発光パターン登録種別.
 */
public enum FlashPatternRegistrationType {
    /** 通常演出. */
    NORMAL(0x00),
    /** 割り込み演出. */
    INTERRUPT(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    FlashPatternRegistrationType(int code) {
        this.code = code;
    }
}
