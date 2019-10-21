package jp.pioneer.carsync.domain.model;

/**
 * 時刻表示.
 */
public enum TimeFormatSetting {
    /** 12時間表示 */
    TIME_FORMAT_12() {
        @Override
        public TimeFormatSetting toggle() {
            return TIME_FORMAT_24;
        }
    },
    /** 24時間表示 */
    TIME_FORMAT_24(){
        @Override
        public TimeFormatSetting toggle() {
            return TIME_FORMAT_12;
        }
    }
    ;

    /**
     * コンストラクタ.
     */
    TimeFormatSetting() {
    }

    /**
     * トグル.
     *
     * @return トグル後のAttMuteSetting
     */
    public abstract TimeFormatSetting toggle();
}
