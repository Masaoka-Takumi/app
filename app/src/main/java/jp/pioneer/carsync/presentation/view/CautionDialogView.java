package jp.pioneer.carsync.presentation.view;

/**
 * Caution画面のinterface
 */

public interface CautionDialogView {

    /**
     * ダイアログ終了
     */
    void callbackClose();

    /**
     * スリープ状態にしない
     */
    void setScreenOn();

    /**
     * パーミッションの要求
     *
     * @param permissions パーミッション
     */
    void requestPermissions(String... permissions);
}
