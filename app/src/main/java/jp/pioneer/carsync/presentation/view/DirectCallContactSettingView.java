package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;
import android.os.Bundle;

/**
 * 連絡先選択リスト画面の抽象クラス
 */

public interface DirectCallContactSettingView {
    /**
     * 選択されている連絡先を設定する
     * @param lookupkey 選択されているlookupkey
     */
    void setTargetContact(String lookupkey);

    /**
     * 連絡先を設定する
     * @param data 連絡先カーソル
     */
    void setGroupCursor(Cursor data, Bundle args);

    /**
     * 連絡先選択時の詳細情報を設定する
     * @param position 選択位置
     * @param data 選択カーソル
     */
    void setChildrenCursor(int position, Cursor data);
}
