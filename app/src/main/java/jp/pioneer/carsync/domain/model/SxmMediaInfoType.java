package jp.pioneer.carsync.domain.model;

/**
 * オーディオ情報種別（SiriusXm）.
 */
public class SxmMediaInfoType {
    /** チャンネル/チャンネルネーム or Advisory Message. */
    public static final int CHANNEL_AND_CHANNEL_NAME_OR_ADVISORY_MESSAGE = 0;
    /** Artist Name/Content Info. */
    public static final int ARTIST_NAME_OR_CONTENT_INFO = 1;
    /** Song Title. */
    public static final int SONG_TITLE = 2;
    /** Category Name. */
    public static final int CATEGORY_NAME = 3;
}
