package jp.pioneer.carsync.presentation.view;

public interface YouTubeLinkCautionView {

    /**
     * チェックボックス画像のCheck/UnCheck切り替え
     * @param isChecked セットするチェック状態 {@code true}:Check　{@code false}:UnCheck
     */
    void updateCheckBox(boolean isChecked);

    /**
     * 画面を閉じる(割り込みによるもの)
     */
    void callbackCloseResetLastSource();
}
