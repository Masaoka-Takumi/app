package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.view.YouTubeLinkSettingView;

/**
 * YouTubeLink設定画面のPresenter
 */
@PresenterLifeCycle
public class YouTubeLinkSettingPresenter extends Presenter<YouTubeLinkSettingView> {

    @Inject AppSharedPreference mPreference;

    @Inject
    public YouTubeLinkSettingPresenter() {
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setYouTubeLinkSettingChecked(mPreference.isYouTubeLinkSettingEnabled());
        });
    }

    /**
     * YouTubeLink設定の有効/無効のPreference保存処理
     *
     * @param isEnabled {@code true}:有効　{@code false}:無効
     */
    public void onYouTubeLinkSettingChange(boolean isEnabled){
        mPreference.setYouTubeLinkSettingEnabled(isEnabled);
    }
}
