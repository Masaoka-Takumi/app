package jp.pioneer.carsync.domain.model;

/**
 * 遷移方向.
 */
public enum TransitionDirection {
    /** 入場. */
    ENTER(0x00),
    /** 退場. */
    EXIT(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    TransitionDirection(int code) {
        this.code = code;
    }
}
