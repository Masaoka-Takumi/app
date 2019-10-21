package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AudioSettingChangeEvent;
import jp.pioneer.carsync.domain.event.AudioSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAudio;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.AudioSettingSpec;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.BeatBlasterSetting;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AudioView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;

/**
 * Audio設定のPresenter
 */
@PresenterLifeCycle
public class AudioPresenter extends Presenter<AudioView> {

    @Inject AppSharedPreference mPreference;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject Context mContext;
    @Inject PreferAudio mPreferAudio;

    @Inject
    public AudioPresenter() {
    }

    @Override
    void onTakeView() {
        updateView();
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * Audio設定ステータス変更イベントハンドラ.
     *
     * @param event Audio設定ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioSettingStatusChangeEvent(AudioSettingStatusChangeEvent event) {
        updateView();
    }

    /**
     * Audio設定変更イベントハンドラ.
     *
     * @param event Audio設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioSettingChangeEvent(AudioSettingChangeEvent event) {
        updateView();
    }

    /**
     * Sound FX設定変更イベントハンドラ.
     *
     * @param event Sound FX設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSoundFxSettingChangeEvent(SoundFxSettingChangeEvent event) {
        updateView();
    }

    /**
     * 車載機ステータス変更イベントハンドラ
     *
     * @param event 車載機ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        updateView();
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            StatusHolder holder = mGetStatusHolder.execute();
            boolean isAudioSettingEnabled = holder.isAudioSettingEnabled() &&
                    holder.isAudioSettingSupported();
            AudioSettingSpec spec = holder.getCarDeviceSpec().audioSettingSpec;
            AudioSettingStatus status = holder.getAudioSettingStatus();
            AudioSetting setting = holder.getAudioSetting();

            view.setBeatBlasterSetting(
                    spec.beatBlasterSettingSupported,
                    status.beatBlasterSettingEnabled && isAudioSettingEnabled,
                    getBeatBlasterSetting()
            );
            view.setLoudnessSetting(
                    spec.loudnessSettingSupported,
                    status.loudnessSettingEnabled && isAudioSettingEnabled,
                    setting.loudnessSetting
            );
            view.setSourceLevelAdjuster(
                    spec.slaSettingSupported,
                    status.slaSettingEnabled && isAudioSettingEnabled,
                    setting.slaSetting.minimumStep,
                    setting.slaSetting.maximumStep,
                    setting.slaSetting.currentStep
            );
            view.setFaderBalanceSetting(
                    spec.balanceSettingSupported,
                    status.balanceSettingEnabled && isAudioSettingEnabled,
                    status.faderSettingEnabled
            );
            view.setAdvancedSetting(
                    isAudioSettingEnabled
            );
            view.setSoundRetrieverSetting(
                    spec.soundRetrieverSettingSupported,
                    status.soundRetrieverSettingEnabled && isAudioSettingEnabled,
                    setting.soundRetrieverSetting
            );
            view.setLoadSetting(
                    spec.loadSettingSupported,
                    status.loadSettingEnabled && isAudioSettingEnabled
            );
            view.setSaveSetting(
                    spec.saveSettingSupported,
                    status.saveSettingEnabled && isAudioSettingEnabled
            );
        });
    }

    private BeatBlasterSetting getBeatBlasterSetting(){
        StatusHolder holder = mGetStatusHolder.execute();
        AudioSetting audioSetting = holder.getAudioSetting();
        SoundFxSetting soundFxSetting = holder.getSoundFxSetting();

        if(soundFxSetting.superTodorokiSetting != SuperTodorokiSetting.OFF){
            switch (soundFxSetting.superTodorokiSetting){
                case LOW:
                    return BeatBlasterSetting.LOW;
                case HIGH:
                case SUPER_HIGH:
                    return BeatBlasterSetting.HIGH;
                default:
                    return audioSetting.beatBlasterSetting;
            }
        } else if(soundFxSetting.liveSimulationSetting.soundFieldControlSettingType != SoundFieldControlSettingType.OFF){
            return BeatBlasterSetting.OFF;
        } else {
            return audioSetting.beatBlasterSetting;
        }
    }

    /**
     * 確認ダイアログ表示
     */
    public void OnShowDialog(String tag, String message){
        Bundle bundle = new Bundle();
        bundle.putString(StatusPopupDialogFragment.TAG, tag);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, message);
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        bundle.putBoolean(StatusPopupDialogFragment.NEGATIVE, true);
        mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
    }

    /**
     * BeatBlaster押下時の処理
     */
    public void onBeatBlasterAction() {
        BeatBlasterSetting setting = getBeatBlasterSetting();
        mPreferAudio.setBeatBlaster(setting.toggle());
    }

    /**
     * Loudness押下時の処理
     */
    public void onLoudnessAction() {
        mEventBus.post(new NavigateEvent(ScreenId.LOUDNESS_DIALOG, Bundle.EMPTY));
    }

    /**
     * SourceLevelAdjuster押下時の処理
     */
    public void onSourceLevelAdjusterAction(int step) {
        mPreferAudio.setSourceLevelAdjuster(step);
    }

    /**
     * FaderBalance押下時の処理
     */
    public void onFaderBalanceAction() {
        int titleId = mGetStatusHolder.execute().getAudioSettingStatus().faderSettingEnabled ? R.string.set_067 : R.string.set_260;
        mEventBus.post(new NavigateEvent(ScreenId.FADER_BALANCE_SETTING, createSettingsParams(mContext.getString(titleId))));
    }

    /**
     * AdvancedSettings押下時の処理
     */
    public void onAdvancedSettingsAction() {
        mEventBus.post(new NavigateEvent(ScreenId.ADVANCED_AUDIO_SETTING, createSettingsParams(mContext.getString(R.string.set_005))));
    }

    /**
     * SoundRetriever押下時の処理
     */
    public void onSoundRetrieverAction() {
        mPreferAudio.toggleSoundRetriever();
    }

    /**
     * Load押下時の処理
     */
    public void onLoadAction() {
        mPreferAudio.loadAudioSetting(new PreferAudio.LoadSaveCallback() {
            @Override
            public void onSuccess() {
                Optional.ofNullable(getView()).ifPresent(view -> view.showToast(mContext.getString(R.string.set_269)));
            }

            @Override
            public void onError() {
                Optional.ofNullable(getView()).ifPresent(view -> view.showToast(mContext.getString(R.string.set_270)));
            }
        });
    }

    /**
     * Save押下時の処理
     */
    public void onSaveAction() {
        mPreferAudio.saveAudioSetting(new PreferAudio.LoadSaveCallback() {
            @Override
            public void onSuccess() {
                Optional.ofNullable(getView()).ifPresent(view -> view.showToast(mContext.getString(R.string.set_271)));
            }

            @Override
            public void onError() {
                Optional.ofNullable(getView()).ifPresent(view -> view.showToast(mContext.getString(R.string.set_272)));
            }
        });
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }
}
