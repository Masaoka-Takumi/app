package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.preference.SwitchPreferenceCompat;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.KaraokePresenter;
import jp.pioneer.carsync.presentation.view.KaraokeView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Created by NSW00_008320 on 2018/01/30.
 */

public class KaraokeFragment extends AbstractPreferenceFragment<KaraokePresenter, KaraokeView> implements KaraokeView {
    @Inject KaraokePresenter mPresenter;

    private SwitchPreferenceCompat mMicrophone;
    private SeekBarPreference mMicVolume;
    private SwitchPreferenceCompat mVocalCancel;

    @Inject
    public KaraokeFragment() {
    }

    public static KaraokeFragment newInstance(Bundle args) {
        KaraokeFragment fragment = new KaraokeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_fx_karaoke, rootKey);

        mMicrophone = (SwitchPreferenceCompat) findPreference(getString(R.string.key_microphone));
        mMicrophone.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onMicrophoneChange((boolean) newValue);
            return true;
        });

        mMicVolume = (SeekBarPreference) findPreference(getString(R.string.key_mic_volume));
        mMicVolume.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onMicVolumeChange((int) newValue);
            return true;
        });

        mVocalCancel = (SwitchPreferenceCompat) findPreference(getString(R.string.key_vocal_cancel));
        mVocalCancel.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onVocalCancelChange((boolean) newValue);
            return true;
        });
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected KaraokePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.KARAOKE_SETTING;
    }

    @Override
    public void setMicrophoneSettingEnabled(boolean isSupported,boolean isEnabled) {
        mMicrophone.setVisible(isSupported);
        mMicrophone.setEnabled(isEnabled);
    }

    @Override
    public void setMicrophoneSetting(boolean setting) {
        mMicrophone.setChecked(setting);
    }

    @Override
    public void setMicVolumeSetting(boolean isSupported, boolean isEnabled, int min, int max, int curr) {
        mMicVolume.setVisible(isSupported);
        mMicVolume.setEnabled(isEnabled);
        mMicVolume.setMin(min);
        mMicVolume.setMax(max);
        mMicVolume.setValue(curr);
    }

    @Override
    public void setVocalCancelSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mVocalCancel.setVisible(isSupported);
        mVocalCancel.setEnabled(isEnabled);
        mVocalCancel.setChecked(setting);
    }
}
