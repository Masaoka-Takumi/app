package jp.pioneer.carsync.infrastructure.crp.event;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.infrastructure.crp.entity.ListUpdateType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * リスト情報更新イベント.
 * <p>
 * 車載機側ソースのリストでリスト内容が更新された場合に発生する。
 * このイベントを受けたらリスト情報を再取得する。
 */
public class CrpListUpdateEvent {
    /** リスト更新種別. */
    public final ListUpdateType type;

    /**
     * コンストラクタ.
     *
     * @param type リスト更新種別
     * @throws NullPointerException {@code type}がnull
     */
    public CrpListUpdateEvent(@NonNull ListUpdateType type) {
        this.type = checkNotNull(type);
    }
}
