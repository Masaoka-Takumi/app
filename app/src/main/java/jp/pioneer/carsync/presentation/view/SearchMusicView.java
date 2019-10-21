package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;

/**
 * 音楽検索画面のinterface
 */

public interface SearchMusicView {

    /**
     * アーティスト検索結果の反映
     *
     * @param data 検索結果
     */
    void setArtistCursor(Cursor data);

    /**
     * アルバム検索結果の反映
     *
     * @param data 検索結果
     */
    void setAlbumCursor(Cursor data);

    /**
     * 楽曲検索結果の反映
     *
     * @param data 検索結果
     */
    void setMusicCursor(Cursor data);

    /**
     * 楽曲検索結果ダイアログを閉幕
     */
    void closeDialog();
}
