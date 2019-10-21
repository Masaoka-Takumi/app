package jp.pioneer.mbg.alexa.manager;

/**
 * Created by esft-sakamori on 2017/08/23.
 */

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import com.google.android.exoplayer2.audio.WLAudioStreamEx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfEventItem;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeakerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.ClearQueueItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.PlayItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.StopItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.AdjustVolumeItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.SetMuteItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.SetVolumeItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechSynthesizer.SpeakItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.PlaybackFailedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.PlaybackFinishedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.PlaybackNearlyFinishedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.PlaybackPausedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.PlaybackQueueClearedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.PlaybackResumedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.PlaybackStartedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.PlaybackStoppedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.ProgressReportDelayElapsedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.ProgressReportIntervalElapsedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.StreamMetadataExtractedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.PlaybackController.NextCommandIssuedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.PlaybackController.PauseCommandIssuedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.PlaybackController.PlayCommandIssuedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.PlaybackController.PreviousCommandIssuedItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.manager.callback.IAudioCallback;
import jp.pioneer.mbg.alexa.manager.callback.IAudioControlCallback;
import jp.pioneer.mbg.alexa.player.AlexaPlayer;
import jp.pioneer.mbg.alexa.player.ExoAlexaPlayer;
import jp.pioneer.mbg.alexa.player.IAlexaPlayer;
import jp.pioneer.mbg.alexa.player.WLAlexaPlayer;
import jp.pioneer.mbg.alexa.util.Constant;
import jp.pioneer.mbg.alexa.util.LogUtil;
import jp.pioneer.mbg.android.vozsis.R;
import okhttp3.Call;

/**
 * AudioPlayer専用再生マネージャ
 */
//public class AlexaAudioManager implements AlexaPlayer.PlaybackCallback {
public class AlexaAudioManager implements IAlexaPlayer.PlaybackCallback {
    private final static String TAG = AlexaAudioManager.class.getSimpleName();
    private static final boolean DBG = true;

    private static AlexaAudioManager mAlexaAudioManager = null;
    /**
     * 音楽Queue
     */
    private ArrayList<PlayItem> mPlaybackList = null;

    /**
     * プレーヤー
     */
//    private AlexaPlayer mPlayer = null;
    private IAlexaPlayer mPlayer = null;
    private ExoAlexaPlayer mURLPlayer = null;
    private IAlexaPlayer mFDPlayer = null;

    private Handler mHandler = null;
    /**
     * ExoPlayerを生成するスレッド
     */
    HandlerThread mCreatePlayerHandlerThread = null;

    /**
     * 直前に再生したPlayディレクティブのトークン(Context送信用)
     */
    private String mPreviousToken = null;
    /**
     * 再生中のPlayディレクティブ
     */
    private PlayItem mCurrentItem = null;

    private Context mContext = null;

    /**
     * 再生時間監視スレッド
     */
    private ProgressThread mProgressThread = null;

    /**
     * 再生状態コールバックインスタンス
     */
    private IAudioCallback mAudioCallback = null;

    /* プレイリスト再生処理用データ */
    /**
     * プレイリストのURLリスト
     */
    private ArrayList<String> mPlaylistUrls = new ArrayList<String>();
    /**
     * プレイリストの再生中Index
     */
    private int mPlaylistIndex = 0;

    public boolean isHasAudioResponse = false;

    /**
     * コンストラクタ
     */
    private AlexaAudioManager() {
        mHandler = new Handler(Looper.getMainLooper());
        mPlaybackList = new ArrayList<>();
    }

    /**
     * AlexaAudioManager取得
     * @return
     */
    public static AlexaAudioManager getInstance() {
        if (mAlexaAudioManager == null) {
            mAlexaAudioManager = new AlexaAudioManager();
        }
        return mAlexaAudioManager;
    }

    /**
     * playerのみの破棄
     */
    public static void releasePlayer() {
        if (mAlexaAudioManager.mPlayer != null) {
            mAlexaAudioManager.mPlayer.stopPlayer();            // 再生中音楽の停止
            mAlexaAudioManager.mPlayer.releasePlayer();
        }
        mAlexaAudioManager.mPlaybackList.clear();           // 音楽リストのクリア
        mAlexaAudioManager.mPlaylistUrls.clear();           // プレイリストのクリア
    }

    public static void stopPlayer() {
        if (mAlexaAudioManager.mPlayer != null) {
            mAlexaAudioManager.mPlayer.stopPlayer();            // 再生中音楽の停止
        }
        mAlexaAudioManager.mPlaybackList.clear();           // 音楽リストのクリア
        mAlexaAudioManager.mPlaylistUrls.clear();           // プレイリストのクリア
    }


    /**
     * AlexaAudioManagerの破棄
     */
    public static void resetManager() {
        releasePlayer();
        // 前回トークンを削除
        mAlexaAudioManager.mPreviousToken = null;
        // 再生中アイテムを削除
        mAlexaAudioManager.mCurrentItem = null;
        // コンテキストを削除
        mAlexaAudioManager.mContext = null;
        // プログレススレッドを停止
        if (mAlexaAudioManager.mProgressThread != null) {
            mAlexaAudioManager.mProgressThread.cancel();
            mAlexaAudioManager.mProgressThread = null;
        }
        // コールバックインスタンスを削除
        mAlexaAudioManager.mAudioCallback = null;
        // 再生中Indexを初期化
        mAlexaAudioManager.mPlaylistIndex = 0;
        // SetMuteディレクティブのスルー用フラグの初期化
        mAlexaAudioManager.isHasAudioResponse = false;

        //プレイヤーの初期化
//        if (mAlexaAudioManager.mPlayer != null) {
//            mAlexaAudioManager.mPlayer.release();
//        }
        mAlexaAudioManager.mPlayer = null;
        if (mAlexaAudioManager.mURLPlayer != null) {
            mAlexaAudioManager.mURLPlayer.release();
        }
        if (mAlexaAudioManager.mFDPlayer != null) {
            mAlexaAudioManager.mFDPlayer.release();
        }
        mAlexaAudioManager.mURLPlayer = null;
        mAlexaAudioManager.mFDPlayer = null;
    }

    /**
     * コンテキストの設定
     * @param context
     * @return
     */
    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * AlexaPlayerの生成
     */
    public void createAlexaPlayer(Context context) {
        setContext(context);
//        if (this.mPlayer == null) {
//            // ExoPlayerを２つ以上生成するとIllegalStateExceptionでクラッシュするため、NULLの時のみ生成させる
//            setAlexaPlayer(new ExoAlexaPlayer(mContext, null));
//        }
        if (this.mURLPlayer == null) {
            // ExoPlayerを２つ以上生成するとIllegalStateExceptionでクラッシュするため、NULLの時のみ生成させる
            mURLPlayer = new ExoAlexaPlayer(mContext, null);
        }
        if (this.mFDPlayer == null) {
            if (false/*DebugManager.isBoolean(DebugManager.DebugSettingId.DEBUG_SETTING_ID_WEBLINK_AUDIO_SPEAK)*/) {
                mFDPlayer = new WLAlexaPlayer(context);
            }
            else {
                mFDPlayer = new AlexaPlayer(context, null);
            }
        }
        setAlexaPlayer(mURLPlayer);
    }

    /**
     * AlexaPlayerインスタンスの設定
     * @param player
     */
    private void setAlexaPlayer(IAlexaPlayer player) {
        this.mPlayer = player;
        this.mPlayer.setCallback(this);
    }

    /**
     * 使用中のAlexaPlayerインスタンスの取得
     * @return
     */
    public IAlexaPlayer getAlexaPlayer() {
        return this.mPlayer;
    }

    /**
     * 再生状態の取得
     * @return
     */
    public boolean isPlaying() {
        if (this.mPlayer != null) {
            return  this.mPlayer.isPlaying();
        }
        return false;
    }

    /**
     * 音楽再生状態コールバックインスタンスの登録
     * @param callback
     */
    public void setIAudioCallback(IAudioCallback callback) {
        this.mAudioCallback = callback;
    }

    /**
     * 再生中のPlayディレクティブ取得
     * @return
     */
    public AlexaIfDirectiveItem getCurrentPlayItem() {
        AlexaIfDirectiveItem result = null;
        if (mCurrentItem != null) {
            result = mCurrentItem;
        }
        else {
            result = null;
        }
        return result;
    }

    /**
     * 音楽Queueを取得
     * @return
     */
    public ArrayList<PlayItem> getPlaybackList() {
        return mPlaybackList;
    }

    /**
     * 直前に再生した音楽のトークンを取得
     * @return
     */
    public String getPreviousToken() {
        return mPreviousToken;
    }

    /**
     * コンテンツチャネルがフォアグラウンドに移行
     * @return 音楽再生する場合：true
     */
    public boolean onForegroundContentsChannel() {
        boolean result = false;
        if (mPlayer != null && mPlayer.isPlaying()) {
            // 再生中
            result = true;
        }
        if (mPlaybackList != null && mPlaybackList.size() > 0) {
            if (mPlayer != null && mCurrentItem != null) {
                // 音楽再生を復帰
                boolean isStart = mPlayer.start();
                if (isStart == true) {
                    // チャンネルをコンテンツに更新
                    AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                    queueManager.startContentsChannel();

                    //PlaybackResumeイベントを送信
                    if (mCurrentItem != null) {
                        String token = mCurrentItem.audioItem.stream.token;
                        long offsetInMilliseconds = mPlayer.getCurrentPosition();
                        PlaybackResumedItem event = new PlaybackResumedItem(token, offsetInMilliseconds);
                        sendEvent(event);
                        if (DBG) android.util.Log.d(TAG, "PlaybackResume", new Throwable());
                    }
                    result = true;
                }
            }
            else {
                // チャンネルをコンテンツに更新
                AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                queueManager.startContentsChannel();

                // 再生していない音楽を再生開始
                PlayItem item = mPlaybackList.get(0);
                play(item);
                result = true;
            }
        }
        return result;
    }

    /**
     * コンテンツチャネルがバックグラウンドに移行
     */
    public void onBackgroundContentsChannel() {
        // 音楽再生中の場合、一時停止する
        if (mPlayer != null && mPlayer.isPlaying() == true) {
            boolean isPause = mPlayer.pause();
            mPlayer.setForcedStopping(false);
            //PlaybackPauseイベントを送信
            if (isPause == true && mCurrentItem != null) {
                String token = mCurrentItem.audioItem.stream.token;
                long offsetInMilliseconds = mPlayer.getCurrentPosition();
                PlaybackPausedItem event = new PlaybackPausedItem(token, offsetInMilliseconds);
                sendEvent(event);
                if (DBG) android.util.Log.d(TAG, " PlaybackPause", new Throwable());
            }
        }
    }

    public void changeMute(boolean isMute){
      mPlayer.setMuteCallback(isMute);
    }

    public void pause(){
        if(mPlayer != null) {
            mPlayer.pause();
        }
    }
    /**
     * ユーザー操作で再生
     */
    public void doPlay(final IAudioControlCallback callback) {
        if (mPlayer != null) {
            // イベントを通知
            {
                PlayCommandIssuedItem event = new PlayCommandIssuedItem();
                AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, true, new AlexaEventManager.AlexaCallback() {
                    @Override
                    public void onExecute(Call call) {
                        if (DBG) android.util.Log.d(TAG, "PlayCommandIssued onExecute()");
                    }

                    @Override
                    public void onResponse(Call call, int httpCode) {
                        if (DBG) android.util.Log.d(TAG, "PlayCommandIssued onResponse()");

                        if (callback != null) {
                            callback.onResponse();
                        }

                        if (200 <= httpCode && httpCode < 300) {
                            // 成功
                            if (DBG) android.util.Log.d(TAG, " - PlayCommandIssued onResponse(), Success");
                        }
                        else {
                            // 失敗
                            android.util.Log.w(TAG, " - PlayCommandIssued onResponse(), Error");
                            if (mAudioCallback != null) {
                                mAudioCallback.onError(mCurrentItem);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        android.util.Log.w(TAG, "PlayCommandIssued onFailure(), e = " + e);
                        if (mAudioCallback != null) {
                            mAudioCallback.onError(mCurrentItem);
                        }
                    }

                    @Override
                    public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                        if (DBG) android.util.Log.w(TAG, "PlayCommandIssued onParsedResponse(), itemList = " + itemList);
                        checkItemList(itemList);
                    }
                });
            }
        }
    }

    /**
     * ユーザー操作で一時停止
     */
    public void doPause(final IAudioControlCallback callback) {
        if (mPlayer != null) {
            {
                PauseCommandIssuedItem event = new PauseCommandIssuedItem();
                AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, true, new AlexaEventManager.AlexaCallback() {
                    @Override
                    public void onExecute(Call call) {
                        if (DBG) android.util.Log.d(TAG, "PauseCommandIssued onExecute()");
                    }

                    @Override
                    public void onResponse(Call call, int httpCode) {
                        if (DBG) android.util.Log.d(TAG, "PauseCommandIssued onResponse()");

                        if (callback != null) {
                            callback.onResponse();
                        }

                        if (200 <= httpCode && httpCode < 300) {
                            // 成功
                            if (DBG) android.util.Log.d(TAG, " - PauseCommandIssued onResponse(), Success");
                        }
                        else {
                            // 失敗
                            android.util.Log.w(TAG, " - PauseCommandIssued onResponse(), Error");
                            if (mAudioCallback != null) {
                                mAudioCallback.onError(mCurrentItem);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        android.util.Log.w(TAG, "PauseCommandIssued onFailure(), e = " + e);
                        if (mAudioCallback != null) {
                            mAudioCallback.onError(mCurrentItem);
                        }
                    }

                    @Override
                    public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                        if (DBG) android.util.Log.w(TAG, "PauseCommandIssued onParsedResponse(), itemList = " + itemList);
                    }
                });
            }
        }
    }

    /**
     * ユーザー操作でトラック送り
     */
    public void doNext(final IAudioControlCallback callback) {
        //トラック送り

        // イベントを通知
        {
            NextCommandIssuedItem event = new NextCommandIssuedItem();
            AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, true, new AlexaEventManager.AlexaCallback() {
                @Override
                public void onExecute(Call call) {
                    if (DBG) android.util.Log.d(TAG, "NextCommandIssued onExecute()");
                }

                @Override
                public void onResponse(Call call, int httpCode) {
                    if (DBG) android.util.Log.d(TAG, "NextCommandIssued onResponse()");

                    if (callback != null) {
                        callback.onResponse();
                    }

                    if (200 <= httpCode && httpCode < 300) {
                        // 成功
                        if (DBG) android.util.Log.d(TAG, " - NextCommandIssued onResponse(), Success");
                    }
                    else {
                        // 失敗
                        android.util.Log.w(TAG, " - NextCommandIssued onResponse(), Error");
                        if (mAudioCallback != null) {
                            mAudioCallback.onError(mCurrentItem);
                        }
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    android.util.Log.w(TAG, "NextCommandIssued onFailure(), e = " + e);
                    if (mAudioCallback != null) {
                        mAudioCallback.onError(mCurrentItem);
                    }
                }

                @Override
                public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                    if (DBG) android.util.Log.w(TAG, "NextCommandIssued onParsedResponse(), itemList = " + itemList);
                    checkItemList(itemList);
                }
            });
        }
    }

    /**
     * ユーザー操作でトラック戻り
     */
    public void doPrev(final IAudioControlCallback callback) {

        // イベントを通知
        {
            PreviousCommandIssuedItem event = new PreviousCommandIssuedItem();
            AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, true, new AlexaEventManager.AlexaCallback() {
                @Override
                public void onExecute(Call call) {
                    if (DBG) android.util.Log.d(TAG, "PreviousCommandIssued onExecute()");
                }

                @Override
                public void onResponse(Call call, int httpCode) {
                    if (DBG) android.util.Log.d(TAG, "PreviousCommandIssued onResponse()");

                    if (callback != null) {
                        callback.onResponse();
                    }

                    if (200 <= httpCode && httpCode < 300) {
                        // 成功
                        if (DBG) android.util.Log.d(TAG, " - PreviousCommandIssued onResponse(), Success");
                    }
                    else {
                        // 失敗
                        android.util.Log.w(TAG, " - PreviousCommandIssued onResponse(), Error");
                        if (mAudioCallback != null) {
                            mAudioCallback.onError(mCurrentItem);
                        }
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    android.util.Log.w(TAG, "PreviousCommandIssued onFailure(), e = " + e);
                    if (mAudioCallback != null) {
                        mAudioCallback.onError(mCurrentItem);
                    }
                }

                @Override
                public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                    if (DBG) android.util.Log.w(TAG, "PreviousCommandIssued onParsedResponse(), itemList = " + itemList);
                    checkItemList(itemList);
                }
            });
        }
    }

    private void checkItemList(ArrayList<AlexaIfDirectiveItem> itemList){
        boolean isPlayingDirective = false;
        if (itemList != null && itemList.size() > 0) {
            // Playディレクティブ、及び、Speakディレクティブがあるかチェック
            for (AlexaIfItem item : itemList) {
                if (item instanceof PlayItem) {
                    isPlayingDirective = true;
                    break;
                }
                else if (item instanceof SpeakItem) {
                    isPlayingDirective = true;
                    break;
                }
            }
        }
        if (!isPlayingDirective) {
            // 再生処理を行うディレクティブが無い -> インジケータを非表示にする
            if (mAudioCallback != null) {
                mAudioCallback.onNoDirectiveAtSendEventResponse();
            }
        }
    }


    /**
     * Playディレクティブ振り分け
     * @param item
     */
    public void post(PlayItem item) {
        if (DBG) android.util.Log.d(TAG, "post() 1");

//        if(isPlay) {
//            //TODO:再生管理フラグ調整中
//            AlexaQueueManager.startDialogChannel();
//        }

        ArrayList<PlayItem> itemList = new ArrayList<>();
        itemList.add(item);
        this.post(itemList);
    }

    /**
     * レスポンス解析完了コールバック.
     *
     * @param itemList
     */
    public void post(final ArrayList<PlayItem> itemList) {
        if (DBG) android.util.Log.d(TAG, "post() 2");

        boolean canPlay = false;
        if (itemList != null && itemList.size() > 0) {
            // プレイリストを初期化
            if (mPlaybackList == null) {
                // 初回取得時はそのまま再生を行う.
                mPlaybackList = itemList;
                canPlay = true;
            } else {
//                int size = mPlaybackList.size();
                for (PlayItem item : itemList) {
                    // 既にリストがある場合は既存のリストに追加する.
                    if(Constant.BEHAVIOR_REPLACE_ALL.equals(item.playBehavior)) {
                        //TODO:Alexa Stop CallBackが遅延により、MetaDataアリ→MetaDataナシへ移行すると
                        // Metadataがもれてしまうのでこちらで対策(仮置き)
                        //AlexaFragment.setMetadata();

                        mPlaybackList.clear();

                        // PlayBehaviorがREPLACE_ALLだった場合はPLAYBACK_STOPPEDイベント送信.
                        if (mCurrentItem != null) {
                            boolean isSendEvent = false;
                            if (mPlayer instanceof WLAlexaPlayer) {
                                if (mPlayer.getWLPlaybackState() == WLAudioStreamEx.WL_PLAYSTATE_PAUSED || mPlayer.getWLPlaybackState() == WLAudioStreamEx.WL_PLAYSTATE_PLAYING) {
                                    isSendEvent = true;
                                }
                            } else {
                                if (mPlayer.getPlaybackState() == AlexaPlayer.PlaybackState.PAUSE || mPlayer.getPlaybackState() == AlexaPlayer.PlaybackState.PLAYING) {
                                    isSendEvent = true;
                                }
                            }
                            if (isSendEvent) {
                                // 再生中、又は、一時停止中(ContentChannelがバックグラウンド)の場合、PlaybackStoppedイベントを送信
                                String token = mCurrentItem.audioItem.stream.token;
                                long offsetInMilliseconds = mPlayer.getCurrentPosition();
                                PlaybackStoppedItem stoppedEvent = new PlaybackStoppedItem(token, offsetInMilliseconds);
                                sendEvent(stoppedEvent);
                            }
                        }
                        mCurrentItem = null;        // 現在の再生アイテムを破棄
                        if(mPlayer != null) {
                            mPlayer.stopPlayer();
                        }
                    }
                    else if (Constant.BEHAVIOR_REPLACE_ENQUEUED.equals(item.playBehavior)) {
                        mPlaybackList.clear();
                        if (mCurrentItem != null) {
                            mPlaybackList.add(mCurrentItem);    // 現在再生中のItemのみ残す
                        }
                    }
                    mPlaybackList.add(item);
                }
//                // 現在停止中で、既存のリストの再生が完了している場合は再生する.
//                canPlay = mPlayer == null
//                        || (size < 1 && mPlayer != null && !mPlayer.isPlaying());
                canPlay = (mPlaybackList.get(0).equals(mCurrentItem) == false);
            }

            // アクティブのチャンネルを判定
            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            AlexaQueueManager.AlexaChannel currentChannel = queueManager.getForegroundChannel();
            if (currentChannel == AlexaQueueManager.AlexaChannel.DialogChannel || currentChannel == AlexaQueueManager.AlexaChannel.AlertChannel) {
                canPlay = false; // 再生不可
            }

            if (DBG) android.util.Log.d(TAG, " - post() canPlay = " + canPlay);
            if (canPlay) {
                // チャンネルをコンテンツに更新
                queueManager.startContentsChannel();

                PlayItem item = mPlaybackList.get(0);
                // 現在再生しているデータを保持する.
                if (DBG) android.util.Log.d(TAG, " - post() play item = " + item);
                if (item instanceof PlayItem) {
                    play(item);
                }
            }
        }
    }

    /**
     * 再生系機能で、再生開始以外のDirectiveを処理する.
     * ・StopItem
     * ・ClearQueueItem
     * ・SetMuteItem
     * ・SetVolumeItem
     * ・AdjustVolumeItem
     * @param item
     */
    public void requestEvent(AlexaIfDirectiveItem item) {
        if (DBG) android.util.Log.d(TAG, "requestEvent()");
        if (item != null) {
            if (DBG) android.util.Log.d(TAG, " - requestEvent() item = " + item);
            String token = "";
            boolean isEventSend = false;
            AlexaIfEventItem event = null;

            if (item instanceof StopItem) {
                if (DBG) android.util.Log.d(TAG, " - requestEvent(StopItem) mPreviousToken = " + mPreviousToken);
                event = postStop((StopItem) item);
                if (event != null) {
                    isEventSend = true;
                }
                else {
                    isEventSend = false;
                }
            } else if (item instanceof ClearQueueItem) {
                ClearQueueItem clearQueueItem = (ClearQueueItem) item;
                if (Constant.BEHAVIOR_CLEAR_ENQUEUED.equals(clearQueueItem.clearBehavior)) {
                    if (DBG) android.util.Log.d(TAG, " - requestEvent(ClearQueueItem) BEHAVIOR_CLEAR_ENQUEUED");
                    // 再生中のものはそのまま、再生キューをクリアする
                    if (mPlaybackList != null && mPlaybackList.size() > 0) {
                        PlayItem backItem = mPlaybackList.get(0);
                        mPlaybackList.clear();
                        mPlaybackList.add(backItem);
                        isEventSend = true;
                        event = new PlaybackQueueClearedItem();
                    }
                }
                else if (Constant.BEHAVIOR_CLEAR_ALL.equals(clearQueueItem.clearBehavior)) {
                    if (DBG) android.util.Log.d(TAG, " - requestEvent(ClearQueueItem) BEHAVIOR_CLEAR_ALL");
                    // 再生中のものも含め、再生キューをクリアする
                    if (mPlayer != null) {
                        mPlayer.stopPlayer();
                    }
                    mPlaybackList.clear();
                    isEventSend = true;
                    event = new PlaybackQueueClearedItem();
                }
                else {
                    // 予期しない値
                }
            } else if (item instanceof AlexaIfSpeakerItem) {
                // SPEAKER Interface時の処理.
                isEventSend = true;
                AlexaIfSpeakerItem speakerItem = ((AlexaIfSpeakerItem) item);

                if (item instanceof SetMuteItem) {
                    // Mute設定時の対応.
                    final boolean isMute = ((SetMuteItem) speakerItem).mute;

                    // Mute変更処理.
                    if (mPlayer != null) {
                        mPlayer.setMuteCallback(isMute);
                        //mPlayer.setMute(isMute);
                        //int volume = (int) mPlayer.getVolume();
                        //event = new MuteChangedItem(volume, isMute);
                    }
                } else if (item instanceof SetVolumeItem) {
                    // Volume変更時の対応.
                    long volume = ((SetVolumeItem) speakerItem).volume;

                    if (mPlayer != null) {
                        mPlayer.setVolumeCallback(volume);
                        //mPlayer.setVolume(volume);
                        //event = new MuteChangedItem(volume, mPlayer.isMute());
                        //event = new VolumeChangedItem(volume, mPlayer.isMute());
                    }
                }
                else if (item instanceof AdjustVolumeItem) {
                    long volume = ((AdjustVolumeItem) speakerItem).volume;

                    if (mPlayer != null) {
                        mPlayer.adjustVolumeCallback(volume);
                        //mPlayer.adjustVolume(volume);
                        //event = new MuteChangedItem((int)mPlayer.getVolume(), mPlayer.isMute());
                        //event = new VolumeChangedItem((int)mPlayer.getVolume(), mPlayer.isMute());
                    }
                }
            }

            if (isEventSend && event != null) {
                if (DBG) android.util.Log.d(TAG, " - requestEvent() send event = " + event);
                sendEvent(event);
            }
        }
    }

    /**
     * Stopディレクティブを受信した時の処理
     */
    private PlaybackStoppedItem postStop(StopItem item) {
        if (DBG) android.util.Log.d(TAG, "postStop()");
        PlaybackStoppedItem event = null;
        if (mPreviousToken != null) {
            String token = mPreviousToken;
            if (!TextUtils.isEmpty(token)) {
                // PLAYBACK_STOPPEDイベントはトークンがないとエラーが返却されるので、
                // 取得できない場合は送信しない.
                int offsetInMilliseconds = 0;
                if (mPlayer != null) {
                    offsetInMilliseconds = mPlayer.getCurrentPosition();
                    mPlayer.pause();
                    mCurrentItem = null;
                    mPlayer.setForcedStopping(true);        // 停止扱い
                }
                event = new PlaybackStoppedItem(token, offsetInMilliseconds);
            }

            // Queue格納中のPlayディレクティブとCurrentを削除する。
            if (mCurrentItem != null) {
                mCurrentItem = null;
            }
            if (mPlaybackList != null) {
                mPlaybackList.clear();
            }

            // Contentチャネルを終了
           // AlexaQueueManager.endContentsChannel();
        }
        return event;
    }

    /**
     * アッテネート設定
     * @param attenuate
     */
    public void setAttenuate(boolean attenuate) {
        if (mPlayer != null) {
            mPlayer.setAttenuate(attenuate);
        }
    }

    /**
     * 音楽再生処理
     * @param item
     */
    private void play(final PlayItem item) {
        if (DBG) android.util.Log.d(TAG, "play() 2");
        if (DBG) android.util.Log.d(TAG, " - play() 2, item = " + item);

        if(item != null){
            mCurrentItem = item;
            mPlaylistUrls.clear();
            mPlaylistIndex = 0;

            FileInputStream fis = null;
            if (item.getAudioContent() != null) {
                byte[] audioContent = item.getAudioContent();
                File tempMp3 = null;
                FileOutputStream fos = null;
                try {
                    tempMp3 = File.createTempFile("alexa", ".mp3", mContext.getCacheDir());
                    fos = new FileOutputStream(tempMp3);
                    fos.write(audioContent, 0, audioContent.length);
                    fis = new FileInputStream(tempMp3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    if (tempMp3 != null) {
                        tempMp3.delete();
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if(fis != null && mPlayer != null){
                if (DBG) android.util.Log.d(TAG, " - play() has fileDescriptor.");
                if (mPlayer.isPlaying() == true) {
                    mPlayer.stopPlayer();
                }
                setAlexaPlayer(mFDPlayer);
                // AndroidOSのAudioFocusを取得
                AmazonAlexaManager.getInstance().requestAudioFocus();
                try {
                    mPlayer.createPlayer(fis.getFD(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                    // 停止処理でエラー
                    String message = null;
                    Resources r = mContext.getResources();
                    if (r != null) {
                        message = r.getString(R.string.alexa_playback_failed_create_player);
                    }
                    onError(IAlexaPlayer.PlaybackErrorType.ERROR_UNKNOWN, message);
                }
                // GalaxyS5(Android6.0.1)ではSecurityExceptionが発生するためcatch
                catch (SecurityException e) {
                    e.printStackTrace();
                    String message = null;
                    Resources r = mContext.getResources();
                    if (r != null) {
                        message = r.getString(R.string.alexa_playback_failed_create_player);
                    }
                    onError(IAlexaPlayer.PlaybackErrorType.ERROR_UNKNOWN, message);
                }
            }
            else {
                if (DBG) android.util.Log.d(TAG, " - play() not has fileDescriptor.");
                setAlexaPlayer(mURLPlayer);

                PlayItem streamItem = null;
                for(int i = 0;i < mPlaybackList.size();i++){
                    AlexaIfDirectiveItem tempItem = mPlaybackList.get(i);
                    if(tempItem instanceof PlayItem){
                        PlayItem audioItem = (PlayItem) tempItem;
                        if (DBG) android.util.Log.d(TAG, " - play() playBehavior is " + audioItem.playBehavior);

                        {
                            if(audioItem.audioItem.stream.url != null){
                                streamItem = audioItem;
                                break;
                            }
                        }
                    }
                    if (DBG) android.util.Log.d(TAG, " - play() POINT_1 streamItem = " + streamItem);
                }

                if (DBG) android.util.Log.d(TAG, " - play() POINT_2 streamItem = " + streamItem);
                if(streamItem != null){
                    // PlayListのチェック
                    final String reserveUrl = streamItem.audioItem.stream.url;
                    if(!TextUtils.isEmpty(reserveUrl)){
                        if (mCreatePlayerHandlerThread == null) {
                            mCreatePlayerHandlerThread = new HandlerThread("createPlayer");
                            mCreatePlayerHandlerThread.start();
                        }
                        Handler handler = new Handler(mCreatePlayerHandlerThread.getLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mPlaylistUrls.clear();
                                mPlaylistIndex = 0;

                                // PlayListの解析に時間がかかるためプログレスを出す
                                if (mAudioCallback != null) {
                                    mAudioCallback.onDecodeStart();
                                }
                                ArrayList<String> playlist = decodePlaylist(reserveUrl);
                                if (mCurrentItem != item) {
                                    if (mAudioCallback != null) {
                                        mAudioCallback.onDecodeFinish();
                                    }
                                    return;
                                }
                                else {
                                    mPlaylistUrls.addAll(playlist);
                                }

                                if (mPlaylistUrls.size() > 0) {
                                    String url = mPlaylistUrls.get(mPlaylistIndex);
                                    boolean success = createPlayer(url);
                                    if (success == false) {
                                        // バッファリング開始時にエラー
                                        String message = null;
                                        Resources r = mContext.getResources();
                                        if (r != null) {
                                            message = r.getString(R.string.alexa_playback_failed_create_player);
                                        }
                                        onError(IAlexaPlayer.PlaybackErrorType.ERROR_UNKNOWN, message);
                                    }
                                }
                                else {
                                    //プログレス消す
                                    if (mAudioCallback != null) {
                                        mAudioCallback.onDecodeFinish();
                                    }
                                    postComplete();
                                }
                            }
                        });
                    }
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * MediaPlayerインスタンスを生成
     * @param url
     * @return
     */
    private boolean createPlayer(String url) {
        if (DBG) android.util.Log.d(TAG, "createPlayer(), url = " + url);
        boolean success = false;
        if (mPlayer != null) {
            // AndroidOSのAudioFocusを取得
            AmazonAlexaManager.getInstance().requestAudioFocus();
            try {
                mPlayer.createPlayer(url, true);
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
        }
        if (DBG) android.util.Log.d(TAG, " - createPlayer(), result = " + success);
        return success;
    }

    /**
     * 再帰的にプレイリストのURLを解析し、すべての楽曲をmPlaylistUrlsに登録する
     * @param playlistUrl
     */
    private ArrayList<String> decodePlaylist(String playlistUrl) {
        return decodePlaylist(playlistUrl, 0);
    }

    /**
     * プレイリストURLの解析
     * @param playlistUrl
     * @param debug
     */
    private ArrayList<String> decodePlaylist(String playlistUrl, int debug) {
        if (DBG) android.util.Log.d(TAG, "decodePlaylist(), url = " + playlistUrl);
        if (DBG) android.util.Log.d(TAG, "decodePlaylist(), debug = " + debug);
        ArrayList<String> result = new ArrayList<String>();
        if (debug > 5) {
            // TODO:再帰が多い場合、中止する。(フェールセーフ)
            if (DBG) android.util.Log.d(TAG, " - decodePlaylist(), return by FailSafe.");
            return result;
        }
        if (TextUtils.isEmpty(playlistUrl) || playlistUrl.indexOf("cid") == 0) {
            // 楽曲orPlayListのURLなし
        }
        else {
            ArrayList<String> playlist = AudioPlaylistDecoder.decodePlaylist(playlistUrl);
            if (playlist.size() > 0) {
                // プレイリスト
                if (DBG) android.util.Log.d(TAG, " - decodePlaylist(), PlayList.");
                for (String url : playlist) {
                    ArrayList<String> urls = decodePlaylist(url, debug + 1);
                    result.addAll(urls);
                }
            } else {
                // 楽曲URL
                if (DBG) android.util.Log.d(TAG, " - decodePlaylist(), Music.");
                result.add(playlistUrl);
            }
        }
        return result;
    }

    /**
     * イベント送信メソッド
     * @param event
     */
    private synchronized void sendEvent(final AlexaIfEventItem event) {
        if (DBG) android.util.Log.d(TAG, "sendEvent(), " + event);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {

                    @Override
                    public void onExecute(Call call) {
                        if (DBG) android.util.Log.d(TAG, event.getName() + " onExecute()");
                    }

                    @Override
                    public void onResponse(Call call, int httpCode) {
                        if (DBG) android.util.Log.d(TAG, event.getName() + " onResponse()");

                        if (200 <= httpCode && httpCode < 300) {
                            // 成功
                            if (DBG) android.util.Log.d(TAG, " - " + event.getName() + " onResponse(), Success");
                        }
                        else {
                            // 失敗
                            android.util.Log.w(TAG, " - " + event.getName() + " onResponse(), Error");
                            if (mAudioCallback != null) {
                                mAudioCallback.onError(mCurrentItem);
                            }
                        }

                        //Response内容あったらTrue
                        if(httpCode ==200){
                            android.util.Log.d(TAG, " - " + event.getName() + " onResponse(), Success");
                            isHasAudioResponse = true;
                        }
                        else{
                            isHasAudioResponse = false;
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        android.util.Log.w(TAG, event.getName() + " onFailure(), e = " + e);
                        if (mAudioCallback != null) {
                            mAudioCallback.onError(mCurrentItem);
                        }
                    }

                    @Override
                    public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                        if (DBG) android.util.Log.w(TAG, event.getName() + " onParsedResponse(), itemList = " + itemList);
                    }
                });
        }
        });
        t.start();

        if (DBG) android.util.Log.i(TAG, "TEST - sendEvent().", new Throwable());

    }

    /* -- AlexaPlayer#PlaybackCallback -- */
    /**
     * 再生完了
     */
    @Override
    public void onCompletion() {
        if (DBG) android.util.Log.d(TAG, "onCompletion()");

        int size = -1000;
        if(mPlaybackList != null && mPlaybackList.size() > 0){
            LogUtil.i(TAG, "onCompletion start finish event");

            if (mAudioCallback != null) {
                mAudioCallback.onComplete(mCurrentItem);
            }
            if (mProgressThread != null) {
                mProgressThread.cancel();
                mProgressThread = null;
            }

            // 終了通知の送信.
            if (mCurrentItem != null) {
                String token = mCurrentItem.audioItem.stream.token;

                if(mPlayer != null) {
                    final PlaybackFinishedItem event = new PlaybackFinishedItem(token, mPlayer.getDuration());
                    sendEvent(event);
                    LogUtil.d(TAG, " - send PlaybackFinished.");
                }
            }

            // 再生アイテムを削除する.
            postComplete();
            size = mPlaybackList.size();
        }

        LogUtil.d(TAG, "onCompletion end size:"+size);
    }

    /**
     * 読み込み開始.
     */
    @Override
    public long onPrepare() {
        if (DBG) android.util.Log.d(TAG, "onPrepare()");
        if (mAudioCallback != null) {
            mAudioCallback.onPrepare(mCurrentItem);
        }
        if (DBG) android.util.Log.d(TAG, "onPlaybackReady()");
        long offsetInMilliseconds = 0;
        if (mCurrentItem != null) {
            offsetInMilliseconds = mCurrentItem.audioItem.stream.offsetInMilliseconds;
        }
        return offsetInMilliseconds;
    }

    /**
     * 読み込み完了
     */
    @Override
    public boolean onPrepared() {
        if (DBG) android.util.Log.d(TAG, "onPrepared()");
        if (mAudioCallback != null) {
            mAudioCallback.onPrepared(mCurrentItem);
        }
       // public boolean isPlay;
        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
        AlexaQueueManager.AlexaChannel currentChannel = queueManager.getForegroundChannel();

        boolean isPlay = false;
        if(currentChannel != AlexaQueueManager.AlexaChannel.DialogChannel && currentChannel != AlexaQueueManager.AlexaChannel.AlertChannel){
            if (mAudioCallback != null) {
                if (mAudioCallback.isAlexaPlayable()) {
                    //音楽再生させる
                    isPlay = true;
                }
            }
        }
        else{
            //再生させない
            isPlay = false;
        }

        return isPlay;
    }

    /**
     * エラー
     */
    @Override
//    public void onError(AlexaPlayer.PlaybackErrorType type, String message) {
    public void onError(IAlexaPlayer.PlaybackErrorType type, String message) {
        if (DBG) android.util.Log.d(TAG, "onError()", new Throwable());
        if (mAudioCallback != null) {
            mAudioCallback.onError(mCurrentItem);
        }

        if(mPlayer != null){
            mPlayer.stopPlayer();
        }
        // releasePlayer();
        if(mPlaybackList != null && mPlaybackList.size() > 0) {
            AlexaIfDirectiveItem item = mPlaybackList.get(0);
            if (item instanceof PlayItem) {
                String token = ((PlayItem)item).audioItem.stream.token;
                // Interfaceごとのイベント開始通知.
                AlexaIfEventItem.CurrentPlaybackState state = new AlexaIfEventItem.CurrentPlaybackState(token, 0L, "FINISHED");
                AlexaIfEventItem.Error error = new AlexaIfEventItem.Error(type.toString(), message);
                PlaybackFailedItem event = new PlaybackFailedItem(token, state, error);
                sendEvent(event);
            }
        }

        if(mHandler != null){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPlaybackList.clear();
                    AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                    if (queueManager.getForegroundChannel() == AlexaQueueManager.AlexaChannel.ContentChannel) {
                        queueManager.endContentsChannel();
                    }
                }
            },100);
        }
    }

    /**
     * 再生準備完了
     */
    @Override
    public long onPlaybackReady() {
        if (DBG) android.util.Log.d(TAG, "onPlaybackReady()");

        if(mPlaybackList != null && mPlaybackList.size() > 0){
            PlayItem item = mPlaybackList.get(0);

            if (item != null) {
                String token = item.audioItem.stream.token;
                mPreviousToken = token;
                sendPlaybackStartedEvent();
            }
        }
        return 0;
    }

    /**
     * 再生開始
     */
    @Override
    public void onPlaybackStarted() {
        if (DBG) android.util.Log.d(TAG, "onPlaybackStarted()");

        if (mAudioCallback != null) {
            mAudioCallback.onPlay(mCurrentItem);
        }

        if (mCurrentItem != null) {
            String token = mCurrentItem.audioItem.stream.token;

            StreamMetadataExtractedItem event = new StreamMetadataExtractedItem(token, null);
            sendEvent(event);
        }

        if (mProgressThread != null) {
            mProgressThread.cancel();
            mProgressThread = null;
        }
        mProgressThread = new ProgressThread();
        mProgressThread.start();

    }

    /**
     * バッファリング完了
     */
    @Override
    public void onBufferedFinish() {
        if (DBG) android.util.Log.d(TAG, "onBufferedFinish()");
    }

    /**
     * PlaybackStartedイベント、及び、PlaybackNearlyFinishedイベントを送信する
     */
    private void sendPlaybackStartedEvent() {
        if (mCurrentItem != null) {
            final String contentsToken = mPreviousToken;
            final long offsetInMilliseconds = mCurrentItem.audioItem.stream.offsetInMilliseconds;

            PlaybackStartedItem startedItem = new PlaybackStartedItem(contentsToken, offsetInMilliseconds);
            sendEvent(startedItem);

            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mPlayer!= null) {
                            PlaybackNearlyFinishedItem event = new PlaybackNearlyFinishedItem(contentsToken, offsetInMilliseconds);
                            sendEvent(event);
                        }
                    }
                }, 100);
            }
        }
    }

    /**
     * 一時停止
     */
    @Override
    public void onPaused() {
        if (DBG) android.util.Log.d(TAG, "onPaused()");
        PlayItem item = mCurrentItem;
        if(item == null){
            return;
        }
        if (mAudioCallback != null) {
            mAudioCallback.onPause(item);
        }
        String token = item.audioItem.stream.token;
        final PlaybackPausedItem event = new PlaybackPausedItem(token, mPlayer.getDuration());
        sendEvent(event);
    }

    @Override
    public void onAdjustVolume(float volume) {
        if (mAudioCallback != null) {
            mAudioCallback.onAdjustVolume(volume);
        }
    }

    @Override
    public void onSetVolume(float volume) {
        if (mAudioCallback != null) {
            mAudioCallback.onSetVolume(volume);
        }
    }

    @Override
    public void onSetMute(boolean isMute) {
        if (mAudioCallback != null) {
            mAudioCallback.onSetMute(isMute);
        }
    }

    @Override
    public void onNoResponse() {

    }

    /**
     * WebLinkのAudioFocusロス
     */
    @Override
    public void onWLAudioFocusLoss() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
        if (mAudioCallback != null) {
            // 再生画面を閉じる（×ボタン押下の動作に準拠する）
            mAudioCallback.onWLAudioFocusLoss();
        }
        // 再生位置取得スレッドを停止
        if (mProgressThread != null) {
            mProgressThread.cancel();
            mProgressThread = null;
        }
    }

    /**
     * 再生再開
     */
    @Override
    public void onResumed() {
        if (DBG) android.util.Log.d(TAG, "onResumed()");

        if (mAudioCallback != null) {
            mAudioCallback.onResume(mCurrentItem);
        }
    }

    /**
     * プログレス管理用スレッド
     * MediaPlayerを作り直すたびにこのスレッドも作り直す
     */
    private class ProgressThread extends Thread {

        boolean mIsCancel = false;

        public void cancel() {
            mIsCancel = true;
        }

        @Override
        public void run() {
            if (mCurrentItem != null) {
                runPlayItem(mCurrentItem);
            }
        }

        /**
         * 音楽再生系のプログレス管理処理
         * @param item
         */
        private void runPlayItem(PlayItem item) {
            boolean isRDM = false;
            boolean isRIM = false;
            long progressReportDelayInMilliseconds = 0;
            long progressReportIntervalInMilliseconds = 0;
            long prevRIM = 0;
            String token = null;

            if (mCurrentItem != null) {
                PlayItem playItem = mCurrentItem;
                AlexaIfDirectiveItem.AudioItem audioItem = playItem.audioItem;
                AlexaIfDirectiveItem.Stream stream = null;
                if (audioItem != null) {
                    stream = audioItem.stream;
                }
                AlexaIfDirectiveItem.ProgressReport progressReport = null;
                if (stream != null) {
                    progressReport = stream.progressReport;
                    token = stream.token;
                }
                if (progressReport != null) {
                    if (progressReport.progressReportDelayInMilliseconds != null) {
                        progressReportDelayInMilliseconds = progressReport.progressReportDelayInMilliseconds.longValue();
                        isRDM = true;
                    }
                    if (progressReport.progressReportIntervalInMilliseconds != null) {
                        progressReportIntervalInMilliseconds = progressReport.progressReportIntervalInMilliseconds.longValue();
                        isRIM = true;
                    }
                }
            }
            if (DBG) android.util.Log.d(TAG, " - ProgressReportDelayElapsed, isRDM = " + isRDM + ", milliseconds = " + progressReportDelayInMilliseconds);
            if (DBG) android.util.Log.d(TAG, " - ProgressReportDelayElapsed, isRIM = " + isRIM + ", milliseconds = " + progressReportIntervalInMilliseconds);

            while (mPlayer != null && mIsCancel == false) {

                int position = mPlayer.getCurrentPosition();
                // ProgressReportDelayElapsedなどを送信する

                if (isRDM && position > progressReportDelayInMilliseconds && !TextUtils.isEmpty(token)) {

                    ProgressReportDelayElapsedItem event = new ProgressReportDelayElapsedItem(token, position);
                    AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {
                        @Override
                        public void onExecute(Call call) {
                            if (DBG) android.util.Log.d(TAG, "ProgressReportDelayElapsed onExecute()");
                        }

                        @Override
                        public void onResponse(Call call, int httpCode) {
                            if (DBG) android.util.Log.d(TAG, "ProgressReportDelayElapsed onResponse()");

                            if (200 <= httpCode && httpCode < 300) {
                                // 成功
                                if (DBG) android.util.Log.d(TAG, " - ProgressReportDelayElapsed onResponse(), Success");
                            }
                            else {
                                // 失敗
                                if (DBG) android.util.Log.w(TAG, " - ProgressReportDelayElapsed onResponse(), Error");
                            }
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (DBG) android.util.Log.w(TAG, "ProgressReportDelayElapsed onFailure(), e = " + e);
                        }

                        @Override
                        public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                            if (DBG) android.util.Log.w(TAG, "ProgressReportDelayElapsed onParsedResponse(), itemList = " + itemList);
                        }
                    });
                    isRDM = false;      // １回のみ
                }
                if (isRIM && position > (progressReportIntervalInMilliseconds + prevRIM)) {
                    prevRIM = position;
                    ProgressReportIntervalElapsedItem event = new ProgressReportIntervalElapsedItem(token, position);
                    AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {
                        @Override
                        public void onExecute(Call call) {
                            if (DBG) android.util.Log.d(TAG, "ProgressReportIntervalElapsed onExecute()");
                        }

                        @Override
                        public void onResponse(Call call, int httpCode) {
                            if (DBG) android.util.Log.d(TAG, "ProgressReportIntervalElapsed onResponse()");

                            if (200 <= httpCode && httpCode < 300) {
                                // 成功
                                if (DBG) android.util.Log.d(TAG, " - ProgressReportIntervalElapsed onResponse(), Success");
                            }
                            else {
                                // 失敗
                                if (DBG) android.util.Log.w(TAG, " - ProgressReportIntervalElapsed onResponse(), Error");
                            }

                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (DBG) android.util.Log.w(TAG, "ProgressReportIntervalElapsed onFailure(), e = " + e);
                        }

                        @Override
                        public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                            if (DBG) android.util.Log.w(TAG, "ProgressReportIntervalElapsed onParsedResponse(), itemList = " + itemList);
                        }
                    });
                }

                // TODO:シークバーに通知するのであれば、ここ
                if (mAudioCallback != null) {
                    mAudioCallback.onUpdateProgress(item, position);
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 1曲再生完了
     */
    private void postComplete() {
        String log = mPlaybackList != null ? mPlaybackList.size() + "" : "-1000";
        if (DBG) android.util.Log.i(TAG, "postComplete(), mPlaybackList size = " + log);
        // 先にプレイリストを判定
        if (mPlaylistUrls.size() > (mPlaylistIndex + 1)) {
            // プレイリスト内の音楽URLが残っている
            mPlaylistIndex = mPlaylistIndex + 1;
            String nextUrl = mPlaylistUrls.get(mPlaylistIndex);
            createPlayer(nextUrl);
        }
        else {
            // プレイリストのすべて音楽を再生完了した

            if (mPlaybackList != null && mPlaybackList.size() > 0) {
                AlexaIfItem item = mPlaybackList.get(0);
                mPlaybackList.remove(item);

                if (DBG) android.util.Log.d(TAG, " - postComplete(), item         = " + item);
                if (DBG) android.util.Log.d(TAG, " - postComplete(), mCurrentItem = " + mCurrentItem);

                // 一時停止中判定のため、ここでmCurrentItemをnullにする
                mCurrentItem = null;
                mPlaylistUrls.clear();
                mPlaylistIndex = 0;

                if (mPlaybackList.size() > 0) {
                    PlayItem nextItem = mPlaybackList.get(0);
                    play(nextItem);
                }
                else {
                    // 再生する音楽が無い場合、コンテンツチャンネルを終了する
                    AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                    if (queueManager.getForegroundChannel() == AlexaQueueManager.AlexaChannel.ContentChannel) {
                        queueManager.endContentsChannel();
                    }
                }
            }
        }
    }
    /**
     * 再生停止
     */
    public void doStop() {
        if (mCurrentItem != null) {
            // イベントを通知
            String token = mCurrentItem.audioItem.stream.token;
            long offsetInMilliseconds = mPlayer.getCurrentPosition();
            PlaybackStoppedItem stopedEvent = new PlaybackStoppedItem(token, offsetInMilliseconds);
            sendEvent(stopedEvent);
        }
        releasePlayer();
        if (mProgressThread != null) {
            mProgressThread.cancel();
            mProgressThread = null;
        }
    }
}
