package jp.pioneer.carsync.presentation.util;

import android.content.Context;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.CdInfo;

/**
 * Created by NSW00_007906 on 2017/06/08.
 */

public class CdTextUtil {

    public static String getTrackNumber(Context context, CdInfo info) {
        return info.trackNumber == null ? context.getString(R.string.ply_024) : info.trackNumber;
    }

    public static String getArtistName(Context context, CdInfo info) {
        return info.artistName == null ? context.getString(R.string.ply_021) : info.artistName;
    }

    public static String getDiscTitle(Context context, CdInfo info) {
        return info.discTitle == null ? context.getString(R.string.ply_020) : info.discTitle;
    }
}
