package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.di.FragmentLifeCycle;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.presentation.presenter.Presenter;
import jp.pioneer.carsync.presentation.view.activity.AbstractActivity;

/**
 * ダイアログ（DialogFragment）の基本クラス.
 * <p>
 * {@link AbstractActivity}の{@link DialogFragment}版。
 * {@link View}の生成が{@link #onCreateDialog(Bundle)}や{@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
 * となるため、{@link AbstractActivity#doCreate(Bundle)}に当たるものは存在しない。<br>
 * ダイアログは、通常呼び出し元へのコールバックを伴うため、コールバックインターフェースを指定するようになっており、
 * その取得とサブクラスへの提供{@link #getCallback()}を本クラスで行っている。
 *
 * @param <P> プレゼンター
 * @param <V> ビュー
 * @param <C> コールバック
 */
public abstract class AbstractDialogFragment<P extends Presenter<V>, V, C> extends DialogFragment {
    private boolean isPauseHide = false;
    /**
     * 終了通知リスナー
     */
    public interface Callback {
        /**
         * ダイアログ終了通知
         *
         * @param fragment ダイアログインスタンス
         */
        void onClose(AbstractDialogFragment fragment);
    }

    private FragmentComponent mFragmentComponent;
    private C mCallback;

    public void setPauseHide(boolean pauseHide) {
        isPauseHide = pauseHide;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
        if(getDialog()!=null){
            getDialog().show();
        }
        getPresenter().resume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        //Background時にダイアログがちらつくため、pauseで非表示にする
        if(getDialog()!=null&&!isPauseHide) {
            getDialog().hide();
        }
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
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onAttach(Context context) {
        super.onAttach(context);
        Object callback = getTargetFragment();
        if (callback == null) {
            callback = getActivity();
        }

        if (isInstanceOfCallback(callback)) {
            //noinspection unchecked
            mCallback = (C) callback;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    /**
     * コールバック取得.
     * <p>
     * ダイアログのコールバックインターフェース
     *
     * @return コールバック
     */
    protected C getCallback() {
        return mCallback;
    }

    /**
     * コールバック設定.
     * <p>
     * サブクラスをUnitTestする際、呼び出し元にコールバックインターフェスの実装を行うのが面倒なため、
     * コールバックのインスタンスを差し替え可能にしている。<br>
     * 言うまでもなくUniTest用のため、プロダクションのコードで使用しないこと。
     *
     * @param callback コールバック
     */
    @VisibleForTesting
    public void setCallback(C callback) {
        mCallback = callback;
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
     * コールバックのインスタンスか否か取得.
     * <p>
     * イレイジャのため、型判定を止む無くサブクラスで行うようになっている。
     * <pre>{@code
     *  return callback instanceof コールバックの型;
     * }</pre>
     * という単純な実装で良い。
     *
     * @param callback コールバック
     * @return {@code true}:{@code callback}はコールバック（{@code C}）のインスタンスである。｛@code false}:それ以外。
     */
    protected abstract boolean isInstanceOfCallback(Object callback);

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
