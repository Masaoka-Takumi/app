package jp.pioneer.carsync.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.greenrobot.eventbus.EventBus;
import org.matthiaszimmermann.location.egm96.Geoid;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.content.FlurryAnalyticsToolStrategy;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.di.component.DaggerAppComponent;
import jp.pioneer.carsync.application.di.module.AppModule;
import jp.pioneer.carsync.application.event.AppStateChangeEvent;
import jp.pioneer.carsync.application.event.AppStateChangeEvent.AppState;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.application.handler.CarsyncUncaughtExceptionHandler;
import jp.pioneer.carsync.domain.DomainInitializer;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.util.PresetChannelDictionary;
import jp.pioneer.carsync.infrastructure.InfrastructureInitializer;
import jp.pioneer.carsync.presentation.presenter.MainPresenter;
import jp.pioneer.carsync.presentation.util.RadioStationNameUtil;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * アプリケーションクラス.
 */
public class App extends Application {
    /** 利用規約最新バージョン（13言語分）. */
    public static final Map<String,Integer> EULA_PRIVACY_NEW_VERSION = new HashMap<String,Integer>() {
        {
            put("ar",5);
            put("de",5);
            put("en-us",5);
            put("es-mx",5);
            put("fa",5);
            put("fr",5);
            put("it",5);
            put("jp",6);
            put("nl",5);
            put("pt",5);
            put("pt-br",5);
            put("ru",5);
            put("zh-tw",5);
        }
    };
    private AppComponent mAppComponent;
    private ComponentFactory mComponentFactory;
    private ActivityLifecycleCallbacksImpl mMyActivityLifecycleCallbacks = new ActivityLifecycleCallbacksImpl();
    @Inject DomainInitializer mDomainInitializer;
    @Inject InfrastructureInitializer mInfrastructureInitializer;
    @Inject ServiceControlManager mServiceControlManager;
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mStatusCase;
    @Inject AnalyticsEventManager mAnalytics;
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        initialize();
        startAnalytics();
        setDefaultUncaughtExceptionHandler();
        registerActivityLifecycleCallbacks(mMyActivityLifecycleCallbacks);
    }

    /**
     * App取得.
     *
     * @param context Context
     * @return App
     * @throws NullPointerException {@code context}がnull
     */
    @NonNull
    public static App getApp(@NonNull Context context) {
        return (App) checkNotNull(context).getApplicationContext();
    }

    /**
     * AppComponent取得.
     *
     * @return AppComponent
     */
    @NonNull
    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    /**
     * ComponentFactory取得.
     *
     * @return ComponentFactory
     */
    @NonNull
    public ComponentFactory getComponentFactory() {
        return mComponentFactory;
    }

    /**
     * AppComponent設定.
     * <p>
     * UnitTest用
     *
     * @param appComponent AppComponent
     * @throws NullPointerException {@code appComponent}がnull
     */
    @VisibleForTesting
    public void setAppComponent(@NonNull AppComponent appComponent) {
        mAppComponent = appComponent;
    }

    /**
     * ComponentFactory設定.
     * <p>
     * UnitTest用
     *
     * @param factory ComponentFactory
     * @throws NullPointerException {@code factory}がnull
     */
    @VisibleForTesting
    public void setComponentFactory(@NonNull ComponentFactory factory) {
        mComponentFactory = factory;
    }

    /**
     * 初期化.
     * <p>
     * UnitTest時は本メソッドをオーバーライドして不必要な初期化を抑止する。
     */
    @VisibleForTesting
    void initialize() {
        setComponentFactory(new ComponentFactory());
        setAppComponent(DaggerAppComponent
                .builder()
                .appModule(new AppModule(this))
                .build());
        getAppComponent().inject(this);

        //連携抑制フラグ初期化前に利用規約同意フラグを更新する必要がある。
        String language = getApplicationContext().getResources().getString(R.string.url_001);
        int savedVersionCodeEula = mPreference.getEulaPrivacyVersionCode(language);
        int newVersionCodeEula = 0;
        Integer newVersionCodeEuraInteger = EULA_PRIVACY_NEW_VERSION.get(language);
        if (newVersionCodeEuraInteger != null) {
            newVersionCodeEula = newVersionCodeEuraInteger;
        }
        Timber.d("language=" + language + ", savedVersionCodeEula=" + savedVersionCodeEula + ", newVersionCodeEula=" + newVersionCodeEula);
        if (savedVersionCodeEula < newVersionCodeEula) {
            mPreference.setAgreedEulaPrivacyPolicy(false);
        }
        if (mPreference.isAdasBillingRecord()) {
            mStatusCase.execute().getAppStatus().deviceConnectionSuppress = true;
            mStatusCase.execute().getAppStatus().adasBillingCheck = false;
        } else if((MainPresenter.sIsVersionQ && !Settings.canDrawOverlays(getApplicationContext())) || !mPreference.isAgreedEulaPrivacyPolicy() || isAlexaAvailableConfirmNeeded()){
            mStatusCase.execute().getAppStatus().deviceConnectionSuppress = true;
            mStatusCase.execute().getAppStatus().adasBillingCheck = true;
        } else {
            mStatusCase.execute().getAppStatus().deviceConnectionSuppress = false;
            mStatusCase.execute().getAppStatus().adasBillingCheck = true;
        }
        mInfrastructureInitializer.initialize();
        mDomainInitializer.initialize();
        mServiceControlManager.initialize();

        //高度表示改善対応
        AltitudeInitTask mAsyncTask = new AltitudeInitTask();
        mAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        RadioStationNameUtil.init(getApplicationContext());
    }

    /**
     * Flurry開始.
     * UnitTest用
     */
    @VisibleForTesting
    public void startAnalytics(){
        mAnalytics.configure(new FlurryAnalyticsToolStrategy());
        if(mPreference.isAgreedEulaPrivacyPolicy()){
            mAnalytics.startSession(getApplicationContext());
        }
    }

    private void setDefaultUncaughtExceptionHandler(){
        if (BuildConfig.DEBUG) {
            final Thread.UncaughtExceptionHandler savedUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
            if(savedUncaughtExceptionHandler != null) {
                if (!(savedUncaughtExceptionHandler instanceof CarsyncUncaughtExceptionHandler)) {
                    Thread.setDefaultUncaughtExceptionHandler(new CarsyncUncaughtExceptionHandler(savedUncaughtExceptionHandler));
                }
            }
        }
    }

    /**
     * アプリケーションがフォアグラウンドか否か取得.
     *
     * @return {@code true}:フォアグラウンドである。{@code false}:それ以外。
     */
    public boolean isForeground() {
        return mMyActivityLifecycleCallbacks.mResumed >= 1;
    }

    /**
     * アプリケーションが起動したか否か取得.
     *
     * @return {@code true}:起動済である。{@code false}:それ以外。
     */
    public boolean isCreated() {
        return mMyActivityLifecycleCallbacks.mCreated >= 1;
    }
    /**
     * アプリケーション状態監視用ActivityLifecycleコールバック.
     */
    public class ActivityLifecycleCallbacksImpl implements ActivityLifecycleCallbacks {
        private int mStarted = 0;
        private int mResumed = 0;
        private int mCreated = 0;
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            ++mCreated;
        }

        @Override
        public void onActivityStarted(Activity activity) {
            ++mStarted;
            if (mStarted == 1) {
                mEventBus.post(new AppStateChangeEvent(AppState.STARTED));
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            ++mResumed;
            if (mResumed == 1) {
                mEventBus.post(new AppStateChangeEvent(AppState.RESUMED));
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            --mResumed;
            if (mResumed == 0) {
                mEventBus.post(new AppStateChangeEvent(AppState.PAUSED));
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            --mStarted;
            if (mStarted == 0) {
                mEventBus.post(new AppStateChangeEvent(AppState.STOPPED));
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            // nothing to do
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            --mCreated;
        }
    }

    // ジオイド高ライブラリ初期化非同期処理クラス
    static class AltitudeInitTask extends AsyncTask<Void, Void, Void>{

        /**
         * コンストラクタ
         */
        public AltitudeInitTask() {
            super();
        }

        /**
         * バックグランドで行う処理
         */
        @Override
        protected Void  doInBackground(Void... value) {
            Timber.d("Geoid.init()");
            Geoid.init();
            return null;
        }

        /**
         * バックグランド処理が完了し、UIスレッドに反映する
         */
        @Override
        protected void onPostExecute(Void result) {
        }
    }

    /**
     * Alexa機能利用可能ダイアログを出すべきかどうかの判定
     * TODO #5244 MainPresenterの同名メソッドと共通化したい
     * @return
     * {@code true}:Alexa機能が利用可能かつAlexa機能利用可能ダイアログを未表示
     * {@code false}:それ以外(Alexa機能が利用不可能またはAlexa機能利用可能ダイアログを表示済み)
     */
    private boolean isAlexaAvailableConfirmNeeded() {
        return mStatusCase.execute().getAppStatus().isAlexaAvailableCountry && !mPreference.isAlexaAvailableConfirmShowed();
    }

}
