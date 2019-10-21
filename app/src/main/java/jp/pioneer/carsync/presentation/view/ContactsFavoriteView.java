package jp.pioneer.carsync.presentation.view;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

/**
 * 電話帳 お気に入りリストの抽象クラス
 */

public interface ContactsFavoriteView {

    /**
     * お気に入りリスト親項目表示
     * @param data リスト親項目のカーソル
     */
    void setGroupCursor(Cursor data, Bundle args);

    /**
     * お気に入りリスト子項目表示
     * @param position 親項目のポジション
     * @param data 子項目のカーソル
     */
    void setChildrenCursor(int position, Cursor data);

    /**
     * 連絡先に発信
     * @param intent インテント
     */
    void dial(Intent intent);
}
