package jp.pioneer.carsync.presentation.view;

/**
 * 起動時PrivacyPolicy画面の抽象クラス
 */

public interface OpeningPrivacyPolicyView {
    /**
     * 同意ボタン有効設定.
     *
     * @param isEnabled 有効か否か
     */
    void setEnabledAgreeBtn(boolean isEnabled);
}
