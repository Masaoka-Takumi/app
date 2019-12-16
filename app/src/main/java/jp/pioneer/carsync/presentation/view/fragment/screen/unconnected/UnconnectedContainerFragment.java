package jp.pioneer.carsync.presentation.view.fragment.screen.unconnected;

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
import jp.pioneer.carsync.presentation.controller.UnconnectedFragmentController;
import jp.pioneer.carsync.presentation.presenter.UnconnectedContainerPresenter;
import jp.pioneer.carsync.presentation.view.UnconnectedContainerView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * 非接続時画面のContainerFragment.
 */
public class UnconnectedContainerFragment extends AbstractScreenFragment<UnconnectedContainerPresenter, UnconnectedContainerView> implements UnconnectedContainerView, OnGoBackListener {
    @Inject UnconnectedContainerPresenter mPresenter;
    @Inject UnconnectedFragmentController mFragmentController;
    private Unbinder mUnbinder;

    public UnconnectedContainerFragment() {
    }

    public static UnconnectedContainerFragment newInstance(Bundle args) {
        UnconnectedContainerFragment fragment = new UnconnectedContainerFragment();
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
    protected UnconnectedContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return mFragmentController.getScreenIdInContainer();
    }

    @Override
    public boolean onGoBack() {
        if (mFragmentController.getScreenIdInContainer() == ScreenId.TIPS_WEB) {
            TipsWebFragment fragment = (TipsWebFragment) mFragmentController.getContainerFragment();
            if (fragment.mWebView.canGoBack()) {
                fragment.mWebView.goBack();
                return true;
            }
        }
        return mFragmentController.goBack();
    }

    @Override
    public boolean onNavigate(ScreenId screenId, Bundle args) {
        boolean result;

        result =  mFragmentController.navigate(screenId, args);
        ((MainActivity)getActivity()).setOrientation(screenId);
        return result;
    }

    @Override
    public void setBillingHelper() {
        ((MainActivity)getActivity()).setupBillingHelper();
    }

    @Override
    public void showAlexaAvailableConfirmDialog() {
        if(getActivity() != null) {
            ((MainActivity) getActivity()).showAlexaAvailableConfirmDialog();
        }
    }
}