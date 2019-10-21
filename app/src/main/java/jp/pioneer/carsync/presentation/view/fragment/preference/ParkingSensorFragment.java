package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.AlarmOutputDestinationSetting;
import jp.pioneer.carsync.domain.model.BackPolarity;
import jp.pioneer.carsync.presentation.presenter.ParkingSensorPresenter;
import jp.pioneer.carsync.presentation.view.ParkingSensorView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * パーキングセンサー設定画面.
 */
public class ParkingSensorFragment extends AbstractPreferenceFragment<ParkingSensorPresenter, ParkingSensorView>
        implements ParkingSensorView {
    @Inject ParkingSensorPresenter mPresenter;
    private SwitchPreferenceCompat mParkingSensorEnabled;
    private Preference mParkingSensorAlarmOutput;
    private SeekBarPreference mParkingSensorAlarmVolume;
    private Preference mBackPolarity;

    /**
     * コンストラクタ.
     */
    public ParkingSensorFragment(){

    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return ThemeFragment
     */
    public static ParkingSensorFragment newInstance(Bundle args) {
        ParkingSensorFragment fragment = new ParkingSensorFragment();
        fragment.setArguments(args);
        return fragment;
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
    protected ParkingSensorPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_car_safety_parking_sensor, rootKey);

        mParkingSensorEnabled = (SwitchPreferenceCompat) findPreference(getString(R.string.key_parking_sensor));
        mParkingSensorEnabled.setOnPreferenceChangeListener((preference, newValue) -> {
            mPresenter.onParkingSensorChange((boolean) newValue);
            return true;
        });
        mParkingSensorAlarmOutput = findPreference(getString(R.string.key_alarm_output));
        mParkingSensorAlarmOutput.setOnPreferenceClickListener((preference) -> {
            mPresenter.onParkingSensorAlarmOutputChange();
            return true;
        });

        mParkingSensorAlarmVolume = (SeekBarPreference) findPreference(getString(R.string.key_alarm_volume));
        mParkingSensorAlarmVolume.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onParkingSensorAlarmVolumeChange((int) newValue);
            return true;
        });

        mBackPolarity = findPreference(getString(R.string.key_back_polarity));
        mBackPolarity.setOnPreferenceClickListener((preference) -> {
            mPresenter.onBackPolarityChange();
            return true;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.PARKING_SENSOR_SETTING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParkingSensorSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mParkingSensorEnabled.setVisible(isSupported);
        mParkingSensorEnabled.setEnabled(isEnabled);
        mParkingSensorEnabled.setChecked(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParkingSensorAlarmOutputDestinationSetting(boolean isSupported, boolean isEnabled, @Nullable AlarmOutputDestinationSetting setting) {
        mParkingSensorAlarmOutput.setVisible(isSupported);
        mParkingSensorAlarmOutput.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(destinationSetting -> {
                    switch (destinationSetting) {
                        case FRONT:
                            mParkingSensorAlarmOutput.setSummary(getString(R.string.val_057));
                            break;
                        case FRONT_LEFT:
                            mParkingSensorAlarmOutput.setSummary(getString(R.string.val_058));
                            break;
                        case FRONT_RIGHT:
                            mParkingSensorAlarmOutput.setSummary(getString(R.string.val_059));
                            break;
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParkingSensorAlarmVolumeSetting(boolean isSupported, boolean isEnabled, int min, int max, int curr) {
        mParkingSensorAlarmVolume.setVisible(isSupported);
        mParkingSensorAlarmVolume.setEnabled(isEnabled);
        mParkingSensorAlarmVolume.setMin(min);
        mParkingSensorAlarmVolume.setMax(max);
        mParkingSensorAlarmVolume.setValue(curr);
    }

    @Override
    public void setBackPolarity(boolean isSupported, boolean isEnabled, @Nullable BackPolarity setting) {
        mBackPolarity.setVisible(isSupported);
        mBackPolarity.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(backPolarity -> {
                    switch (backPolarity) {
                        case BATTERY:
                            mBackPolarity.setSummary(getString(R.string.val_060));
                            break;
                        case GROUND:
                            mBackPolarity.setSummary(getString(R.string.val_061));
                            break;
                    }
                });
    }
}