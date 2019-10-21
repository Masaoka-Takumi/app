package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import jp.pioneer.carsync.domain.model.MusicCategory;
import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;

/**
 * ローカル再生リストタブコンテナのinterface
 */

public interface PlayerTabContainerView extends OnNavigateListener {
    /**
     * タイトル設定
     *
     * @param title タイトル
     */
    void setTitle(String title);

    /**
     * カテゴリー設定
     *
     * @param category タイトル
     */
    void setCategory(MusicCategory category);

    void setCategoryEnabled(boolean isEnabled);

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    void setColor(@ColorRes int color);

    /**
     * リスト階層の判定
     */
    boolean isFirstList();

    void closeDialog();
}
