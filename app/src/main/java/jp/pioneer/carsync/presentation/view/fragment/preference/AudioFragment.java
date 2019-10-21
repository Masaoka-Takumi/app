package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SeekBarPreference;
import android.widget.Toast;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.BeatBlasterSetting;
import jp.pioneer.carsync.domain.model.LoudnessSetting;
import jp.pioneer.carsync.domain.model.SoundRetrieverSetting;
import jp.pioneer.carsync.presentation.presenter.AudioPresenter;
import jp.pioneer.carsync.presentation.view.AudioView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;

/**
 * Audio設定の画面
 */

public class AudioFragment extends AbstractPreferenceFragment<AudioPresenter, AudioView> implements AudioView, StatusPopupDialogFragment.Callback {
    private static final String TAG_DIALOG_SAVE = "save";
    private static final String TAG_DIALOG_LOAD = "load";
    @Inject AudioPresenter mPresenter;
    private Preference mBeatBlaster;
    private Preference mLoudness;
    private Preference mSoundRetriever;
    private Preference mFaderBalance;
    private Preference mAdvancedSettings;
    private SeekBarPreference mSourceLevelAdjuster;
    private Preference mLoad;
    private Preference mSave;

    @Inject
    public AudioFragment() {
    }

    public static AudioFragment newInstance(Bundle args) {
        AudioFragment fragment = new AudioFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_audio, rootKey);

        mBeatBlaster = findPreference("audio_beat_blaster");
        mBeatBlaster.setOnPreferenceClickListener((preference) -> {
            getPresenter().onBeatBlasterAction();
            return true;
        });
        mLoudness = findPreference("audio_loudness");
        mLoudness.setOnPreferenceClickListener((preference) -> {
            getPresenter().onLoudnessAction();
            return true;
        });
        mSourceLevelAdjuster = (SeekBarPreference) findPreference("audio_source_level_adjuster");
        mSourceLevelAdjuster.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onSourceLevelAdjusterAction((int) newValue);
            return true;
        });
        mFaderBalance = findPreference("audio_fader_balance");
        mFaderBalance.setOnPreferenceClickListener((preference) -> {
            getPresenter().onFaderBalanceAction();
            return true;
        });
        mAdvancedSettings = findPreference("audio_advance_settings");
        mAdvancedSettings.setOnPreferenceClickListener((preference) -> {
            getPresenter().onAdvancedSettingsAction();
            return true;
        });
        mSoundRetriever = findPreference("audio_sound_retriever");
        mSoundRetriever.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSoundRetrieverAction();
            return true;
        });
        mLoad = findPreference("setting_audio_load");
        mLoad.setOnPreferenceClickListener((preference) -> {
            getPresenter().OnShowDialog(TAG_DIALOG_LOAD, getString(R.string.set_267));
            return true;
        });
        mSave = findPreference("setting_audio_save");
        mSave.setOnPreferenceClickListener((preference) -> {
            getPresenter().OnShowDialog(TAG_DIALOG_SAVE, getString(R.string.set_268));
            return true;
        });
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AudioPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_AUDIO;
    }

    @Override
    public void setBeatBlasterSetting(boolean isSupported, boolean isEnabled, @Nullable BeatBlasterSetting setting) {
        mBeatBlaster.setVisible(isSupported);
        mBeatBlaster.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(beatBlasterSetting -> mBeatBlaster.setSummary(beatBlasterSetting.label));
    }

    @Override
    public void setLoudnessSetting(boolean isSupported, boolean isEnabled, @Nullable LoudnessSetting setting) {
        mLoudness.setVisible(isSupported);
        mLoudness.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(loudnessSetting -> mLoudness.setSummary(loudnessSetting.label));
    }

    @Override
    public void setSourceLevelAdjuster(boolean isSupported, boolean isEnabled, int min, int max, int curr) {
        mSourceLevelAdjuster.setVisible(isSupported);
        mSourceLevelAdjuster.setEnabled(isEnabled);
        mSourceLevelAdjuster.setMin(min);
        mSourceLevelAdjuster.setMax(max);
        mSourceLevelAdjuster.setValue(curr);
    }

    @Override
    public void setFaderBalanceSetting(boolean isSupported, boolean isEnabled, boolean faderSettingEnabled) {
        mFaderBalance.setVisible(isSupported);
        mFaderBalance.setEnabled(isEnabled);
        if(faderSettingEnabled) {
            mFaderBalance.setTitle(R.string.set_067);
        } else {
            mFaderBalance.setTitle(R.string.set_260);
        }
    }

    @Override
    public void setAdvancedSetting(boolean isEnabled) {
        mAdvancedSettings.setEnabled(isEnabled);
    }

    @Override
    public void setSoundRetrieverSetting(boolean isSupported, boolean isEnabled, @Nullable SoundRetrieverSetting setting) {
        mSoundRetriever.setVisible(isSupported);
        mSoundRetriever.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(soundRetrieverSetting -> mSoundRetriever.setSummary(soundRetrieverSetting.label));
    }

    @Override
    public void setLoadSetting(boolean isSupported, boolean isEnabled) {
        mLoad.setVisible(isSupported);
        mLoad.setEnabled(isEnabled);
    }

    @Override
    public void setSaveSetting(boolean isSupported, boolean isEnabled) {
        mSave.setVisible(isSupported);
        mSave.setEnabled(isEnabled);
    }

    @Override
    public void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClose(StatusPopupDialogFragment fragment, String tag) {

    }

    @Override
    public void onPositiveClick(StatusPopupDialogFragment fragment, String tag) {
        if (tag.equals(TAG_DIALOG_SAVE)) {
            getPresenter().onSaveAction();
        } else if (tag.equals(TAG_DIALOG_LOAD)){
            getPresenter().onLoadAction();
        }
    }

    @Override
    public void onNegativeClick(StatusPopupDialogFragment fragment, String tag) {
    }


}
