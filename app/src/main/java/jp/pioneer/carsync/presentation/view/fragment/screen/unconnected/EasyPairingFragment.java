package jp.pioneer.carsync.presentation.view.fragment.screen.unconnected;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.EasyPairingPresenter;
import jp.pioneer.carsync.presentation.view.EasyPairingView;
import jp.pioneer.carsync.presentation.view.adapter.EasyPairingAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * 簡単ペアリング画面.
 */
public class EasyPairingFragment extends AbstractScreenFragment<EasyPairingPresenter, EasyPairingView> implements EasyPairingView {
    @Inject EasyPairingPresenter mPresenter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public EasyPairingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return EasyPairingFragment
     */
    public static EasyPairingFragment newInstance(Bundle args) {
        EasyPairingFragment fragment = new EasyPairingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_easy_pairing, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArgument(getArguments());

        view.setFocusableInTouchMode(true);
        view.requestFocus();

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
    protected EasyPairingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.EASY_PAIRING;
    }

    @OnClick(R.id.add_device_button)
    public void onClickAddDeviceButton() {
        getPresenter().showPairingSelectDialog();
    }

    @OnClick(R.id.back_btn)
    public void onClickBackButton() {
        getPresenter().onBackAction();
    }


}
