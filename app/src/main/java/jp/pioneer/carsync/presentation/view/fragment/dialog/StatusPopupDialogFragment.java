package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.StatusPopupDialogPresenter;
import jp.pioneer.carsync.presentation.view.StatusPopupDialogView;

/**
 * StatusPopupDialogFragment
 */

public class StatusPopupDialogFragment extends AbstractDialogFragment<StatusPopupDialogPresenter,
        StatusPopupDialogView, StatusPopupDialogFragment.Callback> implements StatusPopupDialogView,DialogInterface.OnClickListener  {
    @Inject StatusPopupDialogPresenter mPresenter;
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String POSITIVE = "positive";
    public static final String NEGATIVE = "negative";
    public static final String POSITIVE_TEXT = "positive_text";
    public static final String NEGATIVE_TEXT = "negative_text";
    public static final String TAG = "tag";
    private String mTag;

    /**
     * コンストラクタ
     */
    public StatusPopupDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return StatusPopupDialogFragment
     */
    public static StatusPopupDialogFragment newInstance(Fragment fragment, Bundle args) {
        StatusPopupDialogFragment dialog = new StatusPopupDialogFragment();
        dialog.setTargetFragment(fragment, 0);
        dialog.setCancelable(false);
        dialog.setArguments(args);
        return dialog;
    }

/*    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            listener = (OnDialogButtonClickListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement Callback interface");
        }
    }*/

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle bundle = getArguments();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogStyle);
        dialog.setMessage(bundle.getString(MESSAGE));
        if(bundle.getBoolean(POSITIVE)){
            if(bundle.getInt(POSITIVE_TEXT)!=0){
                dialog.setPositiveButton(bundle.getInt(POSITIVE_TEXT), this);
            }else {
                dialog.setPositiveButton(R.string.com_003, this);
            }
        }
        if(bundle.getBoolean(NEGATIVE)) {
            if(bundle.getInt(NEGATIVE_TEXT)!=0){
                dialog.setNegativeButton(bundle.getInt(NEGATIVE_TEXT), this);
            }else {
                dialog.setNegativeButton(R.string.com_004, this);
            }
        }
        String title = bundle.getString(TITLE);
        if(title!=null){
            dialog.setTitle(title);
        }

        mTag = bundle.getString(TAG);
        return dialog.create();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected StatusPopupDialogPresenter getPresenter() {
        return mPresenter;
    }


    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof StatusPopupDialogFragment.Callback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this, mTag);
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
        void onClose(StatusPopupDialogFragment fragment, String tag);

        void onPositiveClick(StatusPopupDialogFragment fragment, String tag);

        void onNegativeClick(StatusPopupDialogFragment fragment, String tag);
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (getCallback() != null) {
                    getCallback().onPositiveClick(this,mTag);
                }
                if (getCallback() != null) {
                    getCallback().onClose(this, mTag);
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                if (getCallback() != null) {
                    getCallback().onNegativeClick(this, mTag);
                }
                if (getCallback() != null) {
                    getCallback().onClose(this, mTag);
                }
                break;
        }
    }

}