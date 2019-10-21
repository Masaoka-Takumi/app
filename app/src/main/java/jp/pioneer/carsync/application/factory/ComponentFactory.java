package jp.pioneer.carsync.application.factory;

import android.app.Activity;
import android.app.Service;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.HashMap;
import java.util.Map;

import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.di.component.ActivityComponent;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.di.component.ServiceComponent;
import jp.pioneer.carsync.application.di.module.ActivityModule;
import jp.pioneer.carsync.application.di.module.CarRemoteSessionModule;
import jp.pioneer.carsync.application.di.module.FragmentModule;
import jp.pioneer.carsync.application.di.module.PresenterModule;
import jp.pioneer.carsync.application.di.module.ServiceModule;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Componentファクトリ.
 * <p>
 * dagger2のComponentを生成する。
 * UnitTest時は生成するComponentを差し替えるため、ファクトリ化している。
 *
 * @see App#setComponentFactory(ComponentFactory)
 */
public class ComponentFactory {
    private Map<Class<?>, PresenterComponent> mPresenterComponents = new HashMap<>();

    /**
     * PresenterComponent取得.
     * <p>
     * 画面回転時や環境変更時（Configuration Change）に破棄されれないようにするため、
     * 本クラスでキャッシュしている。
     * 画面破棄時に{@link #releasePresenterComponent(Class)}を呼び出すこと。
     *
     * @param appComponent AppComponent
     * @param clazz PresenterComponentを保持するクラス（Activity or Fragmentを想定するが型チェックは行わない）
     * @return PresenterComponent
     * @throws NullPointerException {@code appComponent}、または、{@code clazz}がnull
     * @see #releasePresenterComponent(Class)
     */
    @NonNull
    public PresenterComponent getPresenterComponent(@NonNull AppComponent appComponent, Class<?> clazz) {
        checkNotNull(appComponent);
        checkNotNull(clazz);
        return obtain(appComponent, clazz);
    }

    /**
     * PresenterComponent解放.
     * <p>
     * {@code clazz}に関連するPresenterComponentのキャッシュをクリアする。
     *
     * @param clazz PresenterComponentを保持するクラス（Activity or Fragmentを想定するが型チェックは行わない）
     * @throws NullPointerException {@code clazz}がnull
     * @see #getPresenterComponent(AppComponent, Class)
     */
    public void releasePresenterComponent(@NonNull Class<?> clazz) {
        checkNotNull(clazz);
        mPresenterComponents.remove(clazz);
    }

    /**
     * ActivityComponent生成.
     *
     * @param presenterComponent PresenterComponent
     * @param activity Activity
     * @return ActivityComponent
     * @throws NullPointerException {@code presenterComponent}か{@code activity}がnull
     */
    @NonNull
    public ActivityComponent createActivityComponent(@NonNull PresenterComponent presenterComponent, @NonNull Activity activity) {
        checkNotNull(presenterComponent);
        checkNotNull(activity);
        return presenterComponent.activityComponent(new ActivityModule(activity));
    }

    /**
     * FragmentComponent生成.
     *
     * @param presenterComponent PresenterComponent
     * @param fragment Fragment
     * @return FragmentComponent
     * @throws NullPointerException {@code presenterComponent}か{@code fragment}がnull
     */
    @NonNull
    public FragmentComponent createFragmentComponent(@NonNull PresenterComponent presenterComponent, @NonNull Fragment fragment) {
        checkNotNull(presenterComponent);
        checkNotNull(fragment);
        return presenterComponent.fragmentComponent(new FragmentModule(fragment));
    }

    /**
     * ServiceComponent生成.
     *
     * @param presenterComponent PresenterComponent
     * @param service Service
     * @return ServiceComponent
     * @throws NullPointerException {@code presenterComponent}か{@code service}がnull
     */
    @NonNull
    public ServiceComponent createServiceComponent(@NonNull PresenterComponent presenterComponent, @NonNull Service service) {
        checkNotNull(presenterComponent);
        checkNotNull(service);
        return presenterComponent.serviceComponent(new ServiceModule());
    }

    /**
     * CarRemoteSessionComponent生成.
     *
     * @param appComponent AppComponent
     * @param session CarRemoteSession
     * @return CarRemoteSessionComponent
     * @throws NullPointerException {@code appComponent}、または、{@code session}がnull
     */
    @NonNull
    public CarRemoteSessionComponent createCarRemoteProtocolComponent(@NonNull AppComponent appComponent, @NonNull CarRemoteSession session) {
        checkNotNull(appComponent);
        checkNotNull(session);
        return appComponent.carRemoteProtocolComponent(new CarRemoteSessionModule(session));
    }

    @NonNull
    private PresenterComponent obtain(@NonNull AppComponent appComponent, @NonNull Class<?> clazz) {
        PresenterComponent presenter;
        if (mPresenterComponents.containsKey(clazz)) {
            presenter = mPresenterComponents.get(clazz);
        } else {
            presenter = appComponent.presenterComponent(new PresenterModule());
            mPresenterComponents.put(clazz, presenter);
        }

        return presenter;
    }
}
