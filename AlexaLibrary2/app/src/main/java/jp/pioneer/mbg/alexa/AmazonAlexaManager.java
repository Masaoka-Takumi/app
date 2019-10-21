package jp.pioneer.mbg.alexa;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.SupportActivity;
import android.support.v4.content.PermissionChecker;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.User;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfItem;
import jp.pioneer.mbg.alexa.AlexaInterface.common.Initiator;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.PlayItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Navigation.SetDestinationItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Notifications.SetIndicatorItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechRecognizer.ExpectSpeechItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechRecognizer.StopCaptureItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechSynthesizer.SpeakItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.System.ReportSoftwareInfoItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.System.SoftwareInfoItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.System.UserInactivityReportItem;
import jp.pioneer.mbg.alexa.connection.ConnectionReceiver;
import jp.pioneer.mbg.alexa.connection.OkHttpClientUtil;
import jp.pioneer.mbg.alexa.manager.AlexaAlertManager;
import jp.pioneer.mbg.alexa.manager.AlexaAudioManager;
import jp.pioneer.mbg.alexa.manager.AlexaDirectiveManager;
import jp.pioneer.mbg.alexa.manager.AlexaEventManager;
import jp.pioneer.mbg.alexa.manager.AlexaNotificationManager;
import jp.pioneer.mbg.alexa.manager.AlexaQueueManager;
import jp.pioneer.mbg.alexa.manager.AlexaSpeakManager;
import jp.pioneer.mbg.alexa.manager.AlexaUserInactivityReportManager;
import jp.pioneer.mbg.alexa.manager.TokenManager;
import jp.pioneer.mbg.alexa.manager.callback.IAudioCallback;
import jp.pioneer.mbg.alexa.player.IAlexaPlayer;
import jp.pioneer.mbg.alexa.player.WLPlayer;
import jp.pioneer.mbg.alexa.util.Constant;
import jp.pioneer.mbg.android.vozsis.R;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * AmazonAlexaマネージャ.
 */
public class AmazonAlexaManager implements AlexaQueueManager.AlexaQueueCallback, AlexaAlertManager.IAlertCallback, AlexaNotificationManager.IAlexaNotificationCallback, AlexaUserInactivityReportManager.IAlexaUserInactivityReportCallback, AlexaDirectiveManager.DownChannelCallback, AlexaQueueManager.AlexaChannelChangeListener, ConnectionReceiver.Observer, AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = AmazonAlexaManager.class.getSimpleName();
    private static final boolean DBG = false;
    private static final boolean ToastDBG = false;

    /* AudioFocus管理 */
    private AudioManager mAudioManager = null;
    private AudioFocusRequest mAudioFocusRequest = null;
    private boolean mHasAudioFocus = false;
    public boolean isShowAlexaDialog = false;
    /**
     * 音声入力ON時 効果音プレーヤー
     */
    private MediaPlayer mOnPlayer;
    /**
     * 音声入力OFF時 効果音プレーヤー
     */
    private MediaPlayer mOffPlayer;

    /**
     * 音声入力ON時 効果音プレーヤー
     */
    private WLPlayer mOnWLPlayer;
    /**
     * 音声入力OFF時 効果音プレーヤー
     */
    private WLPlayer mOffWLPlayer;

    /**
     * マネージャインスタンス
     */
    private static AmazonAlexaManager mManager = null;

    /**
     * Activity
     */
    private SupportActivity mActivity = null;

    /**
     * AmazonAlexaManager -> 画面等へのコールバックインスタンス
     */
    private ArrayList<IAlexaCallback> mAlexaCallbackList = null;

    /**
     * AlexaLoginManager -> AmazonAlexaManagerのコールバックインスタンス
     */
    private AlexaLoginCallback mLoginCallback = null;

    /**
     * Pingを送信するタイマー
     */
    private TimerTask mPingTimerTask = null;
    private Timer mPingTimer = null;

    private AsyncTask mAsyncTask = null;

    /**
     * DownChannel再接続 初回リトライ間隔
     */
    private static final int FIRST_TIME = 500;
    /**
     * DownChannel再接続 リトライ間隔
     */
    private long mWaitTime = FIRST_TIME;
    /**
     * デフォルト乗数値
     */
    private static double DEFAULT_MULTIPLIER = 2;
    /**
     * リトライ間隔の上限
     */
    private static int UPPERLIMIT_TIME = 60000;

    private Handler mHandler = null;
    private Runnable runnable;

    /**
     * 自動ログイン終了フラグ
     */
    private boolean isAutoLoginFinished = false;

    /**
     * AVS接続状態フラグ
     */
    private boolean isAvsConnect = false;
    public static boolean isAlexaUnavailable = false;
    /**
     * DownChannel再接続処理キャンセルフラグ
     */
    private boolean isCancelDownChannelConnection = false;

    /**
     * サンプリングレート(ヘルツ)
     */
    private static final int AUDIO_RATE = 16000;
    /**
     * 録音データのバッファリングサイズ
     */
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(AUDIO_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT);

    /**
     * Synchronized排他制御用オブジェクト
     */
    private final Object mLock = new Object();

    /**
     * レコーダー
     */
    private AudioRecord mAudioRecord;
    /**
     * 録音状態フラグ
     */
    private boolean mIsRecording = false;

    /**
     * 音声入力中のRecognizeイベントのdialogRequestId
     */
    private String mDialogRequestId = null;

    /**
     * AlexaLoginフラグ
     */
    public static boolean mIsAlexaConnection = false;
    public static boolean mIsDownChannelOpened = false;

    /**
     * AudioPlayer、Speak 音声再生リスナー
     */
    private AlexaAudioListener mAlexaAudioListener = null;

    /**
     * 通信中のCallを保持(キャンセル処理のため)
     */
    private Call mCurrentCall = null;

    /**
     * Tap-to-talk用 音声入力RequestBody
     */
    private SpeakRequestBody mRequestBody = new SpeakRequestBody();

    @Override
    public void onNetworkConnect() {
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onNetworkConnect();
            }
        }
    }

    @Override
    public void onNetworDisConnect() {
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onNetworkDisconnect();
            }
        }
    }

    /**
     * Alexaコールバックインタフェース(AmazonAlexaManager -> Activity)
     */
    public interface IAlexaCallback {
        /**
         * ログイン成功
         */
        public void onLoginSuccess();

        /**
         * ログイン失敗
         */
        public void onLoginFailed();

        /**
         * ログアウト
         */
        public void onLogout();

        /**
         * 機能API送信成功
         */
        public void onCapabilitiesSendSuccess();

        /**
         * Alexaに接続されてる
         * */
        public void onConnect();

        /**
         * Alexaに接続されてない
         */
        public void onDisConnect();

        /**
         * Networkに接続されている
         */
        public void onNetworkConnect();

        /**
         * Networkに接続されてない
         */
        public void onNetworkDisconnect();

        /**
         * 音声入力開始
         */
        public void onRecordingStart();

        /**
         * 音声入力中のコールバック
         */
        public void onRecordingMonitor(double db, int hertz);

        /**
         * 音声入力終了
         */
        public void onRecordingStop(boolean isCancel);


        /**
         * 音声再生準備開始
         */
        public void onSpeakingPrepare();

        /**
         * 音声再生準備完了
         */
        public void onSpeakingPrepared();

        /**
         * 音声再生開始
         */
        public void onSpeakingStart();

        /**
         * 音声再生一時再開
         */
        public void onSpeakingResume();

        /**
         * 音声再生一時停止
         */
        public void onSpeakingPause();

        /**
         * 音声再生終了
         */
        public void onSpeakingStop();

        /**
         * 音声再生完了
         */
        public void onSpeakingComplete();

        /**
         * 音楽のTemplateを受信した際にコールバック.
         */
        public void onReceiveRenderPlayerInfo(RenderPlayerInfoItem playerInfoItem);

        /**
         * 音楽再生準備開始
         */
        public void onAudioPrepare();

        /**
         * 音楽再生準備完了
         */
        public void onAudioPrepared();

        /**
         * 音楽再生開始
         */
        public void onAudioStart();

        /**
         * 音楽再生一時再開
         */
        public void onAudioResume();

        /**
         * 音楽再生一時停止
         */
        public void onAudioPause();

        /**
         * 音楽再生終了
         */
        public void onAudioStop();

        /**
         * 音楽再生エラー
         */
        public void onAudioError();

        /**
         * 音楽再生完了
         */
        public void onAudioComplete();

        /**
         * WebLinkのAudioFocusが奪われた
         */
        public void onWLAudioFocusLoss();

        /**
        /**
         * 音楽再生進捗更新
         */
        public void onAudioUpdateProgress(int current, int duration);

        /**
         * システムエラー
         */
        public void onSystemError();
        /**
         * アラート鳴動開始
         */
        public void onAlertStarted();

        /**
         * ショートアラート鳴動開始
         */
        public void onShortAlertStarted();

        /**
         * アラート鳴動終了
         */
        public void onAlertStopped();

        /**
         * アラートのセット
         */
        public void onSetAlert();

        /**
         * アラートの消去
         */
        public void onStopAlertAll();

        /**
         * AlexaNotification ビジュアルインジケータ表示
         */
        public void onPersistVisualIndicator();

        /**
         * AlexaNotification ビジュアルインジケータ非表示
         */
        public void onClearVisualIndicator();

        /**
         * オーディオインジケータ再生
         */
        public void onAudioIndicatorStarted();

        /**
         * オーディオインジケータ停止
         */
        public void onAudioIndicatorStopped();

        /**
         *Volume変更
         */
        public void onSetVolume(float volume);

        public void onAdjustVolume(float volume);

        /*
        * Mute
        * */
        public void onSetMute(boolean isMute);

        public void onNoResponse();

        /**
         * チャンネルのアクティブ／非アクティブに変化があった際に通知
         * @param channel
         * @param isActive
         */
        public void onChannelActiveChange(AlexaQueueManager.AlexaChannel channel, boolean isActive);

        /**
         * マイクパーミッションの状態を通知
         * @param state {@link PermissionChecker#PERMISSION_GRANTED}
         *     or {@link PermissionChecker#PERMISSION_DENIED} or {@link PermissionChecker#PERMISSION_DENIED_APP_OP}.
         */
        public void onMicrophonePermission(int state);

        public void onNoDirectiveAtSendEventResponse();

        /**
         * PlayListの解析開始
         */
        public void onDecodeStart();

        /**
         * PlayListの解析終了
         */
        public void onDecodeFinish();
        public void onSetNaviDestination(Double latitude, Double longitude, String name);

        public void onRecordingNotAvailable();
    }

    /**
     * コンストラクタ
     */
    private AmazonAlexaManager() {
        if (DBG) android.util.Log.d(TAG, "AmazonAlexaManager()");
        this.mHandler = new Handler(Looper.getMainLooper());
//        // APIKey生成時にパッケージ名等からフィンガープリントを取得する必要があり、その時のコード。
//        if(false){
//            SigningKey.getCertificateMD5Fingerprint(mActivity);
//        }
        //init();
    }

    /**
     * 生成済みマネージャインスタンスを取得
     * @return
     */
    public static AmazonAlexaManager getInstance() {
        if (DBG) android.util.Log.d(TAG, "getInstance()");
        if (mManager == null) {
            mManager = new AmazonAlexaManager();
        }
        return mManager;
    }

    /**
     * マネージャインスタンスの破棄
     */
    public static void resetManager() {
        if (mManager != null) {
            if (mManager.mOnPlayer != null) {
                mManager.mOnPlayer.release();
                mManager.mOnPlayer = null;
            }
            if (mManager.mOffPlayer != null) {
                mManager.mOffPlayer.release();
                mManager.mOffPlayer = null;
            }

            if (mManager.mPingTimerTask != null) {
                mManager.mPingTimerTask.cancel();
                mManager.mPingTimerTask = null;
            }
            if (mManager.mPingTimer != null) {
                mManager.mPingTimer.cancel();
                mManager.mPingTimer = null;
            }
            if (mManager.mAsyncTask != null) {
                mManager.mAsyncTask.cancel(true);
            }

            mManager.mWaitTime = FIRST_TIME;

            mManager.runnable = null;

            if (mManager.mAudioRecord != null) {
                mManager.mAudioRecord.stop();
                mManager.mAudioRecord.release();
                mManager.mAudioRecord = null;
            }

            mManager.mIsRecording = false;

            mManager.mDialogRequestId = null;

            mManager.mIsAlexaConnection = false;
            mIsDownChannelOpened = false;
            mManager.mAlexaAudioListener = null;

            if (mManager.mCurrentCall != null) {
                mManager.mCurrentCall.cancel();
                mManager.mCurrentCall = null;
            }
            //ログインで必要なためAlexa切断で破棄しない
/*            mManager.mLoginCallback = null;
            mManager.mAlexaCallback = null;
            mManager.mActivity = null;*/
        }
    }

    /**
     * Activityを設定
     * @param activity
     */
//    public void setActivity(Activity activity) {
//        this.mActivity = activity;
//    }
//
//    private void init() {
//        // ログインマネージャ初期化
//        initLogin();
//        // その他のAlexa機能マネージャ初期化
//        initManager();
//    }

    public void setActivity(SupportActivity activity){
        this.mActivity = activity;
        // ログインマネージャ初期化
        initLogin();
    }

    public void init() {
//        this.mActivity = activity;
//        // ログインマネージャ初期化
//        initLogin();
        // その他のAlexa機能マネージャ初期化
        initManager();

        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        }
    }
    /**
     * Alexa機能を終了する
     * @param activity
     */
    public void finishAmazonAlexa(SupportActivity activity) {
        final boolean r = activity == mActivity;
        showToast("finishAmazonAlexa:" + r);
        //TODO:呼び出しで必ず終了処理を実行
        //if (activity == mActivity) {
            // Activityが同じものであれば終了処理を実行（もしものための２重起動対策）

            // ダウンチャンネルを切断
            AlexaDirectiveManager.cancelDownChannel(false, true);
            OkHttpClientUtil.getAvsConnectionOkHttpClient().dispatcher().cancelAll();

            // 各マネージャをリセット
            AlexaSpeakManager.resetManager();
            AlexaAlertManager.resetManager();
            AlexaNotificationManager.resetManager();
            AlexaAudioManager.resetManager();

            //Speech再生中にバックへ移行すると、isAllocateDirectiveがTrueのまま復帰してしまい、
            //Directive振り分け処理が再開できなくなるので、停止するタイミングでFalseにする
            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            queueManager.setAllocateDirectiveFlag(false);
        //}
    }

    public boolean isSignedIn() {
        boolean result = false;
        AlexaLoginManager loginManager = AlexaLoginManager.getInstance();
        result = loginManager.isSignedIn();

        return result;
    }

    public void addAlexaCallback(IAlexaCallback callback) {
        if (DBG) android.util.Log.d(TAG, "addAlexaCallback(), callback = " + callback);
        if(mAlexaCallbackList==null){
            mAlexaCallbackList = new ArrayList<IAlexaCallback>();
        }
        if(!mAlexaCallbackList.contains(callback)){
            mAlexaCallbackList.add(callback);
        }
    }

    public void removeAlexaCallback(IAlexaCallback callback) {
        if (DBG) android.util.Log.d(TAG, "removeAlexaCallback(), callback = " + callback);
        if (mAlexaCallbackList == null) {
            return;
        }
        mAlexaCallbackList.remove(callback);
    }

    /**
     * 初期通信完了フラグ
     */
    private boolean mIsInitComp = false;
    public boolean isInitComp() {
        return mIsInitComp;
    }
    /* ---- ライフサイクル同期 ---- */
    /**
     * Activityがフォアグラウンドに復帰
     */
    public void onActivityResume() {
        if (DBG) android.util.Log.d(TAG, "onActivityResume()");
        //バックグラウンド遷移でAVS接続を切断しないため接続し直さない
        //onAvsConnect();

        // ログインマネージャに通知
        AlexaLoginManager loginManager = AlexaLoginManager.getInstance();
        loginManager.onActivityResume();
    }

    /**
     * Activityがバックグラウンドに遷移
     */
    public void onActivityPause() {
        if (DBG) android.util.Log.d(TAG, "onActivityPause()");
        onAvsDisconnect();

        //onPause時のみ自分自身の終了
        resetManager();
    }

    /*
    * AVS復帰処理
    * */
    public void onAvsConnect(){
        if (DBG) android.util.Log.d(TAG, "onAvsConnect()");
        //NetworkReceiverでクラッシュしていたので一旦コメントアウト
        //Network状態監視
//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        ConnectionReceiver mReceiver = new ConnectionReceiver(this);
//        mActivity.registerReceiver(mReceiver,filter);

        isAvsConnect = true;

        // Directiveマネージャに通知
        AlexaDirectiveManager.setIsFinishFlag(false);

        // マイクON／OFF効果音のMediaPlayerを初期化
        initMediaPlayer();

        // Directiveマネージャにコールバックインスタンスを登録
        AlexaDirectiveManager.registrationDownChannelCallback(mManager);
    }

    /*
     *  AVS停止処理
     * */
    public void onAvsDisconnect(){
        if (DBG) android.util.Log.d(TAG, "onAvsDisconnect()");
        isAvsConnect = false;

        // ディレクティブの振り分けを停止
        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
        queueManager.onAvsDisconnect();

        //AnyPlace:No33の対応。DownChannnel再接続をキャンセルさせる
        isCancelDownChannelConnection = true;

        AlexaAlertManager manager = AlexaAlertManager.getInstance();
        // アラートの停止
        manager.stop();

        // Directiveマネージャからコールバックインスタンスを解除
        AlexaDirectiveManager.unregistrationDownChannelCallback(mManager);

        AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
        IAlexaPlayer audioPlayer = audioManager.getAlexaPlayer();
        if (audioPlayer != null) {
            // 音楽プレーヤーの破棄
            audioPlayer.stopPlayer();
            audioPlayer.releasePlayer();
        }

        AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();

        IAlexaPlayer speakPlayer = speakManager.getAlexaPlayer();
        if (speakPlayer != null) {
            // 音声再生プレーヤーの破棄
            speakPlayer.stopPlayer();
            speakPlayer.releasePlayer();
        }
        mIsAlexaConnection = false;
        // Alexa機能の停止処理
        finishAmazonAlexa(mActivity);

        // AndroidのAudioFocusを破棄
        abandonAudioFocus();
    }

    public boolean getIsAvsConnect(){
        return isAvsConnect;
    }

    /**
     * 車載機との接続状態を取得する
     * @return
     */
    public boolean isWebLinkConnection() {
        boolean result = true;
        //常に接続中を返す
/*        if (mActivity instanceof WebLinkConnectionActivity) {
            result = ((WebLinkConnectionActivity) mActivity).isConnected();
        }*/

        if (DBG) android.util.Log.d(TAG, "isWebLinkConnection() : " + result);
        return result;
    }

    /* ---- ログイン処理 ---- */
    /**
     * ログイン処理
     */
    public void loginAlexa() {
        if (DBG) android.util.Log.d(TAG, "loginAlexa()");
        AlexaLoginManager loginManager = AlexaLoginManager.getInstance();
        loginManager.doLogin();
    }

    /**
     * 自動ログイン
     */
    public void autoLoginAlexa() {
        if (DBG) android.util.Log.d(TAG, "autoLoginAlexa()");
        AlexaLoginManager loginManager = AlexaLoginManager.getInstance();
        loginManager.doAutoLogin();
    }

    /**
     * ログアウト
     */
    public void logoutAlexa() {
        if (DBG) android.util.Log.d(TAG, "logoutAlexa()");
        AlexaLoginManager loginManager = AlexaLoginManager.getInstance();
        loginManager.logout();
    }
    /**
     * Capabilities API送信
     */
    public void sendCapabilities() {
        if (DBG) android.util.Log.d(TAG, "sendCapabilities()");
        AlexaLoginManager loginManager = AlexaLoginManager.getInstance();
        loginManager.sendCapabilities();
    }

    /**
     * ログイン情報・インスタンス初期設定
     */
    private void initLogin() {
        if (DBG) android.util.Log.d(TAG, "initLogin()");
        mLoginCallback = new AlexaLoginCallback();

        AlexaLoginManager loginManager = AlexaLoginManager.getInstance();
        loginManager.init(mActivity);
        loginManager.setAlexaLoginCallback(mLoginCallback);
    }

    /**
     * ユーザー情報を取得しログに表示する。
     */
    private void fetchUserProfile() {
        if (DBG) android.util.Log.d(TAG, "fetchUserProfile()");
        // Amazonの参考サイトの通りに行っているはずだが、なぜか成功しない。
        // -> Alexaの動作には影響が無いので、このまま。
        User.fetch(mActivity, new Listener<User, AuthError>() {

            /**
             * 取得成功
             * @param user
             */
            @Override
            public void onSuccess(User user) {
                final String name = user.getUserName();
                final String email = user.getUserEmail();
                final String account = user.getUserId();
                final String zipcode = user.getUserPostalCode();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (DBG) android.util.Log.d(TAG, "- fetchUserProfile(), onSuccess(n:"+name+", e:"+email+", account:"+account+", zipcode:"+zipcode+")");
                    }
                });
            }

            /**
             * 取得失敗
             * @param ae
             */
            @Override
            public void onError(AuthError ae) {
                if (DBG) android.util.Log.i(TAG, " - fetchUserProfile(), onError.");
            }
        });
    }

    /* ---- ダウンチャネル ---- */
    /**
     * ダウンチャネル接続
     */
    public void openDownChannel() {
        String accessToken = TokenManager.getToken();
        isCancelDownChannelConnection = false;
        openDownChannel(accessToken);
    }

    /**
     * DownChannelStream確立メソッド.<br>
     * アプリ起動時に行う必要がある.
     * @param token
     */
    private void openDownChannel(final String token){
        if (DBG) android.util.Log.d(TAG, "openDownChannel(), token = " + token);
        mIsInitComp = false;
        mAsyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                //AsyncTask の中でブレークポイントを設定する場合必要
                //android.os.Debug.waitForDebugger();
                AlexaDirectiveManager.registrationDownChannelCallback(mManager);
                AlexaDirectiveManager.openDownChannel(mActivity, token);
                return null;
            }
            @Override
            protected void onPostExecute(Void avsResponse) {
                super.onPostExecute(avsResponse);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 切断
     */
    public static void disconnect(){
        android.util.Log.d(TAG, "***** disconnect()");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Timerのスレッド
                AlexaDirectiveManager.cancelDownChannel(true,false);
            }
        };
        Timer t = new Timer();
        t.scheduleAtFixedRate(task, 0, 1000*60); //1000ミリ秒(1秒)*60*5 → 5分間隔
    }

    /**
     * PINGを送信する
     */
    private void sendPing(){
        if (DBG) android.util.Log.d(TAG, "sendPing()");
        if (mPingTimerTask != null) {
            mPingTimerTask.cancel();
        }
        if (mPingTimer != null) {
            mPingTimer.cancel();
        }
        mPingTimerTask = new TimerTask() {
            @Override
            public void run() {
                // Timerのスレッド
                AlexaEventManager.sendPing(TokenManager.getToken(), mActivity, new AlexaEventManager.AlexaCallback() {

                    @Override
                    public void onExecute(Call call) {
                        if (DBG) android.util.Log.d(TAG, "Ping onExecute()");
                    }

                    @Override
                    public void onResponse(Call call, int httpCode) {
                        if (DBG) android.util.Log.d(TAG, "Ping onResponse()");
                        if (200 <= httpCode && httpCode < 300) {
                            // 成功
                            if (DBG) android.util.Log.d(TAG, " - Ping onResponse(), Success");
                        }
                        else {
                            // 失敗
                            if (DBG) android.util.Log.w(TAG, " - Ping onResponse(), Error");
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (DBG) android.util.Log.w(TAG, "Ping onFailure(), e = " + e);
                    }

                    @Override
                    public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                        if (DBG) android.util.Log.w(TAG, "Ping onParsedResponse(), itemList = " + itemList);
                    }
                });
            }
        };
        // スケジュール化
        mPingTimer = new Timer();
        mPingTimer.scheduleAtFixedRate(mPingTimerTask, 0, 1000*60*5); //1000ミリ秒(1秒)*60*5 → 5分間隔
    }

    /**
     * アプリのバージョン名を取得する
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * SoftwareInfo Event送信
     */
    private void sendSoftwareInfo(){
        //Ver名取得
        String versionName = getVersionName(mActivity.getApplicationContext());
        if(DBG)android.util.Log.i(TAG, "getVersionName() : verName =  "+ versionName);

        //Ver名から数字のみを抜き出して、Eventを送信
        String verName = versionName.replaceAll("\\D","");
        if(DBG)android.util.Log.i(TAG, "getVersionName() : verName =  "+ verName);

        SoftwareInfoItem infoItem = new SoftwareInfoItem(verName);
        AlexaEventManager.sendEvent(TokenManager.getToken(), infoItem, mActivity, new AlexaEventManager.AlexaCallback() {

            @Override
            public void onExecute(Call call) {
                if (DBG) android.util.Log.d(TAG, "SoftwareInfo onExecute()");
            }

            @Override
            public void onResponse(Call call, int httpCode) {
                if (DBG) android.util.Log.d(TAG, "SoftwareInfo onResponse()");
                if (httpCode == 204) {
                    // 成功
                    if (DBG) android.util.Log.d(TAG, " - SoftwareInfo  onResponse(), Success");
                }
                else {
                    // 失敗
                    if (DBG) android.util.Log.w(TAG, " - SoftwareInfo onResponse(), Error");
                    if (DBG) android.util.Log.w(TAG, "   http code is " + httpCode);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                if (DBG) android.util.Log.w(TAG, "SoftwareInfo onFailure(), e = " + e);
            }

            @Override
            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                if (DBG) android.util.Log.w(TAG, "SoftwareInfo onParsedResponse(), itemList = " + itemList);
            }
        });
    }

    /* ---- 音声入力 ---- */

    /**
     * 録音開始
     */
    public void doRecordingStart() {
        if (DBG) android.util.Log.d(TAG, "doRecordingStart()");
        if (DBG) android.util.Log.d(TAG, " - doRecordingStart(), mIsRecording = " + mIsRecording);
        //ExpectSpeechの聞き返し停止フラグをリセット
        AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
        if (speakManager != null) {
            speakManager.mIsStopExpectSpeech = false;
        }
        if(!mIsRecording) {
            // マイクパーミッションをチェック
            checkMicrophonePermission();
        }
    }
    /**
     * 録音終了
     */
    public void doRecordingCancel() {
        if (DBG) android.util.Log.d(TAG, "doRecordingCancel()");
        if (DBG) android.util.Log.d(TAG, " - doRecordingCancel(), mIsRecording = " + mIsRecording);
        if (mIsRecording) {
            // 録音処理を停止
            stopRecording(true);
        }
    }

    /**
     * 録音中か判定
     * @return
     */
    public boolean isRecording() {
        if (DBG) android.util.Log.d(TAG, "isRecording()");
        if (DBG) android.util.Log.d(TAG, " - isRecording(), mIsRecording = " + mIsRecording);
        return mIsRecording;
    }

    /**
     * 音声再生、及び、処理待ちディレクティブのキャンセル
     */
    public void doSpeechCancel() {
        if (DBG) android.util.Log.d(TAG, "doSpeechCancel()");
        if (mDialogRequestId != null) {
            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            queueManager.cancelRecognizeEvent(mDialogRequestId);
            mDialogRequestId = null;
        }
        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
        queueManager.doCancelBlockAbleQueue();
    }

    /**
     * デフォルト音源のプレーヤー生成
     * @param
     * @return
     */
    private void initMediaPlayer() {
/*        if (DebugManager.isBoolean(DebugManager.DebugSettingId.DEBUG_SETTING_ID_WEBLINK_MIC_SOUND_EFFECT)) {
            mOffWLPlayer = new WLPlayer();
            mOnWLPlayer = new WLPlayer();
            mOffWLPlayer.setOnCompletionListener(mWLSoundListener);
            mOnWLPlayer.setOnCompletionListener(mWLSoundListener);
        }
        else {*/
//            mOffPlayer = MediaPlayer.create(mActivity, R.raw.ful_ui_endpointing_touch);
//            mOnPlayer = MediaPlayer.create(mActivity, R.raw.ful_ui_wakesound_touch);
            mOffPlayer = MediaPlayer.create(mActivity, R.raw.med_ui_endpointing_touch_normalize);
            mOnPlayer = MediaPlayer.create(mActivity, R.raw.med_ui_wakesound_touch_normalize);
            mOffPlayer.setOnCompletionListener(mSoundListener);
            mOnPlayer.setOnCompletionListener(mSoundListener);
        //}
    }
    /**
     * マイクパーミッションチェック
     */
    private void checkMicrophonePermission() {
        if (DBG) android.util.Log.d(TAG, "checkMicrophonePermission()");

        int state = PermissionChecker.checkSelfPermission(mActivity, Manifest.permission.RECORD_AUDIO);
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                // UI側にコールバックし処理の内容は任せる
                mAlexaCallbackList.get(i).onMicrophonePermission(state);;
            }
        }
    }


    /**
     * 録音インスタンス生成
     */
    private void createRecorder() {
        if (DBG) android.util.Log.d(TAG, "createRecorder()");
        if (DBG) android.util.Log.e(TAG, "audio bufSize:" + BUFFER_SIZE);
        synchronized (mLock) {
            // AudioRecordの設定はAlexaから指定を受けているフォーマット通り.
            mAudioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    AUDIO_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    BUFFER_SIZE);
        }

    }

    /**
     * 録音開始効果音の再生
     */
    public void playSoundOn(){
        // マイクONの効果音

        // AndroidOSのAudioFocusを取得
        AmazonAlexaManager.getInstance().requestAudioFocus();

/*        if (DebugManager.isBoolean(DebugManager.DebugSettingId.DEBUG_SETTING_ID_WEBLINK_MIC_SOUND_EFFECT)) {
            try {
                mOnWLPlayer.setDataSource(mActivity, R.raw.ful_ui_wakesound_touch, false);
                mOnWLPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {*/
            mOnPlayer.start();
        //}
    }

    /**
     * 録音開始メソッド.
     */
    public void startRecording() {
        if (DBG) android.util.Log.d(TAG, "startRecording(1)");
        this.startRecording(null);
    }

    /**
     * 録音開始メソッド.
     */
    private void startRecording(final Initiator initiator) {
        if (DBG) android.util.Log.d(TAG, "startRecording(2)");
        if (DBG) android.util.Log.d(TAG, " - startRecording(2), mIsRecording = " + mIsRecording);
        if(!mIsRecording){
            // 音楽やアラートを止める
            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            queueManager.startRecognize();

            // 効果音の再生
            playSoundOn();

            // AudioRecordを作成する.
            createRecorder();

            mIsRecording = true;
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    // 音声入力開始を通知
                    mAlexaCallbackList.get(i).onRecordingStart();
                }
            }

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    // 録音音声を送信する.
                    mDialogRequestId = AlexaEventManager.sendAudioEvent(
                            TokenManager.getToken(),
                            mRequestBody,
                            mActivity,
                            mAlexaRecognizeCallback, initiator);
                }
            });
            t.start();
        }
    }

    /**
     * 録音停止メソッド.
     * @param isCancel キャンセルかどうかの判定.
     */
    private void stopRecording(boolean isCancel) {
        if (DBG) android.util.Log.d(TAG, "stopRecording(), isCancel = " + isCancel);
        // 音楽やアラートを再開

        if (isCancel && mDialogRequestId != null) {
            // キャンセルの場合、Recognizeイベントのレスポンスを破棄
            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            queueManager.cancelRecognizeEvent(mDialogRequestId);
            mDialogRequestId = null;
        }
        synchronized (mLock) {
            if (mAudioRecord != null && mIsRecording) {
                mIsRecording = false;
                // AudioRecordを破棄
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
                // マイクOFFの効果音
                if (!isCancel) {
                    // キャンセル時は鳴らさない

                    // AndroidOSのAudioFocusを取得
                    AmazonAlexaManager.getInstance().requestAudioFocus();

/*                    if (DebugManager.isBoolean(DebugManager.DebugSettingId.DEBUG_SETTING_ID_WEBLINK_MIC_SOUND_EFFECT)) {
                        try {
                            mOffWLPlayer.setDataSource(mActivity, R.raw.ful_ui_endpointing_touch, false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mOffWLPlayer.start();
                    }
                    else {*/
                        mOffPlayer.start();
                    //}
                }
                AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                queueManager.endRecognize();
                if (mAlexaCallbackList != null) {
                    // 音声入力の終了を通知
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onRecordingStop(isCancel);
                    }
                }
            }
        }
    }

    /* ---- その他機能 ---- */
    /**
     * アラート手動停止
     */
    public void doAlertStop() {
        if (DBG) android.util.Log.d(TAG, "doAlertStop()");
        AlexaAlertManager manager = AlexaAlertManager.getInstance();
        manager.onAlertStop();
    }

    public void noResponse(){
        if (mAlexaCallbackList != null) {
            // 音声入力開始を通知
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onNoResponse();
            }
        }

    }

    /**
     * 各種マネージャ生成
     */
    private void initManager() {
        if (DBG) android.util.Log.d(TAG, "initManager()");
        mAlexaAudioListener = new AlexaAudioListener();
        // 音楽再生機能マネージャ
        AlexaAudioManager manager = AlexaAudioManager.getInstance();
        manager.createAlexaPlayer(mActivity.getApplicationContext());
        manager.setIAudioCallback(mAlexaAudioListener);
        // 音声再生機能マネージャ
        AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
        speakManager.createAlexaPlayer(mActivity.getApplicationContext());
        speakManager.setIAudioCallback(mAlexaAudioListener);
        // アラート機能マネージャ
        AlexaAlertManager alertManager = AlexaAlertManager.getInstance();
        alertManager.setContext(mActivity.getApplicationContext());
        alertManager.setAlertCallback(this);
        // Notification機能マネージャ
        AlexaNotificationManager notificationManager = AlexaNotificationManager.getInstance();
        notificationManager.init(mActivity.getApplicationContext());
        notificationManager.setAlexaNotificationCallback(this);
        // UserInactivityReportイベント送信機能マネージャ
        AlexaUserInactivityReportManager userInactivityReportManager = AlexaUserInactivityReportManager.getInstance();
        userInactivityReportManager.init(mActivity.getApplicationContext());
        userInactivityReportManager.setAlexaUserInactivityReportCallback(this);

        // AlexaQueueManagerへコールバックリスナーを設定
        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
        queueManager.setAlexaQueueCallback(this);
        queueManager.setAlexaChannelChangeListener(this);

    }

    /**
     * ディレクティブ処理通知
     * @param directive
     */
    @Override
    public void onPost(AlexaIfDirectiveItem directive) {
        if (DBG) android.util.Log.d(TAG, "onPost(), directive = " + directive);
        if (directive instanceof StopCaptureItem) {
            // 音声認識完了
            if(mHandler != null){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 音声入力を終了（正常に入力完了）
                        stopRecording(false);
                    }
                });
            }
        }
        else if (directive instanceof ExpectSpeechItem) {
            // 追加対話 -> 再度、マイクをONにする
            if(!isShowAlexaDialog){
                AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
                if (speakManager != null) {
                    speakManager.stopExpectSpeech();
                }
                return;
            }
            final Initiator initiator = ((ExpectSpeechItem) directive).initiator;

            if(mHandler != null){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 対話の中で受信するExpectSpeechディレクティブのため、パーミッション判定は必要ない
                        startRecording(initiator);
                    }
                });
            }
        }
        else if(directive instanceof ReportSoftwareInfoItem){
            // SoftwareInfoイベント送信要求
            if(mHandler != null){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // SoftwareInfoイベント送信
                        sendSoftwareInfo();
                    }
                });
            }
        }
        else if(directive instanceof RenderPlayerInfoItem){
            // 再生する音楽のディスプレイカードの情報
            final RenderPlayerInfoItem renderPlayerItem = (RenderPlayerInfoItem) directive;
            if(mHandler != null){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mAlexaCallbackList != null) {
                            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                                // 音楽再生時用ディスプレイカードを表示
                                mAlexaCallbackList.get(i).onReceiveRenderPlayerInfo(renderPlayerItem);
                            }
                        }
                    }
                });
            }
        }
        else if(directive instanceof SetDestinationItem){
            // ナビ目的地設定
            final SetDestinationItem setDestinationItem = (SetDestinationItem) directive;
            if (DBG) android.util.Log.d(TAG, "onSetNaviDestination() name=" +setDestinationItem.destinationName
                    + ", latitude=" + String.valueOf(setDestinationItem.latitude)+", longitude="+String.valueOf(setDestinationItem.longitude));
            if(mHandler != null){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mAlexaCallbackList != null) {
                            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                                mAlexaCallbackList.get(i).onSetNaviDestination(setDestinationItem.latitude, setDestinationItem.longitude, setDestinationItem.destinationName);
                            }
                        }
                    }
                });
            }
        }
        else {
            // 意図しないDirective
        }
    }

    /**
     * アラート鳴動開始
     */
    @Override
    public void onAlertStarted() {
        if (DBG) android.util.Log.d(TAG, "onAlertStarted()");
        // アラート再生開始
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onAlertStarted();
            }
        }
        // チャンネルをアラートに更新
        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
        queueManager.startAlertChannel();
    }

    /**
     * ショートアラート鳴動
     */
    @Override
    public void onShortAlertStarted() {
        if (DBG) android.util.Log.d(TAG, "onShortAlertStarted()");
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onShortAlertStarted();
            }
        }
    }

    /**
     * アラート鳴動終了
     */
    @Override
    public void onAlertFinished() {
        if (DBG) android.util.Log.d(TAG, "onAlertFinished()");
        // アラート停止
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onAlertStopped();
            }
        }
    }

    /**
     * アラートのセット
     */
    @Override
    public void onSetAlert() {
        if (DBG) android.util.Log.d(TAG, "onSetAlert()");
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onSetAlert();
            }
        }
    }

    /**
     * アラートの全消去
     */
    @Override
    public void onStopAlertAll() {
        if (DBG) android.util.Log.d(TAG, "onStopAlertAll()");
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onStopAlertAll();
            }
        }
    }

    /* ---------- IAlexaNotificationCallback ---------- */

    /**
     * ビジュアルインジケータ表示
     * @param item
     */
    @Override
    public void onPersistVisualIndicator(SetIndicatorItem item) {
        if (DBG) android.util.Log.d(TAG, "onPersistVisualIndicator(), item = " + item);
        // ビジュアルインジケータを表示する
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onPersistVisualIndicator();
            }
        }
    }

    /**
     * ビジュアルインジケータ非表示
     */
    @Override
    public void onClearVisualIndicator() {
        if (DBG) android.util.Log.d(TAG, "onClearVisualIndicator()");
        // ビジュアルインジケータを非表示にする
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onClearVisualIndicator();
            }
        }
    }

    /**
     * オーディオインジケータ
     * @param item
     */
    @Override
    public void onPlayAudioIndicator(SetIndicatorItem item) {
        if (DBG) android.util.Log.d(TAG, "onPlayAudioIndicator(), item = " + item);
        // オーディオインジケータが有効なSetIndicatorディレクティブを受信したことを通知
        // -> バースト音の鳴動は、onStartedNotificationToRing()でコールバックされる。
    }

    /**
     * Notification鳴動開始
     */
    @Override
    public void onStartedNotificationToRing() {
        if (DBG) android.util.Log.d(TAG, "onStartedNotificationToRing()");
        // オーディオインジケータのバースト音鳴動開始を通知
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onAudioIndicatorStarted();
            }
        }
    }

    /**
     * Notification鳴動終了
     */
    @Override
    public void onFinishedNotificationToRing() {
        if (DBG) android.util.Log.d(TAG, "onFinishedNotificationToRing()");
        // オーディオインジケータのバースト音鳴動終了を通知
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onAudioIndicatorStopped();
            }
        }
    }

    /**
     * 自動ログイン処理完了フラグ
     * @return
     */
    public boolean isAutoLoginFinished() {
        return this.isAutoLoginFinished;
    }

    /**
     * RecognizeEvent通知専用コールバック
     * (RecognizeEventにはタイムアウトを実装するため、固有のコールバックインスタンスを生成)
     */
    private final AlexaCallback mAlexaRecognizeCallback = new AlexaCallback(Constant.EVENT_RECOGNIZE) {
        /**
         * イベント送信の開始を通知
         * @param call
         */
        @Override
        public void onExecute(Call call) {
            android.util.Log.d(TAG, "*** AlexaCallback[Recognize].onExecute().",new Throwable());
            mCurrentCall = call;
            final Call tempCall = call;
            // 音声入力タイムアウト処理（15秒以内にStopCaptureディレクティブが受信されなかったらキャンセルする。）
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (tempCall == mCurrentCall) {
                        if (mCurrentCall != null && mCurrentCall.isExecuted() == true && mCurrentCall.isCanceled() == false) {
                            // 実行中の場合はキャンセル
                            android.util.Log.d(TAG, "*** AlexaCallback[Recognize].onExecute().cancel()");
                            stopRecording(true);
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
                        mCurrentCall = null;
                    }
                }
            }, 15 * 1000);      // 15秒

            super.onExecute(call);
        }

        /**
         * イベント送信完了
         * @param call
         * @param httpCode
         */
        @Override
        public void onResponse(Call call, int httpCode) {
            if (DBG) android.util.Log.d(TAG, "AlexaCallback[Recognize].onResponse().");
            mCurrentCall = null;
            super.onResponse(call, httpCode);
        }

        /**
         * イベント送信失敗
         * @param call
         * @param e
         */
        @Override
        public void onFailure(Call call, IOException e) {
            if (DBG) android.util.Log.w(TAG, "AlexaCallback[Recognize].onFailure().");
            mCurrentCall = null;
            super.onFailure(call, e);

            // RecognizeでStopCaptureのDirectiveが来なかった場合などのキャンセル処理.
            // 音声入力状態を解除する.
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopRecording(true);
                }
            });
        }
    };

    /**
     * UserInactivityReportイベント送信のコールバック
     * @param item
     */
    @Override
    public void onSendEvent(UserInactivityReportItem item) {
        if (DBG) android.util.Log.d(TAG, "IAlexaUserInactivityReportCallback.onSendEvent().");
    }

    /**
     * UserInactivityReportイベントの送信タイマーリセットのコールバック
     */
    @Override
    public void onResetTimer() {
        if (DBG) android.util.Log.d(TAG, "IAlexaUserInactivityReportCallback.onResetTimer().");
    }

    /**
     * AmazonAlexaManager内のEvent送信のコールバッククラス
     */
    private class AlexaCallback implements AlexaEventManager.AlexaCallback {
        private final String TAG = AlexaCallback.class.getSimpleName();

        private String mSendEventName = null;

        private AlexaCallback(String eventName) {
            this.mSendEventName = eventName;
        }

        /**
         * イベント送信開始
         * @param call
         */
        @Override
        public void onExecute(Call call) {
            if (DBG) android.util.Log.d(TAG, "AlexaCallback.onExecute().");
        }

        /**
         * イベント送信完了
         * @param call
         * @param httpCode
         */
        @Override
        public void onResponse(Call call, final int httpCode) {
            if (DBG) android.util.Log.d(TAG, "AlexaCallback.onResponse().");

            final StringBuffer buffer = new StringBuffer();
            buffer.append(mSendEventName).append(" Event send onResponse : ");
            buffer.append("HttpCode = ").append(httpCode);
            if(mHandler != null){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(buffer.toString());
                    }
                });
            }
            if(httpCode==503){
                isAlexaUnavailable = true;
            }else{
                isAlexaUnavailable = false;
            }
        }

        /**
         * イベント送信失敗
         * @param call
         * @param e
         */
        @Override
        public void onFailure(Call call, IOException e) {
            if (DBG) android.util.Log.w(TAG, "AlexaCallback.onFailure().");
            e.printStackTrace();

            Request request = call.request();
            HttpUrl url = null;
            RequestBody body = null;
            if (request != null) {
                url = request.url();
                body = request.body();
            }
            final StringBuffer buffer = new StringBuffer();
            buffer.append(mSendEventName).append(" Event send onFailed : ");
            buffer.append("url = ").append(url);
            buffer.append(", ");
            buffer.append("body = ").append(body);
            if(mHandler != null){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(buffer.toString());
                    }
                });
            }
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onSystemError();
                }
            }
            if (DBG) android.util.Log.w(TAG, "AlexaCallback.onFailure(), " + buffer.toString());
        }

        @Override
        public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
            if (DBG) android.util.Log.w(TAG, "AlexaCallback.onParsedResponse(), itemList = " + itemList);
        }
    }

    private class SpeakRequestBody extends RequestBody {
        private final String TAG = SpeakRequestBody.class.getSimpleName();
        // バッファサイズ
        int bufferSize = AudioRecord.getMinBufferSize(AUDIO_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        /**
         * コンテントタイプ
         * @return
         */
        @Override
        public MediaType contentType() {
            return MediaType.parse("application/octet-stream");
        }

        /**
         * 音声データ バッファ書き込み
         * @param sink
         * @throws IOException
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (DBG) android.util.Log.d(TAG, "SpeakRequestBody.writeTo() Start");
            while (mAudioRecord != null && mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                // 録音中はループ
                if(mAudioRecord != null) {
                    if(sink != null && mAudioRecord != null) {
                        byte[] buf = new byte[bufferSize];
                        mAudioRecord.read(buf, 0, buf.length);

                        {// TODO デシベル算出処理
                            //エンディアン変換
                            ByteBuffer bf = ByteBuffer.wrap(buf);
                            bf.order(ByteOrder.LITTLE_ENDIAN);
                            short[] s = new short[(int) BUFFER_SIZE];
                            for (int i = bf.position(); i < bf.capacity() / 2; i++) {
                                s[i] = bf.getShort();
                            }

                            int FFT_SIZE = 1024;
                            double DB_BASELINE = Math.pow(2, 15) * FFT_SIZE * Math.sqrt(2);

                            //FFTクラスの作成と値の引き渡し
                            FFT4g fft = new FFT4g(FFT_SIZE);
                            double[] FFTdata = new double[FFT_SIZE];

                            for (int i = 0; i < FFT_SIZE; i++) {
                                FFTdata[i] = (double) s[i];
                            }
                            fft.rdft(1, FFTdata);

                            // デシベルの計算
                            double[] dbfs = new double[FFT_SIZE / 2];
                            double max_db = -120d;
                            int max_i = 0;
                            for (int i = 0; i < FFT_SIZE; i += 2) {
                                dbfs[i / 2] = (int) (20 * Math.log10(Math.sqrt(Math
                                        .pow(FFTdata[i], 2)
                                        + Math.pow(FFTdata[i + 1], 2)) / DB_BASELINE));
                                if (max_db < dbfs[i / 2]) {
                                    max_db = dbfs[i / 2];
                                    max_i = i / 2;
                                }
                            }
                            if (mAlexaCallbackList != null) {
                                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                                    mAlexaCallbackList.get(i).onRecordingMonitor(max_db, max_i);
                                }
                            }
                        }

                        if (false) {
                            StringBuffer stringBuffer = new StringBuffer();
                            for (byte b : buf) {
                                stringBuffer.append(Byte.toString(b));
                            }
                            if (DBG) android.util.Log.d(TAG + "_AudioBytes", stringBuffer.toString());
                        }
                        sink.write(buf, 0, buf.length);
                    }
                }

                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (DBG) android.util.Log.d(TAG, "SpeakRequestBody.writeTo() End");
        }

    };

    /**
     * DownChannel再接続待機時間の計算
     * @param intervalTime
     * @return
     */
    public static long getWaitTimeExp(long intervalTime) {
        intervalTime *= DEFAULT_MULTIPLIER;
        if(intervalTime ==0 ){
            intervalTime = FIRST_TIME;
        }

        if (intervalTime > UPPERLIMIT_TIME) {
            // 上限値より大きかった場合、上限値を設定
            intervalTime = UPPERLIMIT_TIME;
        }
        return intervalTime;
    }


    /**
     * DownChannel接続完了コールバック
     */
    @Override
    public void downChannelOpened() {
        if (DBG) android.util.Log.d(TAG, "DownChannelCallback.downChannelOpened() ");
        mIsDownChannelOpened = true;
    }

    /**
     * DownChannel切断コールバック
     * @param isReconnect   即再接続
     * @param isFinish      アプリ終了による切断
     */
    @Override
    public void downChannelClosed(final boolean isReconnect, final boolean isFinish) {
        if (DBG) android.util.Log.d(TAG, "DownChannelCallback.downChannelClosed() ");
        mIsAlexaConnection = false;
        mIsDownChannelOpened = false;
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onDisConnect();
            }
        }
/*        Revision: 2446
        ネットワーク切断時にはSpeakをキャンセルするがChannelは閉じないように修正。
        ----*/
        //Speakの停止
        //doSpeechCancel();
        AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
        if (speakManager != null) {
            speakManager.onSpeechCancel();
        }
        // isFinishフラグがtrueの場合、再接続しない。（アプリ終了時など）
        if (!isFinish) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    if(isCancelDownChannelConnection == true){
                        // 接続成功
                        return;
                    }
                    // 接続処理リトライ
                    openDownChannel(TokenManager.getToken());
                    //if (DBG) android.util.Log.d(TAG, "downChannelOpen... ");
                    // 次にリトライするまでの待ち時間
//                    waitTime = getWaitTimeExp(waitTime);
//                 android.util.Log.d(TAG, "+++++waitTime, " + waitTime);
//                    mHandler.postDelayed(this, waitTime);
                }
            };

            if (isReconnect) {
                // すぐに接続開始
                mWaitTime = 0;
            }
            else {
                // 待ち時間を挟んで接続
//                waitTime = FIRST_TIME;
                mWaitTime = getWaitTimeExp(mWaitTime);
               // android.util.Log.d(TAG, "++++ waitTime"+waitTime);
            }
        //    android.util.Log.d(TAG, "++++ waitTime"+waitTime);
            // トークン再取得
            AlexaLoginManager loginManager = AlexaLoginManager.getInstance();
            loginManager.refreshToken();
        }
    }

    /**
     * 初期通信完了
     */
    @Override
    public void completeInitializeConnection() {
        if (DBG) android.util.Log.d(TAG, "DownChannelCallback.completeInitializeConnection() ");
        showToast("初期通信完了");
        // DownChannel接続成功
        isCancelDownChannelConnection = false;
        mWaitTime = FIRST_TIME;
        //mIsAlexaConnection = true;
        // 接続完了を通知
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                mAlexaCallbackList.get(i).onConnect();
            }
        }
        // 待機中のリトライ処理を解除
        mHandler.removeCallbacks(runnable);

        if (DBG) android.util.Log.d(TAG, "Dis runnable... ");
        mIsInitComp = true;
        // SoftwareInfoイベント送信
        sendSoftwareInfo();

        // Ping送信処理開始
        sendPing();

        AlexaAlertManager alertManager = AlexaAlertManager.getInstance();
        alertManager.retrySendAlertEvent();

        // UserInactivityReportイベント送信タイマー開始
        AlexaUserInactivityReportManager userInactivityReportManager = AlexaUserInactivityReportManager.getInstance();
        userInactivityReportManager.startTimer();
    }

    /**
     * トースト表示
     * @param message
     */
    private void showToast(final String message) {
        if (DBG) {
            android.util.Log.d(TAG, message);
            if (ToastDBG) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    /**
     * 音声、音楽の再生状態コールバックリスナー
     */
    private class AlexaAudioListener implements IAudioCallback {

        /**
         * バッファリング開始
         * @param item
         */
        @Override
        public void onPrepare(AlexaIfItem item) {
            if (item instanceof PlayItem) {
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onAudioPrepare();
                    }
                }
            } else if (item instanceof SpeakItem) {
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onSpeakingPrepare();
                    }
                }
            }
        }

        /**
         * バッファリング完了
         */
        @Override
        public void onPrepared(AlexaIfItem item) {
            if (item instanceof PlayItem) {
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onAudioPrepared();
                    }
                }
            } else if (item instanceof SpeakItem) {
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onSpeakingPrepared();
                    }
                }
            }
        }

        /**
         * 再生開始
         * @param item
         */
        @Override
        public void onPlay(final AlexaIfItem item) {
            if (DBG) android.util.Log.d(TAG, "AlexaAudioListener.onPlay(), " + item);
            {
                String name = item != null ? item.getNamespace() : "NULL";
                showToast("onPlay : " + name);
            }
            IAlexaPlayer player = null;
            IAlexaPlayer exoPlayer = null;
            if (item instanceof PlayItem) {
                AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
                exoPlayer = audioManager.getAlexaPlayer();

                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onAudioStart();
                    }
                }            }
            else if (item instanceof SpeakItem) {
                AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
                player = speakManager.getAlexaPlayer();

                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onSpeakingStart();
                    }
                }
            }
            if (player != null) {
                final IAlexaPlayer aPlayer = player;
                if (player != null&&aPlayer.isPlaying()) {
                    // 画面表示の更新
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(item instanceof PlayItem) {
                                int current = aPlayer.getCurrentPosition();
/*                                if (AlexaFragment.mCurrentText != null) {
                                    AlexaFragment.mCurrentText.setText(convertTime(current));
                                }
                                if (AlexaFragment.mDurationText != null) {
                                    int duration = aPlayer.getDuration();
                                    //Nexus5xではduration = -1が帰ってきていたが、Galaxy　S8では0が帰ってきていた
                                    //if (duration = 0) {
                                    if (duration <= 0) {
                                        AlexaFragment.setMediaProgress(View.GONE);
                                    } else {
                                        //音楽のDurationだけ見たい
//                                    if(item instanceof PlayItem){
                                        AlexaFragment.mDurationText.setText(convertTime(getDifferenceTime(current, duration)));
                                        AlexaFragment.setMediaProgress(View.VISIBLE);
                                        //}

                                    }
                                }
                                if (AlexaFragment.mProgressBar != null) {
                                    int duration = aPlayer.getDuration();
                                    AlexaFragment.mProgressBar.setProgress(current);
                                    AlexaFragment.mProgressBar.setMax(duration);
                                }*/
                                if (mAlexaCallbackList != null) {
                                    int duration = aPlayer.getDuration();
                                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                                        mAlexaCallbackList.get(i).onAudioUpdateProgress(current/1000, duration/1000);
                                    }
                                }
                            }
                        }
                    });
                }
            }
            else if (exoPlayer != null&&exoPlayer.isPlaying()) {
                final IAlexaPlayer aPlayer = exoPlayer;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(item instanceof PlayItem) {
                            int current = aPlayer.getCurrentPosition();
                           /* if (AlexaFragment.mCurrentText != null) {
                                AlexaFragment.mCurrentText.setText(convertTime(current));
                            }
                            if (AlexaFragment.mDurationText != null) {
                                int duration = aPlayer.getDuration();
                                if (duration <= 1) {    // TODO:ExoPlayerを使用すると、Streaminng時は「1」が来る
                                    AlexaFragment.setMediaProgress(View.GONE);
                                } else {
                                    //音楽のDurationだけ見たい
                                    AlexaFragment.mDurationText.setText(convertTime(getDifferenceTime(current, duration)));
                                    AlexaFragment.setMediaProgress(View.VISIBLE);

                                }
                            }
                            if (AlexaFragment.mProgressBar != null) {
                                int duration = aPlayer.getDuration();
                                AlexaFragment.mProgressBar.setProgress(current);
                                AlexaFragment.mProgressBar.setMax(duration);
                            }*/
                            if (mAlexaCallbackList != null) {
                                int duration = aPlayer.getDuration();
                                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                                    mAlexaCallbackList.get(i).onAudioUpdateProgress(current/1000, duration/1000);
                                }
                            }
                        }
                    }
                });
            }
        }

        /**
         * 一時停止
         * @param item
         */
        @Override
        public void onPause(AlexaIfItem item) {
            if (DBG) android.util.Log.d(TAG, "AlexaAudioListener.onPause(), " + item);
            {
                String name = item != null ? item.getNamespace() : "NULL";
                showToast("onPause : " + name);
            }
            if (item instanceof PlayItem) {
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onAudioPause();
                    }
                }
            } else if (item instanceof SpeakItem) {
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onSpeakingPause();
                    }
                }
            }
        }

        /**
         * 再生再開
         * @param item
         */
        public void onResume(AlexaIfItem item) {
            if (DBG) android.util.Log.d(TAG, "AlexaAudioListener.onResume(), " + item);
            {
                String name = item != null ? item.getNamespace() : "NULL";
                showToast("onResume : " + name);
            }
            IAlexaPlayer player = null;
            IAlexaPlayer exoPlayer = null;
            if (item instanceof PlayItem) {
                AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
                exoPlayer = audioManager.getAlexaPlayer();
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onAudioResume();
                    }
                }
            }
            else if (item instanceof SpeakItem) {
                AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
                player = speakManager.getAlexaPlayer();
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onSpeakingResume();
                    }
                }            }


            if (player != null&&player.isPlaying()) {
                final IAlexaPlayer aPlayer = player;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        int current = aPlayer.getCurrentPosition();
                        int duration = aPlayer.getDuration();
 /*                       if (AlexaFragment.mDurationText != null) {
                            AlexaFragment.mDurationText.setText(convertTime(getDifferenceTime(current, duration)));
                        }
                        if (AlexaFragment.mProgressBar != null) {
                            AlexaFragment.mProgressBar.setMax(duration);
                        }*/
                        if (mAlexaCallbackList != null) {
                            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                                mAlexaCallbackList.get(i).onAudioUpdateProgress(current / 1000, duration / 1000);
                            }
                        }
                    }
                });
            }
            else if (exoPlayer != null&&exoPlayer.isPlaying()) {
                final IAlexaPlayer aPlayer = exoPlayer;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        int current = aPlayer.getCurrentPosition();
                        int duration = aPlayer.getDuration();
/*                        if (AlexaFragment.mDurationText != null) {
                            AlexaFragment.mDurationText.setText(convertTime(getDifferenceTime(current, duration)));
                        }
                        if (AlexaFragment.mProgressBar != null) {
                            AlexaFragment.mProgressBar.setMax(duration);
                        }*/
                        if (mAlexaCallbackList != null) {
                            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                                mAlexaCallbackList.get(i).onAudioUpdateProgress(current / 1000, duration / 1000);
                            }
                        }
                    }
                });
            }
        }

        private long getDifferenceTime(long current, long duration) {
            long difference = duration - current;
            return difference;
        }

        private String convertTime(long millisecond) {
            SimpleDateFormat sdf = null;
            if (millisecond >= 1000 * 60 * 60) {
                // 1時間以上
                sdf = new SimpleDateFormat("H:mm:ss");
            }
            else {
                if (millisecond < 0) {
                    // 時間がマイナスの場合は、0に置き換える
                    millisecond = 0;
                }
                sdf = new SimpleDateFormat("m:ss");
            }
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.format(millisecond);
        }

        /**
         * 再生停止
         * @param item
         */
        public void onStop(AlexaIfItem item) {
            if (DBG) android.util.Log.d(TAG, "AlexaAudioListener.onStop(), " + item);
            {
                String name = item != null ? item.getNamespace() : "NULL";
                showToast("onStop : " + name);
            }
            if (item instanceof PlayItem) {
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onAudioStop();
                    }
                }
            } else if (item instanceof SpeakItem) {
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onSpeakingStop();
                    }
                }
            }
        }

        /**
         * 再生完了
         * @param item
         */
        @Override
        public void onComplete(AlexaIfItem item) {
            if (DBG) android.util.Log.d(TAG, "AlexaAudioListener.onComplete(), " + item);
            {
                String name = item != null ? item.getNamespace() : "NULL";
                showToast("onComplete : " + name);
            }
            if (item instanceof PlayItem) {
                // 音楽再生完了をコールバック
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onAudioComplete();
                    }
                }
            } else if (item instanceof SpeakItem) {
                // 音声再生完了をコールバック
                if (mAlexaCallbackList != null) {
                    for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                        mAlexaCallbackList.get(i).onSpeakingComplete();
                    }
                }
            }
        }
        @Override
        public void onAdjustVolume(float v){
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onAdjustVolume(v);
                }
            }
        }

        @Override
        public void onSetVolume(float volume){
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onSetVolume(volume);
                }
            }
        }
        @Override
        public void onSetMute(boolean isMute){
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onSetMute(isMute);
                }
            }
        }



        /**
         * エラー発生
         * @param item
         */
        @Override
        public void onError(AlexaIfItem item) {
            if (DBG) android.util.Log.d(TAG, "AlexaAudioListener.onError(), " + item);
            {
                String name = item != null ? item.getNamespace() : "NULL";
                showToast("onError : " + name);
            }
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onAudioError();
                }
            }
        }

        /**
         * 再生位置更新
         * @param item
         * @param position
         */
        @Override
        public void onUpdateProgress(AlexaIfItem item, final long position) {
            if (DBG) android.util.Log.d(TAG, "AlexaAudioListener.onUpdateProgress(), item = " + item + ", position = " + position);

            if (item instanceof PlayItem) {
                AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
                IAlexaPlayer player = audioManager.getAlexaPlayer();
                if (player != null&&player.isPlaying()) {
                    final int current = player.getCurrentPosition();
                    final int duration = player.getDuration();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

/*                             if (AlexaFragment.mProgressBar != null) {
                                AlexaFragment.mProgressBar.setProgress((int) position);
                            }
                            if (AlexaFragment.mCurrentText != null) {
                                AlexaFragment.mCurrentText.setText(convertTime(current));
                            }
                            if (AlexaFragment.mDurationText != null) {
                                if (AlexaFragment.mDurationText != null) {
                                    AlexaFragment.mDurationText.setText(convertTime(getDifferenceTime(current, duration)));
                                }
                            }*/
                            if (mAlexaCallbackList != null) {
                                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                                    mAlexaCallbackList.get(i).onAudioUpdateProgress((int) position/1000, duration/1000);
                                }
                            }
                        }
                    });
                }
            }
        }

        /**
         * 再生可能状態取得
         *  -> 再生可能な状態であれば、trueを返す
         * @return
         */
        @Override
        public boolean isAlexaPlayable() {
            boolean result = true;
            // TODO:排他制御を行う必要があるAlexa以外の機能が無い場合、デフォルトでtrue
            return result;
        }

        @Override
        public void onNoResponse() {
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onNoResponse();
                }
            }
        }

        @Override
        public void onNoDirectiveAtSendEventResponse() {
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onNoDirectiveAtSendEventResponse();
                }
            }
        }

        @Override
        public void onDecodeStart(){
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onDecodeStart();
                }
            }
        }

        @Override
        public void onDecodeFinish(){
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onDecodeFinish();
                }
            }
        }

        @Override
        public void onWLAudioFocusLoss() {
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onWLAudioFocusLoss();
                }
            }
        }
    }

    @Override
    public void onChannelActiveChange(AlexaQueueManager.AlexaChannel channel, boolean isActive) {
        if (mAlexaCallbackList != null) {
            for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                // フォアグラウンドチャネル変更を通知
                mAlexaCallbackList.get(i).onChannelActiveChange(channel, isActive);
            }
        }
    }

    /**
     * 音声入力ON効果音の再生完了リスナー
     */
    private MediaPlayer.OnCompletionListener mSoundListener =  new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mp == mOffPlayer) {
                //なにもしない
            } else if (mp == mOnPlayer) {
                if(mAudioRecord != null) {
                    android.util.Log.d(TAG, "RecoordingStart");
                    // 効果音の再生完了を待ってから録音開始
                    mAudioRecord.startRecording();
                }

            }
        }
    };

    /**
     * 音声入力ON効果音の再生完了リスナー
     */
    private WLPlayer.IOnCompletionListener mWLSoundListener =  new WLPlayer.IOnCompletionListener() {
        @Override
        public void onCompletion(WLPlayer mp) {
            if (mp == mOffWLPlayer) {
                //なにもしない
            } else if (mp == mOnWLPlayer) {
                if(mAudioRecord != null) {
                    android.util.Log.d(TAG, "RecoordingStart - WLPlayer");
                    // 効果音の再生完了を待ってから録音開始
                    mAudioRecord.startRecording();
                }

            }
        }
    };

    /**
     * ログインコールバックリスナー
     */
    private class AlexaLoginCallback implements AlexaLoginManager.IAlexaLoginCallback {

        /**
         * ログイン成功
         * @param accessToken
         */
        @Override
        public void onLoginSuccess(String accessToken) {
            showToast("AlexaLoginCallback#onLoginSuccess()");
            TokenManager.setToken(accessToken);
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onLoginSuccess();
                }
            }
        }

        /**
         * ログイン失敗（トークン取得失敗）
         */
        @Override
        public void onLoginFailed() {
            showToast("AlexaLoginCallback#onLoginFailed()");
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onLoginFailed();
                }
            }
        }

        /**
         * ログイン失敗（エラー等）
         */
        @Override
        public void onLoginError() {
            showToast("AlexaLoginCallback#onLoginError()");
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onLoginFailed();
                }
            }
        }

        /**
         * 自動ログイン成功（前回ログイン時情報の復帰）
         * @param accessToken
         */
        @Override
        public void onAutoLoginSuccess(String accessToken) {
            showToast("AlexaLoginCallback#onAutoLoginSuccess()");
            isAutoLoginFinished = true;

            TokenManager.setToken(accessToken);
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onLoginSuccess();
                }
            }
            // 車載機接続
/*            SupportActivity activity = mActivity;
            if (activity instanceof WebLinkConnectionActivity) {
                ((WebLinkConnectionActivity) activity).doConnectionUT();
            }*/
        }

        /**
         * 自動ログイン失敗（トークン取得失敗）
         */
        @Override
        public void onAutoLoginFailed() {
            showToast("AlexaLoginCallback#onAutoLoginFailed()");
            isAutoLoginFinished = true;

            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onLoginFailed();
                }
            }
            // 車載機接続
/*            SupportActivity activity = mActivity;
            if (activity instanceof WebLinkConnectionActivity) {
                ((WebLinkConnectionActivity) activity).doConnectionUT();
            }*/
        }

        /**
         * 自動ログイン失敗（エラー等）
         */
        @Override
        public void onAutoLoginError() {
            showToast("AlexaLoginCallback#onAutoLoginError()");
            isAutoLoginFinished = true;

            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onLoginFailed();
                }
            }
            // 車載機接続
            SupportActivity activity = mActivity;
/*            if (activity instanceof WebLinkConnectionActivity) {
                ((WebLinkConnectionActivity) activity).doConnectionUT();
            }*/
        }

        /**
         * ログアウト成功
         */
        @Override
        public void onAlexaLogoutSuccess() {
            showToast("AlexaLoginCallback#onAlexaLogoutSuccess()");
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onLogout();
                }
            }
        }

        /**
         * ログアウト失敗
         */
        @Override
        public void onAlexaLogoutFailed() {
            showToast("AlexaLoginCallback#onAlexaLogoutFailed()");
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onLogout();
                }
            }
        }

        /**
         * トークン再取得（DownChannelの再接続時などに使用）
         * @param accessToken
         */
        @Override
        public void onRefreshToken(String accessToken) {
            showToast("AlexaLoginCallback#onRefreshToken()");
            //android.util.Log.w(TAG, "++++++ RefreshToken");
            // トークンはよく使うのでsetしておく.
            TokenManager.setToken(accessToken);
            mHandler.postDelayed(runnable, mWaitTime);
//            waitTime = FIRST_TIME;
        }

        /**
         * トークン再取得失敗
         */
        @Override
        public void onRefreshTokenError() {
            showToast("AlexaLoginCallback#onRefreshTokenError()");
            AlexaLoginManager loginManager = AlexaLoginManager.getInstance();
            loginManager.doLogin();
        }
        @Override
        public void onCapabilitiesSendSuccess() {
            showToast("AlexaLoginCallback#onCapabilitiesSendSuccess()");
            if (mAlexaCallbackList != null) {
                for (int i = 0; i < mAlexaCallbackList.size(); ++i) {
                    mAlexaCallbackList.get(i).onCapabilitiesSendSuccess();
                }
            }
        }
    
    }

    /* -- Android AudioFocus-- */
    public boolean hasAudioFocus() {
        if (DBG) android.util.Log.d(TAG, "hasAudioFocus(), hasAudioFocus = " + mHasAudioFocus);
        return mHasAudioFocus;
    }

    public void requestAudioFocus() {
        if (DBG) android.util.Log.d(TAG, "abandonAudioFocus()");

        if (!hasAudioFocus()) {
            int result;
            if (Build.VERSION.SDK_INT >= 26) {
                result = requestAudioFocus_NewApi();
            } else {
                result = requestAudioFocus_OldApi();
            }

            if (DBG) android.util.Log.d(TAG, " -- requestAudioFocus(), result = " + result);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // フォーカス取得成功
                mHasAudioFocus = true;
            }
        }
    }

    public void abandonAudioFocus() {
        if (DBG) android.util.Log.d(TAG, "abandonAudioFocus()");

        if (hasAudioFocus()) {
            int result;
            if (Build.VERSION.SDK_INT > 26) {
                result = abandonAudioFocus_NewApi();
            } else {
                result = abandonAudioFocus_OldApi();
            }

            if (DBG) android.util.Log.d(TAG, " -- abandonAudioFocus(), result = " + result);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // フォーカス取得成功
                mHasAudioFocus = false;
            }
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (DBG) android.util.Log.d(TAG, "onAudioFocusChange(), focusChange = " + focusChange);

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                abandonAudioFocus();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                break;
            }
        }

    }

    @RequiresApi(26)
    private int requestAudioFocus_NewApi() {
        AudioAttributes audioAttribute = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .setAudioAttributes(audioAttribute)
                .build();
        return mAudioManager.requestAudioFocus(mAudioFocusRequest);
    }

    @SuppressWarnings("deprecation")
    private int requestAudioFocus_OldApi() {
        return mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }


    @RequiresApi(26)
    private int abandonAudioFocus_NewApi() {
        return mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
    }

    @SuppressWarnings("deprecation")
    private int abandonAudioFocus_OldApi() {
        return mAudioManager.abandonAudioFocus(this);
    }
}
