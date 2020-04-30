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
import android.text.TextPaint;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Optional;
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
import jp.pioneer.carsync.presentation.presenter.AlexaDisplayCardPresenter;
import jp.pioneer.carsync.presentation.view.AlexaDisplayCardView;

import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import jp.pioneer.carsync.presentation.view.adapter.AlexaListTemplateAdapter;
import jp.pioneer.carsync.presentation.view.widget.CustomGestureLayout;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Navigation.SetDestinationItem;
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
    @BindView(R.id.close_button)
    ImageView mCloseBtn;
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
    private Unbinder mUnbinder;
    private int mOrientation;
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
                closeDialogWithAnimation();
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
        if (getCallback() != null) {
            getCallback().onClose(AlexaDisplayCardFragment.this);
        }
        AlexaDisplayCardFragment.this.dismiss();
    }

    @Override
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alexa_display_card, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        Configuration config = getResources().getConfiguration();
        mOrientation = config.orientation;
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //画面タッチで保持する
                Timber.d("view onTouch");
                if(!isSpeaking) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mHandler.removeCallbacks(mRunnable);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            mHandler.postDelayed(mRunnable, IDLE_TIME);
                            break;
                    }
                }
                return true;
            }
        });
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
        isExpectSpeech = false;
        mAmazonAlexaManager = AmazonAlexaManager.getInstance();
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.addAlexaCallback(mAlexaCallback);
            mAmazonAlexaManager.isShowAlexaDisplayCardDialog = true;
            mAmazonAlexaManager.addAlexaDisplayCallback(mAlexaDisplayCallback);
        }
        mAlexaBtnGroup.setVisibility(View.VISIBLE);
        mVoiceChrome.setVisibility(View.VISIBLE);
        if (AlexaSpeakManager.getInstance().getAlexaPlayer() != null) {
            if (AlexaSpeakManager.getInstance().getAlexaPlayer().isPlaying()) {
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
        if (AmazonAlexaManager.isAlexaUnavailable) {
            //Alexa Service Unavailable
            mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.SYSTEM_ERROR);
            //無操作2秒で閉じる
            mHandler.postDelayed(mRunnable, IDLE_TIME);
            return;
        }
        if (!AmazonAlexaManager.mIsAlexaConnection) {
            //システムエラー(Alexaに接続されていない)
            mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.SYSTEM_ERROR);
            //無操作2秒で閉じる
            mHandler.postDelayed(mRunnable, IDLE_TIME);
            return;
        }
        getPresenter().startAlexa();
    }

    @OnClick(R.id.close_button)
    public void onClickDismissBtn() {
        //裏のAlexa画面も閉じる
        ((MainActivity) getActivity()).dismissAlexaDialog();
        getPresenter().dismissDialog();
    }

    @Override
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
                    //setImage(mSkillIcon, null);
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
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(500);
        mContainer.startAnimation(animation);
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
                            mHandler.postDelayed(mRunnable, IDLE_TIME);
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
                    if (source1.size.equals(IMAGE_SIZE[i])) {
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

    private class AlexaDisplayCallback implements AmazonAlexaManager.IAlexaDisplayCallback {
        @Override
        public void onExpectSpeech() {
            Timber.d("onExpectSpeech");
            isExpectSpeech = true;
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
            mVoiceChrome.setVisibility(View.VISIBLE);
            mAlexaBtnGroup.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onRecordingMonitor(double db, int hertz) {

        }

        @Override
        public void onRecordingStop(boolean isCancel) {
            Timber.d("onRecordingStop");
            mAlexaBtnGroup.setVisibility(View.VISIBLE);
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
            Timber.d("onPersistVisualIndicator");
            isPersistIndicator = true;
        }

        @Override
        public void onClearVisualIndicator() {
            Timber.d("onClearVisualIndicator");
            isPersistIndicator = false;
            defaultVoiceChromeStatus();

        }

        @Override
        public void onAudioIndicatorStarted() {
            Timber.d("onAudioIndicatorStarted");
            //どの状態でも割り込む
            mVoiceChrome.setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType.NOTIFICATIONS);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVoiceChrome != null) {
                        mVoiceChrome.setVisibility(View.VISIBLE);
                    }
                }
            });
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

        }

        @Override
        public void onChannelActiveChange(AlexaQueueManager.AlexaChannel channel, boolean isActive) {
            Timber.d("onChannelChange() - afterChannel = " + channel + "isAvtive = " + isActive);
            if (isExpectSpeech) return;
            if (!isActive && channel == AlexaQueueManager.AlexaChannel.DialogChannel) {
                defaultVoiceChromeStatus();
                //発話終了後無操作で2秒経過で非表示
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