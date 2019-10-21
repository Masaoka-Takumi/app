package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.preference.SwitchPreferenceCompat;

import java.util.Locale;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.AdasCameraSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionSetting;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.presentation.presenter.AdasSettingPresenter;
import jp.pioneer.carsync.presentation.view.AdasSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * ADAS設定画面.
 */
@RuntimePermissions
public class AdasSettingFragment extends AbstractPreferenceFragment<AdasSettingPresenter, AdasSettingView>
        implements AdasSettingView {
    private static final double UNIT_CHANGE_M_TO_FT = 3.2808;
    @Inject AdasSettingPresenter mPresenter;
    private SwitchPreferenceCompat mAdas;
    private SwitchPreferenceCompat mAlarm;
    private Preference mCalibration;
    private SeekBarPreference mCameraHeight;
    private SeekBarPreference mFrontNoseDistance;
    private SeekBarPreference mVehicleWidth;
    private SwitchPreferenceCompat mLdw;
    private Preference mLdwSensitivity;
    private SwitchPreferenceCompat mPcw;
    private Preference mPcwSensitivity;
    private SwitchPreferenceCompat mFcw;
    private Preference mFcwSensitivity;
    private SwitchPreferenceCompat mLkw;
    private Preference mLkwSensitivity;
    private Preference mUsageCautionManual;

    /**
     * コンストラクタ
     */
    public AdasSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AdasSettingFragment
     */
    public static AdasSettingFragment newInstance(Bundle args) {
        AdasSettingFragment fragment = new AdasSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_car_safety_adas, rootKey);

        mAdas = (SwitchPreferenceCompat) findPreference(getString(R.string.key_adas_adas));
        mAdas.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectAdasAction();
            return true;
        });

        mAlarm = (SwitchPreferenceCompat) findPreference(getString(R.string.key_adas_alarm));
        mAlarm.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectAdasAlarmAction((boolean) newValue);
            return true;
        });

        mCalibration = findPreference(getString(R.string.key_adas_calibration));
        mCalibration.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectCalibrationAction();
            return true;
        });

        mCameraHeight = (SeekBarPreference) findPreference(getString(R.string.key_adas_camera_camera_height));
        mCameraHeight.setIcon(R.drawable.p1510_adasseticon_1);
        mCameraHeight.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().setCameraHeight((int) newValue * 100);
            return true;
        });

        mFrontNoseDistance = (SeekBarPreference) findPreference(getString(R.string.key_adas_camera_front_nose_distance));
        mFrontNoseDistance.setIcon(R.drawable.p1511_adasseticon_2);
        mFrontNoseDistance.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().setFrontNoseDistance((int) newValue * 100);
            return true;
        });

        mVehicleWidth = (SeekBarPreference) findPreference(getString(R.string.key_adas_camera_vehicle_width));
        mVehicleWidth.setIcon(R.drawable.p1512_adasseticon_3);
        mVehicleWidth.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().setVehicleWidth((int) newValue * 100);
            return true;
        });

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

        mLkw = (SwitchPreferenceCompat) findPreference(getString(R.string.key_adas_function_lkw));
        mLkw.setIcon(R.drawable.p1516_adasseticon_7);
        mLkw.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectLkwAction((boolean) newValue);
            return true;
        });
        mLkw.setVisible(false);
        mLkwSensitivity = findPreference(getString(R.string.key_adas_function_lkw_sensitivity));
        mLkwSensitivity.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectLkwSensitivityAction();
            return true;
        });
        mLkwSensitivity.setVisible(false);

        mUsageCautionManual = findPreference(getString(R.string.key_adas_usage_caution_manual));
        mUsageCautionManual.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectUsageCautionManualAction();
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
    protected AdasSettingPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_ADAS;
    }

    /**
     * ADAS起動.
     */
    @NeedsPermission(Manifest.permission.CAMERA)
    public void setAdas(boolean setting) {
        getPresenter().onSelectAdasAction(setting);
    }

    /**
     * キャリブレーション設定表示.
     */
    @NeedsPermission(Manifest.permission.CAMERA)
    public void setCalibrationSetting() {
        getPresenter().onSelectCalibrationAction();
    }

    @Override
    public void setAdasSetting(boolean setting) {
        mAdas.setChecked(setting);
        mAlarm.setEnabled(setting);
        mCalibration.setEnabled(setting);
        mCameraHeight.setEnabled(setting);
        mFrontNoseDistance.setEnabled(setting);
        mVehicleWidth.setEnabled(setting);
        mLdw.setEnabled(setting);
        mLdwSensitivity.setEnabled(setting);
        mPcw.setEnabled(setting);
        mPcwSensitivity.setEnabled(setting);
        mFcw.setEnabled(setting);
        mFcwSensitivity.setEnabled(setting);
        mLkw.setEnabled(setting);
        mLkwSensitivity.setEnabled(setting);
    }

    @Override
    public void setAdasTrialTermVisible(boolean visible) {
        if(visible){
            mAdas.setLayoutResource(R.layout.element_preference_switch_summary);
        }else{
            mAdas.setLayoutResource(R.layout.element_preference_switch);
        }
    }

    @Override
    public void setAdasTrialTerm(String str) {
        mAdas.setSummary(getString(R.string.set_329)+ " " + str );
    }

    @Override
    public void setAdasAlarmSetting(boolean setting) {
        mAlarm.setChecked(setting);
    }

    @Override
    public void setCameraSetting(AdasCameraSetting setting) {
        int displayHeight = setting.cameraHeight / 100;//単位：[10cm]
        mCameraHeight.setMin(10);
        mCameraHeight.setMax(15);
        mCameraHeight.setValue(displayHeight);

        int displayDistance = setting.frontNoseDistance / 100;
        mFrontNoseDistance.setMin(1);
        mFrontNoseDistance.setMax(20);
        mFrontNoseDistance.setValue(displayDistance);

        int displayWidth = setting.vehicleWidth / 100;
        mVehicleWidth.setMin(15);
        mVehicleWidth.setMax(21);
        mVehicleWidth.setValue(displayWidth);

        if (getPresenter().getDistanceUnit() == DistanceUnit.METER_KILOMETER) {
            mCameraHeight.setSummary(((double) displayHeight / 10) + getString(R.string.unt_003));
            mFrontNoseDistance.setSummary(((double) displayDistance / 10) + getString(R.string.unt_003));
            mVehicleWidth.setSummary(((double) displayWidth / 10) + getString(R.string.unt_003));
        } else {
            mCameraHeight.setSummary(String.format(Locale.ENGLISH,"%.1f" + getString(R.string.unt_004), ((double) displayHeight / 10 * UNIT_CHANGE_M_TO_FT)));
            mFrontNoseDistance.setSummary(String.format(Locale.ENGLISH,"%.1f" + getString(R.string.unt_004), ((double) displayDistance / 10 * UNIT_CHANGE_M_TO_FT)));
            mVehicleWidth.setSummary(String.format(Locale.ENGLISH,"%.1f" + getString(R.string.unt_004), ((double) displayWidth / 10 * UNIT_CHANGE_M_TO_FT)));
        }
    }

    @Override
    public void setLdwSetting(AdasFunctionSetting setting) {
        mLdw.setChecked(setting.settingEnabled);
        if(mAdas.isChecked()) {
            mLdwSensitivity.setEnabled(setting.settingEnabled);
        }else{
            mLdwSensitivity.setEnabled(false);
        }
        mLdwSensitivity.setSummary(setting.functionSensitivity.label);
    }

    @Override
    public void setFcwSetting(AdasFunctionSetting setting) {
        mFcw.setChecked(setting.settingEnabled);
        if(mAdas.isChecked()) {
            mFcwSensitivity.setEnabled(setting.settingEnabled);
        }else{
            mFcwSensitivity.setEnabled(false);
        }
        mFcwSensitivity.setSummary(setting.functionSensitivity.label);
    }

    @Override
    public void setPcwSetting(AdasFunctionSetting setting) {
        mPcw.setChecked(setting.settingEnabled);
        if(mAdas.isChecked()) {
            mPcwSensitivity.setEnabled(setting.settingEnabled);
        }else{
            mPcwSensitivity.setEnabled(false);
        }
        mPcwSensitivity.setSummary(setting.functionSensitivity.label);
    }

    @Override
    public void setLkwSetting(AdasFunctionSetting setting) {
        mLkw.setChecked(setting.settingEnabled);
        if(mAdas.isChecked()) {
            mLkwSensitivity.setEnabled(setting.settingEnabled);
        }else{
            mLkwSensitivity.setEnabled(false);
        }
        mLkwSensitivity.setSummary(setting.functionSensitivity.label);
    }
}
