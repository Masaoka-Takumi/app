package jp.pioneer.carsync.presentation.view;

/**
 * 車載機切断ダイアログのinterface.
 */
public interface SessionStoppedDialogView {

    /**
     * ダイアログ終了
     */
    void callbackClose();

    /**
     * スリープ禁止解除.
     */
    void onScreenOff();
}
