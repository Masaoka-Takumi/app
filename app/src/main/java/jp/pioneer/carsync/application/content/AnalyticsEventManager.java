package jp.pioneer.carsync.application.content;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.application.event.AppStateChangeEvent;
import jp.pioneer.carsync.application.util.Stopwatch;
import jp.pioneer.carsync.domain.event.AppMusicAudioModeChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.presentation.event.MainNavigateEvent;
import jp.pioneer.carsync.presentation.event.SourceChangeReasonEvent;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

public class AnalyticsEventManager {
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    private static final Analytics sAnalytics = Analytics.getInstance();
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
    public void createObserver() {
        observers.add(new ActiveSourceObserver());
        observers.add(new UIOrientationObserver());
        observers.add(new ActiveScreenObserver());
        observers.add(new SourceSelectActionObserver());
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
}
