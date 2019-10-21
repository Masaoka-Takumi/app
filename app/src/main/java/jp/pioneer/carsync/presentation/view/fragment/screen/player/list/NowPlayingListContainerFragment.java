package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.NowPlayingListContainerPresenter;
import jp.pioneer.carsync.presentation.view.NowPlayingListContainerView;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.AndroidMusicFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.PlayerContainerFragment;

import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;

/**
 * NowPlayingListContainerの画面
 */

public class NowPlayingListContainerFragment extends AbstractDialogFragment<NowPlayingListContainerPresenter, NowPlayingListContainerView, AbstractDialogFragment.Callback> implements NowPlayingListContainerView {
    @Inject NowPlayingListContainerPresenter mPresenter;
    @BindView(R.id.directory_pass_text) TextView mTitle;
    private Unbinder mUnbinder;
    @Inject
    @Named(CHILD_FRAGMENT_MANAGER)
    FragmentManager mFragmentManager;

    public NowPlayingListContainerFragment() {
    }

    public static NowPlayingListContainerFragment newInstance(Bundle args) {
        NowPlayingListContainerFragment fragment = new NowPlayingListContainerFragment();
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
        View view = inflater.inflate(R.layout.fragment_now_playing_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mTitle.setText(R.string.ply_039);
        Fragment fragment = new NowPlayingListFragment();

        FragmentTransaction tr = mFragmentManager.beginTransaction();
        tr.replace(R.id.list_container, fragment);
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
        mUnbinder.unbind();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getParentFragment() != null) {
            Fragment fragment = ((PlayerContainerFragment) getParentFragment()).getFragmentInContainer();
            if(fragment instanceof AndroidMusicFragment){
                ((AndroidMusicFragment) fragment).setNowPlayBtnState();
            }
        }
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected NowPlayingListContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof Callback;
    }

    /**
     * 戻るボタン押下イベント
     */
    @OnClick(R.id.back_button)
    public void onClickBackButton() {
        this.dismiss();
    }

    /**
     * ダイアログ消去
     */
    @Override
    public void dismissDialog() {
        this.dismiss();
    }
}
