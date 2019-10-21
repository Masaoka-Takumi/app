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
import jp.pioneer.carsync.domain.model.LoudnessSetting;
import jp.pioneer.carsync.presentation.presenter.LoudnessDialogPresenter;
import jp.pioneer.carsync.presentation.view.LoudnessDialogView;

/**
 * Loudness設定ダイアログのFragment.
 */
public class LoudnessDialogFragment extends AbstractDialogFragment<LoudnessDialogPresenter, LoudnessDialogView, LoudnessDialogFragment.Callback>
        implements LoudnessDialogView {

    @Inject LoudnessDialogPresenter mPresenter;
    private ListView mListView;
    private Handler mHandler = new Handler();

    /** Loudnessのリストアイテム. */
    private final static SparseArrayCompat<LoudnessSetting> LOUDNESS_LIST_ITEMS = new SparseArrayCompat<LoudnessSetting>() {{
        put(0, LoudnessSetting.LOW);
        put(1, LoudnessSetting.MID);
        put(2, LoudnessSetting.HIGH);
        put(3, LoudnessSetting.OFF);
    }};

    /**
     * コンストラクタ.
     */
    public LoudnessDialogFragment() {

    }

    public static LoudnessDialogFragment newInstance(Fragment target, Bundle args) {
        LoudnessDialogFragment fragment = new LoudnessDialogFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] listItem = new String[LOUDNESS_LIST_ITEMS.size()];
        for (int i = 0; i < LOUDNESS_LIST_ITEMS.size(); i++) {
            listItem[i] = getString(LOUDNESS_LIST_ITEMS.get(i).label);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialogStyle)
                .setSingleChoiceItems(listItem, -1, (dialog, which) -> {
                    LoudnessSetting setting = LOUDNESS_LIST_ITEMS.get(which);
                    getPresenter().onSelectAction(setting);
                    callbackClose();
                })
                .setPositiveButton(R.string.com_004, (dialog, which) -> callbackClose())
                .setTitle(R.string.set_125)
                .create();
        mListView = alertDialog.getListView();
        return alertDialog;
    }

    @Override
    public void setLoudnessSetting(LoudnessSetting setting) {
        mHandler.post(() -> {
            if (mListView != null) {
                int index = setting == null ? -1 : LOUDNESS_LIST_ITEMS.indexOfValue(setting);
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
        return callback instanceof LoudnessDialogFragment.Callback;
    }

    @NonNull
    @Override
    protected LoudnessDialogPresenter getPresenter() {
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
        void onClose(LoudnessDialogFragment fragment);
    }
}
