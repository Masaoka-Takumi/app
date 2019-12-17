package jp.pioneer.carsync.presentation.controller;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import javax.inject.Inject;
import javax.inject.Named;

import jp.pioneer.carsync.application.di.FragmentLifeCycle;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;
import jp.pioneer.carsync.presentation.view.fragment.Screen;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.PairingSelectDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.unconnected.EasyPairingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.unconnected.TipsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.unconnected.TipsWebFragment;

import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;

/**
 * Created by NSW00_008320 on 2018/01/10.
 */
@FragmentLifeCycle
public class UnconnectedFragmentController {
    private static final String TAG_DIALOG_PAIRING_SELECT = "pairing_select";
    @Inject
    @Named(CHILD_FRAGMENT_MANAGER)
    FragmentManager mFragmentManager;
    @IdRes private int mContainerViewId;

    @Inject
    public UnconnectedFragmentController() {
    }

    public void setContainerViewId(@IdRes int containerViewId) {
        mContainerViewId = containerViewId;
    }

    public ScreenId getScreenIdInContainer() {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment == null) {
            return null;
        }

        return ((Screen) fragment).getScreenId();
    }

    public boolean navigate(ScreenId screenId, Bundle args) {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnNavigateListener) {
            if (((OnNavigateListener) fragment).onNavigate(screenId, args)) {
                return true;
            }
        }

        switch (screenId) {
            case UNCONNECTED_CONTAINER:
                clearBackStack();
                return true;
            case TIPS:
                replaceFragment(createTipsFragment(args), false);
                return true;
            case TIPS_WEB:
                replaceFragment(createTipsWebFragment(args), true);
                return true;
            case EASY_PAIRING:
                replaceFragment(createEasyPairingFragment(args), true);
                return true;
            case PAIRING_SELECT:
                if (!isShowPairingSelect()) {
                    showPairingSelect(fragment, args);
                }
                return true;
            case SETTINGS_CONTAINER:
                replaceFragment(createSettingsContainerFragment(args), true);
                return true;
            default:
                return false;
        }
    }

    public boolean goBack() {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnGoBackListener) {
            if (((OnGoBackListener) fragment).onGoBack()) {
                return true;
            }
        }

        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStackImmediate();
            return true;
        }
        return false;
    }

    public Fragment getContainerFragment(){
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment == null) {
            return null;
        }
        return fragment;
    }

    public void clearBackStack() {
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry entry = mFragmentManager.getBackStackEntryAt(0);
            mFragmentManager.popBackStack(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void replaceFragment(Fragment fragment, boolean isAddToBackStack) {
        FragmentTransaction tr = mFragmentManager.beginTransaction();
        tr.replace(mContainerViewId, fragment);
        if (isAddToBackStack) {
            tr.addToBackStack(null);
        }

        tr.commit();
    }

    /**
     * Pairing選択ダイアログ表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showPairingSelect(Fragment fragment, Bundle args) {
        createPairingSelectDialogFragment(fragment, args).show(mFragmentManager, TAG_DIALOG_PAIRING_SELECT);
        mFragmentManager.executePendingTransactions();
    }


    /**
     * Pairing選択ダイアログ閉幕
     */
    public void dismissPairingSelect() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_PAIRING_SELECT);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * Pairing選択ダイアログ表示確認
     *
     * @return PTYSearchが表示されているか否か
     */
    public boolean isShowPairingSelect() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_PAIRING_SELECT) != null);
    }

    @VisibleForTesting
    Fragment createTipsFragment(Bundle args) {
        return TipsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createTipsWebFragment(Bundle args) {
        return TipsWebFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createEasyPairingFragment(Bundle args) {
        return EasyPairingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSettingsContainerFragment(Bundle args) {
        return SettingsContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createPairingSelectDialogFragment(Fragment fragment, Bundle args) {
        return PairingSelectDialogFragment.newInstance(fragment, args);
    }
}
