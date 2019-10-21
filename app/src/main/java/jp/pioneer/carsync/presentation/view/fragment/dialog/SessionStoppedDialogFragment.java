package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.SessionStoppedDialogPresenter;
import jp.pioneer.carsync.presentation.view.SessionStoppedDialogView;

/**
 * 車載機切断ダイアログ.
 */
public class SessionStoppedDialogFragment extends AbstractDialogFragment<SessionStoppedDialogPresenter, SessionStoppedDialogView, SessionStoppedDialogFragment.Callback>
        implements SessionStoppedDialogView,DialogInterface.OnClickListener {

    @Inject SessionStoppedDialogPresenter mPresenter;

    /**
     * コンストラクタ
     */
    public SessionStoppedDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param args   引き継ぎ情報
     * @return CautionDialogFragment
     */
    public static SessionStoppedDialogFragment newInstance(Fragment target, Bundle args) {
        SessionStoppedDialogFragment fragment = new SessionStoppedDialogFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogStyle);
        dialog.setMessage(R.string.err_002);
        dialog.setPositiveButton(R.string.com_003, this);

        return dialog.create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof Callback;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    protected SessionStoppedDialogPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }

    @Override
    public void onScreenOff() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        void onClose(SessionStoppedDialogFragment fragment);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                    getPresenter().onConfirmAction();
                break;
        }
    }
}

