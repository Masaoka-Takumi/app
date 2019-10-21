package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 設定リストトランザクション.
 */
public class SettingListTransaction {
    private static final int MAX_TRANSACTION_ID = 65535;
    private static int sId;
    /** トランザクションID. */
    public int id;
    /** リスト種別. */
    public SettingListType listType;
    /** 件数. */
    public int total;
    /** リストインデックス. */
    public int listIndex;
    /** リスト項目. */
    public SparseArrayCompat<SettingListItem> items;

    /**
     * 初期情報設定.
     *
     * @param listType リスト種別
     * @param total 件数
     * @throws NullPointerException {@code listType}がnull
     */
    public void setInitialInfo(@NonNull SettingListType listType, int total) {
        this.listType = checkNotNull(listType);
        this.total = total;
        this.items = new SparseArrayCompat<>();
        this.listIndex = 0;
    }

    /**
     * さらに項目があるか否か取得.
     * <p>
     * {@code true}となった場合、項目を件数分取得し終えていない。
     *
     * @return {@code true}:項目がある。{@code false}:それ以外。（項目を全て取得した）
     */
    public boolean hasNext() {
        if (total == 0 || listIndex + 1 > total) {
            return false;
        } else {
            id = nextTransactionId();
            ++listIndex;
            return true;
        }
    }

    private static int nextTransactionId() {
        if (++sId > MAX_TRANSACTION_ID) {
            sId = 1;
        }

        return sId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("id", id)
                .add("listType", listType)
                .add("total", total)
                .add("listIndex", listIndex)
                .toString();
    }
}
