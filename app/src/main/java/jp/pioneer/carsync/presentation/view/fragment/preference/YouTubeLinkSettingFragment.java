package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.YouTubeLinkSettingPresenter;
import jp.pioneer.carsync.presentation.view.YouTubeLinkSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * YouTubeLink設定画面
 */
public class YouTubeLinkSettingFragment
        extends AbstractPreferenceFragment<YouTubeLinkSettingPresenter, YouTubeLinkSettingView>
        implements YouTubeLinkSettingView {

    @Inject YouTubeLinkSettingPresenter mPresenter;
    private SwitchPreferenceCompat mYouTubeLinkSwitch;

    @Inject
    public YouTubeLinkSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return YouTubeLinkSettingFragment
     */
    public static YouTubeLinkSettingFragment newInstance(Bundle args) {
        YouTubeLinkSettingFragment fragment = new YouTubeLinkSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_setting_youtube_link, rootKey);

        mYouTubeLinkSwitch = (SwitchPreferenceCompat) findPreference("youtube_link_setting_enabled");
        mYouTubeLinkSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getPresenter().onYouTubeLinkSettingChange((boolean) (newValue));
                return true;
            }
        });
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected YouTubeLinkSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.YOUTUBE_LINK_SETTING;
    }

    /**
     * YouTubeLink設定画面の有効/無効によるチェック状態設定
     *
     * @param isChecked {@code true}:有効　{@code false}:無効
     */
    @Override
    public void setYouTubeLinkSettingChecked(boolean isChecked) {
        mYouTubeLinkSwitch.setChecked(isChecked);
    }
}
