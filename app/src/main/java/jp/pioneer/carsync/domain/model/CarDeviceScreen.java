package jp.pioneer.carsync.domain.model;

/**
 * 車載機画面種別.
 */
public enum CarDeviceScreen {
    /** イルミプレビュー画面(BT PHONE COLOR). */
    ILLUMI_PREVIEW_PHONE_COLOR(0x00),
    /** イルミプレビュー画面(メッセージ受信通知COLOR). */
    ILLUMI_PREVIEW_MESSAGE_COLOR(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    CarDeviceScreen(int code) {
        this.code = code;
    }
}
