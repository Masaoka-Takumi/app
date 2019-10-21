package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.text.TextUtils;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;

/**
 * AndroidMusicのTextView用Util
 */

public class AndroidMusicTextUtil {
    private static final String EMPTY = "";

    public static String getSongTitle(Context context, SmartPhoneStatus status, AndroidMusicMediaInfo info) {
        String text;
        switch(status.playbackMode) {
            case STOP:
            case ERROR:
                text = EMPTY;
                break;
            default:
                text = getString(context, info.songTitle, R.string.ply_024);
                break;
        }
        return text;
    }

    public static String getArtistName(Context context, SmartPhoneStatus status, AndroidMusicMediaInfo info) {
        String text;
        switch(status.playbackMode) {
            case STOP:
            case ERROR:
                text =  EMPTY;
                break;
            default:
                text = getString(context, info.artistName, R.string.ply_021);
                break;
        }
        return text;
    }

    public static String getAlbumTitle(Context context, SmartPhoneStatus status, AndroidMusicMediaInfo info) {
        String text;
        switch(status.playbackMode) {
            case STOP:
            case ERROR:
                text = EMPTY;
                break;
            default:
                text = getString(context, info.albumTitle, R.string.ply_020);
                break;
        }
        return text;
    }

    public static String getGenreName(Context context, SmartPhoneStatus status, AndroidMusicMediaInfo info) {
        String text;
        switch(status.playbackMode) {
            case STOP:
            case ERROR:
                text = EMPTY;
                break;
            default:
                text = getString(context, info.genre, R.string.ply_022);
                break;
        }
        return text;
    }

    public static String getString(Context context, String text, int defaultStringId) {
        if (!TextUtils.isEmpty(text)) {
            return text;
        } else {
            return context.getString(defaultStringId);
        }
    }


}
