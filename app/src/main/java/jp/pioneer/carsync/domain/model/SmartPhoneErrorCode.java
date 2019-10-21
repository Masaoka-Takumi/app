package jp.pioneer.carsync.domain.model;

/**
 * SmartPhoneエラーコード.
 *
 * 何も定義が無いので、UNKNOWNをとりあえず定義している。
 */
public enum SmartPhoneErrorCode {
    UNKNOWN(0x00);

    /** プロトコルでの定義値. */
    public final long code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    SmartPhoneErrorCode(long code) {
        this.code = code;
    }
}
