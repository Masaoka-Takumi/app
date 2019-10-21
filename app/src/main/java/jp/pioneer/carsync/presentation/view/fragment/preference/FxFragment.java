package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.FxPresenter;
import jp.pioneer.carsync.presentation.view.FxView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * SoundFx設定画面
 */

public class FxFragment extends AbstractPreferenceFragment<FxPresenter, FxView> implements FxView {

    @Inject FxPresenter mPresenter;

    private Preference mEqSetting;
    private Preference mLiveSimulationSetting;
    private Preference mSuperTodorokiSetting;
    private Preference mSmallCarTaSetting;
    private Preference mKaraokeSetting;

    @Inject
    public FxFragment() {
    }

    public static FxFragment newInstance(Bundle args) {
        FxFragment fragment = new FxFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_fx, rootKey);

        mEqSetting = findPreference(getString(R.string.key_equalizer));
        mEqSetting.setOnPreferenceClickListener((preference) -> {
            getPresenter().onEqSettingAction();
            return true;
        });
        mLiveSimulationSetting = findPreference(getString(R.string.key_live_simulation));
        mLiveSimulationSetting.setOnPreferenceClickListener((preference) -> {
            getPresenter().onLiveSettingAction();
            return true;
        });
        mSuperTodorokiSetting = findPreference(getString(R.string.key_super_todoroki));
        mSuperTodorokiSetting.setOnPreferenceClickListener((preference) -> {
            getPresenter().onTodorokiSettingAction();
            return true;
        });
        mSmallCarTaSetting = findPreference(getString(R.string.key_small_car_ta));
        mSmallCarTaSetting.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSmallCarSettingAction();
            return true;
        });
        mKaraokeSetting = findPreference(getString(R.string.key_karaoke));
        mKaraokeSetting.setOnPreferenceClickListener((preference) -> {
            getPresenter().onKaraokeSettingAction();
            return true;
        });
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected FxPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_FX;
    }

    @Override
    public void setEqualizerSetting(boolean isSupported, boolean isEnabled) {
        mEqSetting.setVisible(isSupported);
        mEqSetting.setEnabled(isEnabled);
    }

    @Override
    public void setLiveSimulationSetting(boolean isSupported, boolean isEnabled) {
        mLiveSimulationSetting.setVisible(isSupported);
        mLiveSimulationSetting.setEnabled(isEnabled);
    }

    @Override
    public void setSuperTodorokiSetting(boolean isSupported, boolean isEnabled) {
        mSuperTodorokiSetting.setVisible(isSupported);
        mSuperTodorokiSetting.setEnabled(isEnabled);
    }

    @Override
    public void setSmallCarTaSetting(boolean isSupported, boolean isEnabled) {
        mSmallCarTaSetting.setVisible(isSupported);
        mSmallCarTaSetting.setEnabled(isEnabled);
    }

    @Override
    public void setKaraokeSetting(boolean isSupported, boolean isEnabled) {
        mKaraokeSetting.setVisible(isSupported);
        mKaraokeSetting.setEnabled(isEnabled);
    }
}
