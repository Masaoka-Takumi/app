package jp.pioneer.carsync.presentation.view.argument;

import android.os.Bundle;

import icepick.Icepick;
import icepick.State;

/**
 * 音楽選択パラメータ
 */

public class MusicParams {
    private static final String PARAGRAPH = ">";
    @State public String pass;
    @State public long artistId;
    @State public long albumId;
    @State public long songId;
    @State public long playlistId;
    @State public long genreId;
    @State public boolean isShuffle = false;
    @State public boolean isFocus = false;
    /**
     * バンドル化
     *
     * @return Bundle
     */
    public Bundle toBundle() {
        Bundle args = new Bundle();
        Icepick.saveInstanceState(this, args);
        return args;
    }

    /**
     * ディレクトリの変更
     *
     * @param directory 変更後のディレクトリ
     */
    public void changeDirectory(String directory) {
        StringBuilder buffer = new StringBuilder();
        if (pass != null) {
            buffer.append(pass);
            buffer.append(PARAGRAPH);
        }
        buffer.append(directory);
        pass = buffer.toString();
    }

    /**
     * 復元
     *
     * @param args Bundle
     * @return MusicParams
     */
    public static MusicParams from(Bundle args) {
        MusicParams params = new MusicParams();
        Icepick.restoreInstanceState(params, args);
        return params;
    }
}
