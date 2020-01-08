package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.AdasErrorEvent;
import jp.pioneer.carsync.domain.event.UsbInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlUsbSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.UsbMediaInfo;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.util.ShortCutKeyEnabledStatus;
import jp.pioneer.carsync.presentation.util.UsbTextUtil;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.UsbView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;


/**
 * USB再生のPresenter
 */

public class UsbPresenter extends PlayerPresenter<UsbView> {
    private static final String EMPTY = "";
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetCase;
    @Inject ControlUsbSource mControlCase;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject ControlMediaList mMediaCase;
    @Inject ShortCutKeyEnabledStatus mShortCutKeyEnabledStatus;

    /**
     * コンストラクタ
     */
    @Inject
    public UsbPresenter() {
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setColor(mPreference.getUiColor().getResource());
        });

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

    @Override
    public void onShowList() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(!view.isShowUsbList()) {
                mEventBus.post(new BackgroundChangeEvent(true));
            }
            mEventBus.post(new NavigateEvent(ScreenId.USB_LIST, Bundle.EMPTY));
        });
    }

    @Override
    void onListTypeChange() {
        updateView();
    }

    /**
     * USB状態の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUsbMediaInfoChangeAction(UsbInfoChangeEvent event) {
        updateView();
    }

    /**
     * 再生・一時停止の切換
     */
    public void onPlayPauseAction() {
        UsbMediaInfo info = getUsbMediaInfo();
        if(info.playbackMode != PlaybackMode.PLAY){
            Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.PLAY));
        }
        mControlCase.togglePlay();
    }

    /**
     * 曲戻しのアクション
     */
    public void onSkipPreviousAction() {
        Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.TRACK_DOWN));
        mControlCase.skipPreviousTrack();
    }

    /**
     * 曲送りのアクション
     */
    public void onSkipNextAction() {
        Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.TRACK_UP));
        mControlCase.skipNextTrack();
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

    /**
     * 設定画面への遷移アクション
     */
    public void onSettingShowAction() {
        //TODO:設定画面へ遷移
        Timber.d("Setting Show");
    }

    /**
     * リピート状態の切換
     */
    public void onRepeatAction() {
        mControlCase.toggleRepeatMode();
    }

    /**
     * シャッフル状態の切換
     */
    public void onShuffleAction() {
        mControlCase.toggleShuffleMode();
    }

    private UsbMediaInfo getUsbMediaInfo() {
        StatusHolder holder = mGetCase.execute();
        CarDeviceMediaInfoHolder mediaHolder = holder.getCarDeviceMediaInfoHolder();
        return mediaHolder.usbMediaInfo;
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            ListType type = mStatusHolder.execute().getCarDeviceStatus().listType;
            UsbMediaInfo info = getUsbMediaInfo();
            view.setMusicTitle(UsbTextUtil.getSongTitleForPlayer(mContext, info));
            view.setMusicInfo(UsbTextUtil.getSongTitleForPlayer(mContext, info), UsbTextUtil.getArtistNameForPlayer(mContext, info), UsbTextUtil.getAlbumNameForPlayer(mContext, info));
            view.setMaxProgress(info.totalSecond);
            view.setCurrentProgress(info.currentSecond);
            view.setRepeatImage(info.repeatMode);
            view.setShuffleImage(info.shuffleMode);
            view.setPlaybackMode(info.playbackMode);
            view.setListEnabled(type != ListType.LIST_UNAVAILABLE);
            view.setAdasEnabled((mPreference.isAdasEnabled()&&mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN&&(mStatusHolder.execute().getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING))||mPreference.isAdasPseudoCooperation());

        });
        setAdasIcon();
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
            AppStatus appStatus = mGetCase.execute().getAppStatus();
            if(appStatus.adasDetected)status = 1;
            if(appStatus.isAdasError())status = 2;
            view.setAdasIcon(status);
        });
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
            AppStatus appStatus = mGetCase.execute().getAppStatus();
            boolean notificationQueued = false;
            if(appStatus.isAlexaAvailableCountry && mPreference.getVoiceRecognitionType()== VoiceRecognizeType.ALEXA){
                notificationQueued = mGetCase.execute().getAppStatus().alexaNotification;
            }
            view.setAlexaNotification(notificationQueued);
        });
    }
}
