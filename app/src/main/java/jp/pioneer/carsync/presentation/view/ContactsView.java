package jp.pioneer.carsync.presentation.view;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

/**
 * 電話帳 連絡先リストの抽象クラス
 */

public interface ContactsView {

    /**
     * 連絡先親項目表示
     * @param data 連絡先親項目のカーソル
     */
    void setGroupCursor(Cursor data, Bundle args);

    /**
     * 連絡先子項目表示
     * @param position 親項目のポジション
     * @param data 連絡先子項目のカーソル
     */
    void setChildrenCursor(int position, Cursor data);

    /**
     * 連絡先に発信
     * @param intent インテント
     */
    void dial(Intent intent);
}
