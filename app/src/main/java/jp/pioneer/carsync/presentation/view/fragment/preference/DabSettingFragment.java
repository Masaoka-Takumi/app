package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.TASetting;
import jp.pioneer.carsync.presentation.presenter.DabSettingPresenter;
import jp.pioneer.carsync.presentation.view.DabSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

public class DabSettingFragment extends AbstractPreferenceFragment<DabSettingPresenter, DabSettingView>
        implements DabSettingView {
    @Inject DabSettingPresenter mPresenter;
    private Preference mTa;
    private SwitchPreferenceCompat mServiceFollow;
    private SwitchPreferenceCompat mSoftLink;

    /**
     * コンストラクタ.
     */
    public DabSettingFragment() {
    }

    /**
     * 新規インスタンス取得.
     *
     * @param args 引き継ぎ情報
     * @return DabSettingFragment
     */
    public static DabSettingFragment newInstance(Bundle args) {
        DabSettingFragment fragment = new DabSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_dab, rootKey);

        mTa = findPreference(getString(R.string.key_dab_ta));
        mTa.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectTaSettingAction();
            return true;
        });

        mServiceFollow = (SwitchPreferenceCompat) findPreference(getString(R.string.key_dab_service_follow));
        mServiceFollow.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectServiceFollowAction((boolean) newValue);
            return true;
        });

        mSoftLink = (SwitchPreferenceCompat) findPreference(getString(R.string.key_dab_soft_link));
        mSoftLink.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectSoftLinkAction((boolean) newValue);
            return true;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_DAB;
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
    protected DabSettingPresenter getPresenter() {
        return mPresenter;
    }



    @Override
    public void setTaSetting(boolean isSupported, boolean isEnabled, TASetting setting) {
        mTa.setVisible(isSupported);
        mTa.setEnabled(isEnabled);
        switch (setting) {
            case DAB_RDS_TA_ON:
                mTa.setSummary(getString(R.string.set_351));
                break;
            case RDS_TA_ON:
                mTa.setSummary(getString(R.string.set_352));
                break;
            case OFF:
                mTa.setSummary(getString(R.string.set_353));
                break;
            default:
                break;
        }
    }

    @Override
    public void setServiceFollowSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mServiceFollow.setVisible(isSupported);
        mServiceFollow.setEnabled(isEnabled);
        mServiceFollow.setChecked(setting);
    }

    @Override
    public void setSoftLinkSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mSoftLink.setVisible(isSupported);
        mSoftLink.setEnabled(isEnabled);
        mSoftLink.setChecked(setting);
    }
}