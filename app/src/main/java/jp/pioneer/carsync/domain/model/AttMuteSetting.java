package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * ATT/MUTE設定.
 */
public enum AttMuteSetting {
    /** MUTE. */
    MUTE(0x00){
        @Override
        public AttMuteSetting toggle() {
            return ATT;
        }
    },
    /** ATT. */
    ATT(0x01) {
        @Override
        public AttMuteSetting toggle() {
            return MUTE;
        }
    }
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    AttMuteSetting(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するAttMuteSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static AttMuteSetting valueOf(byte code) {
        for (AttMuteSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のAttMuteSetting
     */
    public abstract AttMuteSetting toggle();
}
