package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.ReadingMessageDialogPresenter;
import jp.pioneer.carsync.presentation.view.ReadingMessageDialogView;

/**
 * Radio BSM Dialogの画面
 */

public class ReadingMessageDialogFragment extends AbstractDialogFragment<ReadingMessageDialogPresenter,
        ReadingMessageDialogView, ReadingMessageDialogFragment.Callback> implements ReadingMessageDialogView {
    private static final int DELAY_TIME = 10000;
    @Inject ReadingMessageDialogPresenter mPresenter;
    @BindView(R.id.dialog_view) RelativeLayout mDialogView;
    @BindView(R.id.bsm_icon) ImageView mBsmIcon;
    @BindView(R.id.title_text) TextView mTitleText;
    @BindView(R.id.dialog_close_button) ImageView mDialogCloseButton;
    private String mTag;

    /**
     * コンストラクタ
     */
    public ReadingMessageDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AccidentDetectDialogFragment
     */
    public static ReadingMessageDialogFragment newInstance(Bundle args) {
        ReadingMessageDialogFragment fragment = new ReadingMessageDialogFragment();
        fragment.setCancelable(false);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CautionStyle);
        View view = inflater.inflate(R.layout.fragment_dialog_radio_bsm, null, false);
        ButterKnife.bind(this, view);
        builder.setView(view);
        setCancelable(false);
        getPresenter().setArgument(getArguments());
        return builder.create();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected ReadingMessageDialogPresenter getPresenter() {
        return mPresenter;
    }


    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof ReadingMessageDialogFragment.Callback;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void setTitleText(int text) {
        mTitleText.setText(text);
    }

    @Override
    public void setAnimation(String type) {
        int animationResource;
        switch (type) {
            case ReadingMessageDialogPresenter.TAG_READING:
                animationResource = R.drawable.animation_reading;
                mDialogCloseButton.setVisibility(View.VISIBLE);
                mTag = type;
                break;
            default:
                animationResource = R.drawable.animation_reading;
                break;
        }

        mBsmIcon.setImageResource(animationResource);
        AnimationDrawable frameAnimation = (AnimationDrawable) mBsmIcon.getDrawable();
        // アニメーションの開始
        frameAnimation.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this,mTag);
        }
        this.dismiss();
    }

    /**
     * DialogView押下
     *
     * @param view ビュー
     */
    @OnClick(R.id.dialog_view)
    public void onClickDialogView(View view) {
        getPresenter().onDialogClickAction();
    }

    /**
     *
     * BSMの閉じるボタン押下
     *
     * @param view ビュー
     */
    @OnClick(R.id.dialog_close_button)
    public void onClickCloseButton(View view) {
        getPresenter().onCloseAction();
    }

    /**
     * ダイアログ終了通知interface
     */
    public interface Callback {
        /**
         * ダイアログ終了通知
         *
         * @param fragment 終了ダイアログ
         */
        void onClose(ReadingMessageDialogFragment fragment, String tag);
    }
}


