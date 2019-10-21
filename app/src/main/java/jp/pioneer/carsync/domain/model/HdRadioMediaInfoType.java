package jp.pioneer.carsync.domain.model;

/**
 * オーディオ情報種別（HD Radio）.
 */
public class HdRadioMediaInfoType {
    /** 放送局情報 / LINKING. */
    public static final int STATION_INFO = 0x00;
    /** Song Title. */
    public static final int SONG_TITLE = 0x01;
    /** Artist Name. */
    public static final int ARTIST_NAME = 0x02;
    /** MULTICAST PROGRAM NUMBER. */
    public static final int MULTICAST_PROGRAM_NUMBER = 0x03;
}
