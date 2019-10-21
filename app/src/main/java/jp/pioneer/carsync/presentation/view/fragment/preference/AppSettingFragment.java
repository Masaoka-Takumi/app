package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AppSettingPresenter;
import jp.pioneer.carsync.presentation.view.AppSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * App設定画面
 */
public class AppSettingFragment extends AbstractPreferenceFragment<AppSettingPresenter, AppSettingView>
        implements AppSettingView {

    @Inject AppSettingPresenter mPresenter;
    private SwitchPreferenceCompat mShortCutButton;
    private Preference mAlbumArtButton;
    private Preference mGenreListButton;
    private Preference mPlaylistButton;
    private SwitchPreferenceCompat mAppServiceResident;
    /**
     * コンストラクタ
     */
    public AppSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AppSettingFragment
     */
    public static AppSettingFragment newInstance(Bundle args) {
        AppSettingFragment fragment = new AppSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_app, rootKey);

        mShortCutButton = (SwitchPreferenceCompat) findPreference("short_cut_button_enabled");
        mShortCutButton.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onShortCutButtonChange((boolean) newValue);
            return true;
        });

        mAlbumArtButton = findPreference("album_art_enabled");
        mAlbumArtButton.setOnPreferenceClickListener((preference) -> {
            getPresenter().onAlbumArtChange();
            return true;
        });

        mGenreListButton = findPreference("genre_card_enabled");
        mGenreListButton.setOnPreferenceClickListener((preference) -> {
            getPresenter().onGenreCardChange();
            return true;
        });

        mPlaylistButton = findPreference("playlist_card_enabled");
        mPlaylistButton.setOnPreferenceClickListener((preference) -> {
            getPresenter().onPlaylistChange();
            return true;
        });

        mAppServiceResident = (SwitchPreferenceCompat) findPreference("app_service_resident");
        mAppServiceResident.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAppServiceResidentChange((boolean) newValue);
            return true;
        });

    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AppSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_APP;
    }

    /**
     * ShortCutButton設定の有効/無効
     *
     * @param isEnabled {@code true}:有効。｛@code false}:無効。
     */
    @Override
    public void setShortCutSettingEnabled(boolean isEnabled) {
        mShortCutButton.setEnabled(isEnabled);
    }

    /**
     * ShortCutButtonのチェック状態設定
     *
     * @param isEnabled {@code true}:有効。｛@code false}:無効。
     */
    @Override
    public void setShortCutEnabled(boolean isEnabled) {
        mShortCutButton.setChecked(isEnabled);
    }

    /**
     * 真偽値によってAlbumArt表示を切り替える
     *
     * @param isEnabled {@code true}:AlbumArt表示。｛@code false}:List表示。
     */
    @Override
    public void setAlbumArtEnabled(boolean isEnabled) {
        if (isEnabled){
            mAlbumArtButton.setSummary(R.string.val_127);
        } else {
            mAlbumArtButton.setSummary(R.string.val_128);
        }
    }

    /**
     * 真偽値によってGenreCard表示を切り替える
     *
     * @param isEnabled {@code true}:Card表示。｛@code false}:List表示。
     */
    @Override
    public void setGenreCardEnabled(boolean isEnabled) {
        if (isEnabled){
            mGenreListButton.setSummary(R.string.val_129);
        } else {
            mGenreListButton.setSummary(R.string.val_128);
        }
    }

    /**
     * 真偽値によってPlaylistCard表示を切り替える
     *
     * @param isEnabled {@code true}:Card表示。｛@code false}:List表示。
     */
    @Override
    public void setPlaylistCardEnabled(boolean isEnabled) {
        if (isEnabled){
            mPlaylistButton.setSummary(R.string.val_129);
        } else {
            mPlaylistButton.setSummary(R.string.val_128);
        }
    }

    /**
     * 常時待ち受け設定の表示/非表示
     *
     * @param isEnabled {@code true}:有効。｛@code false}:無効。
     */
    @Override
    public void setAppServiceResidentEnabled(boolean isEnabled) {
        mAppServiceResident.setChecked(isEnabled);
    }

}
