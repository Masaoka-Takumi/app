package jp.pioneer.carsync.presentation.view;

import android.util.SparseBooleanArray;

import java.util.ArrayList;

import jp.pioneer.carsync.presentation.model.CustomKeyItem;
import jp.pioneer.carsync.presentation.model.YouTubeLinkSearchItem;
import jp.pioneer.carsync.presentation.view.adapter.YouTubeLinkSearchItemAdapter;

public interface YouTubeLinkSearchItemDialogView {

    /**
     * アダプター設定
     *
     * @param listItem リスト
     */
    void setListItem(ArrayList<YouTubeLinkSearchItem> listItem);

    /**
     * 選択中Itemを設定する。
     *
     * @param positions 有効な位置
     */
    void setCheckedItemPositions(SparseBooleanArray positions);

    /**
     * 選択中Item一覧の取得
     *
     * @return SparseBooleanArray 有効な位置
     */
    SparseBooleanArray getCheckedItemPositions();

    /**
     * ダイアログ終了
     */
    void callbackClose();
}
