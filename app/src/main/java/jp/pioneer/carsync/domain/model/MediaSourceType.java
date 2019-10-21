package jp.pioneer.carsync.domain.model;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * ソース種別.
 */
public enum MediaSourceType {
    /** Radio. */
    RADIO(0x00),
    /** DAB. */
    DAB(0x01),
    /** SiriusXM. */
    SIRIUS_XM(0x02),
    /** HD Radio. */
    HD_RADIO(0x03),
    /** CD. */
    CD(0x04),
    /** USB. */
    USB(0x05),
    /** AUX. */
    AUX(0x06),
    /** BT Audio. */
    BT_AUDIO(0x07),
    /** BT Phone. */
    BT_PHONE(0x08),
    /** OFF. */
    OFF(0x09),
    /** Pandora. */
    PANDORA(0x0A),
    /** Spotify. */
    SPOTIFY(0x0B),
    /** App Music（Android Music）. */
    APP_MUSIC(0x0C),
    /** iPod. */
    IPOD(0x0D),
    /** TI. */
    TI(0x0E),
    /** TTS. */
    TTS(0x0F),
    /** 音声認識. */
    VR(0x10),
    /** [Ver2.5] DAB割り込み. */
    DAB_INTERRUPT(0x11),
    /** [Ver2.5] HD Radio割り込み. */
    HD_RADIO_INTERRUPT(0x12),
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    MediaSourceType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するMediaSourceType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static MediaSourceType valueOf(byte code) {
        for (MediaSourceType value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * チューナー系ソースか否か取得.
     *
     * @return {@code true}:チューナー系ソースである。{@code false}:それ以外。
     */
    public boolean isTunerSource() {
        return this == RADIO || this == DAB || this == HD_RADIO || this == SIRIUS_XM;
    }

    /**
     * リスト機能に対応しているか否か取得.
     *
     * @return {@code true}:リスト機能に対応している。{@code false}:それ以外。
     */
    public boolean isListSupported() {
        return isTunerSource() || this == APP_MUSIC || this == USB;
    }

    /**
     * P.CHリストに対応しているか否か取得.
     *
     * @return {@code true}:P.CHリストに対応している。{@code false}:それ以外
     */
    public boolean isPchListSupported() {
        return isTunerSource() && this != DAB;
    }
}
