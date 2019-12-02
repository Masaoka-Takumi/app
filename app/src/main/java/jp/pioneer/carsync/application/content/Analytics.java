package jp.pioneer.carsync.application.content;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import jp.pioneer.carsync.domain.model.CarDeviceSpec;

public class Analytics {

    enum AnalyticsEvent {
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

    enum AnalyticsParam {
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

    enum AnalyticsSourceChangeReason {
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
        thirdAppChange,//3rdApp切り換えでのAppMusicソース切り替え
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

    //Singleton
    private static final Analytics sAnalytics = new Analytics();
    private static AnalyticsToolStrategy sStrategy;
    private static String sDeviceName;
    private static String sDeviceDivision;

    public static Analytics getInstance() {
        return sAnalytics;
    }

    private Analytics() {
    }

    // アプリ起動時に呼び出す
    public void init(AnalyticsToolStrategy strategy) {
        sStrategy = strategy;
    }

    // EULA/PrivacyPolicy同意済み or 同意したら呼び出す
    public void startSession(Context context) {
        sStrategy.startSession(context);
    }

    /**
     * 連携情報イベントの送信
     */
    public void logDeviceConnectedEvent(CarDeviceSpec carDevice) {
        Date now = new Date();
        String timestamp;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timestamp = dateFormat.format(now);
        sDeviceName = carDevice.modelName == null ? "" : carDevice.modelName;
        sDeviceDivision = String.format("0x%02X", carDevice.carDeviceDestinationInfo.code);
        sStrategy.createEventSubmitter(AnalyticsEvent.deviceConnected)
                .with(AnalyticsParam.timestamp, timestamp)
                .with(AnalyticsParam.accessoryId, String.format("0x%04X", carDevice.accessoryId))
                .with(AnalyticsParam.deviceName, sDeviceName)
                .with(AnalyticsParam.deviceDivision, sDeviceDivision)
                .submit();
    }

    private AnalyticsEventSubmitter createEventSubmitterWithCommonEventParam(AnalyticsEvent event) {
        return sStrategy.createEventSubmitter(event)
                .with(AnalyticsParam.deviceName, sDeviceName)
                .with(AnalyticsParam.deviceDivision, sDeviceDivision);
    }

    public void logActiveSourceEvent(AnalyticsSource source, long duration) {
        createEventSubmitterWithCommonEventParam(AnalyticsEvent.activeSource)
                .with(AnalyticsParam.source, source.value)
                .with(AnalyticsParam.duration, duration)
                .submit();
    }

    public void logUIOrientationEvent(AnalyticsUIOrientation orientation, long duration) {
        createEventSubmitterWithCommonEventParam(AnalyticsEvent.uiOrientation)
                .with(AnalyticsParam.orientation, orientation.value)
                .with(AnalyticsParam.duration, duration)
                .submit();
    }

    public void logSourceSelectReasonEvent(AnalyticsSourceChangeReason reason) {
        createEventSubmitterWithCommonEventParam(AnalyticsEvent.sourceSelectReason)
                .with(AnalyticsParam.trigger, reason.value)
                .submit();
    }

    public void logActiveScreenEvent(AnalyticsActiveScreen screen, long duration) {
        createEventSubmitterWithCommonEventParam(AnalyticsEvent.activeScreen)
                .with(AnalyticsParam.screen, screen.value)
                .with(AnalyticsParam.duration, duration)
                .submit();
    }

    public void logShortcutActionEvent(AnalyticsShortcutAction action,AnalyticsActiveScreen screen) {
        createEventSubmitterWithCommonEventParam(AnalyticsEvent.shortcutAction)
                .with(AnalyticsParam.action, action.value)
                .with(AnalyticsParam.screen, screen.value)
                .submit();
    }

    public void logThirdAppStartUpEvent(AnalyticsThirdAppStartUp startUp) {
        createEventSubmitterWithCommonEventParam(AnalyticsEvent.thirdAppStartUp)
                .with(AnalyticsParam.trigger, startUp.value)
                .submit();
    }
}
