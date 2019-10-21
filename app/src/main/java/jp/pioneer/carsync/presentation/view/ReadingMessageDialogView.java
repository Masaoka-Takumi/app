package jp.pioneer.carsync.presentation.view;

/**
 * Radio BSM Dialogの抽象クラス
 */

public interface ReadingMessageDialogView {

    /**
     * タイトル設定
     */
    void setTitleText(int res);

    /**
     * アニメーション設定
     */
    void setAnimation(String type);

    /**
     * ダイアログ終了
     */
    void callbackClose();
}
