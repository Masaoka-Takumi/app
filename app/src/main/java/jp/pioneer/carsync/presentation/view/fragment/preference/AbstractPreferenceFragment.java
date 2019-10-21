package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.di.FragmentLifeCycle;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.presentation.presenter.Presenter;
import jp.pioneer.carsync.presentation.view.activity.AbstractActivity;
import jp.pioneer.carsync.presentation.view.fragment.Screen;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * プリファレンス（PreferenceFragment）の基本クラス.
 * <p>
 * {@link AbstractActivity}の{@link PreferenceFragmentCompat}版。
 * {@link View}の生成が{@link #onCreatePreferences(Bundle, String)}となるため、
 * {@link AbstractActivity#doCreate(Bundle)}に当たるものは存在しない。<br>
 *
 * @param <P> プレゼンター
 * @param <V> ビュー
 * @see AbstractActivity
 * @see AbstractScreenFragment
 * @see AbstractDialogFragment
 */
public abstract class AbstractPreferenceFragment<P extends Presenter<V>, V> extends PreferenceFragmentCompat
        implements Screen {
    private FragmentComponent mFragmentComponent;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentComponent = createFragmentComponent();
        doInject(mFragmentComponent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //noinspection unchecked
        getPresenter().takeView((V) this);
        if (savedInstanceState == null) {
            getPresenter().initialize();
        } else {
            getPresenter().restoreInstanceState(savedInstanceState);
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        getPresenter().dropView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRemoving() || getActivity().isFinishing()) {
            getPresenter().destroy();
            releasePresenterComponent();
        }
    }

    /**
     * {@link ActionBar}取得.
     * <p>
     * コンビニエンスメソッド。
     *
     * @return ActionBar
     */
    protected ActionBar getSupportActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    /**
     * インジェクション実施.
     * <p>
     * {@link FragmentComponent}にサブクラス用のinjectメソッドを定義しインジェクションを行う。
     *
     * @param fragmentComponent FragmentComponent
     */
    protected abstract void doInject(FragmentComponent fragmentComponent);

    /**
     * プレゼンター取得.
     * <p>
     * イレイジャのためプレゼンターをインジェクション出来ないので、プレゼンターの生成はサブクラスの責務となっている。
     * ただし、プレゼンターのライフサイクルはFragmentのライフサイクルと異なるため、サブクラスでプレゼンターをnewで
     * 生成してはいけない。<br>
     * プレゼンターに{@link FragmentLifeCycle}アノテーションを付与し、{@link #doInject(FragmentComponent)}で
     * インジェクションすること。
     *
     * @return サブクラスのプレゼンター
     */
    @NonNull
    protected abstract P getPresenter();

    private ComponentFactory getComponentFactory() {
        return App.getApp(getContext()).getComponentFactory();
    }

    private FragmentComponent createFragmentComponent() {
        return getComponentFactory().createFragmentComponent(getPresenterComponent(), this);
    }

    private PresenterComponent getPresenterComponent() {
        return getComponentFactory().getPresenterComponent(App.getApp(getContext()).getAppComponent(), getClass());
    }

    private void releasePresenterComponent() {
        getComponentFactory().releasePresenterComponent(getClass());
    }
}
