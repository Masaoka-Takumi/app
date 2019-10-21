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
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchAlbumSongsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchArtistAlbumSongsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchArtistAlbumsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchContactFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchMusicFragment;

import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;

/**
 * 検索関連画面を管理するクラス
 */

public class SearchFragmentController {
    @Inject
    @Named(CHILD_FRAGMENT_MANAGER)
    FragmentManager mFragmentManager;
    @IdRes private int mContainerViewId;

    /**
     * コンストラクタ
     */
    @Inject
    public SearchFragmentController() {
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
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnNavigateListener) {
            if (((OnNavigateListener) fragment).onNavigate(screenId, args)) {
                return true;
            }
        }

        switch (screenId) {
            case SEARCH_CONTACT_RESULTS:
                replaceFragment(createSearchContactFragment(args), false);
                return true;
            case SEARCH_MUSIC_RESULTS:
                replaceFragment(createSearchMusicFragment(args), false);
                return true;
            case SEARCH_MUSIC_ARTIST_ALBUM_LIST:
                replaceFragment(createSearchArtistAlbumsFragment(args), true);
                return true;
            case SEARCH_MUSIC_ARTIST_ALBUM_SONG_LIST:
                replaceFragment(createSearchArtistAlbumSongsFragment(args), true);
                return true;
            case SEARCH_MUSIC_ALBUM_SONG_LIST:
                replaceFragment(createSearchAlbumSongsFragment(args), true);
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

    private void replaceFragment(Fragment fragment, boolean isAddToBackStack) {
        FragmentTransaction tr = mFragmentManager.beginTransaction();
        tr.replace(mContainerViewId, fragment);
        if (isAddToBackStack) {
            tr.addToBackStack(null);
        }

        tr.commit();
    }

    @VisibleForTesting
    Fragment createSearchContactFragment(Bundle args) {
        return SearchContactFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSearchMusicFragment(Bundle args) {
        return SearchMusicFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSearchArtistAlbumsFragment(Bundle args) {
        return SearchArtistAlbumsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSearchArtistAlbumSongsFragment(Bundle args) {
        return SearchArtistAlbumSongsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSearchAlbumSongsFragment(Bundle args) {
        return SearchAlbumSongsFragment.newInstance(args);
    }
}
