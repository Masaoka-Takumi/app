package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.FMTunerSetting;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.TASetting;
import jp.pioneer.carsync.presentation.presenter.RadioFunctionSettingPresenter;
import jp.pioneer.carsync.presentation.view.RadioFunctionSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * ラジオ設定画面.
 */
public class RadioFunctionSettingFragment extends AbstractPreferenceFragment<RadioFunctionSettingPresenter, RadioFunctionSettingView>
        implements RadioFunctionSettingView {
    @Inject RadioFunctionSettingPresenter mPresenter;
    private Preference mFmTuner;
    private SwitchPreferenceCompat mRegion;
    private Preference mLocal;
    private SwitchPreferenceCompat mTa;
    private Preference mTaDab;
    private SwitchPreferenceCompat mAf;
    private SwitchPreferenceCompat mNews;
    private SwitchPreferenceCompat mAlarm;
    private Preference mPchManual;

    /**
     * コンストラクタ.
     */
    public RadioFunctionSettingFragment() {
    }

    /**
     * 新規インスタンス取得.
     *
     * @param args 引き継ぎ情報
     * @return RadioFunctionSettingFragment
     */
    public static RadioFunctionSettingFragment newInstance(Bundle args) {
        RadioFunctionSettingFragment fragment = new RadioFunctionSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_radio, rootKey);

        mFmTuner = findPreference(getString(R.string.key_radio_fm_setting));
        mFmTuner.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectFmTunerSettingAction();
            return true;
        });

        mRegion = (SwitchPreferenceCompat) findPreference(getString(R.string.key_radio_region_enabled));
        mRegion.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectRegionSettingAction((boolean) newValue);
            return true;
        });

        mLocal = findPreference(getString(R.string.key_radio_local));
        mLocal.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectLocalSettingAction();
            return true;
        });

        mTa = (SwitchPreferenceCompat) findPreference(getString(R.string.key_radio_ta));
        mTa.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectTaSettingAction((boolean) newValue);
            return true;
        });

        mTaDab = findPreference(getString(R.string.key_radio_ta_dab));
        mTaDab.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectTaSettingAction();
            return true;
        });

        mAf = (SwitchPreferenceCompat) findPreference(getString(R.string.key_radio_af_enabled));
        mAf.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectAfSettingAction((boolean) newValue);
            return true;
        });

        mNews = (SwitchPreferenceCompat) findPreference(getString(R.string.key_radio_news_enabled));
        mNews.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectNewsSettingAction((boolean) newValue);
            return true;
        });

        mAlarm = (SwitchPreferenceCompat) findPreference(getString(R.string.key_radio_alarm_enabled));
        mAlarm.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectAlarmSettingAction((boolean) newValue);
            return true;
        });

        mPchManual = findPreference(getString(R.string.key_radio_pch_manual));
        mPchManual.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectPchManualSettingAction();
            return true;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_RADIO;
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
    protected RadioFunctionSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setFmTunerSetting(boolean isSupported, boolean isEnabled, @Nullable FMTunerSetting setting) {
        mFmTuner.setVisible(isSupported);
        mFmTuner.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(type -> mFmTuner.setSummary(type.label));
    }

    @Override
    public void setRegionSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mRegion.setVisible(isSupported);
        mRegion.setEnabled(isEnabled);
        mRegion.setChecked(setting);
    }

    @Override
    public void setLocalSetting(boolean isSupported, boolean isEnabled, @Nullable LocalSetting setting) {
        mLocal.setVisible(isSupported);
        mLocal.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(type -> mLocal.setSummary(type.label));
    }

    @Override
    public void setTaSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mTa.setVisible(isSupported);
        mTa.setEnabled(isEnabled);
        mTa.setChecked(setting);
    }

    @Override
    public void setTaDabSetting(boolean isSupported, boolean isEnabled, TASetting setting) {
        mTaDab.setVisible(isSupported);
        mTaDab.setEnabled(isEnabled);
        switch (setting) {
            case DAB_RDS_TA_ON:
                mTaDab.setSummary(getString(R.string.set_351));
                break;
            case RDS_TA_ON:
                mTaDab.setSummary(getString(R.string.set_352));
                break;
            case OFF:
                mTaDab.setSummary(getString(R.string.set_353));
                break;
            default:
                break;
        }
    }

    @Override
    public void setAfSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mAf.setVisible(isSupported);
        mAf.setEnabled(isEnabled);
        mAf.setChecked(setting);
    }

    @Override
    public void setNewsSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mNews.setVisible(isSupported);
        mNews.setEnabled(isEnabled);
        mNews.setChecked(setting);
    }

    @Override
    public void setAlarmSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mAlarm.setVisible(isSupported);
        mAlarm.setEnabled(isEnabled);
        mAlarm.setChecked(setting);
    }

    @Override
    public void setPchManual(boolean isSupported, boolean isEnabled, @Nullable PCHManualSetting setting) {
        mPchManual.setVisible(isSupported);
        mPchManual.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(type -> {
                    switch (type) {
                        case MANUAL:
                            mPchManual.setSummary(getString(R.string.val_123));
                            break;
                        case PCH:
                            mPchManual.setSummary(getString(R.string.val_124));
                            break;
                    }
                });
    }
}
