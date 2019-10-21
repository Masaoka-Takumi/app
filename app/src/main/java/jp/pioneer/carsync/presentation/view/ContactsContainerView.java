package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import jp.pioneer.carsync.presentation.presenter.ContactsContainerPresenter;
import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;

/**
 * 電話帳 コンテナの抽象クラス
 */

public interface ContactsContainerView extends OnNavigateListener {

    /**
     * 現在の選択タブ設定
     * @param tab 現在の選択タブ
     */
    void setCurrentTab(ContactsContainerPresenter.ContactsTab tab);

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    void setColor(@ColorRes int color);

    void callbackClose();
}
