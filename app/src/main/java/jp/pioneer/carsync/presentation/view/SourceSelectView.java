package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import java.util.List;

import jp.pioneer.carsync.presentation.model.SourceSelectItem;

/**
 * ソース選択の抽象クラス
 */

public interface SourceSelectView {
    /**
     * ソース選択リストの設定
     * @param selectItems ソース選択リスト
     */
    void setAdapter(List<SourceSelectItem> selectItems);

    /**
     * 現在のソース設定
     * @param position 選択ソースの位置
     */
    void setCurrentSource(int position);

    /**
     * UIColorの設定
     * @param color 設定色
     */
    void setColor(@ColorRes int color);

    /**
     * ダイアログ消去
     */
    void dismissDialog();
}
