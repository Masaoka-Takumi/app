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
import jp.pioneer.carsync.domain.model.AttMuteSetting;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.domain.model.TimeFormatSetting;
import jp.pioneer.carsync.presentation.presenter.SystemPresenter;
import jp.pioneer.carsync.presentation.view.SystemView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * システム設定画面.
 */
public class SystemFragment extends AbstractPreferenceFragment<SystemPresenter, SystemView> implements SystemView {

    @Inject SystemPresenter mPresenter;
    private Preference mInitialSettings;
    private SwitchPreferenceCompat mBeepTone;
    private SwitchPreferenceCompat mAutoPi;
    private SwitchPreferenceCompat mAux;
    private SwitchPreferenceCompat mBtAudio;
    private SwitchPreferenceCompat mPandora;
    private SwitchPreferenceCompat mSpotify;
    private SwitchPreferenceCompat mPowerSaveMode;
    private SwitchPreferenceCompat mAppAutoLaunch;
    private SwitchPreferenceCompat mUsbAuto;
    private SwitchPreferenceCompat mDisplayOff;
    private Preference mAttMute;
    private Preference mDistanceUnit;
    private Preference mTimeFormatSetting;
    /**
     * コンストラクタ.
     */
    public SystemFragment() {
    }

    /**
     * 新規インスタンス取得.
     *
     * @param args 引き継ぎ情報
     * @return SystemFragment
     */
    public static SystemFragment newInstance(Bundle args) {
        SystemFragment fragment = new SystemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_system, rootKey);

        mInitialSettings = findPreference(getString(R.string.key_system_initial_settings));
        mInitialSettings.setOnPreferenceClickListener((preference) -> {
            getPresenter().onInitialSettingAction();
            return true;
        });
        mBeepTone = (SwitchPreferenceCompat) findPreference(getString(R.string.key_system_beep_tone_enabled));
        mBeepTone.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectBeepToneSettingAction((boolean) newValue);
            return true;
        });
        mAutoPi = (SwitchPreferenceCompat) findPreference(getString(R.string.key_system_auto_pi));
        mAutoPi.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectAutoPiSettingAction((boolean) newValue);
            return true;
        });
        mAux = (SwitchPreferenceCompat) findPreference(getString(R.string.key_system_aux_enabled));
        mAux.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectAuxSettingAction((boolean) newValue);
            return true;
        });
        mBtAudio = (SwitchPreferenceCompat) findPreference(getString(R.string.key_system_bt_audio_enabled));
        mBtAudio.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectBtAudioSettingAction((boolean) newValue);
            return true;
        });
        mPandora = (SwitchPreferenceCompat) findPreference(getString(R.string.key_system_pandora_enabled));
        mPandora.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectPandoraSettingAction((boolean) newValue);
            return true;
        });
        mSpotify = (SwitchPreferenceCompat) findPreference(getString(R.string.key_system_spotify_enabled));
        mSpotify.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectSpotifySettingAction((boolean) newValue);
            return true;
        });
        mPowerSaveMode = (SwitchPreferenceCompat) findPreference(getString(R.string.key_system_power_save_mode_enabled));
        mPowerSaveMode.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectPowerSaveModeSettingAction((boolean) newValue);
            return true;
        });
        mAppAutoLaunch = (SwitchPreferenceCompat) findPreference(getString(R.string.key_system_app_auto_launch));
        mAppAutoLaunch.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectAppAutoLaunchSettingAction((boolean) newValue);
            return true;
        });
        mUsbAuto = (SwitchPreferenceCompat) findPreference(getString(R.string.key_system_usb_auto));
        mUsbAuto.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectUsbAutoSettingAction((boolean) newValue);
            return true;
        });
        mDisplayOff = (SwitchPreferenceCompat) findPreference(getString(R.string.key_system_display_off));
        mDisplayOff.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectDisplayOffSettingAction((boolean) newValue);
            return true;
        });
        mAttMute = findPreference(getString(R.string.key_system_att_mute_setting));
        mAttMute.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectAttMuteSettingAction();
            return true;
        });
        mDistanceUnit = findPreference(getString(R.string.key_system_distance_unit));
        mDistanceUnit.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectDistanceUnitSettingAction();
            return true;
        });
        mTimeFormatSetting = findPreference(getString(R.string.key_system_time_format_setting));
        mTimeFormatSetting.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectTimeFormatSettingAction();
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
    protected SystemPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_SYSTEM;
    }

    // MARK - set list item

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInitialSettings(boolean isSupported, boolean isEnabled) {
        mInitialSettings.setVisible(isSupported);
        mInitialSettings.setEnabled(isEnabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBeepToneSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mBeepTone.setVisible(isSupported);
        mBeepTone.setEnabled(isEnabled);
        mBeepTone.setChecked(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoPiSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mAutoPi.setVisible(isSupported);
        mAutoPi.setEnabled(isEnabled);
        mAutoPi.setChecked(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAuxSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mAux.setVisible(isSupported);
        mAux.setEnabled(isEnabled);
        mAux.setChecked(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBtAudioSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mBtAudio.setVisible(isSupported);
        mBtAudio.setEnabled(isEnabled);
        mBtAudio.setChecked(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPandoraSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mPandora.setVisible(isSupported);
        mPandora.setEnabled(isEnabled);
        mPandora.setChecked(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpotifySetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mSpotify.setVisible(isSupported);
        mSpotify.setEnabled(isEnabled);
        mSpotify.setChecked(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPowerSaveModeSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mPowerSaveMode.setVisible(isSupported);
        mPowerSaveMode.setEnabled(isEnabled);
        mPowerSaveMode.setChecked(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAppAutoLaunchSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mAppAutoLaunch.setVisible(isSupported);
        mAppAutoLaunch.setEnabled(isEnabled);
        mAppAutoLaunch.setChecked(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUsbAutoSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mUsbAuto.setVisible(isSupported);
        mUsbAuto.setEnabled(isEnabled);
        mUsbAuto.setChecked(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisplayOffSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mDisplayOff.setVisible(isSupported);
        mDisplayOff.setEnabled(isEnabled);
        mDisplayOff.setChecked(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttMuteSetting(boolean isSupported, boolean isEnabled, @Nullable AttMuteSetting setting) {
        mAttMute.setVisible(isSupported);
        mAttMute.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(attMuteSetting -> {
                    switch (attMuteSetting) {
                        case ATT:
                            mAttMute.setSummary(getString(R.string.val_035));
                            break;
                        case MUTE:
                            mAttMute.setSummary(getString(R.string.val_034));
                            break;
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDistanceUnit(boolean isSupported, boolean isEnabled, @Nullable DistanceUnit setting) {
        mDistanceUnit.setVisible(isSupported);
        mDistanceUnit.setEnabled(isEnabled);

        if (setting == DistanceUnit.METER_KILOMETER) {
            mDistanceUnit.setSummary(getString(R.string.val_125));
        } else {
            mDistanceUnit.setSummary(getString(R.string.val_126));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeFormatSetting(boolean isSupported, boolean isEnabled, @Nullable TimeFormatSetting setting) {
        mTimeFormatSetting.setVisible(isSupported);
        mTimeFormatSetting.setEnabled(isEnabled);

        if (setting == TimeFormatSetting.TIME_FORMAT_12) {
            mTimeFormatSetting.setSummary(getString(R.string.val_250));
        } else {
            mTimeFormatSetting.setSummary(getString(R.string.val_249));
        }
    }
}
