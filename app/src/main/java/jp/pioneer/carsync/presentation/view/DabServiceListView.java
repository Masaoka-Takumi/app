package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;

import jp.pioneer.carsync.domain.model.ListType;

public interface DabServiceListView {
    /**
     * アダプター設定
     *
     * @param cursor カーソル
     */
    void setCursor(Cursor cursor, ListType listType);
    void setSelectedPositionNotScroll(int position);
    void setAbcSearchResult(boolean result);
}
