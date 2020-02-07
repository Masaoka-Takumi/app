package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.AdasErrorEvent;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlTiSource;
import jp.pioneer.carsync.domain.interactor.ExitMenu;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.util.FrequencyUtil;
import jp.pioneer.carsync.presentation.util.ShortCutKeyEnabledStatus;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.TiView;

/**
 * TiPresenter
 */

public class TiPresenter extends PlayerPresenter<TiView> {

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlTiSource mControlCase;
    @Inject AppSharedPreference mPreference;
    @Inject PreferSoundFx mFxCase;
    @Inject ExitMenu mExitMenu;
    @Inject ShortCutKeyEnabledStatus mShortCutKeyEnabledStatus;
    private RadioInfo mCurrRadio;

    /**
     * コンストラクタ
     */
    @Inject
    public TiPresenter() {
    }

    @Override
    void onTakeView() {
        super.onTakeView();
    }


    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        updateView();

        super.onResume();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);

        super.onPause();
    }

    /**
     * ラジオ情報変更通知イベントハンドラ
     *
     * @param event ラジオ情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioInfoChangeEvent(RadioInfoChangeEvent event) {
        updateView();
    }

    /**
     * 画面更新
     */
    private void updateView() {
        StatusHolder holder = mStatusHolder.execute();
        CarDeviceStatus status = holder.getCarDeviceStatus();
        mCurrRadio = holder.getCarDeviceMediaInfoHolder().radioInfo;

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setFrequency(FrequencyUtil.toString(mContext, mCurrRadio.currentFrequency, mCurrRadio.frequencyUnit));
            view.setAntennaLevel((float) mCurrRadio.antennaLevel / mCurrRadio.maxAntennaLevel);
            view.setAdasEnabled((mPreference.isAdasEnabled()&&mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN&&(mStatusHolder.execute().getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING))||mPreference.isAdasPseudoCooperation());
        });
        setAdasIcon();
    }

    /**
     * 次の周波数へ
     */
    public void onNextChannelAction() {
        Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.FREQUENCY_UP));
        mControlCase.channelUp();
    }

    /**
     * 前の周波数へ
     */
    public void onPreviousChannelAction() {
        Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.FREQUENCY_DOWN));
        mControlCase.channelDown();
    }

    // MARK - EQ FX

    @Override
    void onUpdateSoundFxButton() {
        SoundFxButtonInfo info = getSoundFxButtonInfo();
        String showText = info.isShowEqMessage ? info.textEqButton : info.isShowFxMessage ? info.textFxButton : null;

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setEqFxButtonEnabled(info.isEqEnabled,info.isFxEnabled);
            view.setEqButton(info.textEqButton);
            view.setFxButton(info.textFxButton);
            if(showText != null) {
                view.displayEqFxMessage(showText);
            }
        });
    }

    /**
     * AdasErrorEventハンドラ
     * @param event AdasErrorEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdasErrorEvent(AdasErrorEvent event) {
        setAdasIcon();
    }

    private void setAdasIcon(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            int status = 0;
            AppStatus appStatus = mStatusHolder.execute().getAppStatus();
            if(appStatus.adasDetected)status = 1;
            if(appStatus.isAdasError())status = 2;
            view.setAdasIcon(status);
        });
    }

    /**
     * ボリュームアップのアクション
     */
    public void onVolumeUpAction() {
        Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.VOLUME_UP));
        mControlCase.volumeUp();
    }

    /**
     * ボリュームダウンのアクション
     */
    public void onVolumeDownAction() {
        Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.VOLUME_DOWN));
        mControlCase.volumeDown();
    }

    @Override
    protected void updateShortcutButton() {
        super.updateShortcutButton();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setShortcutKeyItems(mShortCutKeyList);
            view.setShortCutButtonEnabled(mShortCutKeyEnabledStatus.isShortCutKeyEnabled());
        });
    }

    @Override
    protected void updateNotification() {
        super.updateNotification();
        Optional.ofNullable(getView()).ifPresent(view -> view.setShortcutKeyItems(mShortCutKeyList));
    }

    @Override
    protected void updateAlexaNotification() {
        super.updateAlexaNotification();
        Optional.ofNullable(getView()).ifPresent(view ->{
            AppStatus appStatus = mStatusHolder.execute().getAppStatus();
            boolean notificationQueued = false;
            if(mPreference.getVoiceRecognitionType()== VoiceRecognizeType.ALEXA){
                notificationQueued = mStatusHolder.execute().getAppStatus().alexaNotification;
            }
            view.setAlexaNotification(notificationQueued);
        });
    }
}
