package jp.pioneer.carsync.application.di.module;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.VisibleForTesting;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.content.AnalyticsSharedPreference;
import jp.pioneer.carsync.application.content.AppSharedPreference;

import static android.content.Context.MODE_PRIVATE;

/**
 * AppComponent用のDaggerモジュール.
 * <p>
 * Application全体向けはこのクラスに定義する。
 *
 * @see DomainModule
 * @see InfrastructureModule
 * @see InfrastructureBindsModule
 */
@Module
public class AppModule {
    private App mApp;

    /**
     * コンストラクタ.
     *
     * @param app App
     */
    public AppModule(App app) {
        mApp = app;
    }

    /**
     * コンストラクタ.
     * <p>
     * UnitTestでAppやAppを介して提供しているオブジェクトをDIしない場合用。
     * UnitTest用のAppModuleを定義する必要が無くなるので用意した。
     * 以下で生成出来る。（自動でnew AppModule()してくれる）
     * <pre>{@code
     *  TestAppComponent appComponent = DaggerXXX_TestAppComponent.builder().build();
     * }</pre>
     */
    @VisibleForTesting
    public AppModule() {
    }

    @Singleton
    @Provides
    public App provideApp() {
        return mApp;
    }

    @Singleton
    @Provides
    public Context provideApplicationContext(App app) {
        return app.getApplicationContext();
    }

    @Provides
    public PackageManager providePackageManager(Context context) {
        return context.getPackageManager();
    }

    @Provides
    public Resources getResources(Context context) {
        return context.getResources();
    }

    @Singleton
    @Provides
    public EventBus provideEventBus() {
        return EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).build();
    }

    @Singleton
    @Provides
    public AppSharedPreference provideAppSharedPreference(App app) {
        return new AppSharedPreference(app.getSharedPreferences(app.getPackageName() + "_preferences", MODE_PRIVATE));
    }

    @Singleton
    @Provides
    public AnalyticsSharedPreference provideAnalyticsSharedPreference(App app) {
        return new AnalyticsSharedPreference(app.getSharedPreferences(app.getPackageName() + "_analytics_preferences", MODE_PRIVATE));
    }

    @Provides
    public Handler provideMainLooper() {
        return new Handler(Looper.getMainLooper());
    }
}
