package jp.pioneer.carsync.presentation.view;

public interface AppConnectMethodDialogView {
    /**
     * ダイアログ終了
     */
    void callbackClose();

    /**
     * チェックボックス画像のCheck/UnCheck切り替え
     *
     * @param isChecked セットするチェック状態 {@code true}:Check　{@code false}:UnCheck
     */
    void setCheckBox(boolean isChecked);
}

