package jp.pioneer.carsync.infrastructure.crp.event;

import android.support.annotation.Nullable;

import jp.pioneer.carsync.infrastructure.crp.IncomingPacketIdType;

/**
 * StatusHolder更新イベント.
 * <p>
 * StatusHolderが更新された場合に発生する。
 */
public class CrpStatusUpdateEvent {
    /** ヒント. */
    public final IncomingPacketIdType hint;

    /**
     * コンストラクタ.
     *
     * @param hint ヒント。StatusHolderを更新する要因となった受信パケットIDタイプを指定する。
     *             受信パケットIDタイプで何が更新されるか知っている前提だが、何も無いよりかはまし。
     */
    public CrpStatusUpdateEvent(@Nullable IncomingPacketIdType hint) {
        this.hint = hint;
    }

    /**
     * コンストラクタ.
     */
    public CrpStatusUpdateEvent() {
        this(null);
    }
}
