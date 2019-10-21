package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;
import javax.inject.Named;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.controller.PlayerTabFragmentController;
import jp.pioneer.carsync.presentation.presenter.UsbListContainerPresenter;
import jp.pioneer.carsync.presentation.view.UsbListContainerView;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;

import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;

/**
 * Created by NSW00_007906 on 2017/12/22.
 */

public class UsbListContainerFragment extends AbstractDialogFragment<UsbListContainerPresenter, UsbListContainerView, AbstractDialogFragment.Callback> implements UsbListContainerView {
    @Inject UsbListContainerPresenter mPresenter;
    @Inject
    @Named(CHILD_FRAGMENT_MANAGER)
    FragmentManager mFragmentManager;
    @Inject PlayerTabFragmentController mFragmentController;
    public UsbListContainerFragment() {
    }

    public static UsbListContainerFragment newInstance(Bundle args) {
        UsbListContainerFragment fragment = new UsbListContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), R.style.BehindScreenStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_generic, container, false);

        Fragment fragment = new UsbListFragment();

        FragmentTransaction tr = mFragmentManager.beginTransaction();
        tr.replace(R.id.container, fragment);
        tr.commit();
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

    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected UsbListContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof Callback;
    }

}
