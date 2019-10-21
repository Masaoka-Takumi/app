package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.SingleChoiceDialogPresenter;
import jp.pioneer.carsync.presentation.view.SingleChoiceDialogView;

/**
 * SingleChoiceDialogFragment
 */

public class SingleChoiceDialogFragment extends AbstractDialogFragment<SingleChoiceDialogPresenter,
        SingleChoiceDialogView, SingleChoiceDialogFragment.Callback> implements SingleChoiceDialogView, DialogInterface.OnClickListener {
    @Inject SingleChoiceDialogPresenter mPresenter;
    public static final String TITLE = "title";
    public static final String DATA = "items";
    public static final String SELECTED = "selected";
    public static final String CANCELABLE = "cancelable";
    /**
     * コンストラクタ
     */
    public SingleChoiceDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return SingleChoiceDialogFragment
     */
    public static SingleChoiceDialogFragment newInstance(Fragment fragment, Bundle args) {
        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment();
        dialog.setTargetFragment(fragment, 0);
        //dialog.setCancelable(false);
        dialog.setArguments(args);
        return dialog;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle bundle = getArguments();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogStyle);

        dialog.setPositiveButton(R.string.com_004, new PositiveButtonClickListener());
        String title = bundle.getString(TITLE);
        CharSequence[] items = bundle.getStringArray(DATA);
        int position = bundle.getInt(SELECTED);
        boolean cancelable = bundle.getBoolean(CANCELABLE,true);
        dialog.setCancelable(cancelable);
        dialog.setTitle(title);
        //dialog.setSingleChoiceItems(items, position, this);
        dialog.setItems(items,this);
        AlertDialog alertDialog = dialog.create();
        ListView listView = alertDialog.getListView();
        listView.setDivider(new ColorDrawable(ContextCompat.getColor(getContext(),R.color.list_divider)));
        listView.setDividerHeight(2);

        return alertDialog;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected SingleChoiceDialogPresenter getPresenter() {
        return mPresenter;
    }


    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof SingleChoiceDialogFragment.Callback;
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

    /**
     * ダイアログ終了通知interface
     */
    public interface Callback {
        /**
         * ダイアログ終了通知
         *
         * @param fragment 終了ダイアログ
         */
        void onClose(SingleChoiceDialogFragment fragment);

        void selectItem ( int position );

    }

    class PositiveButtonClickListener implements DialogInterface.OnClickListener
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            callbackClose();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (getCallback() != null) {
            getCallback().selectItem(which);
        }
        callbackClose();
    }
}