package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;
import android.os.Bundle;

/**
 * アーティストリストの抽象クラス
 */

public interface ArtistsView {
    /**
     * アーティストの設定
     * @param data アーティストのカーソル
     * @param args アーティストのバンドル
     */
    void setArtistCursor(Cursor data, Bundle args);

    /**
     * 選択中セクションの取得
     */
    int getSectionIndex();

    /**
     * 選択セクションの設定
     *
     * @param sectionIndex セクションインデックス
     */
    void setSectionIndex(int sectionIndex);

    /**
     * 選択ポジションの取得
     */
    int getSelectPosition();

    /**
     * 選択ポジションの設定
     *
     * @param position ポジション
     */
    void setSelectPosition(int position);

    /**
     * 選択項目IDの取得
     *
     * @param position ポジション
     */
    long getItemId(int position);

    /**
     * 選択項目カーソルの取得
     *
     * @param position ポジション
     */
    Cursor getItem(int position);

    /**
     * 全項目数の取得
     */
    int getItemsCount();

    /**
     * 全セクション数の取得
     */
    int getSectionCount();

    /**
     * セクションの頭文字の取得
     *
     * @param sectionIndex セクションインデックス
     */
    String getSectionString(int sectionIndex);
}
