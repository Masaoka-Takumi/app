package jp.pioneer.carsync.application.di.component;

import dagger.Subcomponent;
import jp.pioneer.carsync.application.di.ActivityLifeCycle;
import jp.pioneer.carsync.application.di.module.ActivityModule;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;

/**
 * Activity用のDaggerコンポーネント.
 */
@ActivityLifeCycle
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {
    void inject(MainActivity activity);
}
