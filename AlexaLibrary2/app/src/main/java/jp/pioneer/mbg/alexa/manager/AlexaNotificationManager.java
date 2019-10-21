package jp.pioneer.mbg.alexa.manager;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.Notifications.SetIndicatorItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.manager.notification.NotificationAssetCache;
import jp.pioneer.mbg.alexa.player.WLPlayer;
import jp.pioneer.mbg.alexa.util.AssetCacheUtil;
import jp.pioneer.mbg.alexa.util.CastUtil;
import jp.pioneer.mbg.android.vozsis.R;

/**
 * Created by esft-sakamori on 2017/10/05.
 */

/**
 * Notification機能管理マネージャ
 */
public class AlexaNotificationManager implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, WLPlayer.IOnPreparedListener, WLPlayer.IOnCompletionListener {

    private final static String TAG = AlexaNotificationManager.class.getSimpleName();
    private static final boolean DBG = true;

    /**
     * マネージャインスタンス
     */
    private static AlexaNotificationManager mAlexaNotificationManager = null;

    /**
     * オーディオインジケータのQueue
     */
    private ArrayList<SetIndicatorItem> mAudioIndicatorQueue = null;
    /**
     * 最後の再生したSetIndicatorディレクティブ
     */
    private SetIndicatorItem mLastIndicatorItem = null;

    private Context mContext = null;
    private Handler mHandler = null;

    public static boolean isPlayNotification = false;

    /**
     * Notification機能の状態変化を通知するコールバックリスナー
     */
    private IAlexaNotificationCallback mCallback = null;

    /**
     * Assetのキャッシュ
     */
    private HashMap<String, NotificationAssetCache> mAssetCacheMap = null;

    /**
     * Notification機能の状態変化を通知するコールバックリスナー
     */
    public interface IAlexaNotificationCallback {
        /**
         * ビジュアルインジケータが有効であることを通知
         * @param item
         */
        public void onPersistVisualIndicator(SetIndicatorItem item);

        /**
         * ビジュアルインジケータの削除を通知
         */
        public void onClearVisualIndicator();

        /**
         * オーディオインジケータが有効であることを通知
         * @param item
         */
        public void onPlayAudioIndicator(SetIndicatorItem item);

        /**
         * オーディオインジケータの再生開始を通知
         */
        public void onStartedNotificationToRing();

        /**
         * オーディオインジケータの再生終了を通知
         */
        public void onFinishedNotificationToRing();
    }

    /**
     * コンストラクタ
     */
    public AlexaNotificationManager() {
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mAudioIndicatorQueue = new ArrayList<>();
        this.mAssetCacheMap = new HashMap<String, NotificationAssetCache>();
    }

    /**
     * マネージャインスタンスの取得
     * @return
     */
    public static AlexaNotificationManager getInstance() {
        if (mAlexaNotificationManager == null) {
            mAlexaNotificationManager = new AlexaNotificationManager();
        }
        return mAlexaNotificationManager;
    }

    /**
     * マネージャのリセット
     */
    public static void resetManager() {
        if(mAlexaNotificationManager==null)return;
        // SetIndicatorのQueueをクリア
        mAlexaNotificationManager.mAudioIndicatorQueue.clear();     // インジケータリストのクリア
        // 最後に再生したSetIndicatorを削除
        mAlexaNotificationManager.mLastIndicatorItem = null;
        // コンテキストを削除
        mAlexaNotificationManager.mContext = null;
        //
        mAlexaNotificationManager.isPlayNotification = false;
        // コールバックインスタンスを削除
        mAlexaNotificationManager.mCallback = null;
        // Assetキャッシュマップをクリア
        mAlexaNotificationManager.mAssetCacheMap.clear();
    }

    /**
     * コンテキストの設定
     * @param context
     */
    public void init(Context context) {
        this.mContext = context;

        try {
            loadAssetCacheMap();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    /**
     * コールバックリスナー登録
     * @param callback
     */
    public void setAlexaNotificationCallback(IAlexaNotificationCallback callback) {
        this.mCallback = callback;
    }

    /**
     * 最後に処理したSetIndicatorディレクティブを取得
     * @return
     */
    public SetIndicatorItem getLastIndicatorItem() {
        return this.mLastIndicatorItem;
    }

    /**
     * SetIndicatorディレクティブを振り分け
     * @param item
     * @return
     */
    public boolean setNotification(SetIndicatorItem item) {
        isPlayNotification = false;
        if (DBG) android.util.Log.d(TAG, "setNotification(), item = " + item);
        boolean result = false;
        if (item != null) {
            this.mLastIndicatorItem = item;

            if (Boolean.TRUE.equals(item.persistVisualIndicator)) {
                // ビジュアルインジケータを表示
                if (this.mCallback != null) {
                    this.mCallback.onPersistVisualIndicator(item);
                }
            }
            else {
                if (this.mCallback != null) {
                    this.mCallback.onClearVisualIndicator();
                }
            }
            if (Boolean.TRUE.equals(item.playAudioIndicator)) {
                // オーディオインジケータを再生
                if (this.mCallback != null) {
                    this.mCallback.onPlayAudioIndicator(item);
                }
//                playAudioIndicator(item);
                postAudioIndicator(item);
            }
        }
        return result;
    }

    /**
     * ClearIndicatorディレクティブを振り分け
     * @return
     */
    public boolean clearNotification() {
        if (DBG) android.util.Log.d(TAG, "clearNotification()");
        boolean result = false;
        this.mLastIndicatorItem = null;
        if (this.mCallback != null) {
            this.mCallback.onClearVisualIndicator();
        }
        return result;
    }

    /**
     * 音声入力の開始を通知
     */
    public void onStartRecognize() {
        if (DBG) android.util.Log.d(TAG, "onStartRecognize()");
        // 音声認識開始時にNotificationを鳴動させる
        playAudioIndicator();
    }

    /**
     * 音声入力の終了を通知
     */
    public void onEndRecognize() {
        if (DBG) android.util.Log.d(TAG, "onEndRecognize()");
    }

    /**
     * AudioIndicatorのQueueサイズを取得
     * @return
     */
    public int getAudioIndicatorQueueSize() {
        if (DBG) android.util.Log.d(TAG, "getAudioIndicatorQueueSize()");
        return this.mAudioIndicatorQueue.size();
    }

    /**
     * オーディオインジケータの再生準備
     * @return
     */
    public boolean playAudioIndicator() {
        if (DBG) android.util.Log.d(TAG, "playAudioIndicator(1)");
        boolean result = false;
        if (getAudioIndicatorQueueSize() > 0) {
            AlexaAlertManager alertManager = AlexaAlertManager.getInstance();
            if (alertManager.getCurrentAlertItem() != null) {
                // 鳴動中のAlertがある
                if (DBG) android.util.Log.d(TAG, " - playAudioIndicator(1), Wait AudioIndicator");
                AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                AlexaQueueManager.AlexaChannel currentChannel = queueManager.getForegroundChannel();
                if (currentChannel == AlexaQueueManager.AlexaChannel.DialogChannel) {
                    // 音声認識／TTS再生中
                    result = true;
                    playAudioIndicator(this.mAudioIndicatorQueue.get(0));
                    isPlayNotification = true;
                }
                else {
                    // Alertチャネルアクティブ
                    // NOP
                }
            }
            else {
                // 鳴動中のAlertが無い
                if (DBG) android.util.Log.d(TAG, " - playAudioIndicator(1), Play AudioIndicator");
                result = true;
                playAudioIndicator(this.mAudioIndicatorQueue.get(0));
                isPlayNotification = true;
            }

        }
        else {
            if (DBG) android.util.Log.d(TAG, " - playAudioIndicator(1), AudioIndicatorQueue is Empty");
        }
        return result;
    }

    /**
     * AudioIndicatorをスタック.
     * -> Alert鳴動と排他制御するため。
     */
    private void postAudioIndicator(SetIndicatorItem item) {
        if (DBG) android.util.Log.d(TAG, "postAudioIndicator(), item = " + item);

        if (item != null) {
            this.mAudioIndicatorQueue.add(item);
            boolean isPlay = playAudioIndicator();
            if (isPlay) {
                if (DBG) android.util.Log.d(TAG, " - postAudioIndicator(), AudioIndicator is Play");
            }
            else {
                if (DBG) android.util.Log.d(TAG, " - postAudioIndicator(), AudioIndicator is not Play");
            }
        }
        else {
            if (DBG) android.util.Log.d(TAG, " - postAudioIndicator(), item is NULL");
        }

    }

    /**
     * Notificationを鳴動する
     * @param item
     */
    private void playAudioIndicator(SetIndicatorItem item) {
        if (DBG) android.util.Log.d(TAG, "playAudioIndicator(2), item = " + item);

        // AndroidOSのAudioFocusを取得
        AmazonAlexaManager.getInstance().requestAudioFocus();

        if (item != null && item.asset != null) {
            final String assetId = item.asset.assetId;
            final String url = item.asset.url;
            if (TextUtils.isEmpty(assetId) != true && TextUtils.isEmpty(url) != true) {
                AsyncTask<Void, Void, byte[]> cacheTask = new AsyncTask<Void, Void, byte[]>() {
                    @Override
                    protected byte[] doInBackground(Void... params) {
                        // 非同期でキャッシュ取得
                        byte[] cacheData = AssetCacheUtil.getAssetCache(url);
                        return cacheData;
                    }

                    @Override
                    protected void onPostExecute(byte[] cacheData) {
                        if (cacheData == null || cacheData.length == 0) {
                            // 音源データが取得出来なかった場合
                            if(DBG)android.util.Log.w(TAG, " - playAudioIndicator(2), Asset request Failed.");
                            cacheData = getCacheAssetData(assetId);
                        }
                        else {
                            // 音源データが取得できた場合
                            saveCacheAssetData(assetId, cacheData);
                        }
                        if (cacheData != null) {
                            useAssetPlayer(cacheData);
                        }
                        else {
                            // キャッシュデータも無い
                            useDefaultPlayer();
                        }
                    }
                };

                //Context context= MyApplication.getInstance();
                if (Thread.currentThread().equals(mContext.getMainLooper().getThread())) {
                    // メインスレッド中
                    cacheTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else {
                    // バックグラウンドスレッド中
                    byte[] cacheData = AssetCacheUtil.getAssetCache(url);
                    if (cacheData == null || cacheData.length == 0) {
                        // 音源データが取得出来なかった場合
                        if(DBG)android.util.Log.w(TAG, " - playAudioIndicator(2), Asset request Failed.");
                        cacheData = getCacheAssetData(item.asset.assetId);
                    }
                    else {
                        // 音源データが取得できた場合
                        saveCacheAssetData(item.asset.assetId, cacheData);
                    }
                    if (cacheData != null) {
                        useAssetPlayer(cacheData);
                    }
                    else {
                        // キャッシュデータも無い
                        useDefaultPlayer();
                    }
                }
            }
            else {
                // URLが無い -> デフォルトを再生
                useDefaultPlayer();
            }
        }
        else {
            // アセットが無い -> デフォルトを再生
            useDefaultPlayer();
        }
    }

    /**
     * バースト音再生のプレーヤーインスタンスを生成
     * @param cacheData
     */
    private void useAssetPlayer(byte[] cacheData) {
        if (false/*DebugManager.isBoolean(DebugManager.DebugSettingId.DEBUG_SETTING_ID_WEBLINK_NOTIFICATION)*/) {
            WLPlayer mediaPlayer = new WLPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);

            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            queueManager.setAllocateDirectiveFlag(true);
            try {
                //Context context= MyApplication.getInstance();
                File tempMusic = File.createTempFile("notification_asset_cache", ".mp3", mContext.getCacheDir());
//            tempMusic.deleteOnExit();
                FileOutputStream fos = new FileOutputStream(tempMusic);
                fos.write(cacheData);
                fos.close();

                FileInputStream fis = new FileInputStream(tempMusic);
                mediaPlayer.setDataSource(fis.getFD(), false);
                fis.close();
                if(tempMusic != null){
                    tempMusic.delete();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);

            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            queueManager.setAllocateDirectiveFlag(true);
            try {
                //Context context= MyApplication.getInstance();
                File tempMusic = File.createTempFile("notification_asset_cache", ".mp3", mContext.getCacheDir());
//            tempMusic.deleteOnExit();
                FileOutputStream fos = new FileOutputStream(tempMusic);
                fos.write(cacheData);
                fos.close();

                FileInputStream fis = new FileInputStream(tempMusic);
                mediaPlayer.setDataSource(fis.getFD());
                mediaPlayer.prepareAsync();
                fis.close();
                if(tempMusic != null){
                    tempMusic.delete();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * リソース内にある音源を再生する
     */
    private void useDefaultPlayer() {
        if (false/*DebugManager.isBoolean(DebugManager.DebugSettingId.DEBUG_SETTING_ID_WEBLINK_NOTIFICATION)*/) {
            //Context context= MyApplication.getInstance();
            WLPlayer mediaPlayer = WLPlayer.create(mContext, R.raw.med_alerts_notification_01, false);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.start();

                AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                queueManager.setAllocateDirectiveFlag(true);
            }
        }
        else {
            //Context context= MyApplication.getInstance();
            MediaPlayer mediaPlayer = MediaPlayer.create(mContext, R.raw.med_alerts_notification_01);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.start();

                AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                queueManager.setAllocateDirectiveFlag(true);
            }
        }
    }

    /**
     * キャッシュ保存されている音楽データを取得
     * @return
     */
    private byte[] getCacheAssetData(String assetId) {
        if (DBG) android.util.Log.d(TAG, "getCacheAssetData(), assetId = " + assetId);
        byte[] cache = null;
        if (!TextUtils.isEmpty(assetId)) {
            // キャッシュデータを取得
            if (mAssetCacheMap.containsKey(assetId)) {
                NotificationAssetCache cacheData = mAssetCacheMap.get(assetId);
                if (cacheData != null) {
                    cache = cacheData.cache;
                }
            }
        }
        return cache;
    }

    /**
     * 音楽データをキャッシュする
     * @param assetId
     * @param cache
     */
    private void saveCacheAssetData(String assetId, byte[] cache) {
        if (DBG) android.util.Log.d(TAG, "saveCacheAssetData(), assetId = " + assetId);
        NotificationAssetCache cacheData = new NotificationAssetCache(assetId, cache);
        mAssetCacheMap.put(assetId, cacheData);

        try {
            saveAssetCacheMap();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /* ------ OnCompletionListener ------ */
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (DBG) android.util.Log.d(TAG, "onPrepared()");
        mp.start();

        if (mCallback != null) {
            mCallback.onStartedNotificationToRing();
        }
    }

    /* ------ OnCompletionListener ------ */
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (DBG) android.util.Log.d(TAG, "onCompletion()");
        if (mCallback != null) {
            mCallback.onFinishedNotificationToRing();
        }

        if (mAudioIndicatorQueue.size() > 0) {
            mAudioIndicatorQueue.remove(0);
        }
        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
        queueManager.setAllocateDirectiveFlag(false);

        if (getAudioIndicatorQueueSize() > 0) {
            if (DBG) android.util.Log.d(TAG, " - onCompletion(), Next AudioIndicator.");
            playAudioIndicator();
        }
        else {
            AlexaAlertManager alertManager = AlexaAlertManager.getInstance();
            if (alertManager.getActiveAlertList().size() > 0) {
                // 鳴動待機中のアラートがある
                if (DBG) android.util.Log.d(TAG, " - onCompletion(), Resume Alert.");
                alertManager.executeAlert();
            } else {
                // 鳴動待機中のアラートが無い
                if (DBG) android.util.Log.d(TAG, " - onCompletion(), Alert queue is Empty.");
            }
        }
    }

    @Override
    public void onPrepared(WLPlayer player) {
        if (DBG) android.util.Log.d(TAG, "onPrepared(WLPlayer)");
        player.start();

        if (mCallback != null) {
            mCallback.onStartedNotificationToRing();
        }
    }

    @Override
    public void onCompletion(WLPlayer player) {
        if (DBG) android.util.Log.d(TAG, "onCompletion(WLPlayer)");
        if (mCallback != null) {
            mCallback.onFinishedNotificationToRing();
        }

        if (mAudioIndicatorQueue.size() > 0) {
            mAudioIndicatorQueue.remove(0);
        }
        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
        queueManager.setAllocateDirectiveFlag(false);

        if (getAudioIndicatorQueueSize() > 0) {
            if (DBG) android.util.Log.d(TAG, " - onCompletion(WLPlayer), Next AudioIndicator.");
            playAudioIndicator();
        }
        else {
            AlexaAlertManager alertManager = AlexaAlertManager.getInstance();
            if (alertManager != null && alertManager.getActiveAlertList().size() > 0) {
                // 鳴動待機中のアラートがある
                if (DBG) android.util.Log.d(TAG, " - onCompletion(WLPlayer), Resume Alert.");
                alertManager.executeAlert();
            } else {
                // 鳴動待機中のアラートが無い
                if (DBG) android.util.Log.d(TAG, " - onCompletion(WLPlayer), Alert queue is Empty.");
            }
        }
    }

    /**
     * バースト音をストレージに保存
     */
    private void saveAssetCacheMap() throws IOException {
        if (DBG) android.util.Log.d(TAG, "saveAssetCacheMap()");
        // TODO:Serializableの方法を使用
        //Context context= MyApplication.getInstance();
        ObjectOutputStream outputStream = new ObjectOutputStream(mContext.openFileOutput("notification_asset_cache.txt", Context.MODE_PRIVATE));
        outputStream.writeObject(mAssetCacheMap);

        outputStream.flush();
        outputStream.close();
    }

    /**
     * バースト音のキャッシュをストレージから取得
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void loadAssetCacheMap() throws IOException, ClassNotFoundException {
        if (DBG) android.util.Log.d(TAG, "loadAssetCacheMap()");
        // TODO:Serializableの方法を使用
        //Context context= MyApplication.getInstance();
        ObjectInputStream inputStream = new ObjectInputStream(mContext.openFileInput("notification_asset_cache.txt"));
        Object object = inputStream.readObject();
        if (object instanceof HashMap) {
            mAssetCacheMap = CastUtil.autoCast(object);
        } else {
            mAssetCacheMap = new HashMap<>();
        }
        inputStream.close() ;
    }
}
