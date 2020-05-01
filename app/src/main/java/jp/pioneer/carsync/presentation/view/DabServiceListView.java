package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;

import jp.pioneer.carsync.domain.model.ListType;

public interface DabServiceListView {
    /**
     * アダプター設定
     *
     * @param cursor   カーソル
     * @param listType ListType
     * @param isSph    専用機かどうか
     */
    void setCursor(Cursor cursor, ListType listType, boolean isSph);

    void setSelectedPositionNotScroll(int position);

    void setAbcSearchResult(boolean result);
}
