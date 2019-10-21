package jp.pioneer.carsync.presentation.view.fragment.screen;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.application.di.component.TestFragmentComponent;
import jp.pioneer.carsync.presentation.presenter.TestPresenter;
import jp.pioneer.carsync.presentation.view.TestView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * AbstractScreenFragment用.
 */
public class TestAbstractScreenFragment extends AbstractScreenFragment<TestPresenter, TestView> implements TestView {
    @Inject TestPresenter mPresenter;

    @Override
    public ScreenId getScreenId() {
        return null;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        TestFragmentComponent component = (TestFragmentComponent) fragmentComponent;
        component.inject(this);
    }

    @NonNull
    @Override
    protected TestPresenter getPresenter() {
        return mPresenter;
    }
}
