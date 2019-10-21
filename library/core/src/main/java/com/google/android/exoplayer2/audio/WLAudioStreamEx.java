package com.google.android.exoplayer2.audio;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.abaltatech.weblink.core.audioconfig.AudioFormat;
import com.abaltatech.weblink.core.audioconfig.EAudioType;
import com.abaltatech.wlmediamanager.EAudioFocusState;
import com.abaltatech.wlmediamanager.IWLAudioStreamNotification;
import com.abaltatech.wlmediamanager.WLAudioManager;
import com.abaltatech.wlmediamanager.WLAudioStream;
import com.abaltatech.wlmediamanager.interfaces.WLAudioFormat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import jp.pioneer.mbg.logmanager.TAGS;
import jp.pioneer.mbg.logmanager.TagManager;
import jp.pioneer.mobile.logger.PLogger;
import jp.pioneer.mobile.logger.api.Logger;

public class WLAudioStreamEx {
    private Logger logger = TagManager.getInstance().getLogger(TAGS.ExoPlayer);

    public static final String EXCEPTION_CAUSE_IS_FOCUSLOST_BY_OUTSIDE = "EXCEPTION_CAUSE_IS_FOCUSLOST_BY_OUTSIDE";

    // 状態定義（内部用）
    protected static final int PLAY_STATE_INIT = 0;
    protected static final int PLAY_STATE_CREATED = 1;
    protected static final int PLAY_STATE_PAUSED = 2;
    protected static final int PLAY_STATE_PLAYING = 3;
    protected static final int PLAY_STATE_CLOSED = 4;
    protected static final int PLAY_STATE_NOCHANGE = -1;
    protected static final int PLAY_STATE_ERROR = -2;
    protected static final int PLAY_STATE_WAITFOCUS = -3;


    protected String getStateString(int state) {
        if (state == PLAY_STATE_INIT) return "PLAY_STATE_INIT";
        if (state == PLAY_STATE_CREATED) return "PLAY_STATE_CREATED";
        if (state == PLAY_STATE_PAUSED) return "PLAY_STATE_PAUSED";
        if (state == PLAY_STATE_PLAYING) return "PLAY_STATE_PLAYING";
        if (state == PLAY_STATE_CLOSED) return "PLAY_STATE_CLOSED";
        if (state == PLAY_STATE_NOCHANGE) return "PLAY_STATE_NOCHANGE";
        if (state == PLAY_STATE_ERROR) return "PLAY_STATE_ERROR";
        if (state == PLAY_STATE_WAITFOCUS) return "PLAY_STATE_WAITFOCUS";
        return "PLAY_STATE_UNKNOWN";
    }

    // 状態定義（外部用）
    public static final int WL_PLAYSTATE_STOPPED = 1;
    public static final int WL_PLAYSTATE_PAUSED = 2;
    public static final int WL_PLAYSTATE_PLAYING = 3;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({WL_PLAYSTATE_STOPPED, WL_PLAYSTATE_PAUSED, WL_PLAYSTATE_PLAYING})
    public @interface WLPLAYSTATE { }

    public String getPlayStateString(@WLPLAYSTATE int state) {
        if (state == WL_PLAYSTATE_STOPPED) return "WL_PLAYSTATE_STOPPED";
        if (state == WL_PLAYSTATE_PAUSED) return "WL_PLAYSTATE_PAUSED";
        if (state == WL_PLAYSTATE_PLAYING) return "WL_PLAYSTATE_PLAYING";

        return "WL_PLAYSTATE_UNKNOWN";
    }


    private WLAudioStream mAudioStream;

    private boolean mIsFocusLostByOutSide;
    private boolean mIsDoFocusWaiting;

    private Map<Integer, WLAudioStreamExProcessor> mAudioStreamProcessorList;
    private WLAudioStreamExProcessor mAudioStreamProcessor;
    private int mPlayState;
    private EAudioFocusState mFocusState;

    private long mMyThreadId;
    private String mMyThreadName;

    private long mWriteStartTime;
    private long mWriteFinishTime;

    private WeblinkAudioSink.IWLAudioFocusCallback mWLAudioFocusCallback;

    private WLAudioStreamEx() {
        mAudioStreamProcessorList = null;
        mAudioStreamProcessor = null;
        mAudioStream = null;
        mPlayState = PLAY_STATE_INIT;
        mFocusState = EAudioFocusState.AF_Loss;
        mMyThreadId = 0;
        mMyThreadName = "Unknown";
        mWriteStartTime = 0;
        mWriteFinishTime = 0;
        mIsFocusLostByOutSide = false;

        mAudioStreamProcessorList = new HashMap<>();
        mAudioStreamProcessorList.put(PLAY_STATE_INIT, new WLAudioStreamExProcessorInit());
        mAudioStreamProcessorList.put(PLAY_STATE_CREATED, new WLAudioStreamExProcessorCreated());
        mAudioStreamProcessorList.put(PLAY_STATE_PAUSED, new WLAudioStreamExProcessorPaused());
        mAudioStreamProcessorList.put(PLAY_STATE_PLAYING, new WLAudioStreamExProcessorPlaying());
        mAudioStreamProcessorList.put(PLAY_STATE_CLOSED, new WLAudioStreamExProcessorClosed());
        mAudioStreamProcessorList.put(PLAY_STATE_ERROR, new WLAudioStreamExProcessorError());
        mAudioStreamProcessorList.put(PLAY_STATE_WAITFOCUS, new WLAudioStreamExProcessorWaitFocus());

        initState();

        mWatchDogThread.start();
    }

    ////////////////////////////////////////////////////////
    // Factory
    ////////////////////////////////////////////////////////

    public static WLAudioStreamEx createStream(WLAudioFormat audioFormat, boolean isDoFocusWaiting, WeblinkAudioSink.IWLAudioFocusCallback callback) {
        WLAudioStreamEx streamEx = new WLAudioStreamEx();
        streamEx.mIsDoFocusWaiting = isDoFocusWaiting;
        streamEx.mWLAudioFocusCallback = callback;
        streamEx.mAudioStream = streamEx.create(audioFormat, streamEx.mWLAudioListener);
        streamEx.changeState(PLAY_STATE_CREATED);
        return streamEx;
    }

    ////////////////////////////////////////////////////////
    // Public
    ////////////////////////////////////////////////////////

    synchronized public int writeData(@NonNull byte[] buffer, int offset, int size, long presentationTimeUs) throws IllegalStateException {
        mWriteStartTime = System.currentTimeMillis();
        WLResult result = doAction(WL_AUDIO_EVENT_WRITEDATE, buffer, offset, size, presentationTimeUs);
        mWriteFinishTime = System.currentTimeMillis();
        return result.writtenSize;
    }

    synchronized public void pause() throws IllegalStateException {
        doAction(WL_AUDIO_EVENT_PAUSE);
    }

    synchronized public void play() throws IllegalStateException {
        doAction(WL_AUDIO_EVENT_PLAY);
    }

    synchronized public void close() {
        doAction(WL_AUDIO_EVENT_CLOSE);
    }

    synchronized public void flush() throws IllegalStateException {
        doAction(WL_AUDIO_EVENT_FLUSH);
    }

    synchronized public AudioFormat getAudioFormat() throws IllegalStateException {
        WLResult result = doAction(WL_AUDIO_EVENT_GET_AUDIOFORMAT);
        return result.audioformat;
    }

    synchronized public int getChannelID() throws IllegalStateException {
        WLResult result = doAction(WL_AUDIO_EVENT_GET_CHANNELID);
        return result.channelID;
    }

    synchronized public long getPlaybackPositionUs() throws IllegalStateException {
        WLResult result = doAction(WL_AUDIO_EVENT_GET_PLAYBACKPOSITIONUS);
        return result.playbackPositionUs;
    }

    @WLPLAYSTATE
    public int getPlayState() {
        switch (mPlayState) {
            case PLAY_STATE_INIT:                return WL_PLAYSTATE_STOPPED;
            case PLAY_STATE_CREATED:             return WL_PLAYSTATE_STOPPED;
            case PLAY_STATE_PAUSED:              return WL_PLAYSTATE_PAUSED;
            case PLAY_STATE_PLAYING:             return WL_PLAYSTATE_PLAYING;
            case PLAY_STATE_CLOSED:              return WL_PLAYSTATE_STOPPED;
            case PLAY_STATE_WAITFOCUS:           return WL_PLAYSTATE_PLAYING;
            default:
                logger.wtf();
                return WL_PLAYSTATE_STOPPED;
        }
    }

    public boolean isFocusWaiting() {
        return mPlayState == PLAY_STATE_WAITFOCUS;
    }

    public boolean isDoFocusWaiting() {
        return mIsDoFocusWaiting;
    }

    public EAudioFocusState getFocusState() {
        return mFocusState;
    }


    ////////////////////////////////////////////////////////
    // private
    ////////////////////////////////////////////////////////

    private static final int WL_AUDIO_EVENT_WRITEDATE = 0;
    private static final int WL_AUDIO_EVENT_PLAY = 1;
    private static final int WL_AUDIO_EVENT_PAUSE = 2;
    private static final int WL_AUDIO_EVENT_CLOSE = 3;
    private static final int WL_AUDIO_EVENT_FLUSH = 4;
    private static final int WL_AUDIO_EVENT_GET_AUDIOFORMAT = 5;
    private static final int WL_AUDIO_EVENT_GET_CHANNELID = 6;
    private static final int WL_AUDIO_EVENT_GET_PLAYBACKPOSITIONUS = 7;

    private WLResult doAction(int eventId, Object... args) throws IllegalStateException {
        try {
            WLResult wlresult = doActionSub(eventId, args);
            changeState(wlresult.nextState);
            return wlresult;
        } catch (IllegalStateException | IllegalArgumentException | InterruptedException e) {
            e.printStackTrace();
            logger.e("%s", e);

            changeState(PLAY_STATE_ERROR);

            // ERROR発生要因が"外的要因によるFocusLoss"だと考えられる場合、Causeを設定する
            if (mIsFocusLostByOutSide) {
                throw new IllegalStateException(new Throwable(EXCEPTION_CAUSE_IS_FOCUSLOST_BY_OUTSIDE));
            } else {
                throw new IllegalStateException(e);
            }
        }
    }

    private WLResult doActionSub(int eventId, Object... args) throws IllegalStateException, IllegalArgumentException, InterruptedException {
        switch (eventId) {
            case WL_AUDIO_EVENT_WRITEDATE:
                return lwriteData((byte[]) args[0], (int) args[1], (int) args[2], (long) args[3]);
            case WL_AUDIO_EVENT_PLAY:
                return lplay();
            case WL_AUDIO_EVENT_PAUSE:
                return lpause();
            case WL_AUDIO_EVENT_FLUSH:
                return lflush();
            case WL_AUDIO_EVENT_CLOSE:
                return lclose();
            case WL_AUDIO_EVENT_GET_AUDIOFORMAT:
                return lgetAudioFormat();
            case WL_AUDIO_EVENT_GET_CHANNELID:
                return lgetChannelID();
            case WL_AUDIO_EVENT_GET_PLAYBACKPOSITIONUS:
                return lgetPlaybackPositionUs();
            default:
                logger.wtf();
                return null;
        }
    }

    ////////////////////////////////////////////////////////
    // Private: lXXX関数
    ////////////////////////////////////////////////////////

    private WLResult lwriteData(@NonNull byte[] buffer, int offset, int size, long presentationTimeUs) throws IllegalStateException, IllegalArgumentException, InterruptedException {
        return mAudioStreamProcessor.writeData(buffer, offset, size, presentationTimeUs);
    }

    private WLResult lpause() throws IllegalStateException {
        return mAudioStreamProcessor.pause();
    }

    private WLResult lplay() throws IllegalStateException  {
        return mAudioStreamProcessor.play();
    }

    private WLResult lclose() {
        return mAudioStreamProcessor.close();
    }

    private WLResult lflush() throws IllegalStateException {
        return mAudioStreamProcessor.flush();
    }

    private WLResult lgetAudioFormat() throws IllegalStateException {
        return mAudioStreamProcessor.getAudioFormat();
    }

    private WLResult lgetChannelID() throws IllegalStateException {
        return mAudioStreamProcessor.getChannelID();
    }

    private WLResult lgetPlaybackPositionUs() throws IllegalStateException {
        return mAudioStreamProcessor.getPlaybackPositionUs();
    }

    ////////////////////////////////////////////////////////
    // Private: 状態遷移関数
    ////////////////////////////////////////////////////////

    private void initState() {
        int state = PLAY_STATE_INIT;
        mAudioStreamProcessor = mAudioStreamProcessorList.get(state);
        mAudioStreamProcessor.onEnter(null);
        mPlayState = state;
    }

    private boolean changeState(int next) {
        // 変更する必要がなければSkip
        if (next == PLAY_STATE_NOCHANGE) return false;
        if (next == mPlayState) return false;

        // PLAYINGへの変化時はスレッド情報を保存
        if (mPlayState != PLAY_STATE_PLAYING && next == PLAY_STATE_PLAYING) {
            mMyThreadId = Thread.currentThread().getId();
            mMyThreadName = Thread.currentThread().getName();
            logger.d("%s(%d)", mMyThreadName, mMyThreadId);
        }

        // 変更実施
        mAudioStreamProcessor.onLeave();
        mAudioStreamProcessor = mAudioStreamProcessorList.get(next);
        mAudioStreamProcessor.onEnter(mAudioStream);
        mPlayState = next;

        return true;
    }

    ////////////////////////////////////////////////////////
    // Private: Factoryの処理(一部)
    ////////////////////////////////////////////////////////

    private WLAudioStream create(WLAudioFormat audioFormat, IWLAudioStreamNotification listener) {

        try {
            return WLAudioManager.getInstance().startAudioStream(
                        EAudioType.AT_MEDIA,
                        audioFormat,
                        EAudioFocusState.AF_Gain_TransientMay_Duck,
                        listener);
        } catch (IllegalArgumentException e) {
            logger.e("%s", e);
            e.printStackTrace();
            return null;
        }
    }

    ////////////////////////////////////////////////////////
    // Callback: Focus変化時
    ////////////////////////////////////////////////////////

    private IWLAudioStreamNotification mWLAudioListener = new IWLAudioStreamNotification() {

        @Override
        public void onAudioFocusChanged(WLAudioStream stream, final EAudioFocusState newFocus) {
            if (PLogger.isLoggable()) {
                logger.d(" newFocus: %s\n sender:   %s(%d)\n receiver: %s(%d)\n",
                        newFocus, Thread.currentThread().getName(), Thread.currentThread().getId(), mMyThreadName, mMyThreadId);
            }

            // 自分のStreamに関する情報?
            if ( isMyStream(stream) ) {
                mFocusState = newFocus;
            }

            // 外的要因によるFocusLoss?
            if ( isFocusLostByOutside(stream, newFocus) ) {
                logger.d("外的要因によるFocusLossが発生");
                mIsFocusLostByOutSide = true;
                // Callbackが設定されている場合は通知する
                if (mWLAudioFocusCallback != null) {
                    mWLAudioFocusCallback.onWLAudioFocusLoss(newFocus);
                }
            }

        }

        @Override
        public void onAudioStreamStarted(int streamID, WLAudioFormat format, WLAudioStream stream) {
            logger.d();
        }

        @Override
        public void onAudioStreamClosed(int streamID, WLAudioStream stream, int reason) {
            logger.d();
            // ここが通るケースでは、直前に必ずonAudioFocusChanged()がコールバックされるので、
            // ここでは、特に何もしない。
        }
    };

    ////////////////////////////////////////////////////////
    // Private: Focus関連
    ////////////////////////////////////////////////////////

    private boolean isMyStream(WLAudioStream stream ) {
        return stream == mAudioStream;
    }

    private boolean isFocusLostByOutside(WLAudioStream stream, EAudioFocusState newFocus ) {
        boolean isNeed = true;

        // 条件を全て満たすか確認
        isNeed &= (stream == mAudioStream);                         // 自分のstream?
        isNeed &= isLostValue(newFocus);                            // FocusLoss?
        isNeed &= mMyThreadId != Thread.currentThread().getId();    // 外的要因?
        isNeed &= mPlayState != PLAY_STATE_CLOSED;                  // 未Close?

        return isNeed;
    }

    private boolean isLostValue(EAudioFocusState newFocus ) {
        if (newFocus == EAudioFocusState.AF_Loss) return true;
        if (newFocus == EAudioFocusState.AF_Loss_MayDuck) return true;
        if (newFocus == EAudioFocusState.AF_Loss_Transient) return true;
        if (newFocus == EAudioFocusState.AF_Blocked_Permission) return true;

        if (newFocus == EAudioFocusState.AF_Gain_Exclusive) return false;
        if (newFocus == EAudioFocusState.AF_Gain_MayDuck) return false;
        if (newFocus == EAudioFocusState.AF_Gain_TransientExclusive) return false;
        if (newFocus == EAudioFocusState.AF_Loss_TransientCanDuck) return false; // 他のAF_Gain_TransientMay_Duckな音により減衰再生になっただけ

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private: writeData()でBlocking発生したときのclose処理
    ///////////////////////////////////////////////////////////////////////////

    // WatchDog
    private Thread mWatchDogThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (mPlayState == PLAY_STATE_CLOSED) break;

                if (mPlayState == PLAY_STATE_PLAYING) { // 「PLAYING以外でBlockに中に陥ることは設計上ないはず

                    // Blockされてないかチェック
                    if (mWriteFinishTime >= mWriteStartTime) {
                        // no problem
                    } else {
                        // while writting ... so, check blocked time
                        long blockedTime = System.currentTimeMillis() -  mWriteStartTime;
                        if ( blockedTime > 2000) {
                            // Blocking発生 !!
                            logger.w("writeData() is blocked %d [sec]", blockedTime/1000);
                            // 強制Closeする(排他処理を無視するのでCrashするかもしれない)
                            try {
                                mAudioStream.closeStream();
                            } catch (IllegalStateException e) {
                                logger.w();
                                e.printStackTrace();
                            } finally {
                                break;
                            }
                        }
                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    });

}