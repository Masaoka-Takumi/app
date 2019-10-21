package jp.pioneer.carsync.infrastructure.crp.event;

import jp.pioneer.carsync.domain.model.SettingListType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 設定リスト情報更新イベント.
 * <p>
 * 車載機側ソースの設定リストでリスト内容が更新された場合に発生する。
 * このイベントを受けたらリスト情報を再取得する。
 */
public class CrpSettingListUpdateEvent {
    /** 設定リスト種別. */
    public final SettingListType type;

    /**
     * コンストラクタ.
     *
     * @param type 設定リスト種別
     * @throws NullPointerException {@code type}がnull
     */
    public CrpSettingListUpdateEvent(SettingListType type) {
        this.type = checkNotNull(type);
    }
}
