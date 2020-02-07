package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.SparseArray;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.event.AdasDetectingEvent;
import jp.pioneer.carsync.domain.event.AdasErrorEvent;
import jp.pioneer.carsync.domain.event.AlexaNotificationChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicAudioModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicPlayPositionChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicTrackChangeEvent;
import jp.pioneer.carsync.domain.event.BtAudioInfoChangeEvent;
import jp.pioneer.carsync.domain.event.CdInfoChangeEvent;
import jp.pioneer.carsync.domain.event.DabInfoChangeEvent;
import jp.pioneer.carsync.domain.event.HdRadioInfoChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.event.NotificationListenerServiceConnectedEvent;
import jp.pioneer.carsync.domain.event.PandoraInfoChangeEvent;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationPostedEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationRemovedEvent;
import jp.pioneer.carsync.domain.event.SmartPhoneControlCommandEvent;
import jp.pioneer.carsync.domain.event.SpotifyInfoChangeEvent;
import jp.pioneer.carsync.domain.event.SxmInfoChangeEvent;
import jp.pioneer.carsync.domain.event.SystemSettingChangeEvent;
import jp.pioneer.carsync.domain.event.UsbInfoChangeEvent;
import jp.pioneer.carsync.domain.event.VoiceRecognitionTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ActionSoftwareShortcutKey;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetReadNotificationList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.InitializeSlaSetting;
import jp.pioneer.carsync.domain.interactor.PreferMusicApp;
import jp.pioneer.carsync.domain.interactor.QuerySettingList;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.model.AdasErrorType;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.BtAudioInfo;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.CarRunningStatus;
import jp.pioneer.carsync.domain.model.CdInfo;
import jp.pioneer.carsync.domain.model.ConnectionType;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.MediaSourceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.Notification;
import jp.pioneer.carsync.domain.model.PandoraMediaInfo;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.SmartPhoneControlCommand;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.SpotifyMediaInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.TimeFormatSetting;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.model.UsbMediaInfo;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;
import jp.pioneer.carsync.presentation.event.AlexaRenderPlayerInfoUpdateEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.event.SessionCompletedEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.util.Adas;
import jp.pioneer.carsync.presentation.util.AndroidMusicTextUtil;
import jp.pioneer.carsync.presentation.util.BtAudioTextUtil;
import jp.pioneer.carsync.presentation.util.CdTextUtil;
import jp.pioneer.carsync.presentation.util.CustomKeyActionHandler;
import jp.pioneer.carsync.presentation.util.FrequencyUtil;
import jp.pioneer.carsync.presentation.util.HdRadioTextUtil;
import jp.pioneer.carsync.presentation.util.PandoraTextUtil;
import jp.pioneer.carsync.presentation.util.RadioTextUtil;
import jp.pioneer.carsync.presentation.util.ShortCutKeyEnabledStatus;
import jp.pioneer.carsync.presentation.util.SpotifyTextUtil;
import jp.pioneer.carsync.presentation.util.SxmTextUtil;
import jp.pioneer.carsync.presentation.util.UsbTextUtil;
import jp.pioneer.carsync.presentation.util.YouTubeLinkActionHandler;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.HomeView;
import jp.pioneer.carsync.presentation.view.argument.PermissionParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.PlayerContainerFragment;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.manager.AlexaAudioManager;
import timber.log.Timber;

import static jp.pioneer.carsync.domain.model.PlaybackMode.STOP;

/**
 * HOMEのPresenter
 */
@PresenterLifeCycle
public class HomePresenter extends Presenter<HomeView> implements LoaderManager.LoaderCallbacks<Cursor> {

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject ActionSoftwareShortcutKey mShortcutCase;
    @Inject GetStatusHolder mGetCase;
    @Inject GetReadNotificationList mNotificationCase;
    @Inject AppSharedPreference mPreference;
    @Inject QueryTunerItem mTunerCase;
    @Inject QuerySettingList mGetSettingList;
    @Inject ControlSource mControlSource;
    @Inject InitializeSlaSetting mInitializeSlaSetting;
    @Inject Adas mAdas;
    @Inject CarDeviceMediaRepository mCarDeviceMediaRepository;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    @Inject PreferMusicApp mPreferMusicApp;
    @Inject CustomKeyActionHandler mCustomKeyActionHandler;
    @Inject YouTubeLinkActionHandler mYouTubeLinkActionHandler;
    @Inject YouTubeLinkStatus mYouTubeLinkStatus;
    @Inject ShortCutKeyEnabledStatus mShortCutKeyEnabledStatus;
    @Inject AnalyticsEventManager mAnalytics;
    private List<Notification> mNotifications;
    private PermissionParams mParams;
    private static final ShortcutKey[] KEY_INDEX = new ShortcutKey[]{
            ShortcutKey.SOURCE,
            ShortcutKey.VOICE,
            ShortcutKey.NAVI,
            ShortcutKey.MESSAGE,
            ShortcutKey.PHONE,
    };
    private static final int[][] KEY_IMAGES = new int[][]{
            {R.drawable.p2002_customkey_btn_1nrm, 0},//Source
            {R.drawable.p0162_vrbtn_1nrm, 0},//Voice
            {R.drawable.p0163_navibtn_1nrm, 0},//Navi
            {R.drawable.p0164_messagebtn_1nrm, R.drawable.p0171_notification},//Message
            {R.drawable.p0165_phonebtn_1nrm, 0},//Phone
    };
    private ArrayList<ShortcutKeyItem> mShortCutKeyList = new ArrayList<>();
    private Set<MediaSourceType> mAvailableTypeSet = new HashSet<>();
    private static final int LOADER_ID_PRESET = 1;
    private static final int LOADER_ID_USER_PRESET = 2;
    private static final String KEY_BAND_TYPE = "band_type";
    private static final String EMPTY = "";
    private LoaderManager mLoaderManager;
    private MediaSourceType mSourceType;
    private RadioInfo mCurrRadio;
    private RadioBandType mRadioBand;
    private SparseArray<String> mPresets = new SparseArray<>();
    private SparseArray<Long> mUserPreset = new SparseArray<>();
    private HdRadioInfo mCurrHdRadio;
    private HdRadioBandType mHdRadioBand;
    private SxmMediaInfo mCurrSxm;
    private SxmBandType mSxmBand;
    private DabInfo mCurrDab;
    private final static int YOUTUBE_LINK_ICON = R.drawable.p2001_youtubelink_btn_1nrm;
    private final static int CUSTOM_KEY_ICON = R.drawable.p2002_customkey_btn_1nrm;
    @Inject
    public HomePresenter() {
        for (int i = 0; i < KEY_INDEX.length; i++) {
            ShortcutKeyItem key = new ShortcutKeyItem(KEY_INDEX[i], KEY_IMAGES[i][0], KEY_IMAGES[i][1],true);
            mShortCutKeyList.add(key);
        }
    }

    @Override
    public void onInitialize() {
        for (int i = 0; i < mShortCutKeyList.size(); i++) {
            ShortcutKeyItem item = mShortCutKeyList.get(i);
            item.enabled = true;
        }
        mSourceType = getMediaSourceType();
        StatusHolder holder = mGetCase.execute();
        Set<MediaSourceType> typeSet = holder.getCarDeviceStatus().availableSourceTypes;
        mAvailableTypeSet = new HashSet<>(typeSet);
    }

    @Override
    void onTakeView() {
        // ショートカット領域のアイコン更新(YouTubeアイコン、音声認識のアイコン)
        setShortcutIconResources();
        mSourceType = getMediaSourceType();
        Configuration config = mContext.getResources().getConfiguration();
        int orientation = config.orientation;
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        if(appStatus.isAgreedCaution&&orientation != Configuration.ORIENTATION_LANDSCAPE){
            appStatus.homeViewAdas = false;
        }
        boolean AdasEnabled = ((mPreference.isAdasEnabled()&&
                        mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN&&
                        (mGetCase.execute().getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING))
                        ||mPreference.isAdasPseudoCooperation());
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setSpeedMeterViewType(orientation == Configuration.ORIENTATION_LANDSCAPE&&AdasEnabled
                    , AdasEnabled
                            ?(appStatus.isAgreedCaution
                                ?appStatus.homeViewAdas:
                                    orientation==Configuration.ORIENTATION_LANDSCAPE
                                        ?appStatus.homeViewAdas:false
                            )
                     :false);
            if(appStatus.homeCenterViewAdas){
                view.setClockView(2);
            }else {
                view.setClockView(mPreference.getClockType());
            }
            view.setColor(mPreference.getUiColor().getResource());
            view.setShortcutKeyItems(mShortCutKeyList);
            view.setShortCutButtonEnabled(mShortCutKeyEnabledStatus.isShortCutKeyEnabled());
            view.setAlexaNotification(mGetCase.execute().getAppStatus().alexaNotification);
        });

        appStatus.isTransitionedHomeScreen = true;

        if (!mPreference.isConfiguredSlaSetting()) {
            CarDeviceStatus status = mGetCase.execute().getCarDeviceStatus();
            if(mGetCase.execute().getSessionStatus() == SessionStatus.STARTED) {
                if (status.sourceType == MediaSourceType.APP_MUSIC &&
                        status.sourceStatus == MediaSourceStatus.CHANGE_COMPLETED) {
                    mInitializeSlaSetting.execute();
                    mPreference.setConfiguredSlaSetting(true);
                }
            }
        }

        // カスタムキー[ソース一覧表示]動作のセット
        mCustomKeyActionHandler.setSourceListAction(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putBoolean(PlayerContainerFragment.KEY_HOME_SOURCE_OFF, true);
                mEventBus.post(new NavigateEvent(ScreenId.PLAYER_CONTAINER, bundle));
            }
        });
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        mSourceType = getMediaSourceType();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setAdasEnabled((mPreference.isAdasEnabled()&&mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN&&(mGetCase.execute().getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING))||mPreference.isAdasPseudoCooperation());
            view.setAdasDetection(mAdas.isPedestrianDetecting(),mAdas.isVehicleDetecting(),mAdas.isLeftLaneDetecting(),mAdas.isRightLaneDetecting());
        });
        setAdasIcon();
        updateSourceView();
        updateNotification();
        updateAlexaNotification();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * パーミッションの保持
     *
     * @param args Bundle
     */
    public void setArgument(Bundle args) {
        mParams = PermissionParams.from(args);
    }

    public String getAdasInfoText(){
        String text="";

        text = mAdas.getFpsString()
                +"\nSpeed:" + (mAdas.getSpeed()!=Adas.INVALID&&mAdas.getSpeed()!=-1?String.valueOf(mAdas.getSpeed())+"km/h":"")
                +"\nSensor Y:" + (mAdas.getSensorY()!=Adas.INVALID?String.format(Locale.ENGLISH,"%.6fG",mAdas.getSensorY()*0.1019) :"")
                +"\nSensor Z:" + (mAdas.getSensorZ()!=Adas.INVALID?String.format(Locale.ENGLISH,"%.6fG",mAdas.getSensorZ()*0.1019) :"")
                +"\nDashboard:" + (mAdas.getDashboardRate()!=Adas.INVALID?String.format(Locale.ENGLISH,"%.6f",mAdas.getDashboardRate()):"")
                +"\nDashboard補正:" + (mAdas.getDashboardRateOffset()!=Adas.INVALID?String.format(Locale.ENGLISH,"%.6f",mAdas.getDashboardRateOffset()):"");
        return text;
    }

    /**
     * ShortCutKeyの遷移アクション
     */
    public void onKeyAction(ShortcutKey shortCutKey) {
        switch (shortCutKey) {
            case SOURCE:
                //カスタムキー
                Timber.d("onSourceAction");
                if(mYouTubeLinkStatus.isYouTubeLinkEnabled()){
                    //YouTubeLink機能設定がONの場合
                    mYouTubeLinkActionHandler.execute();
                    mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.youTubeLinkShort, Analytics.AnalyticsActiveScreen.home_screen);
                } else {
                    //カスタムの割り当てがソース切替の場合
                    mCustomKeyActionHandler.execute();
                    mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.customShort, Analytics.AnalyticsActiveScreen.home_screen);
                }
                break;
            case VOICE:
                Timber.d("onVoiceAction");
                mShortcutCase.execute(ShortcutKey.VOICE);
                mAnalytics.sendShortCutActionEvent(mPreference.getVoiceRecognitionType()==VoiceRecognizeType.ALEXA?Analytics.AnalyticsShortcutAction.alexaShort:Analytics.AnalyticsShortcutAction.voiceShort, Analytics.AnalyticsActiveScreen.home_screen);
                break;
            case NAVI:
                Timber.d("onNavigateAction");
                mShortcutCase.execute(ShortcutKey.NAVI);
                mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.navi, Analytics.AnalyticsActiveScreen.home_screen);
                break;
            case MESSAGE:
                Timber.d("onMessageAction");
                mShortcutCase.execute(ShortcutKey.MESSAGE);
                mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.message, Analytics.AnalyticsActiveScreen.home_screen);
                break;
            case PHONE:
                Timber.d("onPhoneAction");
                mShortcutCase.execute(ShortcutKey.PHONE);
                mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.phoneShort, Analytics.AnalyticsActiveScreen.home_screen);
                break;
            default:
                break;
        }
    }

    public void onPlayerAction(){
        Bundle bundle = new Bundle();
        if(getMediaSourceType()==MediaSourceType.OFF) {
            bundle.putBoolean(PlayerContainerFragment.KEY_HOME_SOURCE_OFF, true);
        }else{
            bundle.putBoolean(PlayerContainerFragment.KEY_HOME_SOURCE_OFF, false);
        }
        mEventBus.post(new NavigateEvent(ScreenId.PLAYER_CONTAINER, bundle));

    }

    public void onAdasErrorAction(){
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        if(appStatus.isAdasError()){
            Set<AdasErrorType> errors = appStatus.adasErrorList;

            Bundle bundle = new Bundle();
            String title="";
            String text="";
            if(errors.contains(AdasErrorType.ORIENTATION_PORTRAIT)){
                text = mContext.getString(R.string.err_036);
            }else if(errors.contains(AdasErrorType.PERMISSION_DENIED_ACCESS_LOCATION)){
                title = mContext.getString(R.string.err_027);
                text = mContext.getString(R.string.err_029);
            }else if(errors.contains(AdasErrorType.PERMISSION_DENIED_CAMERA)){
                title = mContext.getString(R.string.err_027);
                text = mContext.getString(R.string.err_030);
            }else if(errors.contains(AdasErrorType.DECLINE_IN_RECOGNITION_RATE)){
                title = mContext.getString(R.string.err_028);
                text = mContext.getString(R.string.err_031);
            }else if(errors.contains(AdasErrorType.LOW_ILLUMINANCE_OR_POOR_VISIBILITY)){
                title = mContext.getString(R.string.err_020);
                text = mContext.getString(R.string.err_023);
            }else if(errors.contains(AdasErrorType.ALARM_ERROR_DURING_NETWORK_MODE)){
                title = mContext.getString(R.string.err_022);
                text = mContext.getString(R.string.err_026);
            }else if(errors.contains(AdasErrorType.ALARM_ERROR_SOURCE_OFF)){
                title = mContext.getString(R.string.err_022);
                text = mContext.getString(R.string.err_025);
            }
            mEventBus.post(new AdasErrorEvent());
            bundle.putString(StatusPopupDialogFragment.TAG, title);
            bundle.putString(StatusPopupDialogFragment.TITLE, title);
            bundle.putString(StatusPopupDialogFragment.MESSAGE, text);
            bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
            mEventBus.post(new NavigateEvent(ScreenId.MAIN_STATUS_DIALOG, bundle));
        }
    }

    public void onLongKeyAction(ShortcutKey shortCutKey) {
        switch (shortCutKey) {
            case PHONE:
                Timber.d("onDirectCallAction");
                if(mPreference.getDirectCallContactNumberId() > -1) {
                    mEventBus.post(new SmartPhoneControlCommandEvent(SmartPhoneControlCommand.DIRECT_CALL));
                }
                mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.phoneLong, Analytics.AnalyticsActiveScreen.home_screen);
                break;
            case SOURCE:
                if(mYouTubeLinkStatus.isYouTubeLinkEnabled()) {
                    // YouTube Link検索対象切り替えを表示する
                    mEventBus.post(new NavigateEvent(ScreenId.YOUTUBE_LINK_SEARCH_ITEM, Bundle.EMPTY));
                    mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.youtubeLinkLong, Analytics.AnalyticsActiveScreen.home_screen);
                } else {
                    // カスタムキー割当画面の表示
                    mEventBus.post(new NavigateEvent(ScreenId.CUSTOM_KEY_SETTING, Bundle.EMPTY));
                    mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.customLong, Analytics.AnalyticsActiveScreen.home_screen);
                }
                break;
            case VOICE:
                if(!mGetCase.execute().getCarDeviceStatus().androidVrEnabled) {
                    if (mGetCase.execute().getAppStatus().isAlexaAvailableCountry) {
                        mAnalytics.sendShortCutActionEvent(mPreference.getVoiceRecognitionType() == VoiceRecognizeType.ALEXA ? Analytics.AnalyticsShortcutAction.alexaLong : Analytics.AnalyticsShortcutAction.voiceLong, Analytics.AnalyticsActiveScreen.home_screen);
                        VoiceRecognizeType nextType = mPreference.getVoiceRecognitionType().toggle();
                        mPreference.setVoiceRecognitionType(nextType);
                        mEventBus.post(new VoiceRecognitionTypeChangeEvent());
                    }
                }else{
                    mEventBus.post(new NavigateEvent(ScreenId.VOICE_RECOGNIZE_TYPE_DIALOG, Bundle.EMPTY));
                }

                break;
            default:
                break;
        }
    }

    /**
     * 時計種別の変更アクション
     */
    public void onChangeClockAction(int type) {
        mPreference.setClockType(type);
    }

    /**
     * スピードメーター種別の変更アクション
     */
    public void onChangeSpeedMeterAction(boolean adasView) {
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.homeViewAdas = adasView;
    }

    /**
     * Settingへの遷移アクション
     */
    public void onSettingsAction(Bundle bundle) {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_CONTAINER, bundle));
    }


    public MediaSourceType getMediaSourceType() {
        StatusHolder holder = mGetCase.execute();
        return holder.getCarDeviceStatus().sourceType;
    }

    private void updateSourceView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(!mAvailableTypeSet.contains(mSourceType)){
                view.setPlayerView(MediaSourceType.OFF);
                updateUnsupportedView();;
                return;
            }
            view.setPlayerView(mSourceType);
            switch (mSourceType) {
                case APP_MUSIC:
                    updateAndroidMusicView();
                    break;
                case CD:
                    updateCdView();
                    break;
                case USB:
                    if(mGetCase.execute().getConnectionType() == ConnectionType.USB){
                        updateUnsupportedView();
                    } else {
                        updateUsbView();
                    }
                    break;
                case PANDORA:
                    updatePandoraView();
                    break;
                case SPOTIFY:
                    updateSpotifyView();
                    break;
                case BT_AUDIO:
                    updateBtAudioView();
                    break;
                case RADIO:
                    updateRadioView();
                    break;
                case DAB:
                    updateDabView();
                    break;
                case SIRIUS_XM:
                    updateSxmView();
                    break;
                case HD_RADIO:
                    updateHdRadioView();
                    break;
                case AUX:
                    updateAuxView();
                    break;
                case BT_PHONE:
                    //TODO:BT_PHONE再生情報表示
                    updateUnsupportedView();
                    break;
                case OFF:
                    updateOffView();
                    break;
                case IPOD:
                    //TODO:IPOD再生情報表示
                    updateUnsupportedView();
                    break;
                case TI:
                    updateTiView();
                    break;
                default:
                    updateUnsupportedView();
                    //throw new IllegalArgumentException("Not car device media source.");
            }
        });
    }

    private void updateAndroidMusicView() {
        StatusHolder holder = mGetCase.execute();
        AppStatus appStatus = holder.getAppStatus();
        if(appStatus.appMusicAudioMode== AudioMode.MEDIA) {
            CarDeviceMediaInfoHolder mediaHolder = holder.getCarDeviceMediaInfoHolder();
            SmartPhoneStatus status = holder.getSmartPhoneStatus();
            AndroidMusicMediaInfo androidMusicInfo = mediaHolder.androidMusicMediaInfo;
            Optional.ofNullable(getView()).ifPresent(view -> {
                view.setMusicTitle(AndroidMusicTextUtil.getSongTitle(mContext, status, androidMusicInfo));
                switch (status.playbackMode) {
                    case STOP:
                        view.setMusicAlbumArt(null);
                        break;
                    default:
                        view.setMusicAlbumArt(androidMusicInfo.artworkImageLocation);
                        break;
                }
                view.setMaxProgress(androidMusicInfo.durationInSec);
                view.setCurrentProgress(androidMusicInfo.positionInSec);
            });
        }else{
            if(appStatus.playerInfoItem!=null) {
                RenderPlayerInfoItem renderPlayerInfoItem = appStatus.playerInfoItem;
                AlexaIfDirectiveItem.Content content = renderPlayerInfoItem.content;
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.setMusicTitle(content.getTitle());
                    AlexaIfDirectiveItem.Source source = null;
                    {
                        AlexaIfDirectiveItem.ImageStructure art = content.getArt();
                        if (art != null) {
                            List<AlexaIfDirectiveItem.Source> sources = art.getSources();
                            if (sources != null && sources.size() > 0) {
                                //small→...→x-largeと仮定して、Listの最後の画像(Large)を取得
                                int logoSize = sources.size() - 1;
                                source = sources.get(logoSize);
                            }
                        }
                    }
                    String imageUrl = null;
                    if (source != null) {
                        imageUrl = source.getUrl();
                    }
                    AlexaIfDirectiveItem.ImageStructure image = content.getArt();
                    List<AlexaIfDirectiveItem.Source> sources = image.getSources();
                    if (imageUrl != null) {
                        view.setMusicAlbumArt(Uri.parse(imageUrl));
                    } else {
                        view.setMusicAlbumArt(null);
                    }
                    view.setMaxProgress(content.getMediaLengthInMilliseconds().intValue());
                });
            }
        }

    }

    private void updateCdView() {
        CarDeviceMediaInfoHolder mediaHolder = mGetCase.execute().getCarDeviceMediaInfoHolder();
        CdInfo cdInfo = mediaHolder.cdInfo;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(cdInfo.playbackMode == STOP ? EMPTY : CdTextUtil.getTrackNumber(mContext, cdInfo));
            view.setCenterImage(R.drawable.p0271_sourceimg);
            view.setMaxProgress(cdInfo.totalSecond);
            view.setCurrentProgress(cdInfo.currentSecond);
        });
    }

    private void updateUsbView() {
        CarDeviceMediaInfoHolder mediaHolder = mGetCase.execute().getCarDeviceMediaInfoHolder();
        UsbMediaInfo usbMediaInfo = mediaHolder.usbMediaInfo;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(UsbTextUtil.getSongTitleForPlayer(mContext, usbMediaInfo));
            view.setCenterImage(R.drawable.p0272_sourceimg);
            view.setMaxProgress(usbMediaInfo.totalSecond);
            view.setCurrentProgress(usbMediaInfo.currentSecond);
        });
    }

    private void updatePandoraView() {
        CarDeviceMediaInfoHolder mediaHolder = mGetCase.execute().getCarDeviceMediaInfoHolder();
        PandoraMediaInfo pandoraMediaInfo = mediaHolder.pandoraMediaInfo;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(pandoraMediaInfo.playbackMode == STOP ? EMPTY : PandoraTextUtil.getSongTitle(mContext, pandoraMediaInfo));
            view.setCenterImage(R.drawable.p0274_sourceimg);
            view.setMaxProgress(pandoraMediaInfo.totalSecond);
            view.setCurrentProgress(pandoraMediaInfo.currentSecond);
        });
    }

    private void updateSpotifyView() {
        CarDeviceMediaInfoHolder mediaHolder = mGetCase.execute().getCarDeviceMediaInfoHolder();
        SpotifyMediaInfo spotifyMediaInfo = mediaHolder.spotifyMediaInfo;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(SpotifyTextUtil.getSongTitle(mContext, spotifyMediaInfo));
            view.setCenterImage(R.drawable.p0274_sourceimg);
            view.setMaxProgress(spotifyMediaInfo.totalSecond);
            view.setCurrentProgress(spotifyMediaInfo.currentSecond);
        });
    }

    private void updateBtAudioView() {
        CarDeviceMediaInfoHolder mediaHolder = mGetCase.execute().getCarDeviceMediaInfoHolder();
        BtAudioInfo btAudioInfo = mediaHolder.btAudioInfo;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(BtAudioTextUtil.getPlayerSongTitle(mContext, btAudioInfo));
            view.setCenterImage(R.drawable.p0273_sourceimg);
            view.setMaxProgress(btAudioInfo.totalSecond);
            view.setCurrentProgress(btAudioInfo.currentSecond);
            view.setAudioDeviceName(BtAudioTextUtil.getPlayerDeviceName(mContext, btAudioInfo));
        });
    }

    private void updateAuxView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(EMPTY);
            view.setCenterImage(R.drawable.p0276_sourceimg);
            view.setMaxProgress(0);
            view.setCurrentProgress(0);
        });
    }

    private void updateOffView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(EMPTY);
            view.setCenterImage(R.drawable.p0275_sourceimg);
            view.setMaxProgress(0);
            view.setCurrentProgress(0);
        });
    }

    private void updateUnsupportedView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(EMPTY);
            view.setCenterImage(R.drawable.p0270_nosource);
            view.setMaxProgress(0);
            view.setCurrentProgress(0);
        });
    }

    private void updateTiView() {
        CarDeviceMediaInfoHolder mediaHolder = mGetCase.execute().getCarDeviceMediaInfoHolder();
        CarDeviceStatus status = mGetCase.execute().getCarDeviceStatus();
        mCurrRadio = mediaHolder.radioInfo;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(mContext.getString(R.string.ply_040));
            view.setRadioInfo(status, mCurrRadio);
            view.setTiInfo();
        });
    }

    private void updateRadioView() {
        CarDeviceMediaInfoHolder mediaHolder = mGetCase.execute().getCarDeviceMediaInfoHolder();
        CarDeviceStatus status = mGetCase.execute().getCarDeviceStatus();
        mCurrRadio = mediaHolder.radioInfo;
        mRadioBand = mCurrRadio.band;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(RadioTextUtil.getPsInfoForMiniPlayer(mContext, status, mCurrRadio));
            view.setRadioInfo(status, mCurrRadio);
        });
        getPch();
    }

    private void updateHdRadioView() {
        CarDeviceMediaInfoHolder mediaHolder = mGetCase.execute().getCarDeviceMediaInfoHolder();
        CarDeviceStatus status = mGetCase.execute().getCarDeviceStatus();
        mCurrHdRadio = mediaHolder.hdRadioInfo;
        mHdRadioBand = mCurrHdRadio.band;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(HdRadioTextUtil.getStationInfoForPlayer(mContext, status, mCurrHdRadio));
            view.setHdRadioInfo(status, mCurrHdRadio);
        });
        getPch();
    }

    private void updateDabView() {
        CarDeviceMediaInfoHolder mediaHolder = mGetCase.execute().getCarDeviceMediaInfoHolder();
        CarDeviceStatus status = mGetCase.execute().getCarDeviceStatus();
        mCurrDab = mediaHolder.dabInfo;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setDabInfo(status, mCurrDab);
        });
    }

    private void updateSxmView() {
        CarDeviceMediaInfoHolder mediaHolder = mGetCase.execute().getCarDeviceMediaInfoHolder();
        CarDeviceStatus status = mGetCase.execute().getCarDeviceStatus();
        mCurrSxm = mediaHolder.sxmMediaInfo;
        mSxmBand = mCurrSxm.band;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setMusicTitle(SxmTextUtil.getMiniPlayerTitle(mContext, mCurrSxm));
            view.setSxmInfo(status, mCurrSxm);
        });
        getPch();
    }

    private void getPch() {
        Bundle args = new Bundle();
        if (mSourceType == MediaSourceType.RADIO) {
            if (mRadioBand != null) {
                args.putByte(KEY_BAND_TYPE, (byte) (mRadioBand.getCode() & 0xFF));
                mLoaderManager.restartLoader(LOADER_ID_PRESET, args, this);
            }
            getUserPresetList();
        } else if (mSourceType == MediaSourceType.SIRIUS_XM) {
            if (mSxmBand != null) {
                args.putByte(KEY_BAND_TYPE, (byte) (mSxmBand.getCode() & 0xFF));
                mLoaderManager.restartLoader(LOADER_ID_PRESET, args, this);
            }
        } else if (mSourceType == MediaSourceType.HD_RADIO) {
            if (mHdRadioBand != null) {
                args.putByte(KEY_BAND_TYPE, (byte) (mHdRadioBand.getCode() & 0xFF));
                mLoaderManager.restartLoader(LOADER_ID_PRESET, args, this);
            }
        }
    }

    /**
     * ユーザー登録PCHリスト取得
     */
    public void getUserPresetList(){
        if(isSphCarDevice()) {
            if (mRadioBand != null && mLoaderManager != null) {
                Bundle args = new Bundle();
                args.putByte(KEY_BAND_TYPE, (byte) (mRadioBand.getCode() & 0xFF));
                mLoaderManager.restartLoader(LOADER_ID_USER_PRESET, args, this);
            }
        }
    }

    private boolean isSphCarDevice(){
        return mGetCase.execute().getProtocolSpec().isSphCarDevice();
    }
    /**
     * LoaderManager登録
     *
     * @param loaderManager LoaderManager
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_PRESET) {
            if (mSourceType == MediaSourceType.RADIO) {
                return mTunerCase.getPresetList(MediaSourceType.RADIO, RadioBandType.valueOf(args.getByte(KEY_BAND_TYPE)));
            } else if (mSourceType == MediaSourceType.SIRIUS_XM) {
                return mTunerCase.getPresetList(MediaSourceType.SIRIUS_XM, SxmBandType.valueOf(args.getByte(KEY_BAND_TYPE)));
            } else if (mSourceType == MediaSourceType.HD_RADIO) {
                return mTunerCase.getPresetList(MediaSourceType.HD_RADIO, HdRadioBandType.valueOf(args.getByte(KEY_BAND_TYPE)));
            }
        }else if(id == LOADER_ID_USER_PRESET){
            if (mSourceType == MediaSourceType.RADIO) {
                return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createRadioPreset(RadioBandType.valueOf(args.getByte(KEY_BAND_TYPE))));
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (id == LOADER_ID_PRESET) {
                SparseArray presetList = createPresetList(data);
                if (mSourceType == MediaSourceType.RADIO&&isSphCarDevice()){
                    setSelectedPreset();
                    return;
                }
                boolean isPreset = false;
                for (int i = 0; i < presetList.size(); i++) {
                    int key = presetList.keyAt(i);
                    boolean isExist = false;
                    if (mSourceType == MediaSourceType.RADIO) {
                        // get the object by the key.
                        String name = (String) presetList.get(key);
                        if(mGetCase.execute().getCarDeviceMediaInfoHolder().radioInfo.isSearchStatus()){
                            isExist = false;
                            isPreset = false;
                        } else {
                            isExist = name.equals(FrequencyUtil.toString(mContext, mCurrRadio.currentFrequency, mCurrRadio.frequencyUnit));
                        }
                    }else if (mSourceType == MediaSourceType.SIRIUS_XM) {
                        Integer number = (Integer) presetList.get(key);
                        isExist = number.equals(mCurrSxm.currentChannelNumber);
                    }else if (mSourceType == MediaSourceType.HD_RADIO) {
                        // get the object by the key.
                        String name = (String) presetList.get(key);
                        if(mGetCase.execute().getCarDeviceMediaInfoHolder().hdRadioInfo.isSearchStatus()){
                            isExist = false;
                            isPreset = false;
                        } else {
                            isExist = name.equals(FrequencyUtil.toString(mContext, mCurrHdRadio.currentFrequency, mCurrHdRadio.frequencyUnit));
                        }
                    }
                    if (isExist) {
                        isPreset = true;
                        view.setPch(key);
                        break;
                    }
                }
                if(!isPreset){
                    view.setPch(-1);
                }
            }else if(id == LOADER_ID_USER_PRESET){
                createUserPresetList(data);
                setSelectedPreset();
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // no action
    }
    private SparseArray createPresetList(Cursor data) {
        SparseArray<Object> preset = new SparseArray<>();
        mPresets.clear();
        boolean isEof = data.moveToFirst();
        while (isEof) {
            if (mSourceType == MediaSourceType.RADIO) {
                RadioBandType band = TunerContract.ListItemContract.Radio.getBandType(data);
                if (mRadioBand == band) {
                    int pch = TunerContract.ListItemContract.Radio.getPchNumber(data);
                    //TODO 周波数(番組名が取得できるようになる?)
                    long frequency = TunerContract.ListItemContract.Radio.getFrequency(data);
                    TunerFrequencyUnit unit = TunerContract.ListItemContract.Radio.getFrequencyUnit(data);
                    String name = FrequencyUtil.toString(mContext, frequency, unit);

                    preset.put(pch, name);
                    mPresets.put(pch, name);
                }
            } else if (mSourceType == MediaSourceType.SIRIUS_XM) {
                SxmBandType band = TunerContract.ListItemContract.SiriusXm.getBandType(data);
                if (mSxmBand == band) {
                    int pch = TunerContract.ListItemContract.SiriusXm.getPchNumber(data);
                    Integer number = TunerContract.ListItemContract.SiriusXm.getChNumber(data);

                    preset.put(pch, number);
                }
            } else if (mSourceType == MediaSourceType.HD_RADIO) {
                HdRadioBandType band = TunerContract.ListItemContract.HdRadio.getBandType(data);
                if (mHdRadioBand == band) {
                    int pch = TunerContract.ListItemContract.HdRadio.getPchNumber(data);
                    //TODO 周波数(番組名が取得できるようになる?)
                    long frequency = TunerContract.ListItemContract.HdRadio.getFrequency(data);
                    TunerFrequencyUnit unit = TunerContract.ListItemContract.HdRadio.getFrequencyUnit(data);
                    String name = FrequencyUtil.toString(mContext, frequency, unit);

                    preset.put(pch, name);
                }
            }
            isEof = data.moveToNext();
        }

        return preset;
    }
    /**
     * PCH登録リスト作成
     * <p>
     * カーソルローダではデータの照合を行えないため、リストに起こす
     *
     * @param data PCH登録情報カーソル
     */
    private void createUserPresetList(Cursor data) {
        mUserPreset.clear();
        data.moveToPosition(-1);
        while (data.moveToNext()) {
            long frequency = TunerContract.FavoriteContract.Radio.getFrequency(data);
            int presetNumber = TunerContract.FavoriteContract.Radio.getPresetNumber(data);
            mUserPreset.put(presetNumber, frequency);
        }
    }
    /**
     * 現在のプリセット番号設定
     */
    private void setSelectedPreset(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            int presetNum=0;
            int userPresetNum=0;
            boolean isPreset = false;
            boolean isUserPreset = false;
            //①従来通りの方法でP.CH番号を調べる(P.CH登録データに登録済みのP.CH番号は除く)
            for (int i = 0; i < mPresets.size(); i++) {
                int key = mPresets.keyAt(i);
                String name = mPresets.get(key);
                if(mUserPreset.get(key,-1L)==-1L) {
                    if (name.equals(FrequencyUtil.toString(mContext, mCurrRadio.currentFrequency, mCurrRadio.frequencyUnit))) {
                        isPreset = true;
                        presetNum = key;
                        break;
                    }
                }
            }
            //②P.CH登録データで周波数(extraData)が一致するP.CH番号(tunerUniqueID)を調べる
            for (int i = 0; i < mUserPreset.size(); i++) {
                int key = mUserPreset.keyAt(i);
                long frequency = mUserPreset.get(key);
                if (frequency == mCurrRadio.currentFrequency) {
                    isUserPreset = true;
                    userPresetNum = key;
                    break;
                }
            }
            if(isPreset&&isUserPreset) {
                //①と②の両方で見つかった場合、値の小さい方をP.CH番号とする
                view.setPch(Math.min(presetNum, userPresetNum));
            }else if(isPreset){
                //①のみで見つかった場合、P.CH登録データに同じP.CH番号を登録していなければ①をP.CH番号とする
                if(mUserPreset.get(presetNum,-1L)==-1L){
                    view.setPch(presetNum);
                }else{
                    view.setPch(-1);
                }
            }else if(isUserPreset){
                //②のみで見つかった場合、②をP.CH番号とする
                view.setPch(userPresetNum);
            }else{
                //いずれも該当しない場合、P.CH番号なしとする
                view.setPch(-1);
            }
        });
    }
    private void updateNotification() {
        mNotifications = mNotificationCase.execute();
        for (int i = 0; i < mShortCutKeyList.size(); i++) {
            ShortcutKeyItem item = mShortCutKeyList.get(i);
            if (item.key == ShortcutKey.MESSAGE) {
                if (mNotifications.size() > 0) {
                    item.optionImageResource = R.drawable.p0171_notification;
                } else {
                    item.optionImageResource = 0;
                }
            }
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setShortcutKeyItems(mShortCutKeyList));
    }

    /**
     * ソース種別変更イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeAction(MediaSourceTypeChangeEvent event) {
        mSourceType = getMediaSourceType();
        Optional.ofNullable(getView()).ifPresent(view -> view.setPlayerView(mSourceType));
        updateSourceView();
    }

    /**
     * 再生位置（時間）の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayPositionChangeAction(AppMusicPlayPositionChangeEvent event) {
        if (mSourceType == MediaSourceType.APP_MUSIC) {
            if(mGetCase.execute().getAppStatus().appMusicAudioMode==AudioMode.ALEXA){
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.setMaxProgress(mGetCase.execute().getAppStatus().alexaAudioPlayDuration);
                    view.setCurrentProgress(mGetCase.execute().getAppStatus().alexaAudioPlayPosition);
                });
            }else {
                AndroidMusicMediaInfo info = mGetCase.execute().getCarDeviceMediaInfoHolder().androidMusicMediaInfo;
                Optional.ofNullable(getView()).ifPresent(view -> view.setCurrentProgress(info.positionInSec));
            }
        }
    }

    /**
     * プレイヤー状態の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackChangeAction(AppMusicTrackChangeEvent event) {
        if (mSourceType == MediaSourceType.APP_MUSIC) {
            SmartPhoneStatus status = mGetCase.execute().getSmartPhoneStatus();
            AndroidMusicMediaInfo info = mGetCase.execute().getCarDeviceMediaInfoHolder().androidMusicMediaInfo;
            Optional.ofNullable(getView()).ifPresent(view -> {
                view.setMusicTitle(AndroidMusicTextUtil.getSongTitle(mContext, status, info));
                view.setMusicAlbumArt(info.artworkImageLocation);
                view.setMaxProgress(info.durationInSec);
            });
        }
    }

    /**
     * CD状態の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCdInfoChangeAction(CdInfoChangeEvent event) {
        if(!mAvailableTypeSet.contains(mSourceType))return;
        if (mSourceType == MediaSourceType.CD) {
            updateCdView();
        }
    }

    /**
     * USB状態の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUsbMediaInfoChangeAction(UsbInfoChangeEvent event) {
        if(!mAvailableTypeSet.contains(mSourceType))return;
        if (mSourceType == MediaSourceType.USB) {
            if(mGetCase.execute().getConnectionType() == ConnectionType.USB){
                updateUnsupportedView();
            } else {
                updateUsbView();
            }
        }
    }

    /**
     * Pandora再生情報の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPandoraInfoChangeAction(PandoraInfoChangeEvent event) {
        if(!mAvailableTypeSet.contains(mSourceType))return;
        if (mSourceType == MediaSourceType.PANDORA) {
            updatePandoraView();
        }
    }

    /**
     * Spotify再生情報の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSpotifyInfoChangeAction(SpotifyInfoChangeEvent event) {
        if(!mAvailableTypeSet.contains(mSourceType))return;
        if (mSourceType == MediaSourceType.SPOTIFY) {
            updateSpotifyView();
        }
    }

    /**
     * BTAudio再生情報の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBtAudioInfoChangeAction(BtAudioInfoChangeEvent event) {
        if(!mAvailableTypeSet.contains(mSourceType))return;
        if (mSourceType == MediaSourceType.BT_AUDIO) {
            updateBtAudioView();
        }
    }

    /**
     * ラジオ情報変更通知イベントハンドラ
     *
     * @param event ラジオ情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioInfoChangeEvent(RadioInfoChangeEvent event) {
        if(!mAvailableTypeSet.contains(mSourceType))return;
        if (mSourceType == MediaSourceType.RADIO) {
            updateRadioView();
        } else if (mSourceType == MediaSourceType.TI) {
            updateTiView();
        }
    }

    /**
     * HDラジオ情報変更通知イベントハンドラ
     *
     * @param event HDラジオ情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHdRadioInfoChangeEvent(HdRadioInfoChangeEvent event) {
        if(!mAvailableTypeSet.contains(mSourceType))return;
        if (mSourceType == MediaSourceType.HD_RADIO) {
            updateHdRadioView();
        }
    }

    /**
     * DAB情報変更通知イベントハンドラ
     *
     * @param event DAB情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDabInfoChangeEvent(DabInfoChangeEvent event) {
        if(!mAvailableTypeSet.contains(mSourceType))return;
        if (mSourceType == MediaSourceType.DAB) {
            updateDabView();
        }
    }

    /**
     * SXM情報変更通知イベントハンドラ
     *
     * @param event SXM情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSxmInfoChangeEvent(SxmInfoChangeEvent event) {
        if(!mAvailableTypeSet.contains(mSourceType))return;
        if (mSourceType == MediaSourceType.SIRIUS_XM) {
            updateSxmView();
        }
    }
    //TODO:各ソースの更新イベント通知を追加する

    /**
     * NotificationListenerServiceConnectedEventハンドラ.
     * <p>
     * 通知監視サービスと接続した場合に動作する。
     *
     * @param event NotificationListenerServiceConnectedEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotificationListenerServiceConnectedEvent(NotificationListenerServiceConnectedEvent event){
        updateNotification();
    }

    /**
     * ReadNotificationPostedEventハンドラ
     * <p>
     * 新規通知を受信した場合に動作する。
     *
     * @param event ReadNotificationPostedEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadNotificationPostedEvent(ReadNotificationPostedEvent event) {
        updateNotification();
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
        updateNotification();
    }

    /**
     * AdasErrorEventハンドラ
     * @param event AdasErrorEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdasErrorEvent(AdasErrorEvent event) {
        setAdasIcon();
    }

    private void setAdasIcon(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            int status = 0;
            AppStatus appStatus = mGetCase.execute().getAppStatus();
            if(appStatus.adasDetected)status = 1;
            if(appStatus.isAdasError())status = 2;
            view.setAdasIcon(status);
        });
    }

    /**
     * AdasDetectingEventハンドラ
     * @param event AdasDetectingEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdasDetectingEvent(AdasDetectingEvent event) {
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        if(appStatus.homeViewAdas){
            Optional.ofNullable(getView()).ifPresent(view ->view.setAdasDetection(mAdas.isPedestrianDetecting(),mAdas.isVehicleDetecting(),mAdas.isLeftLaneDetecting(),mAdas.isRightLaneDetecting()));
        }
    }

    /**
     * システム設定変更イベントハンドラ.
     *
     * @param event システム設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSystemSettingChangeEvent(SystemSettingChangeEvent event) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setDistanceUnit(mPreference.getDistanceUnit()));
    }

    public CarRunningStatus getCarRunningStatus() {
        return mGetCase.execute().getCarRunningStatus();
    }

    public DistanceUnit getDistanceUnit() {
        return mPreference.getDistanceUnit();
    }

    public TimeFormatSetting getTimeFormatSetting() {
        return mPreference.getTimeFormatSetting();
    }

    /**
     * AlexaNotificationChangeEvent
     * @param event AlexaNotificationChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAlexaNotificationChangeEvent(AlexaNotificationChangeEvent event) {
        updateAlexaNotification();
    }

    private void updateAlexaNotification() {
        Optional.ofNullable(getView()).ifPresent(view ->{
            AppStatus appStatus = mGetCase.execute().getAppStatus();
            boolean notificationQueued = false;
            if(mPreference.getVoiceRecognitionType()== VoiceRecognizeType.ALEXA){
                notificationQueued = mGetCase.execute().getAppStatus().alexaNotification;
            }
            view.setAlexaNotification(notificationQueued);
        });
    }
    /**
     * VoiceRecognitionTypeChangeEvent
     * @param event VoiceRecognitionTypeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVoiceRecognitionTypeChangeEvent(VoiceRecognitionTypeChangeEvent event) {
        VoiceRecognizeType type = mPreference.getVoiceRecognitionType();
        if(type==VoiceRecognizeType.PIONEER_SMART_SYNC) {
            AppStatus appStatus = mGetCase.execute().getAppStatus();
            if (appStatus.appMusicAudioMode == AudioMode.ALEXA) {
                appStatus.appMusicAudioMode = AudioMode.MEDIA;
                appStatus.playerInfoItem = null;
                mEventBus.post(new AppMusicAudioModeChangeEvent());
                AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
                if (audioManager != null) {
                    audioManager.doStop();
                }
                updateSourceView();
            }
            AmazonAlexaManager amazonAlexaManager = AmazonAlexaManager.getInstance();
            if (amazonAlexaManager != null) {
                amazonAlexaManager.doSpeechCancel();
            }
        }

        // ショートカット領域のアイコン更新(YouTubeアイコン、音声認識のアイコン)
        setShortcutIconResources();

        Optional.ofNullable(getView()).ifPresent(view ->{
            view.setShortCutButtonEnabled(mShortCutKeyEnabledStatus.isShortCutKeyEnabled());
            view.setShortcutKeyItems(mShortCutKeyList);
            if (!mGetCase.execute().getCarDeviceStatus().androidVrEnabled) {
                if (type == VoiceRecognizeType.ALEXA) {
                    view.displayVoiceMessage(mContext.getString(type.label));
                }
            } else {
                view.displayVoiceMessage(mContext.getString(type.label));
            }
        });
        updateAlexaNotification();
    }

    /**
     * AppMusicAudioModeChangeEventハンドラ
     * @param event AppMusicAudioModeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppMusicAudioModeChangeEvent(AppMusicAudioModeChangeEvent event) {
        updateSourceView();
    }

    /**
     * AlexaRenderPlayerInfoUpdateEventハンドラ
     * @param event AlexaRenderPlayerInfoUpdateEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAlexaRenderPlayerInfoUpdateEvent(AlexaRenderPlayerInfoUpdateEvent event) {
        updateAndroidMusicView();
    }

    /**
     * 車載機との連携開始が完了したときに実行されるイベント
     * YouTubeLinkが車載機のClassId、仕向けに依存するために追加
     * ショートカットのソースアイコンの切り替え、表示設定の更新を行う
     * @param ev SessionCompletedEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSessionCompletedEvent(SessionCompletedEvent ev){
        Timber.i("onSessionCompletedEvent YouTubeLinkIcon");
        // ショートカット領域のアイコン更新(YouTubeアイコン、音声認識のアイコン)
        setShortcutIconResources();

        // ショートカット領域の表示状態をセット
        Optional.ofNullable(getView()).ifPresent(view ->{
            view.setShortcutKeyItems(mShortCutKeyList);
            view.setShortCutButtonEnabled(mShortCutKeyEnabledStatus.isShortCutKeyEnabled());
        });

    }

    /**
     * ショートカット領域のアイコンの切り替え
     * カスタムキーアイコン or YouTubeLinkアイコン
     * デフォルトの音声認識アイコン or 音声認識Alexaアイコン
     */
    private void setShortcutIconResources(){
        for (int i = 0; i < mShortCutKeyList.size(); i++) {
            ShortcutKeyItem item = mShortCutKeyList.get(i);
            if(item.key == ShortcutKey.SOURCE){
                // TODO カスタムキー/YouTubeLinkアイコン切り替え 改善必要
                if(mYouTubeLinkStatus.isYouTubeLinkEnabled()){
                    item.imageResource = YOUTUBE_LINK_ICON;
                }
                else {
                    item.imageResource = CUSTOM_KEY_ICON;
                }
            }
            if(item.key==ShortcutKey.VOICE){
                if(mPreference.getVoiceRecognitionType()== VoiceRecognizeType.ALEXA){
                    item.imageResource = R.drawable.p0167_alexabtn_1nrm;
                }else{
                    item.imageResource = R.drawable.p0162_vrbtn_1nrm;
                }
            }
            item.enabled = true;
        }
    }
}
