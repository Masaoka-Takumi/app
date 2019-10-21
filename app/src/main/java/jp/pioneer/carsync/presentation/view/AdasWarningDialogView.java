package jp.pioneer.carsync.presentation.view;

/**
 * ADAS警報表示の抽象クラス
 */

public interface AdasWarningDialogView {

    /**
     * ADAS画像設定
     * @param resource リソース名
     */
    void setAdasImage(int resource);

    /**
     * ADAS警告文設定
     * @param text リソース名
     */
    void setAdasText(String text);

    /**
     * ダイアログ終了
     */
    void callbackClose();
}
