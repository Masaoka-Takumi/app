package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;
import android.os.Bundle;

/**
 * ラジオお気に入り画面のinterface
 */

public interface RadioFavoriteView {

    /**
     * お気に入りリストの設定
     *
     * @param data Cursor
     * @param args Bundle
     */
    void setCursor(Cursor data, Bundle args);
}
