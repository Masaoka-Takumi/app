package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.text.TextUtils;

import java.util.Locale;

import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;

/**
 * Created by NSW00_007906 on 2017/10/26.
 */

public class SxmTextUtil {
    private static final String EMPTY = "";

    /**
     * Channel Number を頭に付加しない、素の Channel Name / Advisory Message を返す
     * @param info
     * @return
     */
    public static String getRawChannelName(SxmMediaInfo info) {
        return info.channelAndChannelNameOrAdvisoryMessage;
    }

    /**
     * Channel Name を返す
     * @param info
     * @return
     */
    public static String getChannelName(SxmMediaInfo info) {
        return getRawChannelName(info);
    }

    public static String formatChannelNumber(int channelNumber) {
        return String.format(Locale.ENGLISH, " %03d", channelNumber);
    }

    public static String getArtistName(SxmMediaInfo info) {
        if (info.isErrorStatus()) {
            // Invisible
            return EMPTY;
        } else if (info.isCh000()) {
            return info.artistNameOrContentInfo;
        } else {
            return info.artistNameOrContentInfo;
        }
    }

    public static String getSongTitle(SxmMediaInfo info) {
        if (info.isErrorStatus()) {
            // Invisible
            return EMPTY;
        } else {
            return info.songTitle;
        }
    }

    public static String getCategoryName(SxmMediaInfo info) {
        if (info.isCh000() || info.isErrorStatus()) {
            // Invisible
            return EMPTY;
        } else {
            return info.categoryName;
        }
    }

    public static String getBandName(SxmMediaInfo info) {
        if (info.band != null) {
            return info.band.name();
        } else {
            return EMPTY;
        }
    }

    /**
     * チューナーリスト表示用のチャンネルネームを返す
     * @param item
     * @return
     */
    public static String getChannelNameForList(ListInfo.SxmListItem item) {
        if (TextUtils.isEmpty(item.text)) {
            return String.valueOf(item.channelNumber);
        } else {
            return item.text;
        }
    }

    public static String getMiniPlayerTitle(Context context, SxmMediaInfo info) {
        String text = getRawChannelName(info);
        if (TextUtils.isEmpty(text)) {
            return EMPTY;
        } else {
            return String.format(Locale.ENGLISH, "%s", text);
        }
    }

    public static String getMiniPlayerDescription(SxmMediaInfo info) {
        return null;
    }

    public static String getName(SxmMediaInfo info) {
        return getRawChannelName(info);
    }

    public static String getDescription(Context context, SxmMediaInfo info) {
        return String.format(Locale.ENGLISH, "%s-%s", context.getResources().getString(info.band.getLabel()), info.currentChannelNumber);
    }


    public static String getCurrentChannel(SxmMediaInfo info) {
        if (info.isErrorStatus()) {
            return EMPTY;
        } else {
            return "CH" + formatChannelNumber(info.currentChannelNumber);
        }
    }
}
