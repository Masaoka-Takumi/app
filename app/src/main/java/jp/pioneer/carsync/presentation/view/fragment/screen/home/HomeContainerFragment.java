package jp.pioneer.carsync.presentation.view.fragment.screen.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.controller.HomeFragmentController;
import jp.pioneer.carsync.presentation.presenter.HomeContainerPresenter;
import jp.pioneer.carsync.presentation.view.HomeContainerView;
import jp.pioneer.carsync.presentation.view.fragment.OnExitSettingListener;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * Created by BP06566 on 2017/03/02.
 */

public class HomeContainerFragment extends AbstractScreenFragment<HomeContainerPresenter, HomeContainerView>
        implements HomeContainerView, OnGoBackListener, OnExitSettingListener{
    @Inject HomeContainerPresenter mPresenter;
    @Inject HomeFragmentController mFragmentController;
    private Unbinder mUnbinder;

    public HomeContainerFragment() {
    }

    public static HomeContainerFragment newInstance(Bundle args) {
        HomeContainerFragment fragment = new HomeContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_generic, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mFragmentController.setContainerViewId(R.id.container);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected HomeContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return mFragmentController.getScreenIdInContainer();
    }

    @Override
    public boolean onGoBack() {
        return mFragmentController.goBack();
    }

    @Override
    public boolean onExitSetting() {
        return mFragmentController.exitSetting();
    }

    @Override
    public boolean onNavigate(ScreenId screenId, Bundle args) {
        return mFragmentController.navigate(screenId, args);
    }

}