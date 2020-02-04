package jp.pioneer.carsync.application.content;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Handler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import jp.pioneer.carsync.application.event.AppStateChangeEvent;
import jp.pioneer.carsync.application.util.Stopwatch;
import jp.pioneer.carsync.domain.event.AppMusicAudioModeChangeEvent;
import jp.pioneer.carsync.domain.event.AudioSettingChangeEvent;
import jp.pioneer.carsync.domain.event.EqualizerSettingChangeEvent;
import jp.pioneer.carsync.domain.event.EqualizerTypeChangeEvent;
import jp.pioneer.carsync.domain.event.LiveSimulationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationPostedEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SuperTodorokiSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferMarinApp;
import jp.pioneer.carsync.domain.interactor.PreferMusicApp;
import jp.pioneer.carsync.domain.interactor.PreferNaviApp;
import jp.pioneer.carsync.domain.interactor.PreferReadNotification;
import jp.pioneer.carsync.domain.model.AlexaLanguageType;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.BaseApp;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MarinApp;
import jp.pioneer.carsync.domain.model.MarinAppCategory;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.MessagingApp;
import jp.pioneer.carsync.domain.model.MusicApp;
import jp.pioneer.carsync.domain.model.NaviApp;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.domain.model.SoundEffectType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSettingMode;
import jp.pioneer.carsync.presentation.event.MainNavigateEvent;
import jp.pioneer.carsync.presentation.event.MessageReadFinishedEvent;
import jp.pioneer.carsync.presentation.event.SourceChangeReasonEvent;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

public class AnalyticsEventManager {
    @Inject
    GetStatusHolder mGetStatusHolder;
    @Inject
    Context mContext;
    @Inject
    EventBus mEventBus;
    @Inject
    AppSharedPreference mPreference;
    @Inject
    AnalyticsSharedPreference mAnalyticsPreference;
    @Inject
    YouTubeLinkStatus mYouTubeLinkStatus;
    @Inject
    PreferNaviApp mNaviCase;
    @Inject
    PreferMarinApp mMarinCase;
    @Inject
    PreferReadNotification mMessagingCase;
    @Inject
    PreferMusicApp mPreferMusicApp;
    private static final Analytics sAnalytics = Analytics.getInstance();
    private static final int NAVI_APP_SIZE = 14;
    private static final int MESSAGE_APP_SIZE = 9;
    private static final int MUSIC_APP_SIZE = 46;
    private static final boolean DBG = false;
    private static List<AnalyticsEventObserver> observers = new ArrayList<>();
    private static EnumSet<Analytics.AnalyticsThirdAppStartUp> sThirdAppStartUpSendFlg = EnumSet.noneOf(Analytics.AnalyticsThirdAppStartUp.class);//3rd App起動トリガー送信済フラグ

    interface AnalyticsEventObserver {
        void didConnectDevice();

        void didDisconnectDevice();
    }

    /**
     * コンストラクタ.
     */
    @Inject
    public AnalyticsEventManager() {
    }

    // アプリ起動時に呼び出す
    public void configure(AnalyticsToolStrategy strategy) {
        sAnalytics.init(strategy);
        createObserver();
    }

    // EULA/PrivacyPolicy同意済み or 同意したら呼び出す
    public void startSession(Context context) {
        sAnalytics.startSession(context);
        init();
    }

    // Observerを追加
    private void createObserver() {
        observers.add(new ActiveSourceObserver());
        observers.add(new UIOrientationObserver());
        observers.add(new ActiveScreenObserver());
        observers.add(new SourceSelectActionObserver());
        observers.add(new NaviAppUseObserver());
        observers.add(new MessageAppUseObserver());
        observers.add(new MusicAppUseObserver());
        observers.add(new YouTubeLinkUseObserver());
        observers.add(new AlexaUseObserver());
        observers.add(new MessageObserver());
        observers.add(new FxSettingObserver());
        observers.add(new EasySoundTaSettingObserver());
    }

    public void startAnalytics(CarDeviceSpec carDevice) {
        init();

        sAnalytics.logDeviceConnectedEvent(carDevice);
        for (AnalyticsEventObserver observer : observers) {
            observer.didConnectDevice();
        }
    }

    public void finishAnalytics() {
        for (AnalyticsEventObserver observer : observers) {
            observer.didDisconnectDevice();
        }
    }

    private void init() {
        sThirdAppStartUpSendFlg = EnumSet.noneOf(Analytics.AnalyticsThirdAppStartUp.class);
    }

    /**
     * ショートカット操作情報イベント送信
     */
    public void sendShortCutActionEvent(Analytics.AnalyticsShortcutAction action, Analytics.AnalyticsActiveScreen screen) {
        sAnalytics.logShortcutActionEvent(action, screen);
    }

    /**
     * 3rd App起動トリガーイベント送信
     */
    public void sendThirdAppStartUpEvent(Analytics.AnalyticsThirdAppStartUp startUp) {
        //トリガー毎に1度だけ収集
        if (!sThirdAppStartUpSendFlg.contains(startUp)) {
            sThirdAppStartUpSendFlg.add(startUp);
            sAnalytics.logThirdAppStartUpEvent(startUp);
        }
    }

    /**
     * 電話機能の使用情報イベント送信
     */
    public void sendTelephoneCallEvent(Analytics.AnalyticsTelephoneCall trigger) {
        sAnalytics.logTelephoneCallEvent(trigger);
    }

    private void stopAll(EnumMap<?, Stopwatch> stopwatches) {
        for (EnumMap.Entry<?, Stopwatch> entry : stopwatches.entrySet()) {
            entry.getValue().stop();
            //Timber.d("stopWatch:key=" + entry.getKey() + ",totalDuration=" + entry.getValue().getElapsed() + "sec");
        }
    }

    private <T> void startJustOne(EnumMap<?, Stopwatch> stopwatches, T start) {
        for (EnumMap.Entry<?, Stopwatch> entry : stopwatches.entrySet()) {
            if (start == entry.getKey()) {
                entry.getValue().start();
            } else {
                entry.getValue().stop();
            }
        }
    }

    private class ActiveSourceObserver implements AnalyticsEventObserver {
        //視聴ソース情報
        private EnumMap<Analytics.AnalyticsSource, Stopwatch> mSourceActiveDuration = new EnumMap<>(Analytics.AnalyticsSource.class);
        private MediaSourceType mLastSourceType;//ソース変更時保存ソース
        private Analytics.SourceChangeReason mSourceChangeReason;//ソース切り替え操作トリガー

        @Override
        public void didConnectDevice() {
            if (!mEventBus.isRegistered(this)) {
                mEventBus.register(this);
            }
            mSourceChangeReason = null;
            mLastSourceType = mGetStatusHolder.execute().getCarDeviceStatus().sourceType;
            mSourceActiveDuration = new EnumMap<>(Analytics.AnalyticsSource.class);
            startActiveSourceDuration(mGetStatusHolder.execute().getCarDeviceStatus().sourceType);
        }

        @Override
        public void didDisconnectDevice() {
            sendActiveSourceEvent();
            mEventBus.unregister(this);
        }

        /**
         * ソース視聴時間計測開始
         */
        private void startActiveSourceDuration(MediaSourceType sourceType) {
            Analytics.AnalyticsSource analyticsSource;
            Timber.d("startSourceDuration:sourceType=" + sourceType + ",mSourceChangeReason=" + mSourceChangeReason);
            if (mSourceChangeReason != null) {
                if (mSourceChangeReason == Analytics.SourceChangeReason.temporarySourceChange) {
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
            if (mSourceActiveDuration != null) {
                stopAll(mSourceActiveDuration);
                if (analyticsSource != null) {
                    if (!mSourceActiveDuration.containsKey(analyticsSource)) {
                        mSourceActiveDuration.put(analyticsSource, new Stopwatch());
                    }
                    startJustOne(mSourceActiveDuration, analyticsSource);
                }
            }
        }

        /**
         * 視聴ソース情報収集イベント送信
         */
        private void sendActiveSourceEvent() {
            startSourceDuration(null);
            for (EnumMap.Entry<Analytics.AnalyticsSource, Stopwatch> entry : mSourceActiveDuration.entrySet()) {
                Timber.d("sendActiveSourceEvent:" + entry.getKey() + " : " + entry.getValue().getElapsed() + "sec");
                long durationMinute = entry.getValue().getElapsed() / 60;
                if (durationMinute > 0) {
                    sAnalytics.logActiveSourceEvent(entry.getKey(), durationMinute);
                }
                entry.getValue().reset();
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
                if (status.sourceType != mLastSourceType) {
                    // ラストソースと異なるソース変更後
                    startActiveSourceDuration(status.sourceType);
                    mLastSourceType = status.sourceType;
                    mSourceChangeReason = null;
                }
            }
        }

        /**
         * AppMusicAudioModeChangeEventハンドラ
         *
         * @param event AppMusicAudioModeChangeEvent
         */
        @Subscribe
        public void onAppMusicAudioModeChangeEvent(AppMusicAudioModeChangeEvent event) {
            Timber.d("onAppMusicAudioModeChangeEvent:mLastSourceType=" + mLastSourceType + ",sourceType =" + mGetStatusHolder.execute().getCarDeviceStatus().sourceType + ",mode=" + mGetStatusHolder.execute().getAppStatus().appMusicAudioMode);
            if (mLastSourceType == MediaSourceType.APP_MUSIC && mGetStatusHolder.execute().getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC) {
                startActiveSourceDuration(MediaSourceType.APP_MUSIC);
            }
        }

        @Subscribe
        public void onSourceChangeReasonEvent(SourceChangeReasonEvent event) {
            mSourceChangeReason = event.reason;
        }
    }

    private class UIOrientationObserver implements AnalyticsEventObserver {
        //スマホ端末の縦/横割合(連携中)情報
        private EnumMap<Analytics.AnalyticsUIOrientation, Stopwatch> mUIOrientationDuration = new EnumMap<>(Analytics.AnalyticsUIOrientation.class);

        @Override
        public void didConnectDevice() {
            if (!mEventBus.isRegistered(this)) {
                mEventBus.register(this);
            }
            mUIOrientationDuration = new EnumMap<>(Analytics.AnalyticsUIOrientation.class);

            Configuration config = mContext.getResources().getConfiguration();
            startUIOrientationDuration(config.orientation, true);
        }

        @Override
        public void didDisconnectDevice() {
            sendUIOrientationEvent();
            mEventBus.unregister(this);
        }

        /**
         * スマホ端末の縦/横割合(連携中)情報計測開始
         */
        private void startUIOrientationDuration(int orientation, boolean foreground) {
            Analytics.AnalyticsUIOrientation uiOrientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                uiOrientation = Analytics.AnalyticsUIOrientation.portrait;
            } else {
                uiOrientation = Analytics.AnalyticsUIOrientation.landscape;
            }
            Timber.d("startUIOrientationDuration:orientation=" + uiOrientation.value + ",foreground=" + foreground);
            if (mUIOrientationDuration != null) {
                stopAll(mUIOrientationDuration);
                if (foreground) {
                    if (!mUIOrientationDuration.containsKey(uiOrientation)) {
                        mUIOrientationDuration.put(uiOrientation, new Stopwatch());
                    }
                    startJustOne(mUIOrientationDuration, uiOrientation);
                }
            }
        }

        /**
         * スマホ端末の縦/横割合(連携中)情報イベント送信
         */
        private void sendUIOrientationEvent() {
            startUIOrientationDuration(0, false);
            for (EnumMap.Entry<Analytics.AnalyticsUIOrientation, Stopwatch> entry : mUIOrientationDuration.entrySet()) {
                Timber.d("sendUIOrientationEvent:" + entry.getKey() + " : " + entry.getValue().getElapsed() + "sec");
                long durationMinute = entry.getValue().getElapsed() / 60;
                if (durationMinute > 0) {
                    sAnalytics.logUIOrientationEvent(entry.getKey(), durationMinute);
                }
                entry.getValue().reset();
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
            if (ev.appState == AppStateChangeEvent.AppState.STARTED) {
                //アプリがフォアグラウンド
                if (mGetStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED && mGetStatusHolder.execute().getAppStatus().isAgreedCaution) {
                    Configuration config = mContext.getResources().getConfiguration();
                    startUIOrientationDuration(config.orientation, true);
                }
            } else if (ev.appState == AppStateChangeEvent.AppState.STOPPED) {
                //アプリがバックグラウンド
                if (mGetStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED && mGetStatusHolder.execute().getAppStatus().isAgreedCaution) {
                    Configuration config = mContext.getResources().getConfiguration();
                    startUIOrientationDuration(config.orientation, false);
                }
            }
        }
    }

    private class ActiveScreenObserver implements AnalyticsEventObserver {
        //HOME画面/AV画面/バックグラウンドの滞留時間情報
        private EnumMap<Analytics.AnalyticsActiveScreen, Stopwatch> mActiveScreenDuration = new EnumMap<>(Analytics.AnalyticsActiveScreen.class);
        private Analytics.AnalyticsActiveScreen mLastActiveScreen = null;
        private Analytics.AnalyticsActiveScreen mLastForegroundScreen = null;

        @Override
        public void didConnectDevice() {
            if (!mEventBus.isRegistered(this)) {
                mEventBus.register(this);
            }
            mLastActiveScreen = null;
            mActiveScreenDuration = new EnumMap<>(Analytics.AnalyticsActiveScreen.class);

            startActiveScreenDuration(Analytics.AnalyticsActiveScreen.home_screen, true);
        }

        @Override
        public void didDisconnectDevice() {
            sendActiveScreenEvent();
            mEventBus.unregister(this);
        }

        private Analytics.AnalyticsActiveScreen getActiveScreen() {
            return mLastActiveScreen;
        }

        private Analytics.AnalyticsActiveScreen getLastForegroundScreen() {
            return mLastForegroundScreen;
        }

        /**
         * HOME画面/AV画面/バックグラウンドの滞留時間計測開始
         */
        private void startActiveScreenDuration(Analytics.AnalyticsActiveScreen activeScreen, boolean start) {
            if (activeScreen == null) {
                Timber.d("startActiveScreenDuration:activeScreen is Null" + ",start=" + start);
                mLastForegroundScreen = null;
                mLastActiveScreen = null;
                return;
            }
            if (activeScreen == Analytics.AnalyticsActiveScreen.home_screen || activeScreen == Analytics.AnalyticsActiveScreen.av_screen) {
                mLastForegroundScreen = activeScreen;
            }
            Timber.d("startActiveScreenDuration:activeScreen=" + activeScreen.value + ",start=" + start);
            if (mActiveScreenDuration != null) {
                stopAll(mActiveScreenDuration);
                if (start) {
                    if (!mActiveScreenDuration.containsKey(activeScreen)) {
                        mActiveScreenDuration.put(activeScreen, new Stopwatch());
                    }
                    startJustOne(mActiveScreenDuration, activeScreen);
                }
            }
            if (start) {
                mLastActiveScreen = activeScreen;
            } else {
                mLastActiveScreen = null;
            }
        }

        /**
         * HOME画面/AV画面/バックグラウンドの滞留時間情報イベント送信
         */
        private void sendActiveScreenEvent() {
            startActiveScreenDuration(mLastActiveScreen, false);
            for (EnumMap.Entry<Analytics.AnalyticsActiveScreen, Stopwatch> entry : mActiveScreenDuration.entrySet()) {
                Timber.d("sendActiveScreenEvent:" + entry.getKey() + " : " + entry.getValue().getElapsed() + "sec");
                long durationMinute = entry.getValue().getElapsed() / 60;
                if (durationMinute > 0) {
                    sAnalytics.logActiveScreenEvent(entry.getKey(), durationMinute);
                }
                entry.getValue().reset();
            }
            mLastActiveScreen = null;
        }

        /**
         * メイン画面遷移イベントハンドラ.
         *
         * @param ev メイン画面遷移イベント
         */
        @Subscribe
        public void onMainNavigateEvent(MainNavigateEvent ev) {
            ScreenId screenId = ev.screenId;
            if (screenId == ScreenId.PLAYER_CONTAINER) {
                startActiveScreenDuration(Analytics.AnalyticsActiveScreen.av_screen, true);
            } else if (screenId == ScreenId.HOME_CONTAINER) {
                startActiveScreenDuration(Analytics.AnalyticsActiveScreen.home_screen, true);
            } else if (screenId == ScreenId.SETTINGS_CONTAINER || screenId == ScreenId.UNCONNECTED_CONTAINER) {
                startActiveScreenDuration(getActiveScreen(), false);
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
            if (ev.appState == AppStateChangeEvent.AppState.STARTED) {
                //アプリがフォアグラウンド
                if (mGetStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED && mGetStatusHolder.execute().getAppStatus().isAgreedCaution) {
                    startActiveScreenDuration(Analytics.AnalyticsActiveScreen.background, false);
                    //Pause前の表示画面がAV画面/HOME画面であれば計測再開
                    startActiveScreenDuration(getLastForegroundScreen(), true);
                }
            } else if (ev.appState == AppStateChangeEvent.AppState.STOPPED) {
                //アプリがバックグラウンド
                if (mGetStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED && mGetStatusHolder.execute().getAppStatus().isAgreedCaution) {
                    startActiveScreenDuration(getActiveScreen(), false);
                    startActiveScreenDuration(Analytics.AnalyticsActiveScreen.background, true);
                }
            }
        }
    }

    private class SourceSelectActionObserver implements AnalyticsEventObserver {
        private final Handler mHandler = new Handler();
        private Runnable mRunnable;
        private MediaSourceType mLastSourceTypeTrigger;//ソース切り替え操作トリガー用保存ソース
        private MediaSourceType mLastSourceType;//ソース変更時保存ソース
        private Analytics.SourceChangeReason mSourceChangeReason;//ソース切り替え操作トリガー

        @Override
        public void didConnectDevice() {
            if (!mEventBus.isRegistered(this)) {
                mEventBus.register(this);
            }
            mSourceChangeReason = null;
            mLastSourceType = mGetStatusHolder.execute().getCarDeviceStatus().sourceType;
            mLastSourceTypeTrigger = null;
            mLastSourceTypeTrigger = mGetStatusHolder.execute().getCarDeviceStatus().sourceType;
        }

        @Override
        public void didDisconnectDevice() {
            mEventBus.unregister(this);
        }

        /**
         * ソース切り替え操作トリガー情報イベント送信
         */
        private void sendSourceSelectReasonEvent(MediaSourceType sourceType) {
            Timber.d("sendSourceSelectReasonEvent:sourceType=" + sourceType);
            mHandler.removeCallbacks(mRunnable);
            if (sourceType == MediaSourceType.BT_PHONE || sourceType == MediaSourceType.IPOD
                    || sourceType == MediaSourceType.DAB_INTERRUPT || sourceType == MediaSourceType.HD_RADIO_INTERRUPT
                    || sourceType == MediaSourceType.TTS) {
                mSourceChangeReason = null;
                return;
            }
            if (sourceType != mLastSourceTypeTrigger) {
                final Analytics.SourceChangeReason reason = mSourceChangeReason;
                mSourceChangeReason = null;
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Timber.d("sendSourceSelectReasonEvent:mSourceChangeReason=" + reason + ",sourceType=" + sourceType + ",newSourceType=" + mGetStatusHolder.execute().getCarDeviceStatus().sourceType);
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
                    mHandler.postDelayed(mRunnable, 10000);//10s
                } else if (reason == Analytics.SourceChangeReason.appSourceList) {
                    sAnalytics.logSourceSelectReasonEvent(Analytics.AnalyticsSourceChangeReason.appSourceList);
                }
                mLastSourceTypeTrigger = sourceType;
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
                if (status.sourceType != mLastSourceType) {
                    // ラストソースと異なるソース変更後
                    sendSourceSelectReasonEvent(status.sourceType);
                    mLastSourceType = status.sourceType;
                }
            }
        }

        @Subscribe
        public void onSourceChangeReasonEvent(SourceChangeReasonEvent event) {
            mSourceChangeReason = event.reason;
        }
    }

    private class NaviAppUseObserver implements AnalyticsEventObserver {

        @Override
        public void didConnectDevice() {

        }

        @Override
        public void didDisconnectDevice() {
            Timber.d("NaviAppUseObserver");
            //デフォルトのタイムゾーンおよびロケールを使用して現在時間のカレンダを取得
            Calendar nowCal = Calendar.getInstance();
            //ナビアプリの利用情報
            boolean isNaviAppsOneWeekBefore = isSentOneWeekBefore(nowCal, mAnalyticsPreference.getNaviAppsLastSentDate());
            String appInstalledStr = "";
            String appSettingStr = "";
            BaseApp baseApp = null;
            int[] naviInstalledArray = new int[(int) Math.ceil((double) NAVI_APP_SIZE / 4)];
            int weatherInstalled = 0;
            int boatingInstalled = 0;
            int fishingInstalled = 0;
            int[] naviSettingArray = new int[(int) Math.ceil((double) NAVI_APP_SIZE / 4)];
            int weatherSetting = 0;
            int boatingSetting = 0;
            int fishingSetting = 0;
            List<ApplicationInfo> naviApps = mNaviCase.getInstalledTargetAppList();
            for (ApplicationInfo app : naviApps) {
                NaviApp naviApp = NaviApp.fromPackageName(app.packageName);
                int idx = naviApp.getNumber() - 1;
                if (DBG) Timber.d("idx=" + idx + ",App=" + naviApp.name());
                naviInstalledArray[getArrayIndex(naviInstalledArray.length, idx)] = naviInstalledArray[getArrayIndex(naviInstalledArray.length, idx)] | indexTo4Bit(idx);
            }
            StringBuilder naviAppInstalledStr = new StringBuilder();
            for (int b : naviInstalledArray) {
                naviAppInstalledStr.append(String.format("%01X", b));
            }
            if (DBG) logBinaryString(naviInstalledArray);

            if (mPreference.getLastConnectedCarDeviceClassId() == CarDeviceClassId.MARIN) {
                List<ApplicationInfo> weatherApps = mMarinCase.getInstalledWeatherTargetAppList();
                List<ApplicationInfo> boatingApps = mMarinCase.getInstalledBoatingTargetAppList();
                List<ApplicationInfo> fishingApps = mMarinCase.getInstalledFishingTargetAppList();
                MarinApp marinApp;
                for (ApplicationInfo app : weatherApps) {
                    marinApp = MarinApp.fromPackageName(app.packageName);
                    int idx = marinApp.getNumber() - 1;
                    if (DBG) Timber.d("idx=" + idx + ",App=" + marinApp.name());
                    weatherInstalled = weatherInstalled | indexTo8Bit(idx);
                }
                for (ApplicationInfo app : boatingApps) {
                    marinApp = MarinApp.fromPackageName(app.packageName);
                    int idx = marinApp.getNumber() - 1;
                    if (DBG) Timber.d("idx=" + idx + ",App=" + marinApp.name());
                    boatingInstalled = boatingInstalled | indexTo8Bit(idx);
                }
                for (ApplicationInfo app : fishingApps) {
                    marinApp = MarinApp.fromPackageName(app.packageName);
                    int idx = marinApp.getNumber() - 1;
                    if (DBG) Timber.d("idx=" + idx + ",App=" + marinApp.name());
                    fishingInstalled = fishingInstalled | indexTo8Bit(idx);
                }

                if (DBG) logBinaryString(weatherInstalled, boatingInstalled, fishingInstalled);
                AppSharedPreference.Application app = mPreference.getNavigationMarinApp();

                try {
                    if (mPreference.getNavigationMarinApp() != null) {
                        baseApp = MarinApp.fromPackageNameNoThrow(mPreference.getNavigationMarinApp().packageName);
                        if (baseApp != null) {
                            MarinApp settingmarinApp = (MarinApp) baseApp;
                            int idx = settingmarinApp.getNumber() - 1;
                            //起動設定したアプリがアンインストールされている場合は未設定となる(ALL0)。
                            if (settingmarinApp.getCategory() == MarinAppCategory.WEATHER) {
                                if (isSettingAppInstalled(weatherInstalled, indexTo8Bit(idx))) {
                                    weatherSetting = indexTo8Bit(idx);
                                }
                            } else if (settingmarinApp.getCategory() == MarinAppCategory.BOATING) {
                                if (isSettingAppInstalled(boatingInstalled, indexTo8Bit(idx))) {
                                    boatingSetting = indexTo8Bit(idx);
                                }
                            } else if (settingmarinApp.getCategory() == MarinAppCategory.FISHING) {
                                if (isSettingAppInstalled(fishingInstalled, indexTo8Bit(idx))) {
                                    fishingSetting = indexTo8Bit(idx);
                                }
                            }
                        } else {
                            baseApp = NaviApp.fromPackageName(mPreference.getNavigationMarinApp().packageName);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Timber.e(e.getMessage());
                }
            } else {
                try {
                    baseApp = NaviApp.fromPackageName(mPreference.getNavigationApp().packageName);
                } catch (IllegalArgumentException e) {
                    Timber.e(e.getMessage());
                }
            }
            StringBuilder naviAppSettingStr = new StringBuilder();
            if (baseApp instanceof NaviApp) {
                NaviApp settingNaviApp = (NaviApp) baseApp;
                int idx = settingNaviApp.getNumber() - 1;
                //起動設定したアプリがアンインストールされている場合は未設定となる(ALL0)。
                if (isSettingAppInstalled(naviInstalledArray[getArrayIndex(naviInstalledArray.length, idx)], indexTo4Bit(idx))) {
                    naviSettingArray[getArrayIndex(naviSettingArray.length, idx)] = indexTo4Bit(idx);
                }
                for (int b : naviSettingArray) {
                    naviAppSettingStr.append(String.format("%01X", b));
                }
            } else {
                naviAppSettingStr.append("0000");
            }
            appInstalledStr = String.format("%s/%02X/%02X/%02X", naviAppInstalledStr, weatherInstalled, boatingInstalled, fishingInstalled);
            appSettingStr = String.format("%s/%02X/%02X/%02X", naviAppSettingStr, weatherSetting, boatingSetting, fishingSetting);
            if (DBG)
                Timber.d("appInstalledStr=" + appInstalledStr + ",appSettingStr" + appSettingStr);
            // 未送信、または前回送信時から変化した、または前回送信時から1週間以上経過した場合送信
            if ((mAnalyticsPreference.getNaviAppsInstalled() == null)
                    || !appInstalledStr.equals(mAnalyticsPreference.getNaviAppsInstalled())
                    || !appSettingStr.equals(mAnalyticsPreference.getNaviAppsSetting())
                    || isNaviAppsOneWeekBefore) {
                mAnalyticsPreference.setNaviAppsInstalled(appInstalledStr);
                mAnalyticsPreference.setNaviAppsSetting(appSettingStr);
                sAnalytics.logNaviAppsEvent(appInstalledStr, appSettingStr);
                mAnalyticsPreference.setNaviAppLastSentDate(nowCal.getTimeInMillis());
            }
        }
    }

    private class MessageAppUseObserver implements AnalyticsEventObserver {
        @Override
        public void didConnectDevice() {

        }

        @Override
        public void didDisconnectDevice() {
            Timber.d("MessageAppUseObserver");
            //デフォルトのタイムゾーンおよびロケールを使用して現在時間のカレンダを取得
            Calendar nowCal = Calendar.getInstance();
            //ナビアプリの利用情報
            boolean isMessageAppsOneWeekBefore = isSentOneWeekBefore(nowCal, mAnalyticsPreference.getMessageAppsLastSentDate());
            int[] messageInstalledArray = new int[(int) Math.ceil((double) MESSAGE_APP_SIZE / 4)];
            List<ApplicationInfo> MessageApps = mMessagingCase.getInstalledTargetAppList();
            for (ApplicationInfo app : MessageApps) {
                MessagingApp MessageApp = MessagingApp.fromPackageName(app.packageName);
                int idx = MessageApp.getNumber() - 1;
                if (DBG) Timber.d("idx=" + idx + ",App=" + MessageApp.name());
                messageInstalledArray[getArrayIndex(messageInstalledArray.length, idx)] = messageInstalledArray[getArrayIndex(messageInstalledArray.length, idx)] | indexTo4Bit(idx);
            }
            StringBuilder MessageAppInstalledStr = new StringBuilder();
            for (int b : messageInstalledArray) {
                MessageAppInstalledStr.append(String.format("%01X", b));
            }
            if (DBG) logBinaryString(messageInstalledArray);
            // 未送信、または前回送信時から変化した、または前回送信時から1週間以上経過した場合送信
            if ((mAnalyticsPreference.getMessageAppsInstalled() == null)
                    || !MessageAppInstalledStr.toString().equals(mAnalyticsPreference.getMessageAppsInstalled())
                    || isMessageAppsOneWeekBefore) {
                mAnalyticsPreference.setMessageAppsInstalled(MessageAppInstalledStr.toString());
                sAnalytics.logMessageAppsEvent(MessageAppInstalledStr.toString());
                mAnalyticsPreference.setMessageAppLastSentDate(nowCal.getTimeInMillis());
            }
        }
    }

    private class MusicAppUseObserver implements AnalyticsEventObserver {
        @Override
        public void didConnectDevice() {

        }

        @Override
        public void didDisconnectDevice() {
            Timber.d("MusicAppUseObserver");
            //デフォルトのタイムゾーンおよびロケールを使用して現在時間のカレンダを取得
            Calendar nowCal = Calendar.getInstance();
            //ナビアプリの利用情報
            boolean isMusicAppsOneWeekBefore = isSentOneWeekBefore(nowCal, mAnalyticsPreference.getMusicAppsLastSentDate());
            int[] musicInstalledArray = new int[(int) Math.ceil((double) MUSIC_APP_SIZE / 4)];
            List<ApplicationInfo> musicApps = mPreferMusicApp.getInstalledTargetAppList();
            for (ApplicationInfo app : musicApps) {
                MusicApp musicApp = MusicApp.fromPackageName(app.packageName);
                int idx = musicApp.getNumber() - 1;
                if (DBG) Timber.d("idx=" + idx + ",App=" + musicApp.name());
                musicInstalledArray[getArrayIndex(musicInstalledArray.length, idx)] = musicInstalledArray[getArrayIndex(musicInstalledArray.length, idx)] | indexTo4Bit(idx);
            }
            StringBuilder MusicAppInstalledStr = new StringBuilder();
            for (int b : musicInstalledArray) {
                MusicAppInstalledStr.append(String.format("%01X", b));
            }
            if (DBG) logBinaryString(musicInstalledArray);
            // 未送信、または前回送信時から変化した、または前回送信時から1週間以上経過した場合送信
            if ((mAnalyticsPreference.getMusicAppsInstalled() == null)
                    || !MusicAppInstalledStr.toString().equals(mAnalyticsPreference.getMusicAppsInstalled())
                    || isMusicAppsOneWeekBefore) {
                mAnalyticsPreference.setMusicAppsInstalled(MusicAppInstalledStr.toString());
                sAnalytics.logMusicAppsEvent(MusicAppInstalledStr.toString());
                mAnalyticsPreference.setMusicAppLastSentDate(nowCal.getTimeInMillis());
            }
        }
    }

    private int getArrayIndex(int arrayLength, int idx) {
        return arrayLength - 1 - idx / 4;
    }

    private int indexTo4Bit(int idx) {
        return 1 << (idx % 4);
    }

    private int indexTo8Bit(int idx) {
        return 1 << (idx % 8);
    }

    private void logBinaryString(int[] array) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int b : array) {
            //int i = Byte.toUnsignedInt(b); // 符号なし変換
            String str = Integer.toBinaryString(b); // バイナリ文字列を取得
            str = String.format("%4s", str).replace(' ', '0'); // 0パディング
            stringBuilder.append(str);
        }
        Timber.d("2進数：App=" + stringBuilder);
    }

    private boolean isSettingAppInstalled(int installed, int setting) {
        return (installed & setting) != 0;
    }

    private void logBinaryString(int weatherApp, int boatingApp, int fishingApp) {
        Timber.d("2進数：weatherApp=" + String.format("%8s", Integer.toBinaryString(weatherApp)).replace(' ', '0')
                + ",boatingApp=" + String.format("%8s", Integer.toBinaryString(boatingApp)).replace(' ', '0')
                + ",fishingApp=" + String.format("%8s", Integer.toBinaryString(fishingApp)).replace(' ', '0'));
    }

    private class MessageObserver implements AnalyticsEventObserver {
        @Override
        public void didConnectDevice() {
            if (!mEventBus.isRegistered(this)) {
                mEventBus.register(this);
            }
        }

        @Override
        public void didDisconnectDevice() {
            mEventBus.unregister(this);
        }

        @Subscribe
        public void onMessageReadFinishedEvent(MessageReadFinishedEvent event) {
            //メッセージ読み上げ機能の使用情報イベント送信【メッセージ読み上げ】
            try {
                MessagingApp messagingApp = MessagingApp.fromPackageName(event.packageName);
                String app = messagingApp.getAppName();
                sAnalytics.logMessageReadEvent(app);
            } catch (IllegalArgumentException e) {
                Timber.e(e.getMessage());
            }
        }

        /**
         * ReadNotificationPostedEventハンドラ
         * <p>
         * 新規通知を受信した場合に動作する。
         *
         * @param event ReadNotificationPostedEvent
         */
        @Subscribe
        public void onReadNotificationPostedEvent(ReadNotificationPostedEvent event) {
            //メッセージ読み上げ機能の使用情報イベント送信【新着メッセージ受信】
            try {
                MessagingApp messagingApp = MessagingApp.fromPackageName(event.notification.getPackageName());
                String app = messagingApp.getAppName();
                sAnalytics.logMessageArrivalEvent(app);
            } catch (IllegalArgumentException e) {
                Timber.e(e.getMessage());
            }
        }
    }

    private class YouTubeLinkUseObserver implements AnalyticsEventObserver {

        @Override
        public void didConnectDevice() {

        }

        @Override
        public void didDisconnectDevice() {
            Timber.d("YouTubeLinkUseObserver");
            //デフォルトのタイムゾーンおよびロケールを使用して現在時間のカレンダを取得
            Calendar nowCal = Calendar.getInstance();
            boolean isYoutubeLinkUseSentOneWeekBefore = isSentOneWeekBefore(nowCal, mAnalyticsPreference.getYoutubeLinkUseLastSentDate());
            boolean isYoutubeLinkEnabled = mPreference.isYouTubeLinkSettingEnabled();
            //最終連携車載機がYoutubeLink対応で未送信
            if (mYouTubeLinkStatus.isYouTubeLinkSettingAvailable()
                    && mAnalyticsPreference.getYoutubeLinkUseLastSentDate() == 0) {
                //現在の設定値がONであればONにしたことがある送信
                if (isYoutubeLinkEnabled) {
                    mAnalyticsPreference.setYoutubeLinkUse(true);
                }
                sAnalytics.logYouTubeLinkUseEvent(mAnalyticsPreference.isYoutubeLinkUse() ? Analytics.AnalyticsYouTubeLinkUse.on : Analytics.AnalyticsYouTubeLinkUse.neverOn);
                mAnalyticsPreference.setYoutubeLinkUseLastSentDate(nowCal.getTimeInMillis());
            } else if (isYoutubeLinkUseSentOneWeekBefore
                    || (isYoutubeLinkEnabled && !mAnalyticsPreference.isYoutubeLinkUse())) {
                //YoutubeLink設定ONで「ONにした」未送信
                //前回送信時から1週間以上経過した場合、使用情報を送信
                if (isYoutubeLinkEnabled) {
                    mAnalyticsPreference.setYoutubeLinkUse(true);
                }
                sAnalytics.logYouTubeLinkUseEvent(mAnalyticsPreference.isYoutubeLinkUse() ? Analytics.AnalyticsYouTubeLinkUse.on : Analytics.AnalyticsYouTubeLinkUse.neverOn);
                mAnalyticsPreference.setYoutubeLinkUseLastSentDate(nowCal.getTimeInMillis());
            }
        }
    }

    private class AlexaUseObserver implements AnalyticsEventObserver {
        @Override
        public void didConnectDevice() {
        }

        @Override
        public void didDisconnectDevice() {
            Timber.d("AlexaUseObserver");
            //デフォルトのタイムゾーンおよびロケールを使用して現在時間のカレンダを取得
            Calendar nowCal = Calendar.getInstance();
            //Alexa使用情報送信
            boolean isAlexaUseSentOneWeekBefore = isSentOneWeekBefore(nowCal, mAnalyticsPreference.getAlexaUseLastSentDate());
            boolean isAuthenticated = mGetStatusHolder.execute().getAppStatus().alexaAuthenticated;
            //Alexa対応国で未送信
            if (mGetStatusHolder.execute().getAppStatus().isAlexaAvailableCountry
                    && mAnalyticsPreference.getAlexaUseLastSentDate() == 0) {
                //ログイン状態であれば「ログインに成功した」送信
                if (isAuthenticated) {
                    mAnalyticsPreference.setAlexaUse(true);
                }
                sAnalytics.logAlexaUseEvent(mAnalyticsPreference.isAlexaUse() ? Analytics.AnalyticsAlexaUse.loginSuccess : Analytics.AnalyticsAlexaUse.neverLogin);
                mAnalyticsPreference.setAlexaUseLastSentDate(nowCal.getTimeInMillis());
            } else if (isAlexaUseSentOneWeekBefore
                    || (isAuthenticated && !mAnalyticsPreference.isAlexaUse())) {
                //ログイン状態で「ログインに成功した」未送信
                //前回送信時から1週間以上経過した場合、使用情報を送信
                if (isAuthenticated) {
                    mAnalyticsPreference.setAlexaUse(true);
                }
                sAnalytics.logAlexaUseEvent(mAnalyticsPreference.isAlexaUse() ? Analytics.AnalyticsAlexaUse.loginSuccess : Analytics.AnalyticsAlexaUse.neverLogin);
                mAnalyticsPreference.setAlexaUseLastSentDate(nowCal.getTimeInMillis());
            }

            //Alexa言語設定情報送信
            boolean isAlexaLanguageSentOneWeekBefore = isSentOneWeekBefore(nowCal, mAnalyticsPreference.getAlexaLanguageLastSentDate());
            //Alexa対応国でログイン状態で送信
            if (mGetStatusHolder.execute().getAppStatus().isAlexaAvailableCountry && isAuthenticated) {
                AlexaLanguageType alexaLanguageType = mPreference.getAlexaLanguage();
                // 未送信、または前回送信時から変化した、または前回送信時から1週間以上経過した場合送信
                if (mAnalyticsPreference.getAlexaLanguageSent() == null
                        || alexaLanguageType != mAnalyticsPreference.getAlexaLanguageSent()
                        || isAlexaLanguageSentOneWeekBefore) {
                    mAnalyticsPreference.setAlexaLanguageSent(alexaLanguageType);
                    sAnalytics.logAlexaLanguageEvent(alexaLanguageType.strValue);
                    mAnalyticsPreference.setAlexaLanguageLastSentDate(nowCal.getTimeInMillis());
                }
            }
        }
    }

    private class FxSettingObserver implements AnalyticsEventObserver {
        boolean mDeviceEqualizerSettingSet = false;//車載機からEQ設定を取得したか（一度でもソースOFF以外にしたか）
        boolean mDeviceLiveSimulationSettingSet = false;//車載機からライブシミュレーション設定を取得したか（一度でもAppMusicソースにしたか）
        boolean mDeviceSuperTodorokiSettingSet = false;//車載機からスーパー轟設定を取得したか（一度でもソースOFF以外にしたか）


        @Override
        public void didConnectDevice() {
            if (!mEventBus.isRegistered(this)) {
                mEventBus.register(this);
            }
            mDeviceEqualizerSettingSet = false;
            mDeviceLiveSimulationSettingSet = false;
            mDeviceSuperTodorokiSettingSet = false;
            if (mGetStatusHolder.execute().getCarDeviceStatus().sourceType != MediaSourceType.OFF) {
                mDeviceEqualizerSettingSet = true;
                mDeviceSuperTodorokiSettingSet = true;
            }
            if (mGetStatusHolder.execute().getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC) {
                mDeviceLiveSimulationSettingSet = true;
            }
        }

        @Override
        public void didDisconnectDevice() {
            Timber.d("FxSettingObserver");
            mEventBus.unregister(this);
            //デフォルトのタイムゾーンおよびロケールを使用して現在時間のカレンダを取得
            Calendar nowCal = Calendar.getInstance();
            //FX設定の使用情報-EQ設定
            boolean isFxEqualizerSentOneWeekBefore = isSentOneWeekBefore(nowCal, mAnalyticsPreference.getFxEqualizerLastSentDate());
            if (mDeviceEqualizerSettingSet) {
                StatusHolder holder = mGetStatusHolder.execute();
                SoundFxSetting fxSetting = holder.getSoundFxSetting();
                SoundFxSettingEqualizerType settingType = fxSetting.soundFxSettingEqualizerType;
                if (DBG) Timber.d("SoundFxSettingEqualizerType=" + settingType);
                // 未送信、または前回送信時から変化した、または前回送信時から1週間以上経過した場合送信
                if (mAnalyticsPreference.getFxEqualizerSent() == null
                        || settingType != mAnalyticsPreference.getFxEqualizerSent()
                        || isFxEqualizerSentOneWeekBefore) {
                    mAnalyticsPreference.setFxEqualizerSent(settingType);
                    sAnalytics.logFXEqualizerEvent(settingType.getAnalyticsStr());
                    mAnalyticsPreference.setFxEqualizerLastSentDate(nowCal.getTimeInMillis());
                }
            } else {
                // 送信したことがあり前回送信時から1週間以上経過した場合送信
                if (mAnalyticsPreference.getFxEqualizerSent() != null
                        && isFxEqualizerSentOneWeekBefore) {
                    sAnalytics.logFXEqualizerEvent(mAnalyticsPreference.getFxEqualizerSent().getAnalyticsStr());
                    mAnalyticsPreference.setFxEqualizerLastSentDate(nowCal.getTimeInMillis());
                }
            }
            //FX設定の使用情報-ライブシミュレーション設定
            boolean isFxLiveSimulationSentOneWeekBefore = isSentOneWeekBefore(nowCal, mAnalyticsPreference.getFxLiveSimulationLastSentDate());
            if (mDeviceLiveSimulationSettingSet) {
                StatusHolder holder = mGetStatusHolder.execute();
                SoundFxSetting fxSetting = holder.getSoundFxSetting();
                SoundFieldControlSettingType sfcSettingType = fxSetting.liveSimulationSetting.soundFieldControlSettingType;
                SoundEffectType seSettingType = fxSetting.liveSimulationSetting.soundEffectSettingType.type;
                if (DBG) Timber.d("sfcSettingType=" + sfcSettingType + ",seSettingType=" + seSettingType);
                // 未送信、または前回送信時から変化した、または前回送信時から1週間以上経過した場合送信
                if ((mAnalyticsPreference.getFxLiveSimulationSfcSent() == null && mAnalyticsPreference.getFxLiveSimulationSeSent() == null)
                        || sfcSettingType != mAnalyticsPreference.getFxLiveSimulationSfcSent()
                        || seSettingType != mAnalyticsPreference.getFxLiveSimulationSeSent()
                        || isFxLiveSimulationSentOneWeekBefore) {
                    mAnalyticsPreference.setFxLiveSimulationSfcSent(sfcSettingType);
                    mAnalyticsPreference.setFxLiveSimulationSeSent(seSettingType);
                    sAnalytics.logFXLiveSimulationEvent(sfcSettingType.getAnalyticsStr(), seSettingType.getAnalyticsStr());
                    mAnalyticsPreference.setFxLiveSimulationLastSentDate(nowCal.getTimeInMillis());
                }
            } else {
                // 送信したことがあり前回送信時から1週間以上経過した場合送信
                if (mAnalyticsPreference.getFxLiveSimulationSfcSent() != null && mAnalyticsPreference.getFxLiveSimulationSeSent() != null
                        && isFxLiveSimulationSentOneWeekBefore) {
                    sAnalytics.logFXLiveSimulationEvent(mAnalyticsPreference.getFxLiveSimulationSfcSent().getAnalyticsStr(), mAnalyticsPreference.getFxLiveSimulationSeSent().getAnalyticsStr());
                    mAnalyticsPreference.setFxLiveSimulationLastSentDate(nowCal.getTimeInMillis());
                }
            }
            //FX設定の使用情報-スーパー轟設定
            boolean isFxSuperTodorokiSentOneWeekBefore = isSentOneWeekBefore(nowCal, mAnalyticsPreference.getFxSuperTodorokiLastSentDate());
            if (mDeviceSuperTodorokiSettingSet) {
                StatusHolder holder = mGetStatusHolder.execute();
                SoundFxSetting fxSetting = holder.getSoundFxSetting();
                SuperTodorokiSetting settingType = fxSetting.superTodorokiSetting;
                if (DBG) Timber.d("SuperTodorokiSetting=" + settingType);

                // 未送信、または前回送信時から変化した、または前回送信時から1週間以上経過した場合送信
                if (mAnalyticsPreference.getFxSuperTodorokiSent() == null
                        || settingType != mAnalyticsPreference.getFxSuperTodorokiSent()
                        || isFxSuperTodorokiSentOneWeekBefore) {
                    mAnalyticsPreference.setFxSuperTodorokiSent(settingType);
                    sAnalytics.logFXSuperTodorokiEvent(settingType.getAnalyticsStr());
                    mAnalyticsPreference.setFxSuperTodorokiLastSentDate(nowCal.getTimeInMillis());
                }
            } else {
                // 送信したことがあり前回送信時から1週間以上経過した場合送信
                if (mAnalyticsPreference.getFxSuperTodorokiSent() != null
                        && isFxSuperTodorokiSentOneWeekBefore) {
                    sAnalytics.logFXSuperTodorokiEvent(mAnalyticsPreference.getFxSuperTodorokiSent().getAnalyticsStr());
                    mAnalyticsPreference.setFxSuperTodorokiLastSentDate(nowCal.getTimeInMillis());
                }
            }
        }

        /**
         * Equalizer設定変更イベント通知
         *
         * @param event Equalizer設定変更イベント
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEqualizerSettingChangeEvent(EqualizerSettingChangeEvent event) {
            if (DBG) Timber.d("onEqualizerSettingChangeEvent");
            mDeviceEqualizerSettingSet = true;
        }

        /**
         * LiveSimulation設定変更イベント.
         *
         * @param event LiveSimulationSettingChangeEvent
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onLiveSimulationSettingChangeEvent(LiveSimulationSettingChangeEvent event) {
            if (DBG) Timber.d("onLiveSimulationSettingChangeEvent");
            mDeviceLiveSimulationSettingSet = true;
        }

        /**
         * SoundFx設定の更新通知
         *
         * @param event SoundFxChangeEvent
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onSoundFxSettingChangeEvent(SoundFxSettingChangeEvent event) {
            if (DBG) Timber.d("onSoundFxChangeEvent");
            mDeviceSuperTodorokiSettingSet = true;
        }
    }

    private class EasySoundTaSettingObserver implements AnalyticsEventObserver {
        boolean mDeviceFxSettingSet = false;//車載機からFx設定を取得したか（一度でもソースOFF以外にしたか）

        @Override
        public void didConnectDevice() {
            if (!mEventBus.isRegistered(this)) {
                mEventBus.register(this);
            }
            mDeviceFxSettingSet = false;
            if (mGetStatusHolder.execute().getCarDeviceStatus().sourceType != MediaSourceType.OFF) {
                mDeviceFxSettingSet = true;
            }
        }

        @Override
        public void didDisconnectDevice() {
            Timber.d("EasySoundTaSettingObserver");
            mEventBus.unregister(this);
            //デフォルトのタイムゾーンおよびロケールを使用して現在時間のカレンダを取得
            Calendar nowCal = Calendar.getInstance();
            //FX設定の使用情報-EQ設定
            boolean isFxTimeAlignmentOneWeekBefore = isSentOneWeekBefore(nowCal, mAnalyticsPreference.getFxTimeAlignmentLastSentDate());
            if (mDeviceFxSettingSet) {
                StatusHolder holder = mGetStatusHolder.execute();
                SoundFxSetting fxSetting = holder.getSoundFxSetting();
                SmallCarTaSettingType easySoundFitSettingType = fxSetting.smallCarTaSetting.smallCarTaSettingType;
                AudioSetting audioSetting = holder.getAudioSetting();
                TimeAlignmentSettingMode taSettingType = audioSetting.timeAlignmentSetting.mode;
                if (DBG)
                    Timber.d("easySoundFitSettingType=" + easySoundFitSettingType + ",taSettingType=" + taSettingType);
                // 未送信、または前回送信時から変化した、または前回送信時から1週間以上経過した場合送信
                if ((mAnalyticsPreference.getFxEasySoundFitSent() == null && mAnalyticsPreference.getFxTimeAlignmentSent() == null)
                        || easySoundFitSettingType != mAnalyticsPreference.getFxEasySoundFitSent()
                        || taSettingType != mAnalyticsPreference.getFxTimeAlignmentSent()
                        || isFxTimeAlignmentOneWeekBefore) {
                    mAnalyticsPreference.setFxEasySoundFitSent(easySoundFitSettingType);
                    mAnalyticsPreference.setFxTimeAlignmentSent(taSettingType);
                    sAnalytics.logFXTimeAlignmentEvent(easySoundFitSettingType.getAnalyticsStr(), taSettingType.getAnalyticsStr());
                    mAnalyticsPreference.setFxTimeAlignmentLastSentDate(nowCal.getTimeInMillis());
                }
            } else {
                // 送信したことがあり前回送信時から1週間以上経過した場合送信
                if (mAnalyticsPreference.getFxEasySoundFitSent() != null && mAnalyticsPreference.getFxTimeAlignmentSent() != null
                        && isFxTimeAlignmentOneWeekBefore) {
                    sAnalytics.logFXTimeAlignmentEvent(mAnalyticsPreference.getFxEasySoundFitSent().getAnalyticsStr(), mAnalyticsPreference.getFxTimeAlignmentSent().getAnalyticsStr());
                    mAnalyticsPreference.setFxTimeAlignmentLastSentDate(nowCal.getTimeInMillis());
                }
            }
        }

        /**
         * SoundFx設定の更新通知
         *
         * @param event SoundFxChangeEvent
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onSoundFxSettingChangeEvent(SoundFxSettingChangeEvent event) {
            if (DBG) Timber.d("onSoundFxChangeEvent");
            mDeviceFxSettingSet = true;
        }

        /**
         * Audio設定変更イベント通知
         *
         * @param event Audio設定変更イベント
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onAudioSettingChangeAction(AudioSettingChangeEvent event) {
            if (DBG) Timber.d("onAudioSettingChangeAction");
            mDeviceFxSettingSet = true;
        }
    }

    private boolean isSentOneWeekBefore(Calendar nowCal, long lastSentDateMillis) {
        Date nowDate = nowCal.getTime();
        Date sentDate = new Date(lastSentDateMillis);
        Calendar sentCal = Calendar.getInstance();
        sentCal.setTime(sentDate);
        sentCal.add(Calendar.DATE, 7);//前回送信日時から7日追加
        sentDate = sentCal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        if (DBG) Timber.d("isSentOneWeekBefore:NowDate:%s", sdf.format(nowDate));
        if (DBG) Timber.d("isSentOneWeekBefore:EndDate:%s", sdf.format(sentDate));
        //前回送信時から1週間以上経過した
        return nowDate.after(sentDate);
    }
}
