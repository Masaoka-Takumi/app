package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.text.TextUtils;

import java.util.Locale;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.RadioInfo;

/**
 * Created by NSW00_007906 on 2017/06/08.
 */

public class RadioTextUtil {
    private static final String EMPTY = "";

    /**
     * チューナー系リスト用のPS情報の文字列を返す
     *
     * @param context
     * @param item
     * @return
     */
    public static String getPsInfoForList(Context context, ListInfo.RadioListItem item) {
        if (TextUtils.isEmpty(item.text)) {
            return FrequencyUtil.format(context, item.frequency, item.frequencyUnit, true);
        } else {
            return item.text;
        }
    }

    public static String getPsInfoForMiniPlayer(Context context, CarDeviceStatus status, RadioInfo info) {
        String text;
        if(info.rdsInterruptionType != null) {
            switch (info.rdsInterruptionType) {
                case TA:
                    text = context.getString(R.string.ply_035);
                    break;
                case NEWS:
                    text = context.getString(R.string.ply_019);
                    break;
                case ALARM:
                    text = context.getString(R.string.ply_005);
                    break;
                case NORMAL:
                default:
                    text = getPsInfoForPlayer(context, status, info);
                    break;
            }
        } else {
            text = getPsInfoForPlayer(context, status, info);
        }

        return text;
    }

    public static String getPsInfoForPlayer(Context context, CarDeviceStatus status, RadioInfo info) {
        String text;
        switch(info.tunerStatus){
            case BSM:
                text = context.getString(R.string.ply_012);
                break;
            case PI_SEARCH:
            case SEEK:
                text = context.getString(R.string.ply_030);
                break;
            case PTY_SEARCH:
                text = context.getString(R.string.ply_042);
                break;
            default:
                if(!TextUtils.isEmpty(info.psInfo) &&
                        (info.getBand() != null && info.getBand().isFMVariant())){
                    text = info.psInfo;
                } else {
                    text = EMPTY;
                }
                break;
        }
        return text;
    }

    public static String getPsInfo(Context context, RadioInfo info) {
        return TextUtils.isEmpty(info.psInfo) ?
                FrequencyUtil.toString(context, info.currentFrequency, info.frequencyUnit) : info.psInfo;
    }

    public static String getPtyInfo(RadioInfo info) {
        return info.ptyInfo;
    }

    public static CharSequence getPtyInfoForPlayer(Context context, RadioInfo info) {
        CharSequence text;
        if (info.isSearchStatus()) {
            text = EMPTY;
        } else if(!TextUtils.isEmpty(info.songTitle)){
            if(info.isNoTitle()){
                if(TextUtils.isEmpty(info.ptyInfo)){
                    text = context.getString(R.string.ply_024);
                } else if(info.isNoPty()){
                    text = context.getString(R.string.ply_057);
                } else {
                    text = info.ptyInfo;
                }
            } else {
                text = info.songTitle;
            }
        } else {
            if(TextUtils.isEmpty(info.ptyInfo)){
                text = EMPTY;
            } else if(info.isNoPty()){
                text = context.getString(R.string.ply_057);
            } else {
                text = info.ptyInfo;
            }
        }

        return text;
    }

    public static CharSequence getSongTitle(Context context, RadioInfo info) {
        return TextUtils.isEmpty(info.songTitle)
                ? context.getString(R.string.ply_024) : info.songTitle;
    }

    public static String getArtistNameForPlayer(Context context, RadioInfo info) {
        String text;
        if (info.isSearchStatus()) {
            text = EMPTY;
        } else {
            text = getArtistName(context, info);
        }

        return text;
    }

    public static String getArtistName(Context context, RadioInfo info) {
        return TextUtils.isEmpty(info.artistName)
                ? context.getString(R.string.ply_021) : info.artistName;
    }

    public static String getBandName(Context context, RadioInfo info) {
        return info.band != null ? context.getString(info.band.label) : "";
    }

    public static String getFavoriteName(Context context, RadioInfo info) {
        return TextUtils.isEmpty(info.ptyInfo) ? context.getString(R.string.ply_024) : info.ptyInfo;
    }

    public static String getMiniPlayerPrimaryText(Context context, RadioInfo info) {
        if (info.band == null) return EMPTY;
        return String.format(Locale.ENGLISH, "%s-%s",
                context.getResources().getString(info.band.getLabel()),
                TextUtils.isEmpty(info.psInfo) ?
                        FrequencyUtil.toString(context, info.currentFrequency, info.frequencyUnit) :
                        info.psInfo
        );
    }

    public static String getFavoriteDescription(Context context, RadioInfo info) {
        return String.format(Locale.ENGLISH, "%s-%s",
                context.getResources().getString(info.band.getLabel()), FrequencyUtil.toString(context, info.currentFrequency, info.frequencyUnit)
        );
    }
}
