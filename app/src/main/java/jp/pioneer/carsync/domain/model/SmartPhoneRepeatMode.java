package jp.pioneer.carsync.domain.model;

/**
 * SmartPhoneメディア（AppMusic）のリピートモード.
 * <p>
 * 車載機メディアは{@link CarDeviceRepeatMode}
 */
public enum SmartPhoneRepeatMode {
    /** OFF. */
    OFF(0x00) {
        /**
         * {@inheritDoc}
         */
        @Override
        public SmartPhoneRepeatMode toggle() {
            return ONE;
        }
    },
    /** 1曲リピート. */
    ONE(0x01) {
        /**
         * {@inheritDoc}
         */
        @Override
        public SmartPhoneRepeatMode toggle() {
            return ALL;
        }
    },
    /** 全曲リピート. */
    ALL(0x02) {
        /**
         * {@inheritDoc}
         */
        @Override
        public SmartPhoneRepeatMode toggle() {
            return OFF;
        }
    };

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    SmartPhoneRepeatMode(int code) {
        this.code = code;
    }

    /**
     * トグル.
     *
     * @return トグル後のSmartPhoneRepeatMode
     */
    public SmartPhoneRepeatMode toggle() {
        return this;
    }
}
