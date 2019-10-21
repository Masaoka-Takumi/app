package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

/**
 * 設定リスト情報.
 */
public class SettingListInfo {
    public final SettingListType type;
    public final SparseArrayCompat<SettingListItem> items = new SparseArrayCompat<>();

    public SettingListInfo(@NonNull SettingListType type) {
        this.type = type;
    }

    public void reset() {
        synchronized (items) {
            items.clear();
        }
    }

    public SparseArrayCompat<SettingListItem> cloneItems() {
        synchronized (items) {
            return items.clone();
        }
    }
}
