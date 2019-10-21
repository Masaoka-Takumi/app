package jp.pioneer.carsync.application.di.component;

import dagger.Subcomponent;
import jp.pioneer.carsync.application.di.ActivityLifeCycle;
import jp.pioneer.carsync.application.di.module.ActivityModule;
import jp.pioneer.carsync.presentation.view.activity.TestAbstractActivity;

/**
 * UnitTest用.
 */
@ActivityLifeCycle
@Subcomponent(modules = {
        ActivityModule.class
})
public interface TestActivityComponent extends ActivityComponent {
    void inject(TestAbstractActivity activity);
}
