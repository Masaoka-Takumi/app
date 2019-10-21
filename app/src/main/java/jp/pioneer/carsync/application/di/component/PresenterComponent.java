package jp.pioneer.carsync.application.di.component;

import dagger.Subcomponent;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.di.module.ActivityModule;
import jp.pioneer.carsync.application.di.module.FragmentModule;
import jp.pioneer.carsync.application.di.module.PresenterModule;
import jp.pioneer.carsync.application.di.module.ServiceModule;

/**
 * Presenter用のDaggerコンポーネント.
 */
@PresenterLifeCycle
@Subcomponent(modules = PresenterModule.class)
public interface PresenterComponent {
    ActivityComponent activityComponent(ActivityModule module);
    FragmentComponent fragmentComponent(FragmentModule module);
    ServiceComponent serviceComponent(ServiceModule module);
}
