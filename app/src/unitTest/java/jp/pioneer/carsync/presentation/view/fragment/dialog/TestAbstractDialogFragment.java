package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.application.di.component.TestDialogFragmentComponent;
import jp.pioneer.carsync.presentation.presenter.TestDialogPresenter;
import jp.pioneer.carsync.presentation.view.TestDialogView;

/**
 * AbstractDialogFragmentTest用.
 */
public class TestAbstractDialogFragment extends AbstractDialogFragment<TestDialogPresenter, TestDialogView, TestDialogView.Callback>
        implements TestDialogView {
    @Inject TestDialogPresenter mPresenter;

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        TestDialogFragmentComponent component = (TestDialogFragmentComponent) fragmentComponent;
        component.inject(this);
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof TestDialogView.Callback;
    }

    @NonNull
    @Override
    protected TestDialogPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void doCallback() {
        getCallback().onCallback();
    }
}
