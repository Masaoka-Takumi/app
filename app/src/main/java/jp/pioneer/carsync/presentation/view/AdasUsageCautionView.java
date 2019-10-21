package jp.pioneer.carsync.presentation.view;

public interface AdasUsageCautionView {
    /**
     * 同意ボタン表示設定.
     *
     * @param visible 表示/非表示
     */
    void setVisibleAgreeBtn(boolean visible);
    /**
     * 同意ボタン有効設定.
     *
     * @param isEnabled 有効か否か
     */
    void setEnabledAgreeBtn(boolean isEnabled);

    void setPage(int page);
}
