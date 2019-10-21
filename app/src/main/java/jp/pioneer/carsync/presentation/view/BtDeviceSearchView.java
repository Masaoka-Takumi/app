package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;
import android.os.Bundle;

/**
 * Created by NSW00_008320 on 2017/07/26.
 */

public interface BtDeviceSearchView {
    /**
     * BTデバイスの設定.
     *
     * @param data BTデバイスのカーソル
     * @param args BTデバイスのバンドル
     */
    void setDeviceCursor(Cursor data, Bundle args);

    /**
     * 設定無効状態の設定
     *
     * @param isEnabled 有効/無効
     * @param isSearching 検索中
     */
    void setEnable(boolean isEnabled, boolean isSearching);

    /**
     * ペアリング中ダイアログの非表示
     */
    void dismissPairingDialog();

    /**
     * Toast表示
     */
    void showToast(String str);
}
