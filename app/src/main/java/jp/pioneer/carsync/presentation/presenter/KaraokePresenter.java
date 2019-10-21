package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.KaraokeView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;

/**
 * Created by NSW00_008320 on 2018/01/30.
 */
@PresenterLifeCycle
public class KaraokePresenter extends Presenter<KaraokeView> {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject PreferSoundFx mPreferSoundFx;

    @Inject
    public KaraokePresenter() {
    }

    @Override
    void onTakeView() {
        updateView();
        updateMicrophoneSettingValue();
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

    private void updateView(){
        StatusHolder holder = mGetStatusHolder.execute();
        boolean isFxSettingEnabled = holder.getCarDeviceStatus().soundFxSettingEnabled;
        SoundFxSettingSpec spec = holder.getCarDeviceSpec().soundFxSettingSpec;
        SoundFxSettingStatus status = holder.getSoundFxSettingStatus();
        SoundFxSetting setting = holder.getSoundFxSetting();

        Optional.ofNullable(getView()).ifPresent(view -> {
            //Microphone設定のみ車載器ステータス変更で更新しないようにする
            view.setMicrophoneSettingEnabled(
                    spec.karaokeSettingSupported,
                    status.karaokeSettingEnabled && isFxSettingEnabled
            );
            view.setMicVolumeSetting(
                    spec.karaokeSettingSupported,
                    status.karaokeSettingEnabled && isFxSettingEnabled && setting.karaokeSetting,
                    setting.micVolumeSetting.min,
                    setting.micVolumeSetting.max,
                    setting.micVolumeSetting.current
            );
            view.setVocalCancelSetting(
                    spec.karaokeSettingSupported,
                    status.karaokeSettingEnabled && isFxSettingEnabled,
                    setting.vocalCancelSetting
            );
        });
    }

    private void updateMicrophoneSettingValue(){
        StatusHolder holder = mGetStatusHolder.execute();
        SoundFxSetting setting = holder.getSoundFxSetting();

        Optional.ofNullable(getView()).ifPresent(view -> {
            //Microphone設定のみ車載器ステータス変更でONOFF更新しないようにする
            view.setMicrophoneSetting(setting.karaokeSetting);
        });
    }

    public void onMicrophoneChange(boolean value){
        mPreferSoundFx.setKaraokeSetting(value);
    }

    public void onMicVolumeChange(int volume){
        mPreferSoundFx.setMicVolume(volume);
    }

    public void onVocalCancelChange(boolean value){
        mPreferSoundFx.setVocalCancel(value);
    }

    /**
     * SoundFx設定の更新通知
     *
     * @param event SoundFxChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSoundFxChangeEvent(SoundFxSettingChangeEvent event) {
        updateView();
        updateMicrophoneSettingValue();
    }

    /**
     * Sound FX設定ステータス変更通知
     *
     * @param event SoundFxSettingStatusChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSoundFxSettingStatusChangeEvent(SoundFxSettingStatusChangeEvent event) {
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

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }
}
