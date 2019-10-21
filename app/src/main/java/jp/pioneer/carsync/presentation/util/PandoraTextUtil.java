package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.text.TextUtils;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.PandoraMediaInfo;

/**
 * Created by NSW00_007906 on 2017/06/08.
 */

public class PandoraTextUtil {
    private static final String EMPTY = "";

    public static String getStationInfo(Context context, PandoraMediaInfo info) {
        String text;
        switch(info.playbackMode) {
            case STOP:
            case ERROR:
                text = EMPTY;
                break;
            default:
                text = TextUtils.isEmpty(info.stationName) ?
                        context.getResources().getString(R.string.ply_023) : info.stationName;
                break;
        }
        return text;
    }

    public static String getSongTitle(Context context, PandoraMediaInfo info) {
        String text;
        switch(info.playbackMode) {
            case STOP:
            case ERROR:
                text = EMPTY;
                break;
            default:
                text = TextUtils.isEmpty(info.songTitle) ?
                        context.getString(R.string.ply_024) : info.songTitle;
                break;
        }
        return text;
    }

    public static String getArtistName(Context context, PandoraMediaInfo info) {
        String text;
        switch(info.playbackMode) {
            case STOP:
            case ERROR:
                text = EMPTY;
                break;
            default:
                text = TextUtils.isEmpty(info.artistName) ?
                        context.getString(R.string.ply_021) : info.artistName;
                break;
        }
        return text;
    }

    public static String getAlbumName(Context context, PandoraMediaInfo info) {
        String text;
        switch(info.playbackMode) {
            case STOP:
            case ERROR:
                text = EMPTY;
                break;
            default:
                text = TextUtils.isEmpty(info.albumName) ?
                        context.getString(R.string.ply_020) : info.albumName;
                break;
        }
        return text;
    }
}
