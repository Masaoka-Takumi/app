package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AdasErrorEvent;
import jp.pioneer.carsync.domain.event.AppMusicAudioModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicPlayPositionChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicPlaybackModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicRepeatModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicShuffleModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicTrackChangeEvent;
import jp.pioneer.carsync.domain.interactor.ActionSoftwareShortcutKey;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ExitMenu;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.LiveSimulationSetting;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.SoundEffectType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.CaptureSetEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.SoundFxItem;
import jp.pioneer.carsync.presentation.util.AndroidMusicTextUtil;
import jp.pioneer.carsync.presentation.util.ShortCutKeyEnabledStatus;
import jp.pioneer.carsync.presentation.view.AndroidMusicView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.manager.AlexaAudioManager;
import jp.pioneer.mbg.alexa.manager.AlexaQueueManager;
import timber.log.Timber;

/**
 * Androidローカルコンテンツ再生のPresenter
 *
 * @see AndroidMusicView
 */
@PresenterLifeCycle
public class AndroidMusicPresenter extends PlayerPresenter<AndroidMusicView> {
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetCase;
    @Inject ControlAppMusicSource mControlCase;
    @Inject Context mContext;
    @Inject ActionSoftwareShortcutKey mShortcutCase;
    @Inject AppSharedPreference mPreference;
    @Inject PreferSoundFx mFxCase;
    @Inject ExitMenu mExitMenu;
    @Inject ControlMediaList mControlMediaList;
    @Inject ShortCutKeyEnabledStatus mShortCutKeyEnabledStatus;
    private static final String EMPTY = "";
    private List<SoundFxItem> mSoundFxArray = new ArrayList<SoundFxItem>(){{
        add(new SoundFxItem(SoundFxItem.ItemType.OFF,               SoundFieldControlSettingType.OFF,       SuperTodorokiSetting.OFF));
        add(new SoundFxItem(SoundFxItem.ItemType.LIVE_SIMULATION,   SoundFieldControlSettingType.CAFE,      SuperTodorokiSetting.OFF));
        add(new SoundFxItem(SoundFxItem.ItemType.LIVE_SIMULATION,   SoundFieldControlSettingType.CLUB,  SuperTodorokiSetting.OFF));
        add(new SoundFxItem(SoundFxItem.ItemType.LIVE_SIMULATION,   SoundFieldControlSettingType.CONCERT_HALL,   SuperTodorokiSetting.OFF));
        add(new SoundFxItem(SoundFxItem.ItemType.LIVE_SIMULATION,   SoundFieldControlSettingType.OPEN_AIR,      SuperTodorokiSetting.OFF));
        add(new SoundFxItem(SoundFxItem.ItemType.TODOROKI,          SoundFieldControlSettingType.OFF,       SuperTodorokiSetting.LAST));
    }};
    /** Alexaマネージャ. */
    private AmazonAlexaManager mAmazonAlexaManager;
    private AndroidMusicPresenter.AlexaCallback mAlexaCallback = new AndroidMusicPresenter.AlexaCallback();
    private AudioMode mAudioMode = AudioMode.MEDIA;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    /**
     * コンストラクタ
     */
    @Inject
    public AndroidMusicPresenter() {
    }

    @Override
    void onTakeView() {
		Optional.ofNullable(getView()).ifPresent(view -> view.setColor(mPreference.getUiColor().getResource()));
        super.onTakeView();
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        //updateShortcutButton();
        updateAlexaView();
        updateView(true);

        super.onResume();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.removeAlexaCallback(mAlexaCallback);
        }
        super.onPause();
    }

    public void updateAlexaView(){
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        mAudioMode = appStatus.appMusicAudioMode;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setAudioMode(mAudioMode);
        });
        if(mAudioMode == AudioMode.ALEXA){
            mSoundFxArray = new ArrayList<SoundFxItem>(){{
                add(new SoundFxItem(SoundFxItem.ItemType.OFF,               SoundFieldControlSettingType.OFF,       SuperTodorokiSetting.OFF));
                add(new SoundFxItem(SoundFxItem.ItemType.TODOROKI,          SoundFieldControlSettingType.OFF,       SuperTodorokiSetting.LAST));
            }};
            mAmazonAlexaManager = AmazonAlexaManager.getInstance();
            if (mAmazonAlexaManager != null) {
                mAmazonAlexaManager.addAlexaCallback(mAlexaCallback);
            }
            RenderPlayerInfoItem playerInfoItem = appStatus.playerInfoItem;
            Optional.ofNullable(getView()).ifPresent(view -> {
                view.setAmazonMusicLayout();
                view.setAmazonMusicInfo(playerInfoItem);
                view.setShortcutKeyItems(mShortCutKeyList);
                if(isPlaying()){
                    view.setPlaybackMode(PlaybackMode.PLAY);
                }else{
                    view.setPlaybackMode(PlaybackMode.PAUSE);
                }
                view.setEqFxButtonEnabled(
                        false,
                        true);
            });
            mControlCase.sendMusicInfo();
        }else{
            mSoundFxArray = new ArrayList<SoundFxItem>(){{
                add(new SoundFxItem(SoundFxItem.ItemType.OFF,               SoundFieldControlSettingType.OFF,       SuperTodorokiSetting.OFF));
                add(new SoundFxItem(SoundFxItem.ItemType.LIVE_SIMULATION,   SoundFieldControlSettingType.CAFE,      SuperTodorokiSetting.OFF));
                add(new SoundFxItem(SoundFxItem.ItemType.LIVE_SIMULATION,   SoundFieldControlSettingType.CLUB,  SuperTodorokiSetting.OFF));
                add(new SoundFxItem(SoundFxItem.ItemType.LIVE_SIMULATION,   SoundFieldControlSettingType.CONCERT_HALL,   SuperTodorokiSetting.OFF));
                add(new SoundFxItem(SoundFxItem.ItemType.LIVE_SIMULATION,   SoundFieldControlSettingType.OPEN_AIR,      SuperTodorokiSetting.OFF));
                add(new SoundFxItem(SoundFxItem.ItemType.TODOROKI,          SoundFieldControlSettingType.OFF,       SuperTodorokiSetting.LAST));
            }};
        }
    }

    public void updateView(boolean isUpdateTrackArt){
        SmartPhoneStatus status = getSmartPhoneStatus();
        Optional.ofNullable(getView()).ifPresent(view -> {
            boolean isStop = mStatusHolder.execute().getSmartPhoneStatus().playbackMode == PlaybackMode.STOP;
            boolean isFormatRead = mStatusHolder.execute().getAppStatus().isFormatRead;
            if(mAudioMode==AudioMode.MEDIA) {
                view.setRepeatImage(getSmartPhoneStatus().repeatMode);
                view.setShuffleImage(getSmartPhoneStatus().shuffleMode);
                AndroidMusicMediaInfo info = getAndroidMusicMediaInfo();
                view.setMusicTitle(AndroidMusicTextUtil.getSongTitle(mContext, status, info));
                view.setMusicInfo(AndroidMusicTextUtil.getSongTitle(mContext, status, info),AndroidMusicTextUtil.getArtistName(mContext, status, info),AndroidMusicTextUtil.getAlbumTitle(mContext, status, info),AndroidMusicTextUtil.getGenreName(mContext, status, info));
                //view.setMusicArtist(AndroidMusicTextUtil.getArtistName(mContext, status, info));
                //view.setMusicAlbum(AndroidMusicTextUtil.getAlbumTitle(mContext, status, info));
                //view.setMusicGenre(AndroidMusicTextUtil.getGenreName(mContext, status, info));
                if (status.playbackMode == PlaybackMode.ERROR) {
                    view.setMusicAlbumArt(null);
                    view.setMaxProgress(0);
                    view.setCurrentProgress(0);
                } else {
                    if (isUpdateTrackArt) {
                        view.setMusicAlbumArt(info.artworkImageLocation);
                    }
                    view.setMaxProgress(info.durationInSec);
                    view.setCurrentProgress(info.positionInSec);
                }

                view.setPlaybackMode(status.playbackMode);
                ListType type = mStatusHolder.execute().getCarDeviceStatus().listType;
                view.setListEnabled((!isFormatRead || isStop) && type != ListType.LIST_UNAVAILABLE);
                view.setEqFxButtonEnabled(!isFormatRead || isStop,!isFormatRead || isStop);
            }
            view.setAdasEnabled((mPreference.isAdasEnabled()&&mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN&&(mGetCase.execute().getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING))||mPreference.isAdasPseudoCooperation());
        });
        setAdasIcon();
    }

    @Override
    public void onShowList() {
        //HOMEなどに遷移後に呼び出される場合があるため、画面表示中のみ実行
        Optional.ofNullable(getView()).ifPresent(view -> {
            //すでにダイアログ表示中であれば背景ぼかししない
            if(!view.isShowPlayerTabContainer()) {
                mEventBus.post(new BackgroundChangeEvent(true));
            }
            mEventBus.post(new NavigateEvent(ScreenId.PLAYER_LIST_CONTAINER, Bundle.EMPTY));
        });
    }

    @Override
    void onListTypeChange() {
        updateView(false);
    }

    /**
     * 再生・一時停止の切換
     */
    public void onPlayPauseAction() {
        if(mAudioMode==AudioMode.MEDIA) {
            if (getSmartPhoneStatus().playbackMode != PlaybackMode.PLAY) {
                Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.PLAY));
            }
            mControlCase.togglePlay();
        }else{
            if (isPlaying()) {
                AlexaAudioManager.getInstance().doPause(null);
            } else {
                AlexaAudioManager.getInstance().doPlay(null);
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.showGesture(GestureType.PLAY);
                });
            }
        }
    }

    /**
     * SEEK開始
     */
    public void onStartSeek() {
        mEventBus.post(new CaptureSetEvent(true));
    }

    /**
     * SEEK終了
     */
    public void onFinishSeek() {
        mEventBus.post(new CaptureSetEvent(false));
    }

    /**
     * SEEKアクション
     */
    public void onSeekAction(int time) {
        if(time>0){
            mControlCase.fastForward(time);
        }else{
            mControlCase.rewind(-time);
        }
    }

    /**
     * 曲戻しのアクション
     */
    public void onSkipPreviousAction() {
        Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.TRACK_DOWN));
        if(mAudioMode==AudioMode.MEDIA) {
            mControlCase.skipPreviousTrack();
        }else{
            AlexaAudioManager.getInstance().doPrev(null);
        }
    }

    /**
     * 曲送りのアクション
     */
    public void onSkipNextAction() {
        Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.TRACK_UP));
        if(mAudioMode==AudioMode.MEDIA){
            mControlCase.skipNextTrack();
        }else{
            AlexaAudioManager.getInstance().doNext(null);
        }
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
     * NowPlayingList画面への遷移アクション
     */
    public void onNowPlayingAction() {
        mEventBus.post(new BackgroundChangeEvent(true));
        mEventBus.post(new NavigateEvent(ScreenId.NOW_PLAYING_LIST, Bundle.EMPTY));
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

    /**
     * SoundFx切り換えアクション
     */
    public void onSelectFxAction() {
        SoundFxSetting fxSetting = getFxSetting();

        mIsEqClick = false;
        mIsFxClick = true;

        int soundFxStr;
        SuperTodorokiSetting superTodorokiSetting = fxSetting.superTodorokiSetting;
        LiveSimulationSetting liveSimulationSetting = fxSetting.liveSimulationSetting;
        int index = 0;
        if(superTodorokiSetting == SuperTodorokiSetting.OFF &&liveSimulationSetting.soundFieldControlSettingType==SoundFieldControlSettingType.OFF){
            index = 0;
        }else if(liveSimulationSetting.soundFieldControlSettingType!=SoundFieldControlSettingType.OFF){
            for(SoundFxItem item : mSoundFxArray){
                if(item.soundFieldControlSetting == liveSimulationSetting.soundFieldControlSettingType){
                    index = mSoundFxArray.indexOf(item);
                    break;
                }
            }
        }else{
            for(SoundFxItem item : mSoundFxArray){
                if(item.type == SoundFxItem.ItemType.TODOROKI){
                    index = mSoundFxArray.indexOf(item);
                    break;
                }
            }
        }
        if(index >= mSoundFxArray.size() - 1){
            index = 0;
        }else{
            index = index + 1 ;
        }
        SoundFxItem nextItem = mSoundFxArray.get(index);

        mExitMenu.execute();
        if(nextItem.type== SoundFxItem.ItemType.LIVE_SIMULATION) {
            SoundEffectType effectType = liveSimulationSetting.soundEffectSettingType.type;
            mFxCase.setLiveSimulation(nextItem.soundFieldControlSetting, effectType);
        }else {
            mFxCase.setSuperTodoroki(nextItem.superTodorokiSetting);
        }
    }

    /**
     * EQ切り換えアクション
     */
    public void onSelectVisualAction() {
        mExitMenu.execute();

        mIsEqClick = true;
        mIsFxClick = false;

        SoundFxSetting fxSetting = getFxSetting();
        if(fxSetting.liveSimulationSetting.soundFieldControlSettingType != SoundFieldControlSettingType.OFF){
            mFxCase.setLiveSimulation(SoundFieldControlSettingType.OFF, fxSetting.liveSimulationSetting.soundEffectSettingType.type);
        } else if (fxSetting.superTodorokiSetting != SuperTodorokiSetting.OFF){
            mFxCase.setSuperTodoroki(SuperTodorokiSetting.OFF);
        } else {
            SoundFxSettingEqualizerType equalizerType = fxSetting.soundFxSettingEqualizerType;
            int index = mEqArray.indexOf(equalizerType);
            if (index < 0) {
                return;
            } else {
                if (index >= mEqArray.size() - 1) {
                    index = 0;
                } else {
                    for(SoundFxSettingEqualizerType eq : mEqArray) {
                        index++;
                        if (!(!mPreference.isDebugSpecialEqEnabled() && mEqArray.get(index).code >= (1 << 8))) {
                            break;
                        }
                    }
                }
            }
            mFxCase.setEqualizer(mEqArray.get(index));
        }
    }

    private SoundFxSetting getFxSetting() {
        StatusHolder holder = mGetCase.execute();
        return holder.getSoundFxSetting();
    }

    private SmartPhoneStatus getSmartPhoneStatus() {
        StatusHolder holder = mGetCase.execute();
        return holder.getSmartPhoneStatus();
    }

    private AndroidMusicMediaInfo getAndroidMusicMediaInfo() {
        StatusHolder holder = mGetCase.execute();
        CarDeviceMediaInfoHolder mediaHolder = holder.getCarDeviceMediaInfoHolder();
        return mediaHolder.androidMusicMediaInfo;
    }

    // subscribe

    /**
     * 再生位置（時間）の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayPositionChangeAction(AppMusicPlayPositionChangeEvent event) {
        Optional.ofNullable(getView()).ifPresent(view -> {
                if(mAudioMode==AudioMode.MEDIA) {
                    view.setCurrentProgress(getAndroidMusicMediaInfo().positionInSec);
                }else{
                    view.setCurrentProgress(mGetCase.execute().getAppStatus().alexaAudioPlayPosition);
                }
            }
        );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppMusicPlaybackModeChangeEvent(AppMusicPlaybackModeChangeEvent event) {
        updateView(false);
    }

    /**
     * プレイヤー状態の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackChangeAction(AppMusicTrackChangeEvent event) {
        updateView(true);
    }

    /**
     * リピート状態の変更イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRepeatModeChangeAction(AppMusicRepeatModeChangeEvent event) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setRepeatImage(getSmartPhoneStatus().repeatMode));
    }

    /**
     * シャッフル状態の変更イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShuffleModeChangeAction(AppMusicShuffleModeChangeEvent event) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setShuffleImage(getSmartPhoneStatus().shuffleMode));
    }

    // MARK - EQ FX

    @Override
    void onUpdateSoundFxButton() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            boolean isStop = mStatusHolder.execute().getSmartPhoneStatus().playbackMode == PlaybackMode.STOP;
            boolean isFormatRead = mStatusHolder.execute().getAppStatus().isFormatRead;
            SoundFxButtonInfo info = getSoundFxButtonInfo();
            String showText = info.isShowEqMessage ? info.textEqButton : info.isShowFxMessage ? info.textFxButton : null;
            if(mAudioMode==AudioMode.MEDIA) {
                view.setEqFxButtonEnabled(
                        info.isEqEnabled && (!isFormatRead || isStop),
                        info.isFxEnabled && (!isFormatRead || isStop));
            }else{
                //AlexaModeではEQボタンはFlatのみであるため無効
                view.setEqFxButtonEnabled(
                        false,
                        info.isFxEnabled);
            }
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
            view.setShortCutButtonEnabled(mShortCutKeyEnabledStatus.isShortCutKeyEnabled());
            view.setShortcutKeyItems(mShortCutKeyList);
        });
        updateNotification();
        updateAlexaNotification();
    }

    @Override
    protected void updateNotification() {
        super.updateNotification();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setShortcutKeyItems(mShortCutKeyList);
        });
    }

    /**
     * アレクサのイベントのコールバックを受けるメソッド.
     */
    private class AlexaCallback implements AmazonAlexaManager.IAlexaCallback {
        @Override
        public void onLoginSuccess() {

        }

        @Override
        public void onLoginFailed() {

        }

        @Override
        public void onLogout() {

        }

        @Override
        public void onConnect() {

        }

        @Override
        public void onDisConnect() {

        }

        @Override
        public void onNetworkConnect() {

        }

        @Override
        public void onNetworkDisconnect() {

        }

        @Override
        public void onRecordingStart() {

        }

        @Override
        public void onRecordingMonitor(double db, int hertz) {

        }

        @Override
        public void onRecordingStop(boolean isCancel) {

        }

        @Override
        public void onSpeakingPrepare() {

        }

        @Override
        public void onSpeakingPrepared() {

        }

        @Override
        public void onSpeakingStart() {

        }

        @Override
        public void onSpeakingResume() {

        }

        @Override
        public void onSpeakingPause() {

        }

        @Override
        public void onSpeakingStop() {

        }

        @Override
        public void onSpeakingComplete() {

        }

        @Override
        public void onAudioError() {
            Timber.d("onAudioError");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Optional.ofNullable(getView()).ifPresent(view -> {
                        view.setPlaybackMode(PlaybackMode.PAUSE);
                        //view.setControlEnable(false);
                    });
                }
            });
        }

        @Override
        public void onAudioComplete() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Optional.ofNullable(getView()).ifPresent(view -> {
                        view.setPlaybackMode(PlaybackMode.PAUSE);
                        //view.setControlEnable(false);
                    });
                }
            });
        }

        @Override
        public void onAlertStarted() {

        }

        @Override
        public void onShortAlertStarted() {

        }

        @Override
        public void onAlertStopped() {

        }

        @Override
        public void onSetAlert() {

        }

        @Override
        public void onStopAlertAll() {

        }

        @Override
        public void onPersistVisualIndicator() {

        }

        @Override
        public void onClearVisualIndicator() {

        }

        @Override
        public void onAudioIndicatorStarted() {

        }

        @Override
        public void onAudioIndicatorStopped() {

        }

        @Override
        public void onSetVolume(float volume) {

        }

        @Override
        public void onAdjustVolume(float volume) {

        }

        @Override
        public void onSetMute(boolean isMute) {

        }

        @Override
        public void onNoResponse() {

        }

        @Override
        public void onChannelActiveChange(AlexaQueueManager.AlexaChannel channel, boolean isActive) {

        }

        @Override
        public void onMicrophonePermission(int state) {

        }

        @Override
        public void onNoDirectiveAtSendEventResponse() {

        }

        @Override
        public void onReceiveRenderPlayerInfo(RenderPlayerInfoItem playerInfoItem) {
            Timber.d("onReceiveRenderPlayerInfo");
            AppStatus appStatus = mGetCase.execute().getAppStatus();
            appStatus.playerInfoItem = playerInfoItem;
            if(mAudioMode==AudioMode.ALEXA) {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.setAmazonMusicInfo(playerInfoItem);
                });
            }
        }

        @Override
        public void onAudioPrepare() {
            Timber.d("onAudioPrepare");
            //mAvLayoutHandler.onAudioPrepare();
        }

        @Override
        public void onAudioPrepared() {
            Timber.d("onAudioPrepared");
            //mAvLayoutHandler.onAudioPrepared();
        }

        @Override
        public void onAudioStart() {
            Timber.d("onAudioStart");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Optional.ofNullable(getView()).ifPresent(view -> view.setPlaybackMode(PlaybackMode.PLAY));
                }
            });
            //mAvLayoutHandler.onAudioStart();
        }

        @Override
        public void onAudioResume() {
            Timber.d("onAudioResume");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Optional.ofNullable(getView()).ifPresent(view -> view.setPlaybackMode(PlaybackMode.PLAY));
                }
            });
        }

        @Override
        public void onAudioPause() {
            Timber.d("onAudioPause");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Optional.ofNullable(getView()).ifPresent(view -> view.setPlaybackMode(PlaybackMode.PAUSE));
                }
            });
        }

        @Override
        public void onAudioStop() {
            Timber.d("onAudioStop");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Optional.ofNullable(getView()).ifPresent(view -> view.setPlaybackMode(PlaybackMode.PAUSE));
                }
            });
        }

        @Override
        public void onAudioUpdateProgress(int current, int duration) {
            Timber.d("onAudioUpdateProgress current=" + current+ ", duration=" + duration);
            Optional.ofNullable(getView()).ifPresent(view -> {
                view.setMaxProgress(duration);
                view.setCurrentProgress(current);
            });
        }

        @Override
        public void onSystemError() {

        }

        @Override
        public void onCapabilitiesSendSuccess() {

        }

        @Override
        public void onSetNaviDestination(Double latitude, Double longitude, String name) {

        }

        @Override
        public void onRecordingNotAvailable() {

        }
        @Override
        public void onWLAudioFocusLoss() {

        }

        @Override
        public void onDecodeStart() {

        }

        @Override
        public void onDecodeFinish() {

        }
    }

    /**
     * AppMusicAudioModeChangeEventハンドラ
     * @param event AppMusicAudioModeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppMusicAudioModeChangeEvent(AppMusicAudioModeChangeEvent event) {
        if(mGetCase.execute().getAppStatus().appMusicAudioMode!=mAudioMode) {
            updateAlexaView();
            updateView(true);
            onUpdateSoundFxButton();
            updateNotification();
            updateAlexaNotification();
        }
    }

    public void onExitAlexaMode(){
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.appMusicAudioMode = AudioMode.MEDIA;
        appStatus.playerInfoItem = null;
        mAudioMode = AudioMode.MEDIA;
        AlexaAudioManager.getInstance().doStop();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setAudioMode(mAudioMode);
        });
        mEventBus.post(new AppMusicAudioModeChangeEvent());
        updateShortcutButton();
        updateAlexaView();
        updateView(true);
        onUpdateSoundFxButton();
        mControlCase.sendMusicInfo();
    }

    /**
     * 音楽再生状態を取得するメソッド.
     *
     * @return
     */
    private boolean isPlaying() {
        boolean isPlaying = false;
        if (AlexaAudioManager.getInstance().getAlexaPlayer() != null) {
            isPlaying = AlexaAudioManager.getInstance().getAlexaPlayer().isPlaying();
        } else {
            isPlaying = false;
        }
        return isPlaying;
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

    @Override
    protected void updateVoiceRecognitionType() {
        if(mAudioMode==AudioMode.ALEXA) {
            onExitAlexaMode();
        }
    }
    public void exitList(){
        mControlMediaList.exitList();
    }
}
