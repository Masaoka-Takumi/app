package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Listening Position設定.
 */
public enum ListeningPositionSetting {
    /** OFF. */
    OFF(0x00),
    /** Front-Left. */
    FRONT_LEFT(0x01),
    /** Front-Right. */
    FRONT_RIGHT(0x02),
    /** Front. */
    FRONT(0x03),
    /** ALL. */
    ALL(0x04, false)
    ;

    /** プロトコルでの定義値. */
    public final int code;
    /** 2wayNetworkModeをサポートするか否か. */
    public final boolean twoWayNetworkModeSupported;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ListeningPositionSetting(int code) {
        this(code, true);
    }

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param supported 2wayNetworkModeをサポートするか否か
     */
    ListeningPositionSetting(int code, boolean supported) {
        this.code = code;
        this.twoWayNetworkModeSupported = supported;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するListeningPositionSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static ListeningPositionSetting valueOf(byte code) {
        for (ListeningPositionSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }

    /**
     * サポートするか否か取得.
     * <p>
     * 2wayNetworkModeにおいて{@link #ALL}は設定出来ないので、その判定を行う便利メソッド。
     *
     * @param mode Audio output mode
     * @return {@code true}:サポートする。{@code false}:それ以外。
     */
    public boolean isSupported(AudioOutputMode mode) {
        return mode != AudioOutputMode.TWO_WAY_NETWORK || twoWayNetworkModeSupported;
    }
}
