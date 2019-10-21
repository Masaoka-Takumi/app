package jp.pioneer.carsync.application.di.component;

import dagger.Subcomponent;
import jp.pioneer.carsync.application.di.FragmentLifeCycle;
import jp.pioneer.carsync.application.di.module.FragmentModule;
import jp.pioneer.carsync.presentation.view.fragment.preference.TestAbstractPreferenceFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.TestAbstractScreenFragment;

/**
 * UnitTest用.
 */
@FragmentLifeCycle
@Subcomponent(modules = {
        FragmentModule.class,
})
public interface TestFragmentComponent extends FragmentComponent {
    void inject(TestAbstractScreenFragment fragment);
    void inject(TestAbstractPreferenceFragment fragment);
}
