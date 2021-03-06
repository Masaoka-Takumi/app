package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;

public interface DabEnsembleListView {
    /**
     * アダプター設定
     *
     * @param cursor カーソル
     */
    void setCursor(Cursor cursor);
    void setSelectedPositionNotScroll(int position);
}
