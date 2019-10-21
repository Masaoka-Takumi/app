package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;

/**
 * 連絡帳検索画面のinterface
 */

public interface SearchContactView {
    /**
     * 緊急連絡先親項目表示
     *
     * @param data 緊急連絡先親項目のカーソル
     */
    void setGroupCursor(Cursor data);

    /**
     * 緊急連絡先子項目表示
     *
     * @param position 親項目のポジション
     * @param data     緊急連絡先子項目のカーソル
     */
    void setChildrenCursor(int position, Cursor data);

    /**
     * 発話準備
     *
     * @param number 発話先電話番号
     */
    void dial(String number);

    /**
     * 楽曲検索結果ダイアログを閉幕
     */
    void closeDialog();

}
