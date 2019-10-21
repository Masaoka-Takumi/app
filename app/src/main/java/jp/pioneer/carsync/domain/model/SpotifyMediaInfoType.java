package jp.pioneer.carsync.domain.model;

/**
 * オーディオ情報種別（Spotify）.
 */
public class SpotifyMediaInfoType {
    /** Track Name or Spotify ERROR. */
    public static final int TRACK_NAME_OR_SPOTIFY_MESSAGE = 0;
    /** Artist Name. */
    public static final int ARTIST_NAME = 1;
    /** Album Name. */
    public static final int ALBUM_NAME = 2;
    /** 再生曲の再生源. */
    public static final int PLAYING_TRACK_SOURCE = 3;
}
