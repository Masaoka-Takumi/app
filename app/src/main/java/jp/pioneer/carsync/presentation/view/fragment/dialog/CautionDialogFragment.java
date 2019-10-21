package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.CautionDialogPresenter;
import jp.pioneer.carsync.presentation.view.CautionDialogView;

/**
 * Caution画面
 */
public class CautionDialogFragment extends AbstractDialogFragment<CautionDialogPresenter, CautionDialogView, CautionDialogFragment.Callback>
        implements CautionDialogView {
    public static final int REQUEST_CODE_ASK_FOR_PERMISSION = 100;

    @Inject CautionDialogPresenter mPresenter;
    @BindView(R.id.caution_text) TextView mCautionText;

    /**
     * コンストラクタ
     */
    public CautionDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param args   引き継ぎ情報
     * @return CautionDialogFragment
     */
    public static CautionDialogFragment newInstance(Fragment target, Bundle args) {
        CautionDialogFragment fragment = new CautionDialogFragment();
        fragment.setTargetFragment(target, 0);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_caution, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void requestPermissions(final String... permissions) {
        requestPermissions(permissions, REQUEST_CODE_ASK_FOR_PERMISSION);
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
    protected CautionDialogPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }

    @Override
    public void setScreenOn() {
        // not sleep
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 確認ボタン押下イベント
     */
    @OnClick(R.id.confirm_button)
    public void onClickConfirm() {
        getPresenter().onConfirmAction();
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
        void onClose(CautionDialogFragment fragment);
    }
}
