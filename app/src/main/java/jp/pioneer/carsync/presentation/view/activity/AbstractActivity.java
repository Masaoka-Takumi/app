package jp.pioneer.carsync.presentation.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.di.component.ActivityComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.presentation.presenter.Presenter;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.AbstractPreferenceFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * Activityの基本クラス.
 * <p>
 * {@link Presenter}のライフサイクル関係の呼び出しを実装しており、サブクラスは{@link Presenter}の
 * サブクラスが提供するメソッドの呼び出しを行えば良いようになっている。<br>
 * {@link #setContentView(int)}が必要な{@link #onCreate(Bundle)}はプレゼンターの処理を行う都合上、
 * {@link #doCreate(Bundle)}で行うようになっている。<br>
 * 基本的に本クラスでオーバーライドしているメソッドをサブクラスでオーバーライドすることはない。
 *
 * @param <P> プレゼンター
 * @param <V> ビュー
 * @see AbstractScreenFragment
 * @see AbstractDialogFragment
 * @see AbstractPreferenceFragment
 */
public abstract class AbstractActivity<P extends Presenter<V>, V> extends AppCompatActivity {
    private ActivityComponent mActivityComponent;

    /**
     * {@inheritDoc}
     * <p>
     * プレゼンターの初期化の都合のため、サブクラスは本メソッドをオーバーライドせずに、
     * {@link #doCreate(Bundle)}で{@link #onCreate(Bundle)} 相当の処理を行うこと。
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityComponent = createActivityComponent();
        doInject(mActivityComponent);
        doCreate(savedInstanceState);
        //noinspection unchecked
        getPresenter().takeView((V) this);
        if (savedInstanceState == null) {
            getPresenter().initialize();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        getPresenter().restoreInstanceState(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        getPresenter().resume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        getPresenter().pause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getPresenter().saveInstanceState(outState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().dropView();
        if (isFinishing()) {
            getPresenter().destroy();
            releasePresenterComponent();
        }
    }

    /**
     * インジェクション実施.
     * <p>
     * {@link ActivityComponent}にサブクラス用のinjectメソッドを定義しインジェクションを行う。
     *
     * @param activityComponent ActivityComponent
     */
    protected abstract void doInject(ActivityComponent activityComponent);

    /**
     * 生成実施.
     * <p>
     * {@link #setContentView(int)}は本メソッドで行う。
     *
     * @param savedInstanceState Activity復元の場合は{@link #onSaveInstanceState}で設定した{@link Bundle}。
     *                           それ以外の場合はnull。
     */
    protected abstract void doCreate(@Nullable Bundle savedInstanceState);

    /**
     * プレゼンター取得.
     * <p>
     * イレイジャのためプレゼンターをインジェクション出来ないので、プレゼンターの生成はサブクラスの責務となっている。
     * ただし、プレゼンターのライフサイクルはActivityのライフサイクルと異なるため、サブクラスでプレゼンターをnewで
     * 生成してはいけない。<br>
     * プレゼンターに{@link PresenterLifeCycle}アノテーションを付与し、{@link #doInject(ActivityComponent)}で
     * インジェクションすること。
     *
     * @return サブクラスのプレゼンター
     */
    @NonNull
    protected abstract P getPresenter();

    private ComponentFactory getComponentFactory() {
        return App.getApp(this).getComponentFactory();
    }

    private ActivityComponent createActivityComponent() {
        return getComponentFactory().createActivityComponent(getPresenterComponent(), this);
    }

    private PresenterComponent getPresenterComponent() {
        return getComponentFactory().getPresenterComponent(App.getApp(this).getAppComponent(), getClass());
    }

    private void releasePresenterComponent() {
        getComponentFactory().releasePresenterComponent(getClass());
    }
}
