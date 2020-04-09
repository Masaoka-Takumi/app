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
import android.support.v4.content.PermissionChecker;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AlexaDisplayCardPresenter;
import jp.pioneer.carsync.presentation.view.AlexaDisplayCardView;

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
import timber.log.Timber;

/**
 * AlexaのDisplayCard
 */
public class AlexaDisplayCardFragment extends AbstractDialogFragment<AlexaDisplayCardPresenter, AlexaDisplayCardView, AbstractDialogFragment.Callback>
        implements AlexaDisplayCardView {
    private static final int IDLE_TIME = 2000;
    @Inject
    AlexaDisplayCardPresenter mPresenter;
    @BindView(R.id.container)
    ConstraintLayout mContainer;
    @BindView(R.id.alexa_start_button_group)
    RelativeLayout mAlexaBtnGroup;
    @BindView(R.id.alexa_start_button)
    ImageView mAlexaBtn;
    @BindView(R.id.alexa_notification_circle)
    ImageView mAlexaNotification;
    @BindView(R.id.alexa_voice_chrome_large)
    CustomVoiceChromeView mVoiceChrome;
    @BindView(R.id.text_field)
    TextView mTextField;
    @BindView(R.id.close_button)
    ImageView mCloseBtn;
    @BindView(R.id.image)
    ImageView mImage;
    @BindView(R.id.body_template2)
    ConstraintLayout mBodyTemplate2;
    @BindView(R.id.list_template1)
    ConstraintLayout mListTemplate1;
    @BindView(R.id.list_view)
    ListView mListView;
    private Unbinder mUnbinder;
    /**
     * Alexaマネージャ.
     */
    AmazonAlexaManager mAmazonAlexaManager;
    private AlexaCallback mAlexaCallback = new AlexaCallback();
    private AlexaDisplayCallback mAlexaDisplayCallback = new AlexaDisplayCallback();
    private AlexaListTemplateAdapter mAdapter;
    private boolean isPersistIndicator = false;
    private boolean isSpeaking = false;
    private boolean isExpectSpeech = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (!((MainActivity) getActivity()).isShowAlexaDialog()) {
                callbackClose();
            }
        }
    };

    /**
     * コンストラクタ
     */
    public AlexaDisplayCardFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param args   引き継ぎ情報
     * @return AlexaDisplayCardFragment
     */
    public static AlexaDisplayCardFragment newInstance(android.support.v4.app.Fragment target, Bundle args) {
        AlexaDisplayCardFragment fragment = new AlexaDisplayCardFragment();
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
        return callback instanceof AlexaDisplayCardFragment.Callback;
    }

    @NonNull
    @Override
    protected AlexaDisplayCardPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void callbackClose() {
        Timber.d("callbackClose");
        AlphaAnimation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (getCallback() != null) {
                    getCallback().onClose(AlexaDisplayCardFragment.this);
                }
                AlexaDisplayCardFragment.this.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mContainer.startAnimation(animation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alexa_display_card, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart");
        isSpeaking = false;
        isPersistIndicator = false;
        isExpectSpeech =false;
        mAmazonAlexaManager = AmazonAlexaManager.getInstance();
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.addAlexaCallback(mAlexaCallback);
            mAmazonAlexaManager.isShowAlexaDisplayCardDialog = true;
            mAmazonAlexaManager.addAlexaDisplayCallback(mAlexaDisplayCallback);
        }
        mVoiceChrome.setVisibility(View.VISIBLE);
        if (AlexaSpeakManager.getInstance().getAlexaPlayer() != null) {
            if(AlexaSpeakManager.getInstance().getAlexaPlayer().isPlaying()){
                isSpeaking = true;
                defaultVoiceChromeStatus();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.d("onStop");
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.removeAlexaCallback(mAlexaCallback);
            mAmazonAlexaManager.isShowAlexaDisplayCardDialog = false;
            mAmazonAlexaManager.removeAlexaDisplayCallback();
        }
        mHandler.removeCallbacks(mRunnable);
    }

    @OnClick(R.id.alexa_start_button)
    public void onClickAlexaBtn() {
        mHandler.removeCallbacks(mRunnable);
        if(AmazonAlexaManager.isAlexaUnavailable){
            //Alexa Service Unavailable
            mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.SYSTEM_ERROR);
            return;
        }
        if (!AmazonAlexaManager.mIsAlexaConnection) {
            //システムエラー(Alexaに接続されていない)
            mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.SYSTEM_ERROR);
            return;
        }
        getPresenter().startAlexa();
    }

    @OnClick(R.id.close_button)
    public void onClickDismissBtn() {
        ((MainActivity) getActivity()).dismissAlexaDialog();
        getPresenter().dismissDialog();
    }

    @Override
    public void setTemplate(final RenderTemplateItem renderTemplateItem) {
        if (renderTemplateItem == null) return;
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

        switch (renderTemplateItem.type) {
            case "BodyTemplate2":
                setBodyTemplate2(renderTemplateItem);
                break;
            case "ListTemplate1":
            case "LocalSearchListTemplate1":
                setListTemplate1(renderTemplateItem);
                break;
            case "WeatherTemplate":
                break;
        }
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(500);
        mContainer.startAnimation(animation);
    }

    private void setBodyTemplate2(final RenderTemplateItem renderTemplateItem) {
        mBodyTemplate2.setVisibility(View.VISIBLE);
        mListTemplate1.setVisibility(View.GONE);
        mTextField.setText(renderTemplateItem.textField);
        if (renderTemplateItem.image != null) {
            AlexaIfDirectiveItem.Source source = null;
            AlexaIfDirectiveItem.ImageStructure imageStructure = renderTemplateItem.image;
            List<AlexaIfDirectiveItem.Source> sources = imageStructure.getSources();
            if (sources != null && sources.size() > 0) {
                //small→...→x-largeと仮定して、Listの最後の画像(Large)を取得
                int logoSize = sources.size() - 1;
                source = sources.get(logoSize);
            }
            String imageUrl = null;
            if (source != null) {
                imageUrl = source.getUrl();
            }
            if (mImage != null) {
                if (imageUrl != null) {
                    setImage(Uri.parse(imageUrl));
                } else {
                    setImage(null);
                }
            }
        }
    }

    private void setListTemplate1(final RenderTemplateItem renderTemplateItem) {
        mBodyTemplate2.setVisibility(View.GONE);
        mListTemplate1.setVisibility(View.VISIBLE);
        mAdapter = new AlexaListTemplateAdapter(getContext(),renderTemplateItem.type);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setAdapter(mAdapter);
        mAdapter.clear();
        mAdapter.addAll(renderTemplateItem.listItems);
    }

    private void setImage(Uri uri) {
        Glide.with(getContext())
                .load(uri)
                .error(R.drawable.p0070_noimage)
                .into(mImage);
    }

    /**
     * VoiceChromeの状態を戻すメソッド.
     */
    private void defaultVoiceChromeStatus() {
        Timber.d("defaultVoiceChromeStatus");
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
                            } else if (!isEnableMicrophonePermission()) {
                                // マイク使用許可 or マイク搭載なし
                                mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.PRIVACY);
                                callbackClose();
                            } else if (isPersistIndicator) {
                                // Notificationあり
                                mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.NOTIFICATIONS_QUEUED);
                            } else {
                                // IDLE状態
                                Timber.d("defaultVoiceChromeStatus IDLE");
                                mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.IDLE);
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

    private class AlexaDisplayCallback implements AmazonAlexaManager.IAlexaDisplayCallback{
        @Override
        public void onExpectSpeech() {
            Timber.d("onExpectSpeech");
            isExpectSpeech=true;
            onClickAlexaBtn();
        }
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

        }

        @Override
        public void onDisConnect() {

        }

        @Override
        public void onNetworkConnect() {

        }

        @Override
        public void onNetworkDisconnect() {

        }

        @Override
        public void onRecordingStart() {
            Timber.d("onRecordingStart");
        }

        @Override
        public void onRecordingMonitor(double db, int hertz) {

        }

        @Override
        public void onRecordingStop(boolean isCancel) {

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
            isSpeaking = true;
            // Speaking状態に移行する.
           defaultVoiceChromeStatus();
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
            isExpectSpeech = false;
        }

        @Override
        public void onSpeakingStop() {
            Timber.d("onSpeakingStop");
            isSpeaking = false;
            isExpectSpeech = false;
        }

        @Override
        public void onSpeakingComplete() {
            Timber.d("onSpeakingComplete");
            isSpeaking = false;
            isExpectSpeech = false;
        }

        @Override
        public void onReceiveRenderPlayerInfo(RenderPlayerInfoItem playerInfoItem) {

        }

        @Override
        public void onReceiveRenderTemplate(RenderTemplateItem templateItem) {

        }

        @Override
        public void onAudioPrepare() {

        }

        @Override
        public void onAudioPrepared() {

        }

        @Override
        public void onAudioStart() {

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

        }

        @Override
        public void onAudioComplete() {

        }

        @Override
        public void onWLAudioFocusLoss() {

        }

        @Override
        public void onAudioUpdateProgress(int current, int duration) {

        }

        @Override
        public void onSystemError() {

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

        }

        @Override
        public void onClearVisualIndicator() {

        }

        @Override
        public void onAudioIndicatorStarted() {

        }

        @Override
        public void onAudioIndicatorStopped() {

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

        }

        @Override
        public void onChannelActiveChange(AlexaQueueManager.AlexaChannel channel, boolean isActive) {
            Timber.d("onChannelChange() - afterChannel = " + channel + "isAvtive = " + isActive);
            if(isExpectSpeech)return;
            if (!isActive && channel == AlexaQueueManager.AlexaChannel.DialogChannel) {
                defaultVoiceChromeStatus();
                mHandler.postDelayed(mRunnable, IDLE_TIME);
            }
        }

        @Override
        public void onMicrophonePermission(int state) {

        }

        @Override
        public void onNoDirectiveAtSendEventResponse() {

        }

        @Override
        public void onDecodeStart() {

        }

        @Override
        public void onDecodeFinish() {

        }

        @Override
        public void onSetNaviDestination(Double latitude, Double longitude, String name) {

        }

        @Override
        public void onRecordingNotAvailable() {

        }
    }
}