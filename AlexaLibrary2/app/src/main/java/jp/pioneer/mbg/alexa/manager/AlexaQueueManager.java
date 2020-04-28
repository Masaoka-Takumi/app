package jp.pioneer.mbg.alexa.manager;

/**
 * Created by esft-sakamori on 2017/08/18.
 */

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Alerts.DeleteAlertItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Alerts.SetAlertItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfAlertsItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfAudioPlayerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeechRecognizerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeechSynthesizerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.ClearQueueItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.PlayItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.StopItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Navigation.SetDestinationItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Notifications.ClearIndicatorItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Notifications.SetIndicatorItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.AdjustVolumeItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.SetMuteItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.SetVolumeItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechRecognizer.ExpectSpeechItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechRecognizer.StopCaptureItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechSynthesizer.SpeakItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.System.ReportSoftwareInfoItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.System.ResetUserInactivityItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.System.SetEndpointItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderTemplateItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;


/**
 * AlexaVoiceServiceからのDirectiveをQueue構造で管理するマネージャ.
 * ------------------------------------------------------------------
 */
public class AlexaQueueManager {
    private static final String TAG = AlexaQueueManager.class.getSimpleName();
    private static final boolean DBG = false;

    /**
     * シングルトンインスタンス
     */
    private static final AlexaQueueManager mInstance = new AlexaQueueManager();

    /**
     * 音声入力中判定フラグ
     */
    private boolean isRec = false;

    /**
     * ディレクティブ振り分けコールバックリスナー
     */
    private AlexaQueueCallback mAlexaQueueCallback = null;
    /**
     * チャネル更新コールバックリスナー
     */
    private AlexaChannelChangeListener mAlexaChannelChangeListener = null;

    /**
     * ディレクティブ振り分け一時停止フラグ
     */
    private boolean isAllocateDirective = false;
    /**
     * 振り分け一時停止機能付きスレッド
     */
    private BlockAbleThread mBlockThread = null;
    /**
     * 振り分け一時停止機能なしスレッド
     */
    private UnBlockAbleThread mUnBlockThread = null;

    /**
     * キャンセルしたRecognizeイベントのdialogRequestId
     */
    private String mCanceledRecognizeID = null;

    /**
     * finishStateの値
     */
    public static final int FINISH_STATE_NOT_FINISH = 0;
    public static final int FINISH_STATE_FINISH     = 1;
    public static final int FINISH_STATE_COMPLETION = 2;

    /**
     * チャネル
     */
    public enum AlexaChannel {
        DialogChannel("DialogChannel"),
        AlertChannel("AlertChannel"),
        ContentChannel("ContentChannel"),
        NonChannel("NonChannel");

        AlexaChannel(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        };
        public String name = null;
    }

    /**
     * コンストラクタ
     */
    private AlexaQueueManager() {
    }

    /**
     * インスタンス取得
     * @return
     */
    public static AlexaQueueManager getInstance() {
        return mInstance;
    }

    /**
     * チャネル更新コールバックリスナー
     */
    public interface AlexaChannelChangeListener {
        /**
         * チャンネルのアクティブ／非アクティブに変化があった際に通知
         * @param channel
         * @param isActive
         */
        public void onChannelActiveChange(AlexaChannel channel, boolean isActive);
    }

    /**
     * Directiveの処理を行うクラスに通知を行うインターフェース
     */
    public interface AlexaQueueCallback {
        void onPost(AlexaIfDirectiveItem directive);
    }

    /**
     * チャンネル変更リスナーを設定
     * @param listener
     */
    public void setAlexaChannelChangeListener(AlexaChannelChangeListener listener) {
        mAlexaChannelChangeListener = listener;
    }

    /**
     * コールバックインターフェースを設定
     * @param callback
     */
    public void setAlexaQueueCallback(AlexaQueueCallback callback) {
        mAlexaQueueCallback = callback;
    }

    /**
     * 音声認識状態 取得
     * @return
     */
    public boolean isRec() {
        return isRec;
    }

    /**
     * ブロック機能付きスレッドの停止／再開 設定
     * @param flag
     */
    public synchronized void setAllocateDirectiveFlag(boolean flag) {
        isAllocateDirective = flag;
    }

    /**
     * 音声入力を開始する際に他の機能へ通知する
     */
    public synchronized void startRecognize() {
        startDialogChannel();
        isRec = true;
        AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
        speakManager.speechStop();
        AlexaNotificationManager notificationManager = AlexaNotificationManager.getInstance();
        notificationManager.onStartRecognize();
    }

    /**
     * 音声入力を終了した際に他の機能へ通知する
     */
    public synchronized void endRecognize() {
        if(DBG)android.util.Log.d(TAG, " - endRecgnize",new Throwable());//
        isRec = false;

        AlexaNotificationManager notificationManager = AlexaNotificationManager.getInstance();
        notificationManager.onEndRecognize();
    }

    /**
     * キャンセルするdialogRequestIdを設定
     * @param dialogRequestId
     */
    public synchronized void cancelRecognizeEvent(String dialogRequestId) {
        if(DBG)android.util.Log.d(TAG, "cancelRecognizeEvent(), dialogRequestId = " + dialogRequestId);
        mCanceledRecognizeID = dialogRequestId;
    }

    /**
     * ダイアログチャネルのアクティブ化通知
     */
    public void startDialogChannel() {
        if(DBG)android.util.Log.d(TAG, " - startDialogChannel()");
        onChangeChannelActive(AlexaChannel.DialogChannel, true);
    }

    /**
     * ダイアログチャネルを終了しても良いか判定する
     * @return
     */
    public synchronized int isDialogChannelFinish() {
        int finishState = FINISH_STATE_NOT_FINISH;
        boolean isHasDialogChannelItem = false;
        if (mBlockThread != null) {
            // 振り分け前のディレクティブをチェック
            isHasDialogChannelItem = mBlockThread.hasDialogChannelDirective();
        }

        if (isRec) {
            // 音声入力状態
            //   --> Dialogチャネルを維持
            finishState = FINISH_STATE_NOT_FINISH;
        }
        else if(isActiveDialogChannel()) {
            // Speakディレクティブがスタック、又は、再生中。
            //   --> Dialogチャネルを維持
            finishState = FINISH_STATE_COMPLETION;
        }
        else if (isHasDialogChannelItem) {
            // 振り分け待ちのDialogチャネル用ディレクティブがある
            //   --> Dialogチャネルを維持
            finishState = FINISH_STATE_NOT_FINISH;
        }
        else {
            // DownChannelを非アクティブにする
            finishState = FINISH_STATE_FINISH;
        }
        return finishState;
    }

    /**
     * ダイアログチャネルの非アクティブ化通知
     */
    public void endDialogChannel() {
        if(DBG)android.util.Log.d(TAG, " - endDialogChannel() ", new Throwable());
        onChangeChannelActive(AlexaChannel.DialogChannel, false);
    }

    /**
     * アラートチャネルのアクティブ化通知
     */
    public void startAlertChannel() {
        if (DBG) android.util.Log.d(TAG, " - startAlertChannel()");
        onChangeChannelActive(AlexaChannel.AlertChannel, true);
    }

    /**
     * アラートチャネルの非アクティブ化通知
     */
    public void endAlertChannel() {
        if(DBG)android.util.Log.d(TAG, " - endAlertChannel()");
        onChangeChannelActive(AlexaChannel.AlertChannel, false);
    }
    /*
    * コンテンツチャンネルのアクティブ化通知
    * */
    public void startContentsChannel() {
        if (DBG) android.util.Log.d(TAG, " - startContentsChannel()");
        onChangeChannelActive(AlexaChannel.ContentChannel, true);
    }
    /*
    * コンテンツチャンネルの非アクティブ化通知
    * */
    public void endContentsChannel() {
        if (DBG) android.util.Log.d(TAG, " - endContentsChannel()");
        onChangeChannelActive(AlexaChannel.ContentChannel, false);
    }

    /**
     * チャンネルのアクティブ／非アクティブに変化があった
     * @param channel
     * @param isActive
     */
    private synchronized void onChangeChannelActive(final AlexaChannel channel, final boolean isActive) {

        if (channel == AlexaChannel.DialogChannel) {
            // Dialogチャネルが変化
            if (isActive) {
                if (isActiveAlertsChannel()) {
                    // Alertsチャネルがアクティブ
                    AlexaAlertManager alertManager = AlexaAlertManager.getInstance();
                    alertManager.onAlertChannelBackground();
                    alertManager.pause();
                }
                else if (isActiveContentChannel()) {
                    // Contentチャネルがアクティブ
                    AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
                    audioManager.onBackgroundContentsChannel();
                }
            }
            else {
                if (isActiveAlertsChannel()) {
                    // Alertsチャネルがアクティブ
                    AlexaAlertManager alertManager = AlexaAlertManager.getInstance();
                    alertManager.onAlertChannelForeground();
                    alertManager.resume();
                }
                else if (isActiveContentChannel()) {
                    // Contentチャネルがアクティブ
                    AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
                    audioManager.onForegroundContentsChannel();
                }
            }
        }
        else if (channel == AlexaChannel.AlertChannel) {
            // Alertsチャネルが変化
            if (isActiveDialogChannel()) {
                // Dialogチャネルがアクティブ
                // --> Contentチャネルは、すでにバックグラウンド中のため通知しない
            }
            else {
                if (isActive) {
                    if (isActiveContentChannel()) {
                        // Contentチャネルがアクティブ
                        AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
                        audioManager.onBackgroundContentsChannel();
                    }
                } else {
                    if (isActiveContentChannel()) {
                        // Contentチャネルがアクティブ
                        AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
                        audioManager.onForegroundContentsChannel();
                    }
                }
            }
        }
        else if (channel == AlexaChannel.ContentChannel) {
            // Contentsチャネルが変化
            //  --> 優先度下位のチャネルが無いため、通知しない
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mAlexaChannelChangeListener != null) {
                    // リスナー経由でUI側に通知する
                    mAlexaChannelChangeListener.onChannelActiveChange(channel, isActive);
                }
            }
        });
    }

    /**
     * 最も優先度の高いアクティブなチャネルを取得
     * @return
     */
    public synchronized AlexaChannel getForegroundChannel() {
        if (isActiveDialogChannel()) {
            // Dialogチャネルがアクティブ
            return AlexaChannel.DialogChannel;
        }
        else if (isActiveAlertsChannel()) {
            // Alertsチャネルがアクティブ
            return AlexaChannel.AlertChannel;
        }
        else if (isActiveContentChannel()) {
            // Contentチャネルがアクティブ
            return AlexaChannel.ContentChannel;
        }
        // どのチャネルもアクティブでない
        return AlexaChannel.NonChannel;
    }

    /**
     * Dialogチャネルのアクティブ状態判定
     * @return
     */
    public synchronized boolean isActiveDialogChannel() {
        boolean result = false;

        if (isRec) {
            // 音声入力中
            result = true;
        }
        else {
            final AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
            final ArrayList<AlexaIfDirectiveItem> playbackList = speakManager.getPlaybackList();
            if (playbackList != null && !playbackList.isEmpty()) {
                // Speakディレクティブスタック中、又は、再生中
                result = true;
            }
        }

        return result;
    }

    /**
     * Alertsチャネルのアクティブ状態判定
     * @return
     */
    public synchronized boolean isActiveAlertsChannel() {
        boolean result = false;
        final AlexaAlertManager alertManager = AlexaAlertManager.getInstance();
        final List<SetAlertItem> activeAlertList = alertManager.getActiveAlertList();
        if (activeAlertList != null && !activeAlertList.isEmpty()) {
            result = true;
        }

        return result;
    }

    /**
     * Contentチャネルのアクティブ状態判定
     * @return
     */
    public synchronized boolean isActiveContentChannel() {
        boolean result = false;
        AlexaAudioManager audioManager = AlexaAudioManager.getInstance();

        List<PlayItem> playItemList = audioManager.getPlaybackList();
//        if (playItemList != null && !playItemList.isEmpty() && audioManager.getCurrentPlayItem() != null) {
        if (playItemList != null && !playItemList.isEmpty()) {      // 振り分け済みのPlayディレクティブが合ったらアクティブ
            result = true;
        }
        return result;
    }

    /**
     * 受信したディレクティブを振り分ける
     * @param list
     * @return
     */
    public synchronized void post(List<AlexaIfDirectiveItem> list) {
        if (DBG) android.util.Log.d(TAG, "post()");
        // dialogRequestIdがあるディレクティブのリスト
        ArrayList<AlexaIfDirectiveItem> blockAbleList = new ArrayList<>();
        // dialogRequestIdがないディレクティブのリスト
        ArrayList<AlexaIfDirectiveItem> unBlockAbleList = new ArrayList<>();

        // Dialogチャネルで処理するディレクティブの有無
        boolean hasDialogChannelDirective = false;

        if (list != null && list.size() > 0) {
            for (AlexaIfItem item : list) {
                String id = item.getDialogRequestId();
                if (mCanceledRecognizeID != null && mCanceledRecognizeID.equals(id)) {
                    if (DBG) android.util.Log.d(TAG, " - post(), CanceledRecognizeID is Match. item = " + item);
                    // キャンセルしたRecognizeイベントのdialogRequestIdと一致
                    // -> キャンセルしたイベントのレスポンスのため、破棄。
                }
                else if (id == null||item instanceof RenderTemplateItem) {
                    //通常系ディスプレイカードの通知は発話終了を待たない
                    if (DBG) android.util.Log.d(TAG, " - post(), Add unBlockAbleList. item = " + item);
                    unBlockAbleList.add((AlexaIfDirectiveItem) item);
                } else {
                    if (DBG) android.util.Log.d(TAG, " - post(), Add blockAbleList. item = " + item);
                    blockAbleList.add((AlexaIfDirectiveItem) item);
                    if (!hasDialogChannelDirective) {
                        if (item instanceof AlexaIfSpeechRecognizerItem) {
                            // SpeechRecognizerインタフェース
                            hasDialogChannelDirective = true;
                        } else if (item instanceof AlexaIfSpeechSynthesizerItem) {
                            // SpeechSynthesizerインタフェース
                            hasDialogChannelDirective = true;
                        }
                    }
                }
            }
        }
        boolean isCancelDialogChannel = postBlockQueue(blockAbleList);
        postUnBlockQueue(unBlockAbleList);

        if (isCancelDialogChannel) {
            boolean isKeepDialogChannel = false;
            if (isRec || hasDialogChannelDirective) {
                // 音声入力中 or Dialogチャネルのディレクティブ受信
                isKeepDialogChannel = true;
            }
            // Dialogチャネルを維持する要素がない場合、Dialogチャネル非アクティブを通知する。
            if (!isKeepDialogChannel) {
                endDialogChannel();
            }
        }

    }

    /**
     * Recognizeイベントのレスポンスにディレクティブが無かったことを通知
     */
    public void onRecognizeNoResponse() {
        if(DBG)android.util.Log.d(TAG, "onRecognizeNoResponse()");
        switch (isDialogChannelFinish()) {
            case (FINISH_STATE_FINISH):
                // finishする場合
                endDialogChannel();
                break;
            case (FINISH_STATE_COMPLETION):
                // onCompletionを呼ぶ場合
                AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
                speakManager.onCompletion();
                break;
        }

        //問題点リストNo21の対応
        //Responseがなかったらアイコンを小さくする
        AmazonAlexaManager amazonAlexaManager = AmazonAlexaManager.getInstance();
        amazonAlexaManager.noResponse();
    }

    /**
     * 一旦AlexaSpeakManagerに振り分けたExpectSpeechItemを
     * @param item
     */
    public void doPostExpectSpeechDirective(ExpectSpeechItem item) {
        if (mAlexaQueueCallback != null) {
            mAlexaQueueCallback.onPost(item);
        }
    }

    /**
     * 一時停止機能付きスレッドを開始
     * @param list
     */
    private synchronized boolean postBlockQueue(final ArrayList<AlexaIfDirectiveItem> list) {
        if (DBG) android.util.Log.d(TAG, "postBlockQueue()");

        boolean isCancelDialogChannel = false;

        if (list.size() > 0) {
            if (mBlockThread != null) {
                mBlockThread.clearDirectiveList();
            }
            AlexaIfDirectiveItem item = list.get(0);
            String runningRecognizeID = item.getDialogRequestId();
            AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
            ArrayList<AlexaIfDirectiveItem> speakPlaybackList = null;

            speakPlaybackList = speakManager.getPlaybackList();

            if (speakPlaybackList != null && speakPlaybackList.size() > 0) {
                AlexaIfDirectiveItem speakItem = speakPlaybackList.get(0);
                if (runningRecognizeID.equals(speakItem.getDialogRequestId())) {
                    // 現在処理中のSpeakディレクティブとdialogRequestIdが同じ
                    // -> キャンセルしない
                    isCancelDialogChannel = false;
                } else {
                    // 再生中のSpeakディレクティブをキャンセル
                    speakManager.releaseSpeechPlayer();
                    isAllocateDirective = false;
                    isCancelDialogChannel = true;
                }
            } else {
                // このケースではダイアログチャネルではないけど、キャンセル処理に通す
                isCancelDialogChannel = true;
            }
            if (mBlockThread == null) {
                mBlockThread = new BlockAbleThread();
                mBlockThread.addDirectiveList(list);
                mBlockThread.start();
            } else {
                mBlockThread.addDirectiveList(list);
                mBlockThread.notifyLockObject();
            }
        }
        return isCancelDialogChannel;
    }

    /**
     * Androidの音声着信など、AVS以外のトリガーによってBlockAbleThreadをキャンセルする場合
     */
    public synchronized void doCancelBlockAbleQueue() {
        AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
        // 再生中のSpeakディレクティブをキャンセル
        speakManager.onSpeechCancel();

        if (mBlockThread != null) {
            mBlockThread.clearDirectiveList();
        }
        // ブロックフラグを初期化
        isAllocateDirective = false;

        switch (isDialogChannelFinish()) {
            case (FINISH_STATE_FINISH):
                // finishする場合
                endDialogChannel();
                break;
            case (FINISH_STATE_COMPLETION):
                // onCompletionを呼ぶ場合
                speakManager.onCompletion();
                break;
        }
    }

    /**
     * 車載機切断
     */
    public synchronized void onAvsDisconnect() {
        if (mBlockThread != null) {
            // ブロック機能ありスレッドを終了
            mBlockThread.finish();
            mBlockThread.notifyLockObject();
            mBlockThread = null;
        }
        if (mUnBlockThread != null) {
            // ブロック機能なしスレッドを終了
            mUnBlockThread.finish();
            mUnBlockThread.notifyLockObject();
            mUnBlockThread = null;
        }
    }

    /**
     * 一時停止機能付きスレッド
     */
    private class BlockAbleThread extends Thread {

        private final Object mLockObject = new Object();

        private boolean isFinish = false;
        private final ArrayList<AlexaIfDirectiveItem> mDirectiveList = new ArrayList<AlexaIfDirectiveItem>();

        public BlockAbleThread() {
        }

        public synchronized void addDirectiveList(ArrayList<AlexaIfDirectiveItem> directiveList) {
            mDirectiveList.addAll(directiveList);
        }

        public synchronized void clearDirectiveList() {
            mDirectiveList.clear();
        }

        public void notifyLockObject() {
            synchronized (mLockObject) {
                mLockObject.notify();
            }
        }
        public void finish() {
            isFinish = true;
        }
        @Override
        public void run() {
            if (DBG) android.util.Log.i(TAG, "****** checkQueue()"+mDirectiveList);

            while (!isFinish) {
                if (mDirectiveList.size() == 0) {
                    // 待機処理
                    synchronized (mLockObject) {
                        try {
                            mLockObject.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (isFinish) {
                    break;
                }
                //フラグがオフだったら、振り分け処理をしない
                //再生処理が終了したら振り分け再開
                while (isAllocateDirective) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(isFinish){
                    break;
                }

                AlexaIfDirectiveItem item = null;
                synchronized (this) {
                    if (mDirectiveList.size() != 0) {
                        item = mDirectiveList.get(0);
                        mDirectiveList.remove(item);
                    }
                }
                if (item != null) {
                    distributeItems(item);
                }
            }
        }

        /**
         * QueueにDialogチャネルのディレクティブが含まれている場合、trueを返却する
         * @return
         */
        public synchronized boolean hasDialogChannelDirective() {
            boolean success = false;
            for (AlexaIfDirectiveItem item : mDirectiveList) {
                if (item instanceof AlexaIfSpeechRecognizerItem) {
                    // SpeechRecognizerインタフェース
                    success = true;
                    break;
                } else if (item instanceof AlexaIfSpeechSynthesizerItem) {
                    // SpeechSynthesizerインタフェース
                    success = true;
                    break;
                }
            }
            return success;
        }
        /**
         * QueueにAlertチャネルのディレクティブが含まれている場合、trueを返却する
         * @return
         */
        public synchronized boolean hasAlertChannelDirective() {
            boolean success = false;
            for (AlexaIfDirectiveItem item : mDirectiveList) {
                if (item instanceof AlexaIfAlertsItem) {
                    // アラートインタフェース
                    success = true;
                    break;
                }
            }
            return success;
        }
        /**
         * QueueにContentチャネルのディレクティブが含まれている場合、trueを返却する
         * @return
         */
        public synchronized boolean hasContentChannelDirective() {
            boolean success = false;
            for (AlexaIfDirectiveItem item : mDirectiveList) {
                if (item instanceof AlexaIfAudioPlayerItem) {
                    // AudioPlayerインタフェース
                    success = true;
                    break;
                }
            }
            return success;
        }
    }

    /**
     * QueueにDialogチャネルのディレクティブが含まれている場合、trueを返却する
     * @return
     */
    public synchronized boolean hasDialogChannelDirective() {
        boolean success = false;
        if (mBlockThread != null && mBlockThread.isAlive()) {
            success = mBlockThread.hasDialogChannelDirective();
        }
        return success;
    }
    /**
     * QueueにAlertチャネルのディレクティブが含まれている場合、trueを返却する
     * @return
     */
    public synchronized boolean hasAlertChannelDirective() {
        boolean success = false;
        if (mBlockThread != null && mBlockThread.isAlive()) {
            success = mBlockThread.hasAlertChannelDirective();
        }
        return success;
    }
    /**
     * QueueにContentチャネルのディレクティブが含まれている場合、trueを返却する
     * @return
     */
    public synchronized boolean hasContentChannelDirective() {
        boolean success = false;
        if (mBlockThread != null && mBlockThread.isAlive()) {
            success = mBlockThread.hasContentChannelDirective();
        }
        return success;
    }

    /**
     * 一時停止機能なし振り分けスレッド
     */
    private class UnBlockAbleThread extends Thread {

        private final Object mLockObject = new Object();

        private boolean isFinish = false;

        private ArrayList<AlexaIfDirectiveItem> mDirectiveList = new ArrayList<>();

        public UnBlockAbleThread() {
        }

        public synchronized void addDirectiveItems(ArrayList<AlexaIfDirectiveItem> directiveItems) {
            mDirectiveList.addAll(directiveItems);
        }

        public void notifyLockObject() {
            synchronized (mLockObject) {
                mLockObject.notify();
            }
        }
        public void finish() {
            isFinish = true;
        }
        @Override
        public void run() {
            while (!isFinish) {
                if (mDirectiveList.size() == 0) {
                    // 待機処理
                    synchronized (mLockObject) {
                        try {
                            mLockObject.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (isFinish) {
                    break;
                }
                AlexaIfDirectiveItem item = null;
                synchronized (mDirectiveList) {
                    if (mDirectiveList.size() > 0) {
                        item = mDirectiveList.get(0);
                        mDirectiveList.remove(item);
                    }
                }
                distributeItems(item);
            }
        }
    }

    /**
     * dialogRequestIdを持たないDirectiveの振り分け
     */
    public synchronized void postUnBlockQueue(final ArrayList<AlexaIfDirectiveItem> list) {
        if (DBG) android.util.Log.d(TAG, "***postUnBlockQueue()"+list);

        if (list.size() > 0) {
            if (mUnBlockThread == null || !mUnBlockThread.isAlive()) {
                mUnBlockThread = new UnBlockAbleThread();
                mUnBlockThread.addDirectiveItems(list);
                mUnBlockThread.start();
            }
            else {
                mUnBlockThread.addDirectiveItems(list);
                mUnBlockThread.notifyLockObject();
            }
        }
    }

    /**
     * 各Directiveの処理を各Managerに振り分け
     * @param item
     */
    private synchronized void distributeItems(final AlexaIfItem item) {
        if (DBG) android.util.Log.d(TAG, "distributeItems()");
        if (DBG) android.util.Log.d(TAG, " - distributeItems() : item = " + item);

        if (item instanceof DeleteAlertItem) {
            // アラーム設定削除
            AlexaAlertManager manager = AlexaAlertManager.getInstance();
            boolean result = false;

            result = manager.deleteAlert((DeleteAlertItem) item);
            if (result) {
                // アラーム削除完了
            }
            else {
                // アラーム削除失敗
            }
        }
        else if (item instanceof SetAlertItem) {
            AlexaAlertManager alertManager = AlexaAlertManager.getInstance();
            if (alertManager.compareAlarmToken((SetAlertItem) item)) {
                alertManager.onAlertStop();
            }
            // アラーム設定
            AlexaAlertManager manager = AlexaAlertManager.getInstance();
            boolean result = false;

            result = manager.setAlert((SetAlertItem) item);

            if (result) {
                // アラーム設定完了
            }
            else {
                // アラーム設定失敗
            }
        }
        else if (item instanceof ClearQueueItem) {
            // 音楽再生Queueクリアー
            AlexaAudioManager manager = AlexaAudioManager.getInstance();
            manager.requestEvent((ClearQueueItem) item);
        }
        else if (item instanceof PlayItem) {
            // 音楽再生
            AlexaAudioManager manager = AlexaAudioManager.getInstance();
            manager.post((PlayItem) item);
        }
        else if (item instanceof StopItem) {
            // 音楽停止
            AlexaAudioManager manager = AlexaAudioManager.getInstance();
            manager.requestEvent((StopItem) item);
        }
        else if (item instanceof ClearIndicatorItem) {
            // 通知解除
            AlexaNotificationManager manager = AlexaNotificationManager.getInstance();
            manager.clearNotification();
        }
        else if (item instanceof SetIndicatorItem) {
            // 通知
            AlexaNotificationManager manager = AlexaNotificationManager.getInstance();
            manager.setNotification((SetIndicatorItem) item);
        }
        else if (item instanceof AdjustVolumeItem) {
            // 音量設定(相対)
            AlexaAudioManager manager = AlexaAudioManager.getInstance();
            manager.requestEvent((AdjustVolumeItem) item);

            AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
            speakManager.requestEvent((AdjustVolumeItem) item);
        }
        else if (item instanceof SetMuteItem) {
            // ミュート
            AlexaAudioManager manager = AlexaAudioManager.getInstance();
            manager.requestEvent((SetMuteItem) item);

            AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
            speakManager.requestEvent((SetMuteItem) item);

        }
        else if (item instanceof SetVolumeItem) {
            // 音量設定(絶対)
            AlexaAudioManager manager = AlexaAudioManager.getInstance();
            manager.requestEvent((SetVolumeItem) item);

            AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
            speakManager.requestEvent((SetVolumeItem) item);
        }
        else if (item instanceof ExpectSpeechItem) {
            // 追加会話
            AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
            speakManager.post((ExpectSpeechItem) item);
        }
        else if (item instanceof StopCaptureItem) {
            // 音声認識
            if (mAlexaQueueCallback != null) {
                mAlexaQueueCallback.onPost((StopCaptureItem) item);
            }
        }
        else if (item instanceof ReportSoftwareInfoItem) {
            // ソフトウェア情報
            if (mAlexaQueueCallback != null) {
                mAlexaQueueCallback.onPost((ReportSoftwareInfoItem) item);
            }
        }
        else if (item instanceof SpeakItem) {
            // 音声読み上げ
            AlexaSpeakManager manager = AlexaSpeakManager.getInstance();
            manager.post((SpeakItem) item);
        }
        else if (item instanceof ResetUserInactivityItem) {
            if (mAlexaQueueCallback != null) {
                mAlexaQueueCallback.onPost((ResetUserInactivityItem)item);
            }
            // 定期通信スパンのリセット
        }
        else if (item instanceof SetEndpointItem) {
            // エンドポイント設定
            AlexaManager.setUrlEndpoint(((SetEndpointItem)item).endpoint);
            AlexaDirectiveManager.cancelDownChannel(true, false);      // ダウンチャンネルを切断
        }
        else if (item instanceof RenderPlayerInfoItem) {
            // 音楽再生系ディスプレイカード
            if (mAlexaQueueCallback != null) {
                mAlexaQueueCallback.onPost((RenderPlayerInfoItem) item);
            }
        }
        else if (item instanceof RenderTemplateItem) {
            // 通常系ディスプレイカード
            if (mAlexaQueueCallback != null) {
                mAlexaQueueCallback.onPost((RenderTemplateItem) item);
            }
        }
        else if (item instanceof SetDestinationItem) {
            // ナビ目的地設定
            if (mAlexaQueueCallback != null) {
                mAlexaQueueCallback.onPost((SetDestinationItem) item);
            }
        }
        else {
            // 予期しないアイテム
            if (DBG) android.util.Log.w(TAG, " - distributeItems() : Unexpected item.");
        }
    }

}
