package jp.pioneer.carsync.application.content;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

public class Analytics {

    public enum AnalyticsEvent {
        deviceConnected("EventUser_DeviceConnected"),
        activeSource("EventUser_ActiveSource"),
        uiOrientation("EventUser_UIOrientation"),
        shortcutAction("EventUser_ShortcutAction"),
        activeScreen("EventUser_ActiveScreen"),
        sourceSelectReason("EventUser_SourceSelectAction"),
        thirdAppStartUp("EventUser_ThirdAppStartUp"),
        ;
        public final String name;

        AnalyticsEvent(String name) {
            this.name = name;
        }
    }

    public enum AnalyticsParam {
        timestamp("timestamp"),
        accessoryId("accessoryId"),
        deviceName("deviceName"),
        deviceDivision("deviceDivision"),
        source("source"),
        duration("duration"),
        orientation("orientation"),
        trigger("trigger"),
        screen("screen"),
        action("action"),
        ;
        public final String name;

        AnalyticsParam(String name) {
            this.name = name;
        }
    }

    enum AnalyticsSource {
        appMusic("App Music"),
        alexa("App Music(Alexa)"),
        btAudio("BT Audio"),
        radio("Radio"),
        dab("DAB"),
        spotify("Spotify"),
        usb("USB"),
        aux("AUX"),
        cd("CD"),
        hdRadio("HD Radio"),
        sourceOff("Source OFF"),
        siriusXM("Sirius XM"),
        pandora("Pandora"),
        ti("Traffic Information"),
        ;
        public final String value;

        AnalyticsSource(String value) {
            this.value = value;
        }
    }

    public enum AnalyticsShortcutAction {
        navi("Navi"),
        customShort("Custom(短押し)"),
        alexaShort("Alexa(短押し)"),
        youTubeLinkShort("YouTubeLink(短押し)"),
        customLong("Custom(長押し)"),
        phoneShort("Phone(短押し)"),
        message("Message"),
        voiceShort("Voice(短押し)"),
        youtubeLinkLong("YouTubeLink(長押し)"),
        phoneLong("Phone(長押し)"),
        voiceLong("Voice(長押し)"),
        alexaLong("Alexa(長押し)"),
        ;
        public final String value;

        AnalyticsShortcutAction(String value) {
            this.value = value;
        }
    }

    public enum AnalyticsSourceChangeReason {
        carDeviceKey("H/Uキーソーストグル"),
        appCustomKey("Customキーソーストグル"),
        appSourceList("ソース一覧から選択"),
        ;
        public final String value;

        AnalyticsSourceChangeReason(String value) {
            this.value = value;
        }
    }

    public enum SourceChangeReason {
        appCustomKey,//Customキーソーストグル
        appCustomKeyDirectSource,//Customキーダイレクトソース
        getAppCustomKeySourceOnOff,//CustomキーソースOnOff
        appSourceList,//ソース一覧から選択
        temporarySourceChange,//一時的な切り替え(音声認識、YouTubeLinkでのAppMusicソース切り替え)
        temporarySourceChangeBack,//・一時的な切り替えの解除(音声認識、YouTubeLink実行前のソースに戻す)
        speechRecognizeSourceChange,//音声認識発話でのソース遷移
        alexaStart,//Alexa開始でのAppMusicソース切り替え
        alexaEnd,//Alexa終了での実行前のソースに戻す
    }

    public enum AnalyticsActiveScreen {
        background("バックグラウンド"),
        av_screen("AV画面"),
        home_screen("HOME画面"),
        ;
        public final String value;

        AnalyticsActiveScreen(String value) {
            this.value = value;
        }

    }

    enum AnalyticsUIOrientation {
        portrait("縦"),
        landscape("横"),
        ;
        public final String value;

        AnalyticsUIOrientation(String value) {
            this.value = value;
        }

    }

    public enum AnalyticsThirdAppStartUp {
        appSourceList("ソース一覧から起動"),
        appCustomKey("Customキーから起動"),
        ;
        public final String value;

        AnalyticsThirdAppStartUp(String value) {
            this.value = value;
        }

    }

    @Inject GetStatusHolder mGetStatusHolder;
    private static String sDeviceName;
    private static String sDeviceDivision;
    private static Map<AnalyticsSource, Long> sSourceActiveDuration = new HashMap<>();
    private static AnalyticsSource sLastSource = null;
    private static long sLastSourceStartTime;//ms
    private static Map<AnalyticsUIOrientation, Long> sUIOrientationDuration = new HashMap<>();
    private static AnalyticsUIOrientation sLastUIOrientation = null;
    private static long sLastUIOrientationTime;//ms
    private static boolean sThirdAppStartUpSendFlgAppSourceList = false;
    private static boolean sThirdAppStartUpSendFlgAppCustomKey = false;
    private static Map<AnalyticsActiveScreen, Long> sActiveScreenDuration = new HashMap<>();
    private static AnalyticsActiveScreen sLastActiveScreen = null;
    private static long sLastActiveScreenTime;//ms
    private static final Handler mHandler = new Handler();
    private static Runnable sRunnable;
    private static SourceChangeReason sSourceChangeReason;
    private static MediaSourceType sLastSourceType;

    /**
     * コンストラクタ.
     */
    @Inject
    public Analytics() {

    }

    // EULA/PrivacyPolicy同意済み or 同意したら呼び出す
    public static void startSession(Context context) {
        Flurry.sessionStart(context);
        init();
    }

    public static AnalyticsActiveScreen getActiveScreen() {
        return sLastActiveScreen;
    }

    public void setSourceSelectReason(SourceChangeReason reason) {
        sSourceChangeReason = reason;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //ソース変更できなかった場合のトリガークリア
                Timber.d("sSourceChangeReason Clear " + sSourceChangeReason);
                sSourceChangeReason = null;
            }
        }, 1000);
    }

    /**
     * 連携情報イベントの送信
     */
    public void logDeviceConnectedEvent(CarDeviceSpec carDevice) {
        init();

        Date now = new Date();
        String timestamp;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timestamp = dateFormat.format(now);
        sDeviceName = carDevice.modelName == null ? "" : carDevice.modelName;
        sDeviceDivision = String.format("0x%02X", carDevice.carDeviceDestinationInfo.code);
        Flurry.createEventSubmitter(AnalyticsEvent.deviceConnected)
                .with(AnalyticsParam.timestamp, timestamp)
                .with(AnalyticsParam.accessoryId, String.format("0x%04X", carDevice.accessoryId))
                .with(AnalyticsParam.deviceName, sDeviceName)
                .with(AnalyticsParam.deviceDivision, sDeviceDivision)
                .submit();
    }

    private static void init() {
        sDeviceName = null;
        sDeviceDivision = null;
        sLastSource = null;
        sLastSourceStartTime = 0;
        sSourceActiveDuration = new HashMap<>();
        sLastUIOrientation = null;
        sLastUIOrientationTime = 0;
        sUIOrientationDuration = new HashMap<>();
        sLastActiveScreen = null;
        sLastActiveScreenTime = 0;
        sActiveScreenDuration = new HashMap<>();
        sThirdAppStartUpSendFlgAppSourceList = false;
        sThirdAppStartUpSendFlgAppCustomKey = false;
        sSourceChangeReason = null;
        sLastSourceType = null;
    }

    /**
     * ソース視聴時間計測開始
     */
    public void startActiveSourceDuration(MediaSourceType sourceType) {
        Analytics.AnalyticsSource analyticsSource;
        Timber.d("startSourceDuration:sourceType=" + sourceType + ",sSourceChangeReason=" + sSourceChangeReason);
        if (sSourceChangeReason != null) {
            if (sSourceChangeReason == SourceChangeReason.temporarySourceChange || sSourceChangeReason == SourceChangeReason.temporarySourceChangeBack) {
                return;
            }
        }
        switch (sourceType) {
            case RADIO:
                analyticsSource = Analytics.AnalyticsSource.radio;
                break;
            case SIRIUS_XM:
                analyticsSource = Analytics.AnalyticsSource.siriusXM;
                break;
            case USB:
                analyticsSource = Analytics.AnalyticsSource.usb;
                break;
            case CD:
                analyticsSource = Analytics.AnalyticsSource.cd;
                break;
            case BT_AUDIO:
                analyticsSource = Analytics.AnalyticsSource.btAudio;
                break;
            case APP_MUSIC:
                if (mGetStatusHolder.execute().getAppStatus().appMusicAudioMode == AudioMode.ALEXA) {
                    analyticsSource = Analytics.AnalyticsSource.alexa;
                } else {
                    analyticsSource = Analytics.AnalyticsSource.appMusic;
                }
                break;
            case PANDORA:
                analyticsSource = Analytics.AnalyticsSource.pandora;
                break;
            case SPOTIFY:
                analyticsSource = Analytics.AnalyticsSource.spotify;
                break;
            case AUX:
                analyticsSource = Analytics.AnalyticsSource.aux;
                break;
            case OFF:
                analyticsSource = Analytics.AnalyticsSource.sourceOff;
                break;
            case TI:
                analyticsSource = Analytics.AnalyticsSource.ti;
                break;
            case DAB:
                analyticsSource = Analytics.AnalyticsSource.dab;
                break;
            case HD_RADIO:
                analyticsSource = Analytics.AnalyticsSource.hdRadio;
                break;
            case BT_PHONE:
            case IPOD:
            case DAB_INTERRUPT:
            case HD_RADIO_INTERRUPT:
            case TTS:
            default:
                return;
        }
        startSourceDuration(analyticsSource);
    }

    private void startSourceDuration(AnalyticsSource analyticsSource) {
        Timber.d("startSourceDuration:analyticsSource=" + analyticsSource + " start");
        long now = System.currentTimeMillis();
        if (sSourceActiveDuration != null) {
            if (sLastSource != null && sLastSourceStartTime != 0) {
                long duration = now - sLastSourceStartTime;
                long totalDuration = 0;
                Long longTotal;
                longTotal = sSourceActiveDuration.get(sLastSource);
                if (longTotal != null) {
                    totalDuration = longTotal;
                }
                totalDuration += duration / 1000;//second
                Timber.d("startSourceDuration:sLastSource=" + sLastSource.value + ",totalDuration=" + totalDuration + "sec");
                sSourceActiveDuration.put(sLastSource, totalDuration);
            }
        }
        sLastSource = analyticsSource;
        sLastSourceStartTime = now;
    }

    /**
     * 視聴ソース情報収集イベント送信
     */
    public void sendActiveSourceEvent() {
        startSourceDuration(sLastSource);
        for (Map.Entry<AnalyticsSource, Long> entry : sSourceActiveDuration.entrySet()) {
            Timber.d("sendActiveSourceEvent:" + entry.getKey() + " : " + entry.getValue());
            int durationMinute = (int) (entry.getValue() / 60);
            if (durationMinute > 0) {
                logActiveSourceEvent(entry.getKey(), durationMinute);
            }
        }
        sLastSource = null;
        sLastSourceStartTime = 0;
    }

    /**
     * スマホ端末の縦/横割合(連携中)情報計測開始
     */
    public void startUIOrientationDuration(int orientation, boolean foreground) {
        AnalyticsUIOrientation uiOrientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            uiOrientation = AnalyticsUIOrientation.portrait;
        } else {
            uiOrientation = AnalyticsUIOrientation.landscape;
        }
        Timber.d("startUIOrientationDuration:orientation=" + uiOrientation.value + ",foreground=" + foreground);
        long now = System.currentTimeMillis();
        if (sUIOrientationDuration != null) {
            if (sLastUIOrientation != null && sLastUIOrientationTime != 0) {
                long duration = now - sLastUIOrientationTime;
                long totalDuration = 0;
                Long longTotal;
                longTotal = sUIOrientationDuration.get(sLastUIOrientation);
                if (longTotal != null) {
                    totalDuration = longTotal;
                }
                totalDuration += duration / 1000;//second
                Timber.d("startUIOrientationDuration:sLastUIOrientation=" + sLastUIOrientation.value + ",totalDuration=" + totalDuration + "sec");
                sUIOrientationDuration.put(sLastUIOrientation, totalDuration);
            }
        }
        if (foreground) {
            sLastUIOrientation = uiOrientation;
            sLastUIOrientationTime = now;
        } else {
            sLastUIOrientation = null;
            sLastUIOrientationTime = 0;
        }
    }

    /**
     * スマホ端末の縦/横割合(連携中)情報イベント送信
     */
    public void sendUIOrientationEvent() {
        startUIOrientationDuration(0, false);
        for (Map.Entry<AnalyticsUIOrientation, Long> entry : sUIOrientationDuration.entrySet()) {
            Timber.d("sendUIOrientationEvent:" + entry.getKey() + " : " + entry.getValue());
            int durationMinute = (int) (entry.getValue() / 60);
            if (durationMinute > 0) {
                logUIOrientationEvent(entry.getKey(), durationMinute);
            }
        }
        sLastUIOrientation = null;
        sLastUIOrientationTime = 0;
    }

    /**
     * ショートカット操作情報イベント送信
     */
    public void sendShortCutActionEvent(AnalyticsShortcutAction action,AnalyticsActiveScreen screen) {
        logShortcutActionEvent(action,screen);
    }

    /**
     * ソース切り替え操作トリガー情報イベント送信
     */
    public void sendSourceSelectReasonEvent(MediaSourceType sourceType) {
        Timber.d("sendSourceSelectReasonEvent:sourceType=" + sourceType);
        if (sourceType == MediaSourceType.BT_PHONE || sourceType == MediaSourceType.IPOD
                || sourceType == MediaSourceType.DAB_INTERRUPT || sourceType == MediaSourceType.HD_RADIO_INTERRUPT
                || sourceType == MediaSourceType.TTS) {
            return;
        }
        if (sourceType != sLastSourceType) {
            mHandler.removeCallbacks(sRunnable);
            final SourceChangeReason reason = sSourceChangeReason;
            sSourceChangeReason = null;
            sRunnable = new Runnable() {
                @Override
                public void run() {
                    Timber.d("sendSourceSelectReasonEvent:sSourceChangeReason=" + reason + ",sourceType=" + sourceType + ",newSourceType=" + mGetStatusHolder.execute().getCarDeviceStatus().sourceType);
                    if (mGetStatusHolder.execute().getCarDeviceStatus().sourceType == sourceType) {
                        if (reason != null) {
                            if (reason == SourceChangeReason.appCustomKey) {
                                logSourceSelectReasonEvent(AnalyticsSourceChangeReason.appCustomKey);
                            } else if (reason == SourceChangeReason.appSourceList) {
                                logSourceSelectReasonEvent(AnalyticsSourceChangeReason.appSourceList);
                            }
                        } else {
                            logSourceSelectReasonEvent(AnalyticsSourceChangeReason.carDeviceKey);
                        }
                    }
                }
            };
            //トグル切換のため、ソースを切り替えてから10秒固定されたらソース切り替えされたとする
            mHandler.postDelayed(sRunnable, 10000);//10s
            sLastSourceType = sourceType;
        }
    }

    /**
     * HOME画面/AV画面/バックグラウンドの滞留時間計測開始
     */
    public void startActiveScreenDuration(AnalyticsActiveScreen activeScreen, boolean start) {
        if (activeScreen == null) {
            Timber.d("startActiveScreenDuration:activeScreen is Null" + ",start=" + start);
            sLastActiveScreen = null;
            sLastActiveScreenTime = 0;
            return;
        }
        Timber.d("startActiveScreenDuration:activeScreen=" + activeScreen.value + ",start=" + start);
        long now = System.currentTimeMillis();
        if (sActiveScreenDuration != null) {
            if (sLastActiveScreen != null && sLastActiveScreenTime != 0) {
                long duration = now - sLastActiveScreenTime;
                long totalDuration = 0;
                Long longTotal;
                longTotal = sActiveScreenDuration.get(sLastActiveScreen);
                if (longTotal != null) {
                    totalDuration = longTotal;
                }
                totalDuration += duration / 1000;//second
                Timber.d("startActiveScreenDuration:sLastActiveScreen=" + sLastActiveScreen.value + ",totalDuration=" + totalDuration + "sec");
                sActiveScreenDuration.put(sLastActiveScreen, totalDuration);
            }
        }
        if (start) {
            sLastActiveScreen = activeScreen;
            sLastActiveScreenTime = now;
        } else {
            sLastActiveScreen = null;
            sLastActiveScreenTime = 0;
        }
    }

    /**
     * HOME画面/AV画面/バックグラウンドの滞留時間情報イベント送信
     */
    public void sendActiveScreenEvent() {
        Timber.d("sendActiveScreenEvent");
        startActiveScreenDuration(sLastActiveScreen, false);
        for (Map.Entry<AnalyticsActiveScreen, Long> entry : sActiveScreenDuration.entrySet()) {
            Timber.d("sendActiveScreenEvent:" + entry.getKey() + " : " + entry.getValue());
            int durationMinute = (int) (entry.getValue() / 60);
            if (durationMinute > 0) {
                logActiveScreenEvent(entry.getKey(), durationMinute);
            }
        }
        sLastActiveScreen = null;
        sLastActiveScreenTime = 0;
    }

    /**
     * 3rd App起動トリガーイベント送信
     */
    public void sendThirdAppStartUpEvent(AnalyticsThirdAppStartUp startUp) {
        //トリガー毎に1度だけ収集
        if (startUp == AnalyticsThirdAppStartUp.appSourceList && !sThirdAppStartUpSendFlgAppSourceList) {
            logThirdAppStartUpEvent(startUp);
            sThirdAppStartUpSendFlgAppSourceList = true;
        } else if (startUp == AnalyticsThirdAppStartUp.appCustomKey && !sThirdAppStartUpSendFlgAppCustomKey) {
            logThirdAppStartUpEvent(startUp);
            sThirdAppStartUpSendFlgAppCustomKey = true;
        }
    }

    private AnalyticsEventSubmitter createEventSubmitterWithCommonEventParam(AnalyticsEvent event) {
        return Flurry.createEventSubmitter(event)
                .with(AnalyticsParam.deviceName, sDeviceName)
                .with(AnalyticsParam.deviceDivision, sDeviceDivision);
    }

    private void logActiveSourceEvent(AnalyticsSource source, int duration) {
        this.createEventSubmitterWithCommonEventParam(AnalyticsEvent.activeSource)
                .with(AnalyticsParam.source, source.value)
                .with(AnalyticsParam.duration, duration)
                .submit();
    }

    private void logUIOrientationEvent(AnalyticsUIOrientation orientation, int duration) {
        this.createEventSubmitterWithCommonEventParam(AnalyticsEvent.uiOrientation)
                .with(AnalyticsParam.orientation, orientation.value)
                .with(AnalyticsParam.duration, duration)
                .submit();
    }

    private void logSourceSelectReasonEvent(AnalyticsSourceChangeReason reason) {
        this.createEventSubmitterWithCommonEventParam(AnalyticsEvent.sourceSelectReason)
                .with(AnalyticsParam.trigger, reason.value)
                .submit();
    }

    private void logActiveScreenEvent(AnalyticsActiveScreen screen, int duration) {
        this.createEventSubmitterWithCommonEventParam(AnalyticsEvent.activeScreen)
                .with(AnalyticsParam.screen, screen.value)
                .with(AnalyticsParam.duration, duration)
                .submit();
    }

    private void logShortcutActionEvent(AnalyticsShortcutAction action,AnalyticsActiveScreen screen) {
        this.createEventSubmitterWithCommonEventParam(AnalyticsEvent.shortcutAction)
                .with(AnalyticsParam.action, action.value)
                .with(AnalyticsParam.screen, screen.value)
                .submit();
    }

    private void logThirdAppStartUpEvent(AnalyticsThirdAppStartUp startUp) {
        this.createEventSubmitterWithCommonEventParam(AnalyticsEvent.thirdAppStartUp)
                .with(AnalyticsParam.trigger, startUp.value)
                .submit();
    }
}
