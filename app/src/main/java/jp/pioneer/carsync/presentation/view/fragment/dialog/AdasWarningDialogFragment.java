package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AdasWarningDialogPresenter;
import jp.pioneer.carsync.presentation.view.AdasWarningDialogView;

/**
 * ADAS警報表示の画面
 */

public class AdasWarningDialogFragment extends AbstractDialogFragment<AdasWarningDialogPresenter,
        AdasWarningDialogView, AdasWarningDialogFragment.Callback> implements AdasWarningDialogView {
    @Inject AdasWarningDialogPresenter mPresenter;
    @BindView(R.id.adas_image) ImageView mAdasImage;
    @BindView(R.id.adas_text) TextView mAdasText;

    /**
     * コンストラクタ
     */
    public AdasWarningDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param args   引き継ぎ情報
     * @return AdasWarningDialogFragment
     */
    public static AdasWarningDialogFragment newInstance(Fragment target, Bundle args) {
        AdasWarningDialogFragment fragment = new AdasWarningDialogFragment();
        fragment.setTargetFragment(target, 0);
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
        View view = inflater.inflate(R.layout.fragment_dialog_adas_warning, null, false);
        ButterKnife.bind(this, view);
        builder.setView(view);
        setCancelable(false);
        return builder.create();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AdasWarningDialogPresenter getPresenter() {
        return mPresenter;
    }


    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof AdasWarningDialogFragment.Callback;
    }

    @Override
    public void setAdasImage(int resource){
        mAdasImage.setImageResource(resource);
    }

    @Override
    public void setAdasText(String text){
        mAdasText.setText(text);
    }

    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
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
        void onClose(AdasWarningDialogFragment fragment);
    }
}
