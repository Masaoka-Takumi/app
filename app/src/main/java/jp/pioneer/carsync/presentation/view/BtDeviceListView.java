package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;
import android.os.Bundle;

/**
 * BTデバイスリスト画面の抽象クラス.
 */
public interface BtDeviceListView {
    /**
     * BTデバイスの設定.
     *
     * @param data BTデバイスのカーソル
     * @param args BTデバイスのバンドル
     */
    void setDeviceCursor(Cursor data, Bundle args);

    /**
     * Addボタン有効無効の設定.
     *
     * @param isEnabled 有効無効
     */
    void setAddButtonEnabled(boolean isEnabled);

    /**
     * Deleteボタン有効無効の設定.
     *
     * @param isEnabled 有効無効
     */
    void setDeleteButtonEnabled(boolean isEnabled);

    /**
     * 設定無効状態の設定
     *
     * @param isEnabled 有効/無効
     */
    void setEnable(boolean isEnabled);

    void updateListView();
}
