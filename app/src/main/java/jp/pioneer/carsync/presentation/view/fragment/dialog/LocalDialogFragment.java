package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.widget.ListView;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.presentation.presenter.LocalDialogPresenter;
import jp.pioneer.carsync.presentation.view.LocalDialogView;

/**
 * Local設定ダイアログのFragment.
 */
public class LocalDialogFragment extends AbstractDialogFragment<LocalDialogPresenter, LocalDialogView, LocalDialogFragment.Callback>
        implements LocalDialogView {

    @Inject LocalDialogPresenter mPresenter;
    private ListView mListView;
    private Handler mHandler = new Handler();
    private SparseArrayCompat<LocalSetting> mLocalSettingListItems;

    /** Local設定のリストアイテム. */
    private final static SparseArrayCompat<LocalSetting> FM_LOCAL_SETTING_LIST_ITEMS = new SparseArrayCompat<LocalSetting>() {{
        put(0, LocalSetting.LEVEL1);
        put(1, LocalSetting.LEVEL2);
        put(2, LocalSetting.LEVEL3);
        put(3, LocalSetting.LEVEL4);
        put(4, LocalSetting.OFF);
    }};

    private final static SparseArrayCompat<LocalSetting> AM_LOCAL_SETTING_LIST_ITEMS = new SparseArrayCompat<LocalSetting>() {{
        put(0, LocalSetting.LEVEL1);
        put(1, LocalSetting.LEVEL2);
        put(2, LocalSetting.OFF);
    }};

    /**
     * コンストラクタ.
     */
    public LocalDialogFragment() {

    }

    public static LocalDialogFragment newInstance(Fragment target, Bundle args) {
        LocalDialogFragment fragment = new LocalDialogFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean isFm = getPresenter().isFm();

        mLocalSettingListItems = isFm ? FM_LOCAL_SETTING_LIST_ITEMS : AM_LOCAL_SETTING_LIST_ITEMS;
        String[] listItem = new String[mLocalSettingListItems.size()];
        for (int i = 0; i < mLocalSettingListItems.size(); i++) {
            listItem[i] = getString(mLocalSettingListItems.get(i).label);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialogStyle)
                .setSingleChoiceItems(listItem, -1, (dialog, which) -> {
                    LocalSetting setting = mLocalSettingListItems.get(which);
                    getPresenter().onSelectAction(setting);
                    callbackClose();
                })
                .setPositiveButton(R.string.com_004, (dialog, which) -> callbackClose())
                .setTitle(R.string.set_124)
                .create();
        mListView = alertDialog.getListView();
        return alertDialog;
    }

    @Override
    public void setLocalSetting(LocalSetting setting) {
        mHandler.post(() -> {
            if (mListView != null) {
                int index = setting == null ? -1 : mLocalSettingListItems.indexOfValue(setting);
                mListView.setItemChecked(index, true);
            } else {
                callbackClose();
            }
        });
    }

    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof LocalDialogFragment.Callback;
    }

    @NonNull
    @Override
    protected LocalDialogPresenter getPresenter() {
        return mPresenter;
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
        void onClose(LocalDialogFragment fragment);
    }
}
