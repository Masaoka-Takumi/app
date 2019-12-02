package jp.pioneer.carsync.application.content;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import javax.inject.Inject;

import jp.pioneer.carsync.application.event.AppStateChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicAudioModeChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.SessionStatus;
import timber.log.Timber;

public class AnalyticsEventManager {
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    private static final Analytics sAnalytics = Analytics.getInstance();
    //視聴ソース情報
    private static EnumMap<Analytics.AnalyticsSource, Long> sSourceActiveDuration = new EnumMap<Analytics.AnalyticsSource, Long>(Analytics.AnalyticsSource.class);
    private static Analytics.AnalyticsSource sLastSource = null;
    private static long sLastSourceStartTime;//ms
    //スマホ端末の縦/横割合(連携中)情報
    private static EnumMap<Analytics.AnalyticsUIOrientation, Long> sUIOrientationDuration = new EnumMap<Analytics.AnalyticsUIOrientation, Long>(Analytics.AnalyticsUIOrientation.class);
    private static Analytics.AnalyticsUIOrientation sLastUIOrientation = null;
    private static long sLastUIOrientationTime;//ms
    //HOME画面/AV画面/バックグラウンドの滞留時間情報
    private static EnumMap<Analytics.AnalyticsActiveScreen, Long> sActiveScreenDuration = new EnumMap<Analytics.AnalyticsActiveScreen, Long>(Analytics.AnalyticsActiveScreen.class);
    private static Analytics.AnalyticsActiveScreen sLastActiveScreen = null;
    private static Analytics.AnalyticsActiveScreen sLastForegroundScreen = null;
    private static long sLastActiveScreenTime;//ms

    private static EnumSet<Analytics.AnalyticsThirdAppStartUp> sThirdAppStartUpSendFlg = EnumSet.noneOf(Analytics.AnalyticsThirdAppStartUp.class);//3rd App起動トリガー送信済フラグ
    private static final Handler sHandler = new Handler();
    private static Runnable sRunnable;
    private static Analytics.SourceChangeReason sSourceChangeReason;//ソース切り替え操作トリガー
    private static MediaSourceType sLastSourceTypeTrigger;//ソース切り替え操作トリガー用保存ソース
    private static MediaSourceType sLastSourceType;//ソース変更時保存ソース

    /**
     * コンストラクタ.
     */
    @Inject
    private AnalyticsEventManager(){
    }

    // アプリ起動時に呼び出す
    public static void configure(AnalyticsToolStrategy strategy) {
        sAnalytics.init(strategy);
    }

    // EULA/PrivacyPolicy同意済み or 同意したら呼び出す
    public static void startSession(Context context) {
        sAnalytics.startSession(context);
        init();
    }

    public Analytics.AnalyticsActiveScreen getActiveScreen() {
        return sLastActiveScreen;
    }

    private Analytics.AnalyticsActiveScreen getLastForegroundScreen() {
        return sLastForegroundScreen;
    }

    public void setSourceSelectReason(Analytics.SourceChangeReason reason) {
        sSourceChangeReason = reason;
    }

    public void startAnalytics(CarDeviceSpec carDevice){
        init();

        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        sAnalytics.logDeviceConnectedEvent(carDevice);
        startActiveSourceDuration(mGetStatusHolder.execute().getCarDeviceStatus().sourceType);
        Configuration config = mContext.getResources().getConfiguration();
        startUIOrientationDuration(config.orientation,true);
        startActiveScreenDuration(Analytics.AnalyticsActiveScreen.home_screen,true);
        sLastSourceType = mGetStatusHolder.execute().getCarDeviceStatus().sourceType;
        sLastSourceTypeTrigger = mGetStatusHolder.execute().getCarDeviceStatus().sourceType;;
    }

    public void finishAnalytics(){
        startSourceDuration(null);
        startUIOrientationDuration(0, false);
        startActiveScreenDuration(sLastActiveScreen, false);
        mEventBus.unregister(this);
    }

    public void sendFinishLogEvent(){
        //FlurryのSessionが終了しているとログを送れない(MainActivityがバックグラウンド中はセッションが終了している)
        sendActiveSourceEvent();
        sendUIOrientationEvent();
        sendActiveScreenEvent();
    }

    private static void init() {
        sLastSource = null;
        sLastSourceStartTime = 0;
        sSourceActiveDuration = new EnumMap<Analytics.AnalyticsSource, Long>(Analytics.AnalyticsSource.class);
        sLastUIOrientation = null;
        sLastUIOrientationTime = 0;
        sUIOrientationDuration = new EnumMap<Analytics.AnalyticsUIOrientation, Long>(Analytics.AnalyticsUIOrientation.class);
        sLastActiveScreen = null;
        sLastActiveScreenTime = 0;
        sActiveScreenDuration = new EnumMap<Analytics.AnalyticsActiveScreen, Long>(Analytics.AnalyticsActiveScreen.class);
        sThirdAppStartUpSendFlg = EnumSet.noneOf(Analytics.AnalyticsThirdAppStartUp.class);
        sSourceChangeReason = null;
        sLastSourceType = null;
        sLastSourceTypeTrigger = null;
    }

    /**
     * ソース視聴時間計測開始
     */
    public void startActiveSourceDuration(MediaSourceType sourceType) {
        Analytics.AnalyticsSource analyticsSource;
        Timber.d("startSourceDuration:sourceType=" + sourceType + ",sSourceChangeReason=" + sSourceChangeReason);
        if (sSourceChangeReason != null) {
            if (sSourceChangeReason == Analytics.SourceChangeReason.temporarySourceChange) {
                startSourceDuration(null);
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
                startSourceDuration(null);
                return;
        }
        startSourceDuration(analyticsSource);
    }

    private void startSourceDuration(Analytics.AnalyticsSource analyticsSource) {
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
    private void sendActiveSourceEvent() {
        startSourceDuration(null);
        for (Map.Entry<Analytics.AnalyticsSource, Long> entry : sSourceActiveDuration.entrySet()) {
            Timber.d("sendActiveSourceEvent:" + entry.getKey() + " : " + entry.getValue() + "sec");
            long durationMinute = entry.getValue() / 60;
            if (durationMinute > 0) {
                sAnalytics.logActiveSourceEvent(entry.getKey(), durationMinute);
            }
        }
        sLastSource = null;
        sLastSourceStartTime = 0;
    }

    /**
     * スマホ端末の縦/横割合(連携中)情報計測開始
     */
    public void startUIOrientationDuration(int orientation, boolean foreground) {
        Analytics.AnalyticsUIOrientation uiOrientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            uiOrientation = Analytics.AnalyticsUIOrientation.portrait;
        } else {
            uiOrientation = Analytics.AnalyticsUIOrientation.landscape;
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
    private void sendUIOrientationEvent() {
        startUIOrientationDuration(0, false);
        for (Map.Entry<Analytics.AnalyticsUIOrientation, Long> entry : sUIOrientationDuration.entrySet()) {
            Timber.d("sendUIOrientationEvent:" + entry.getKey() + " : " + entry.getValue() + "sec");
            long durationMinute = entry.getValue() / 60;
            if (durationMinute > 0) {
                sAnalytics.logUIOrientationEvent(entry.getKey(), durationMinute);
            }
        }
        sLastUIOrientation = null;
        sLastUIOrientationTime = 0;
    }

    /**
     * ショートカット操作情報イベント送信
     */
    public void sendShortCutActionEvent(Analytics.AnalyticsShortcutAction action, Analytics.AnalyticsActiveScreen screen) {
        sAnalytics.logShortcutActionEvent(action,screen);
    }

    /**
     * ソース切り替え操作トリガー情報イベント送信
     */
    public void sendSourceSelectReasonEvent(MediaSourceType sourceType) {
        Timber.d("sendSourceSelectReasonEvent:sourceType=" + sourceType);
        sHandler.removeCallbacks(sRunnable);
        if (sourceType == MediaSourceType.BT_PHONE || sourceType == MediaSourceType.IPOD
                || sourceType == MediaSourceType.DAB_INTERRUPT || sourceType == MediaSourceType.HD_RADIO_INTERRUPT
                || sourceType == MediaSourceType.TTS) {
            sSourceChangeReason = null;
            return;
        }
        if (sourceType != sLastSourceTypeTrigger) {
            final Analytics.SourceChangeReason reason = sSourceChangeReason;
            sSourceChangeReason = null;
            sRunnable = new Runnable() {
                @Override
                public void run() {
                    Timber.d("sendSourceSelectReasonEvent:sSourceChangeReason=" + reason + ",sourceType=" + sourceType + ",newSourceType=" + mGetStatusHolder.execute().getCarDeviceStatus().sourceType);
                    if (mGetStatusHolder.execute().getCarDeviceStatus().sourceType == sourceType) {
                        if (reason == null) {
                            sAnalytics.logSourceSelectReasonEvent(Analytics.AnalyticsSourceChangeReason.carDeviceKey);
                        } else if (reason == Analytics.SourceChangeReason.appCustomKey) {
                            sAnalytics.logSourceSelectReasonEvent(Analytics.AnalyticsSourceChangeReason.appCustomKey);
                        }
                    }
                }
            };
            if (reason == null || reason == Analytics.SourceChangeReason.appCustomKey) {
                //トグル切換のため、ソースを切り替えてから10秒固定されたらソース切り替えされたとする
                sHandler.postDelayed(sRunnable, 10000);//10s
            } else if (reason == Analytics.SourceChangeReason.appSourceList) {
                sAnalytics.logSourceSelectReasonEvent(Analytics.AnalyticsSourceChangeReason.appSourceList);
            }
            sLastSourceTypeTrigger = sourceType;
        };
    }

    /**
     * HOME画面/AV画面/バックグラウンドの滞留時間計測開始
     */
    public void startActiveScreenDuration(Analytics.AnalyticsActiveScreen activeScreen, boolean start) {
        if (activeScreen == null) {
            Timber.d("startActiveScreenDuration:activeScreen is Null" + ",start=" + start);
            sLastForegroundScreen = null;
            sLastActiveScreen = null;
            sLastActiveScreenTime = 0;
            return;
        }
        if(activeScreen== Analytics.AnalyticsActiveScreen.home_screen||activeScreen== Analytics.AnalyticsActiveScreen.av_screen){
            sLastForegroundScreen = activeScreen;
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
    private void sendActiveScreenEvent() {
        Timber.d("sendActiveScreenEvent");
        startActiveScreenDuration(sLastActiveScreen, false);
        for (Map.Entry<Analytics.AnalyticsActiveScreen, Long> entry : sActiveScreenDuration.entrySet()) {
            Timber.d("sendActiveScreenEvent:" + entry.getKey() + " : " + entry.getValue());
            long durationMinute = entry.getValue() / 60;
            if (durationMinute > 0) {
                sAnalytics.logActiveScreenEvent(entry.getKey(), durationMinute);
            }
        }
        sLastActiveScreen = null;
        sLastActiveScreenTime = 0;
    }

    /**
     * 3rd App起動トリガーイベント送信
     */
    public void sendThirdAppStartUpEvent(Analytics.AnalyticsThirdAppStartUp startUp) {
        //トリガー毎に1度だけ収集
        if(!sThirdAppStartUpSendFlg.contains(startUp)){
            sThirdAppStartUpSendFlg.add(startUp);
            sAnalytics.logThirdAppStartUpEvent(startUp);
        }
    }

    /**
     * アプリケーション状態変更イベントハンドラ.
     *
     * @param ev アプリケーション状態イベント
     */
    @Subscribe
    public void onAppStateChangedEvent(AppStateChangeEvent ev) {
        //Timber.d("onAppStateChangedEvent:ev.appState="+ev.appState);
        if(ev.appState == AppStateChangeEvent.AppState.STARTED) {
            //アプリがフォアグラウンド
            if (mGetStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED&&mGetStatusHolder.execute().getAppStatus().isAgreedCaution) {
                startActiveScreenDuration(Analytics.AnalyticsActiveScreen.background, false);
                //Pause前の表示画面がAV画面/HOME画面であれば計測再開
                startActiveScreenDuration(getLastForegroundScreen(), true);
                Configuration config = mContext.getResources().getConfiguration();
                startUIOrientationDuration(config.orientation, true);
            }
        }else if(ev.appState == AppStateChangeEvent.AppState.STOPPED){
            //アプリがバックグラウンド
            if(mGetStatusHolder.execute().getSessionStatus()==SessionStatus.STARTED&&mGetStatusHolder.execute().getAppStatus().isAgreedCaution) {
                startActiveScreenDuration(getActiveScreen(),false);
                startActiveScreenDuration(Analytics.AnalyticsActiveScreen.background,true);
                Configuration config = mContext.getResources().getConfiguration();
                startUIOrientationDuration(config.orientation,false);
            }
        }
    }

    /**
     * ソース種別変更イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe
    public void onMediaSourceTypeChangeAction(MediaSourceTypeChangeEvent event) {
        //Timber.d("onMediaSourceTypeChangeAction");
        if (mGetStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED && mGetStatusHolder.execute().getAppStatus().isAgreedCaution) {
            CarDeviceStatus status = mGetStatusHolder.execute().getCarDeviceStatus();
            if (status.sourceType != sLastSourceType) {
                // ラストソースと異なるソース変更後
                startActiveSourceDuration(status.sourceType);
                sendSourceSelectReasonEvent(status.sourceType);
                sLastSourceType = status.sourceType;
            }
        }
    }

    /**
     * AppMusicAudioModeChangeEventハンドラ
     * @param event AppMusicAudioModeChangeEvent
     */
    @Subscribe
    public void onAppMusicAudioModeChangeEvent(AppMusicAudioModeChangeEvent event) {
        Timber.d("onAppMusicAudioModeChangeEvent:sLastSourceType="+sLastSourceType +",sourceType ="+mGetStatusHolder.execute().getCarDeviceStatus().sourceType+",mode="+mGetStatusHolder.execute().getAppStatus().appMusicAudioMode);
        if(sLastSourceType==MediaSourceType.APP_MUSIC&&mGetStatusHolder.execute().getCarDeviceStatus().sourceType==MediaSourceType.APP_MUSIC){
            startActiveSourceDuration(MediaSourceType.APP_MUSIC);
        }
    }
}
