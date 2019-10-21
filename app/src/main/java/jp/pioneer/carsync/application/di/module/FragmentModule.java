package jp.pioneer.carsync.application.di.module;

import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * FragmentComponent用のDaggerモジュール.
 */
@Module
public class FragmentModule {
    /**
     * {@link Fragment#getChildFragmentManager()}の{@link FragmentManager}をDIしたい場合に使用するNamedアノテーション名.
     * <p>
     * アノテーションを付与しなければ、{@link Fragment#getFragmentManager()}の{@link FragmentManager}となる。<br>
     * {@link Fragment#getChildFragmentManager()}の{@link FragmentManager}をDI:
     * <pre>
     *   &#064;Inject &#064;Named(CHILD_FRAGMENT_MANAGER) FragmentManager mFragmentManager;
     * </pre>
     * {@link Fragment#getFragmentManager()} ()}の{@link FragmentManager}をDI:
     * <pre>
     *   &#064;Inject FragmentManager mFragmentManager;
     * </pre>
     */
    public static final String CHILD_FRAGMENT_MANAGER = "child_manager";
    private Fragment mFragment;

    /**
     * コンストラクタ.
     *
     * @param fragment Fragment
     */
    public FragmentModule(Fragment fragment) {
        mFragment = fragment;
    }

    /**
     * コンストラクタ.
     * <p>
     * UnitTestでFragmentを介して提供しているオブジェクトをDIしない場合用。
     * UnitTest用のFragmentModuleを定義する必要が無くなるので用意した。
     */
    @VisibleForTesting
    public FragmentModule() {
    }

    @Provides
    public FragmentManager provideFragmentManager() {
        return mFragment.getFragmentManager();
    }

    @Provides
    @Named(CHILD_FRAGMENT_MANAGER)
    public FragmentManager provideChildFragmentManager() {
        return mFragment.getChildFragmentManager();
    }
}
