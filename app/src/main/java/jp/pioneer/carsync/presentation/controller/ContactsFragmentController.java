package jp.pioneer.carsync.presentation.controller;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import javax.inject.Inject;
import javax.inject.Named;

import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;
import jp.pioneer.carsync.presentation.view.fragment.Screen;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.contacts.ContactsFavoriteFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.contacts.ContactsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.contacts.ContactsHistoryFragment;

import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;

/**
 * 電話帳画面を管理するクラス
 */

public class ContactsFragmentController {
    @Inject
    @Named(CHILD_FRAGMENT_MANAGER)
    FragmentManager mFragmentManager;
    @IdRes private int mContainerViewId;

    /**
     * コンストラクタ
     */
    @Inject
    public ContactsFragmentController() {
    }

    /**
     * 現在のViewIDの設定
     * @param containerViewId ViewID
     */
    public void setContainerViewId(@IdRes int containerViewId) {
        mContainerViewId = containerViewId;
    }

    /**
     * 現在のViewIDの取得
     @return ScreenId
     */
    public ScreenId getScreenIdInContainer() {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment == null) {
            return null;
        }

        return ((Screen) fragment).getScreenId();
    }

    /**
     * 指定した画面IDに遷移
     * @param screenId ScreenId
     * @param args Bundle　
     * @return boolean
     */
    public boolean navigate(ScreenId screenId, Bundle args) {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnNavigateListener) {
            if (((OnNavigateListener) fragment).onNavigate(screenId, args)) {
                return true;
            }
        }

        switch (screenId) {
            case CONTACTS_LIST:
                replaceFragment(createContactsFragment(args), false);
                return true;
            case CONTACTS_HISTORY:
                replaceFragment(createContactsHistoryFragment(args), false);
                return true;
            case CONTACTS_FAVORITE:
                replaceFragment(createContactsFavoriteFragment(args), false);
                return true;
        }
        return false;
    }

    /**
     * 遷移元画面に戻る
     * @return boolean
     */
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

    private void replaceFragment(Fragment fragment, boolean isAddToBackStack) {
        FragmentTransaction tr = mFragmentManager.beginTransaction();
        tr.replace(mContainerViewId, fragment);
        if (isAddToBackStack) {
            tr.addToBackStack(null);
        }

        tr.commit();
    }

    @VisibleForTesting
    Fragment createContactsFragment(Bundle args) {
        return ContactsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createContactsHistoryFragment(Bundle args) {
        return ContactsHistoryFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createContactsFavoriteFragment(Bundle args) {
        return ContactsFavoriteFragment.newInstance(args);
    }
}
