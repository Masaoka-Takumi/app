package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AudioSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.AudioSettingSpec;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.FxView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Created by NSW00_008316 on 2017/03/24.
 */
@PresenterLifeCycle
public class FxPresenter extends Presenter<FxView> {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject PreferSoundFx mPreferSoundFx;
    @Inject
    public FxPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        updateView();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    private void updateView(){
        StatusHolder holder = mGetStatusHolder.execute();
        boolean isFxSettingEnabled = holder.getCarDeviceStatus().soundFxSettingEnabled &&
                holder.getCarDeviceSpec().soundFxSettingSupported;
        SoundFxSettingSpec fxSpec = holder.getCarDeviceSpec().soundFxSettingSpec;
        SoundFxSettingStatus fxStatus = holder.getSoundFxSettingStatus();
        AudioSettingSpec audioSpec = holder.getCarDeviceSpec().audioSettingSpec;
        AudioSettingStatus audioStatus = holder.getAudioSettingStatus();
        ProtocolVersion version = holder.getProtocolSpec().getConnectingProtocolVersion();
        //通信プロトコル4.1以上で通信している場合、Alexa再生モード時、31バンドEQ設定を有効にする
        boolean isAlexaPlayMode = version.isLessThan(ProtocolVersion.V4_1)&&holder.getCarDeviceStatus().sourceType== MediaSourceType.APP_MUSIC&&holder.getAppStatus().appMusicAudioMode== AudioMode.ALEXA;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setEqualizerSetting(
                    audioSpec.equalizerSettingSupported,
                    audioStatus.equalizerSettingEnabled && isFxSettingEnabled && !isAlexaPlayMode
            );
            view.setLiveSimulationSetting(
                    fxSpec.liveSimulationSettingSupported,
                    fxStatus.liveSimulationSettingEnabled && isFxSettingEnabled
            );
            view.setSuperTodorokiSetting(
                    fxSpec.superTodorokiSettingSupported,
                    fxStatus.superTodorokiSettingEnabled && isFxSettingEnabled
            );
            view.setSmallCarTaSetting(
                    fxSpec.smallCarTaSettingSupported,
                    fxStatus.smallCarTaSettingEnabled && isFxSettingEnabled
            );
            view.setKaraokeSetting(
                    fxSpec.karaokeSettingSupported,
                    fxStatus.karaokeSettingEnabled && isFxSettingEnabled
            );
        });
    }

    public void onEqSettingAction(){
        mEventBus.post(new NavigateEvent(ScreenId.EQ_SETTING, createSettingsParams(mContext.getString(R.string.set_002))));
    }

    public void onLiveSettingAction(){
        mEventBus.post(new NavigateEvent(ScreenId.LIVE_SIMULATION_SETTING, createSettingsParams(mContext.getString(R.string.set_118))));
    }

    public void onTodorokiSettingAction(){
        mEventBus.post(new NavigateEvent(ScreenId.TODOROKI_SETTING, createSettingsParams(mContext.getString(R.string.set_208))));
    }

    public void onSmallCarSettingAction(){
        mEventBus.post(new NavigateEvent(ScreenId.SMALL_CAR_TA_SETTING, createSettingsParams(mContext.getString(R.string.set_196))));
    }

    public void onKaraokeSettingAction(){
        mEventBus.post(new NavigateEvent(ScreenId.KARAOKE_SETTING, createSettingsParams(mContext.getString(R.string.set_106))));
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
     * Audio設定ステータス変更通知
     *
     * @param event AudioSettingStatusChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioSettingStatus(AudioSettingStatusChangeEvent event) {
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
