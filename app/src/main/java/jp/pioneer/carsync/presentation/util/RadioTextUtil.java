package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.text.TextUtils;

import java.util.Locale;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.CarDeviceDestinationInfo;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.CarRunningStatus;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;

/**
 * Created by NSW00_007906 on 2017/06/08.
 */

public class RadioTextUtil {
    private static final String EMPTY = "";

    /**
     * お気に入り登録/ユーザーPCH登録/YouTubeLink検索Tag用のPS情報の文字列を返す
     */
    public static String getPsInfoForFavorite(int deviceDestination, CarRunningStatus runningStatus, RadioInfo info) {
        String psInfoText;
        if(deviceDestination == CarDeviceDestinationInfo.JP.code){
            psInfoText = RadioTextUtil.getPsInfoForListJP(runningStatus, info.band, info.currentFrequency);
        }else{
            psInfoText = TextUtils.isEmpty(info.psInfo) ? "" : info.psInfo;
        }
        return psInfoText;
    }

    /**
     * 連携中の車載機がJP仕向けの場合のチューナー系リスト用のPS情報の文字列を返す
     */
    public static String getPsInfoForListJP(CarRunningStatus status, RadioBandType band, long frequency) {
        return RadioStationNameUtil.getStationName(status,band,frequency);
    }

    /**
     * Home画面用PS情報の文字列を返す（現在使用していない）
     */
    public static String getPsInfoForMiniPlayer(Context context, int deviceDestination, CarRunningStatus status, RadioInfo info) {
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
                    text = getPsInfoForPlayer(context, deviceDestination, status, info);
                    break;
            }
        } else {
            text = getPsInfoForPlayer(context, deviceDestination, status, info);
        }

        return text;
    }

    /**
     * 再生画面用PS情報の文字列を返す
     */
    public static String getPsInfoForPlayer(Context context, int deviceDestination, CarRunningStatus status, RadioInfo info) {
        String text;
        if (deviceDestination == CarDeviceDestinationInfo.JP.code) {
            text = getPsInfoForPlayerJP(context, status, info);
        } else {
            switch (info.tunerStatus) {
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
                    if (!TextUtils.isEmpty(info.psInfo) &&
                            (info.getBand() != null && info.getBand().isFMVariant())) {
                        text = info.psInfo;
                    } else {
                        text = EMPTY;
                    }
                    break;
            }
        }
        return text;
    }

    private static String getPsInfoForPlayerJP(Context context, CarRunningStatus status, RadioInfo info) {
        String text;
        switch(info.tunerStatus){
            case BSM:
                text = context.getString(R.string.ply_012);
                break;
            case PI_SEARCH:
            case SEEK:
                text = context.getString(R.string.ply_030);
                break;
            default:
                text = RadioStationNameUtil.getStationName(status,info.band,info.currentFrequency);
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
