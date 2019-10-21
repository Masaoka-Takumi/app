package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.PairingSelectDialogPresenter;
import jp.pioneer.carsync.presentation.view.PairingSelectDialogView;
import jp.pioneer.carsync.presentation.view.adapter.EasyPairingAdapter;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

/**
 * PairingSelectDialogFragment
 */
@RuntimePermissions
public class PairingSelectDialogFragment extends AbstractDialogFragment<PairingSelectDialogPresenter,
        PairingSelectDialogView, AbstractDialogFragment.Callback> implements PairingSelectDialogView, DialogInterface.OnClickListener {
    @Inject PairingSelectDialogPresenter mPresenter;
    public static final String TITLE = "title";
    public static final String DATA = "items";
    public static final String SELECTED = "selected";
    private EasyPairingAdapter mAdapter;
    /**
     * コンストラクタ
     */
    public PairingSelectDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return PairingSelectDialogFragment
     */
    public static PairingSelectDialogFragment newInstance(Fragment fragment, Bundle args) {
        PairingSelectDialogFragment dialog = new PairingSelectDialogFragment();
        dialog.setTargetFragment(fragment, 0);
        dialog.setCancelable(true);
        dialog.setArguments(args);
        return dialog;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogStyle);

        dialog.setPositiveButton(R.string.com_004, new PairingSelectDialogFragment.PositiveButtonClickListener());
        dialog.setTitle(R.string.tip_008);
        mAdapter = new EasyPairingAdapter(getContext(), new ArrayList<>());
        dialog.setAdapter(mAdapter,this);
        AlertDialog alertDialog = dialog.create();
        ListView listView = alertDialog.getListView();
        listView.setDivider(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.list_divider)));
        listView.setDividerHeight(2);
        return alertDialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PairingSelectDialogFragmentPermissionsDispatcher.searchWithCheck(this);
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    public void search() {
        Timber.d("search");
        getPresenter().search(mAdapter);
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected PairingSelectDialogPresenter getPresenter() {
        return mPresenter;
    }


    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof PairingSelectDialogFragment.Callback;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        getPresenter().stop();
        super.onDismiss(dialog);
    }

    @Override
    public void bluetoothDisabled() {
        showToast(getString(R.string.tip_017));
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
        dismiss();
    }

    private void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }

    class PositiveButtonClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        getPresenter().pairing(which);
        dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Timber.d("onRequestPermissionsResult(requestCode: %d)", requestCode);

        PairingSelectDialogFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);

        if (!PermissionUtils.verifyPermissions(grantResults)) {
            // DialogFragmentなので、許可できなかったときはDialogを閉じる
            this.dismissAllowingStateLoss();
        }
    }
}