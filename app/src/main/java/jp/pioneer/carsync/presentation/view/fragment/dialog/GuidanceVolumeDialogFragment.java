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
import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;
import jp.pioneer.carsync.presentation.presenter.GuidanceVolumeDialogPresenter;
import jp.pioneer.carsync.presentation.view.GuidanceVolumeDialogView;

/**
 * ナビガイド音声音量設定ダイアログのFragment.
 */
public class GuidanceVolumeDialogFragment extends AbstractDialogFragment<GuidanceVolumeDialogPresenter, GuidanceVolumeDialogView, GuidanceVolumeDialogFragment.Callback>
        implements GuidanceVolumeDialogView {

    @Inject GuidanceVolumeDialogPresenter mPresenter;
    private ListView mListView;
    private Handler mHandler = new Handler();

    /** ナビガイド音声音量設定のリストアイテム. */
    private final static SparseArrayCompat<NaviGuideVoiceVolumeSetting> GUIDANCE_VOLUME_LIST_ITEM = new SparseArrayCompat<NaviGuideVoiceVolumeSetting>() {{
        put(0, NaviGuideVoiceVolumeSetting.MINIMUM);
        put(1, NaviGuideVoiceVolumeSetting.SMALL);
        put(2, NaviGuideVoiceVolumeSetting.MEDIUM);
        put(3, NaviGuideVoiceVolumeSetting.LARGE);
        put(4, NaviGuideVoiceVolumeSetting.MAXIMUM);
    }};

    /**
     * コンストラクタ.
     */
    public GuidanceVolumeDialogFragment() {

    }

    public static GuidanceVolumeDialogFragment newInstance(Fragment target, Bundle args) {
        GuidanceVolumeDialogFragment fragment = new GuidanceVolumeDialogFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] listItem = new String[GUIDANCE_VOLUME_LIST_ITEM.size()];
        for (int i = 0; i < GUIDANCE_VOLUME_LIST_ITEM.size(); i++) {
            listItem[i] = getString(GUIDANCE_VOLUME_LIST_ITEM.get(i).label);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialogStyle)
                .setSingleChoiceItems(listItem, -1, (dialog, which) -> {
                    NaviGuideVoiceVolumeSetting setting = GUIDANCE_VOLUME_LIST_ITEM.get(which);
                    getPresenter().onSelectAction(setting);
                    callbackClose();
                })
                .setPositiveButton(R.string.com_004, (dialog, which) -> callbackClose())
                .setTitle(R.string.set_075)
                .create();
        mListView = alertDialog.getListView();
        return alertDialog;
    }

    @Override
    public void setVolumeSetting(NaviGuideVoiceVolumeSetting setting) {
        mHandler.post(() -> {
            if (mListView != null) {
                int index = setting == null ? -1 : GUIDANCE_VOLUME_LIST_ITEM.indexOfValue(setting);
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
        return callback instanceof Callback;
    }

    @NonNull
    @Override
    protected GuidanceVolumeDialogPresenter getPresenter() {
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
        void onClose(GuidanceVolumeDialogFragment fragment);
    }
}
