package jp.pioneer.carsync.domain.model;

/**
 * SmartPhoneから通知するオーディオ情報種別.
 */
public enum SmartPhoneMediaInfoType {
    /** Song Title. */
    SONG_NAME(0x00),
    /** Artist Name. */
    ARTIST_NAME(0x01),
    /** Album Title. */
    ALBUM_NAME(0x02),
    /** Genre. */
    GENRE(0x03)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    SmartPhoneMediaInfoType(int code) {
        this.code = code;
    }
}
