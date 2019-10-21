package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.AdasFunctionSetting;
import jp.pioneer.carsync.presentation.presenter.AdasWarningSettingPresenter;
import jp.pioneer.carsync.presentation.view.AdasWarningSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Created by NSW00_007906 on 2018/07/04.
 */

public class AdasWarningSettingFragment extends AbstractPreferenceFragment<AdasWarningSettingPresenter, AdasWarningSettingView>
        implements AdasWarningSettingView {

    @Inject AdasWarningSettingPresenter mPresenter;
    private SwitchPreferenceCompat mLdw;
    private Preference mLdwSensitivity;
    private SwitchPreferenceCompat mPcw;
    private Preference mPcwSensitivity;
    private SwitchPreferenceCompat mFcw;
    private Preference mFcwSensitivity;

    /**
     * コンストラクタ
     */
    public AdasWarningSettingFragment(){
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AdasWarningSettingFragment
     */
    public static AdasWarningSettingFragment newInstance(Bundle args) {
        AdasWarningSettingFragment fragment = new AdasWarningSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_adas_warning, rootKey);

        mLdw = (SwitchPreferenceCompat) findPreference(getString(R.string.key_adas_function_ldw));
        mLdw.setIcon(R.drawable.p1515_adasseticon_6);
        mLdw.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectLdwAction((boolean) newValue);
            return true;
        });

        mLdwSensitivity = findPreference(getString(R.string.key_adas_function_ldw_sensitivity));
        mLdwSensitivity.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectLdwSensitivityAction();
            return true;
        });

        mPcw = (SwitchPreferenceCompat) findPreference(getString(R.string.key_adas_function_pcw));
        mPcw.setIcon(R.drawable.p1514_adasseticon_5);
        mPcw.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectPcwAction((boolean) newValue);
            return true;
        });
        mPcw.setVisible(false);

        mPcwSensitivity = findPreference(getString(R.string.key_adas_function_pcw_sensitivity));
        mPcwSensitivity.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectPcwSensitivityAction();
            return true;
        });
        mPcwSensitivity.setVisible(false);

        mFcw = (SwitchPreferenceCompat) findPreference(getString(R.string.key_adas_function_fcw));
        mFcw.setIcon(R.drawable.p1513_adasseticon_4);
        mFcw.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectFcwAction((boolean) newValue);
            return true;
        });

        mFcwSensitivity = findPreference(getString(R.string.key_adas_function_fcw_sensitivity));
        mFcwSensitivity.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectFcwSensitivityAction();
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
    protected AdasWarningSettingPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.ADAS_WARNING_SETTING;
    }


    @Override
    public void setLdwSetting(AdasFunctionSetting setting) {
        mLdw.setChecked(setting.settingEnabled);
        mLdwSensitivity.setEnabled(setting.settingEnabled);
        mLdwSensitivity.setSummary(setting.functionSensitivity.label);
    }

    @Override
    public void setFcwSetting(AdasFunctionSetting setting) {
        mFcw.setChecked(setting.settingEnabled);
        mFcwSensitivity.setEnabled(setting.settingEnabled);
        mFcwSensitivity.setSummary(setting.functionSensitivity.label);
    }

    @Override
    public void setPcwSetting(AdasFunctionSetting setting) {
        mPcw.setChecked(setting.settingEnabled);
        mPcwSensitivity.setEnabled(setting.settingEnabled);
        mPcwSensitivity.setSummary(setting.functionSensitivity.label);
    }

}