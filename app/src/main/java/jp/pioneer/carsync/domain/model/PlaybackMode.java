package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 再生状態.
 * <p>
 * 定義値はメディア系（Tuner系以外）のものであり、Tuner系は定義値を個々で判断する。（列挙値のみ利用する）
 */
public enum PlaybackMode {
    /** Stop. */
    STOP(0x00),
    /** Pause. */
    PAUSE(0x01),
    /** Play. */
    PLAY(0x02),
    /** Fast Forward. */
    FAST_FORWARD(0x03),
    /** Rewind. */
    REWIND(0x04),
    /** ERROR. */
    ERROR(0x07),
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    PlaybackMode(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するPlaybackMode
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static PlaybackMode valueOf(byte code) {
        for (PlaybackMode value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
