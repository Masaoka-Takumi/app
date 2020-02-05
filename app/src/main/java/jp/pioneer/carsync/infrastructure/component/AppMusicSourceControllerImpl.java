package jp.pioneer.carsync.infrastructure.component;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.annimon.stream.Stream;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Inject;

import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.event.AppStateChangeEvent;
import jp.pioneer.carsync.application.event.AppStateChangeEvent.AppState;
import jp.pioneer.carsync.domain.component.AppMusicSourceController;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.event.CustomEqualizerSettingChangeEvent;
import jp.pioneer.carsync.domain.event.EqualizerSettingChangeEvent;
import jp.pioneer.carsync.domain.event.LiveSimulationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SuperTodorokiSettingChangeEvent;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.LiveSimulationSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SmartPhoneMediaInfoType;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.SoundEffectSettingType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.entity.SmartPhoneMediaCommand;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStartedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSmartPhoneMediaCommandEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpStatusUpdateEvent;
import jp.pioneer.carsync.infrastructure.database.AppMusicPlaylistCursor;
import jp.pioneer.carsync.infrastructure.repository.NowPlayingListRepositoryImpl;
import jp.pioneer.carsync.infrastructure.util.AppMusicUtil;
import jp.pioneer.mbg.alexa.manager.AlexaAudioManager;
import jp.pioneer.mle.pmg.player.PMGPlayer;
import jp.pioneer.mle.pmg.player.PMGPlayerListener;
import jp.pioneer.mle.pmg.player.data.FilterStatus;
import jp.pioneer.mle.pmg.player.data.MultiBandEqualizerStatus;
import jp.pioneer.mle.pmg.player.data.PlayRange;
import jp.pioneer.mle.pmg.player.data.PlayerStatus;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.domain.content.AppMusicContract.Genre.getName;
import static jp.pioneer.carsync.domain.content.AppMusicContract.PlayParams.createPlayParams;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createAllSongs;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createGenresForAudioId;
import static jp.pioneer.carsync.domain.content.AppMusicContract.Song.getAlbum;
import static jp.pioneer.carsync.domain.content.AppMusicContract.Song.getArtist;
import static jp.pioneer.carsync.domain.content.AppMusicContract.Song.getArtworkUri;
import static jp.pioneer.carsync.domain.content.AppMusicContract.Song.getData;
import static jp.pioneer.carsync.domain.content.AppMusicContract.Song.getId;
import static jp.pioneer.carsync.domain.content.AppMusicContract.Song.getTitle;
import static jp.pioneer.carsync.domain.content.AppMusicContract.Song.getTrack;
import static jp.pioneer.carsync.infrastructure.component.AppMusicSourceControllerImpl.Status.CLOSED;
import static jp.pioneer.carsync.infrastructure.component.AppMusicSourceControllerImpl.Status.ERROR;
import static jp.pioneer.carsync.infrastructure.component.AppMusicSourceControllerImpl.Status.IDLE;
import static jp.pioneer.carsync.infrastructure.component.AppMusicSourceControllerImpl.Status.INVALID;
import static jp.pioneer.carsync.infrastructure.component.AppMusicSourceControllerImpl.Status.PAUSED;
import static jp.pioneer.carsync.infrastructure.component.AppMusicSourceControllerImpl.Status.PLAYING;
import static jp.pioneer.carsync.infrastructure.component.AppMusicSourceControllerImpl.Status.PREPARING;
import static jp.pioneer.carsync.infrastructure.util.AppMusicUtil.APPLAUSE_FOLDER_NAME;
import static jp.pioneer.carsync.infrastructure.util.AppMusicUtil.convertStepValueCarDeviceToMle;
import static jp.pioneer.carsync.infrastructure.util.AppMusicUtil.convertStepValueMleToCarDevice;
import static jp.pioneer.mle.pmg.player.PMGPlayer.ResultCode.SUCCESS;

/**
 * AppMusic操作クラス
 */
public class AppMusicSourceControllerImpl extends SourceControllerImpl
        implements AppMusicSourceController, PMGPlayerListener, AudioManager.OnAudioFocusChangeListener {
    private static final float DEFAULT_PMG_VOLUME = -6f;
    private static final int SAMPLING = 44100;
    private static final int CHANNEL_NUM = 2;
    private static final int MAX_RECORDS = 1;
    private static final String MUSIC_DB_NAME = "music.db";
    private static final long TRACK_POSITION_MONITOR_INTERVAL = 100;
    private static final int SPE_ANA_SAMPLE_COUNT = 4096;
    private static final int SPE_ANA_NUM_BANDS = 31;
    private static final boolean SPE_ANA_OVERLAP_MODE = false;
    private static final int SPE_ANA_INTERVAL = 90;

    @Inject App mApp;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject AudioManager mAudioManager;
    @Inject TelephonyManager mTelephonyManager;
    @Inject PMGPlayer mPlayer;
    @Inject EventBus mEventBus;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    private Status mStatus = Status.CLOSED;
    private ShuffleMode mShuffleMode;
    private SmartPhoneRepeatMode mRepeatMode;
    private boolean mEqUseStatus;
    private AppMusicPlaylistCursor mCursor;
    private int mPendingPlayPosition;
    private AppMusicContract.PlayParams mPlayParams;
    private int mMusicDuration;
    private TrackPositionMonitor mTrackPositionMonitor;
    private PlayResumeTask mPlayResumeTask = new PlayResumeTask();
    private boolean mIsPreventNoticePlaybackMode;
    private AppMusicUtil.SeekCalculator mSeekCalculator;
    private boolean mIsRequirePermission;
    private final Object mContent = new Object();
    private final WeakHashMap<OnSpeAnaDataListener, Object> mSpeAnaDataListeners = new WeakHashMap<>();
    private boolean mIsStartSpeAna;
    private boolean mIsReturnToPlayMode;
    private ArrayList<Float> mCurrentBandList = new ArrayList<>();
    private AudioFocus mAudioFocus;
    private SoundFieldControlSettingType mCurrentSoundFieldControlSettingType;
    private SoundEffectSettingType mCurrentSoundEffectSettingType;
    private AudioAttributes mPlaybackAttributes;
    private AudioFocusRequest mFocusRequest;
    enum AudioFocus {
        NONE,
        LOSS,
        GAIN
    }

    enum Status {
        CLOSED,
        PREPARING,
        IDLE {
            @Override
            Status toggle() {
                return PLAYING;
            }
        },
        PLAYING {
            @Override
            Status toggle() {
                return PAUSED;
            }
        },
        PAUSED {
            @Override
            Status toggle() {
                return PLAYING;
            }
        },
        INVALID,
        ERROR;

        Status toggle() {
            return this;
        }
    }

    /**
     * コンストラクタ
     */
    @Inject
    public AppMusicSourceControllerImpl() {
    }

    /**
     * 初期化.
     */
    public void initialize() {
        Timber.i("initialize()");

        mEventBus.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onActive() {
        Timber.i("onActive()");
        if (mStatus == INVALID) {
            return;
        }

        if (mStatus == CLOSED) {
            try {
                mTrackPositionMonitor = createTrackPositionMonitor();
                mSeekCalculator = AppMusicUtil.createSeekCalculator();
                openPMGPlayer();

                // 初回イコライザー設定,ライブシミュレーション設定反映
                updateEqualizer();
                updateLiveSimulation();
            } catch (UnsatisfiedLinkError | Exception e) {
                Timber.e(e);
                setStatus(INVALID);
                return;
            }

            if (mPlayParams == null) {
                mShuffleMode = mPreference.getAppMusicShuffleMode();
                mRepeatMode = mPreference.getAppMusicRepeatMode();
                restoreState();
            }

            setStatus(PREPARING);
        }

        if (mStatus == PREPARING || mStatus == ERROR) {
            try {
                prepare(mPlayParams);
            } catch (PMGPlayerException e) {
                Timber.e(e);
                return;
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        if (mCursor != null) {
            updateAppMusicInfo();
        }

        if (!mSpeAnaDataListeners.isEmpty()) {
            startSpectrumAnalyze();
        }

        if(!mStatusHolder.getAppStatus().isLaunchedThirdPartyAudioApp) {
            mAudioFocus = AudioFocus.GAIN;
            setSmartPhoneEqUseStatus(true);
        } else {
            mAudioFocus = AudioFocus.NONE;
            setSmartPhoneEqUseStatus(false);
            SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
            AndroidMusicMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo;
            postSmartPhoneStatus(smartPhoneStatus);
            postAudioInfo(info);
            mCarDeviceConnection.sendPacket(mPacketBuilder.createSmartPhoneAudioPlaybackTimeNotification(
                    MediaSourceType.APP_MUSIC, info.durationInSec, info.positionInSec));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onInactive() {
        Timber.i("onInactive()");

        // ローカル再生停止
        try {
            setSmartPhoneEqUseStatus(false);
            pause();
        } catch (PMGPlayerException e) {
            Timber.e(e);
        } finally {
            saveState();
        }
        AppStatus appStatus = mStatusHolder.getAppStatus();
        if (appStatus.appMusicAudioMode == AudioMode.ALEXA) {
            if (isPlaying()){
                AlexaAudioManager.getInstance().onBackgroundContentsChannel();
            }
        }
        // 3rd app再生停止
        dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE);

        mAudioFocus = AudioFocus.NONE;
        stopSpectrumAnalyze();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void play(AppMusicContract.PlayParams params) {
        Timber.i("play() params = " + params);

        try {
            Status status = mStatus.toggle();
            if (status == PAUSED) {
                pause();
                status = mStatus.toggle();
            }

            if (status == PLAYING || mStatus == PREPARING || mStatus == ERROR) {
                try {
                    mIsPreventNoticePlaybackMode = true;
                    setStatus(PREPARING);
                    if (prepare(params)) {
                        play(AppMusicPlaylistCursor.Action.PREPARE);
                    } else {
                        mIsPreventNoticePlaybackMode = false;
                        updatePlayStatus(PREPARING);
                    }
                } catch (PMGPlayerException e) {
                    Timber.e(e);
                    return;
                } finally {
                    mIsPreventNoticePlaybackMode = false;
                }
            }
        } catch (PMGPlayerException e) {
            Timber.e(e);
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void togglePlay() {
        Timber.i("togglePlay()");
        try {
            switch (mStatus.toggle()) {
                case PLAYING:
                    play(AppMusicPlaylistCursor.Action.NEXT);
                    break;
                case PAUSED:
                    pause();
                    break;
                default:
                    break;
            }
        } catch (PMGPlayerException e) {
            Timber.e(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toggleShuffleMode() {
        Timber.i("toggleShuffleMode()");

        setShuffleMode(mShuffleMode.toggle());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toggleRepeatMode() {
        Timber.i("toggleRepeatMode()");

        setRepeatMode(mRepeatMode.toggle());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skipNextTrack() {
        Timber.i("skipNextTrack()");

        if (mCursor == null) {
            return;
        }

        boolean isPlaying = mStatus == PLAYING;

        if (!mCursor.skipNext()) {
            return;
        }

        try {
            mIsPreventNoticePlaybackMode = true;
            pause();
            prepareAudio(AppMusicPlaylistCursor.Action.NEXT);
            if (isPlaying) {
                play(AppMusicPlaylistCursor.Action.NEXT);
            }
        } catch (PMGPlayerException e) {
            Timber.e(e);
            return;
        } finally {
            mIsPreventNoticePlaybackMode = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skipPreviousTrack() {
        Timber.i("skipPreviousTrack()");

        if (mCursor == null) {
            return;
        }

        boolean isPlaying = mStatus == PLAYING;

        if (!mCursor.skipPrevious()) {
            return;
        }

        try {
            mIsPreventNoticePlaybackMode = true;
            pause();
            prepareAudio(AppMusicPlaylistCursor.Action.PREVIOUS);
            if (isPlaying) {
                play(AppMusicPlaylistCursor.Action.PREVIOUS);
            }
        } catch (PMGPlayerException e) {
            Timber.e(e);
            return;
        } finally {
            mIsPreventNoticePlaybackMode = false;
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void volumeUp() {
        Timber.i("volumeUp()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.VOLUME_UP);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void volumeDown() {
        Timber.i("volumeDown()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.VOLUME_DOWN);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSpeAnaDataListener(@Nullable OnSpeAnaDataListener listener) {
        Timber.i("addSpeAnaDataListener()");
        checkNotNull(listener);

        synchronized (this) {
            mSpeAnaDataListeners.put(listener, mContent);
        }

        if (isActive()) {
            startSpectrumAnalyze();
        }
    }

    @Override
    public void deleteSpeAnaDataListener(@NonNull OnSpeAnaDataListener listener) {
        Timber.i("deleteSpeAnaDataListener()");
        checkNotNull(listener);

        synchronized (this) {
            mSpeAnaDataListeners.remove(listener);
        }

        if (mSpeAnaDataListeners.isEmpty()) {
            stopSpectrumAnalyze();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCompletePlayMusic(PlayerStatus.PlayEndStatus status, boolean idling, int playTime, int time, int finished, int next) {
        Timber.i("onCompletePlayMusic() next = " + next);

        mHandler.post(() -> {
            try {
                mIsPreventNoticePlaybackMode = true;
                setStatus(IDLE);
                if (mCursor.moveToNext()) {
                    prepareAudio(AppMusicPlaylistCursor.Action.COMPLETE);
                    if(mRepeatMode == SmartPhoneRepeatMode.OFF && mCursor.isFirst()) {
                        mIsPreventNoticePlaybackMode = false;
                        updatePlayStatus(Status.PAUSED);
                    } else {
                        play(AppMusicPlaylistCursor.Action.NEXT);
                    }
                } else {
                    mIsPreventNoticePlaybackMode = false;
                    mCursor.moveToFirst();
                    prepareAudio(AppMusicPlaylistCursor.Action.COMPLETE);
                }
                saveState();
            } catch (PMGPlayerException e) {
                Timber.e(e);
                return;
            } finally {
                mIsPreventNoticePlaybackMode = false;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartPlayMusic(int id) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPauseMusic() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCompleteSmartShuffle(boolean result, ArrayList<Integer> outputMusicIdList) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCompleteMusicAnalyze(int id, ByteBuffer data, boolean result) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelMusicAnalyze(int id) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onCompletePostSpeAna(float[] bandData, int numData) {
        for (Map.Entry<OnSpeAnaDataListener, Object> listener : mSpeAnaDataListeners.entrySet()) {
            if (listener.getKey() != null) {
                listener.getKey().onSpeAnaData(bandData);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(PlayerStatus.ErrorStatus status, int id) {
        Timber.e("onError() status = " + status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceiveMidiMessage(byte[] bytes, int i) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        try {
            AppStatus appStatus = mStatusHolder.getAppStatus();
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    Timber.i("onAudioFocusChange() focusChange = AUDIOFOCUS_LOSS");
                    mIsReturnToPlayMode = mStatus == PLAYING;
                    setSmartPhoneEqUseStatus(false);
                    pause();
                    mAudioFocus = AudioFocus.LOSS;
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    Timber.i("onAudioFocusChange() focusChange = AUDIOFOCUS_GAIN");
                    if (mStatusHolder.getSessionStatus() != SessionStatus.STARTED) {
                        abandonAudioFocusRequest();
                    } else {
                        if (mIsReturnToPlayMode && appStatus.appMusicAudioMode == AudioMode.MEDIA) {
                            play(AppMusicPlaylistCursor.Action.NEXT);
                            mIsReturnToPlayMode = false;
                        }
                        mAudioFocus = AudioFocus.GAIN;

                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Timber.i("onAudioFocusChange() focusChange = AUDIOFOCUS_LOSS_TRANSIENT");
                    mIsReturnToPlayMode = mStatus == PLAYING;
                    pause();
                    mAudioFocus = AudioFocus.LOSS;
                    break;
                default:
                    break;
            }
        } catch (PMGPlayerException e) {
            Timber.e(e);
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectTrack(int trackNo) {
        if (mCursor == null) {
            return;
        }

        checkArgument(trackNo >= 0);
        checkArgument(trackNo < mCursor.getCount());

        if (!mCursor.selectTrack(trackNo)) {
            return;
        }

        try {
            mIsPreventNoticePlaybackMode = true;
            pause();
            prepareAudio(AppMusicPlaylistCursor.Action.SELECT);
            play(AppMusicPlaylistCursor.Action.SELECT);
        } catch (PMGPlayerException e) {
            Timber.e(e);
            return;
        } finally {
            mIsPreventNoticePlaybackMode = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fastForwardForPlayer(int time) {
        try {
            if (mStatus == IDLE
                    || mStatus == Status.PLAYING
                    || mStatus == Status.PAUSED) {

                fastForward(time, false);
            }
        } catch (PMGPlayerException e) {
            Timber.e(e);
        }
    }

    /**
     * 早送り
     * <p>
     * 指定された分早送りを実施する
     * 早送りしても問題ない状況かどうかは呼び出し元でチェックする
     * 早送り後の再生時間が曲の再生時間を超えている場合は丸める
     * スマートフォンステータスの更新は呼び出し元で実施する
     */
    private void fastForward(int time, boolean isResetStatus) throws PMGPlayerException {
        PlayRange playRange = mPlayer.getCurrentMusicPlayRange();
        if (playRange == null) {
            Timber.w("fastForward() PlayRange is null.");
            return;
        }

        int pos = mPlayer.getCurrentPosition();
        int duration = (int) playRange.getOutPoint();
        int newPosition = pos + time;
        newPosition -= (newPosition % 1000);
        newPosition = Math.min(newPosition, duration);

        if (pos != newPosition) {
            checkResultCodeForException(mPlayer.seek(newPosition));

            // 前のスケジュールをキャンセルして即時に実行
            mTrackPositionMonitor.restart();

            // 早送り状態からステータスを戻すためのタスクを実行
            if(isResetStatus) {
                mHandler.removeCallbacks(mResetPlaybackModeTask);
                mHandler.postDelayed(mResetPlaybackModeTask, 1000);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rewindForPlayer(int time) {
        try {
            if (mStatus == IDLE
                    || mStatus == Status.PLAYING
                    || mStatus == Status.PAUSED) {

                rewind(time, false);
            }
        } catch (PMGPlayerException e) {
            Timber.e(e);
        }
    }

    /**
     * 巻き戻し
     * <p>
     * 指定された分巻き戻しを実施する
     * 巻き戻ししても問題ない状況かどうかは呼び出し元でチェックする
     * 巻き戻し後の再生時間が0を下回る場合は丸める
     * スマートフォンステータスの更新は呼び出し元で実施する
     */
    public void rewind(int time, boolean isResetStatus) throws PMGPlayerException {
        int pos = mPlayer.getCurrentPosition();

        // 秒以下の単位を0に揃えるのは次のrewindが来る約500msの間に秒が進んで表示が1秒前進するのを防ぐため
        int newPosition = pos - time;
        newPosition -= (newPosition % 1000);
        newPosition = Math.max(newPosition, 0);

        if (pos != newPosition) {
            checkResultCodeForException(mPlayer.seek(newPosition));

            // 前のスケジュールをキャンセルして即時に実行
            mTrackPositionMonitor.restart();

            // 巻き戻し状態からステータスを戻すためのタスクを実行
            if(isResetStatus) {
                mHandler.removeCallbacks(mResetPlaybackModeTask);
                mHandler.postDelayed(mResetPlaybackModeTask, 1000);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abandonAudioFocus() {
        try {
            pause();

            mAudioFocus = AudioFocus.LOSS;
            //3rdPartyAppの自動再生
            dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY);
            SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
            AndroidMusicMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo;
            postSmartPhoneStatus(smartPhoneStatus);
			postAudioInfo(info);
            mCarDeviceConnection.sendPacket(mPacketBuilder.createSmartPhoneAudioPlaybackTimeNotification(
                    MediaSourceType.APP_MUSIC, info.durationInSec, info.positionInSec));
        } catch (PMGPlayerException e) {
            Timber.e(e);
        }
    }

    @Override
    public void sendPlaybackTime(int durationInSec, int positionInSec){
        mCarDeviceConnection.sendPacket(mPacketBuilder.createSmartPhoneAudioPlaybackTimeNotification(
                MediaSourceType.APP_MUSIC, durationInSec, positionInSec));
    }

    @Override
    public void sendMusicInfo(){
        if(mStatusHolder.getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC) {
            AndroidMusicMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo;
            CarDeviceSpec carDeviceSpec = mStatusHolder.getCarDeviceSpec();
            Stream.of(SmartPhoneMediaInfoType.values())
                    .forEach(type -> mCarDeviceConnection.sendPacket(mPacketBuilder.createSmartPhoneAudioInfoNotification(info, type, carDeviceSpec)));
        }
    }

    @SuppressWarnings("deprecation")
    public boolean abandonAudioFocusRequest(){
        boolean result = false;
        mAudioFocus = AudioFocus.LOSS;
        if (mAudioManager != null) {
            int request = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
            if(Build.VERSION.SDK_INT>=26) {
                if(mFocusRequest!=null) {
                    request = mAudioManager.abandonAudioFocusRequest(mFocusRequest);
                }
            }else{
                request = mAudioManager.abandonAudioFocus(this);
            }
            if (request == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Timber.d("abandonAudioFocusRequest() success");
                result = true;
            }
        }
        return result;
    }
    /**
     * アプリケーション状態変更イベントハンドラ.
     * <p>
     * READ_EXTERNAL_STORAGEが無い場合、アプリがフォアグラウンドになる度に権限を許可された
     * と期待して復元を試みる。
     * 権限の許可ダイアログから許可を行う場合に有効であるが、アプリの設定から変更した
     * 場合には検知出来ない。ポーリングするしか手段が無いのでどうしようもない。
     *
     * @param event アプリケーション状態変更イベント
     */
    @Subscribe
    public void onAppStateChangedEvent(AppStateChangeEvent event) {
        if (isActive()) {
            if (event.appState != AppState.RESUMED) {
                return;

            }

            mHandler.post(() -> {
                if (mIsRequirePermission) {
                    try {
                        prepare(mPlayParams);
                    } catch (PMGPlayerException e) {
                        Timber.e(e);
                    }
                }
            });
        }
    }

    /**
     * セッション開始イベントハンドラ.
     *
     * @param event セッション開始イベント
     */
    @Subscribe
    public void onCrpSessionStartedEvent(CrpSessionStartedEvent event) {
        if (isActive()) {
            mHandler.post(() -> {
                if (mCursor != null && mCursor.getCount() >= 1) {
                    updateAppMusicInfo();
                }
            });
        }
    }

    /**
     * セッション停止イベントハンドラ.
     *
     * @param event セッション停止イベント
     */
    @Subscribe
    public void onCrpSessionStoppedEvent(CrpSessionStoppedEvent event) {
        if (isActive()) {
            mHandler.post(() -> {
                try {
                    pause();
                    abandonAudioFocusRequest();
                } catch (PMGPlayerException e) {
                    Timber.e(e);
                    return;
                }
            });
        }
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
    /**
     * SmartPhoneメディアコマンドイベントハンドラ
     *
     * @param event SmartPhoneメディアコマンドイベント
     */
    @Subscribe
    public void onCrpSmartPhoneMediaCommandEvent(CrpSmartPhoneMediaCommandEvent event) {
        if (isActive()) {
            mHandler.post(() -> {
                if (!isActive()) {
                    return;
                }
                if(mStatusHolder.getAppStatus().appMusicAudioMode == AudioMode.ALEXA&&
                        (event.command== SmartPhoneMediaCommand.FAST_FORWARD||event.command== SmartPhoneMediaCommand.REWIND||event.command== SmartPhoneMediaCommand.RANDOM
                                ||event.command== SmartPhoneMediaCommand.REPEAT))return;
                AppStatus appStatus = mStatusHolder.getAppStatus();
                try {
                    switch (event.command) {
                        case PLAY_PAUSE:
                            if (!isAndroidMusicInterrupted()) {
                                if(appStatus.appMusicAudioMode==AudioMode.ALEXA){
                                    if (isPlaying()) {
                                        AlexaAudioManager.getInstance().doPause(null);
                                    } else {
                                        AlexaAudioManager.getInstance().doPlay(null);
                                    }
                                }else {
                                    if (mAudioFocus == AudioFocus.GAIN) {
                                        togglePlay();
                                    } else {
                                        dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                                    }
                                }
                            }
                            break;
                        case PLAY_RESUME:
                            if (!isAndroidMusicInterrupted()) {
                                if (!appStatus.isLaunchedThirdPartyAudioApp) {
                                    if(appStatus.appMusicAudioMode==AudioMode.ALEXA){
                                        if (!isPlaying()&&!appStatus.isShowAlexaDialog) {
                                            AlexaAudioManager.getInstance().onForegroundContentsChannel();
                                        }
                                    } else {
                                        // 音声認識とYouTubeLink画面表示中はAppMusicソースを自動再生しない
                                        if(!appStatus.isShowAlexaDialog && !appStatus.isShowSpeechRecognizerDialog
                                            && !appStatus.isShowYouTubeLinkWebView) {
                                            if (mAudioFocus == AudioFocus.GAIN) {
                                                mHandler.removeCallbacks(mPlayResumeTask);
                                                mHandler.postDelayed(mPlayResumeTask, 1000);
                                            } else {
                                                dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY);
                                            }
                                        }
                                    }
                                } else {
                                    //3rdPartyAppの自動再生
                                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY);
                                }
                            }
                            break;
                        case TRACK_UP:
                            if(appStatus.appMusicAudioMode==AudioMode.ALEXA) {
                                AlexaAudioManager.getInstance().doNext(null);
                            }else {
                                if (mAudioFocus == AudioFocus.GAIN) {
                                    skipNextTrack();
                                } else {
                                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
                                }
                            }
                            break;
                        case TRACK_DOWN:
                            if(appStatus.appMusicAudioMode==AudioMode.ALEXA) {
                                AlexaAudioManager.getInstance().doPrev(null);
                            }else {
                                if (mAudioFocus == AudioFocus.GAIN) {
                                    skipPreviousTrack();
                                } else {
                                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                                }
                            }
                            break;
                        case FAST_FORWARD:
                            if (mAudioFocus == AudioFocus.GAIN) {
                                fastForwardForMediaCommand();
                            } else {
                                dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);
                            }
                            break;
                        case REWIND:
                            if (mAudioFocus == AudioFocus.GAIN) {
                                rewindForMediaCommand();
                            } else {
                                dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_REWIND);
                            }
                            break;
                        case RANDOM:
                            if (mAudioFocus == AudioFocus.GAIN) {
                                toggleShuffleMode();
                            }
                            break;
                        case REPEAT:
                            if (mAudioFocus == AudioFocus.GAIN) {
                                toggleRepeatMode();
                            }
                        default:
                            break;
                    }
                } catch (PMGPlayerException e) {
                    Timber.e(e);
                }
            });
        }
    }

    private void restoreState() {
        mPendingPlayPosition = mPreference.getAppMusicAudioPlayPosition();
        QueryParams queryParams = mPreference.getAppMusicQueryParams();
        //JSONの復元に失敗した場合のセーフ処理
        if(queryParams.uri==null){
            queryParams = createAllSongs();
        }
        long audioId = mPreference.getAppMusicAudioId();
        mPlayParams = createPlayParams(queryParams, audioId, mShuffleMode);
    }

    private void saveState() {
        int playPosition = -1;
        if (mStatus == PAUSED) {
            playPosition = mPlayer.getCurrentPosition();
        }

        long audioId = -1;
        if (mCursor != null) {
            audioId = getId(mCursor);
        }

        if (mPlayParams != null) {
            if(mPlayParams.queryParams.uri!=null){
                mPreference.setAppMusicQueryParams(mPlayParams.queryParams);
            }
        }

        mPreference.setAppMusicAudioPlayPosition(playPosition);
        mPreference.setAppMusicAudioId(audioId);
    }

    private void openPMGPlayer() throws PMGPlayerException, IOException {
        File file = getDataDir();
        if (!file.exists() && !file.mkdir()) {
            throw new IOException("mkdir(" + file + ") failed.");
        }

        String musicDbPath = mContext.getApplicationInfo().dataDir + "/databases/" + MUSIC_DB_NAME;
        String libPath = mContext.getApplicationInfo().dataDir + "/lib";
        checkResultCodeForException(mPlayer.open(SAMPLING, CHANNEL_NUM, musicDbPath, MAX_RECORDS, libPath));
        mPlayer.addListener(this);

        mPlayer.setFilterConfig(
                FilterStatus.FilterFunctionId.APPLAUSE_EFFECT.getValue() |
                        FilterStatus.FilterFunctionId.SOUND_FIELD_CONTROLLER.getValue()
        );

        String applauseDir = mContext.getApplicationInfo().dataDir + "/" + APPLAUSE_FOLDER_NAME;
        AppMusicUtil.prepareApplause(mContext, applauseDir);
        checkResultCodeForException(mPlayer.applauseEffectInitialize(applauseDir));
    }

    private void closePMGPlayer() {
        if (mStatus == CLOSED) {
            return;
        }

        mPlayer.deleteListener(this);
        mPlayer.close();
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }

        setStatus(CLOSED);
    }

    /**
     * プレイヤー再生準備
     * <p>
     * ・再生する楽曲情報の取得
     * ・再生する楽曲のPMGプレイヤーへの登録
     * ・再生する楽曲プレイリストの作成及びPMGプレイヤーへの登録
     * を実施する。
     */
    private boolean prepare(AppMusicContract.PlayParams params) throws PMGPlayerException {
        if (!prepareCursor(params)) {
            clearAppMusicInfo();
            if(mStatusHolder.getAppStatus().isFormatRead) {
                mStatusHolder.getAppStatus().isFormatRead = false;
            }
            return false;
        }
        mPlayParams = params;

        prepareAudio(AppMusicPlaylistCursor.Action.PREPARE);

        return true;
    }

    private boolean prepareCursor(AppMusicContract.PlayParams params) {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }

        if (!checkSelfPermission()) {
            mIsRequirePermission = true;
            return false;
        }

        mIsRequirePermission = false;
        Cursor originalCursor = createCursor(params.queryParams);
        if (originalCursor == null) {
            return false;
        } else if (originalCursor.getCount() == 0) {
            originalCursor.close();
            return false;
        }

        boolean isFound = false;
        if (params.audioId != -1) {
            while (originalCursor.moveToNext()) {
                if (getId(originalCursor) == params.audioId) {
                    isFound = true;
                    break;
                }
            }

            if (!isFound) {
                originalCursor.moveToFirst();
            }
        } else {
            originalCursor.moveToFirst();
        }

        // 復元時の呼び出しかつ、対象が存在することを確認
        if (mPendingPlayPosition != -1 && !isFound) {
            mPendingPlayPosition = -1;
        }

        if (params.shuffleMode != null) {
            setShuffleMode(params.shuffleMode);
        }

        mCursor = createAppMusicPlaylistCursor(originalCursor, mRepeatMode, mShuffleMode);

        return true;
    }

    private void prepareAudio(AppMusicPlaylistCursor.Action action) throws PMGPlayerException {
        mCursor.prepareSearch(action);
        while (true) {
            if (addMusicToPmgPlayer()) {
                break;
            } else {
                if(mPendingPlayPosition != -1){
                    mPendingPlayPosition = -1;
                }

                if (!mCursor.reSearch()) {
                    checkResultCodeForError(PMGPlayer.ResultCode.FAILED);
                }
            }
        }

        setStatus(PAUSED);
        if (mPendingPlayPosition != -1) {
            checkResultCodeForException(mPlayer.seek(mPendingPlayPosition));
            mTrackPositionMonitor.stop();
            mTrackPositionMonitor.start();
            mPendingPlayPosition = -1;
            updateAppMusicInfo();
        }
    }

    private boolean addMusicToPmgPlayer() throws PMGPlayerException {
        ArrayList<Integer> deletedIds = new ArrayList<>();
        ArrayList<Integer> musicIds = new ArrayList<>();

        musicIds.add(mPlayer.registerMusicData(getData(mCursor), deletedIds));
        checkResultCodeForException(mPlayer.clearPlaylist());

        boolean result = checkResultCode(mPlayer.addMusicToPlaylist(musicIds));

        if(result) {
            updateAppMusicInfo();
        }
        return result;
    }

    private void clearAppMusicInfo() {
        AndroidMusicMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo;
        AndroidMusicMediaInfo old = new AndroidMusicMediaInfo(info);
        info.songTitle = null;
        info.artistName = null;
        info.albumTitle = null;
        info.trackNumber = 0;
        info.mediaId = 0;
        info.genre = null;
        info.artworkImageLocation = null;
        mMusicDuration = 0;
        info.durationInSec = 0;
        info.positionInSec = 0;

        // 何度も呼ばれる可能性があるので、変化をチェックする
        if (!old.equals(info)) {
            postAudioInfo(info);
        }
    }

    private void updateAppMusicInfo() {
        PlayRange playRange = mPlayer.getCurrentMusicPlayRange();
        if (playRange == null) {
            Timber.w("updateAppMusicInfo() PlayRange is null.");
            return;
        }

        AndroidMusicMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo;
        info.songTitle = getTitle(mCursor);
        info.artistName = getArtist(mCursor);
        info.albumTitle = getAlbum(mCursor);
        info.trackNumber = getTrack(mCursor);
        info.mediaId = getId(mCursor);
        info.genre = getGenre(getId(mCursor));
        info.artworkImageLocation = getArtworkUri(mCursor);
        mMusicDuration = (int) (playRange.getOutPoint() - playRange.getInPoint());
        info.durationInSec = mMusicDuration / 1000;
        info.positionInSec = mPlayer.getCurrentPosition() / 1000;
        Timber.d("updateAppMusicInfo() " + info.toString());

        postAudioInfo(info);
    }

    private void play(AppMusicPlaylistCursor.Action action) throws PMGPlayerException {

        if(mStatusHolder.getAppStatus().isLaunchedThirdPartyAudioApp) {
            mStatusHolder.getAppStatus().isLaunchedThirdPartyAudioApp = false;
            setSmartPhoneEqUseStatus(true);
            //dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE);
            SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
            AndroidMusicMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo;
            postSmartPhoneStatus(smartPhoneStatus);
            postAudioInfo(info);
            mCarDeviceConnection.sendPacket(mPacketBuilder.createSmartPhoneAudioPlaybackTimeNotification(
                    MediaSourceType.APP_MUSIC, info.durationInSec, info.positionInSec));
        }

        if (mStatus.toggle() != PLAYING) {
            return;
        }

        if (!requestAudioFocus()) {
            return;
        }

        mIsReturnToPlayMode = false;
        mIsPreventNoticePlaybackMode = false;

        boolean isTryToPlay = true;
        int currentPlayIndex = mCursor.getPosition();
        while (true) {
            if(isTryToPlay) {
                if (checkResultCode(mPlayer.play())) {
                    setStatus(PLAYING);
                    break;
                }
            }

            setStatus(PAUSED);
            if(action == AppMusicPlaylistCursor.Action.PREVIOUS){
                if (!mCursor.skipPrevious()) {
                    checkResultCodeForError(PMGPlayer.ResultCode.FAILED);
                }
            } else {
                if (!mCursor.skipNext()) {
                    checkResultCodeForError(PMGPlayer.ResultCode.FAILED);
                }
            }

            isTryToPlay = addMusicToPmgPlayer();

            if(currentPlayIndex == mCursor.getPosition()){
                break;
            } else if(mRepeatMode == SmartPhoneRepeatMode.OFF && mCursor.isFirst()){
                break;
            }
        }
    }

    private void pause() throws PMGPlayerException {
        if (mStatus == PLAYING) {
            checkResultCodeForException(mPlayer.pause());
            setStatus(PAUSED);

            // 再生情報保存
            saveState();
        }
    }

    private void setShuffleMode(ShuffleMode shuffleMode) {
        if (mShuffleMode != shuffleMode) {
            Timber.d("ShuffleMode " + mShuffleMode + " -> " + shuffleMode);
            mShuffleMode = shuffleMode;
            mPreference.setAppMusicShuffleMode(shuffleMode);

            if (mCursor != null) {
                mCursor.setShuffleMode(shuffleMode);
            }

            SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
            smartPhoneStatus.shuffleMode = mShuffleMode;
            postSmartPhoneStatus(smartPhoneStatus);

            // シャッフル状態変更によるNowPlayingList更新更新
            mEventBus.post(new NowPlayingListRepositoryImpl.NowPlayingListUpdateEvent(mCursor));
        }
    }

    private void setRepeatMode(SmartPhoneRepeatMode repeatMode) {
        if (mRepeatMode != repeatMode) {
            Timber.d("RepeatMode " + mRepeatMode + " -> " + repeatMode);
            mRepeatMode = repeatMode;
            mPreference.setAppMusicRepeatMode(repeatMode);

            if (mCursor != null) {
                mCursor.setRepeatMode(repeatMode);
            }

            SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
            smartPhoneStatus.repeatMode = mRepeatMode;
            postSmartPhoneStatus(smartPhoneStatus);
        }
    }

    private void setSmartPhoneEqUseStatus(boolean eqUseStatus) {
        Timber.d("setSmartPhoneEqUseStatus:"+eqUseStatus);
        if(mEqUseStatus != eqUseStatus) {
            mEqUseStatus = eqUseStatus;
            SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
            smartPhoneStatus.smartPhoneEqUseStatus = mEqUseStatus;
            postSmartPhoneStatus(smartPhoneStatus);
        }
    }
    private void fastForwardForMediaCommand() throws PMGPlayerException {
        if (mStatus == IDLE
                || mStatus == Status.PLAYING
                || mStatus == Status.PAUSED) {

            SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
            if (mSeekCalculator.isFirstFastForward(getSystemCurrentTimeMillis()) ||
                    smartPhoneStatus.playbackMode != PlaybackMode.FAST_FORWARD) {
                smartPhoneStatus.playbackMode = PlaybackMode.FAST_FORWARD;
                postSmartPhoneStatus(smartPhoneStatus);
            }
            int seekStep = mSeekCalculator.fastForward();
            fastForward(seekStep, true);
        }
    }

    private void rewindForMediaCommand() throws PMGPlayerException {
        if (mStatus == IDLE
                || mStatus == Status.PLAYING
                || mStatus == Status.PAUSED) {

            SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
            if (mSeekCalculator.isFirstRewind(getSystemCurrentTimeMillis()) ||
                    smartPhoneStatus.playbackMode != PlaybackMode.REWIND) {
                smartPhoneStatus.playbackMode = PlaybackMode.REWIND;
                postSmartPhoneStatus(smartPhoneStatus);
            }
            int seekStep = mSeekCalculator.rewind();
            rewind(seekStep, true);
        }
    }

    private Runnable mResetPlaybackModeTask = () -> updatePlayStatus(mStatus);

    private void setStatus(Status status) {
        if (mStatus != status) {
            mStatus = status;
            updatePlayStatus(mStatus);
        }
    }

    private void updatePlayStatus(Status status) {
        SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
        PlaybackMode oldMode = smartPhoneStatus.playbackMode;
        PlaybackMode newMode = oldMode;
        switch (status) {
            case CLOSED:
            case PREPARING:
            case IDLE:
                newMode = PlaybackMode.STOP;
                mTrackPositionMonitor.stop();
                break;
            case PLAYING:
                newMode = PlaybackMode.PLAY;
                mTrackPositionMonitor.start();
                break;
            case PAUSED:
                newMode = PlaybackMode.PAUSE;
                mTrackPositionMonitor.stop();
                break;
            case INVALID:
            case ERROR:
                newMode = PlaybackMode.ERROR;
                mTrackPositionMonitor.stop();
                break;
        }

        if ((!mIsPreventNoticePlaybackMode && oldMode != newMode) ||
                (oldMode == PlaybackMode.ERROR || newMode == PlaybackMode.ERROR)) {
            smartPhoneStatus.playbackMode = newMode;
            postSmartPhoneStatus(smartPhoneStatus);
        }
    }

    private boolean checkSelfPermission() {
        int result = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED) {
            Timber.e("checkSelfPermission() result = " + result);
            return false;
        }

        return true;
    }
    @SuppressWarnings("deprecation")
    private boolean requestAudioFocus() {
        int result;
        if(Build.VERSION.SDK_INT>=26){
            mPlaybackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mPlaybackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, mHandler)
                    .build();
            result = mAudioManager.requestAudioFocus(mFocusRequest);
        }else{
            result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Timber.e("requestAudioFocus() result = " + result);
            return false;
        }

        mAudioFocus = AudioFocus.GAIN;
        return true;
    }

    @NonNull
    private String getGenre(long audioId) {
        QueryParams queryParams = createGenresForAudioId(audioId);
        Cursor cursor = createCursor(queryParams);
        if (cursor == null) {
            return "";
        }

        try {
            if (cursor.getCount() == 0) {
                return "";
            }

            cursor.moveToFirst();
            String name = getName(cursor);
            return (name == null) ? "" : name;
        } finally {
            cursor.close();
        }
    }

    private boolean isAndroidMusicInterrupted() {
        return mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }

    private void startSpectrumAnalyze() {
        if (mIsStartSpeAna) {
            return;
        }

        PMGPlayer.ResultCode resultCode = mPlayer.startPostSpectrumAnalyze(
                SPE_ANA_SAMPLE_COUNT, SPE_ANA_NUM_BANDS, SPE_ANA_OVERLAP_MODE, SPE_ANA_INTERVAL);
        if (resultCode != SUCCESS) {
            Timber.w("startPostSpectrumAnalyze() resultCode = " + resultCode);
            return;
        }

        mIsStartSpeAna = true;
    }

    private void stopSpectrumAnalyze() {
        if (mIsStartSpeAna) {
            mPlayer.stopPostSpectrumAnalyze();
            mIsStartSpeAna = false;
        }
    }

    private void postAudioInfo(AndroidMusicMediaInfo info) {
        if(mStatusHolder.getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC) {
            mEventBus.post(new CrpStatusUpdateEvent());
            CarDeviceSpec carDeviceSpec = mStatusHolder.getCarDeviceSpec();
            Stream.of(SmartPhoneMediaInfoType.values())
                    .forEach(type -> mCarDeviceConnection.sendPacket(mPacketBuilder.createSmartPhoneAudioInfoNotification(info, type, carDeviceSpec)));
        }
    }

    private void postSmartPhoneStatus(SmartPhoneStatus status) {
        mEventBus.post(new CrpStatusUpdateEvent());
        ProtocolVersion version = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();
        mCarDeviceConnection.sendPacket(mPacketBuilder.createSmartPhoneStatusNotification(version, status));
    }

    @VisibleForTesting
    long getSystemCurrentTimeMillis() {
        return SystemClock.elapsedRealtime();
    }

    // MARK - Now Playing List

    /**
     * {@inheritDoc}
     */
    @Override
    public AppMusicPlaylistCursor getPlaylistCursor() {
        return mCursor;
    }

    // MARK - Sound FX

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] convertBandValue(@Size(31) float[] bands) {
        if (mPlayer != null /*&& mStatus != CLOSED*/) {
            try {
                AudioSetting audioSetting = mStatusHolder.getAudioSetting();
                if (audioSetting.equalizerSetting.maximumStep == 0 ||
                        audioSetting.equalizerSetting.minimumStep == 0) {
                    return null;
                }

                // 31->13band変換
                float[] convertBands = new float[13];
                checkResultCodeForException(mPlayer.convertBand(PMGPlayer.MultiBandEQConvertBandMode.CONVERT_EQ_MODE_31_13, bands, convertBands));

                // 車載機へ通知するためにSTEP値を修正
                return convertStepValueMleToCarDevice(
                        audioSetting.equalizerSetting.maximumStep,
                        audioSetting.equalizerSetting.minimumStep,
                        convertBands);
            } catch (PMGPlayerException e) {
                Timber.e(e);
            }
        }
        return null;
    }

    /**
     * Equalizer設定変更イベントハンドラ.
     *
     * @param ev Equalizer設定変更イベント
     */
    @Subscribe
    public void onEqualizerSettingChangeEvent(EqualizerSettingChangeEvent ev) {
        if (mPlayer != null && mStatus != CLOSED) {
            try {
                updateEqualizer();
            } catch (PMGPlayerException e) {
                Timber.e(e);
            }
        }
    }

    /**
     * LiveSimulation設定変更イベントハンドラ.
     *
     * @param ev LiveSimulation設定変更イベント
     */
    @Subscribe
    public void onLiveSimulationSettingChangeEvent(LiveSimulationSettingChangeEvent ev) {
        if (mPlayer != null && mStatus != CLOSED) {
            try {
                mIsPreventNoticePlaybackMode = true;
                updateLiveSimulation();
                updateEqualizer();
            } catch (PMGPlayerException e) {
                Timber.e(e);
            } finally {
                mIsPreventNoticePlaybackMode = false;
            }
        }
    }

    /**
     * Super轟設定変更イベントハンドラ.
     *
     * @param ev Super轟設定変更イベント
     */
    @Subscribe
    public void onSuperTodorokiSettingChangeEvent(SuperTodorokiSettingChangeEvent ev) {
        if (mPlayer != null && mStatus != CLOSED) {
            try {
                updateEqualizer();
            } catch (PMGPlayerException e) {
                Timber.e(e);
            }
        }
    }

    /**
     * Custom Equalizer設定変更イベントハンドラ.
     *
     * @param ev Custom Equalizer設定変更イベント
     */
    @Subscribe
    public void onCustomEqualizerSettingChangeEvent(CustomEqualizerSettingChangeEvent ev) {
        if (mPlayer != null /*&& mStatus != CLOSED*/) {
            try {
                AudioSetting audioSetting = mStatusHolder.getAudioSetting();
                SoundFxSetting soundFxSetting = mStatusHolder.getSoundFxSetting();
                if (audioSetting.customEqualizerSetting.maximumStep == 0 ||
                        audioSetting.customEqualizerSetting.minimumStep == 0) {
                    return;
                }

                float[] convertBands = new float[31];
                checkResultCodeForException(mPlayer.convertBand(PMGPlayer.MultiBandEQConvertBandMode.CONVERT_EQ_MODE_13_31, audioSetting.customEqualizerSetting.getBandArray(), convertBands));

                // MLEに反映するためにSTEP値を修正
                float[] mleBands = convertStepValueCarDeviceToMle(
                        audioSetting.customEqualizerSetting.maximumStep,
                        audioSetting.customEqualizerSetting.minimumStep,
                        convertBands);

                if (audioSetting.customEqualizerSetting.customEqType == CustomEqType.CUSTOM1) {
                    soundFxSetting.customBandSettingA.bands = mleBands;
                    mPreference.setCustomBandSettingA(soundFxSetting.customBandSettingA);
                } else {
                    soundFxSetting.customBandSettingB.bands = mleBands;
                    mPreference.setCustomBandSettingB(soundFxSetting.customBandSettingB);
                }

                updateEqualizer();
            } catch (PMGPlayerException e) {
                Timber.e(e);
            }
        }
    }

    /**
     * MLEのイコライザーを更新する
     * <p>
     * LiveSimulation設定がOFFの場合は指定されたイコライザーへ更新
     * LiveSimulation設定がOFF以外の場合はFLATへ更新
     * <p>
     * Super轟設定がOFFの場合は指定されたイコライザーへ更新
     * Super轟設定がOFF以外の場合はFLATへ更新
     */
    private void updateEqualizer() throws PMGPlayerException {
        SoundFxSetting soundFxSetting = mStatusHolder.getSoundFxSetting();
        ArrayList<Float> bandList;
        LiveSimulationSetting liveSimulationSetting = soundFxSetting.liveSimulationSetting;
        SuperTodorokiSetting superTodorokiSetting = soundFxSetting.superTodorokiSetting;

        if (liveSimulationSetting.soundFieldControlSettingType != SoundFieldControlSettingType.OFF ||
                superTodorokiSetting != SuperTodorokiSetting.OFF) {
            bandList = soundFxSetting.getEqualizerBandList(SoundFxSettingEqualizerType.FLAT);
        } else {
            bandList = soundFxSetting.getEqualizerBandList(soundFxSetting.soundFxSettingEqualizerType);
        }

        if (!mCurrentBandList.equals(bandList)) {
            checkResultCodeForException(mPlayer.setMultiBandEqualizer(MultiBandEqualizerStatus.EqualizerType.TYPE_31_BAND, bandList));
            mCurrentBandList = bandList;
        }
    }

    /**
     * LiveSimulation機能を更新する
     */
    private void updateLiveSimulation() throws PMGPlayerException {
        LiveSimulationSetting setting = mStatusHolder.getSoundFxSetting().liveSimulationSetting;
        if(mCurrentSoundFieldControlSettingType != setting.soundFieldControlSettingType) {
            checkResultCodeForException(mPlayer.setFilterSoundFieldMode(setting.soundFieldControlSettingType.mode));
            mCurrentSoundFieldControlSettingType = setting.soundFieldControlSettingType;
        }

        SoundEffectSettingType type = setting.soundFieldControlSettingType.getEnableSoundEffectSettingType(setting.soundEffectSettingType.type);
        if(mCurrentSoundEffectSettingType != type) {
            checkResultCodeForException(mPlayer.applauseEffectSetMode(type.mode));
            mCurrentSoundEffectSettingType = type;
        }
    }

    // MARK - PMGPlayerException

    private static class PMGPlayerException extends Exception {
        private static final long serialVersionUID = 117511424305435294L;

        PMGPlayerException(PMGPlayer.ResultCode resultCode) {
            super("ResultCode = " + resultCode);
        }
    }

    private void checkResultCodeForException(PMGPlayer.ResultCode resultCode) throws PMGPlayerException {
        /*
         * 結果がNGだった場合の処理、エラー処理はここに実装する。
         */
    }

    private void checkResultCodeForError(PMGPlayer.ResultCode resultCode) throws PMGPlayerException {
        if(resultCode != PMGPlayer.ResultCode.SUCCESS){
            clearAppMusicInfo();
            setStatus(ERROR);
            throw new PMGPlayerException(resultCode);
        }
    }

    private boolean checkResultCode(PMGPlayer.ResultCode resultCode) {
        return resultCode == PMGPlayer.ResultCode.SUCCESS;
    }

    private void dispatchMediaKeyEvent(int keyCode){
        mAudioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        mAudioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
    }

    /**
     * Cursor生成.
     * <p>
     * UnitTest用
     *
     * @param params クエリパラメータ
     * @return Cursor
     */
    @VisibleForTesting
    @Nullable
    Cursor createCursor(QueryParams params) {
        ContentResolver resolver = mContext.getContentResolver();
        return resolver.query(params.uri, params.projection, params.selection, params.selectionArgs, params.sortOrder);
    }

    /**
     * AppMusicPlaylistCursor生成.
     * <p>
     * UnitTest用
     *
     * @param cursor      Cursor
     * @param repeatMode  SmartPhoneRepeatMode
     * @param shuffleMode ShuffleMode
     * @return AppMusicPlaylistCursor
     */
    @VisibleForTesting
    AppMusicPlaylistCursor createAppMusicPlaylistCursor(Cursor cursor, SmartPhoneRepeatMode repeatMode, ShuffleMode shuffleMode) {
        return new AppMusicPlaylistCursor(cursor, repeatMode, shuffleMode);
    }

    /**
     * dataファイル取得
     * <p>
     * UnitTest用
     *
     * @return {@link File}
     */
    @VisibleForTesting
    File getDataDir() {
        return new File(mContext.getApplicationInfo().dataDir + "/databases");
    }

    /**
     * 再生位置監視モニタ生成
     * <p>
     * UnitTest用
     *
     * @return {@link TrackPositionMonitor}
     */
    @VisibleForTesting
    TrackPositionMonitor createTrackPositionMonitor() {
        return new TrackPositionMonitor();
    }

    class TrackPositionMonitor implements Runnable {

        void start() {
            mHandler.postDelayed(this, TRACK_POSITION_MONITOR_INTERVAL);
        }

        void stop() {
            mHandler.removeCallbacks(this);
        }

        void restart(){
            mHandler.removeCallbacks(this);
            mHandler.post(this);
        }

        @Override
        public void run() {
            int position = mPlayer.getCurrentPosition();
            AndroidMusicMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo;
            int positionInSec = position / 1000;
            if (info.positionInSec != positionInSec) {
                info.positionInSec = positionInSec;

                mEventBus.post(new CrpStatusUpdateEvent());
                mCarDeviceConnection.sendPacket(mPacketBuilder.createSmartPhoneAudioPlaybackTimeNotification(
                        MediaSourceType.APP_MUSIC, info.durationInSec, info.positionInSec));
            }

            if (mStatus == Status.PLAYING) {
                start();
            }
        }
    }

    class PlayResumeTask implements Runnable {

        @Override
        public void run() {
            try {
                if(mStatusHolder.getCarDeviceStatus().sourceType != MediaSourceType.APP_MUSIC){
                    return;
                }

                if(!isActive()){
                    return;
                }

                play(AppMusicPlaylistCursor.Action.NEXT);
            } catch (PMGPlayerException e) {
                Timber.e(e);
            }
        }
    }
}
