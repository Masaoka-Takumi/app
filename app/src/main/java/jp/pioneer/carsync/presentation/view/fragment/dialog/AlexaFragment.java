package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.text.TextPaint;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AlexaPresenter;
import jp.pioneer.carsync.presentation.view.AlexaView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import jp.pioneer.carsync.presentation.view.adapter.AlexaListTemplateAdapter;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechSynthesizer.SpeakItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderTemplateItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.CustomVoiceChromeView;
import jp.pioneer.mbg.alexa.manager.AlexaQueueManager;
import jp.pioneer.mbg.alexa.manager.AlexaSpeakManager;
import jp.pioneer.mbg.alexa.util.Constant;
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

    @BindView(R.id.container)
    ConstraintLayout mContainer;
    @BindView(R.id.display_card_layout)
    ConstraintLayout mDisplayCardLayout;
    @BindView(R.id.cover_view)
    View mCoverView;
    @BindView(R.id.main_Title)
    TextView mMainTitle;
    @BindView(R.id.skill_icon)
    ImageView mSkillIcon;
    @BindView(R.id.body_template2)
    ConstraintLayout mBodyTemplate2;
    @BindView(R.id.body_sub_Title)
    TextView mBodySubTitle;
    @BindView(R.id.image)
    ImageView mImage;
    @BindView(R.id.list_template1)
    ConstraintLayout mListTemplate1;
    @BindView(R.id.list_sub_Title)
    TextView mListSubTitle;
    @BindView(R.id.list_view)
    ListView mListView;
    @BindView(R.id.weather_template)
    ConstraintLayout mWeatherTemplate;
    @BindView(R.id.weather_sub_Title)
    TextView mWeatherSubTitle;
    @BindView(R.id.current_weather_icon)
    ImageView mCurrentWeatherIcon;
    @BindView(R.id.current_weather_text)
    TextView mCurrentWeatherText;
    @BindView(R.id.high_arrow)
    ImageView mHighArrow;
    @BindView(R.id.high_temperature)
    TextView mHighTemperature;
    @BindView(R.id.low_arrow)
    ImageView mLowArrow;
    @BindView(R.id.low_temperature)
    TextView mLowTemperature;
    @BindView(R.id.weather_forecast)
    LinearLayout mWeatherForecastLayout;
    private AlexaListTemplateAdapter mAdapter;
    private Unbinder mUnbinder;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mOrientation;
    private static final int IDLE_TIME = 1000;
    private static final int IDLE_TIME_2 = 2000;
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
            if(mDisplayCardLayout.getVisibility()==View.VISIBLE) {
                closeDialogWithAnimation();
            }else {
                callbackClose();
            }
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
        fragment.setCancelable(false);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.BehindScreenStyle);
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
        mDisplayCardLayout.setVisibility(View.GONE);
        mContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //表示カード表示中、画面タッチで保持する
                if(mDisplayCardLayout.getVisibility()==View.VISIBLE) {
                    Timber.d("view onTouch");
                    if (!isSpeaking) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                mHandler.removeCallbacks(mRunnable);
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                mHandler.postDelayed(mRunnable, IDLE_TIME_2);
                                break;
                        }
                    }
                }
                return true;
            }
        });

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
        if(mIsBack) {
            callbackClose();
            return;
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
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
        if(mDisplayCardLayout.getVisibility()==View.VISIBLE){
            AlphaAnimation animation = new AlphaAnimation(1, 0);
            animation.setDuration(500);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mCommunicationLayoutHandler.clickClose();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mContainer.startAnimation(animation);
        }else {
            mCommunicationLayoutHandler.clickClose();
        }
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
                    if(mStateTextView!=null) {
                        mStateTextView.setText(R.string.alx_001);
                    }
                    if(mCoverView!=null) {
                        mCoverView.setVisibility(View.VISIBLE);
                        AlphaAnimation animation = new AlphaAnimation(0, 1);
                        animation.setDuration(500);
                        mCoverView.startAnimation(animation);
                    }
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
                            if(mAlexaBtnGroup!=null&&mStateTextView!=null) {
                                mAlexaBtnGroup.setVisibility(View.VISIBLE);
                                mStateTextView.setText("");
                            }
                        }
                    });
                }
                //StopCaptureからCancelする場合Elseにはいる
            } else {
                // 録音が終わったらThinking状態に移行する.
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mAlexaBtnGroup!=null&&mStateTextView!=null) {
                            mAlexaBtnGroup.setVisibility(View.VISIBLE);
                            mStateTextView.setText(R.string.alx_002);
                        }
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
                    if(mAlexaBtnGroup!=null) {
                        mAlexaBtnGroup.setVisibility(View.VISIBLE);
                    }
                    if(mCoverView!=null){
                        mCoverView.setVisibility(View.GONE);
                    }
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
        public void onReceiveRenderTemplate(RenderTemplateItem templateItem) {
            Timber.d("onReceiveRenderTemplate");
            if(templateItem.type.equals(Constant.TEMPLATE_TYPE_BODY_TEMPLATE2)||templateItem.type.equals(Constant.TEMPLATE_TYPE_LIST_TEMPLATE1)
            ||templateItem.type.equals(Constant.TEMPLATE_TYPE_LOCAL_SEARCH_LIST_TEMPLATE1)||templateItem.type.equals(Constant.TEMPLATE_TYPE_WEATHER_TEMPLATE)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setTemplate(templateItem);
                    }
                });
            }
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
                        if(mStateTextView!=null&&mVoiceChrome!=null) {
                            mStateTextView.setText(R.string.alx_003);
                            mVoiceChrome.setVisibility(View.VISIBLE);
                        }
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
                if(mDisplayCardLayout.getVisibility()==View.VISIBLE){
                    mHandler.postDelayed(mRunnable, IDLE_TIME_2);
                }else {
                    mHandler.postDelayed(mRunnable, IDLE_TIME);
                }
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
                if(mAlexaBtnGroup!=null) {
                    mAlexaBtnGroup.setVisibility(visibility);
                }
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
                    if(mVoiceChrome!=null&&mAlexaBtnGroup!=null) {
                        mVoiceChrome.setVisibility(View.VISIBLE);
                        mAlexaBtnGroup.setVisibility(View.INVISIBLE);
                    }
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
                    if(mAlexaBtnGroup!=null) {
                        mAlexaBtnGroup.setVisibility(View.VISIBLE);
                    }
                    if(mCoverView!=null) {
                        mCoverView.clearAnimation();
                        mCoverView.setVisibility(View.GONE);
                    }
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

    public void closeDialogWithAnimation() {
        AlphaAnimation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                callbackClose();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mContainer.startAnimation(animation);
    }

    public void setTemplate(final RenderTemplateItem renderTemplateItem) {

        if (renderTemplateItem == null) {
            Timber.d("setTemplate:renderTemplateItem is null ");
            callbackClose();
            return;
        }
        Timber.d("setTemplate:type=" + renderTemplateItem.type);
        AlexaSpeakManager alexaSpeakManager = AlexaSpeakManager.getInstance();
        SpeakItem currentItem = alexaSpeakManager.getCurrentItem();
        if (currentItem != null) {
            String currentItemId = currentItem.getDialogRequestId();
            String renderItemId = renderTemplateItem.getDialogRequestId();
            if (!currentItemId.equals(renderItemId)) {
                // DialogRequestIdが不一致
                Timber.d("setTemplate:DialogRequestId is not equal " + renderTemplateItem.messageId);
                callbackClose();
                return;
            }
        } else {
            Timber.d("setTemplate:currentItemId is null ");
            callbackClose();
            return;
        }

        mMainTitle.setText(renderTemplateItem.title.mainTitle);
        if (renderTemplateItem.skillIcon != null) {
            AlexaIfDirectiveItem.ImageStructure imageStructure = renderTemplateItem.skillIcon;
            AlexaIfDirectiveItem.Source source = getSourceImage(imageStructure);
            String imageUrl = null;
            if (source != null) {
                imageUrl = source.getUrl();
            }
            if (mSkillIcon != null) {
                if (imageUrl != null) {
                    setImageNoBox(mSkillIcon, Uri.parse(imageUrl));
                } else {
                    setImageNoBox(mSkillIcon, null);
                }
            }
        }
        switch (renderTemplateItem.type) {
            case Constant.TEMPLATE_TYPE_BODY_TEMPLATE2:
                setBodyTemplate2(renderTemplateItem);
                break;
            case Constant.TEMPLATE_TYPE_LIST_TEMPLATE1:
            case Constant.TEMPLATE_TYPE_LOCAL_SEARCH_LIST_TEMPLATE1:
                setListTemplate1(renderTemplateItem);
                break;
            case Constant.TEMPLATE_TYPE_WEATHER_TEMPLATE:
                setWeatherTemplate(renderTemplateItem);
                break;
        }
        mCoverView.clearAnimation();
        mCoverView.setVisibility(View.GONE);
        mDisplayCardLayout.setVisibility(View.VISIBLE);
        Animation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(500);
        mContainer.startAnimation(alphaAnimation);
    }

    private void setBodyTemplate2(final RenderTemplateItem renderTemplateItem) {
        mBodyTemplate2.setVisibility(View.VISIBLE);
        mListTemplate1.setVisibility(View.GONE);
        mWeatherTemplate.setVisibility(View.GONE);
        mBodySubTitle.setText(renderTemplateItem.title.subTitle);
        if (renderTemplateItem.image != null) {
            AlexaIfDirectiveItem.ImageStructure imageStructure = renderTemplateItem.image;
            AlexaIfDirectiveItem.Source source = getSourceImage(imageStructure);
            String imageUrl = null;
            if (source != null) {
                imageUrl = source.getUrl();
            }
            if (mImage != null) {
                if (imageUrl != null) {
                    setImage(mImage, Uri.parse(imageUrl));
                } else {
                    setImage(mImage, null);
                }
            }
        }
    }

    private void setListTemplate1(final RenderTemplateItem renderTemplateItem) {
        mBodyTemplate2.setVisibility(View.GONE);
        mListTemplate1.setVisibility(View.VISIBLE);
        mWeatherTemplate.setVisibility(View.GONE);
        mListSubTitle.setText(renderTemplateItem.title.subTitle);
        mAdapter = new AlexaListTemplateAdapter(getContext(), renderTemplateItem.type);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setAdapter(mAdapter);
        mListView.setDivider(null);
        mAdapter.clear();
        mAdapter.addAll(renderTemplateItem.listItems);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (renderTemplateItem.type.equals(Constant.TEMPLATE_TYPE_LOCAL_SEARCH_LIST_TEMPLATE1)) {
                    AlexaIfDirectiveItem.ListItem item = mAdapter.getItem(position);
                    if (item != null) {
                        mAmazonAlexaManager.onPost(item.getSetDestinationItem());
                    }
                }
            }
        });
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Timber.d("listView onTouch");
                if(!isSpeaking) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mHandler.removeCallbacks(mRunnable);
                            return true;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            mHandler.postDelayed(mRunnable, IDLE_TIME_2);
                            return false;
                    }
                }else{
                    return false;
                }
                return true;
            }
        });
    }

    private void setWeatherTemplate(final RenderTemplateItem renderTemplateItem) {
        mBodyTemplate2.setVisibility(View.GONE);
        mListTemplate1.setVisibility(View.GONE);
        mWeatherTemplate.setVisibility(View.VISIBLE);
        mWeatherSubTitle.setText(renderTemplateItem.title.subTitle);
        mCurrentWeatherText.setText(renderTemplateItem.currentWeather);

        if (renderTemplateItem.currentWeatherIcon != null) {
            AlexaIfDirectiveItem.ImageStructure imageStructure = renderTemplateItem.currentWeatherIcon;
            AlexaIfDirectiveItem.Source source = getSourceImage(imageStructure);
            String imageUrl = null;
            if (source != null) {
                imageUrl = source.getDarkBackgroundUrl();
            }
            if (mCurrentWeatherIcon != null) {
                if (imageUrl != null) {
                    setImage(mCurrentWeatherIcon, Uri.parse(imageUrl));
                } else {
                    setImage(mCurrentWeatherIcon, null);
                }
            }
        }
        if (renderTemplateItem.highTemperature != null) {
            AlexaIfDirectiveItem.Temperature temperature = renderTemplateItem.highTemperature;
            AlexaIfDirectiveItem.ImageStructure imageStructure = temperature.arrow;
            AlexaIfDirectiveItem.Source source = getSourceImage(imageStructure);
            String imageUrl = null;
            if (source != null) {
                imageUrl = source.getDarkBackgroundUrl();
            }
            if (mHighArrow != null) {
                if (imageUrl != null) {
                    setImage(mHighArrow, Uri.parse(imageUrl));
                } else {
                    setImage(mHighArrow, null);
                }
            }
            mHighTemperature.setText(temperature.value);
        }
        if (renderTemplateItem.lowTemperature != null) {
            AlexaIfDirectiveItem.Temperature temperature = renderTemplateItem.lowTemperature;
            AlexaIfDirectiveItem.ImageStructure imageStructure = temperature.arrow;
            AlexaIfDirectiveItem.Source source = getSourceImage(imageStructure);
            String imageUrl = null;
            if (source != null) {
                imageUrl = source.getDarkBackgroundUrl();
            }
            if (mLowArrow != null) {
                if (imageUrl != null) {
                    setImage(mLowArrow, Uri.parse(imageUrl));
                } else {
                    setImage(mLowArrow, null);
                }
            }
            mLowTemperature.setText(temperature.value);
        }
        float dayLength = getResources().getDimension(R.dimen.alexa_display_card_weather_forecast_text_width);
        if (renderTemplateItem.weatherForecast != null) {
            List<AlexaIfDirectiveItem.WeatherForecast> weatherForecastList = renderTemplateItem.weatherForecast;
            int i = 0;
            for (AlexaIfDirectiveItem.WeatherForecast weatherForecast : weatherForecastList) {
                ConstraintLayout layout = (ConstraintLayout) mWeatherForecastLayout.getChildAt(i);
                ImageView image = layout.findViewById(R.id.image);
                TextView day = layout.findViewById(R.id.day);
                TextView highTemperature = layout.findViewById(R.id.high_temperature);
                TextView lowTemperature = layout.findViewById(R.id.low_temperature);

                AlexaIfDirectiveItem.ImageStructure imageStructure = weatherForecast.getImage();
                AlexaIfDirectiveItem.Source source = getSourceImage(imageStructure);
                String imageUrl = null;
                if (source != null) {
                    imageUrl = source.getDarkBackgroundUrl();
                }
                if (image != null) {
                    if (imageUrl != null) {
                        setImage(image, Uri.parse(imageUrl));
                    } else {
                        setImage(image, null);
                    }
                }
                day.setText(weatherForecast.getDay());
                highTemperature.setText(weatherForecast.getHighTemperature());
                lowTemperature.setText(weatherForecast.getLowTemperature());
                i++;
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    dayLength = Math.max(calculateTextLen(day), dayLength);
                    if (i >= 4) break;
                } else {
                    if (i >= 5) break;
                }
            }
            //縦画面のdayの横幅調整
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                for (int h = 0; h < 4; h++) {
                    ConstraintLayout layout = (ConstraintLayout) mWeatherForecastLayout.getChildAt(h);
                    TextView day = layout.findViewById(R.id.day);
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) day.getLayoutParams();
                    layoutParams.width = (int) Math.ceil(dayLength);
                    day.setLayoutParams(layoutParams);
                }
            }
        }
    }

    private float calculateTextLen(TextView view) {
        TextPaint tp = view.getPaint();
        String strTxt = view.getText().toString();
        float mt = tp.measureText(strTxt);
        return mt;
    }

    private AlexaIfDirectiveItem.Source getSourceImage(AlexaIfDirectiveItem.ImageStructure imageStructure) {
        final String[] IMAGE_SIZE = new String[]{"LARGE","X-LARGE","MEDIUM","SMALL","X-SMALL"};
        AlexaIfDirectiveItem.Source source = null;
        List<AlexaIfDirectiveItem.Source> sources = imageStructure.getSources();
        SparseArray<AlexaIfDirectiveItem.Source> priorityList = new SparseArray<>();
        //IMAGE_SIZEの優先度で画像を採用する
        if(sources!=null) {
            for (AlexaIfDirectiveItem.Source source1 : sources) {
                for (int i = 0; i < IMAGE_SIZE.length; i++) {
                    if (source1.size!=null&&source1.size.toUpperCase().equals(IMAGE_SIZE[i])) {
                        priorityList.put(i, source1);
                    }
                }
            }
            for (int i = 0; i < IMAGE_SIZE.length; i++) {
                if (priorityList.get(i) != null) {
                    source = priorityList.get(i);
                    break;
                }
            }
            //sizeを取得できなかったら
            if(source==null){
                if (sources.size() > 0) {
                    //small→...→x-largeと仮定して、Listの最後の画像(Large)を取得
                    int logoSize = sources.size() - 1;
                    source = sources.get(logoSize);
                }
            }
        }
        return source;
    }

    private void setImage(ImageView view, Uri uri) {
        Glide.with(getContext())
                .load(uri)
                .error(android.R.color.darker_gray)
                .into(view);
    }

    private void setImageNoBox(ImageView view, Uri uri) {
        Glide.with(getContext())
                .load(uri)
                .error(null)
                .into(view);
    }

}