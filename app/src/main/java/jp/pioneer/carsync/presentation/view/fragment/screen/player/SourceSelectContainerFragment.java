package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.app.Dialog;
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
import jp.pioneer.carsync.presentation.controller.SourceSelectFragmentController;
import jp.pioneer.carsync.presentation.presenter.SourceSelectContainerPresenter;
import jp.pioneer.carsync.presentation.view.SourceSelectContainerView;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;

/**
 * Source Select Containerの画面
 */

public class SourceSelectContainerFragment extends AbstractDialogFragment<SourceSelectContainerPresenter, SourceSelectContainerView, AbstractDialogFragment.Callback>
        implements SourceSelectContainerView, OnGoBackListener {

    @Inject SourceSelectContainerPresenter mPresenter;
    @Inject SourceSelectFragmentController mFragmentController;
    private Unbinder mUnbinder;

    public SourceSelectContainerFragment() {
    }

    public static SourceSelectContainerFragment newInstance(Bundle args) {
        SourceSelectContainerFragment fragment = new SourceSelectContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.BehindScreenStyle);
        setCancelable(false);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_generic, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mFragmentController.setContainerViewId(R.id.container);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogFragmentAnimation;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected SourceSelectContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof Callback;
    }

    @Override
    public boolean onGoBack() {
        return mFragmentController.goBack();
    }

    @Override
    public boolean onNavigate(ScreenId screenId, Bundle args) {
        return mFragmentController.navigate(screenId, args);
    }
}
