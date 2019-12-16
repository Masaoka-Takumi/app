package jp.pioneer.carsync.presentation.presenter;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.event.AppStateChangeEvent;
import jp.pioneer.carsync.application.util.AppUtil;
import jp.pioneer.carsync.domain.component.TextToSpeechController;
import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.event.AdasWarningUpdateEvent;
import jp.pioneer.carsync.domain.event.AlexaNotificationChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicAudioModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicPlayPositionChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicPlaybackModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicTrackChangeEvent;
import jp.pioneer.carsync.domain.event.AppStartCommandEvent;
import jp.pioneer.carsync.domain.event.CarDeviceErrorEvent;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.ImpactEvent;
import jp.pioneer.carsync.domain.event.InitialSettingChangeEvent;
import jp.pioneer.carsync.domain.event.ListTypeChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.event.ParkingSensorDisplayStatusChangeEvent;
import jp.pioneer.carsync.domain.event.RadioFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.RadioFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationRemovedEvent;
import jp.pioneer.carsync.domain.event.ShortcutKeyEvent;
import jp.pioneer.carsync.domain.event.SmartPhoneControlCommandEvent;
import jp.pioneer.carsync.domain.event.SxmInfoChangeEvent;
import jp.pioneer.carsync.domain.event.TransportStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.CheckAvailableTextToSpeech;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlImpactDetector;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.DirectCall;
import jp.pioneer.carsync.domain.interactor.GetReadNotificationList;
import jp.pioneer.carsync.domain.interactor.GetRunningStatus;
import jp.pioneer.carsync.domain.interactor.InitializeSlaSetting;
import jp.pioneer.carsync.domain.interactor.IsGrantReadNotification;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.interactor.PreferRadioFunction;
import jp.pioneer.carsync.domain.interactor.PrepareReadNotification;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.interactor.ReadNotification;
import jp.pioneer.carsync.domain.model.AdasWarningStatus;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceErrorType;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.Notification;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.ParkingSensorSetting;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ProtocolSpec;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.SystemSetting;
import jp.pioneer.carsync.domain.model.SystemSettingSpec;
import jp.pioneer.carsync.domain.model.TransportStatus;
import jp.pioneer.carsync.domain.model.TunerFunctionSettingStatus;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.infrastructure.component.NotificationListenerServiceImpl;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionErrorEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStartedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.presentation.event.AlexaRenderPlayerInfoUpdateEvent;
import jp.pioneer.carsync.presentation.event.AlexaVoiceRecognizeEvent;
import jp.pioneer.carsync.presentation.event.MessageReadFinishedEvent;
import jp.pioneer.carsync.presentation.event.SessionCompletedEvent;
import jp.pioneer.carsync.presentation.event.SourceChangeReasonEvent;
import jp.pioneer.carsync.presentation.event.StartGetRunningStatusEvent;
import jp.pioneer.carsync.presentation.view.ResourcefulView;
import jp.pioneer.carsync.presentation.view.service.ForegroundReason;
import jp.pioneer.carsync.presentation.view.service.InitializeState;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.manager.AlexaAudioManager;
import jp.pioneer.mbg.alexa.manager.AlexaQueueManager;
import timber.log.Timber;

import static jp.pioneer.carsync.application.content.AppSharedPreference.KEY_IMPACT_DETECTION_DEBUG_MODE_ENABLED;
import static jp.pioneer.carsync.application.content.AppSharedPreference.KEY_IMPACT_DETECTION_ENABLED;
import static jp.pioneer.carsync.application.content.AppSharedPreference.KEY_IMPACT_NOTIFICATION_CONTACT_NUMBER;

/**
 * CarSyncサービスのpresenter
 */
@PresenterLifeCycle
public class ResourcefulPresenter extends Presenter<ResourcefulView>
        implements TextToSpeechController.Callback, AppSharedPreference.OnAppSharedPreferenceChangeListener, Loader.OnLoadCompleteListener<Cursor>  {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject ControlImpactDetector mControlImpactDetector;
    @Inject IsGrantReadNotification mIsGrantCase;
    @Inject GetReadNotificationList mNotificationCase;
    @Inject CheckAvailableTextToSpeech mCheckTtsCase;
    @Inject ReadNotification mReadCase;
    @Inject ControlAppMusicSource mControlCase;
    @Inject PrepareReadNotification mPrepareReadCase;
    @Inject GetRunningStatus mGetRunningStatusCase;
    @Inject QueryContact mContactCase;
    @Inject DirectCall mDirectCallCase;
    @Inject @ForInfrastructure Handler mHandler;
    @Inject InitializeSlaSetting mInitializeSlaSetting;
    @Inject ControlSource mControlSource;
    @Inject PreferAdas mPreferAdas;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject QueryTunerItem mTunerCase;
    @Inject PreferRadioFunction mPreferRadioFunction;
    @Inject AnalyticsEventManager mAnalytics;
    private CursorLoader mCursorLoader;
    private static final int LOADER_ID_NUMBER = 1;
    private ForegroundReason mReason;
    private List<Notification> mNotifications;
    private int mReadingIndex = -1;
    private Notification mPendingNotification;
    private Notification mReadingNotification;
    private InitializeState mInitState = InitializeState.YET;
    private final Object mLockObj = new Object();
    private boolean mIsPrepareRead = false;
    private boolean mIsTTSMode = false;
    private boolean  mIsReadState = false;
    private Boolean mCurrentSubscriptionUpdatingShowing = null;
    private ListType mCurrentListType;
    private ReconnectNotificationListenerServiceTask mReconnectServiceTask = new ReconnectNotificationListenerServiceTask();
    /** Alexaマネージャ. */
    AmazonAlexaManager mAmazonAlexaManager;
    private AlexaCallback mAlexaCallback = new AlexaCallback();
    private MediaSourceType mLastSourceType = null; // FlurryAnalytics Sourceタイプ記憶用

    /**
     * コンストラクタ
     */
    @Inject
    public ResourcefulPresenter() {
    }

    @Override
    void onTakeView() {
        mReadCase.initialize(this);
        mPreference.registerOnAppSharedPreferenceChangeListener(this);
        //API26のバックグラウンド実行制限により、startForegroundServiceでServiceを起動直後にstartForegroundを呼び出す
        Optional.ofNullable(getView()).ifPresent(view -> view.startForeground(""));
        // フォアで起動するか確認
        startForegroundIfNeeded();

        mReconnectServiceTask.start();
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    @Override
    void onDropView() {
        mReadCase.terminate();
    }

    @Override
    void onDestroy() {
        // Stop the cursor loader
        Timber.d("onDestroy");
        if (mCursorLoader != null) {
            mCursorLoader.unregisterListener(this);
            mCursorLoader.cancelLoad();
            mCursorLoader.stopLoading();
        }
        mReason=null;
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (loader.getId() == LOADER_ID_NUMBER) {
                if(data.getCount() > 0) {
                    data.moveToFirst();
                    String number = ContactsContract.Phone.getNumber(data);
                    //止めないと繰り返しコールバックされる
                    if (mCursorLoader != null) {
                        mCursorLoader.unregisterListener(this);
                        mCursorLoader.cancelLoad();
                        mCursorLoader.stopLoading();
                    }
                    mDirectCallCase.execute(number);
                    mAnalytics.sendTelephoneCallEvent(Analytics.AnalyticsTelephoneCall.directCall);
                }
            }
        });
    }

    public void startDeviceConnectionSuppress(){
        //連携抑制のフラグON
        mStatusHolder.getAppStatus().deviceConnectionSuppress = true;
        if(mStatusHolder.getSessionStatus() != SessionStatus.STOPPED) {
            mCarDeviceConnection.sessionStop();
        }
    }

/// MARK - event bus subscribe method

    /**
     * 車載機接続イベントハンドラ.
     *
     * @param ev 車載機接続イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onCrpSessionStartedEvent(CrpSessionStartedEvent ev) {
        CarDeviceSpec spec = mStatusHolder.getCarDeviceSpec();
        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        ProtocolSpec protocolSpec = mStatusHolder.getProtocolSpec();
        SystemSettingSpec systemSpec = spec.systemSettingSpec;
        SystemSetting systemSetting = mStatusHolder.getSystemSetting();
        mPreference.setLastConnectedCarDeviceModel(spec.modelName);
        mPreference.setLastConnectedCarDeviceDestination(spec.carDeviceDestinationInfo.code);
        //連携車載機のClassIdを保存する
        mPreference.setLastConnectedCarDeviceClassId(protocolSpec.getCarDeviceClassId());
        //mPreference.setLastConnectedCarDeviceClassId(CarDeviceClassId.MARIN);
        //連携車載機のプロトコルバージョンを保存する
        mPreference.setLastConnectedCarDeviceProtocolVersion(protocolSpec.getConnectingProtocolVersion());
        if(mPreference.getLastConnectedCarDeviceClassId()!=CarDeviceClassId.MARIN) {
            //ADAS対応車載機か否かを保存する
            boolean isAdasAvailable = mStatusHolder.getCarDeviceSpec().adasAlarmSupported;
            mPreference.setLastConnectedCarDeviceAdasAvailable(isAdasAvailable);
            if (!isAdasAvailable && mPreferAdas.getAdasEnabled()) {
                mPreferAdas.setAdasEnabled(false);
            }
        }

        //AndroidVR対応車載機か否かを保存する
        mPreference.setLastConnectedCarDeviceAndroidVr(spec.androidVrSupported);
        //AndroidVR設定時に非対応車載機と連携したら、Defaultに設定値を変更
        if(!spec.androidVrSupported) {
            if(mPreference.getVoiceRecognitionType() == VoiceRecognizeType.ANDROID_VR){
                mPreference.setVoiceRecognitionType(VoiceRecognizeType.PIONEER_SMART_SYNC);
            }
        }

        //距離単位設定対応車載機と連携した場合、車載器の単位設定を保存する
        if(systemSpec.distanceUnitSettingSupported){
            mPreference.setDistanceUnit(systemSetting.distanceUnit);
        }
        //連携車載器情報保存後に走行状態取得開始
        mGetRunningStatusCase.start();

        startImpactDetectionIfNeeded();
        //専用機連携時AM Stepの設定変更された場合はAMのP.CH登録データを全て削除する。(AM1/AM2は削除しない)
        checkAmStepSettingChange();

        //TODO:Alexaを塞ぐ
        mAmazonAlexaManager = AmazonAlexaManager.getInstance();
        if (mAmazonAlexaManager != null) {
            Timber.d("addAlexaCallback");
            mAmazonAlexaManager.addAlexaCallback(mAlexaCallback);
        }

        // YouTubeLinkに関するショートカット表示更新のためにイベントを投げる
        mEventBus.post(new SessionCompletedEvent());
    }
    /**
     * ラジオ情報変更通知イベントハンドラ
     *
     * @param event ラジオ情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioInfoChangeEvent(RadioInfoChangeEvent event) {
        RadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo;
        if(info.tunerStatus== TunerStatus.BSM){
            //専用機のRadioソースでBSM状態になったら、受信中バンドのP.CH登録データを全て削除する。
            deleteUserPresetCurrentBand(info.band);
        }
    }

    private void deleteUserPresetCurrentBand(RadioBandType band){
        if(isSphCarDevice()) {
            mTunerCase.unregisterFavorite(TunerContract.FavoriteContract.DeleteParamsBuilder.createParamsPreset(band));
        }
    }

    /**
     * ラジオ設定ステータス変更通知イベントハンドラ
     *
     * @param event ラジオ設定ステータス変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioFunctionSettingStatusChangeEvent(RadioFunctionSettingStatusChangeEvent event) {
        setPchManualSetting();
    }

    /**
     * Radio Function設定変更イベントハンドラ.
     *
     * @param event Radio Function設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioFunctionSettingChangeEvent(RadioFunctionSettingChangeEvent event) {
        setPchManualSetting();
    }

    /**
     * 車載機ステータス変更イベントハンドラ
     *
     * @param event 車載機ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        setPchManualSetting();
        checkAmStepSettingChange();
    }

    private void setPchManualSetting(){
        //専用機はRadioFunction設定のシーク設定が有効状態＆設定値がP.CHの場合
        //  MANUALに設定値を変更する
        if(isSphCarDevice()){
            boolean isRadioSettingEnabled = mStatusHolder.getCarDeviceStatus().tunerFunctionSettingEnabled &&
                    mStatusHolder.getCarDeviceSpec().tunerFunctionSettingSupported;
            TunerFunctionSettingStatus tunerFunctionSettingStatus = mStatusHolder.getTunerFunctionSettingStatus();
            if(isRadioSettingEnabled&&tunerFunctionSettingStatus.pchManualEnabled&&mStatusHolder.getTunerFunctionSetting().pchManualSetting == PCHManualSetting.PCH) {
                mPreferRadioFunction.togglePchManual();
            }
        }
    }

    /**
     * 初期設定変更イベントハンドラ.
     *
     * @param event 初期設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInitialSettingChangeEvent(InitialSettingChangeEvent event) {
        checkAmStepSettingChange();
    }

    private void checkAmStepSettingChange(){
        //専用機連携時AM Stepの設定変更された場合はAMのP.CH登録データを全て削除する。(AM1/AM2は削除しない)
        if(isSphCarDevice()){
            CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
            if(status.seekStep!=mPreference.getLastConnectedCarDeviceAmStep()){
                mTunerCase.unregisterFavorite(TunerContract.FavoriteContract.DeleteParamsBuilder.createParamsPresetAm());
            }
        }
    }

    private boolean isSphCarDevice(){
        return mStatusHolder.getProtocolSpec().isSphCarDevice();
    }

    /**
     * 車載機切断イベントハンドラ.
     *
     * @param ev 車載機切断イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onCrpSessionStoppedEvent(CrpSessionStoppedEvent ev) {
		//MainActivityが終了している時に連携切断した場合、連携抑制のフラグを戻す
        if(mPreference.isAdasBillingRecord()&&!App.getApp(mContext).isCreated()){
            mStatusHolder.getAppStatus().deviceConnectionSuppress=true;
            mStatusHolder.getAppStatus().adasBillingCheck = false;
        }
        mGetRunningStatusCase.stop();
        mControlImpactDetector.stopDetection();
        Timber.d("finishAlexaConnection");
        mAmazonAlexaManager = AmazonAlexaManager.getInstance();
        // Alexa 切断処理
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.onActivityPause();
        }
    }

    /**
     * 車載機エラーイベントハンドラ.
     *
     * @param ev 車載機エラーイベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onCrpSessionErrorEvent(CrpSessionErrorEvent ev) {
        startForegroundIfNeeded();
    }

    /**
     * AppMusicPlaybackModeChangeEventハンドラ.
     * <p>
     * AppMusicの再生状態がForegroundServiceに関連する。
     *
     * @param ev AppMusicPlaybackModeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppMusicPlaybackModeChangeEvent(AppMusicPlaybackModeChangeEvent ev) {
        startForegroundIfNeeded();
    }

    /**
     * ShortcutKeyEventハンドラ
     * <p>
     * 車載器のハードキー押下時に動作する。
     *
     * @param ev ShortcutKeyEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShortcutKeyEvent(ShortcutKeyEvent ev) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            AppStatus appStatus = mStatusHolder.getAppStatus();
            if(appStatus.isTransitionedHomeScreen && AppUtil.isScreenOn(mContext)) {
                switch (ev.shortcutKey) {
                    case APP:
                        if (mPreference.isAgreedEulaPrivacyPolicy()) {
                            view.dispatchAppKey();
                        }
                        break;
                    case NAVI:
                        if (mPreference.getLastConnectedCarDeviceClassId() == CarDeviceClassId.MARIN) {
                            if (mPreference.getNavigationMarinApp() == null) {
                                view.showError(mContext.getString(R.string.err_035));
                            } else {
                                view.dispatchNaviMarinKey(mPreference.getNavigationMarinApp().packageName);
                            }
                        } else {
                            view.dispatchNaviKey(mPreference.getNavigationApp().packageName);
                        }
                        break;
                    case MESSAGE:
                        if (mPreference.isReadNotificationEnabled()) {
                            view.dispatchMessageKey();
                        } else {
                            view.showError(mContext.getString(R.string.err_018));
                        }
                        break;
                    case PHONE:
                        if (mPreference.isPhoneBookAccessible()) {
                            view.dispatchPhoneKey();
                        } else {
                            view.showError(mContext.getString(R.string.err_018));
                        }
                        break;
                    case SOURCE:
                        //何もしない
                        break;
                    case VOICE:
                        mAmazonAlexaManager = AmazonAlexaManager.getInstance();
                        if (mAmazonAlexaManager != null) {
                            Timber.d("addAlexaCallback");
                            mAmazonAlexaManager.addAlexaCallback(mAlexaCallback);
                        }
                        if(appStatus.isShowAlexaDialog){
                            mEventBus.post(new AlexaVoiceRecognizeEvent());
                        } else if (mStatusHolder.getAppStatus().isAlexaAvailableCountry||mPreference.getLastConnectedCarDeviceAndroidVr() || mPreference.isVoiceRecognitionEnabled()) {
                            // Alexa機能が利用可能なら音声認識は利用可能
                            //「連携中Siri/Google VR動作可能機能に対応車載機」と連携中の場合は利用可能
                            // 上記利用不可能なら音声認識機能の有効/無効を判定する
                            view.dispatchVoiceKey();
                        } else {
                            view.showError(mContext.getString(R.string.err_018));
                        }
                        break;
                    default:
                        Timber.e("onShortcutKeyEvent() can't happen. shortcutKey = " + ev.shortcutKey);
                }
            }
        });
    }

    /**
     * SmartPhone操作コマンドイベント.
     * <p>
     * 車載機からSmartPhoneを操作する場合に発生する。
     *
     * @param ev SmartPhoneControlCommandEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSmartPhoneControlCommandEvent(SmartPhoneControlCommandEvent ev){
        Optional.ofNullable(getView()).ifPresent(view -> {
            AppStatus appStatus = mStatusHolder.getAppStatus();
            if(mPreference.isDisplaySmartPhoneControlCommand()) {
                String commandText = mContext.getString(ev.command.label) + " ";
                if (ev.command.commandStatusCode == 0x01) {
                    commandText += mContext.getString(R.string.control_command_long);
                } else {
                    commandText += mContext.getString(R.string.control_command_short);
                }
                view.showShortToast(commandText);
            }
            if(appStatus.isTransitionedHomeScreen && AppUtil.isScreenOn(mContext)) {
                switch (ev.command) {
                    case AV:
                        view.dispatchAvKey();
                        break;
                    case DIRECT_CALL:
                        if (!mPreference.isPhoneBookAccessible()) {
                            view.showError(mContext.getString(R.string.err_018));
                        }else if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                        ||ContextCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                            view.dispatchPermissionRequest();
                        }else {
                            Long number = mPreference.getDirectCallContactNumberId();
                            if (number >= 0) {
                                mCursorLoader = mContactCase.execute(ContactsContract.QueryParamsBuilder.createPhone(number));
                                mCursorLoader.registerListener(LOADER_ID_NUMBER, this);
                                mCursorLoader.startLoading();
                            }
                        }
                        break;
                    default:
                        //
                }
            }
        });
    }

    /**
     * ImpactEventハンドラ
     * <p>
     * 端末が衝突を検知した場合に動作する。
     *
     * @param ev ImpactEvent
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onImpactEvent(ImpactEvent ev) {
        if (!mPreference.isImpactDetectionEnabled() || TextUtils.isEmpty(mPreference.getImpactNotificationContactNumber()) || mPreference.getLastConnectedCarDeviceClassId()==CarDeviceClassId.MARIN) {
            return;
        }
        Optional.ofNullable(getView()).ifPresent(ResourcefulView::showAccidentDetect);
    }

    /**
     * リバース線状態変更イベントハンドラ
     *
     * @param ev リバース線状態変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReverseStatusChangeEvent(ParkingSensorDisplayStatusChangeEvent ev){
        ParkingSensorSetting parkingSensorSetting = mStatusHolder.getParkingSensorSetting();
        boolean isDisplayParkingSensor = mStatusHolder.getCarDeviceStatus().isDisplayParkingSensor;
        if (isDisplayParkingSensor) {
            Optional.ofNullable(getView()).ifPresent(ResourcefulView::showParkingSensor);
        }
    }

    /**
     * ADAS警報状態更新イベントハンドラ
     *
     * @param ev ADAS警報状態更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdasWarningUpdateEvent(AdasWarningUpdateEvent ev){
       AppStatus status = mStatusHolder.getAppStatus();
        AdasWarningStatus adasWarningStatus = status.getAdasWarningStatus();
        if(adasWarningStatus != AdasWarningStatus.NONE){
            Optional.ofNullable(getView()).ifPresent(ResourcefulView::showAdasWarning);
        }
    }

    /**
     * SXM情報変更通知イベントハンドラ
     *
     * @param event SXM情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSxmInfoChangeEvent(SxmInfoChangeEvent event) {
        SxmMediaInfo currSxm = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;
        if (mCurrentSubscriptionUpdatingShowing == null){
            if(currSxm.subscriptionUpdatingShowing){
                Optional.ofNullable(getView()).ifPresent(ResourcefulView::showSubscriptionUpdating);
                mCurrentSubscriptionUpdatingShowing = currSxm.subscriptionUpdatingShowing;
            }
        }else if(mCurrentSubscriptionUpdatingShowing != currSxm.subscriptionUpdatingShowing) {
            Optional.ofNullable(getView()).ifPresent(ResourcefulView::showSubscriptionUpdating);
            mCurrentSubscriptionUpdatingShowing = currSxm.subscriptionUpdatingShowing;
        }
    }

    /**
     * 車載機エラーイベントハンドラ
     *
     * @param event 車載機エラーイベント.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceErrorEvent(CarDeviceErrorEvent event) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            String errorTag = "";
            String errorTitle = "";
            String errorText = "";
            switch (event.errorType) {
                case AMP_ERROR:
                    errorTag = CarDeviceErrorType.AMP_ERROR.toString();
                    errorText = mContext.getResources().getString(R.string.err_003);
                    break;
                case CHECK_USB:
                    errorTag = CarDeviceErrorType.CHECK_USB.toString();
                    errorText = mContext.getResources().getString(R.string.err_004);
                    break;
                case CHECK_TUNER:
                    errorTag = CarDeviceErrorType.CHECK_TUNER.toString();
                    errorText = mContext.getResources().getString(R.string.err_005);
                    break;
                case CHECK_ANTENNA:
                    errorTag = CarDeviceErrorType.CHECK_ANTENNA.toString();
                    errorText = mContext.getResources().getString(R.string.err_006);
                    break;
                default:
                    break;

            }
            view.showCarDeviceError(errorTag,errorTitle,errorText);
        });
    }

    /**
     * AppMusicTrackChangeEventハンドラ
     * <p>
     * 再生中に楽曲切り替わり通知を受信した場合に動作する。
     *
     * @param event AppMusicTrackChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppMusicTrackChangeEvent(AppMusicTrackChangeEvent event) {
        if (!App.getApp(mContext).isForeground()) {
            CarDeviceMediaInfoHolder mediaHolder = mStatusHolder.getCarDeviceMediaInfoHolder();
            if (mStatusHolder.getSmartPhoneStatus().playbackMode == PlaybackMode.PLAY) {
                Optional.ofNullable(getView()).ifPresent(view -> view.showSongNotification(mediaHolder.androidMusicMediaInfo));
            }
        }
    }

    /**
     * ReadNotificationRemovedEventハンドラ
     * <p>
     * 通知を削除した場合に動作する。
     *
     * @param event ReadNotificationRemovedEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadNotificationRemovedEvent(ReadNotificationRemovedEvent event) {
    }

    /**
     * 通信路状態変更イベントハンドラ
     *
     * @param event 通信路状態変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransportStatusChangeEvent(TransportStatusChangeEvent event) {
        startForegroundIfNeeded();
    }

    /**
     * アプリケーション起動イベントハンドラ.
     * <p>
     * 利用規約同意前は起動しない。
     *
     * @param event アプリケーション起動イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppStartCommandEvent(AppStartCommandEvent event) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (mPreference.isAgreedEulaPrivacyPolicy()) {
                view.startApplication(event.packageName);
            }
        });
    }

    /**
     * ソース種別変更イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeAction(MediaSourceTypeChangeEvent event) {
        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        if(mIsPrepareRead){
            if(status.sourceType == MediaSourceType.TTS &&
                    status.sourceStatus == MediaSourceStatus.CHANGE_COMPLETED){
                mIsPrepareRead = false;
                mIsTTSMode = true;
                Optional.ofNullable(getView()).ifPresent(ResourcefulView::readMessage);
                onExistNotificationAction();
            }
        }
        if(mIsTTSMode){
            if(status.sourceType != MediaSourceType.TTS &&
                    status.sourceStatus == MediaSourceStatus.CHANGING){
                mIsTTSMode = false;
                mIsReadState = false;
                mReadingNotification = null;
                mReadCase.stopReading();
            }
        }

        initializeSlaSetting();
    }

    /**
     * リスト種別変更イベントハンドラ
     *
     * @param event リスト種別変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onListTypeChangeEvent(ListTypeChangeEvent event) {
        ListType type = mStatusHolder.getCarDeviceStatus().listType;
        if(mCurrentListType == null || mCurrentListType == ListType.NOT_LIST) {
            if (type == ListType.PCH_LIST || type == ListType.LIST || type == ListType.ABC_SEARCH_LIST
                    ||type == ListType.SERVICE_LIST || type == ListType.PTY_NEWS_INFO_LIST || type == ListType.PTY_POPULER_LIST
                    || type == ListType.PTY_CLASSICS_LIST || type == ListType.PTY_OYHERS_LIST
                    || type == ListType.ENSEMBLE_CATEGORY || type == ListType.ENSEMBLE_LIST) {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    if (AppUtil.isScreenOn(mContext)) {
                        view.dispatchEnterList();
                    }
                });
            }
        }
        mCurrentListType = type;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStartGetRunningStatusEvent(StartGetRunningStatusEvent event) {
        mGetRunningStatusCase.start();
    }

/// MARK - public method

    /**
     * 通知読み上げ準備処理
     * (車載器のMessagingボタン押下時の処理)
     */
    public void onMessageKeyAction(){
        List<Notification> notifications = mNotificationCase.execute();
        if(notifications.size() != 0) {
            mCheckTtsCase.execute(result -> {
                switch (result) {
                    case AVAILABLE:
                    default:
                        if (mStatusHolder.getCarDeviceStatus().sourceType == MediaSourceType.TTS &&
                                mStatusHolder.getCarDeviceStatus().sourceStatus == MediaSourceStatus.CHANGE_COMPLETED) {
                            onExistNotificationAction();
                        } else {
                            mIsPrepareRead = true;
                            mPrepareReadCase.start();
                        }
                        break;
                    case LANG_NOT_SUPPORTED:
                    case MAY_NOT_DISABLED:
                        Optional.ofNullable(getView()).ifPresent(view -> {
                            try {
                                view.onShowAndroidSettings("com.android.settings.TTS_SETTINGS");
                                view.showError(mContext.getString(R.string.err_019));
                            } catch (Exception ex) {
                                view.onShowAndroidSettings(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                            }
                        });
                        break;
                    case LANG_MISSING_DATA:
                        Optional.ofNullable(getView()).ifPresent(view -> view.onShowAndroidSettings(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));
                        break;
                }
            });


        }
    }

    /**
     * 既存通知の読み上げ処理
     * (車載器のMessagingボタン押下時の処理)
     */
    public void onExistNotificationAction() {
        Timber.d("onExistNotificationAction in ReadNotificationPresenter");
        if (mPendingNotification != null) {
            return;
        }

        if (!mIsGrantCase.execute()) {
            return;
        }

        synchronized (this) {
            if (getInitializeState() == InitializeState.INITIALIZING) {
                // 生成中はボタン押下イベントをスルー
                return;
            } else if (getInitializeState() == InitializeState.ERROR) {
                setInitializeState(InitializeState.INITIALIZING);
                if (!mIsReadState && mReadingNotification == null){
                    mNotifications = mNotificationCase.execute();
                    mReadingIndex = -1;
                }
                // init成功時のために読み上げする通知を保持
                mPendingNotification = getNextNotification();
                mReadCase.initialize(this);
                return;
            }
        }

        if (!mIsReadState && mReadingNotification == null){
            mNotifications = mNotificationCase.execute();
            mReadingIndex = -1;
        }
        Notification notification = getNextNotification();
        if (notification == null) {
            mReadCase.stopReading();
            mPrepareReadCase.finish();
            mReadingNotification = null;
        } else {
            mPendingNotification = notification;
            mReadCase.startReading(notification);
        }
    }

/// MARK - private method

    private void initializeSlaSetting(){
        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();

        if(mStatusHolder.getSessionStatus() == SessionStatus.STARTED) {
            if (status.sourceType == MediaSourceType.APP_MUSIC &&
                    status.sourceStatus == MediaSourceStatus.CHANGE_COMPLETED) {
                if (!mPreference.isConfiguredSlaSetting()) {
                    mInitializeSlaSetting.execute();
                    mPreference.setConfiguredSlaSetting(true);
                }
            }
        }
    }

    /**
     * アプリケーション状態変更イベントハンドラ.
     *
     * @param ev アプリケーション状態イベント
     */
    @Subscribe
    public void onAppStateChangedEvent(AppStateChangeEvent ev) {
        Timber.i("onAppStateChangedEvent ev=" + ev.appState.name());
        if (ev.appState == AppStateChangeEvent.AppState.RESUMED) {
            startForegroundIfNeeded();
        } else if (ev.appState == AppStateChangeEvent.AppState.PAUSED) {
            startForegroundIfNeeded();
        }
    }

    private void startForegroundIfNeeded() {
        Timber.i("startForegroundIfNeeded");
        if(!mPreference.isAppServiceResident()&&mStatusHolder.getSessionStatus() != SessionStatus.STARTED&&!App.getApp(mContext).isForeground()){
            Optional.ofNullable(getView()).ifPresent(ResourcefulView::stopForeground);
            mReason= null;
            return;
        }

        if(mStatusHolder.getAppStatus().errorType == null) {
            ForegroundReason reason = null;
            if (mStatusHolder.getTransportStatus() != TransportStatus.UNUSED) {
                switch (mStatusHolder.getTransportStatus()) {
                    case BLUETOOTH_LISTENING:
                        reason = ForegroundReason.BLUETOOTH_LISTENING;
                        break;
                    case BLUETOOTH_CONNECTING:
                        reason = ForegroundReason.BLUETOOTH_CONNECTING;
                        break;
                    case USB_CONNECTING:
                        reason = ForegroundReason.USB_CONNECTING;
                        break;
                    default:
                        throw new AssertionError("can't happen");
                }
            }
            if (mReason!=null&&mReason == reason) {
                return;
            }

            if (reason != null) {
                String message = mContext.getResources().getString(reason.getMessageResId());
                Optional.ofNullable(getView()).ifPresent(view -> view.startForeground(message));
            } else {
				//API26のバックグラウンド実行制限により、バッググラウンドサービスが停止されるため、常時フォアグラウンドサービスとして起動
                if(Build.VERSION.SDK_INT>=26) {
                    Optional.ofNullable(getView()).ifPresent(view -> view.startForeground(mContext.getResources().getString(R.string.ntc_008)));
                }else {
                    Optional.ofNullable(getView()).ifPresent(ResourcefulView::stopForeground);
                }
            }
            mReason = reason;
        } else {
            String message = mContext.getResources().getString(mStatusHolder.getAppStatus().errorType.text);
            Optional.ofNullable(getView()).ifPresent(view -> view.startForeground(message));
        }
    }

    private void startImpactDetectionIfNeeded() {
        if (mPreference.isImpactDetectionEnabled() &&
                !TextUtils.isEmpty(mPreference.getImpactNotificationContactNumber()) &&
                mStatusHolder.getSessionStatus() == SessionStatus.STARTED) {
            try {
                mControlImpactDetector.startDetection();
            } catch (Exception e) {
                Timber.e(e.getMessage());
            }
        } else {
            mControlImpactDetector.stopDetection();
        }
    }

    @Nullable
    private Notification getNextNotification() {
        if (mReadingIndex < mNotifications.size() - 1) {
            return mNotifications.get(++mReadingIndex);
        }
        return null;
    }

    @VisibleForTesting
    void setInitializeState(InitializeState newState) {
        synchronized (mLockObj) {
            mInitState = newState;
        }
    }

    private InitializeState getInitializeState() {
        synchronized (mLockObj) {
            return mInitState;
        }
    }

/// MARK - AppSharedPreference.OnAppSharedPreferenceChangeListener

    @Override
    public void onAppSharedPreferenceChanged(@NonNull AppSharedPreference preferences, @NonNull String key) {
        if (TextUtils.equals(key, KEY_IMPACT_DETECTION_ENABLED)) {
            startImpactDetectionIfNeeded();
        } else if (TextUtils.equals(key, KEY_IMPACT_DETECTION_DEBUG_MODE_ENABLED)) {
            mControlImpactDetector.stopDetection();
            startImpactDetectionIfNeeded();
        } else if (TextUtils.equals(key, KEY_IMPACT_NOTIFICATION_CONTACT_NUMBER)) {
            startImpactDetectionIfNeeded();
        }
        startForegroundIfNeeded();
    }

/// MARK - TextToSpeechController.Callback

    @Override
    public void onInitializeSuccess() {
        setInitializeState(InitializeState.COMPLETE);
        if (mPendingNotification != null) {
            mReadCase.startReading(mPendingNotification);
        }
    }

    @Override
    public void onInitializeError(@NonNull TextToSpeechController.Error error) {
        if (getInitializeState() == InitializeState.YET) {
            // 初回initではエラー通知を行わない
            setInitializeState(InitializeState.ERROR);
        } else {
            setInitializeState(InitializeState.ERROR);
            Optional.ofNullable(getView()).ifPresent(view -> {
                switch (error) {
                    case LANG_NOT_SUPPORTED:
                    case FAILURE:
                        try {
                            view.onShowAndroidSettings("com.android.settings.TTS_SETTINGS");
                        } catch (Exception ex){
                            view.onShowAndroidSettings(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        }
                        break;
                    case LANG_MISSING_DATA:
                        view.onShowAndroidSettings(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        break;
                }
            });
        }
    }

    @Override
    public void onSpeakStart() {
        Timber.i("ReadNotificationPresenter.onSpeakStart");
        Notification notification = mPendingNotification;
        mPendingNotification = null;
        mReadingNotification = notification;
        mIsReadState = true;
    }

    @Override
    public void onSpeakDone() {
        Timber.i("ReadNotificationPresenter.onSpeakDone");
        mEventBus.post(new MessageReadFinishedEvent(mReadingNotification.getPackageName()));
        mReadingNotification = null;
        //通知読み上げ終了
        mPrepareReadCase.finish();
    }

    @Override
    public void onSpeakError(@NonNull TextToSpeechController.Error error) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            switch (error) {
                case LANG_NOT_SUPPORTED:
                case FAILURE:
                    try {
                        view.onShowAndroidSettings("com.android.settings.TTS_SETTINGS");
                    } catch (Exception ex){
                        view.onShowAndroidSettings(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    }
                    break;
                case LANG_MISSING_DATA:
                    view.onShowAndroidSettings(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    break;
            }
        });
        mReadingNotification = null;
        //通知読み上げ終了
        mPrepareReadCase.finish();
    }

    /**
     * 通知監視サービス再接続タスク.
     * <p>
     * 1分に1回、5回リトライを実施する。
     * 通知領域へのアクセスが許可されていない場合は実施しない。
     * 既に接続されている場合は実施しない。
     */
    class ReconnectNotificationListenerServiceTask implements Runnable {
        private static final long DELAY_TIME = 60000;
        private static final int MAX_TRY_COUNT = 5;
        private int mCount;

        /**
         * コンストラクタ
         */
        ReconnectNotificationListenerServiceTask() {
            mCount = MAX_TRY_COUNT;
        }

        /**
         * 開始.
         */
        public void start(){
            mHandler.postDelayed(this, DELAY_TIME);
        }

        @Override
        public void run() {
            try {
                if(!mIsGrantCase.execute()){
                    Timber.d("ReconnectNotificationListenerServiceTask run() not GrantReadNotification");
                    return;
                }

                if(mStatusHolder.getAppStatus().isConnectNotificationListenerService){
                    Timber.d("ReconnectNotificationListenerServiceTask run() is connected");
                    return;
                }

                if (--mCount >= 0) {
                    PackageManager pm = mContext.getPackageManager();
                    Timber.i("ReconnectNotificationListenerServiceTask run() disconnect");
                    pm.setComponentEnabledSetting(new ComponentName(mContext, NotificationListenerServiceImpl.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    Timber.i("ReconnectNotificationListenerServiceTask run() connect");
                    pm.setComponentEnabledSetting(new ComponentName(mContext, NotificationListenerServiceImpl.class),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                    mHandler.postDelayed(this, DELAY_TIME);
                }
            } catch (Exception ex){
                Timber.e(ex);
            }
        }
    }

    /**
     * アレクサのイベントのコールバックを受けるメソッド.
     */
    private class AlexaCallback implements AmazonAlexaManager.IAlexaCallback{

        @Override
        public void onLoginSuccess() {

        }

        @Override
        public void onLoginFailed() {

        }

        @Override
        public void onLogout() {
            Timber.d("onLogout");
            AppStatus appStatus = mStatusHolder.getAppStatus();
            appStatus.alexaAuthenticated = false;
            //ログアウト時にCapabilitiesSend状態クリア
            mPreference.setAlexaCapabilitiesSend(false);
            if (appStatus.appMusicAudioMode == AudioMode.ALEXA) {
                appStatus.appMusicAudioMode = AudioMode.MEDIA;
                mEventBus.post(new AppMusicAudioModeChangeEvent());
                AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
                if (audioManager != null) {
                    audioManager.doStop();
                }
            }
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

        }

        @Override
        public void onSpeakingResume() {

        }

        @Override
        public void onSpeakingPause() {

        }

        @Override
        public void onSpeakingStop() {

        }

        @Override
        public void onSpeakingComplete() {

        }

        @Override
        public void onAudioPrepare() {

        }

        @Override
        public void onAudioPrepared() {

        }

        @Override
        public void onAudioResume() {
            Timber.d("onAudioResume");
            if(mStatusHolder.getCarDeviceStatus().sourceType!=MediaSourceType.APP_MUSIC){
                mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.alexaStart));
                mControlSource.selectSource(MediaSourceType.APP_MUSIC);
            }
            AppStatus appStatus  = mStatusHolder.getAppStatus();
            if(appStatus.appMusicAudioMode==AudioMode.MEDIA) {
                appStatus.appMusicAudioMode = AudioMode.ALEXA;
                mEventBus.post(new AppMusicAudioModeChangeEvent());
            }
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
            AppStatus appStatus = mStatusHolder.getAppStatus();
            appStatus.alexaNotification = true;
            mEventBus.post(new AlexaNotificationChangeEvent());
        }

        @Override
        public void onClearVisualIndicator() {
            Timber.d("onClearVisualIndicator");
            AppStatus appStatus = mStatusHolder.getAppStatus();
            appStatus.alexaNotification = false;
            mEventBus.post(new AlexaNotificationChangeEvent());
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

        }

        @Override
        public void onMicrophonePermission(int state) {

        }

        @Override
        public void onNoDirectiveAtSendEventResponse() {

        }

        @Override
        public void onReceiveRenderPlayerInfo(RenderPlayerInfoItem playerInfoItem) {
            Timber.d("onReceiveRenderPlayerInfo");
            AppStatus appStatus = mStatusHolder.getAppStatus();
            appStatus.playerInfoItem = playerInfoItem;
            mControlCase.sendMusicInfo();
            mEventBus.post(new AlexaRenderPlayerInfoUpdateEvent());
        }

        @Override
        public void onAudioStart() {
            Timber.d("onAudioStart");
            if(mStatusHolder.getCarDeviceStatus().sourceType!=MediaSourceType.APP_MUSIC){
                mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.alexaStart));
                mControlSource.selectSource(MediaSourceType.APP_MUSIC);
            }
            AppStatus appStatus  = mStatusHolder.getAppStatus();
            if(appStatus.appMusicAudioMode==AudioMode.MEDIA) {
                appStatus.appMusicAudioMode = AudioMode.ALEXA;
                mEventBus.post(new AppMusicAudioModeChangeEvent());
            }
        }

        @Override
        public void onAudioUpdateProgress(int current, int duration) {
            Timber.d("onAudioUpdateProgress");
            if(mStatusHolder.getCarDeviceStatus().sourceType==MediaSourceType.APP_MUSIC) {
                AppStatus appStatus = mStatusHolder.getAppStatus();
                appStatus.alexaAudioPlayPosition = current;
                appStatus.alexaAudioPlayDuration = duration;
                mEventBus.post(new AppMusicPlayPositionChangeEvent());
                if (appStatus.playerInfoItem != null) {
                    mControlCase.sendPlaybackTime(duration, current);
                }
            }else{
                //AppMusicソースでなかったらソース変更する
                mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.alexaStart));
                mControlSource.selectSource(MediaSourceType.APP_MUSIC);
            }
        }

        @Override
        public void onSystemError() {

        }

        @Override
        public void onCapabilitiesSendSuccess() {
            Timber.d("onCapabilitiesSendSuccess");
            mPreference.setAlexaCapabilitiesSend(true);
        }

        @Override
        public void onSetNaviDestination(Double latitude, Double longitude, String name) {
            Timber.d("onSetNaviDestination");
            Optional.ofNullable(getView()).ifPresent(view -> view.setNaviDestination(latitude,longitude,name));
        }

        @Override
        public void onRecordingNotAvailable() {

        }
        @Override
        public void onWLAudioFocusLoss() {

        }

        @Override
        public void onDecodeStart() {

        }

        @Override
        public void onDecodeFinish() {

        }
    }
}
