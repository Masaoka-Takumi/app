package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * HDデジタル音声受信状態.
 */
public enum HdRadioDigitalAudioStatus {
    /** デジタル音声未取得（アナログ受信中）. */
    NOT_RECEIVING(0x00),
    /** デジタル音声取得中（デジタル受信中）. */
    RECEIVING(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    HdRadioDigitalAudioStatus(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するHdRadioDigitalAudioStatus
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static HdRadioDigitalAudioStatus valueOf(byte code) {
        for (HdRadioDigitalAudioStatus value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }
}
