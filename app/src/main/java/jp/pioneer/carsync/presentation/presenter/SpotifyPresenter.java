package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AdasErrorEvent;
import jp.pioneer.carsync.domain.event.SpotifyInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlSpotifySource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.SpotifyMediaInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.util.ShortCutKeyEnabledStatus;
import jp.pioneer.carsync.presentation.util.SpotifyTextUtil;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.SpotifyView;
import timber.log.Timber;

/**
 * Spotify再生のPresenter
 */
@PresenterLifeCycle
public class SpotifyPresenter extends PlayerPresenter<SpotifyView> {
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetCase;
    @Inject ControlSpotifySource mControlCase;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject ShortCutKeyEnabledStatus mShortCutKeyEnabledStatus;
    private static final String EMPTY = "";

    /**
     * コンストラクタ
     */
    @Inject
    public SpotifyPresenter() {

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
        updateView();

        super.onResume();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);

        super.onPause();
    }

    /**
     * Spotify再生情報の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSpotifyInfoChangeAction(SpotifyInfoChangeEvent event) {
        updateView();
    }

    /**
     * 再生・一時停止の切換
     */
    public void onPlayPauseAction() {
        Timber.d("Play Pause Action");
        SpotifyMediaInfo info = getSpotifyMediaInfo();
        if(info.playbackMode != PlaybackMode.PLAY){
            Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.PLAY));
        }
        mControlCase.togglePlay();
    }

    /**
     * 曲戻しのアクション
     */
    public void onSkipPreviousAction() {
        Timber.d("Skip Previous Action");
        Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.TRACK_DOWN));
        mControlCase.skipPreviousTrack();
    }

    /**
     * 曲送りのアクション
     */
    public void onSkipNextAction() {
        Timber.d("Skip Next Action");
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
     * ThumbUpアクション
     */
    public void onThumbUpAction() {
        Timber.d("onThumbUpAction");
        SpotifyMediaInfo info = getSpotifyMediaInfo();
        if (info.radioPlaying) {
            mControlCase.setThumbUp();
        }
    }

    /**
     * ThumbDownアクション
     */
    public void onThumbDownAction() {
        Timber.d("onThumbDownAction");
        SpotifyMediaInfo info = getSpotifyMediaInfo();
        if (info.radioPlaying) {
            mControlCase.setThumbDown();
        }
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

    private SpotifyMediaInfo getSpotifyMediaInfo() {
        StatusHolder holder = mGetCase.execute();
        CarDeviceMediaInfoHolder mediaHolder = holder.getCarDeviceMediaInfoHolder();
        return mediaHolder.spotifyMediaInfo;
    }

    private void updateView() {
        SpotifyMediaInfo info = getSpotifyMediaInfo();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setModeView(info.radioPlaying);
            view.setMusicTitle(SpotifyTextUtil.getSongTitle(mContext, info));
            view.setMusicInfo(SpotifyTextUtil.getSongTitle(mContext, info),SpotifyTextUtil.getArtistName(mContext, info),
                    SpotifyTextUtil.getAlbumTitle(mContext, info),SpotifyTextUtil.getPlayingTrackSource(mContext, info));
            view.setMaxProgress(info.totalSecond);
            view.setCurrentProgress(info.currentSecond);
            view.setThumbStatus(info.thumbStatus);
            view.setRepeatImage(info.repeatMode);
            view.setShuffleImage(info.shuffleMode);
            view.setPlaybackMode(info.playbackMode);
            view.setAdasEnabled((mPreference.isAdasEnabled()&&mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN&&(mGetCase.execute().getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING))||mPreference.isAdasPseudoCooperation());

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
