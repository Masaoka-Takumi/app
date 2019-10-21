package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 車載機側のソース状態.
 */
public enum MediaSourceStatus {
    /** ソース切り替え開始. */
    CHANGING(0x00),
    /** ソース切り替え完了. */
    CHANGE_COMPLETED(0x0F)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    MediaSourceStatus(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するMediaSourceStatus
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static MediaSourceStatus valueOf(byte code) {
        for (MediaSourceStatus value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
