package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;
import android.os.Bundle;

/**
 * 衝突検知機能設定 緊急連絡先設定の抽象クラス
 */

public interface ImpactDetectionContactSettingView {
    
    /**
     * 現在の緊急連絡先設定
     * @param name 現在の緊急連絡先のlookupキー
     */
    void setTargetContact(String name);

    /**
     * 緊急連絡先親項目表示
     * @param data 緊急連絡先親項目のカーソル
     */
    void setGroupCursor(Cursor data, Bundle args);

    /**
     * 緊急連絡先子項目表示
     * @param position 親項目のポジション
     * @param data 緊急連絡先子項目のカーソル
     */
    void setChildrenCursor(int position, Cursor data);
}
