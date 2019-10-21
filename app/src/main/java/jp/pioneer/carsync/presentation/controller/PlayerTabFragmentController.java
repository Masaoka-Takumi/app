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
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.AlbumSongsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.AlbumsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.ArtistAlbumSongsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.ArtistAlbumsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.ArtistsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.GenreSongsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.GenresFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.PlaylistSongsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.PlaylistsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.SongsFragment;

import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;

/**
 * Created by BP06566 on 2017/03/06.
 */

public class PlayerTabFragmentController {

    @Inject
    @Named(CHILD_FRAGMENT_MANAGER)
    FragmentManager mFragmentManager;
    @IdRes private int mContainerViewId;

    @Inject
    public PlayerTabFragmentController() {
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
            case ARTIST_LIST:
                clearBackStack();
                replaceFragment(createArtistsFragment(args), false);
                return true;
            case ARTIST_ALBUM_LIST:
                replaceFragment(createArtistAlbumsFragment(args), true);
                return true;
            case ARTIST_ALBUM_SONG_LIST:
                replaceFragment(createArtistAlbumSongsFragment(args), true);
                return true;
            case ALBUM_LIST:
                clearBackStack();
                replaceFragment(createAlbumsFragment(args), false);
                return true;
            case ALBUM_SONG_LIST:
                replaceFragment(createAlbumSongsFragment(args), true);
                return true;
            case SONG_LIST:
                clearBackStack();
                replaceFragment(createSongsFragment(args), false);
                return true;
            case PLAYLIST_LIST:
                clearBackStack();
                replaceFragment(createPlaylistsFragment(args), false);
                return true;
            case PLAYLIST_SONG_LIST:
                replaceFragment(createPlaylistSongsFragment(args), true);
                return true;
            case GENRE_LIST:
                clearBackStack();
                replaceFragment(createGenresFragment(args), false);
                return true;
            case GENRE_SONG_LIST:
                replaceFragment(createGenreSongsFragment(args), true);
                return true;
            case PLAYER_LIST_CONTAINER:
                return true;
        }
        return false;
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
    Fragment createArtistsFragment(Bundle args) {
        return ArtistsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createArtistAlbumsFragment(Bundle args) {
        return ArtistAlbumsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createArtistAlbumSongsFragment(Bundle args) {
        return ArtistAlbumSongsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAlbumsFragment(Bundle args) {
        return AlbumsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAlbumSongsFragment(Bundle args) {
        return AlbumSongsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSongsFragment(Bundle args) {
        return SongsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createPlaylistsFragment(Bundle args) {
        return PlaylistsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createPlaylistSongsFragment(Bundle args) {
        return PlaylistSongsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createGenresFragment(Bundle args) {
        return GenresFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createGenreSongsFragment(Bundle args) {
        return GenreSongsFragment.newInstance(args);
    }
}
