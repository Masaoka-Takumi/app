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
import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.domain.model.SphBtPhoneColorSetting;
import jp.pioneer.carsync.presentation.presenter.PhoneSettingPresenter;
import jp.pioneer.carsync.presentation.view.PhoneSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Phone設定画面.
 */
public class PhoneSettingFragment extends AbstractPreferenceFragment<PhoneSettingPresenter, PhoneSettingView>
        implements PhoneSettingView {

    @Inject PhoneSettingPresenter mPresenter;
    private Preference mDeviceSettings;
    private SwitchPreferenceCompat mAutoPairing;
    private Preference mDirectCall;
    private SwitchPreferenceCompat mAccessible;
    private Preference mCallPattern;
    private Preference mCallColor;
    private SwitchPreferenceCompat mAutoAnswer;

    /**
     * コンストラクタ.
     */
    public PhoneSettingFragment() {
    }

    /**
     * 新規インスタンス取得.
     *
     * @param args 引き継ぎ情報
     * @return PhoneSettingFragment
     */
    public static PhoneSettingFragment newInstance(Bundle args) {
        PhoneSettingFragment fragment = new PhoneSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_phone, rootKey);

        mDeviceSettings = findPreference(getString(R.string.key_phone_device_settings));
        mDeviceSettings.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectDeviceSettingsAction();
            return true;
        });
        mAutoPairing = (SwitchPreferenceCompat) findPreference(getString(R.string.key_phone_auto_pairing));
        mAutoPairing.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectAutoPairingAction((boolean) newValue);
            return true;
        });
        mDirectCall = findPreference(getString(R.string.key_phone_direct_call));
        mDirectCall.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectDirectCallAction();
            return true;
        });
        mAccessible = (SwitchPreferenceCompat) findPreference(getString(R.string.key_phone_phone_book_access_setting));
        mAccessible.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectPhoneBookAccessibleAction((boolean) newValue);
            return true;
        });
        mCallPattern = findPreference(getString(R.string.key_phone_incoming_call_pattern));
        mCallPattern.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectIncomingCallPatternItemAction();
            return true;
        });
        mCallColor = findPreference(getString(R.string.key_phone_incoming_call_color));
        mCallColor.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectIncomingCallColorItemAction();
            return true;
        });
        mAutoAnswer = (SwitchPreferenceCompat) findPreference(getString(R.string.key_phone_incoming_call_auto_answer));
        mAutoAnswer.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectIncomingCallAutoAnswer((boolean) newValue);
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
    protected PhoneSettingPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_PHONE;
    }

    @Override
    public void setDeviceSettings(boolean isSupported, boolean isEnabled) {
        mDeviceSettings.setVisible(isSupported);
        mDeviceSettings.setEnabled(isEnabled);
    }

    @Override
    public void setAutoPairingSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mAutoPairing.setVisible(isSupported);
        mAutoPairing.setEnabled(isEnabled);
        mAutoPairing.setChecked(setting);
    }

    @Override
    public void setPhoneBookAccessibleSetting(boolean setting) {
        mAccessible.setChecked(setting);
    }

    @Override
    public void setIncomingCallPatternSetting(boolean isSupported, boolean isEnabled, @Nullable BtPhoneColor setting) {
        mCallPattern.setVisible(isSupported);
        mCallPattern.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(color -> mCallPattern.setSummary(getString(color.label)));
    }

    @Override
    public void setIncomingCallColorSetting(boolean isSupported, boolean isEnabled, @Nullable SphBtPhoneColorSetting setting) {
        mCallColor.setVisible(isSupported);
        mCallColor.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(color -> mCallColor.setSummary(getString(color.label)));
    }

    @Override
    public void setAutoAnswerSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mAutoAnswer.setVisible(isSupported);
        mAutoAnswer.setEnabled(isEnabled);
        mAutoAnswer.setChecked(setting);
    }
}
