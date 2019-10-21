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

import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;
import jp.pioneer.carsync.presentation.view.fragment.Screen;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.YouTubeLinkCautionFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.YouTubeLinkWebViewFragment;
import timber.log.Timber;

import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;

public class YouTubeLinkFragmentController {

    @Inject
    @Named(CHILD_FRAGMENT_MANAGER)
    FragmentManager mFragmentManager;
    @IdRes private int mContainerViewId;

    @Inject
    public YouTubeLinkFragmentController() {
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
     * @return 遷移したか否か
     */
    public boolean navigate(ScreenId screenId, Bundle args) {
        Timber.i("YouTubeLinkFragmentController navigate");
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnNavigateListener) {
            if (((OnNavigateListener) fragment).onNavigate(screenId, args)) {
                return true;
            }
        }

        switch (screenId) {
            case YOUTUBE_LINK_CAUTION:
                Timber.i("YouTubeLinkCaution navigate");
                clearBackStack();
                replaceFragment(createYouTubeLinkCautionFragment(args), false);
                return true;
            case YOUTUBE_LINK_WEBVIEW:
                Timber.i("YouTubeLinkWebView navigate");
                replaceFragment(createYouTubeLinkWebViewFragment(args), false);
                return true;
            default:
                return false;
        }
    }

    /**
     * 画面戻し処理
     *
     * @return 戻ったか否か
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

    private void replaceFragment(Fragment fragment, boolean isAddToBackStack){
        FragmentTransaction tr = mFragmentManager.beginTransaction();
        tr.replace(mContainerViewId, fragment);
        if(isAddToBackStack){
            tr.addToBackStack(null);
        }
        tr.commit();
    }
    @VisibleForTesting
    Fragment createYouTubeLinkCautionFragment(Bundle args){
        return YouTubeLinkCautionFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createYouTubeLinkWebViewFragment(Bundle args){
        return YouTubeLinkWebViewFragment.newInstance(args);
    }
}
