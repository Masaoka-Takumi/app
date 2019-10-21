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
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.presentation.presenter.HdRadioSettingPresenter;
import jp.pioneer.carsync.presentation.view.HdRadioSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

public class HdRadioSettingFragment extends AbstractPreferenceFragment<HdRadioSettingPresenter, HdRadioSettingView>
        implements HdRadioSettingView {
    @Inject HdRadioSettingPresenter mPresenter;
    private Preference mLocal;
    private SwitchPreferenceCompat mSeek;
    private SwitchPreferenceCompat mBlending;
    private SwitchPreferenceCompat mActiveRadio;

    /**
     * コンストラクタ.
     */
    public HdRadioSettingFragment() {
    }

    /**
     * 新規インスタンス取得.
     *
     * @param args 引き継ぎ情報
     * @return HdRadioSettingFragment
     */
    public static HdRadioSettingFragment newInstance(Bundle args) {
        HdRadioSettingFragment fragment = new HdRadioSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_hdradio, rootKey);
        mLocal = findPreference(getString(R.string.key_hdradio_local));
        mLocal.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectLocalSettingAction();
            return true;
        });

        mSeek = (SwitchPreferenceCompat) findPreference(getString(R.string.key_hdradio_seek_enabled));
        mSeek.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectSeekSettingAction((boolean) newValue);
            return true;
        });

        mBlending = (SwitchPreferenceCompat) findPreference(getString(R.string.key_hdradio_blending_enabled));
        mBlending.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectBlendingSettingAction((boolean) newValue);
            return true;
        });

        mActiveRadio = (SwitchPreferenceCompat) findPreference(getString(R.string.key_hdradio_active_radio_enabled));
        mActiveRadio.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSelectActiveRadioSettingAction((boolean) newValue);
            return true;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_HD_RADIO;
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
    protected HdRadioSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setLocalSetting(boolean isSupported, boolean isEnabled, @Nullable LocalSetting setting) {
        mLocal.setVisible(isSupported);
        mLocal.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(type -> mLocal.setSummary(type.label));
    }

    @Override
    public void setSeekSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mSeek.setVisible(isSupported);
        mSeek.setEnabled(isEnabled);
        mSeek.setChecked(setting);
    }

    @Override
    public void setBlendingSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mBlending.setVisible(isSupported);
        mBlending.setEnabled(isEnabled);
        mBlending.setChecked(setting);
    }

    @Override
    public void setActiveRadioSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mActiveRadio.setVisible(isSupported);
        mActiveRadio.setEnabled(isEnabled);
        mActiveRadio.setChecked(setting);
    }
}