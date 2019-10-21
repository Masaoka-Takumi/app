package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * スピーカー種別.
 */
public enum SpeakerType {
    // --Standard mode--
    /** Front. */
    FRONT(0x00, AudioOutputMode.STANDARD),
    /** Rear. */
    REAR(0x01, AudioOutputMode.STANDARD),
    /** Subwoofer. */
    SUBWOOFER_STANDARD_MODE(0x02, AudioOutputMode.STANDARD),
    // --2way Network Mode--
    /** High. */
    HIGH(0x03, AudioOutputMode.TWO_WAY_NETWORK),
    /** Mid-HPF. */
    MID_HPF(0x04, AudioOutputMode.TWO_WAY_NETWORK),
    /** Mid-LPF. */
    MID_LPF(0x05, AudioOutputMode.TWO_WAY_NETWORK),
    /** Subwoofer. */
    SUBWOOFER_2WAY_NETWORK_MODE(0x06, AudioOutputMode.TWO_WAY_NETWORK)
    ;

    /** プロトコルでの定義値. */
    public final int code;
    /** Audio output mode. */
    public final AudioOutputMode mode;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param mode Audio output mode
     */
    SpeakerType(int code, @NonNull AudioOutputMode mode) {
        this.code = code;
        this.mode = mode;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSpeakerType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SpeakerType valueOf(byte code) {
        for (SpeakerType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
