package jp.pioneer.carsync.presentation.view;

/**
 * YouTubeLink設定画面のinterface
 */
public interface YouTubeLinkSettingView {

    /**
     * YouTubeLink設定画面の有効/無効によるチェック状態設定
     *
     * @param isChecked {@code true}:有効　{@code false}:無効
     */
    void setYouTubeLinkSettingChecked(boolean isChecked);
}
