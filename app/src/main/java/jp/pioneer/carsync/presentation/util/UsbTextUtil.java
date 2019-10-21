package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.text.TextUtils;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.UsbMediaInfo;

/**
 * Created by NSW00_007906 on 2017/06/08.
 */

public class UsbTextUtil {
    private static final String EMPTY = "";

    public static String getSongTitle(Context context, UsbMediaInfo info) {
        return TextUtils.isEmpty(info.songTitle) ?
                context.getString(R.string.ply_024) : info.songTitle;
    }

    public static String getSongTitleForPlayer(Context context, UsbMediaInfo info) {
        return info.playbackMode == PlaybackMode.STOP ? EMPTY : getSongTitle(context, info);
    }

    public static String getArtistName(Context context, UsbMediaInfo info) {
        return TextUtils.isEmpty(info.artistName) ?
                context.getString(R.string.ply_021) : info.artistName;
    }

    public static String getArtistNameForPlayer(Context context, UsbMediaInfo info) {
        String text;
        if (info.playbackMode == PlaybackMode.STOP) {
            // アイコン表示は不要
            text = EMPTY;
        } else {
            text = getArtistName(context, info);
        }
        return text;
    }

    public static String getAlbumNameForPlayer(Context context, UsbMediaInfo info) {
        return info.playbackMode == PlaybackMode.STOP ? EMPTY : getAlbumName(context, info);
    }

    public static String getAlbumName(Context context, UsbMediaInfo info) {
        return TextUtils.isEmpty(info.albumName) ?
                context.getString(R.string.ply_020) : info.albumName;
    }

    public static String getTrackNumberForPlayer(CarDeviceStatus status, UsbMediaInfo info) {
        return (info.musicProtected || info.drmSkipped || info.unplayableFile) ?
                null : getTrackNumber(info);
    }

    public static String getTrackNumber(UsbMediaInfo info) {
        return info.trackNumber;
    }
}
