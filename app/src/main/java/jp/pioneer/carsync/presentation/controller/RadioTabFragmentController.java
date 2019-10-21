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
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.RadioFavoriteFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.RadioPresetFragment;

import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;

/**
 * ラジオリスト系コンテナの管理クラス
 */

public class RadioTabFragmentController {
    private static final String TAG_DIALOG_RADIO_BSM = "radio_bsm";
    @Inject
    @Named(CHILD_FRAGMENT_MANAGER)
    FragmentManager mFragmentManager;
    @IdRes private int mContainerViewId;

    /**
     * コンストラクタ
     */
    @Inject
    public RadioTabFragmentController() {
    }

    /**
     * コンテナのViewIDを設定
     *
     * @param containerViewId ResourceID
     */
    public void setContainerViewId(@IdRes int containerViewId) {
        mContainerViewId = containerViewId;
    }

    /**
     * コンテナ内のScreenId取得
     * <p>
     * 現在コンテナ内に表示されている画面IDの取得
     *
     * @return ScreenId 現在表示されている画面ID
     */
    public ScreenId getScreenIdInContainer() {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment == null) {
            return null;
        }

        return ((Screen) fragment).getScreenId();
    }

    /**
     * 画面遷移
     *
     * @param screenId 遷移先ID
     * @param args     引き継ぎ情報
     * @return 当コンテナで遷移したか否か
     */
    public boolean navigate(ScreenId screenId, Bundle args) {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnNavigateListener) {
            if (((OnNavigateListener) fragment).onNavigate(screenId, args)) {
                return true;
            }
        }

        switch (screenId) {
            case RADIO_PRESET_LIST:
                clearBackStack();
                replaceFragment(createRadioPresetFragment(args), false);
                return true;
            case RADIO_FAVORITE_LIST:
                clearBackStack();
                replaceFragment(createRadioFavoriteFragment(args), false);
                return true;
            case RADIO_LIST_CONTAINER:
                return true;
        }
        return false;
    }

    /**
     * 画面戻し
     *
     * @return 当コンテナで戻したか否か
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

    private void clearBackStack() {
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

    @VisibleForTesting
    Fragment createRadioPresetFragment(Bundle args) {
        return RadioPresetFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createRadioFavoriteFragment(Bundle args) {
        return RadioFavoriteFragment.newInstance(args);
    }

}
