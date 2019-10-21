package jp.pioneer.mbg.alexa.manager;

/**
 * Created by esft-sakamori on 2017/08/23.
 */

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfEventItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeakerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.AdjustVolumeItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.SetMuteItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.SetVolumeItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechRecognizer.ExpectSpeechItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechSynthesizer.SpeakItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer.PlaybackNearlyFinishedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.SpeechRecognizer.ExpectSpeechTimedOutItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.SpeechSynthesizer.SpeechFinishedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.SpeechSynthesizer.SpeechStartedItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.manager.callback.IAudioCallback;
import jp.pioneer.mbg.alexa.player.AlexaPlayer;
import jp.pioneer.mbg.alexa.player.IAlexaPlayer;
import jp.pioneer.mbg.alexa.player.WLAlexaPlayer;
import jp.pioneer.mbg.alexa.util.LogUtil;
import okhttp3.Call;

/**
 * SpeakItem専用再生マネージャ
 */
public class AlexaSpeakManager implements IAlexaPlayer.PlaybackCallback {
    private final static String TAG = AlexaSpeakManager.class.getSimpleName();
    private static final boolean DBG = true;

    private static AlexaSpeakManager mAlexaSpeakManager = null;

    /**
     * SpeakディレクティブのQueue
     */
    private ArrayList<AlexaIfDirectiveItem> mPlaybackList = null;

    /**
     * プレーヤー
     */
    private IAlexaPlayer mPlayer = null;
    private Handler mHandler = null;

    /**
     * 再生中のSpeakディレクティブ
     */
    private SpeakItem mCurrentItem = null;

    /**
     * 直前に再生したSpeakディレクティブ
     */
    private SpeakItem mLastSpeckItem = null;

    private Context mContext = null;

    /**
     * 再生状態コールバックリスナー
     */
    private IAudioCallback mAudioCallback = null;
    public boolean mIsHasSpeakResponse = false;
    public boolean mIsStopExpectSpeech = false;
    /**
     * コンストラクタ
     */
    private AlexaSpeakManager() {
        mHandler = new Handler(Looper.getMainLooper());
        mPlaybackList = new ArrayList<>();
    }

    /**
     * マネージャインスタンス取得
     * @return
     */
    public static AlexaSpeakManager getInstance() {
        if (mAlexaSpeakManager == null) {
            mAlexaSpeakManager = new AlexaSpeakManager();
        }
        return mAlexaSpeakManager;
    }

    /**
     * マネージャインスタンスの破棄
     */
    public static void resetManager() {
        // 音声再生の停止
        mAlexaSpeakManager.releaseSpeechPlayer();
        mAlexaSpeakManager.mPlayer = null;
        // Speakディレクティブのリストをクリア
        mAlexaSpeakManager.mPlaybackList.clear();
        // 再生中アイテムを削除
        mAlexaSpeakManager.mCurrentItem = null;
        // 前回再生アイテムを削除
        mAlexaSpeakManager.mLastSpeckItem = null;
        // コンテキストを削除
        mAlexaSpeakManager.mContext = null;
        // コールバックインスタンスを削除
        mAlexaSpeakManager.mAudioCallback = null;
        // イベントレスポンスの有無の判定フラグを初期化
        mAlexaSpeakManager.mIsHasSpeakResponse = false;
    }

    /**
     * コンテキスト
     * @param context
     */
    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * コンテキスト
     * @param context
     */
    public void createAlexaPlayer(Context context) {
        setContext(context);
        if (false/*DebugManager.isBoolean(DebugManager.DebugSettingId.DEBUG_SETTING_ID_WEBLINK_AUDIO_SPEAK)*/) {
            mAlexaSpeakManager.setAlexaPlayer(new WLAlexaPlayer(context));
        }
        else {
            mAlexaSpeakManager.setAlexaPlayer(new AlexaPlayer(context, null));
        }
    }

    /**
     * プレーヤーを設定
     * @param player
     */
    private void setAlexaPlayer(IAlexaPlayer player) {
        this.mPlayer = player;
        this.mPlayer.setCallback(this);
    }

    /**
     * プレーヤーインスタンスを取得
     * @return
     */
    public IAlexaPlayer getAlexaPlayer() {
        return this.mPlayer;
    }

    /**
     * 音楽再生状態コールバックリスナーを設定
     * @param callback
     */
    public void setIAudioCallback(IAudioCallback callback) {
        this.mAudioCallback = callback;
    }

    /**
     * SpeakディレクティブのQueueを取得
     * @return
     */
    public ArrayList<AlexaIfDirectiveItem> getPlaybackList() {
        return mPlaybackList;
    }

    /**
     * 直前に再生したSpeakディレクティブを取得
     * @return
     */
    public SpeakItem getLastSpeckItem() {
        return mLastSpeckItem;
    }

    /**
     * ユーザー操作で一時停止
     */
    public void doPause() {
        if (DBG) android.util.Log.d(TAG, "doPause() : ", new Throwable());
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    /**
     * ディレクティブ振り分け
     * @param item
     */
    public void post(AlexaIfDirectiveItem item) {
        if(DBG)android.util.Log.d(TAG, "post() 1");

        ArrayList<AlexaIfDirectiveItem> itemList = new ArrayList<>();
        itemList.add(item);
        this.post(itemList);
    }

    /**
     * ディレクティブ振り分け
     *
     * @param itemList
     */
    public void post(final ArrayList<AlexaIfDirectiveItem> itemList) {
        if(DBG)android.util.Log.d(TAG, "post() 2");
        boolean canPlay = false;
        if (itemList != null && itemList.size() > 0) {
            if (mPlaybackList == null) {
                // 初回取得時はそのまま再生を行う.
                mPlaybackList = itemList;
                canPlay = true;
            } else {
                for (AlexaIfDirectiveItem item : itemList) {
                    // 既にリストがある場合は既存のリストに追加する.
                    if(item instanceof SpeakItem){
                       if(mPlaybackList.size() == 1) {
                           //TODO:一度に複数のSpeakDirectiveを受信することもあるらしい
                           //SpeakDirectiveのDialogReqestIDが異なる場合、最初のSpeakを消す
                           //同じDialogReqestIDの場合は消さない
                           AlexaIfDirectiveItem playlist = mPlaybackList.get(0);
                           String requestId = playlist.getHeader().getDialogRequestId();

                           if(requestId != null && !requestId.equals(item.getDialogRequestId())){
                              mPlaybackList.clear();
                           }
                           else if(requestId == null){
                               mPlaybackList.clear();
                           }
                       }
                    }
                    mPlaybackList.add(item);
                }
                // 現在停止中で、既存のリストの再生が完了している場合は再生する.
                 canPlay = (mPlaybackList.get(0).equals(mCurrentItem) == false);
            }
            if (DBG) android.util.Log.d(TAG, " - post() canPlay = " + canPlay);
            if (canPlay) {
                AlexaIfDirectiveItem item = mPlaybackList.get(0);
                if(mIsStopExpectSpeech){
                    mPlaybackList.clear();
                    return;
                }
                if (DBG) android.util.Log.d(TAG, " - post() play item = " + item);
                if (item instanceof SpeakItem) {
                    // チャンネルをダイアログに更新
                    AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                    queueManager.startDialogChannel();
                    queueManager.setAllocateDirectiveFlag(true);
                    play((SpeakItem) item);
                }
                else if (item instanceof ExpectSpeechItem) {
                    mPlaybackList.remove(item);
                    AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                    queueManager.doPostExpectSpeechDirective((ExpectSpeechItem) item);
                }
            }
        }

    }

    /**
     * Speak再生系機能で、再生開始以外のDirectiveを処理する.
     * ・SetMuteItem
     * ・SetVolumeItem
     * ・AdjustVolumeItem
     * @param item
     */
    public void requestEvent(AlexaIfDirectiveItem item) {
        if (DBG) android.util.Log.d(TAG, "requestEvent()");

        if (item != null) {
            if (DBG) android.util.Log.d(TAG, " - requestEvent() item = " + item);
            boolean isEventSend = false;
            AlexaIfEventItem event = null;

            if (item instanceof AlexaIfSpeakerItem) {
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
//                        mPlayer.setVolume(volume);
//                        event = new MuteChangedItem(volume, mPlayer.isMute());
//                        event = new VolumeChangedItem(volume, mPlayer.isMute());
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
     * SpeakItem用再生処理
     * @param item
     */
    private void play(final SpeakItem item) {
        if (DBG) android.util.Log.d(TAG, "play() 1");
        byte[] audioContent = item.getAudioContent();
        int size = audioContent.length;
        if(size < 500 ){
            final SpeechStartedItem startedItem = new SpeechStartedItem(item.token);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    AlexaEventManager.sendEvent(TokenManager.getToken(), startedItem, mContext, new AlexaEventManager.AlexaCallback() {

                        @Override
                        public void onExecute(Call call) {
                            if (DBG) android.util.Log.d(TAG, startedItem.getName() + " onExecute()");
                        }

                        @Override
                        public void onResponse(Call call, int httpCode) {
                            if (DBG) android.util.Log.d(TAG, startedItem.getName() + " onResponse()");

                            if (200 <= httpCode && httpCode < 300) {
                                // 成功
                                if (DBG) android.util.Log.d(TAG, " - " + startedItem.getName() + " onResponse(), Success");
                                SpeechFinishedItem finishedItem = new SpeechFinishedItem(item.token);
                                sendEvent(finishedItem);
                            }
                            else {
                                // 失敗
                                if (DBG) android.util.Log.w(TAG, " - " + startedItem.getName() + " onResponse(), Error");
                            }
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (DBG) android.util.Log.w(TAG, startedItem.getName() + " onFailure(), e = " + e);
                        }

                        @Override
                        public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                            if (DBG) android.util.Log.w(TAG, startedItem.getName() + " onParsedResponse(), itemList = " + itemList);
                        }
                    });
                }
            });
            t.start();
        }else if(item != null){
            mCurrentItem = item;

            FileInputStream fis = null;
            if (item.getAudioContent() != null) {
                //byte[] audioContent = item.getAudioContent();
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
                if (mPlayer.isPlaying() == true) {
                    mPlayer.stopPlayer();
                }
                // AndroidOSのAudioFocusを取得
                AmazonAlexaManager.getInstance().requestAudioFocus();
                try {
                    mPlayer.createPlayer(fis.getFD(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (mAudioCallback != null) {
                        mAudioCallback.onError(mCurrentItem);
                    }
                }
                // GalaxyS5(Android6.0.1)ではSecurityExceptionが発生するためcatch
                catch (SecurityException e) {
                    e.printStackTrace();
                    if (mAudioCallback != null) {
                        mAudioCallback.onError(mCurrentItem);
                    }
                }
            } else {
                LogUtil.i(TAG, "no playable Voice file, we gonna play streaming audio.");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        postComplete();
                        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                        queueManager.setAllocateDirectiveFlag(false);

                        // TODO: 暫定 再生データが無い場合はエラーだとは思うが、一応完了通知を投げてみる。
                        SpeechFinishedItem event = new SpeechFinishedItem(item.token);
                        sendEvent(event);
                    }
                });

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

    /*
    * Event送信
    * */
    private synchronized void sendEvent(final AlexaIfEventItem event) {
        if (DBG) android.util.Log.d(TAG, "sendEvent()");
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
                            if (DBG) android.util.Log.w(TAG, " - " + event.getName() + " onResponse(), Error");
                        }
                        if(event.getName().equals("SpeechFinished")) {
                            if (httpCode == 204) {
                                if (mAudioCallback != null) {
                                    mAudioCallback.onNoResponse();
                                }
                                mIsHasSpeakResponse = false;
                            }
                            else if(httpCode == 200){
                                mIsHasSpeakResponse = true;
                            }
                            else{
                                mIsHasSpeakResponse = false;
                            }
                            postComplete();
                            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                            queueManager.setAllocateDirectiveFlag(false);
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (DBG) android.util.Log.w(TAG, event.getName() + " onFailure(), e = " + e);
                    }

                    @Override
                    public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                        if (DBG) android.util.Log.w(TAG, event.getName() + " onParsedResponse(), itemList = " + itemList);
                    }
                });
        }
        });
        t.start();

    }

    /* -- IAlexaPlayer#PlaybackCallback -- */

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

            // 終了通知の送信.
            if (mCurrentItem != null) {
                String token = mCurrentItem.token;
                SpeechFinishedItem event = new SpeechFinishedItem(token);
                sendEvent(event);
            }

            // 再生アイテムを削除する.

            size = mPlaybackList.size();
            //TODO:
            //mPlaybackList.clear();
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
        return 0;
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
//        return true;
        boolean isPlay = false;
        //Speakは再生優先
        if (mAudioCallback != null) {
            if (mAudioCallback.isAlexaPlayable()) {
                //音楽再生させる
                isPlay = true;
            }
            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            if(queueManager.isRec()){
                isPlay = false;
            }
        }
        return isPlay;
    }

    /**
     * エラー
     */
    @Override
    public void onError(IAlexaPlayer.PlaybackErrorType type, String message) {
        if (DBG) android.util.Log.d(TAG, "onError()", new Throwable());

        if (mAudioCallback != null) {
            mAudioCallback.onError(mCurrentItem);
        }

        if(mPlayer != null){
            mPlayer.releasePlayer();
        }
        if(mPlaybackList != null && mPlaybackList.size() > 0) {
            // エラー発生のタイミングでもQueueのブロックを解除
            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            queueManager.setAllocateDirectiveFlag(false);
        }

        if(mHandler != null){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPlaybackList.clear();
                    AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                    switch (queueManager.isDialogChannelFinish()) {
                        case (AlexaQueueManager.FINISH_STATE_FINISH):
                            // finishする場合
                            queueManager.endDialogChannel();
                            break;
                        case (AlexaQueueManager.FINISH_STATE_COMPLETION):
                            // onCompletionを呼ぶ場合
                            AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
                            speakManager.onCompletion();
                            break;
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

        long offsetInMilliseconds = 0;

        if(mPlaybackList != null && mPlaybackList.size() > 0){
            AlexaIfDirectiveItem item = mPlaybackList.get(0);
            if (item instanceof SpeakItem) {
                String token = ((SpeakItem) item).token;
                SpeechStartedItem event = new SpeechStartedItem(token);
                sendEvent(event);

                mLastSpeckItem = (SpeakItem) item;
            }

        }

        return offsetInMilliseconds;
    }
    /*
    * Speech中のSpeech再生停止処理
    * */
    public void speechStop() {
        if(mPlayer != null) {
            if (mPlayer.isPlaying() == true) {
                //Speech再生とめる
                mPlayer.pause();
            }
        }
    }

    /**
     * プレーヤーを破棄する
     */
    public void releaseSpeechPlayer() {
        if (mPlayer != null) {
            mPlayer.releasePlayer();
        }
    }

    /*
    * 停止されていたSpeech再生復帰処理
    * */
    public void speechStart() {
            if (mPlaybackList != null && mPlaybackList.size() > 0) {
                    AlexaIfDirectiveItem item = mPlaybackList.get(0);
                    if (item instanceof SpeakItem) {
//                        //tuneInのspeech再生中に他のspeechが割り込んできた場合
//                        HandlerThread handlerThread = new HandlerThread("foo");
//                        handlerThread.start();
//                        new Handler(handlerThread.getLooper()).postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                //tuneInのspeechと他のspeechを入れ替えるので
//                                //再生再開時点で2秒遅らせないと、TuneinのSpeechが一瞬流れてしまう
//                                if(mPlayer.getPlaybackState() != AlexaPlayer.PlaybackState.FINISHED){
//
//                                    mPlayer.speechStart();
//                                }
//                            }//1秒だと駄目
//                        }, 2000);       // TODO:2秒固定は避けるべき
//
//                        String token = ((SpeakItem) item).token;
//                        SpeechStartedItem event = new SpeechStartedItem(token);
//                        sendEvent(event);
//
//                        mLastSpeckItem = (SpeakItem) item;
                        this.releaseSpeechPlayer();
                        this.onCompletion();
                    }
            }
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
    }

    /**
     * バッファリング完了
     */
    @Override
    public void onBufferedFinish() {
        if (DBG) android.util.Log.d(TAG, "onBufferedFinish()");
        if(mCurrentItem != null){

            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentItem != null) {
                            String token = mCurrentItem.token;
                            PlaybackNearlyFinishedItem event = new PlaybackNearlyFinishedItem(token, 0);
                            sendEvent(event);
                        }
                    }
                }, 100);
            }
        }
    }

    /*
    * 再生停止
    * */
    @Override
    public void onPaused() {
        if (DBG) android.util.Log.d(TAG, "onPaused()");

        if (mAudioCallback != null) {
            mAudioCallback.onPause(mCurrentItem);
        }
    }

    @Override
    public void onAdjustVolume(float volume) {

    }

    @Override
    public void onSetVolume(float volume) {

    }

    @Override
    public void onSetMute(boolean isMute) {

    }

    @Override
    public void onNoResponse() {

    }

    @Override
    public void onWLAudioFocusLoss() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 対話モデル下位のアラート機能へも伝える
                AlexaAlertManager.getInstance().onWLAudioFocusLoss();
                // 画面への通知処理は、最下位のAlexaAudioManagerが行う
            }
        },0);
    }

    /*
    * 再生再開
    * */
    @Override
    public void onResumed() {
        if (DBG) android.util.Log.d(TAG, "onResumed()");

        if (mAudioCallback != null) {
            mAudioCallback.onResume(mCurrentItem);
        }
    }

    /**
     * Speakの再生完了
     */
    private void postComplete() {
        if (mPlaybackList != null && mPlaybackList.size() > 0) {
            AlexaIfDirectiveItem item = mPlaybackList.get(0);
            mPlaybackList.remove(item);
            if (mPlaybackList.size() > 0) {
                AlexaIfDirectiveItem nextItem = mPlaybackList.get(0);
                if (nextItem instanceof SpeakItem) {
                    play((SpeakItem) nextItem);
                }
                else if (nextItem instanceof ExpectSpeechItem) {
                    mPlaybackList.remove(nextItem);
                    AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                    queueManager.doPostExpectSpeechDirective((ExpectSpeechItem) nextItem);
                }
            }
            else {//次のSpeakがない
                if(!mIsHasSpeakResponse) {
                    AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                    switch (queueManager.isDialogChannelFinish()) {
                        case (AlexaQueueManager.FINISH_STATE_FINISH):
                            // finishする場合
                            queueManager.endDialogChannel();
                            break;
                        case (AlexaQueueManager.FINISH_STATE_COMPLETION):
                            // onCompletionを呼ぶ場合
                            AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
                            speakManager.onCompletion();
                            break;
                    }
                }
            }
        }
    }

    /**
     * Speakのキャンセル
     */
    public void onSpeechCancel() {
        if (mPlaybackList != null && mPlaybackList.size() > 0) {
            this.speechStop();
            mPlaybackList.clear();
        }
    }
    public void stopExpectSpeech(){
        if (DBG) android.util.Log.d(TAG, "stopExpectSpeech()");
        ExpectSpeechTimedOutItem event = new ExpectSpeechTimedOutItem();
        sendEvent(event);
        mIsStopExpectSpeech = true;
        onSpeechCancel();
        //mIsHasSpeakResponse= false;
        //AlexaQueueManager.endDialogChannel();
    }
}
