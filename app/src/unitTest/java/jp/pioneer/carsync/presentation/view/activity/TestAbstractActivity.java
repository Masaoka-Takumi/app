package jp.pioneer.carsync.presentation.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.ActivityComponent;
import jp.pioneer.carsync.application.di.component.TestActivityComponent;
import jp.pioneer.carsync.presentation.view.TestView;
import jp.pioneer.carsync.presentation.presenter.TestPresenter;

/**
 * AbstractActivityTest用.
 */
public class TestAbstractActivity extends AbstractActivity<TestPresenter, TestView> implements TestView {
    @Inject TestPresenter mPresenter;

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_test);
    }

    @Override
    protected void doInject(ActivityComponent activityComponent) {
        TestActivityComponent component = (TestActivityComponent) activityComponent;
        component.inject(this);
    }

    @NonNull
    @Override
    protected TestPresenter getPresenter() {
        return mPresenter;
    }
}
