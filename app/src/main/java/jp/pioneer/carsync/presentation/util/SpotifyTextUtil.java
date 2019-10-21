package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.SpotifyMediaInfo;

/**
 * SpotifyのTextView用Util
 */

public class SpotifyTextUtil {
    private static final String EMPTY = "";
    private static final String STOP = "Stop";
    public static String getMiniPlayerTitle(SpotifyMediaInfo info) {
        return info.trackNameOrSpotifyError;
    }

    public static String getMiniPlayerDescription(SpotifyMediaInfo info) {
        return info.artistName;
    }

    public static String getSongTitle(Context context, @NonNull SpotifyMediaInfo info) {
        String text;
        switch(info.playbackMode) {
            case STOP:
                text = EMPTY;
                break;
            case ERROR:
                text = TextUtils.isEmpty(info.trackNameOrSpotifyError) ? EMPTY : info.trackNameOrSpotifyError;
                break;
            default:
                text = TextUtils.isEmpty(info.trackNameOrSpotifyError) ? context.getResources().getString(R.string.ply_024)
                        : info.trackNameOrSpotifyError;
                break;
        }
        return text;
    }

    public static String getArtistName(Context context, @NonNull SpotifyMediaInfo info) {
        String text;
        switch(info.playbackMode) {
            case STOP:
            case ERROR:
                text =  EMPTY;
                break;
            default:
                text = TextUtils.isEmpty(info.artistName) ? context.getResources().getString(R.string.ply_021)
                        : info.artistName;
                break;
        }
        return text;
    }

    public static String getAlbumTitle(Context context, @NonNull SpotifyMediaInfo info) {
        String text;
        switch(info.playbackMode) {
            case STOP:
            case ERROR:
                text = EMPTY;
                break;
            default:
                text = TextUtils.isEmpty(info.albumName) ? context.getResources().getString(R.string.ply_020)
                        : info.albumName;
                break;
        }
        return text;
    }

    public static String getPlayingTrackSource(Context context, @NonNull SpotifyMediaInfo info) {
        String text;
        switch(info.playbackMode) {
            case STOP:
            case ERROR:
                text = EMPTY;
                break;
            default:
                text = TextUtils.isEmpty(info.playingTrackSource)||info.radioPlaying ? context.getResources().getString(R.string.ply_022)
                        : info.playingTrackSource;
                break;
        }
        return text;
    }
}

