package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.preference.SwitchPreferenceCompat;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.AdasWarningStatus;
import jp.pioneer.carsync.domain.model.SmartPhoneInterruption;
import jp.pioneer.carsync.presentation.model.TipsContentsEndpoint;
import jp.pioneer.carsync.presentation.presenter.DebugSettingPresenter;
import jp.pioneer.carsync.presentation.view.DebugSettingView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.widget.NumberPickerPreference;
import jp.pioneer.carsync.presentation.view.widget.NumberPickerPreferenceFloat;

/**
 * デバッグ設定の画面
 */

public class DebugSettingFragment extends AbstractPreferenceFragment<DebugSettingPresenter, DebugSettingView> implements DebugSettingView {
    @Inject DebugSettingPresenter mPresenter;
    private SwitchPreferenceCompat mLogEnabled;
    private Preference mClassicBTLinkKey;
    private Preference mBLELinkKey;
    private SwitchPreferenceCompat mImpactDetectionDebugModeEnabled;
    private Preference mAdasWarning;
    private Preference mSmartPhoneInterrupt;
    private Preference mGoHome;
    private SwitchPreferenceCompat mDebugSpecialEq;
    private Preference mTipsServer;
    private SeekBarPreference mDebugPmgVolumeValue;
    private SwitchPreferenceCompat mVersion1_1Function;
    private SwitchPreferenceCompat mAdasSimJudgement;
    private Preference mAdasTrialReset;
    private SwitchPreferenceCompat mAdasPurchased;
    private SwitchPreferenceCompat mAdasConfigured;
    private SwitchPreferenceCompat mAdasPseudoCooperation;
    private SeekBarPreference mVRDelayTime;
    private SwitchPreferenceCompat mDebugRunningStatus;
    private Preference mHomeCenterView;
    private NumberPickerPreference mAdasCarSpeed;
    private NumberPickerPreference mAdasLdwMinSpeed;
    private NumberPickerPreference mAdasLdwMaxSpeed;
    private NumberPickerPreference mAdasPcwMinSpeed;
    private NumberPickerPreference mAdasPcwMaxSpeed;
    private NumberPickerPreference mAdasFcwMinSpeed;
    private NumberPickerPreference mAdasFcwMaxSpeed;
    private NumberPickerPreferenceFloat mAdasAccelerateY;
    private NumberPickerPreferenceFloat mAdasAccelerateZMin;
    private NumberPickerPreferenceFloat mAdasAccelerateZMax;
    private NumberPickerPreference mAdasFps;
    private SwitchPreferenceCompat mAdasCameraPreview;
    private SwitchPreferenceCompat mAlexaSimJudgement;
    private SwitchPreferenceCompat mSmartPhoneControlComand;
    private SeekBarPreference mDeviceVolume;
    private final static SparseArrayCompat<SmartPhoneInterruption> INTERRUPT_LIST_ITEMS = new SparseArrayCompat<SmartPhoneInterruption>() {{
        put(0, SmartPhoneInterruption.LOW);
        put(1, SmartPhoneInterruption.MIDDLE);
        put(2, SmartPhoneInterruption.HIGH);
        put(3, null);
    }};

    /**
     * コンストラクタ
     */
    public DebugSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return ThemeFragment
     */
    public static DebugSettingFragment newInstance(Bundle args) {
        DebugSettingFragment fragment = new DebugSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_setting_debug, rootKey);
        mLogEnabled = (SwitchPreferenceCompat) findPreference("setting_debug_log_enabled");
        mLogEnabled.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onLogEnabledAction((boolean)newValue);
            return true;
        });
        mClassicBTLinkKey = findPreference("setting_debug_classic_bt_link_key");
        mClassicBTLinkKey.setOnPreferenceClickListener((preference) -> {
            getPresenter().onClassicBTLinkKeyAction();
            return true;
        });
        mBLELinkKey = findPreference("setting_debug_ble_link_key");
        mBLELinkKey.setOnPreferenceClickListener((preference) -> {
            getPresenter().onBLELinkKeyAction();
            return true;
        });
        mImpactDetectionDebugModeEnabled = (SwitchPreferenceCompat) findPreference("setting_debug_impact_detection_debug_mode_enabled");
        mImpactDetectionDebugModeEnabled.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onImpactDetectionDebugModeEnabledAction((boolean)newValue);
            return true;
        });
        mAdasWarning = findPreference("setting_debug_adas_warning");
        mAdasWarning.setOnPreferenceClickListener((preference) -> {
            new AlertDialog.Builder(getContext())
                    .setTitle(getContext().getString(R.string.dbg_005))
                    .setItems(getContext().getResources().getStringArray(R.array.debug_adas_warning), (dialog, which) -> {
                        getPresenter().onAdasWarningAction(AdasWarningStatus.values()[which]);
                    })
                    .show();
            return true;
        });
        mSmartPhoneInterrupt = findPreference("setting_debug_smart_phone_interrupt");
        mSmartPhoneInterrupt.setOnPreferenceClickListener((preference) -> {
            new AlertDialog.Builder(getContext())
                    .setTitle(getContext().getString(R.string.dbg_006))
                    .setItems(getContext().getResources().getStringArray(R.array.debug_smart_phone_interrupt), (dialog, which) -> {
                        getPresenter().onSmartPhoneInterruptAction(INTERRUPT_LIST_ITEMS.get(which), which > 2);
                    })
                    .show();
            return true;
        });
        mGoHome = findPreference("setting_debug_go_home");
        mGoHome.setOnPreferenceClickListener((preference) -> {
            getPresenter().onGoHomeAction();
            return true;
        });
        mDebugSpecialEq = (SwitchPreferenceCompat) findPreference("setting_debug_special_eq_debug_mode_enabled");
        mDebugSpecialEq.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onDebugSpecialEqAction((boolean)newValue);
            return true;
        });
        mTipsServer = findPreference("setting_debug_tips_Server");
        mTipsServer.setOnPreferenceClickListener((preference) -> {
            getPresenter().onTipsServerAction();
            return true;
        });
        mVersion1_1Function = (SwitchPreferenceCompat) findPreference("setting_debug_version_1_1_function");
        mVersion1_1Function.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onVersion1_1Action((boolean)newValue);
            return true;
        });
        mAdasSimJudgement = (SwitchPreferenceCompat) findPreference("setting_debug_adas_sim_judgement");
        mAdasSimJudgement.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasSimJudgement((boolean)newValue);
            return true;
        });
        mAdasTrialReset = findPreference("setting_debug_adas_trial_reset");
        mAdasTrialReset.setOnPreferenceClickListener((preference) -> {
            getPresenter().onAdasTrialReset();
            return true;
        });
        mAdasPurchased = (SwitchPreferenceCompat) findPreference("setting_debug_adas_purchased");
        mAdasPurchased.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasPurchasedAction((boolean)newValue);
            return true;
        });
        mAdasConfigured = (SwitchPreferenceCompat) findPreference("setting_debug_adas_configured");
        mAdasConfigured.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasConfiguredAction((boolean)newValue);
            return true;
        });
        mAdasPseudoCooperation = (SwitchPreferenceCompat) findPreference("setting_debug_adas_pseudo_cooperation");
        mAdasPseudoCooperation.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasPseudoCooperation((boolean)newValue);
            return true;
        });

        mVRDelayTime = (SeekBarPreference) findPreference("setting_debug_vr_delay_time");
        mVRDelayTime.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onVRDelayTime((int) newValue);
            return true;
        });
        mVRDelayTime.setVisible(false);
        mDebugRunningStatus = (SwitchPreferenceCompat) findPreference("setting_debug_running_status");
        mDebugRunningStatus.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onDebugRunningStatus((boolean)newValue);
            return true;
        });
        mHomeCenterView = findPreference("setting_debug_home_center_view");
        mHomeCenterView.setOnPreferenceClickListener((preference) -> {
            getPresenter().onHomeCenterAction();
            return true;
        });
        mAdasCarSpeed = (NumberPickerPreference)findPreference("setting_debug_adas_car_speed");
        mAdasCarSpeed.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasCarSpeedAction((int)newValue);
            return true;
        });
        mAdasLdwMinSpeed = (NumberPickerPreference)findPreference("setting_debug_adas_ldw_min_speed");
        mAdasLdwMinSpeed.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasLdwMinSpeed((int)newValue);
            return true;
        });
        mAdasLdwMaxSpeed = (NumberPickerPreference)findPreference("setting_debug_adas_ldw_max_speed");
        mAdasLdwMaxSpeed.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasLdwMaxSpeed((int)newValue);
            return true;
        });
        mAdasPcwMinSpeed = (NumberPickerPreference)findPreference("setting_debug_adas_pcw_min_speed");
        mAdasPcwMinSpeed.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasPcwMinSpeed((int)newValue);
            return true;
        });
        mAdasPcwMaxSpeed = (NumberPickerPreference)findPreference("setting_debug_adas_pcw_max_speed");
        mAdasPcwMaxSpeed.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasPcwMaxSpeed((int)newValue);
            return true;
        });
        mAdasFcwMinSpeed = (NumberPickerPreference)findPreference("setting_debug_adas_fcw_min_speed");
        mAdasFcwMinSpeed.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasFcwMinSpeed((int)newValue);
            return true;
        });
        mAdasFcwMaxSpeed = (NumberPickerPreference)findPreference("setting_debug_adas_fcw_max_speed");
        mAdasFcwMaxSpeed.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasFcwMaxSpeed((int)newValue);
            return true;
        });
        mAdasAccelerateY = (NumberPickerPreferenceFloat)findPreference("setting_debug_adas_accelerate_y_range");
        mAdasAccelerateY.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasAccelerateY((int)newValue);
            return true;
        });

        mAdasAccelerateZMin = (NumberPickerPreferenceFloat)findPreference("setting_debug_adas_accelerate_z_range_min");
        mAdasAccelerateZMin.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasAccelerateZMin((int)newValue);
            return true;
        });
        mAdasAccelerateZMax = (NumberPickerPreferenceFloat)findPreference("setting_debug_adas_accelerate_z_range_max");
        mAdasAccelerateZMax.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasAccelerateZMax((int)newValue);
            return true;
        });
        mAdasFps = (NumberPickerPreference)findPreference("setting_debug_adas_fps");
        mAdasFps.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasFps((int)newValue);
            return true;
        });
        mAdasCameraPreview = (SwitchPreferenceCompat) findPreference("setting_debug_adas_camera_view");
        mAdasCameraPreview.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onAdasCameraPreview((boolean)newValue);
            return true;
        });
        mAlexaSimJudgement = (SwitchPreferenceCompat) findPreference("setting_debug_alexa_sim_judgement");
        mAlexaSimJudgement.setOnPreferenceChangeListener(((preference, newValue) -> {
            getPresenter().onAlexaSimJudgement((boolean)newValue);
            return true;
        }));

        mSmartPhoneControlComand = (SwitchPreferenceCompat) findPreference("setting_debug_smart_phone_control_command");
        mSmartPhoneControlComand.setOnPreferenceChangeListener(((preference, newValue) -> {
            getPresenter().onSmartPhoneControlComand((boolean)newValue);
            return true;
        }));

        mDeviceVolume = (SeekBarPreference) findPreference("setting_debug_device_volume");
        mDeviceVolume.setOnPreferenceChangeListener(((preference, newValue) -> {
            getPresenter().onDeviceVolumeChangeCommand((int) newValue);
            return true;
        }));
    }
    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof NumberPickerPreference) {
            DialogFragment dialogFragment = NumberPickerPreference.NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), null);
        } else if(preference instanceof NumberPickerPreferenceFloat){
            DialogFragment dialogFragment = NumberPickerPreferenceFloat.NumberPickerPreferenceFloatDialogFragmentCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 1);
            dialogFragment.show(getFragmentManager(), null);
        }else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected DebugSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.DEBUG_SETTING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLogEnabled(boolean isEnabled) {
        mLogEnabled.setChecked(isEnabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImpactDetectionDebugModeEnabled(boolean isEnabled) {
        mImpactDetectionDebugModeEnabled.setChecked(isEnabled);
    }

    @Override
    public void setSpecialEqDebugModeEnabled(boolean isEnabled) {
        mDebugSpecialEq.setChecked(isEnabled);
    }

    @Override
    public void setTipsServer(TipsContentsEndpoint endpoint) {
        mTipsServer.setSummary(endpoint.label);
    }

    @Override
    public void setVersion1_1FunctionEnabled(boolean isEnabled) {
        mVersion1_1Function.setChecked(isEnabled);
    }

    @Override
    public void setAdasPurchased(boolean purchased) {
        mAdasPurchased.setChecked(purchased);
    }

    @Override
    public void setAdasSimJudgement(boolean purchased) {
        mAdasSimJudgement.setChecked(purchased);
    }

    @Override
    public void setAdasConfigured(boolean configured) {
        mAdasConfigured.setChecked(configured);
    }

    @Override
    public void setAdasPseudoCooperation(boolean enabled) {
        mAdasPseudoCooperation.setChecked(enabled);
    }

    @Override
    public void setVRDelayTime(int value) {
        mVRDelayTime.setMax(1500);
        mVRDelayTime.setMin(0);
        mVRDelayTime.setValue(value);
    }

    @Override
    public void setDebugRunningStatus(boolean enabled) {
        mDebugRunningStatus.setChecked(enabled);
    }
    @Override
    public void setHomeCenterView(boolean value) {
        if (value) {
            mHomeCenterView.setSummary(R.string.setting_debug_home_center_view_adas_detected);
        }else{
            mHomeCenterView.setSummary(R.string.setting_debug_home_center_view_clock);
        }
    }
    @Override
    public void setAdasCarSpeed(int speed) {
        mAdasCarSpeed.setValue(speed);
    }

    @Override
    public void setAdasLdwMinSpeed(int value) {
        mAdasLdwMinSpeed.setValue(value);
    }

    @Override
    public void setAdasLdwMaxSpeed(int value) {
        mAdasLdwMaxSpeed.setValue(value);
    }

    @Override
    public void setAdasPcwMinSpeed(int value) {
        mAdasPcwMinSpeed.setValue(value);
    }

    @Override
    public void setAdasPcwMaxSpeed(int value) {
        mAdasPcwMaxSpeed.setValue(value);
    }

    @Override
    public void setAdasFcwMinSpeed(int value) {
        mAdasFcwMinSpeed.setValue(value);
    }

    @Override
    public void setAdasFcwMaxSpeed(int value) {
        mAdasFcwMaxSpeed.setValue(value);
    }

    @Override
    public void setAdasAccelerateY(float value) {
        mAdasAccelerateY.setFloatValue(value);
    }

    @Override
    public void setAdasAccelerateZMin(float value) {
        mAdasAccelerateZMin.setFloatValue(value);
    }

    @Override
    public void setAdasAccelerateZMax(float value) {
        mAdasAccelerateZMax.setFloatValue(value);
    }

    @Override
    public void setAdasFps(int value) {
        mAdasFps.setValue(value);
    }

    @Override
    public void setAdasCameraPreview(boolean enabled) {
        mAdasCameraPreview.setChecked(enabled);
    }

    @Override
    public void setAlexaSimJudgement(boolean value) {
        mAlexaSimJudgement.setChecked(value);
    }

    @Override
    public void recheckSim() {
        if(getActivity() != null) {
            ((MainActivity) getActivity()).checkSim();
        }
    }

    @Override
    public void setSmartPhoneControlComand(boolean value) {
        mSmartPhoneControlComand.setChecked(value);
    }

    @Override
    public void setDeviceVolume(int max,int current) {
        mDeviceVolume.setVisible(true);
        mDeviceVolume.setEnabled(true);
        mDeviceVolume.setMin(0);
        mDeviceVolume.setMax(max);
        mDeviceVolume.setValue(current);
    }
}
