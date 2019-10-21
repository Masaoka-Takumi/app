package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;

/**
 * 音声認識検索コンテナのinterface
 */

public interface SearchContainerView extends OnNavigateListener {
    /**
     * 検索タイトル設定
     *
     * @param title 検索ワード
     * @param isResult 検索結果リストか
     */
    void setTitle(String title, boolean isResult);

    void onClose();
}
