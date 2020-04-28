package jp.pioneer.carsync.presentation.presenter;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.util.AppUtil;
import jp.pioneer.carsync.domain.component.TextToSpeechController;
import jp.pioneer.carsync.domain.event.AdasCalibrationStatusChangeEvent;
import jp.pioneer.carsync.domain.event.AdasPriceChangeEvent;
import jp.pioneer.carsync.domain.event.AdasPurchaseStatusChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicPlayPositionChangeEvent;
import jp.pioneer.carsync.domain.event.ExitMenuEvent;
import jp.pioneer.carsync.domain.event.FinishVoiceRecognitionEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.event.SmartPhoneControlCommandEvent;
import jp.pioneer.carsync.domain.event.UpdateAdasEvent;
import jp.pioneer.carsync.domain.interactor.CheckAvailableTextToSpeech;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlCustomFlashPattern;
import jp.pioneer.carsync.domain.interactor.ControlSiriusXmSource;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.DeviceVoiceRecognition;
import jp.pioneer.carsync.domain.interactor.GetAddressFromLocationName;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.JudgeVoiceCommand;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.interactor.PrepareReadNotification;
import jp.pioneer.carsync.domain.interactor.PrepareSpeechRecognizer;
import jp.pioneer.carsync.domain.interactor.ReadText;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.BaseApp;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ConnectedDevicesCountStatus;
import jp.pioneer.carsync.domain.model.ConnectionType;
import jp.pioneer.carsync.domain.model.MarinApp;
import jp.pioneer.carsync.domain.model.MediaSourceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.NaviApp;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.SmartPhoneControlCommand;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.ThemeType;
import jp.pioneer.carsync.domain.model.VoiceCommand;
import jp.pioneer.carsync.domain.model.VoiceRecognitionSearchType;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionErrorEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStartedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.presentation.event.AlexaLoginSuccessEvent;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.CaptureSetEvent;
import jp.pioneer.carsync.presentation.event.CloseDialogEvent;
import jp.pioneer.carsync.presentation.event.DeviceConnectionSuppressEvent;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.MainNavigateEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.event.ShowCautionEvent;
import jp.pioneer.carsync.presentation.event.SourceChangeReasonEvent;
import jp.pioneer.carsync.presentation.event.StartGetRunningStatusEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.SettingEntrance;
import jp.pioneer.carsync.presentation.model.SimCountryIso;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.util.Adas;
import jp.pioneer.carsync.presentation.view.MainView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import jp.pioneer.carsync.presentation.view.argument.SearchContentParams;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SpeechRecognizerDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.util.SettingsUpdatedUtil;
import timber.log.Timber;

import static jp.pioneer.carsync.domain.interactor.JudgeVoiceCommand.JudgeResult.UNKNOWN;

/**
 * MainActivityのpresenter
 */
@PresenterLifeCycle
public class MainPresenter extends Presenter<MainView> implements AppSharedPreference.OnAppSharedPreferenceChangeListener, TextToSpeechController.Callback, RecognitionListener {
    private static final int RECOGNIZER_RESTART_REMIT = 2;
    private static final int RE_CALIBRATION_SESSION_COUNT_MAX = 15;//15回連携

    private static final int VERSION_CODE_1_5 = 8;//v1.5バージョンコード
    public static final int ALEXA_CAPABILITIES_NEW_VERSION = 5;//機能API最新バージョン
    public static final String TAG_DIALOG_ERROR = "error";
    public static final String TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE = "sxm_subscription_update";
    public static final String TAG_DIALOG_ADAS_TRIAL_END = "tag_adas_trial_end";
    public static final String TAG_DIALOG_ADAS_RE_CALIBRATION = "tag_adas_re_calibration";
    public static final String TAG_DIALOG_ADAS_BILLING_STATUS_ERROR = "adas_billing_status_error";
    public static final String TAG_DIALOG_ADAS_BILLING_STATUS_FAILURE = "adas_billing_status_failure";
    private static final String TAG_DIALOG_ADAS_GET_PRICE_ERROR = "adas_get_price_error";
    public static final String TAG_DIALOG_ADAS_PURCHASE_ERROR = "adas_purchase_error";
    private static final String TAG_DIALOG_ADAS_BILLING_RESTORE_FAILURE = "adas_billing_restore_failure";
    private static final String TAG_DIALOG_ADAS_BILLING_RESTORE_SUCCESS = "adas_billing_restore_success";
    public static final String TAG_DIALOG_ALEXA_AVAILABLE_CONFIRM = "alexa_confirm";

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mStatusCase;
    @Inject PrepareSpeechRecognizer mMicCase;
    @Inject JudgeVoiceCommand mJudgeVoiceCase;
    @Inject DeviceVoiceRecognition mDeviceVoiceRecognition;
    @Inject GetAddressFromLocationName mLocationCase;
    @Inject CheckAvailableTextToSpeech mCheckTtsCase;
    @Inject ReadText mReadText;
    @Inject ControlSource mControlSource;
    @Inject ControlCustomFlashPattern mControlCustomFlashPattern;
    @Inject Adas mAdas;
    @Inject ControlSiriusXmSource mControlSxmCase;
    @Inject PrepareReadNotification mPrepareReadCase;
    @Inject PreferAdas mPreferAdas;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    @Inject AnalyticsEventManager mAnalytics;
    private PrepareSpeechRecognizer.FinishBluetoothHeadset mFinishBluetoothHeadset;
    private VoiceCommand mResentVoiceCommand;
    private boolean mIsRecognizerRestarted = false;
    private int mRecognizerRestartCount = 0;
    private VoiceRecognitionSearchType mSearchType;
    private boolean mIsSubscribeExitMenu;
    private boolean mIsAdasStarted = false;
    private ArrayList<String> mRecognizeResults;
    private SpeechRecognizer mSpeechRecognizer;
    private boolean mIsSpeechRecognizeEnable;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private FinishSpeechRecognizerTask mFinishSpeechRecognizerTask = new FinishSpeechRecognizerTask();
    private StopSpeechRecognizerTask mStopSpeechRecognizerTask = new StopSpeechRecognizerTask();
    private boolean mIsAccidentDetectShow = false;
    private PhoneBroadcastReceiver mPhoneBroadcastReceiver = new PhoneBroadcastReceiver();
    private MyPhoneStateListener mPhoneStateListener = new MyPhoneStateListener();
    private boolean isRegisterBroadcastReceiver = false;
    private boolean isRegisterPhoneStateListener = false;
    private boolean mDeliberatelyCalledStop = false;
    private ArrayList<String> mStackPopUp = new ArrayList<>();
    private Set<MediaSourceType> mCurrentAvailableSourceType = new HashSet<>();
    private Map<String, Intent> mCommandIntentMap = new HashMap<>();
    private boolean mIsInitializedReadText;
    private MediaSourceType mPreviousSourceType;
    private SessionStatus mCurrentSessionStatus = SessionStatus.STOPPED;
    private boolean mIsAdasBillingSessionStop = false;
    public final static boolean mIsDebug = BuildConfig.DEBUG;
    private boolean mIsAlexaStart = false;
    private PrepareSpeechRecognizer.Device mSpeechRecognizerDevice;
    private boolean mIsRecognizeSourceChanged = false;
    public final static boolean sIsVersionQ = Build.VERSION.SDK_INT >= 29;
    private boolean mIsAccessLocationPermissionGranted = false;
    /**
     * コンストラクタ
     */
    @Inject
    public MainPresenter() {
    }

    @Override
    void onInitialize() {
        int vCode = mPreference.getVersionCode();
        if(vCode < VERSION_CODE_1_5){
            mPreference.setConfiguredSlaSetting(false);
        }
        mPreference.setVersionCode(BuildConfig.VERSION_CODE);
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.navigate(ScreenId.OPENING, Bundle.EMPTY);
            view.checkSim();
            if(mIsDebug) {
                view.getWriteExternalPermission();
            }
        });
        mPreference.setAdasPseudoCooperation(false);
        mPreference.registerOnAppSharedPreferenceChangeListener(this);
        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Optional.ofNullable(getView()).ifPresent(MainView::reloadBackground);
        if (savedInstanceState.getBoolean("adas")) {
            // ADAS作動中に画面が回転した
            Timber.d("onRestoreInstanceState() startAdas");
            Optional.ofNullable(getView()).ifPresent(MainView::startAdas);
        }

        String status = savedInstanceState.getString("session_status");
        if(status != null){
            mCurrentSessionStatus = SessionStatus.valueOf(status);
        }

        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("adas", mIsAdasStarted);
        outState.putString("session_status", mCurrentSessionStatus.name());
    }

    @Override
    void onResume() {
        SessionStatus status = mStatusCase.execute().getSessionStatus();
        checkOverlayAuthority();
        if (mCurrentSessionStatus != SessionStatus.STOPPED &&
                mCurrentSessionStatus != SessionStatus.PENDING &&
                status != mCurrentSessionStatus &&
                status != SessionStatus.STARTED) {
            sessionStop();
        }

        if (status != mCurrentSessionStatus && status == SessionStatus.STARTED) {
            sessionStart();
        }

        exitSetting();

        mCurrentSessionStatus = status;
        if(!mPreference.isRegistered(this)) {
            mPreference.registerOnAppSharedPreferenceChangeListener(this);
        }

        Optional.ofNullable(getView()).ifPresent(view -> {
            if (view.isShowSpeechRecognizerDialog()) {
                if (mFinishBluetoothHeadset == null) {
                    view.dismissSpeechRecognizerDialog();
                }
            }
        });

        if ((isSessionConnected()||mPreference.isAdasPseudoCooperation())) {
            AppStatus appStatus =mStatusCase.execute().getAppStatus();
            if(mPreferAdas.getAdasEnabled()&&!mIsAdasStarted&&!appStatus.isAdasCalibrationSetting&&mAdas.checkAdasPermission()) {
                // ADAS作動中にBackGroundに遷移した
                Timber.d("onResume() startAdas");
                Optional.ofNullable(getView()).ifPresent(MainView::startAdas);
            }
        }
        //OS設定で位置情報許可した場合、位置情報取得をやり直す
        boolean accessCoarseLocation = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean accessFineLocation = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if ((accessCoarseLocation && accessFineLocation)!=mIsAccessLocationPermissionGranted) {
            mEventBus.post(new StartGetRunningStatusEvent());
        }
        mIsAccessLocationPermissionGranted = accessCoarseLocation && accessFineLocation;
        mFinishSpeechRecognizerTask.stop();
        mStopSpeechRecognizerTask.start();

    }

    public void analyticsActiveScreenByNavigate(ScreenId screenId){
        if (mStatusCase.execute().getSessionStatus() == SessionStatus.STARTED && mStatusCase.execute().getAppStatus().isAgreedCaution) {
            if(screenId == ScreenId.PLAYER_CONTAINER
                    ||screenId == ScreenId.HOME_CONTAINER
                    ||screenId == ScreenId.SETTINGS_CONTAINER
                    ||screenId == ScreenId.UNCONNECTED_CONTAINER) {
                mEventBus.post(new MainNavigateEvent(screenId));
            }
        }
    }

    /**
     * オーバーレイ権限状態確認
     */
    public void checkOverlayAuthority(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(mPreference.isAgreedEulaPrivacyPolicy()) {
                if (sIsVersionQ) {
                    if(Settings.canDrawOverlays(mContext)){
                        if (view.isShowPromptAuthorityPermissionDialog()) {
                            Timber.d("Overlay:dismissPromptAuthorityPermissionDialog()");
                            view.dismissPromptAuthorityPermissionDialog();
                        }
                    }else {
                        startDeviceConnectionSuppress();
                        if(view.getScreenId()!=ScreenId.OPENING) {
                            if (!view.isShowPromptAuthorityPermissionDialog()){
                                view.showPromptAuthorityPermissionDialog(Bundle.EMPTY);
                            }
                        }
                    }
                }
            }
        });
    }
    /**
     * オーバーレイ権限許可後課金情報チェック
     */
    public void checkAdasPurchase(){
        Timber.d("Overlay:checkAdasPurchase()");
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (sIsVersionQ) {
                if(Settings.canDrawOverlays(mContext)) {
                    if (mPreference.isAdasBillingRecord() && !mStatusCase.execute().getAppStatus().adasBillingCheck) {
                        Timber.d("Overlay:setupBillingHelper");
                        view.setupBillingHelper();
                    } else {
                        finishDeviceConnectionSuppress();
                    }
                }
            }
        });
    }

    @Override
    void onPause() {
        mCurrentSessionStatus = mStatusCase.execute().getSessionStatus();
        MediaSourceType currentSourceType = mStatusCase.execute().getCarDeviceStatus().sourceType;
        mStopSpeechRecognizerTask.stop();
        if (mFinishBluetoothHeadset != null) {
            mFinishSpeechRecognizerTask.start();
        } else {
            mFinishSpeechRecognizerTask.stop();
        }
    }

    @Override
    public void destroy() {
        Timber.d("onDestroy");
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.stopAdas();
            mAdas.releaseWarning();
        });
        if(mPreference.isAdasBillingRecord()){
			//非連携中にdestroyした時のみ連携抑制のフラグを戻す
            if(mStatusCase.execute().getSessionStatus()!=SessionStatus.STARTING
                    && mStatusCase.execute().getSessionStatus()!=SessionStatus.STARTED){
                mStatusCase.execute().getAppStatus().deviceConnectionSuppress = true;
                mStatusCase.execute().getAppStatus().adasBillingCheck = false;
            }
        }else{
            mStatusCase.execute().getAppStatus().deviceConnectionSuppress = false;
        }
        //オーバーレイ権限不許可なら連携抑制のフラグを戻す（不許可時にフラグを戻しているため必要ない？）
        if(MainPresenter.sIsVersionQ&&!Settings.canDrawOverlays(mContext)||isAlexaAvailableConfirmNeeded()) {
            mStatusCase.execute().getAppStatus().deviceConnectionSuppress = true;
        }

        mEventBus.unregister(this);
        mPreference.unregisterOnAppSharedPreferenceChangeListener(this);
        super.destroy();
    }

    @Override
    public void onAppSharedPreferenceChanged(@NonNull AppSharedPreference preferences, @NonNull String key) {
        switch (key) {
            case AppSharedPreference.KEY_THEME_TYPE:
                setBackground(preferences);
                break;
            case AppSharedPreference.KEY_ADAS_ENABLED:
                Optional.ofNullable(getView()).ifPresent(view -> {
                    //ADASは連携中のみ動作
                    if(((isSessionConnected()&&(mStatusCase.execute().getAppStatus().adasPurchased||preferences.getAdasTrialState() == AdasTrialState.TRIAL_DURING))||preferences.isAdasPseudoCooperation())) {
                        if (preferences.isAdasEnabled()) {
                            Timber.d("onAppSharedPreferenceChanged:AppSharedPreference.KEY_ADAS_ENABLED:startAdas");
                            view.startAdas();
                        } else {
                            view.stopAdas();
                            mAdas.releaseWarning();
                            mAdas.resetAdasError();
                        }
                    }
                });
                break;
            case AppSharedPreference.KEY_ADAS_ALARM_ENABLED:
                mAdas.checkAdasError();
                break;
            case AppSharedPreference.KEY_ADAS_PSEUDO_COOPERATION:
                Optional.ofNullable(getView()).ifPresent(view -> {
                    //ADAS用擬似連携
                    if(preferences.isAdasPseudoCooperation()||(mStatusCase.execute().getAppStatus().adasPurchased||preferences.getAdasTrialState() == AdasTrialState.TRIAL_DURING)){
                        if (preferences.isAdasEnabled()) {
                            Timber.d("onAppSharedPreferenceChanged:AppSharedPreference.KEY_ADAS_PSEUDO_COOPERATION:startAdas");
                            view.startAdas();
                        }
                    } else {
                        view.stopAdas();
                        mAdas.releaseWarning();
                        mAdas.resetAdasError();
                    }
                });
            default:
                break;
        }
    }
    public AppStatus getAppStatus(){
        return mStatusCase.execute().getAppStatus();
    }


    /**
     * 背景設定
     */
    public void setBackground() {
        setBackground(mPreference);
    }

    private void setBackground(@NonNull AppSharedPreference preferences) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            ThemeType type = preferences.getThemeType();
            view.changeBackgroundType(type.isVideo());
            if (type.isVideo()) {
                Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + type.getResourceId());
                view.changeBackgroundVideo(uri);
            } else if(type == ThemeType.PICTURE_PATTERN13) {
                InputStream in = null;
                try {
                    in = mContext.openFileInput("myPhoto.jpg");
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    view.changeBackgroundBitmap(bitmap);
                } catch (IOException e) {
                    Timber.e(e.getMessage());
                }finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException ignored) {
                    }
                }
            }else{
                view.changeBackgroundImage(type.getResourceId());
            }
        });
    }

    /**
     * LightingEffect再生開始要求
     */
    public void startFlashPattern() {
        mControlCustomFlashPattern.start();
    }

    /**
     * UiColor取得
     *
     * @return Color
     */
    public UiColor getUiColor() {
        return mPreference.getUiColor();
    }

    /**
     * コマンドインテント追加.
     * <p>
     * Destroy対策として、コマンドのインテントを保持するために使用する
     *
     * @param tag タグ名
     * @param intent インテント
     */
    public void addCommandIntent(String tag, Intent intent){
        if(!mCommandIntentMap.containsKey(tag)){
            //onNewIntentが2回呼ばれる。2回目は無視
           if(tag.equals(MainActivity.ACTION_SHOW_ACCIDENT_DETECT)&&mIsAccidentDetectShow)return;
            mCommandIntentMap.put(tag, intent);
        } else {
            mCommandIntentMap.remove(tag);
            mCommandIntentMap.put(tag, intent);
        }
    }

    /**
     * 保持しているコマンドインテント取得.
     * <p>
     * 保持していない場合はnullを返す
     *
     * @param tag タグ名
     * @return コマンドインテント
     */
    @Nullable
    public Intent getCommandIntent(String tag){
        if(mCommandIntentMap.containsKey(tag)){
            Intent retIntent = mCommandIntentMap.get(tag);
            mCommandIntentMap.remove(tag);
            return retIntent;
        }
        return null;
    }

    /**
     * 保持しているコマンドインテント削除.
     *
     * @param tag タグ名
     */
    public void removeCommandIntent(String tag){
        if(mCommandIntentMap.containsKey(tag)){
            mCommandIntentMap.remove(tag);
        }
    }

    /**
     * Appコマンドアクション
     */
    public void onAppCommandAction() {
        if (App.getApp(mContext).isForeground()) {
            if (mPreference.isAgreedEulaPrivacyPolicy()) {
                mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));
            }
        }
    }

    /**
     * Phoneコマンドアクション
     */
    public void onPhoneCommandAction() {
        if (App.getApp(mContext).isForeground()) {
            if (mPreference.isPhoneBookAccessible()) {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    if (view.isShowSearchContainer()) {
                        view.dismissSearchContainer();
                    }
                    if (!view.isShowContactContainer()) {
                        mEventBus.post(new BackgroundChangeEvent(true));
                        view.navigate(ScreenId.CONTACTS_CONTAINER, Bundle.EMPTY);
                    }
                });
            }
        }
    }

    /**
     * AVコマンドアクション
     */
    public void onAvCommandAction() {
        if (App.getApp(mContext).isForeground()) {
            Optional.ofNullable(getView()).ifPresent(view -> {
                view.navigate(ScreenId.PLAYER_CONTAINER, Bundle.EMPTY);
            });
        }
    }

    /**
     * Enter Listコマンドアクション
     */
    public void onEnterListCommandAction() {
        if (App.getApp(mContext).isForeground()) {
            Optional.ofNullable(getView()).ifPresent(view -> {
                if(!view.getScreenId().isPlayer()) {
                    view.navigate(ScreenId.PLAYER_CONTAINER, Bundle.EMPTY);
                }
            });
        }
    }

    /**
     * 通知読み上げアクション
     */
    public void onReadingMessageAction(Bundle args) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (view.isShowSpeechRecognizerDialog()) {
                closeSpeechRecognizer(false);
            }
            if (view.isShowSearchContainer()) {
                view.dismissSearchContainer();
            }
            if (view.isShowContactContainer()) {
                view.dismissContactContainer();
            }
            if (!view.isShowReadMessageDialog()) {
                //view.showReadMessageDialog(args);
                view.navigate(ScreenId.READING_MESSAGE, args);
            }
        });
    }

    public void setAccidentDetectShow(boolean accidentDetectShow) {
        mIsAccidentDetectShow = accidentDetectShow;
    }

    /**
     * 事故通知画面の表示
     *
     * @param args Bundle
     */
    public void onShowAccidentDetectAction(Bundle args) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (view.isShowSpeechRecognizerDialog()) {
                closeSpeechRecognizer(false);
            }
            if (view.isShowReadMessageDialog()) {
                view.dismissReadMessageDialog();
            }
            if (view.isShowSearchContainer()) {
                view.dismissSearchContainer();
            }
            if (view.isShowContactContainer()) {
                view.dismissContactContainer();
            }
            //画面回転で何度も呼ばれるのを回避、タイマー終了後は新規表示
            if(!mIsAccidentDetectShow) {
                if (view.isShowAccidentDetect()){
                    view.dismissAccidentDetect();
                }
                view.navigate(ScreenId.IMPACT_DETECTION, args);
                mIsAccidentDetectShow = true;
            }
        });
    }

    /**
     * 事故通知画面の再表示
     *
     * @param args Bundle
     */
    public void onReShowAccidentDetectAction(Bundle args) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (mIsAccidentDetectShow) {
                if (!view.isShowAccidentDetect()) {
                    view.navigate(ScreenId.IMPACT_DETECTION, args);
                    mIsAccidentDetectShow = true;
                }
            }
        });
    }

    /**
     * パーキングセンサー画面の表示
     *
     * @param args Bundle
     */
    public void onShowParkingSensorAction(Bundle args) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (view.isShowSpeechRecognizerDialog()) {
                closeSpeechRecognizer(false);
            }
            if (view.isShowReadMessageDialog()) {
                view.dismissReadMessageDialog();
                finishReadingMessage();
            }
            if (view.isShowSearchContainer()) {
                view.dismissSearchContainer();
            }
            if (view.isShowContactContainer()) {
                view.dismissContactContainer();
            }
            if (view.isShowAccidentDetect()) {
                view.dismissAccidentDetect();
                mIsAccidentDetectShow = true;
            }
            if (!view.isShowParkingSensor()) {
                view.navigate(ScreenId.PARKING_SENSOR, args);
            }
        });
    }

    /**
     * パーキングセンサー画面の再表示
     *
     * @param args Bundle
     */
    public void onReShowParkingSensorAction(Bundle args) {
        StatusHolder holder = mStatusCase.execute();
        boolean isDisplayParkingSensor = holder.getCarDeviceStatus().isDisplayParkingSensor;
        if (isDisplayParkingSensor) {
            Optional.ofNullable(getView()).ifPresent(view -> {
                if (!view.isShowParkingSensor()) {
                    view.navigate(ScreenId.PARKING_SENSOR, args);
                }
            });
        }
    }

    /**
     * ADAS警告画面の表示
     *
     * @param args Bundle
     */
    public void onShowAdasWarningAction(Bundle args) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (view.isShowSpeechRecognizerDialog()) {
                //closeSpeechRecognizer(false);
                return;
            }
            if (view.isShowReadMessageDialog()) {
                //view.dismissReadMessageDialog();
                //finishReadingMessage();
                return;
            }
            if (view.isShowSearchContainer()) {
                //view.dismissSearchContainer();
                return;
            }
            if (view.isShowContactContainer()) {
                //view.dismissContactContainer();
                return;
            }
            if (!view.isShowAdasWarning()) {
                view.navigate(ScreenId.ADAS_WARNING, args);
            }
        });
    }

    public String getStackPopUp() {
        if(mStackPopUp.size()>0) {
            return mStackPopUp.get(mStackPopUp.size() - 1);
        }
        return null;
    }

    public void removeStackPopUp(String tag) {
        mStackPopUp.remove(tag);
    }

    public void clearStackPopUp() {
        mStackPopUp.clear();
    }

    /**
     * 車載機エラー画面の表示
     *
     * @param args Bundle
     */
    public void onShowCarDeviceErrorAction(Bundle args) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (view.isShowSpeechRecognizerDialog()) {
                closeSpeechRecognizer(false);
            }
            if (view.isShowReadMessageDialog()) {
                view.dismissReadMessageDialog();
                finishReadingMessage();
            }
            if (view.isShowSearchContainer()) {
                view.dismissSearchContainer();
            }
            if (view.isShowContactContainer()) {
                view.dismissContactContainer();
            }
            String errorTag = args.getString("errorTag");
            String errorTitle = args.getString("errorTitle");
            String errorText = args.getString("errorText");
            if (!view.isShowCarDeviceErrorDialog(errorTag)) {
                Bundle bundle = new Bundle();
                bundle.putString(StatusPopupDialogFragment.TAG, errorTag);
                //bundle.putString(StatusPopupDialogFragment.TITLE, errorTitle);
                bundle.putString(StatusPopupDialogFragment.MESSAGE, errorText);
                bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
                mStackPopUp.add(errorTag);
                view.navigate(ScreenId.CAR_DEVICE_ERROR, bundle);
            }
        });
    }

    /**
     * SXM Subscription Update画面の表示
     *
     * @param args Bundle
     */
    public void onShowSubscriptionUpdateAction(Bundle args) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            SxmMediaInfo currSxm = mStatusCase.execute().getCarDeviceMediaInfoHolder().sxmMediaInfo;
            if (currSxm.subscriptionUpdatingShowing) {
                if (view.isShowSpeechRecognizerDialog()) {
                    closeSpeechRecognizer(false);
                }
                if (view.isShowReadMessageDialog()) {
                    view.dismissReadMessageDialog();
                    finishReadingMessage();
                }
                if (view.isShowSearchContainer()) {
                    view.dismissSearchContainer();
                }
                if (view.isShowContactContainer()) {
                    view.dismissContactContainer();
                }
                if (!view.isShowCarDeviceErrorDialog(TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE);
                    bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getResources().getString(R.string.ply_076));
                    bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
                    mStackPopUp.add(TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE);
                    view.navigate(ScreenId.SXM_SUBSCRIPTION_UPDATE, bundle);
                }
            } else {
                removeStackPopUp(TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE);
                view.dismissCarDeviceErrorDialog(TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE);
            }
        });
    }

    /**
     * SXM購読更新ダイアログ承認
     */
    public void onReleaseSubscription() {
        mControlSxmCase.releaseSubscriptionUpdating();
    }

    /**
     * 権限許可
     */
    public void onFinishRequestPermissions(){
        mEventBus.post(new StartGetRunningStatusEvent());
    }

    /**
     * 衝突検知設定
     *
     * @param newValue スイッチの状態
     */
    public void onImpactDetectionChange(boolean newValue) {
        mPreference.setImpactDetectionEnabled(newValue);
    }

    public void directCall() {
        mEventBus.post(new SmartPhoneControlCommandEvent(SmartPhoneControlCommand.DIRECT_CALL));
    }

    /**
     * 画面遷移イベントハンドラ
     *
     * @param ev 画面遷移イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNavigateEvent(NavigateEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            Optional.ofNullable(getView()).ifPresent(view -> view.navigate(ev.screenId, ev.args));
        }
    }

    /**
     * 画面差し戻しイベントハンドラ
     *
     * @param ev 画面差し戻しイベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBackEvent(GoBackEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            Optional.ofNullable(getView()).ifPresent(MainView::goBack);
        }
    }

    /**
     * ダイアログクローズイベントハンドラ
     *
     * @param ev ダイアログクローズイベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCloseDialogEvent(CloseDialogEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            Optional.ofNullable(getView()).ifPresent(view -> view.closeDialog(ev.screenId));
        }
    }

    /**
     * 背景変更イベント
     *
     * @param ev 背景変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBackgroundChangeEvent(BackgroundChangeEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            Optional.ofNullable(getView()).ifPresent(view -> {
                ThemeType type = mPreference.getThemeType();
                if (type.isVideo()) {
                    Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + type.getResourceId());
                    view.setCaptureImage(ev.isBlur, uri);
                }
                view.changeBackgroundBlur(ev.isBlur);
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCaptureSetEvent(CaptureSetEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            Optional.ofNullable(getView()).ifPresent(view -> {
                ThemeType type = mPreference.getThemeType();
                if (type.isVideo()) {
                    Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + type.getResourceId());
                    view.setCaptureImage(ev.isCapture, uri);
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayPositionChangeAction(AppMusicPlayPositionChangeEvent event) {
        if (App.getApp(mContext).isForeground()) {
            Optional.ofNullable(getView()).ifPresent(MainView::reloadBackground);
        }
    }

    public void onCloseDialogAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            ThemeType type = mPreference.getThemeType();
            if (type.isVideo()) {
                view.setCaptureImage(false, null);
            }
            view.changeBackgroundBlur(false);
        });
    }

    public void changeBackground(){
        mEventBus.post(new BackgroundChangeEvent(true));
    }

    /**
     * 車載機接続イベントハンドラ.
     * <p>
     * 利用規約同意前は表示しない。
     *
     * @param ev 車載機接続イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onCrpSessionStartedEvent(CrpSessionStartedEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            sessionStart();
        }
        mCommandIntentMap.clear();
    }

    /**
     * 車載機切断イベントハンドラ.
     *
     * @param ev 車載機切断イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onCrpSessionStoppedEvent(CrpSessionStoppedEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            sessionStop();
        }
        mCommandIntentMap.clear();
    }

    /**
     * 車載機エラーイベントハンドラ
     *
     * @param ev 車載機エラーイベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onCrpSessionErrorEvent(CrpSessionErrorEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            showErrorDialog();
        }
    }

    /**
     * 連携済みか否か
     */
    public boolean isSessionConnected(){
        return mStatusCase.execute().getSessionStatus() == SessionStatus.STARTED;
    }

    private void sessionStop() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            //表示中ダイアログを消す
            if (view.isShowCaution()) {
                view.dismissCaution();
            }
            if (view.isShowAccidentDetect()) {
                view.dismissAccidentDetect();
            }
			mIsAccidentDetectShow = false;
            if (view.isShowParkingSensor()) {
                view.dismissParkingSensor();
            }
            if (view.isShowAdasWarning()) {
                view.dismissAdasWarning();
            }
            if (view.isShowCarDeviceErrorDialog()) {
                view.dismissCarDeviceErrorDialog();
            }
            if (view.isShowReadMessageDialog()) {
                view.dismissReadMessageDialog();
                finishReadingMessage();
            }
            if (view.isShowSpeechRecognizerDialog()) {
                closeSpeechRecognizer(false);
            }
            view.stopAdas();
            mAdas.releaseWarning();

            if (sIsVersionQ) {
                if (!Settings.canDrawOverlays(mContext)) {
                    if (!view.isShowPromptAuthorityPermissionDialog()) {
                        view.showPromptAuthorityPermissionDialog(Bundle.EMPTY);
                    }
                }
            }
            //連携切断でAdas課金画面に遷移する場合はApp連携方法ダイアログか切断ダイアログを表示しない
            if(!mIsAdasBillingSessionStop) {
                if(!mPreference.isAppConnectMethodNoDisplayAgain()) {
                    if (!view.isShowAppConnectMethodDialog()) {
                        view.showAppConnectMethodDialog(Bundle.EMPTY);
                    }
                }else{
                    if(!view.isShowSessionStopped()) {
                        view.showSessionStopped(Bundle.EMPTY);
                    }
                }
                mEventBus.post(new NavigateEvent(ScreenId.UNCONNECTED_CONTAINER, Bundle.EMPTY));
            }
        });

        mIsInitializedReadText = false;
        mReadText.terminate();
    }

    private void sessionStart() {
        StatusHolder holder = mStatusCase.execute();
        mIsAdasBillingSessionStop = false;
        if (mPreference.isAgreedEulaPrivacyPolicy()) {
            if (mPreference.isFirstInitialSettingCompletion()) {
                mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));

            } else {
                if (holder.getProtocolSpec().isSphCarDevice()) {
                    //mControlSource.selectSource(MediaSourceType.OFF);
                    transitionFirstInitialSetting();

                    if(!mPreference.isConfiguredShortCutButtonEnabled()){
                        mPreference.setShortCutButtonEnabled(false);
                        mPreference.setConfiguredShortCutButtonEnabled(true);
                    }
                } else {
                    mPreference.setFirstInitialSettingCompletion(true);
                    mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));

                    mPreference.setConfiguredShortCutButtonEnabled(true);
                }
            }
            showCaution();
        }

        //接続方式がUSBで現在のソースがUSBであればAPP MUSICにソース変更
        if(holder.getConnectionType()== ConnectionType.USB&&holder.getCarDeviceStatus().sourceType==MediaSourceType.USB){
            if(mPreference.isAgreedEulaPrivacyPolicy()&&!mPreference.isFirstInitialSettingCompletion()&&holder.getProtocolSpec().isSphCarDevice()){
                //mControlSource.selectSource(MediaSourceType.OFF);
            }else {
                mControlSource.selectSource(MediaSourceType.APP_MUSIC);
            }
        }
        mIsInitializedReadText = false;
        mReadText.initialize(this);

        if(holder.getAppStatus().isAlexaAvailableCountry) {
            startAlexa();
        }
    }

    /**
     * Caution表示イベントハンドラ.
     *
     * @param ev Caution表示イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShowCautionEvent(ShowCautionEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            showCaution();
        }
    }

    private void showCaution() {
        StatusHolder holder = mStatusCase.execute();
        if(!holder.getAppStatus().isAgreedCaution) {
            if (isSessionConnected()) {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    if(view.isShowParkingSensor()){
                        view.dismissParkingSensor();
                    }
                    if (!view.isShowCaution()) {
                        view.showCaution(Bundle.EMPTY);
                    }
                });
            }
        }
    }

    /**
     * メニュー解除イベントハンドラ.
     *
     * @param ev メニュー解除イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExitMenuEvent(ExitMenuEvent ev) {
        mIsSubscribeExitMenu = true;
        if (App.getApp(mContext).isForeground()) {
            exitSetting();
        }
    }

    private void exitSetting() {
        if (mIsSubscribeExitMenu) {
            Optional.ofNullable(getView()).ifPresent(MainView::exitSetting);
            mIsSubscribeExitMenu = false;
        }
    }

    /**
     * ソース変更イベントハンドラ.
     *
     * @param ev ソース変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent ev) {
        CarDeviceStatus status = mStatusCase.execute().getCarDeviceStatus();
        if (status.sourceType != MediaSourceType.BT_PHONE&&mSpeechRecognizerDevice == PrepareSpeechRecognizer.Device.HEADSET
                ||status.sourceType != MediaSourceType.APP_MUSIC&&mSpeechRecognizerDevice == PrepareSpeechRecognizer.Device.PHONE) {
            if (mFinishBluetoothHeadset != null) {
                mFinishBluetoothHeadset.execute(() -> {
                    // nothing to do
                }, true);
            }
            if (App.getApp(mContext).isForeground()) {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    stopSpeechRecognizer();

                    if (status.sourceStatus == MediaSourceStatus.CHANGE_COMPLETED) {
                        if(mRecognizeResults != null) {
                            RecognizeResultAction(mRecognizeResults);
                        }
                    }
                });
            }
        }

        if(mIsAdasStarted) {
            mAdas.checkAdasError();
        }
    }

    private void transitionFirstInitialSetting() {
        SettingsParams params = new SettingsParams();
        params.pass = mContext.getString(R.string.set_104);
        params.mScreenId = ScreenId.SETTINGS_SYSTEM_INITIAL;
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_CONTAINER, params.toBundle()));
    }

    public void showErrorDialog(){
        if(mStatusCase.execute().getAppStatus().errorType != null) {
            Optional.ofNullable(getView()).ifPresent(view -> {
                Timber.i("showErrorDialog()");
                Bundle bundle = new Bundle();
                bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ERROR);
                bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getString(R.string.err_015));
                bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
                view.navigate(ScreenId.USB_ERROR, bundle);
            });
        }
    }

    // MARK - ADAS
    public void setAdasAvailable(SimCountryIso simCountryIso){
        List countryList = Arrays.asList(SimCountryIso.US, SimCountryIso.CA, SimCountryIso.NO_AVAILABLE);
        boolean available = !countryList.contains(simCountryIso);
        AppStatus appStatus = mStatusCase.execute().getAppStatus();
        if(appStatus.adasSimJudgement) {
            appStatus.isAdasAvailableCountry = available;
        }else{
            appStatus.isAdasAvailableCountry = true;
        }
    }

    public void onAlexaAvailableConfirm() {
        mPreference.setIsAlexaAvailableConfirmShowed(true);
    }

    public void setPurchase(boolean isPurchased){
        mStatusCase.execute().getAppStatus().adasPurchased = isPurchased;
        if(isPurchased){
            mPreference.setAdasBillingRecord(true);
        }
        mEventBus.post(new AdasPurchaseStatusChangeEvent());
    }

    public void setAdasPrice(String price){
        mStatusCase.execute().getAppStatus().adasPrice = price;
        mEventBus.post(new AdasPriceChangeEvent());
    }

    public void setAdasBillingCheck(boolean isChecked){
        mStatusCase.execute().getAppStatus().adasBillingCheck = isChecked;
    }

    public void startDeviceConnectionSuppress(){
        mStatusCase.execute().getAppStatus().deviceConnectionSuppress = true;
        mEventBus.post(new DeviceConnectionSuppressEvent());
    }

    public void finishDeviceConnectionSuppress(){
        //Timber.d("Overlay:finishDeviceConnectionSuppress:canDrawOverlays=" + Settings.canDrawOverlays(mContext));
        //購入情報チェックが非同期で行われるため、セーフ処理
        if(sIsVersionQ&&!Settings.canDrawOverlays(mContext)){
            mStatusCase.execute().getAppStatus().deviceConnectionSuppress = true;
            return;
        }
        mStatusCase.execute().getAppStatus().adasBillingCheck = true;
        if(isAlexaAvailableConfirmNeeded()) {
            mStatusCase.execute().getAppStatus().deviceConnectionSuppress = true;
            showAlexaAvailableConfirmDialog();
            return;
        }

        mStatusCase.execute().getAppStatus().deviceConnectionSuppress = false;
        mEventBus.post(new DeviceConnectionSuppressEvent());
        showAppConnectMethodDialog();
    }

    public void suppressDeviceConnection(ScreenId screenId){
        AppStatus status = mStatusCase.execute().getAppStatus();
		if((sIsVersionQ&&!Settings.canDrawOverlays(mContext))||!mPreference.isAgreedEulaPrivacyPolicy()||isAlexaAvailableConfirmNeeded()) {
           return;
        }
		if(mPreference.isAdasBillingRecord()){
            if(!status.adasBillingCheck||screenId==ScreenId.TIPS) {
                return;
            }
        }
        if(!screenId.isDialog()) {
            if (screenId == ScreenId.ADAS_TUTORIAL || screenId == ScreenId.ADAS_USAGE_CAUTION
                    || screenId == ScreenId.ADAS_BILLING) {
                if(!mStatusCase.execute().getAppStatus().adasPurchased&&mPreference.getAdasTrialState()!=AdasTrialState.TRIAL_DURING) {
                    status.deviceConnectionSuppress = true;
                }
            } else {
                if(status.deviceConnectionSuppress) {
                    status.deviceConnectionSuppress = false;
                }
            }
        }
    }

    public void showAdasBillingStatusErrorDialog(){
        Timber.d("showAdasBillingStatusErrorDialog");
        Bundle bundle = new Bundle();
        String text = mContext.getString(R.string.tip_024);
        bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ADAS_BILLING_STATUS_ERROR);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, text);
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        bundle.putInt(StatusPopupDialogFragment.POSITIVE_TEXT, R.string.com_003);
        bundle.putBoolean(StatusPopupDialogFragment.NEGATIVE, true);
        bundle.putInt(StatusPopupDialogFragment.NEGATIVE_TEXT, R.string.tip_023);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.navigate(ScreenId.CAR_DEVICE_ERROR, bundle);
                });
            }
        });
    }

    public void showAdasBillingStatusFailureDialog(){
        Timber.d( "showAdasBillingStatusFailureDialog");
        Bundle bundle = new Bundle();
        String text = mContext.getString(R.string.tip_025);
        bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ADAS_BILLING_STATUS_FAILURE);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, text);
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.navigate(ScreenId.CAR_DEVICE_ERROR, bundle);
                });
            }
        });
    }
    public void showAdasGetPriceErrorDialog(){
        Timber.d("showAdasGetPriceErrorDialog");
        Bundle bundle = new Bundle();
        String text = mContext.getString(R.string.set_375);
        bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ADAS_GET_PRICE_ERROR);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, text);
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.navigate(ScreenId.CAR_DEVICE_ERROR, bundle);
                });
            }
        });
    }
    public void showAdasPurchaseErrorDialog(){
        Timber.d("showAdasPurchaseErrorDialog");
        Bundle bundle = new Bundle();
        String text = mContext.getString(R.string.set_378);
        bundle.putString(StatusPopupDialogFragment.TAG, MainPresenter.TAG_DIALOG_ADAS_PURCHASE_ERROR);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, text);
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.navigate(ScreenId.CAR_DEVICE_ERROR, bundle);
                });
            }
        });
    }

    public void showAdasBillingRestoreFailureDialog(){
        Timber.d("showAdasBillingRestoreFailureDialog");
        Bundle bundle = new Bundle();
        String text = mContext.getString(R.string.set_380);
        bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ADAS_BILLING_RESTORE_FAILURE);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, text);
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.navigate(ScreenId.CAR_DEVICE_ERROR, bundle);
                });
            }
        });
    }
    public void showAdasBillingRestoreSuccessDialog(){
        Timber.d("showAdasBillingRestoreSuccessDialog");
        Bundle bundle = new Bundle();
        String text = mContext.getString(R.string.set_382);
        bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ADAS_BILLING_RESTORE_SUCCESS);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, text);
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.navigate(ScreenId.CAR_DEVICE_ERROR, bundle);
                });
            }
        });
    }

    public void showAdasConfirmDialog(){
        if(mPreference.getLastConnectedCarDeviceClassId()== CarDeviceClassId.MARIN)return;

        StatusHolder holder = mStatusCase.execute();
        //ADASお試し期間終了判定
        if(mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING){
            Calendar cal  = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getDefault());
            Date nowDate = cal.getTime();

            Date endDate = new Date(mPreference.getAdasTrialPeriodEndDate());
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeZone(TimeZone.getTimeZone("UTC"));
            endCal.setTime(endDate);
            endCal.setTimeZone(TimeZone.getDefault());
            endCal.clear(Calendar.MINUTE);
            endCal.clear(Calendar.SECOND);
            endCal.clear(Calendar.MILLISECOND);
            endCal.set(Calendar.HOUR_OF_DAY,0);
            endDate = endCal.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getDefault());
            Timber.d("AdasTrialPeriodNowDate:%s",sdf.format(nowDate));
            Timber.d("AdasTrialPeriodEndDate:%s",sdf.format(endDate));
            if(nowDate.after(endDate)||nowDate.equals(endDate)){
                mPreference.setAdasTrialState(AdasTrialState.TRIAL_END);
                //設定を変えない
                //mPreference.setAdasEnabled(false);
                Bundle bundle = new Bundle();
                String text = mContext.getString(R.string.hom_030);
                bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ADAS_TRIAL_END);
                bundle.putString(StatusPopupDialogFragment.MESSAGE, text);
                bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
                bundle.putInt(StatusPopupDialogFragment.POSITIVE_TEXT, R.string.hom_032);
                bundle.putBoolean(StatusPopupDialogFragment.NEGATIVE, true);
                bundle.putInt(StatusPopupDialogFragment.NEGATIVE_TEXT, R.string.hom_031);
                mEventBus.post(new NavigateEvent(ScreenId.MAIN_STATUS_DIALOG, bundle));
            }
        }
        //ADAS再キャリブレーション設定
        if ((holder.getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING)
                &&mPreferAdas.getAdasEnabled()) {
            Bundle bundle = new Bundle();
            String text = mContext.getString(R.string.hom_033);
            bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ADAS_RE_CALIBRATION);
            bundle.putString(StatusPopupDialogFragment.MESSAGE, text);
            bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
            bundle.putInt(StatusPopupDialogFragment.POSITIVE_TEXT, R.string.hom_035);
            bundle.putBoolean(StatusPopupDialogFragment.NEGATIVE, true);
            bundle.putInt(StatusPopupDialogFragment.NEGATIVE_TEXT, R.string.hom_034);
            mEventBus.post(new NavigateEvent(ScreenId.MAIN_STATUS_DIALOG, bundle));
        }
    }

    // Alexa機能利用ダイアログ表示処理
    public void showAlexaAvailableConfirmDialog() {
        Bundle bundle = new Bundle();
        String text = mContext.getString(R.string.sta_014);
        bundle.putString(StatusPopupDialogFragment.TAG, MainPresenter.TAG_DIALOG_ALEXA_AVAILABLE_CONFIRM);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, text);
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.navigate(ScreenId.CAR_DEVICE_ERROR, bundle);
                });
            }
        });
    }

    public void goAdasBilling() {
        mIsAdasBillingSessionStop = true;
        mCarDeviceConnection.sessionStop();
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_CONTAINER, createSettingsParams(ScreenId.ADAS_BILLING, mContext.getString(R.string.set_289))));
    }

    public void goCalibrationSetting(){
        mEventBus.post(new NavigateEvent(ScreenId.CALIBRATION_SETTING, createSettingsParams(ScreenId.HOME_CONTAINER, mContext.getString(R.string.set_036))));
    }

    /**
     * ADAS設定更新イベントハンドラ.
     *
     * @param ev ADAS設定更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onUpdateAdasEvent(UpdateAdasEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            if (mPreferAdas.getAdasEnabled()&&mIsAdasStarted) {
                Optional.ofNullable(getView()).ifPresent(MainView::updateAdas);
            }
        }
    }

    /**
     * ADASCalibration設定状態変更ハンドラ.
     *
     * @param ev ADASCalibration設定状態変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onAdasCalibrationStatusChangeEvent(AdasCalibrationStatusChangeEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            AppStatus appStatus = mStatusCase.execute().getAppStatus();
            Optional.ofNullable(getView()).ifPresent(view -> {
                if(isSessionConnected()||mPreference.isAdasPseudoCooperation()) {
                    //キャリブレーション中はADASを停止する。Cameraを同時に複数動作できないため。
                    if (mPreferAdas.getAdasEnabled()) {
                        if (appStatus.isAdasCalibrationSetting) {
                            view.stopAdas();
                        } else {
                            Timber.d("onAdasCalibrationStatusChangeEvent() startAdas");
                            view.startAdas();
                        }
                    }
                }
            });
        }
    }

    /**
     * キャリブレーション設定確認
     *
     * @return キャリブレーション設定済か否か {@code true}:設定済 {@code false}:未設定
     */
    public boolean checkConfiguredCalibration() {
        return mAdas.isSettingFinished();
    }

    public void checkAdasPermissionError(){
        mAdas.checkAdasPermission();
    }

    /**
     * ADAS初期化
     *
     * @param cameraImageHeight カメラ画素数高さ
     * @param cameraImageWidth  カメラ画素数幅
     * @return 初期化に成功したか否か {@code true}:初期化に成功 {@code false}:初期化に失敗
     */
    public boolean initAdas(int cameraImageHeight, int cameraImageWidth) {
        Timber.d("initAdas");
        if(!mAdas.checkAdasPermission()) {
            mIsAdasStarted = false;
            return false;
        }
        if (mAdas.init(cameraImageHeight, cameraImageWidth)) {
            mAdas.setFcw();
            mAdas.setLdw();
            mAdas.setPcw();
            mIsAdasStarted = true;
            return true;
        }
        mIsAdasStarted = false;
        return false;
    }

    /**
     * ADAS解放
     */
    public void releaseAdas() {
        if (mIsAdasStarted) {
            mIsAdasStarted = false;
            mAdas.release();
        }
    }
    /**
     * ADAS終了
     */
    public void finishAdas() {
        mPreferAdas.setAdasEnabled(false);
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * ADAS解析処理
     *
     * @param data 画像データ
     */
    public void processAdas(byte[] data) {
        mAdas.process(data);
    }


    // MARK - 音声認識

    /**
     * 音声認識前準備
     */
    public void prepareRecognizer() {
        Timber.d("prepareRecognizer");
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (mStatusCase.execute().getCarDeviceStatus().sourceType == MediaSourceType.BT_PHONE) {
                return;
            }
            if (view.isShowReadMessageDialog()) {
                view.dismissReadMessageDialog();
                finishReadingMessage();
            }
            if (view.isShowSearchContainer()) {
                view.dismissSearchContainer();
            }
            if (view.isShowContactContainer()) {
                view.dismissContactContainer();
            }
            if (view.isShowAccidentDetect() || view.isShowParkingSensor()) {
                return;
            }
            if (view.isShowSpeechRecognizerDialog()) {
                return;
            }
            StatusHolder holder = mStatusCase.execute();
            if(mPreference.getVoiceRecognitionType()==VoiceRecognizeType.ANDROID_VR){
                if(mStatusCase.execute().getPhoneSettingStatus().hfDevicesCountStatus == ConnectedDevicesCountStatus.NONE){
                    view.showToast(mContext.getString(R.string.err_038));
                }else {
                    mDeviceVoiceRecognition.start();
                }
            }else if(mPreference.getVoiceRecognitionType()== VoiceRecognizeType.ALEXA){
                if(view.isShowAlexaDialog()){
                    view.dismissAlexaDialog();
                }
                int state = PermissionChecker.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO);
                Bundle bundle = new Bundle();
                String title="";
                AmazonAlexaManager manager = AmazonAlexaManager.getInstance();
                if(manager==null){
                    return;
                }
                if(!holder.getAppStatus().alexaAuthenticated){
                    // Alexaにサインインしていない、またはAlexaのサインインの確認ができていない場合
                    title=mContext.getString(R.string.err_033);
                    bundle.putString(StatusPopupDialogFragment.TAG, title);
                    bundle.putString(StatusPopupDialogFragment.MESSAGE, title);
                    bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
                    mEventBus.post(new NavigateEvent(ScreenId.MAIN_STATUS_DIALOG, bundle));
                }else if(!AmazonAlexaManager.mIsAlexaConnection||!manager.isInitComp()||!AmazonAlexaManager.mIsDownChannelOpened){
                    // AVSとの接続が完了していない
                    title=mContext.getString(R.string.err_034);
                    bundle.putString(StatusPopupDialogFragment.TAG, title);
                    bundle.putString(StatusPopupDialogFragment.MESSAGE, title);
                    bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
                    mEventBus.post(new NavigateEvent(ScreenId.MAIN_STATUS_DIALOG, bundle));
                    SettingsUpdatedUtil.setLocale(mContext.getString(mPreference.getAlexaLanguage().locale));
                    manager.openDownChannel();
                }else if (state!= PermissionChecker.PERMISSION_GRANTED) {
                    // マイクのパーミッションが無い場合
                    title = mContext.getString(R.string.err_032);
                    bundle.putString(StatusPopupDialogFragment.TAG, title);
                    bundle.putString(StatusPopupDialogFragment.MESSAGE, title);
                    bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
                    mEventBus.post(new NavigateEvent(ScreenId.MAIN_STATUS_DIALOG, bundle));
                }else{
                    SettingsUpdatedUtil.setLocale(mContext.getString(mPreference.getAlexaLanguage().locale));
                    mIsAlexaStart = true;
                    holder.getAppStatus().alexaPreviousSourceType = holder.getCarDeviceStatus().sourceType;
                    if(holder.getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC) {
                        mEventBus.post(new NavigateEvent(ScreenId.ALEXA, Bundle.EMPTY));
                    }else{
                        mEventBus.post(new NavigateEvent(ScreenId.ALEXA, Bundle.EMPTY));
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.alexaStart));
                                mControlSource.selectSource(MediaSourceType.APP_MUSIC);
                            }
                        }, 1000);
                    }
                }
            }else {
                //状態初期化
                mResentVoiceCommand = null;
                mRecognizeResults = null;
                mIsRecognizeSourceChanged = false;
                mCurrentAvailableSourceType = mStatusCase.execute().getCarDeviceStatus().availableSourceTypes;
                mJudgeVoiceCase.setCurrentAvailableSourceType(mCurrentAvailableSourceType);
                mPreviousSourceType = holder.getCarDeviceStatus().sourceType;
                if (holder.getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC) {
                    mSearchType = VoiceRecognitionSearchType.LOCAL;
                } else {
                    mSearchType = VoiceRecognitionSearchType.GLOBAL;
                }
                mMicCase.execute(new PrepareSpeechRecognizer.PrepareCallback() {
                    @UiThread
                    @Override
                    public void onComplete(PrepareSpeechRecognizer.Device device, PrepareSpeechRecognizer.FinishBluetoothHeadset finishBluetoothHeadset) {
                        //Timber.d("MicCase.execute() onComplete device = %s , finishBluetoothHeadset = %s",device,finishBluetoothHeadset);
                        if (!isRegisterBroadcastReceiver) {
                            registerBroadcastReceiver();
                            isRegisterBroadcastReceiver = true;
                        }
                        mSpeechRecognizerDevice = device;
                        Bundle args = new Bundle();
                        args.putBoolean(SpeechRecognizerDialogPresenter.SEARCH_TYPE, mSearchType == VoiceRecognitionSearchType.LOCAL);
                        view.navigate(ScreenId.SPEECH_RECOGNIZER, args);
                        mStatusCase.execute().getAppStatus().isShowSpeechRecognizerDialog = true;
                        mFinishBluetoothHeadset = finishBluetoothHeadset;
                        if(mSpeechRecognizerDevice == PrepareSpeechRecognizer.Device.PHONE&&mPreviousSourceType!=MediaSourceType.APP_MUSIC){
                            mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.temporarySourceChange));
                            mControlSource.selectSource(MediaSourceType.APP_MUSIC);
                        }
                        if(mPreviousSourceType==MediaSourceType.APP_MUSIC){
                            SmartPhoneStatus status = holder.getSmartPhoneStatus();
                            if(status.playbackMode== PlaybackMode.PLAY){
                                mControlAppMusicSource.togglePlay();
                            }
                        }
                        view.startRecognizer();
                    }

                    @UiThread
                    @Override
                    public void onError() {
                        mSearchType = null;
                    }
                });
            }
        });
    }

    /**
     * 音声認識終了処理
     */
    public void finishRecognizer(boolean isRinging) {
        Timber.d("finishRecognizer");
        if (mFinishBluetoothHeadset != null && !isRinging) {
            Timber.d("dismissSpeechRecognizerDialog finishRecognizer");
            mFinishBluetoothHeadset.execute(mFinishCallback, false);
        } else {
            if (App.getApp(mContext).isForeground()) {
                Optional.ofNullable(getView()).ifPresent(view ->{
                    if(view.isShowSpeechRecognizerDialog()){
                        view.dismissSpeechRecognizerDialog();
                    }
                });
            }
        }
        mStatusCase.execute().getAppStatus().isShowSpeechRecognizerDialog = false;
        if(mPreviousSourceType==MediaSourceType.APP_MUSIC){
            SmartPhoneStatus status = mStatusCase.execute().getSmartPhoneStatus();
            if(status.playbackMode != PlaybackMode.PLAY){
                mControlAppMusicSource.togglePlay();
            }
        }
        if(mIsInitializedReadText) {
            mReadText.stopReading();
        }
        mIsRecognizerRestarted = false;
        mRecognizerRestartCount = 0;
        initBroadcastReceiver();
        initPhoneStateListener();
    }

    PrepareSpeechRecognizer.FinishCallback mFinishCallback = new PrepareSpeechRecognizer.FinishCallback() {
        @Override
        @UiThread
        public void onComplete() {
            if (App.getApp(mContext).isForeground()) {
                Timber.d("dismissSpeechRecognizerDialog isForeground");
                Optional.ofNullable(getView()).ifPresent(MainView::dismissSpeechRecognizerDialog);
            } else {
                Timber.d("dismissSpeechRecognizerDialog isNotForeground");
            }
            CarDeviceStatus status = mStatusCase.execute().getCarDeviceStatus();
            if (mRecognizeResults != null &&
                    status.sourceType != MediaSourceType.BT_PHONE &&
                    status.sourceStatus == MediaSourceStatus.CHANGE_COMPLETED) {
                RecognizeResultAction(mRecognizeResults);
            }
            mFinishBluetoothHeadset = null;
            if(mSpeechRecognizerDevice == PrepareSpeechRecognizer.Device.PHONE&&mPreviousSourceType != MediaSourceType.APP_MUSIC&&!mIsRecognizeSourceChanged){
                Timber.d("selectPreviousSource");
                mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.temporarySourceChangeBack));
                mControlSource.selectSource(mPreviousSourceType);
            }
        }
    };


    /**
     * 音声認識結果解析
     *
     * @param results 結果ワード群
     */
    public void onRecognizeResults(@Nullable ArrayList<String> results) {
        Timber.d("onRecognizeResults");

        mStopSpeechRecognizerTask.stop();
        mRecognizeResults = null;
        if(results != null){
            Timber.d("onRecognizeResults:results=" + results.toString());
/*            Optional.ofNullable(getView()).ifPresent(view -> {
                view.showToast("認識ワード：" + results.toString());
            });*/
        }
        if (results == null) {
            speechPromotion(null);
            return;
        } else if (mResentVoiceCommand == null) {
            // 発話しているが枕詞が決まっていない
            JudgeVoiceCommand.JudgeResult result = mJudgeVoiceCase.execute(results, mSearchType);
            if (result == null) {
                // 枕詞が見つからない
                speechPromotion(null);
                return;
            } else {
                // 枕詞見つかる
                if (result.mSearchWords == null) {
                    // 発話内容がない
                    mResentVoiceCommand = result.mVoiceCommand;
                    mRecognizerRestartCount = 0;
                    setSpeechRecognizerText();
                    speechPromotion(result.mVoiceCommand);
                    return;
                } else {
                    // 発話内容ある
                    if (result.mVoiceCommand == VoiceCommand.AUDIO) {
                        String judgeWord = judgeSearchWord(VoiceCommand.AUDIO, new ArrayList<>(Arrays.asList(result.mSearchWords)));
                        if (!AUDIO_ACTION.containsKey(judgeWord) ||
                                !mCurrentAvailableSourceType.contains(AUDIO_ACTION.get(judgeWord))) {
                            // 枕詞AUDIOだが中身が間違っている
                            mResentVoiceCommand = result.mVoiceCommand;
                            mRecognizerRestartCount = 0;
                            setSpeechRecognizerText();
                            speechPromotion(result.mVoiceCommand);
                            return;
                        }
                    } else if (result.mVoiceCommand == VoiceCommand.SETTING) {
                        String judgeWord = judgeSearchWord(VoiceCommand.SETTING, new ArrayList<>(Arrays.asList(result.mSearchWords)));
                        if (!SETTING_ACTION.containsKey(judgeWord)) {
                            // 枕詞SETTINGだが中身が間違っている
                            mResentVoiceCommand = result.mVoiceCommand;
                            mRecognizerRestartCount = 0;
                            setSpeechRecognizerText();
                            speechPromotion(result.mVoiceCommand);
                            return;
                        }
                    }
                }
            }
        } else {
            // 発話して枕詞が決まっている
            if (mResentVoiceCommand == VoiceCommand.AUDIO) {
                String sourceName = judgeSearchWord(VoiceCommand.AUDIO, results);
                if (!AUDIO_ACTION.containsKey(sourceName) ||
                        !mCurrentAvailableSourceType.contains(AUDIO_ACTION.get(sourceName))) {
                    setSpeechRecognizerText();
                    speechPromotion(mResentVoiceCommand);
                    return;
                }
            } else if (mResentVoiceCommand == VoiceCommand.SETTING) {
                String settingName = judgeSearchWord(VoiceCommand.SETTING, results);
                if (!SETTING_ACTION.containsKey(settingName)) {
                    setSpeechRecognizerText();
                    speechPromotion(mResentVoiceCommand);
                    return;
                }
            }
        }

        mRecognizeResults = results;
        stopSpeechRecognizer();
    }

    private void setSpeechRecognizerText(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            String text = "";
            switch (mResentVoiceCommand) {
                case NAVI:
                    text = mContext.getString(R.string.rec_021);
                    break;
                case PHONE:
                    text = mContext.getString(R.string.rec_023);
                    break;
                case AUDIO:
                    text = mContext.getString(R.string.rec_025);
                    break;
                case SETTING:
                    text = mContext.getString(R.string.rec_027);
                    break;
                case ARTIST:
                    text = mContext.getString(R.string.rec_015);
                    break;
                case ALBUM:
                    text = mContext.getString(R.string.rec_017);
                    break;
                case SONG:
                    text = mContext.getString(R.string.rec_019);
                    break;
            }
            view.setSpeechRecognizerText(text);
        });
    }

    private String judgeSearchWord(VoiceCommand command, ArrayList<String> words) {
        String[] searchWords = words.subList(0, Math.min(words.size(), 10)).toArray(new String[0]);

        String sourceName = "";
        for (String text : searchWords) {
            sourceName = mJudgeVoiceCase.judgeSearchWord(command, text);
            if ((!sourceName.equals(UNKNOWN)) && (!sourceName.equals(""))) break;
        }
        return sourceName;
    }

    private void speechPromotion(@Nullable VoiceCommand command) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setSpeechRecognizerState(SpeechRecognizerDialogFragment.StateType.SPEAKING));
        if (command == null) {
            speechText(R.string.tts_017);
        } else {
            switch (command) {
                case NAVI:
                    speechText(R.string.tts_012);
                    break;
                case PHONE:
                    speechText(R.string.tts_013);
                    break;
                case AUDIO:
                    speechText(R.string.tts_014);
                    break;
                case SETTING:
                    speechText(R.string.tts_015);
                    break;
                case ARTIST:
                    speechText(R.string.tts_009);
                    break;
                case ALBUM:
                    speechText(R.string.tts_010);
                    break;
                case SONG:
                    speechText(R.string.tts_011);
                    break;
            }
        }
    }

    private void RecognizeResultAction(ArrayList<String> results) {
        VoiceCommand command;
        String[] searchWords;
        if(results != null){
            Timber.d("RecognizeResultAction:results=" + results.toString());
        }
        if (mResentVoiceCommand == null) {
            JudgeVoiceCommand.JudgeResult result = mJudgeVoiceCase.execute(results, mSearchType);
            command = result == null ? null : result.mVoiceCommand;
            searchWords = result == null ? null : result.mSearchWords;
        } else {
            command = mResentVoiceCommand;
            searchWords = results.size() == 0 ? null : results.subList(0, Math.min(results.size(), 10)).toArray(new String[0]);
            mResentVoiceCommand = null;
        }

        if (command != null) {
            switch (command) {
                case NAVI:
                    searchNavi(searchWords);
                    break;
                case PHONE:
                    searchPhone(searchWords);
                    break;
                case AUDIO:
                    searchAudio(searchWords);
                    break;
                case SETTING:
                    searchSetting(searchWords);
                    break;
                case ARTIST:
                    // 音声認識検索 - アーティスト名検索
                    searchMusic(VoiceCommand.ARTIST, searchWords);
                    break;
                case ALBUM:
                    // 音声認識検索 - アルバム名検索
                    searchMusic(VoiceCommand.ALBUM, searchWords);
                    break;
                case SONG:
                    // 音声認識検索 - 曲名検索
                    searchMusic(VoiceCommand.SONG, searchWords);
                    break;
            }
        }

        mRecognizeResults = null;
    }

    private Bundle createSettingsParams(ScreenId screenId, String pass) {
        SettingsParams params = new SettingsParams();
        params.mScreenId = screenId;
        params.pass = pass;
        return params.toBundle();
    }

    public void restartRecognizer(Bundle results) {
        mIsRecognizerRestarted = true;
        mRecognizerRestartCount++;
        if (mRecognizerRestartCount > RECOGNIZER_RESTART_REMIT) {
            if (mResentVoiceCommand != null&&mRecognizeResults!=null) {
                mRecognizeResults.clear();
            }
            Optional.ofNullable(getView()).ifPresent(MainView::finishSpeechRecognizer);
        } else {
            onRecognizeResults(results == null ? null : results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
        }
    }

    private void speechText(@StringRes int resId) {
        Optional.ofNullable(getView()).ifPresent(view -> mCheckTtsCase.execute(result -> {
            switch (result) {
                case AVAILABLE:
                    mReadText.startReading(resId);
                    break;
                case LANG_MISSING_DATA:
                case LANG_NOT_SUPPORTED:
                case MAY_NOT_DISABLED:
                default:
                    if ((mResentVoiceCommand != null) || (mIsRecognizerRestarted)) {
                        view.startRecognizer();
                        mIsRecognizerRestarted = false;
                    } else {
                        view.finishSpeechRecognizer();
                    }
                    break;
            }
        }));
    }

    private final static Map<String, MediaSourceType> AUDIO_ACTION = new HashMap<String, MediaSourceType>() {{
        put(JudgeVoiceCommand.JudgeResult.SOURCE_RADIO, MediaSourceType.RADIO);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_DAB, MediaSourceType.DAB);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_HD_RADIO, MediaSourceType.HD_RADIO);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_TI, MediaSourceType.TI);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_CD, MediaSourceType.CD);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_USB, MediaSourceType.USB);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_AUX, MediaSourceType.AUX);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_BT_AUDIO, MediaSourceType.BT_AUDIO);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_PANDORA, MediaSourceType.PANDORA);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_SPOTIFY, MediaSourceType.SPOTIFY);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_APP_MUSIC, MediaSourceType.APP_MUSIC);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_SIRIUS_XM, MediaSourceType.SIRIUS_XM);
        put(JudgeVoiceCommand.JudgeResult.SOURCE_OFF, MediaSourceType.OFF);
    }};

    private void searchAudio(@Nullable String[] searchText) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(searchText == null){
                return;
            }

            if (searchText.length > 0) {
                String sourceName = "";
                for (String text : searchText) {
                    sourceName = mJudgeVoiceCase.judgeSearchWord(VoiceCommand.AUDIO, text);
                    if ((!sourceName.equals(UNKNOWN)) && (!sourceName.equals(""))) break;
                }

                if (AUDIO_ACTION.containsKey(sourceName)) {
                    MediaSourceType type = AUDIO_ACTION.get(sourceName);

                    if (mCurrentAvailableSourceType.contains(type)) {
                        mIsRecognizeSourceChanged = true;
                        mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.speechRecognizeSourceChange));
                        mControlSource.selectSource(type);
                        view.navigate(ScreenId.PLAYER_CONTAINER, Bundle.EMPTY);
                    } else {
                        // 有効ではないソースを認識
                        Timber.d("UnsupportedSource");
                    }
                }
            }
        });
    }

    private final static Map<String, SettingActionParams> SETTING_ACTION = new HashMap<String, SettingActionParams>() {{
        put(JudgeVoiceCommand.JudgeResult.SETTING_SYSTEM, new SettingActionParams(SettingEntrance.SYSTEM.getResource(), ScreenId.SETTINGS_SYSTEM));
        put(JudgeVoiceCommand.JudgeResult.SETTING_VOICE, new SettingActionParams(SettingEntrance.VOICE.getResource(), ScreenId.SETTINGS_VOICE));
        put(JudgeVoiceCommand.JudgeResult.SETTING_NAVI, new SettingActionParams(SettingEntrance.NAVIGATION.getResource(), ScreenId.SETTINGS_NAVIGATION));
        put(JudgeVoiceCommand.JudgeResult.SETTING_MESSAGE, new SettingActionParams(SettingEntrance.MESSAGE.getResource(), ScreenId.SETTINGS_MESSAGE));
        put(JudgeVoiceCommand.JudgeResult.SETTING_PHONE, new SettingActionParams(SettingEntrance.PHONE.getResource(), ScreenId.SETTINGS_PHONE));
        put(JudgeVoiceCommand.JudgeResult.SETTING_CAR_SAFETY, new SettingActionParams(SettingEntrance.CAR_SAFETY.getResource(), ScreenId.CAR_SAFETY_SETTINGS));
        put(JudgeVoiceCommand.JudgeResult.SETTING_THEME, new SettingActionParams(SettingEntrance.APPEARANCE.getResource(), ScreenId.SETTINGS_THEME));
        put(JudgeVoiceCommand.JudgeResult.SETTING_SOUND_FX, new SettingActionParams(SettingEntrance.SOUND_FX.getResource(), ScreenId.SETTINGS_FX));
        put(JudgeVoiceCommand.JudgeResult.SETTING_AUDIO, new SettingActionParams(SettingEntrance.AUDIO.getResource(), ScreenId.SETTINGS_AUDIO));
        put(JudgeVoiceCommand.JudgeResult.SETTING_RADIO, new SettingActionParams(SettingEntrance.RADIO.getResource(), ScreenId.SETTINGS_RADIO));
        put(JudgeVoiceCommand.JudgeResult.SETTING_DAB, new SettingActionParams(SettingEntrance.DAB.getResource(), ScreenId.SETTINGS_DAB));
        put(JudgeVoiceCommand.JudgeResult.SETTING_HD_RADIO, new SettingActionParams(SettingEntrance.HD_RADIO.getResource(), ScreenId.SETTINGS_HD_RADIO));
        put(JudgeVoiceCommand.JudgeResult.SETTING_FUNCTION, new SettingActionParams(SettingEntrance.FUNCTION.getResource(), ScreenId.SETTINGS_APP));
        put(JudgeVoiceCommand.JudgeResult.SETTING_INFO, new SettingActionParams(SettingEntrance.INFORMATION.getResource(), ScreenId.SETTINGS_INFORMATION));
    }};

    static class SettingActionParams {
        @StringRes int pass;
        ScreenId id;

        SettingActionParams(@IdRes int pass, ScreenId id) {
            this.pass = pass;
            this.id = id;
        }
    }

    private void searchSetting(@Nullable String[] searchText) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (searchText != null) {
                String settingName = "";
                for (String text : searchText) {
                    settingName = mJudgeVoiceCase.judgeSearchWord(VoiceCommand.SETTING, text);
                    if ((!settingName.equals(UNKNOWN)) && (!settingName.equals(""))) break;
                }

                if (SETTING_ACTION.containsKey(settingName)) {
                    StatusHolder holder = mStatusCase.execute();
                    CarDeviceStatus status = holder.getCarDeviceStatus();
                    CarDeviceSpec spec = holder.getCarDeviceSpec();
                    SettingActionParams params = SETTING_ACTION.get(settingName);
                    boolean isNavigateEntrance = false;

                    switch (params.id) {
                        case SETTINGS_SYSTEM:
                            if (!spec.systemSettingSupported || !status.systemSettingEnabled) {
                                isNavigateEntrance = true;
                            }
                            break;
                        case SETTINGS_THEME:
                            if (!spec.illuminationSettingSupported || !status.illuminationSettingEnabled) {
                                isNavigateEntrance = true;
                            }
                            break;
                        case SETTINGS_FX:
                            if (!spec.soundFxSettingSupported || !status.soundFxSettingEnabled) {
                                isNavigateEntrance = true;
                            }
                            break;
                        case SETTINGS_AUDIO:
                            if (!holder.isAudioSettingSupported() || !holder.isAudioSettingEnabled()) {
                                isNavigateEntrance = true;
                            }
                            break;
                        case SETTINGS_RADIO:
                            if (!spec.tunerFunctionSettingSupported || !status.tunerFunctionSettingEnabled) {
                                isNavigateEntrance = true;
                            }
                            break;
                        case SETTINGS_DAB:
                            if (!spec.dabFunctionSettingSupported || !status.dabFunctionSettingEnabled) {
                                isNavigateEntrance = true;
                            }
                            break;
                        case SETTINGS_HD_RADIO:
                            if (!spec.hdRadioFunctionSettingSupported || !status.hdRadioFunctionSettingEnabled) {
                                isNavigateEntrance = true;
                            }
                            break;
                        case CAR_SAFETY_SETTINGS:
                            if(!spec.parkingSensorSettingSupported && !mPreference.isVersion_1_1_FunctionEnabled()){
                                isNavigateEntrance = true;
                            }
                            break;
                    }

                    if (isNavigateEntrance) {
                        view.navigate(ScreenId.SETTINGS_CONTAINER, createSettingsParams(ScreenId.SETTINGS_ENTRANCE, mContext.getString(R.string.hom_015)));
                    } else {
                        view.navigate(ScreenId.SETTINGS_CONTAINER, createSettingsParams(params.id, mContext.getString(params.pass)));
                    }
                    return;
                }
            }

            view.navigate(ScreenId.SETTINGS_CONTAINER, createSettingsParams(ScreenId.SETTINGS_ENTRANCE, mContext.getString(R.string.hom_015)));
        });
    }

    private void searchMusic(VoiceCommand command, @Nullable String[] searchText) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (searchText == null) {
                return;
            }

            Bundle args = SearchContentParams.toBundle(command, searchText);
            ThemeType type = mPreference.getThemeType();
            if (type.isVideo()) {
                Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + type.getResourceId());
                view.setCaptureImage(true, uri);
            } else {
                view.changeBackgroundBlur(true);
            }
            view.showSearchContainer(args);
        });
    }

    private void searchNavi(@Nullable String[] searchText) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            BaseApp baseApp = null;
            if (mPreference.getLastConnectedCarDeviceClassId() == CarDeviceClassId.MARIN) {
                try {
                    if(mPreference.getNavigationMarinApp() == null){
                        view.showToast(mContext.getString(R.string.err_035));
                    }else {
                        baseApp = MarinApp.fromPackageNameNoThrow(mPreference.getNavigationMarinApp().packageName);
                        if (baseApp != null) {
                            Intent intent = baseApp.createMainIntent(mContext);
                            view.startMarin(intent);
                            return;
                        }
                        baseApp = NaviApp.fromPackageName(mPreference.getNavigationMarinApp().packageName);
                    }
                } catch (IllegalArgumentException e) {
                    view.showToast(mContext.getString(R.string.err_035));
                }
            } else {
                try {
                    baseApp = NaviApp.fromPackageName(mPreference.getNavigationApp().packageName);
                } catch (IllegalArgumentException e) {
                    view.showToast(mContext.getString(R.string.err_007));
                }
            }
            if (baseApp instanceof NaviApp) {
                NaviApp naviApp = (NaviApp) baseApp;
                if (searchText != null && !searchText[0].isEmpty()) {
                    // 音声認識検索 - ナビ起動(ルート案内)
                    mLocationCase.execute(searchText[0], new GetAddressFromLocationName.Callback() {
                        @Override
                        public void onSuccess(@NonNull Address addresses) {
                            Intent intent = naviApp.createNavigationIntent(addresses.getLatitude(), addresses.getLongitude(), searchText[0], mContext);
                            view.startNavigation(intent);
                        }

                        @Override
                        public void onError(@NonNull GetAddressFromLocationName.Error error) {
                            Intent intent = naviApp.createMainIntent(mContext);
                            view.startNavigation(intent);
                        }
                    });
                } else {
                    Intent intent = naviApp.createMainIntent(mContext);
                    view.startNavigation(intent);
                }
            }
        });
    }

    private void searchPhone(@Nullable String[] searchText) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (searchText == null) {
                return;
            }

            Bundle args = SearchContentParams.toBundle(VoiceCommand.PHONE, searchText);
            ThemeType type = mPreference.getThemeType();
            if (type.isVideo()) {
                Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + type.getResourceId());
                view.setCaptureImage(true, uri);
            } else {
                view.changeBackgroundBlur(true);
            }
            view.showSearchContainer(args);
        });
    }

    /**
     * 音声認識終了イベント.
     * <p>
     * 車載機側の都合で音声認識を終了する必要がある場合に
     * 発行されるイベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFinishVoiceRecognitionEvent(FinishVoiceRecognitionEvent ev) {
        if (App.getApp(mContext).isForeground()) {
            Optional.ofNullable(getView()).ifPresent(MainView::finishSpeechRecognizer);
        }
    }

    public void initSpeechRecognizer() {
        mIsSpeechRecognizeEnable = true;
    }

    public void startSpeechRecognizer() {
        Timber.d("startSpeechRecognizer");
        //タイミングによってパーキングセンサー中に音声認識するのを防ぐ
        StatusHolder holder = mStatusCase.execute();
        boolean isDisplayParkingSensor = holder.getCarDeviceStatus().isDisplayParkingSensor;
        if (isDisplayParkingSensor) {
            stopSpeechRecognizer();
            return;
        }

        if (mSpeechRecognizer == null) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
            mSpeechRecognizer.setRecognitionListener(this);
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                AppUtil.getCurrentLocale(mContext).toString());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "音声を入力してください");
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.getPackageName());
        Optional.ofNullable(getView()).ifPresent(view -> {
            //ダイアログ表示動作と重なり音声認識開始音が鳴らないことがあるため、開始時間を遅らせる
            int delayTime;
            if (!view.isShowSpeechRecognizerDialog()) {
                //delayTime = holder.getAppStatus().speechRecognizerDelayTime;
                delayTime = 1000;//1000ms遅延
            } else {
                delayTime = 0;
            }
            view.setSpeechRecognizerState(SpeechRecognizerDialogFragment.StateType.WAITING);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mSpeechRecognizer!=null) {
                        mSpeechRecognizer.startListening(intent);
                        mStopSpeechRecognizerTask.start();
                    }
                }
            }, delayTime);
        });
    }

    public void stopSpeechRecognizer() {
        Timber.d("stopSpeechRecognizer");
        mStopSpeechRecognizerTask.stop();
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            mDeliberatelyCalledStop = true;
            mSpeechRecognizer = null;
        }

        finishRecognizer(false);
    }

    public void closeSpeechRecognizer(boolean isRinging) {
        Timber.d("closeSpeechRecognizer");
        mResentVoiceCommand = null;
        mRecognizeResults = null;

        mStopSpeechRecognizerTask.stop();
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            mDeliberatelyCalledStop = true;
            mSpeechRecognizer = null;
        }

        finishRecognizer(isRinging);
    }

    public void stopListening() {
        Timber.d("stopListening");
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.stopListening();
            mDeliberatelyCalledStop = true;
        }
    }

    // 音声認識準備完了
    @Override
    public void onReadyForSpeech(Bundle params) {
        Timber.v( "音声認識準備完了");
		Optional.ofNullable(getView()).ifPresent(view -> view.setSpeechRecognizerState(SpeechRecognizerDialogFragment.StateType.LISTENING));
    }

    // 音声入力開始
    @Override
    public void onBeginningOfSpeech() {
        Timber.v( "入力開始");
    }

    // 入力音声のdBが変化した
    @Override
    public void onRmsChanged(float rmsdB) {
        //Timber.v("recieve : " + rmsdB + "dB");

        Optional.ofNullable(getView()).ifPresent(view -> view.changeRsm(rmsdB));
    }

    // 録音データのフィードバック用
    @Override
    public void onBufferReceived(byte[] buffer) {
        Timber.v("onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Timber.v(  "入力終了");
    }

    @Override
    public void onError(int error) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (App.getApp(mContext).isForeground()) {
                mStopSpeechRecognizerTask.stop();
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        // 音声データ保存失敗
                        Timber.e("Audio recording error");
                        view.finishSpeechRecognizer();
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        // Android端末内のエラー(その他)
                        if (mDeliberatelyCalledStop) {
                            mDeliberatelyCalledStop = false;
                        } else {
                            Timber.e("Other client side errors");
                            view.finishSpeechRecognizer();
                        }
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        // 権限無し
                        Timber.e("Insufficient permissions");
                        view.finishSpeechRecognizer();
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        // ネットワークエラー(その他)
                        Timber.e("Network related errors");
                        view.finishSpeechRecognizer();
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        // ネットワークタイムアウトエラー
                        Timber.e("Network operation timed out");
                        view.finishSpeechRecognizer();
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        // 音声認識結果無し
                        Timber.e("No recognition result matched");
                        restartRecognizer(null);
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        // RecognitionServiceへ要求出せず
                        Timber.e("RecognitionService busy");
                        view.finishSpeechRecognizer();
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        // Server側からエラー通知
                        Timber.e("Server sends error status");
                        view.finishSpeechRecognizer();
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        // 音声入力無し
                        Timber.e("No speech input");
                        restartRecognizer(null);
                        break;
                    default:
                        stopSpeechRecognizer();
                }
            } else {
                stopSpeechRecognizer();
            }
        });
    }

    @Override
    public void onResults(Bundle results) {
        if (App.getApp(mContext).isForeground()) {
            restartRecognizer(results);
        } else {
            stopSpeechRecognizer();
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Timber.v("onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Timber.v("onEvent");
    }

    class PhoneBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
                registerPhoneStateListener();
            }
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String callNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:   //着信
                    if (mFinishBluetoothHeadset != null) {
                        closeSpeechRecognizer(true);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        mContext.registerReceiver(mPhoneBroadcastReceiver, filter);
    }

    private void initBroadcastReceiver() {
        if (isRegisterBroadcastReceiver) {
            mContext.unregisterReceiver(mPhoneBroadcastReceiver);
            isRegisterBroadcastReceiver = false;
        }
    }

    private void registerPhoneStateListener() {
        if (!isRegisterPhoneStateListener) {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                isRegisterPhoneStateListener = true;
            }
        }
    }

    private void initPhoneStateListener() {
        if (isRegisterPhoneStateListener) {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
                isRegisterPhoneStateListener = false;
            }
        }
    }

    class FinishSpeechRecognizerTask implements Runnable {
        private static final int FINISH_TIMER_COUNT = 3000;
        boolean isSpeechRecognizeFinished;

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            if (isSpeechRecognizeFinished) {
                stopSpeechRecognizer();
            }
        }

        public void start() {
            if (mSpeechRecognizer != null) {
                mHandler.postDelayed(this, FINISH_TIMER_COUNT);
                isSpeechRecognizeFinished = true;
            }
        }

        public void stop() {
            mHandler.removeCallbacks(this);
            isSpeechRecognizeFinished = false;
        }
    }

    class StopSpeechRecognizerTask implements Runnable {
        private static final int STOP_TIMER_COUNT = 5000;
        private boolean isRestart;

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            stopListening();
            Timber.d("StopSpeechRecognizerTask run()");

            if (isRestart) {
                start();
            } else {
                stop();
            }
        }

        public void start() {
            if (mSpeechRecognizer != null) {
                mHandler.postDelayed(this, STOP_TIMER_COUNT);
                isRestart = true;
            }
        }

        public void stop() {
            mHandler.removeCallbacks(this);
            isRestart = false;
        }
    }

    // MARK - 通知読み上げ

    @Override
    public void onInitializeSuccess() {
        mIsInitializedReadText = true;
    }

    @Override
    public void onInitializeError(@NonNull TextToSpeechController.Error error) {
        mIsInitializedReadText = false;
    }

    @Override
    public void onSpeakStart() {

    }

    @Override
    public void onSpeakDone() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if ((mResentVoiceCommand != null) || (mIsRecognizerRestarted)) {
                view.startRecognizer();
                mIsRecognizerRestarted = false;
            } else {
                view.finishSpeechRecognizer();
            }
        });
    }

    @Override
    public void onSpeakError(@NonNull TextToSpeechController.Error error) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if ((mResentVoiceCommand != null) || (mIsRecognizerRestarted)) {
                view.startRecognizer();
                mIsRecognizerRestarted = false;
            } else {
                if (mFinishBluetoothHeadset != null) {
                    view.finishSpeechRecognizer();
                }
            }
        });
    }

    public void finishReadingMessage(){
        mPrepareReadCase.finish();
    }



    // MARK - Alexa

    public void startAlexa(){
        StatusHolder holder = mStatusCase.execute();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (holder.getAppStatus().alexaAuthenticated) {
                SettingsUpdatedUtil.setLocale(mContext.getString(mPreference.getAlexaLanguage().locale));
                view.startAlexaConnection();
            }
        });
    }
    public void onLogIn(){
        StatusHolder holder = mStatusCase.execute();
        holder.getAppStatus().alexaAuthenticated = true;
        mEventBus.post(new AlexaLoginSuccessEvent());
        int vCode = mPreference.getAlexaCapabilitiesVersionCode();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (vCode < MainPresenter.ALEXA_CAPABILITIES_NEW_VERSION) {
                mPreference.setAlexaCapabilitiesSend(false);
                view.setAlexaCapabilities();
                mPreference.setAlexaCapabilitiesVersionCode(MainPresenter.ALEXA_CAPABILITIES_NEW_VERSION);
            }else{
                if (!mPreference.isAlexaCapabilitiesSend()) {
                    view.setAlexaCapabilities();
                }
            }
        });
    }

    public void onLogOut(){
        StatusHolder holder = mStatusCase.execute();
        holder.getAppStatus().alexaAuthenticated = false;
        //ログアウト時にCapabilitiesSend状態クリア
        mPreference.setAlexaCapabilitiesSend(false);
    }

    public void onCapabilitiesSendSuccess(){
        mPreference.setAlexaCapabilitiesSend(true);
    }

    public void setAlexaAvailable(SimCountryIso simCountryIso){
        AppStatus appStatus = mStatusCase.execute().getAppStatus();
        
        // デバッグ版ではデバッグ設定のSIM判定がONなら、その結果を採用する
        // SIM判定がOFFの場合は常にAlexa利用可能とする(SIM判定 DefaultはON)
        List countryList = Arrays.asList(SimCountryIso.US, SimCountryIso.JP, SimCountryIso.IN, SimCountryIso.GB);
        boolean available = countryList.contains(simCountryIso);
        if(mPreference.isAlexaRequiredSimCheck()) {
            appStatus.isAlexaAvailableCountry = available;
            if(!available) {
            	// 現在の設定がAlexaでAlexaが利用不可能な場合は音声認識のタイプをPSSにする
                if(mPreference.getVoiceRecognitionType()==VoiceRecognizeType.ALEXA) {
                    mPreference.setVoiceRecognitionType(VoiceRecognizeType.PIONEER_SMART_SYNC);
                }
            }
        } else {
            appStatus.isAlexaAvailableCountry = true;
        }
        // Alexa利用可能ダイアログを出す必要がある場合は連携抑制開始
        if(isAlexaAvailableConfirmNeeded() && !appStatus.deviceConnectionSuppress) {
            startDeviceConnectionSuppress();
        }
    }

    /**
     * Alexa機能利用可能ダイアログを出すべきかどうかの判定
     * @return
     * {@code true}:Alexa機能が利用可能かつAlexa機能利用可能ダイアログを未表示
     * {@code false}:それ以外(Alexa機能が利用不可能またはAlexa機能利用可能ダイアログを表示済み)
     */
    public boolean isAlexaAvailableConfirmNeeded() {
        return mStatusCase.execute().getAppStatus().isAlexaAvailableCountry && !mPreference.isAlexaAvailableConfirmShowed();
    }

    public void onSetNaviDestination(Bundle args){
        Optional.ofNullable(getView()).ifPresent(view -> {
            Double latitude = args.getDouble("latitude");
            Double longitude = args.getDouble("longitude");
            String name = args.getString("destName");
            NaviApp naviApp = null;
            try {
                if (mPreference.getLastConnectedCarDeviceClassId() == CarDeviceClassId.MARIN) {
                    if (mPreference.getNavigationMarinApp() == null) {
                        //マリンアプリ未設定
                        view.showToast(mContext.getString(R.string.err_007));
                    } else {
                        naviApp = NaviApp.fromPackageName(mPreference.getNavigationMarinApp().packageName);
                    }
                } else {
                    naviApp = NaviApp.fromPackageName(mPreference.getNavigationApp().packageName);
                }
                if(naviApp!=null){
                    Intent intent = naviApp.createNavigationIntent(latitude, longitude, name, mContext);
                    view.startNavigation(intent);
                }
            } catch (IllegalArgumentException e) {
                //Marineモデル連携時、マリン系アプリが設定されている場合にはERR_007を表示する
                //連携車載機がMarineモデルか否かにかかわらず、アプリ未設定時はERR_007を表示する
                view.showToast(mContext.getString(R.string.err_007));
            }
        });
    }

    /// MARK - ユーザー画像背景設定
    public void previewBackground(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.navigate(ScreenId.BACKGROUND_PREVIEW, null);
        });
    }

    private void showAppConnectMethodDialog(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (!mPreference.isAppConnectMethodNoDisplayAgain()) {
                if (!view.isShowAppConnectMethodDialog()) {
                    view.showAppConnectMethodDialog(Bundle.EMPTY);
                }
            }
        });
    }

}
