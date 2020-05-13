package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.EnumSet;
import java.util.Set;

import jp.pioneer.carsync.presentation.presenter.RadioTabContainerPresenter;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AdasCalibrationSettingFragment;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderTemplateItem;

/**
 * 99App状態.
 */
public class AppStatus {
    /** 選択中楽曲リスト種類. */
    public MusicCategory musicCategory;
    /** ホーム画面遷移済みか否か. */
    public boolean isTransitionedHomeScreen;
    /** Cautionダイアログの確認をしたか否か. */
    public boolean isAgreedCaution;
    /** サードパーティ音楽アプリを起動したか否か. */
    public boolean isLaunchedThirdPartyAudioApp;
    /** 99Appソース FormatRead中か否か. */
    public boolean isFormatRead;
    /** List情報取得中か否か. */
    public boolean isRunningListTask;
    /** Session Error Type. */
    public SessionErrorType errorType;
    /** 通知監視サービスと接続済か否か. */
    public boolean isConnectNotificationListenerService;
    /** 現在のLocaleがTTSに対応しているか否か. */
    public boolean isTtsSupportedCurrentLocale;
    /** 発生しているADASのエラー. */
    public Set<AdasErrorType> adasErrorList;
    /** ADAS検知中. */
    public boolean adasDetected;
    /** ADAS警告群. */
    public Set<AdasWarningEvent> adasWarningEvents;
    /** ADASキャリブレーション中 */
    public boolean  isAdasCalibrationSetting;
    /** HOME画面右領域にADAS検知中を表示する */
    public boolean  homeViewAdas;
    /** HOME画面中央領域にADAS情報を表示する */
    public boolean  homeCenterViewAdas;
    /** ADAS用車速設定（km/h） */
    public int adasCarSpeed;
    /** ADAS LDW最小動作速度（km/h） */
    public int adasLdwMinSpeed;
    /** ADAS LDW最大動作速度（km/h） */
    public int adasLdwMaxSpeed;
    /** ADAS PCW最小動作速度（km/h） */
    public int adasPcwMinSpeed;
    /** ADAS PCW最大動作速度（km/h） */
    public int adasPcwMaxSpeed;
    /** ADAS FCW最小動作速度（km/h） */
    public int adasFcwMinSpeed;
    /** ADAS FCW最大動作速度（km/h） */
    public int adasFcwMaxSpeed;
    /** ADAS キャリブレーション加速度Y最大値(絶対値)（G） */
    public float adasAccelerateYRange;
    /** ADAS キャリブレーション加速度Z最小値（G） */
    public float adasAccelerateZRangeMin;
    /** ADAS キャリブレーション加速度Z最大値（G） */
    public float adasAccelerateZRangeMax;
    /** ADAS FPS設定 */
    public int adasFps;
    /** ADAS Sim判定 */
    public boolean adasSimJudgement;
    /** Adas課金状態チェック済 */
    public boolean adasBillingCheck;
    /** 課金状態 */
    public boolean adasPurchased;
    /** Adas価格 */
    public String adasPrice;
    /** 音声認識開始遅延時間 */
    public int speechRecognizerDelayTime;
    /** 走行状態取得 */
    public boolean getRunningStatus;
    /** ADAS対応国 */
    public boolean isAdasAvailableCountry;
    /** ADASお試し期間中 */
    public boolean adasTrial;
    /** ADASカメラ表示 */
    public boolean adasCameraView;
    /** Alexa認証済 */
    public boolean alexaAuthenticated;
    /** AudioMode MEDIA/ALEXA */
    public AudioMode appMusicAudioMode;
    public RenderPlayerInfoItem playerInfoItem;
    public int alexaAudioPlayPosition;
    public int alexaAudioPlayDuration;
    /** Alexa通知有無 */
    public boolean alexaNotification;
    /** Alexa画面表示中 */
    public boolean isShowAlexaDialog;
    /** Alexa画面表示前のソース */
    public MediaSourceType alexaPreviousSourceType;
    /** Alexa対応国 */
    public boolean isAlexaAvailableCountry;
    /** 音声認識画面表示中 */
    public boolean isShowSpeechRecognizerDialog;
    /** 連携の抑制状態 */
    public boolean deviceConnectionSuppress;
    /** カスタムキー[ソースON/OFF]時の直前のソース*/
    public MediaSourceType lastDirectSource;
    /** カスタムキー[ソースON/OFF]の実行時刻 */
    public long lastSourceOnTime;
    /** YouTubeLinkWebView画面遷移前のソース */
    public MediaSourceType lastSourceBeforeYouTubeLink;
    /** YouTubeLinkWebView画面表示中 */
    public boolean isShowYouTubeLinkWebView;
    /** RadioList表示中 */
    public boolean isShowRadioTabContainer;
    public AppStatus() {
        reset();
        isLaunchedThirdPartyAudioApp = false;
        adasBillingCheck = false;
		adasPurchased = false;
        adasPrice = "";
        adasCarSpeed = 0;
        adasLdwMinSpeed = 30;
        adasLdwMaxSpeed = 251;
        adasPcwMinSpeed = 1;
        adasPcwMaxSpeed = 30;
        adasFcwMinSpeed = 5;
        adasFcwMaxSpeed = 40;
        adasAccelerateYRange = AdasCalibrationSettingFragment.ACCELERATE_Y_OK_RANGE_MAX;
        adasAccelerateZRangeMin = AdasCalibrationSettingFragment.ACCELERATE_Z_OK_RANGE_MIN;
        adasAccelerateZRangeMax = AdasCalibrationSettingFragment.ACCELERATE_Z_OK_RANGE_MAX;
        adasFps = 30;
        speechRecognizerDelayTime = 0;
        adasSimJudgement = true;
        isAdasAvailableCountry = false;
        adasTrial = false;
        homeViewAdas = false;
        homeCenterViewAdas =false;
        adasCameraView = false;
        alexaAuthenticated=false;
        isAlexaAvailableCountry = false;
        deviceConnectionSuppress = false;
        lastDirectSource = null;
        lastSourceOnTime = 0L;
        lastSourceBeforeYouTubeLink = null;
    }

    /**
     * リセット.
     */
    public void reset() {
        musicCategory = MusicCategory.ARTIST;
        isTransitionedHomeScreen = false;
        isAgreedCaution = false;
        isFormatRead = true;
        isRunningListTask = false;
        adasErrorList = EnumSet.noneOf(AdasErrorType.class);
        getRunningStatus = false;
        adasDetected = false;
        adasWarningEvents = EnumSet.noneOf(AdasWarningEvent.class);
        appMusicAudioMode = AudioMode.MEDIA;
		playerInfoItem = null;
        alexaNotification = false;
        isShowAlexaDialog = false;
        isShowSpeechRecognizerDialog = false;
        alexaAudioPlayPosition = 0;
        alexaAudioPlayDuration = 0;
        alexaPreviousSourceType = MediaSourceType.APP_MUSIC;
        lastDirectSource = null;
        lastSourceOnTime = 0L;
        lastSourceBeforeYouTubeLink = null;
        isShowYouTubeLinkWebView = false;
        isShowRadioTabContainer = false;
    }
	/**
     * ADAS警告状態取得.
     * <p>
     * 現在発生しているADASの警告イベントから警告状態を取得する
     * 車線逸脱の場合の警告状態は連続
     * 歩行者衝突予測、前方衝突予測の警告状態は単発
     * 発生していない場合は警告なし
     *
     * @return ADAS警告状態
     */
    public AdasWarningStatus getAdasWarningStatus(){
        int warningQuantity = adasWarningEvents.size();

        if (warningQuantity > 0) {
            if (adasWarningEvents.contains(AdasWarningEvent.PEDESTRIAN_WARNING_EVENT) || adasWarningEvents.contains(AdasWarningEvent.PEDESTRIAN_CAREFUL_EVENT)
                    || adasWarningEvents.contains(AdasWarningEvent.FORWARD_TTC_COLLISION_EVENT) || adasWarningEvents.contains(AdasWarningEvent.FORWARD_HEADWAY_COLLISION_EVENT)) {
                //歩行者衝突予測、前方衝突予測
                return AdasWarningStatus.SINGLE;
            } else if (adasWarningEvents.contains(AdasWarningEvent.OFF_ROAD_LEFT_SOLID_EVENT) || adasWarningEvents.contains(AdasWarningEvent.OFF_ROAD_LEFT_DASH_EVENT)
                    || adasWarningEvents.contains(AdasWarningEvent.OFF_ROAD_RIGHT_SOLID_EVENT) || adasWarningEvents.contains(AdasWarningEvent.OFF_ROAD_RIGHT_DASH_EVENT)) {
                //車線逸脱
                return AdasWarningStatus.CONTINUOUS;
            }
            return AdasWarningStatus.SINGLE;
        } else {
            return AdasWarningStatus.NONE;
        }
    }
    /**
     * ADASエラー発生状態取得.
     *
     * @return ADASエラー発生状態
     */
    public boolean isAdasError(){
        return adasErrorList.size() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("musicCategory", musicCategory)
                .add("isTransitionedHomeScreen", isTransitionedHomeScreen)
                .add("isAgreedCaution", isAgreedCaution)
                .add("isLaunchedThirdPartyAudioApp", isLaunchedThirdPartyAudioApp)
                .add("isFormatRead", isFormatRead)
                .add("isRunningListTask", isRunningListTask)
                .add("adasErrorList", adasErrorList)
                .add("isAdasCalibrationSetting", isAdasCalibrationSetting)
                .add("adasCarSpeed", adasCarSpeed)
                .add("adasPurchased", adasPurchased)
                .add("adasPrice", adasPrice)
                .add("getRunningStatus", getRunningStatus)                
				.add("alexaAuthenticated", alexaAuthenticated)
                .add("appMusicAudioMode", appMusicAudioMode)
                .add("alexaNotification", alexaNotification)
                .add("lastDirectSource", lastDirectSource)
                .add("lastSourceOnTime", lastSourceOnTime)
                .add("lastSourceBeforeYouTubeLink", lastSourceBeforeYouTubeLink)
                .toString();
    }
}
