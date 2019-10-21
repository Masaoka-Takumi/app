package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.presentation.presenter.MenuDisplayLanguageDialogPresenter;
import jp.pioneer.carsync.presentation.view.MenuDisplayLanguageDialogView;

/**
 * Menu表示言語設定ダイアログのFragment.
 */
public class MenuDisplayLanguageDialogFragment extends AbstractDialogFragment<MenuDisplayLanguageDialogPresenter, MenuDisplayLanguageDialogView, MenuDisplayLanguageDialogFragment.Callback>
        implements MenuDisplayLanguageDialogView {

    @Inject MenuDisplayLanguageDialogPresenter mPresenter;
    private ListView mListView;
    private Handler mHandler = new Handler();
    private List<MenuDisplayLanguageType> mListItems = new ArrayList<>();

    /**
     * コンストラクタ.
     */
    public MenuDisplayLanguageDialogFragment() {

    }

    public static MenuDisplayLanguageDialogFragment newInstance(Fragment target, Bundle args) {
        MenuDisplayLanguageDialogFragment fragment = new MenuDisplayLanguageDialogFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Set<MenuDisplayLanguageType> supportedLanguage = getPresenter().getSupportedLanguage();

        mListItems = new ArrayList<>();
        for(MenuDisplayLanguageType type : MenuDisplayLanguageType.values()){
            if(supportedLanguage.contains(type)){
                mListItems.add(type);
            }
        }

        String[] listItem = new String[mListItems.size()];
        for (int i = 0; i < mListItems.size(); i++) {
            listItem[i] = getString(mListItems.get(i).label);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialogStyle)
                .setSingleChoiceItems(listItem, -1, (dialog, which) -> {
                    MenuDisplayLanguageType type = mListItems.get(which);
                    getPresenter().onSelectAction(type);
                    callbackClose();
                })
                .setPositiveButton(R.string.com_004, (dialog, which) -> callbackClose())
                .setTitle(R.string.set_110)
                .create();
        mListView = alertDialog.getListView();
        return alertDialog;
    }

    @Override
    public void setMenuDisplayLanguageSetting(MenuDisplayLanguageType setting) {
        mHandler.post(() -> {
            if (mListView != null) {
                int index = -1;
                if(mListItems.contains(setting)){
                    index = mListItems.indexOf(setting);
                }
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
        return callback instanceof MenuDisplayLanguageDialogFragment.Callback;
    }

    @NonNull
    @Override
    protected MenuDisplayLanguageDialogPresenter getPresenter() {
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
        void onClose(MenuDisplayLanguageDialogFragment fragment);
    }
}
