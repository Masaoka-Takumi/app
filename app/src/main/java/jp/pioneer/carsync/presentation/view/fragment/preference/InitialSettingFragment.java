package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.AmStep;
import jp.pioneer.carsync.domain.model.FmStep;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.RearOutputPreoutOutputSetting;
import jp.pioneer.carsync.domain.model.RearOutputSetting;
import jp.pioneer.carsync.presentation.presenter.InitialSettingPresenter;
import jp.pioneer.carsync.presentation.view.InitialSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * 初期設定画面.
 */
public class InitialSettingFragment extends AbstractPreferenceFragment<InitialSettingPresenter, InitialSettingView>
        implements InitialSettingView {

    @Inject InitialSettingPresenter mPresenter;
    private Preference mMenuDisplayLanguage;
    private Preference mFmStep;
    private Preference mAmStep;
    private Preference mRearOutputPreoutOutput;
    private Preference mRearOutput;

    /**
     * コンストラクタ
     */
    public InitialSettingFragment() {
    }

    /**
     * 新規インスタンス取得.
     *
     * @param args 引き継ぎ情報
     * @return SystemFragment
     */
    public static InitialSettingFragment newInstance(Bundle args) {
        InitialSettingFragment fragment = new InitialSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_initial, rootKey);

        mMenuDisplayLanguage = findPreference(getString(R.string.key_initial_menu_display_language));
        mMenuDisplayLanguage.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectMenuDisplayLanguage();
            return true;
        });
        mFmStep = findPreference(getString(R.string.key_initial_fm_step));
        mFmStep.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectFmStepSetting();
            return true;
        });
        mAmStep = findPreference(getString(R.string.key_initial_am_step));
        mAmStep.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectAmStepSetting();
            return true;
        });
        mRearOutputPreoutOutput = findPreference(getString(R.string.key_initial_rear_output_preout_output));
        mRearOutputPreoutOutput.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectRearOutputPreoutOutput();
            return true;
        });
        mRearOutput = findPreference(getString(R.string.key_initial_rear_output));
        mRearOutput.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectRearOutput();
            return true;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    protected InitialSettingPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_SYSTEM_INITIAL;
    }

    // MARK - set list item

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMenuDisplayLanguageSetting(boolean isSupported, boolean isEnabled, @Nullable MenuDisplayLanguageType setting) {
        mMenuDisplayLanguage.setVisible(isSupported);
        mMenuDisplayLanguage.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(step -> mMenuDisplayLanguage.setSummary(getString(step.label)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFmStepSetting(boolean isSupported, boolean isEnabled, @Nullable FmStep setting) {
        mFmStep.setVisible(isSupported);
        mFmStep.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(step -> mFmStep.setSummary(getString(step.label)));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAmStepSetting(boolean isSupported, boolean isEnabled, @Nullable AmStep setting) {
        mAmStep.setVisible(isSupported);
        mAmStep.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(step -> mAmStep.setSummary(getString(step.label)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRearOutputPreoutOutputSetting(boolean isSupported, boolean isEnabled, @Nullable RearOutputPreoutOutputSetting setting) {
        mRearOutputPreoutOutput.setVisible(isSupported);
        mRearOutputPreoutOutput.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(output -> mRearOutputPreoutOutput.setSummary(getString(output.label)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRearOutputSetting(boolean isSupported, boolean isEnabled, @Nullable RearOutputSetting setting) {
        mRearOutput.setVisible(isSupported);
        mRearOutput.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(output -> mRearOutput.setSummary(getString(output.label)));
    }
}
