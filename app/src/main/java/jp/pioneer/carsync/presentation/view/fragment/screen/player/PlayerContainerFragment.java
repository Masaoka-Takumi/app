package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.controller.PlayerFragmentController;
import jp.pioneer.carsync.presentation.presenter.PlayerContainerPresenter;
import jp.pioneer.carsync.presentation.view.PlayerContainerView;
import jp.pioneer.carsync.presentation.view.fragment.OnCloseDialogListener;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.OnShowDialogListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * ローカルコンテンツ再生 コンテナの画面
 */

public class PlayerContainerFragment extends AbstractScreenFragment<PlayerContainerPresenter, PlayerContainerView>
        implements PlayerContainerView, OnGoBackListener, OnShowDialogListener, OnCloseDialogListener {
    /** メッセージ表示のディレイ時間. */
    protected static final int MESSAGE_DELAY_TIME = 1500;
    public static final String KEY_HOME_SOURCE_OFF = "home_source_off";
    @Inject PlayerContainerPresenter mPresenter;
    @Inject PlayerFragmentController mFragmentController;
    @BindView(R.id.src_message) ConstraintLayout mSrcMessage;
    @BindView(R.id.fx_eq_message_text) TextView mFxEqMessageText;
    @BindView(R.id.fx_eq_message_text_white) TextView mFxEqMessageTextWhite;
    @BindView(R.id.message_line) ImageView mMassageLine;
    @BindView(R.id.message_line_white) ImageView mMassageLineWhite;
    private Unbinder mUnbinder;
    private final Handler mHandler = new Handler();
    private Runnable mDelayMessageFunc = new Runnable() {
        @Override
        public void run() {
            if(mSrcMessage!=null)
                mSrcMessage.setVisibility(View.INVISIBLE);
        }
    };
    /*
     * コンストラクタ
     */
    public PlayerContainerFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return PlayerContainerFragment
     */
    public static PlayerContainerFragment newInstance(Bundle args) {
        PlayerContainerFragment fragment = new PlayerContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_player, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArgument(getArguments());
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
    protected PlayerContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return mFragmentController.getScreenIdInContainer();
    }

    public Fragment getFragmentInContainer(){return mFragmentController.getFragmentInContainer();}

    /**
     * 遷移元画面に戻る
     *
     * @return boolean
     */
    @Override
    public boolean onGoBack() {
        return mFragmentController.goBack();
    }

    /**
     * 指定した画面IDに遷移
     *
     * @param screenId ScreenId
     * @param args     　Bundle
     * @return boolean
     */
    @Override
    public boolean onNavigate(ScreenId screenId, Bundle args) {
        return mFragmentController.navigate(screenId, args);
    }

    @Override
    public void navigate(ScreenId screenId, Bundle args){
        // UIスレッドで同期的にnavigateしないと、Fragmentのreplaceが上手く行かない
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                onNavigate(screenId, args);
           }
        });
    }

    @Override
    public boolean isShowDialog() {
        return mFragmentController.isShowListDialog();
    }

    public boolean isShowPlayerTabContainer() {
        return mFragmentController.isShowPlayerTabContainer();
    }

    public boolean isShowRadioTabContainer() {
        return mFragmentController.isShowRadioTabContainer();
    }

    public boolean isShowUsbList() {
        return mFragmentController.isShowUsbList();
    }

    @Override
    public boolean onClose(ScreenId screenId) {
        return mFragmentController.close(screenId);
    }

    public void setActionCall(boolean isCalling){
        getPresenter().setCalling(isCalling);
    }

    @Override
    public void displaySrcMessage(String str) {
        mHandler.removeCallbacks(mDelayMessageFunc);
        mFxEqMessageText.setText(str);
        mSrcMessage.setVisibility(View.VISIBLE);
        mFxEqMessageText.setVisibility(View.VISIBLE);
        mFxEqMessageTextWhite.setVisibility(View.INVISIBLE);
        mMassageLine.setVisibility(View.VISIBLE);
        mMassageLineWhite.setVisibility(View.INVISIBLE);
        mHandler.postDelayed(mDelayMessageFunc, MESSAGE_DELAY_TIME);
    }

    @Override
    public void displayVoiceMessage(String str) {
        mHandler.removeCallbacks(mDelayMessageFunc);
        mFxEqMessageTextWhite.setText(str);
        mFxEqMessageText.setVisibility(View.INVISIBLE);
        mFxEqMessageTextWhite.setVisibility(View.VISIBLE);
        mSrcMessage.setVisibility(View.VISIBLE);
        mMassageLine.setVisibility(View.INVISIBLE);
        mMassageLineWhite.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mDelayMessageFunc, MESSAGE_DELAY_TIME);
    }
}
