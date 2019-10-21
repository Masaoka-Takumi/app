package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;

/**
 * Created by NSW00_007906 on 2017/12/22.
 */

public interface UsbListView {
    /**
     * タイトル設定
     *
     * @param title タイトル
     */
    void setTitle(String title);

    /**
     * アダプター設定
     *
     * @param cursor カーソル
     */
    void setCursor(Cursor cursor);

    /**
     * ダイアログを閉じる
     */
    void closeDialog();

    /**
     * カーソル更新
     */
    void updateCursor();

    /**
     * 初期更新
     */
    void setFirst(boolean first);

    /**
     * BackButton表示非表示設定
     */
    void setBackButtonVisible(boolean visible);

    /**
     * 選択中ポジションの設定
     *
     * @param position 選択中ポジション
     */
    void setSelectedPosition(int position);

    void setSelectedPositionNotScroll(int position);

    /**
     * 選択ポジションの取得
     */
    int getSelectPosition();

    /**
     * 選択項目カーソルの取得
     *
     * @param position ポジション
     */
    Cursor getItem(int position);
}
