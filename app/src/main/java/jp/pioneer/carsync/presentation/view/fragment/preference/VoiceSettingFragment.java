package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.VoiceRecognizeMicType;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.presenter.VoiceSettingPresenter;
import jp.pioneer.carsync.presentation.view.VoiceSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SingleChoiceDialogFragment;

/**
 * Voice設定画面.
 */
public class VoiceSettingFragment extends AbstractPreferenceFragment<VoiceSettingPresenter, VoiceSettingView>
        implements VoiceSettingView, SingleChoiceDialogFragment.Callback {
    @Inject VoiceSettingPresenter mPresenter;
    private SwitchPreferenceCompat mVoiceRecognition;
    private Preference mVoiceRecognitionType;
    private PreferenceCategory mVoiceRecognitionDescription;
    private Preference mVoiceRecognitionMicType;
    /**
     * コンストラクタ.
     */
    public VoiceSettingFragment(){
    }

    /**
     * 新規インスタンス取得.
     *
     * @param args 引き継ぎ情報
     * @return VoiceSettingFragment
     */
    public static VoiceSettingFragment newInstance(Bundle args) {
        VoiceSettingFragment fragment = new VoiceSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_voice, rootKey);

        mVoiceRecognition = (SwitchPreferenceCompat) findPreference(getString(R.string.key_voice_recognition_enabled));
        mVoiceRecognition.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onVoiceRecognitionChange((boolean) newValue);
            return true;
        });

        mVoiceRecognitionType =  findPreference(getString(R.string.key_voice_recognition_type));
        mVoiceRecognitionType.setOnPreferenceClickListener((preference) -> {
            getPresenter().onVoiceRecognitionTypeChange();
            return true;
        });

        mVoiceRecognitionMicType =  findPreference(getString(R.string.key_voice_recognition_mic_type));
        mVoiceRecognitionMicType.setOnPreferenceClickListener((preference) -> {
            getPresenter().onVoiceRecognitionMicTypeChange();
            return true;
        });

        mVoiceRecognitionDescription = (PreferenceCategory)findPreference(getString(R.string.key_voice_recognition_description));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        //setDivider(new ColorDrawable(Color.TRANSPARENT));
        //setDividerHeight(0);
        String str = getString(R.string.rec_014) + "\n"
                + getString(R.string.rec_016) + "\n"
                + getString(R.string.rec_018) + "\n"
                + getString(R.string.rec_020) + "\n"
                + getString(R.string.rec_022) + "\n"
                + getString(R.string.rec_024) + "\n"
                + getString(R.string.rec_026);
        mVoiceRecognitionDescription.setSummary(str);
        return view;
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
    protected VoiceSettingPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_VOICE;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setVoiceRecognitionVisible(boolean isVisible) {
        mVoiceRecognition.setVisible(isVisible);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setVoiceRecognitionTypeVisible(boolean isVisible) {
        mVoiceRecognitionType.setVisible(isVisible);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setVoiceRecognitionEnabled(boolean isEnabled) {
        mVoiceRecognition.setChecked(isEnabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVoiceRecognitionTypeEnabled(boolean isEnabled) {
        mVoiceRecognitionType.setEnabled(isEnabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVoiceRecognitionType(VoiceRecognizeType type) {
        mVoiceRecognitionType.setSummary(type.label);
        mVoiceRecognitionDescription.setVisible(mPresenter.isVoiceRecognitionDescriptionVisible(type));
    }

    @Override
    public void onClose(SingleChoiceDialogFragment fragment) {

    }

    @Override
    public void selectItem(int position) {
        getPresenter().setVoiceRecognizeType(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVoiceRecognitionMicTypeVisible(boolean isVisible) {
        mVoiceRecognitionMicType.setVisible(isVisible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVoiceRecognitionMicTypeEnabled(boolean isEnabled) {
        mVoiceRecognitionMicType.setEnabled(isEnabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVoiceRecognitionMicType(VoiceRecognizeMicType type) {
        mVoiceRecognitionMicType.setSummary(type.label);
    }
}
