package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.PermissionChecker;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AlexaPresenter;
import jp.pioneer.carsync.presentation.view.AlexaView;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechSynthesizer.SpeakItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.CustomVoiceChromeView;
import jp.pioneer.mbg.alexa.manager.AlexaQueueManager;
import jp.pioneer.mbg.alexa.manager.AlexaSpeakManager;
import timber.log.Timber;

/**
 * Alexa関連のUIを制御するクラス.
 */
public class AlexaFragment extends AbstractDialogFragment<AlexaPresenter, AlexaView, AlexaFragment.Callback>
        implements AlexaView {
    @Inject AlexaPresenter mPresenter;
    @BindView(R.id.alexa_start_button_group) RelativeLayout mAlexaBtnGroup;
    @BindView(R.id.alexa_start_button) ImageView mAlexaBtn;
    @BindView(R.id.alexa_notification_circle) ImageView mAlexaNotification;
    @BindView(R.id.alexa_voice_chrome_large) CustomVoiceChromeView mVoiceChrome;
    @BindView(R.id.state_text_view) TextView mStateTextView;
    @BindView(R.id.close_button) ImageView mCloseBtn;
    private Unbinder mUnbinder;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mOrientation;
    private static final int IDLE_TIME = 1000;
    private boolean isThinking = false;
    private boolean isPersistIndicator = false;
    private boolean isSpeaking = false;
    private boolean isAudioPlay = false;
    /** Alexaマネージャ. */
    AmazonAlexaManager mAmazonAlexaManager;
    /** 音声入力画面制御. */
    private AlexaCommunicationLayoutHandler mCommunicationLayoutHandler = null;
    private AlexaCallback mAlexaCallback = new AlexaCallback();
    private boolean mIsBack = false;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            callbackClose();
        }
    };
    /**
     * コンストラクタ
     */
    public AlexaFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param args   引き継ぎ情報
     * @return AlexaFragment
     */
    public static AlexaFragment newInstance(android.support.v4.app.Fragment target, Bundle args) {
        AlexaFragment fragment = new AlexaFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.BehindScreenStyle);
        dialog.setCancelable(false);
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (getCallback() != null) {
                    getCallback().onClose(this);
                } else {
                    this.dismiss();
                }
                return true;
            }
            return false;
        });
        return dialog;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof Callback;
    }

    @NonNull
    @Override
    protected AlexaPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void callbackClose() {
        if(!isSpeaking&&!isAudioPlay) {
            getPresenter().changePreviousSource();
        }
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }

    /**
     * ダイアログ終了通知interface
     */
    public interface Callback {
        /**
         * ダイアログ終了通知
         *
         * @param fragment 終了ダイアログ
         */
        void onClose(AlexaFragment fragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alexa_screen, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        Configuration config = getResources().getConfiguration();
        mOrientation = config.orientation;
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate");
        mIsBack = false;
        super.setPauseHide(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart");
        initView();
        isSpeaking = false;
        isPersistIndicator = false;
        mAmazonAlexaManager = AmazonAlexaManager.getInstance();
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.addAlexaCallback(mAlexaCallback);
            mAmazonAlexaManager.isShowAlexaDialog = true;
        }
        mCommunicationLayoutHandler.defaultVoiceChromeStatus();
        clickAlexa();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        if(mIsBack) {
            callbackClose();
            return;
        }
    }

    @Override
    public void onPause() {
        Timber.d("onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Timber.d("onStop");
        mIsBack=true;

        if (mAmazonAlexaManager != null) {
            if (mAmazonAlexaManager.isRecording()) {
                mAmazonAlexaManager.doRecordingCancel();
            }
            mAmazonAlexaManager.removeAlexaCallback(mAlexaCallback);
            mAmazonAlexaManager.isShowAlexaDialog = false;
        }
        mHandler.removeCallbacks(mRunnable);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isAudioPlay) {
            if (mAmazonAlexaManager != null) {
                mAmazonAlexaManager.doSpeechCancel();
            }
        } else {
            //Speakの停止
            AlexaSpeakManager manager = AlexaSpeakManager.getInstance();
            if (manager != null) {
                manager.speechStop();
            }
        }
    }

    /**
     * Viewを初期化するメソッド.
     */
    private void initView() {
        mCommunicationLayoutHandler = new AlexaCommunicationLayoutHandler();
    }

    @OnClick(R.id.alexa_start_button)
    public void onClickAlexaBtn() {
        clickAlexa();
    }

    @OnClick(R.id.close_button)
    public void onClickDismissBtn() {
        mCommunicationLayoutHandler.clickClose();
/*        if (mAmazonAlexaManager.isRecording()) {
            mAmazonAlexaManager.doRecordingCancel();
        }*/
        //callbackClose();
    }

    @Override
    public void setNotificationQueuedState(boolean notification) {
        isPersistIndicator= notification;
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
        public void onCapabilitiesSendSuccess() {

        }

        @Override
        public void onConnect() {
            Timber.d("onConnect");
            mCommunicationLayoutHandler.defaultVoiceChromeStatus();
        }

        @Override
        public void onDisConnect() {
            Timber.d("onDisConnect");
            //mCommunicationLayoutHandler.defaultVoiceChromeStatus();
        }

        @Override
        public void onNetworkConnect() {
            Timber.d("onNetworkConnect");
            mCommunicationLayoutHandler.defaultVoiceChromeStatus();
        }

        @Override
        public void onNetworkDisconnect() {
            Timber.d("onNetworkConnect");
            //mCommunicationLayoutHandler.defaultVoiceChromeStatus();
        }

        @Override
        public void onRecordingStart() {
            Timber.d("onRecordingStart");
            mHandler.removeCallbacks(mRunnable);
            mCommunicationLayoutHandler.startRecognize();
            mCommunicationLayoutHandler.setLargeVoiceChromeStatus(CustomVoiceChromeView.VoiceChromeType.LISTENING);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mStateTextView.setText(R.string.alx_001);
                }
            });
        }

        @Override
        public void onRecordingMonitor(double db, int hertz) {
            //Timber.d("onRecordingMonitor db = " + db + ", hz = " + hertz);
            mVoiceChrome.setVoiceLevel(db);
        }

        @Override
        public void onRecordingStop(boolean isCancel) {
            Timber.d("onRecordingStop isCancel = " + isCancel);
            AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
            ArrayList<AlexaIfDirectiveItem> speakPlaybackList = null;
            if (speakManager != null) {
                speakPlaybackList = speakManager.getPlaybackList();
            }
            if (isCancel) {
                // キャンセルの場合は状態をクリア.
                mCommunicationLayoutHandler.defaultVoiceChromeStatus();
                Timber.d( "********* onRecordingStop isCancel = true");
                if(speakPlaybackList==null)return;
                if (speakPlaybackList.size() == 0) {
                    mCommunicationLayoutHandler.stopDialogChannel();
                } else if (speakPlaybackList.size() > 0) {
                    mCommunicationLayoutHandler.setLargeVoiceChromeStatus(CustomVoiceChromeView.VoiceChromeType.SPEAKING);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAlexaBtnGroup.setVisibility(View.VISIBLE);
                            mStateTextView.setText("");
                        }
                    });
                }
                //StopCaptureからCancelする場合Elseにはいる
            } else {
                // 録音が終わったらThinking状態に移行する.
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAlexaBtnGroup.setVisibility(View.VISIBLE);
                        mStateTextView.setText(R.string.alx_002);
                    }
                });
                mCommunicationLayoutHandler.setLargeVoiceChromeStatus(CustomVoiceChromeView.VoiceChromeType.THINKING);
            }
        }

        @Override
        public void onSpeakingPrepare() {

        }

        @Override
        public void onSpeakingPrepared() {

        }


        @Override
        public void onSpeakingStart() {
            Timber.d("onSpeakingStart");
            mHandler.removeCallbacks(mRunnable);
            //mCommunicationLayoutHandler.startRecognize();
            isSpeaking = true;
            // Speaking状態に移行する.
            mCommunicationLayoutHandler.defaultVoiceChromeStatus();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlexaBtnGroup.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onSpeakingResume() {
            Timber.d("onSpeakingResume");
            isSpeaking = true;
        }

        @Override
        public void onSpeakingPause() {
            Timber.d("onSpeakingPause");
            isSpeaking = false;
        }

        @Override
        public void onSpeakingStop() {
            Timber.d("onSpeakingStop");
            isSpeaking = false;
        }

        @Override
        public void onSpeakingComplete() {
            Timber.d("onSpeakingComplete");
            isSpeaking = false;
        }

        @Override
        public void onReceiveRenderPlayerInfo(RenderPlayerInfoItem playerInfoItem) {
            Timber.d("onReceiveRenderPlayerInfo");
            mCommunicationLayoutHandler.defaultVoiceChromeStatus();
            getPresenter().setPlayInfo(playerInfoItem);
            isAudioPlay = true;
        }

        @Override
        public void onAudioPrepare() {
            Timber.d("onAudioPrepare");
            isAudioPlay = true;
        }

        @Override
        public void onAudioPrepared() {
            Timber.d("onAudioPrepared");
            isAudioPlay = true;
        }

        @Override
        public void onAudioStart() {
            Timber.d("onAudioStart");
            isAudioPlay = true;
            getPresenter().onAudioPlay();
        }

        @Override
        public void onAudioResume() {

        }

        @Override
        public void onAudioPause() {

        }

        @Override
        public void onAudioStop() {

        }

        @Override
        public void onAudioError() {
            Timber.d("onAudioError");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCommunicationLayoutHandler.stopDialogChannel();
                }
            });
        }

        @Override
        public void onAudioComplete() {

        }

        @Override
        public void onAudioUpdateProgress(int current, int duration) {

        }

        @Override
        public void onSystemError() {
            Timber.d("onSystemError");
/*            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mStateTextView.setText(R.string.alx_005);
                    mVoiceChrome.setVisibility(View.VISIBLE);
                }
            });
            mCommunicationLayoutHandler.setLargeVoiceChromeStatus(CustomVoiceChromeView.VoiceChromeType.SYSTEM_ERROR);*/
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
            Timber.d("onPersistVisualIndicator");
            isPersistIndicator = true;
        }

        @Override
        public void onClearVisualIndicator() {
            Timber.d("onClearVisualIndicator");
            isPersistIndicator = false;
            mCommunicationLayoutHandler.defaultVoiceChromeStatus();
        }

        @Override
        public void onAudioIndicatorStarted() {
            Timber.d("onAudioIndicatorStarted");
            //どの状態でも割り込む
            //if (!isSpeaking) {
                mCommunicationLayoutHandler.setLargeVoiceChromeStatus(CustomVoiceChromeView.VoiceChromeType.NOTIFICATIONS);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mStateTextView.setText(R.string.alx_003);
                        mVoiceChrome.setVisibility(View.VISIBLE);
                    }
                });
            //}
        }

        @Override
        public void onAudioIndicatorStopped() {
            Timber.d("onAudioIndicatorStopped");
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
            Timber.d("onNoResponse");
            //↓Recognize中にExpectSpeech来ると閉じちゃうから
            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            if (queueManager.isRec()) {
                return;
            }

            {
                if (queueManager.hasDialogChannelDirective()) {
                    // Dialogチャネルのディレクティブが残っている
                    return;
                }
                AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
                ArrayList<AlexaIfDirectiveItem> list = speakManager.getPlaybackList();
                if (list != null && list.size() > 0) {
                    boolean hasSpeakItem = false;
                    for (AlexaIfDirectiveItem item : list) {
                        // Dialogチャネルのディレクティブが残っているかチェック
                        if (item instanceof SpeakItem) {
                            hasSpeakItem = true;
                            break;
                        }
                    }
                    if (hasSpeakItem) {
                        return;
                    }
                }
            }
            mCommunicationLayoutHandler.stopDialogChannel();
        }

        @Override
        public void onChannelActiveChange(AlexaQueueManager.AlexaChannel channel, boolean isActive) {
            Timber.d("onChannelChange() - afterChannel = " + channel + "isAvtive = " + isActive);
            if (!isActive && channel == AlexaQueueManager.AlexaChannel.DialogChannel) {
                // Dialogチャネルが非アクティブ
                mCommunicationLayoutHandler.stopDialogChannel();
                mHandler.postDelayed(mRunnable,IDLE_TIME);
            }
            if(isActive&&channel==AlexaQueueManager.AlexaChannel.ContentChannel){
                isAudioPlay = true;
            }
        }

        @Override
        public void onMicrophonePermission(int state) {
            Timber.d("onMicrophonePermission(), state = " + state);
            // マイクパーミッション状態
            if (state == PermissionChecker.PERMISSION_GRANTED) {
                if (mAmazonAlexaManager != null) {
                    mAmazonAlexaManager.startRecording();
                }
            } else {
                Toast.makeText(getActivity(), "Microphone Permission is DENIED.", Toast.LENGTH_SHORT).show();
                callbackClose();
            }
        }

        @Override
        public void onNoDirectiveAtSendEventResponse() {

        }

        @Override
        public void onSetNaviDestination(Double latitude, Double longitude, String name) {

        }

        @Override
        public void onRecordingNotAvailable() {
        }
        @Override
        public void onWLAudioFocusLoss() {
            // 閉じるボタン押下に準拠
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    // 音楽再生画面の状態だと、音声認識キャンセルでAlexaアイコンを小さい状態に戻そうとするので、
                    // 音楽再生画面を先に閉じる。
/*                    if (mAvLayoutHandler != null && mAvLayoutHandler.isAlexaScreen) {
                        // 音楽再生画面状態を閉じる
                        mAvLayoutHandler.clickClose();
                    }*/
                    Timber.d("onWLAudioFocusLoss() ");
                    if (mCommunicationLayoutHandler != null && mCommunicationLayoutHandler.mmIsRecognizeMode) {
                        // 音声認識を閉じる
                        isSpeaking = false;
                        mCommunicationLayoutHandler.clickClose();
                    }
                }
            });
        }

        @Override
        public void onDecodeStart() {

        }

        @Override
        public void onDecodeFinish() {

        }
    }

    @Override
    public void setVoiceCommand() {
        if (mAlexaBtnGroup.getVisibility() == View.VISIBLE) {
            clickAlexa();
        }
    }

    /**
     * 音声認識開始ボタン.
     */
    public void clickAlexa() {
        mHandler.removeCallbacks(mRunnable);
        if(AmazonAlexaManager.isAlexaUnavailable){
            //Alexa Service Unavailable
            mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.SYSTEM_ERROR);
            mStateTextView.setText(R.string.alx_006);
            return;
        }
        if (!AmazonAlexaManager.mIsAlexaConnection) {
            //システムエラー(Alexaに接続されていない)
            mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.SYSTEM_ERROR);
            mStateTextView.setText(R.string.alx_005);
            return;
        }
        if (mAmazonAlexaManager.isRecording()) {
            mAmazonAlexaManager.doRecordingCancel();
        } else {
            mAmazonAlexaManager.doRecordingStart();
        }
    }

    /**
     * Alexaのアイコンの表示を切り替えるメソッド.
     *
     * @param visibility 可視性
     */
    public void setAlexaIconVisibility(final int visibility) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAlexaBtnGroup.setVisibility(visibility);
            }
        });
    }

    /**
     * =============================================================================================
     * 音声対話中の状態を制御する.
     * =============================================================================================
     */
    private class AlexaCommunicationLayoutHandler implements OnClickListener {

        /** 音声入力状態かを管理するフラグ. */
        boolean mmIsRecognizeMode = false;

        public AlexaCommunicationLayoutHandler() {
            init();
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.alexa_start_button) {
                clickAlexa();
            }
        }

        /**
         * 初期化設定.
         */
        private void init() {
            //mAlexaBtn.setOnClickListener(this);
        }

        public void clickClose() {
            AmazonAlexaManager amazonAlexaManager = AmazonAlexaManager.getInstance();
/*        if (mAvLayoutHandler.isAlexaScreen) {
            mCommunicationLayoutHandler.toSmallChrome();
        }*/
            AlexaSpeakManager speakManager = AlexaSpeakManager.getInstance();
            if (speakManager.getPlaybackList().size() > 0) {
                speakManager.onSpeechCancel();
            }
            mCommunicationLayoutHandler.stopDialogChannel();

            if (amazonAlexaManager.isRecording()) {
                amazonAlexaManager.doRecordingCancel();
                amazonAlexaManager.doSpeechCancel();
                Timber.d("*** doRecCancel");
            } else {
                amazonAlexaManager.doSpeechCancel();
                amazonAlexaManager.doRecordingCancel();
                Timber.d("*** doSpeechCancel");
            }
            //閉じる
            callbackClose();
        }

        /**
         * ダイアログチャネルの開始時のレイアウト処理.
         * ・右下のアイコンの消去
         * ・背景の有効化
         * ・拡大アニメーション
         */
        public void startRecognize() {
            if (mmIsRecognizeMode) {
                //ExpectSpeechでstopRecognizeがこない
                //return;
            } else {
                mmIsRecognizeMode = true;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVoiceChrome.setVisibility(View.VISIBLE);
                    mAlexaBtnGroup.setVisibility(View.INVISIBLE);
                }
            });
        }

        /**
         * ダイアログチャネルの終了時のレイアウト処理.
         * ・右下のアイコンの復活
         * ・背景の無効化
         * ・縮小アニメーション
         */
        public void stopDialogChannel() {
            Timber.d("stopDialogChannel mmIsRecognizeMode = " + mmIsRecognizeMode);
            mmIsRecognizeMode = false;
            mCommunicationLayoutHandler.defaultVoiceChromeStatus();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlexaBtnGroup.setVisibility(View.VISIBLE);
                }
            });
        }

        /**
         * VoiceChromeの状態を変更するメソッド.
         */
        private void setLargeVoiceChromeStatus(final CustomVoiceChromeView.VoiceChromeType type) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVoiceChrome != null) {
                        mVoiceChrome.setVoiceChromeType(type);
                    }
                }
            });
        }

        /**
         * VoiceChromeの状態を戻すメソッド.
         */
        private void defaultVoiceChromeStatus() {
            Timber.d("defaultVoiceChromeStatus");
            isThinking = false;
            Activity activity = getActivity();
            if (activity != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        View rootView = getView();
                        if (rootView != null) {
                            if (mVoiceChrome != null) {
                                if (isSpeaking) {
                                    mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.SPEAKING);
                                    mStateTextView.setText("");
                                } else if (!isEnableMicrophonePermission()) {
                                    // マイク使用許可 or マイク搭載なし
                                    mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.PRIVACY);
                                    mStateTextView.setText(R.string.alx_006);
                                    callbackClose();
/*                                } else if (!AmazonAlexaManager.mIsAlexaConnection) {
                                    //システムエラー(AlexaにLoginされていない)
                                    mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.SYSTEM_ERROR);
                                    mStateTextView.setText(R.string.alx_005);*/
                                } else if (isPersistIndicator) {
                                    // Notificationあり
                                    mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.NOTIFICATIONS_QUEUED);
                                    mStateTextView.setText(R.string.alx_004);
                                } else {
                                    // IDLE状態
                                    Timber.d("defaultVoiceChromeStatus IDLE");
                                    mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.IDLE);
                                    mStateTextView.setText("");
                                }
                            }
                        }
                    }
                });
            }

        }

        /**
         * マイクの権限をチェックするメソッド.
         *
         * @return true:許可, false：非許可
         */
        private boolean isEnableMicrophonePermission() {
            int state = PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO);
            if (state == PermissionChecker.PERMISSION_GRANTED) {
                // 許可
                return true;
            } else {
                // 不許可
                return false;
            }
        }
    }

}