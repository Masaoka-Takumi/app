package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.application.di.component.TestFragmentComponent;
import jp.pioneer.carsync.presentation.presenter.TestPresenter;
import jp.pioneer.carsync.presentation.view.TestView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * AbstractPreferenceFragmentTest用.
 */
public class TestAbstractPreferenceFragment extends AbstractPreferenceFragment<TestPresenter, TestView> implements TestView {
    @Inject TestPresenter mPresenter;

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

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public ScreenId getScreenId() {
        return null;
    }
}
