package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.controller.YouTubeLinkFragmentController;
import jp.pioneer.carsync.presentation.presenter.YouTubeLinkContainerPresenter;
import jp.pioneer.carsync.presentation.view.YouTubeLinkContainerView;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

/**
 * YouTubeLinkCaution画面、YouTubeLinkWebView画面のContainer
 */
public class YouTubeLinkContainerFragment extends AbstractDialogFragment<YouTubeLinkContainerPresenter, YouTubeLinkContainerView, AbstractDialogFragment.Callback>
        implements YouTubeLinkContainerView, OnGoBackListener {

    @Inject YouTubeLinkContainerPresenter mPresenter;
    @Inject YouTubeLinkFragmentController mFragmentController;
    private Unbinder mUnbinder;

    public YouTubeLinkContainerFragment() {
    }

    public static YouTubeLinkContainerFragment newInstance(Bundle args){
        YouTubeLinkContainerFragment fragment = new YouTubeLinkContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext(), R.style.BehindScreenStyle);
        this.setCancelable(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof Callback;
    }

    @NonNull
    @Override
    protected YouTubeLinkContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean onGoBack() {
        return mFragmentController.goBack();
    }

    @Override
    public boolean onNavigate(ScreenId screenId, Bundle args) {
        return mFragmentController.navigate(screenId, args);
    }

    /**
     * キーボードを閉じる
     */
    @Override
    public void closeKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * ダイアログ終了(ラストソース復帰による間接的)
     * callbackを行った後、ラストソース復帰により画面を間接的に閉じる
     */
    @Override
    public void closeContainerDialogByChangeLastSource() {
        if(getCallback() != null){
            getCallback().onClose(this);
        }
        mPresenter.closeContainerDialogByChangeLastSource();
    }

    /**
     * ダイアログ終了(割り込み画面表示によるもの)
     * callbackを行った後、ラストソースをクリアして画面を直接閉じる
     */
    @Override
    public void closeContainerDialogResetLastSource() {
        if(getCallback() != null){
            getCallback().onClose(this);
        }
        mPresenter.closeContainerDialogResetLastSource();
    }

    /**
     * ダイアログ終了
     */
    @Override
    public void dismissDialog() {
        this.dismiss();
    }
}

