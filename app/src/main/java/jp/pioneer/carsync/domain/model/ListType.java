package jp.pioneer.carsync.domain.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * リスト種別.
 */
public enum ListType {
    /** リスト状態以外. */
    NOT_LIST(0x00),
    /** リスト遷移不可状態. */
    LIST_UNAVAILABLE(0x01),
    /** P.CHリスト. */
    PCH_LIST(0x02, MediaSourceType.RADIO, MediaSourceType.SIRIUS_XM, MediaSourceType.HD_RADIO),
    /** Serviceリスト. */
    SERVICE_LIST(0x02, MediaSourceType.DAB),
    /** リスト. */
    LIST(0x02, MediaSourceType.APP_MUSIC, MediaSourceType.USB),
    /** ABCサーチリスト. */
    ABC_SEARCH_LIST(0x03, MediaSourceType.DAB, MediaSourceType.APP_MUSIC),
    /** 退場（リスト遷移通知コマンドの退場時に使用する。車載機の状態としては通知されないはず。）. */
    EXIT(0xFF)
    ;

    /** プロトコルでの定義値. */
    public final int code;
    /** {@link #code}になり得るソース種別. */
    public final Set<MediaSourceType> types;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param types {@link #code}になり得るソース種別群
     */
    ListType(int code, MediaSourceType... types) {
        this.code = code;
        this.types = new HashSet<>();
        if (types != null) {
            this.types.addAll(Arrays.asList(types));
        }
    }

    /**
     * リスト遷移通知で入場を通知可能な種別か否か取得.
     *
     * @return {@code true}:通知可能。{@code false}:それ以外。
     */
    public boolean canEnter() {
        return this == NOT_LIST || this == ABC_SEARCH_LIST;
    }

    /**
     * リスト遷移通知で退場を通知可能な種別か否か取得.
     *
     * @return {@code true}:通知可能。{@code false}:それ以外。
     */
    public boolean canExit() {
        return this != NOT_LIST && this != LIST_UNAVAILABLE;
    }

    /**
     * プロトコルでの定義値とソース種別から取得.
     *
     * @param code プロトコルでの定義値
     * @param type ソース種別
     * @return プロトコルでの定義値とソース種別に該当するListType
     * @throws IllegalArgumentException プロトコルでの定義値とソース種別に該当するものがない
     */
    public static ListType valueOf(byte code, MediaSourceType type) {
        for (ListType value : values()) {
            if (value.code == ubyteToInt(code) && (value.types.isEmpty() || value.types.contains(type))) {
                return value;
            }
        }

        throw new IllegalArgumentException(String.format(Locale.US, "invalid code: %d, type: %s", code, type));
    }
}
