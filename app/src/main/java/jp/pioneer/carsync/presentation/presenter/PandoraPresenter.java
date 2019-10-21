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
import jp.pioneer.carsync.domain.event.PandoraInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlPandoraSource;
import jp.pioneer.carsync.domain.interactor.ExitMenu;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.PandoraMediaInfo;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.util.PandoraTextUtil;
import jp.pioneer.carsync.presentation.util.ShortCutKeyEnabledStatus;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.PandoraView;
import timber.log.Timber;

import static jp.pioneer.carsync.domain.model.PlaybackMode.STOP;

/**
 * Pandora再生のPresenter
 */
@PresenterLifeCycle
public class PandoraPresenter extends PlayerPresenter<PandoraView> {
    private static final String EMPTY = "";

    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetCase;
    @Inject ControlPandoraSource mControlCase;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject PreferSoundFx mFxCase;
    @Inject ExitMenu mExitMenu;
    @Inject ShortCutKeyEnabledStatus mShortCutKeyEnabledStatus;

    /**
     * コンストラクタ
     */
    @Inject
    public PandoraPresenter() {

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
     * Pandora再生情報の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPandoraInfoChangeAction(PandoraInfoChangeEvent event) {
        updateView();
    }

    /**
     * 再生・一時停止の切換
     */
    public void onPlayPauseAction() {
        Timber.d("Play Pause Action");
        PandoraMediaInfo info = getPandoraMediaInfo();
        if(info.playbackMode != PlaybackMode.PLAY){
            Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.PLAY));
        }
        mControlCase.togglePlay();
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
     * ThumbUpアクション
     */
    public void onThumbUpAction() {
        Timber.d("onThumbUpAction");
        mControlCase.setThumbUp();
    }

    /**
     * ThumbDownアクション
     */
    public void onThumbDownAction() {
        Timber.d("onThumbDownAction");
        mControlCase.setThumbDown();
    }

    /**
     * 設定画面への遷移アクション
     */
    public void onSettingShowAction() {
        //TODO:設定画面へ遷移
        Timber.d("Setting Show");
    }

    private PandoraMediaInfo getPandoraMediaInfo() {
        StatusHolder holder = mGetCase.execute();
        CarDeviceMediaInfoHolder mediaHolder = holder.getCarDeviceMediaInfoHolder();
        return mediaHolder.pandoraMediaInfo;
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            PandoraMediaInfo info = getPandoraMediaInfo();
            view.setMusicTitle(info.playbackMode == STOP ? EMPTY : PandoraTextUtil.getSongTitle(mContext, info));
            view.setMusicInfo(info.playbackMode == STOP ? EMPTY : PandoraTextUtil.getSongTitle(mContext, info),info.playbackMode == STOP ? EMPTY : PandoraTextUtil.getArtistName(mContext, info),
                    info.playbackMode == STOP ? EMPTY : PandoraTextUtil.getAlbumName(mContext, info),info.playbackMode == STOP ? EMPTY : PandoraTextUtil.getStationInfo(mContext, info));
            view.setMaxProgress(info.totalSecond);
            view.setCurrentProgress(info.currentSecond);
            view.setThumbStatus(info.thumbStatus);
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
            if(mPreference.getVoiceRecognitionType()== VoiceRecognizeType.ALEXA){
                notificationQueued = mGetCase.execute().getAppStatus().alexaNotification;
            }
            view.setAlexaNotification(notificationQueued);
        });
    }
}
