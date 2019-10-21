package jp.pioneer.carsync.presentation.view;

import android.database.Cursor;
import android.os.Bundle;

import jp.pioneer.carsync.domain.model.PlaybackMode;

/**
 * NowPlayingListの抽象クラス
 */

public interface NowPlayingListView {

    /**
     * 曲の設定
     *
     * @param data 曲のカーソル
     * @param args 曲のバンドル
     */
    void setSongCursor(Cursor data, Bundle args);

    /**
     * 選択位置の設定
     *
     * @param position 選択位置
     */
    void setSelection(int position);

    /**
     * 再生中曲のIDの設定
     *
     * @param trackNo 再生中曲のトラック
     * @param id 再生中曲のID
     * @param playbackMode 再生状態
     */
    void setNowPlaySong(int trackNo, long id , PlaybackMode playbackMode);

}
