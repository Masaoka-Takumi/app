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

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlRadioSource;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;
import jp.pioneer.carsync.presentation.view.fragment.Screen;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SingleChoiceDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.AndroidMusicFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.AuxFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.BtAudioFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.CdFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.DabFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.HdRadioFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.PandoraFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.RadioFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.SourceOffFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.SourceSelectContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.SpotifyFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.SxmFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.TiFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.UnsupportedFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.UsbFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.NowPlayingListContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.PlayerTabContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.RadioTabContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.UsbListContainerFragment;

import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;

/**
 * ローカルコンテンツ再生画面を管理するクラス
 */
public class PlayerFragmentController {

    private static final String TAG_DIALOG_RADIO_LIST_CONTAINER = "radio_list_container";
    private static final String TAG_DIALOG_PLAYER_LIST_CONTAINER = "player_list_container";
    private static final String TAG_DIALOG_SOURCE_SELECT = "source_select";
    private static final String TAG_DIALOG_NOW_PLAYING_LIST = "now_playing_list";
    private static final String TAG_DIALOG_USB_LIST = "usb_list";
    private static final String TAG_DIALOG_PTY_SELECT = "pty_select";
    private static final String[] KEY_CONTAINERS = new String[]{
            TAG_DIALOG_RADIO_LIST_CONTAINER, TAG_DIALOG_PLAYER_LIST_CONTAINER, TAG_DIALOG_SOURCE_SELECT
    };
    private static final String[] KEY_DIALOGS = new String[]{
            TAG_DIALOG_NOW_PLAYING_LIST, TAG_DIALOG_USB_LIST, TAG_DIALOG_PTY_SELECT
    };
    @Inject
    @Named(CHILD_FRAGMENT_MANAGER)
    FragmentManager mFragmentManager;
    @IdRes private int mContainerViewId;
    @Inject ControlMediaList mMediaCase;
    @Inject ControlRadioSource mControlCase;

    /**
     * コンストラクタ
     */
    @Inject
    public PlayerFragmentController() {
    }

    /**
     * 現在のViewIDの設定
     *
     * @param containerViewId ViewID
     */
    public void setContainerViewId(@IdRes int containerViewId) {
        mContainerViewId = containerViewId;
    }

    /**
     * 現在のViewIDの取得
     *
     * @return ScreenId
     */
    public ScreenId getScreenIdInContainer() {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment == null) {
            return null;
        }

        return ((Screen) fragment).getScreenId();
    }

    /**
     * 現在のFragmentの取得
     *
     * @return Fragment
     */
    public Fragment getFragmentInContainer() {
       return mFragmentManager.findFragmentById(mContainerViewId);
    }

    /**
     * 指定した画面IDに遷移
     *
     * @param screenId ScreenId
     * @param args     Bundle
     * @return boolean
     */
    public boolean navigate(ScreenId screenId, Bundle args) {
        Fragment dialog;
        // 表示しているコンテナダイアログがある場合、優先的に画面遷移
        for (String key : KEY_CONTAINERS) {
            dialog = mFragmentManager.findFragmentByTag(key);
            if (dialog instanceof OnNavigateListener) {
                if (((OnNavigateListener) dialog).onNavigate(screenId, args)) {
                    return true;
                } else {
                    ((DialogFragment) dialog).dismiss();
                    //リスト退場
                    if(key.equals(TAG_DIALOG_RADIO_LIST_CONTAINER)||key.equals(TAG_DIALOG_PLAYER_LIST_CONTAINER)) {
                        mMediaCase.exitList();
                    }
                    break;
                }
            }
        }

        //表示しているダイアログを閉幕
        for (String key : KEY_DIALOGS) {
            dialog = mFragmentManager.findFragmentByTag(key);
            if (dialog instanceof DialogFragment) {
                if (key.equals(TAG_DIALOG_USB_LIST)) {
                    if (screenId == ScreenId.USB_LIST) {
                        return true;
                    }
                    //リスト退場
                    mMediaCase.exitList();
                }
                ((DialogFragment) dialog).dismiss();

                break;
            }
        }

        // 子View内の画面遷移がある場合、優先的に画面遷移
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnNavigateListener) {
            if (((OnNavigateListener) fragment).onNavigate(screenId, args)) {
                return true;
            }
        }

        switch (screenId) {
            case SOURCE_SELECT:
                showSourceSelect(args);
                return true;
            case RADIO:
                clearBackStack();
                replaceFragment(createRadioFragment(args), false);
                return true;
            case RADIO_PTY_SELECT:
                if (!isShowPtySelect()) {
                    showPtySelect(fragment, args);
                }
                return true;
            case RADIO_LIST_CONTAINER:
                showRadioTabContainerDialogFragment(args);
                return true;
            case SIRIUS_XM:
                clearBackStack();
                replaceFragment(createSxmFragment(args), false);
                return true;
            case USB:
                replaceFragment(createUsbFragment(args), false);
                return true;
            case USB_LIST:
                if (!isShowUsbList()) {
                    showUsbList(args);
                }
                return true;
            case CD:
                clearBackStack();
                replaceFragment(createCdFragment(args), false);
                return true;
            case BT_AUDIO:
                clearBackStack();
                replaceFragment(createBtAudioFragment(args), false);
                return true;
            case ANDROID_MUSIC:
                clearBackStack();
                replaceFragment(createAndroidMusicFragment(args), false);
                return true;
            case NOW_PLAYING_LIST:
                if (!isShowNowPlayingList()) {
                    showNowPlayingList(args);
                }
                return true;
            case PLAYER_LIST_CONTAINER:
                showPlayerTabContainer(args);
                return true;
            case PANDORA:
                clearBackStack();
                replaceFragment(createPandoraFragment(args), false);
                return true;
            case SPOTIFY:
                clearBackStack();
                replaceFragment(createSpotifyFragment(args), false);
                return true;
            case AUX:
                clearBackStack();
                replaceFragment(createAuxFragment(args), false);
                return true;
            case TI:
                clearBackStack();
                replaceFragment(createTiFragment(args), false);
                return true;
            case DAB:
                clearBackStack();
                replaceFragment(createDabFragment(args), false);
                return true;
            case HD_RADIO:
                clearBackStack();
                replaceFragment(createHdRadioFragment(args), false);
                return true;
            case SOURCE_OFF:
                clearBackStack();
                replaceFragment(createSourceOffFragment(args), false);
                return true;
            case UNSUPPORTED:
                clearBackStack();
                replaceFragment(createUnsupportedFragment(args), false);
                return true;
            case PLAYER_CONTAINER:
                return true;
        }
        return false;
    }

    /**
     * 遷移元画面に戻る
     *
     * @return boolean
     */
    public boolean goBack() {
        Fragment dialog;
        for (String key : KEY_DIALOGS) {
            dialog = mFragmentManager.findFragmentByTag(key);
            if (dialog != null) {
                ((DialogFragment) dialog).dismiss();
                if (key.equals(TAG_DIALOG_USB_LIST)) {
                    //リスト退場
                    mMediaCase.exitList();
                }
                return true;
            }
        }

        for (String key : KEY_CONTAINERS) {
            dialog = mFragmentManager.findFragmentByTag(key);
            if (dialog instanceof OnGoBackListener) {
                if (((OnGoBackListener) dialog).onGoBack()) {
                    return true;
                } else {
                    ((DialogFragment) dialog).dismiss();
                    //リスト退場
                    mMediaCase.exitList();
                    return true;
                }
            }
        }

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

    /**
     * ダイアログをクローズする
     *
     * @param screenId ScreenId
     * @return boolean
     */
    public boolean close(ScreenId screenId) {
        switch (screenId) {
            case RADIO_PTY_SELECT:
                if (isShowPtySelect()) {
                    dismissPtySelect();
                }
                return true;
            case SOURCE_SELECT:
                if(isShowSourceSelect()){
                    dismissSourceSelect();
                }
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
        // 引数4つのsetCustomAnimationsだとGalaxyJ3Proでアニメーションが有効にならない
        // tr.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        tr.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        tr.replace(mContainerViewId, fragment);
        if (isAddToBackStack) {
            tr.addToBackStack(null);
        }
        tr.commitAllowingStateLoss();
    }


    /**
     * ラジオリスト表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showRadioTabContainerDialogFragment(Bundle args) {
        createRadioTabContainerDialogFragment(args).show(mFragmentManager, TAG_DIALOG_RADIO_LIST_CONTAINER);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * ラジオリスト閉幕
     */
    public void dismissRadioTabContainerDialogFragment() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_RADIO_LIST_CONTAINER);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
            mMediaCase.exitList();
        }
    }

    /**
     * ラジオリスト表示確認
     *
     * @return リストが表示されているか否か
     */
    public boolean isShowRadioTabContainer() {
        return mFragmentManager.findFragmentByTag(TAG_DIALOG_RADIO_LIST_CONTAINER) != null;
    }

    /**
     * 音楽リスト表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showPlayerTabContainer(Bundle args) {
        createPlayerTabContainerDialogFragment(args).show(mFragmentManager, TAG_DIALOG_PLAYER_LIST_CONTAINER);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * 音楽リスト閉幕
     */
    public void dismissPlayerTabContainer() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_PLAYER_LIST_CONTAINER);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
            mMediaCase.exitList();
        }
    }

    /**
     * 音楽リスト表示確認
     *
     * @return リストが表示されているか否か
     */
    public boolean isShowPlayerTabContainer() {
        return mFragmentManager.findFragmentByTag(TAG_DIALOG_PLAYER_LIST_CONTAINER) != null;
    }

    /**
     * ソース選択表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showSourceSelect(Bundle args) {
        createSourceSelectContainerFragment(args).show(mFragmentManager, TAG_DIALOG_SOURCE_SELECT);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * ソース選択表示確認
     */
    public boolean isShowSourceSelect() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_SOURCE_SELECT) != null);
    }

    /**
     * ソース選択閉幕
     */
    public void dismissSourceSelect() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_SOURCE_SELECT);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * NowPlayingList表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showNowPlayingList(Bundle args) {
        createNowPlayingListContainerFragment(args).show(mFragmentManager, TAG_DIALOG_NOW_PLAYING_LIST);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * NowPlayingList閉幕
     */
    public void dismissNowPlayingList() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_NOW_PLAYING_LIST);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * NowPlayingList表示確認
     *
     * @return NowPlayingListが表示されているか否か
     */
    public boolean isShowNowPlayingList() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_NOW_PLAYING_LIST) != null);
    }

    /**
     * UsbList表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showUsbList(Bundle args) {
        createUsbListFragment(args).show(mFragmentManager, TAG_DIALOG_USB_LIST);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * UsbList閉幕
     */
    public void dismissUsbList() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_USB_LIST);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
            mMediaCase.exitList();
        }
    }

    /**
     * UsbList表示確認
     *
     * @return UsbListが表示されているか否か
     */
    public boolean isShowUsbList() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_USB_LIST) != null);
    }

    /**
     * リスト表示確認
     *
     * @return リストが表示されているか否か
     */
    public boolean isShowListDialog() {
        return mFragmentManager.findFragmentByTag(TAG_DIALOG_RADIO_LIST_CONTAINER) != null
                || mFragmentManager.findFragmentByTag(TAG_DIALOG_PLAYER_LIST_CONTAINER) != null
                || mFragmentManager.findFragmentByTag(TAG_DIALOG_NOW_PLAYING_LIST) != null
                || mFragmentManager.findFragmentByTag(TAG_DIALOG_USB_LIST) != null
                || mFragmentManager.findFragmentByTag(TAG_DIALOG_SOURCE_SELECT) != null;
    }

    /**
     * PTYSelectダイアログ表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showPtySelect(Fragment fragment, Bundle args) {
        createPtySelectDialogFragment(fragment, args).show(mFragmentManager, TAG_DIALOG_PTY_SELECT);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * PTYSelectダイアログ閉幕
     */
    public void dismissPtySelect() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_PTY_SELECT);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * PTYSelectダイアログ表示確認
     *
     * @return PTYSearchが表示されているか否か
     */
    public boolean isShowPtySelect() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_PTY_SELECT) != null);
    }

    @VisibleForTesting
    DialogFragment createSourceSelectContainerFragment(Bundle args) {
        return SourceSelectContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createNowPlayingListContainerFragment(Bundle args) {
        return NowPlayingListContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createUsbListFragment(Bundle args) {
        return UsbListContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createRadioFragment(Bundle args) {
        return RadioFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSxmFragment(Bundle args) {
        return SxmFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAndroidMusicFragment(Bundle args) {
        return AndroidMusicFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createCdFragment(Bundle args) {
        return CdFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createUsbFragment(Bundle args) {
        return UsbFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAuxFragment(Bundle args) {
        return AuxFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createPandoraFragment(Bundle args) {
        return PandoraFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSpotifyFragment(Bundle args) {
        return SpotifyFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createBtAudioFragment(Bundle args) {
        return BtAudioFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createTiFragment(Bundle args) {
        return TiFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createDabFragment(Bundle args) {
        return DabFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createHdRadioFragment(Bundle args) {
        return HdRadioFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSourceOffFragment(Bundle args) {
        return SourceOffFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createUnsupportedFragment(Bundle args) {
        return UnsupportedFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createRadioTabContainerDialogFragment(Bundle args) {
        return RadioTabContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createPlayerTabContainerDialogFragment(Bundle args) {
        return PlayerTabContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createPtySelectDialogFragment(Fragment fragment, Bundle args) {
        return SingleChoiceDialogFragment.newInstance(fragment, args);
    }

}
