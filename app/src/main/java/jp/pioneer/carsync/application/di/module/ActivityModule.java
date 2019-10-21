package jp.pioneer.carsync.application.di.module;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import dagger.Module;
import dagger.Provides;

/**
 * ActivityComponent用のDaggerモジュール.
 */
@Module
public class ActivityModule {
    private Activity mActivity;

    /**
     * コンストラクタ.
     *
     * @param activity Activity
     */
    public ActivityModule(Activity activity) {
        mActivity = activity;
    }

    /**
     * コンストラクタ.
     * <p>
     * UnitTestでActivityを介して提供しているオブジェクトをDIしない場合用。
     * UnitTest用のActivityModuleを定義する必要が無くなるので用意した。
     */
    public ActivityModule() {
    }

    @Provides
    public FragmentManager provideFragmentManager() {
        return ((AppCompatActivity) mActivity).getSupportFragmentManager();
    }
}
