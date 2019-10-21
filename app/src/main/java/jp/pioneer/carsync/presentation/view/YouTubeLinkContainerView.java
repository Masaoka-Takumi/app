package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;

public interface YouTubeLinkContainerView extends OnNavigateListener {

    /**
     * キーボードを閉じる
     */
    void closeKeyBoard();

    /**
     * ダイアログ終了(ラストソース復帰による間接的)
     */
    void closeContainerDialogByChangeLastSource();

    /**
     * ダイアログ終了(割り込み画面表示によるもの)
     */
    void closeContainerDialogResetLastSource();

    /**
     * ダイアログ終了
     */
    void dismissDialog();
}
