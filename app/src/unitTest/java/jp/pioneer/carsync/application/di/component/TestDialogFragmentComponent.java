package jp.pioneer.carsync.application.di.component;

import dagger.Subcomponent;
import jp.pioneer.carsync.application.di.FragmentLifeCycle;
import jp.pioneer.carsync.application.di.module.FragmentModule;
import jp.pioneer.carsync.presentation.view.fragment.dialog.TestAbstractDialogFragment;

/**
 * UnitTest用.
 */
@FragmentLifeCycle
@Subcomponent(modules = {
        FragmentModule.class,
})
public interface TestDialogFragmentComponent extends FragmentComponent {
    void inject(TestAbstractDialogFragment fragment);
}
