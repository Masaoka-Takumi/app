package jp.pioneer.carsync.presentation.model;

import jp.pioneer.carsync.domain.model.SubDisplayInfo;

import static jp.pioneer.carsync.domain.model.SubDisplayInfo.*;

/**
 * 楽曲リスト種別.
 */
public enum AndroidMusicListType {
    TOP_SONG(false, true, 1, TOP_LIST),
    SONG(true, false, 2, SONGS),
    TOP_PLAYLIST(false, true, 1, TOP_LIST),
    PLAYLIST(true, true, 2, PLAYLISTS),
    PLAYLIST_SONG(true, false, 3, PLAYLISTS),
    TOP_ALBUM(false, true, 1, TOP_LIST),
    ALBUM(true, true, 2, ALBUMS),
    ALBUM_SONG(true, false, 3, ALBUMS),
    TOP_ARTIST(false, true, 1, TOP_LIST),
    ARTIST(true, true, 2, ARTISTS),
    ARTIST_ALBUM(true ,true, 3, ARTISTS),
    ARTIST_ALBUM_SONG(true, false, 4, ARTISTS),
    TOP_GENRE(false, true, 1, TOP_LIST),
    GENRE(true, true, 2, GENRES),
    GENRE_SONG(true, false, 3, GENRES)
    ;

    /** 上位層があるか否か. */
    public final boolean hasParent;

    /** 下位層があるか否か. */
    public final boolean hasChild;

    /** 階層数. */
    public final int position;

    /** サブディスプレイ情報. */
    public final SubDisplayInfo displayInfo;

    AndroidMusicListType(boolean hasParent,
                         boolean hasChild,
                         int position,
                         SubDisplayInfo displayInfo){
        this.hasParent = hasParent;
        this.hasChild = hasChild;
        this.position = position;
        this.displayInfo = displayInfo;
    }
}
