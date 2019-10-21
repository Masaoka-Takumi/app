package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.text.TextUtils;

import java.util.Locale;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.domain.util.TextMatchingUtil;

/**
 * Created by tsuyosh on 2015/04/14.
 */
public class DabTextUtil {
	private static final String EMPTY = "";
	private Context mContext;


	public static String getServiceComponentLabelForPlayer(Context context, DabInfo info) {
		String text;
		if (info.isPauseStatus()) {
			text = context.getString(R.string.ply_095);
		} else if (info.tunerStatus == TunerStatus.ERROR) {
			text = context.getString(R.string.ply_099);
		} else if (info.isNoSignal()) {
			text = context.getString(R.string.ply_089);
		} else if (info.isNoService()) {
			text = context.getString(R.string.ply_090);
/*		} else if (info.tunerStatus == TunerStatus.LIST_UPDATE) {
			text = context.getString(R.string.ply_096);
		} else if (info.tunerStatus == TunerStatus.SEEK) {
			text = context.getString(R.string.ply_069);*/
		} else {
			text = getServiceComponentLabel(info);
		}
		return text;
	}

	public static String getServiceComponentLabel(DabInfo info) {
		return info.serviceComponentLabel;
	}

	public static String getServiceNumberForPlayer(DabInfo info) {
		String text;
/*		if (info.isPauseStatus()) {
			text = EMPTY;
		} else */if (info.isErrorStatus() || info.isSearchStatus()) {
			text = EMPTY;
		} else {
			return getServiceNumber(info);
		}
		return text;
	}

	public static String getServiceNumber(DabInfo info) {
		return info.serviceNumber != null ? info.serviceNumber : EMPTY;
	}

	public static String getDynamicLabelForPlayer(CarDeviceStatus status, DabInfo info) {
		String text;
		if (info.isPauseStatus()) {
			text = EMPTY;
		} else if (info.isErrorStatus() || info.isSearchStatus()) {
			text = EMPTY;
		} else {
			text = getDynamicLabel(info);
		}
		return text;
	}

	public static String getDynamicLabel(DabInfo info) {
		return info.dynamicLabel;
	}

	public static String getPtyInfoForPlayer(Context context, CarDeviceStatus status, DabInfo info) {
		String text;
/*		if (info.isPauseStatus()) {
			text = EMPTY;
		} else */
		if (info.isErrorStatus() || info.isSearchStatus()) {
			text = EMPTY;
		} else if(TextMatchingUtil.equals(info.ptyInfo, "NO PTY")){
			text = context.getString(R.string.ply_057);
		} else {
			text = getPtyInfo(info);
		}
		return text;
	}

	public static String getPtyInfo(DabInfo info) {
		return info.ptyInfo;
	}

	public  static String getBandName(Context context, DabInfo info) {
		return info.band != null ? context.getString(info.band.label) : EMPTY;
	}

	public static String getFmLink(Context context, DabInfo info) {
		return info.tunerStatus == TunerStatus.FM_LINK ?
				context.getString(R.string.ply_091) : null;
	}

	public static String getFavoriteName(Context context, DabInfo info) {
		return TextUtils.isEmpty(info.serviceComponentLabel) ?
				context.getString(R.string.ply_024) : info.serviceComponentLabel;
	}

	public static String getFavoriteDescription(Context context, DabInfo info) {
		return String.format(Locale.ENGLISH, "%s %s",
				info.band.label, FrequencyUtil.toString(context, info.currentFrequency, info.frequencyUnit)
		);
	}

	public static String getMiniPlayerPrimaryText(DabInfo info) {
		if (info == null) return EMPTY;
		return String.format(Locale.ENGLISH, "%s %s",
				info.band.getLabel(), info.serviceComponentLabel);
	}

	/**
	 * リスト画面用のService Nameを返します
	 * @param item
	 * @return
	 */
	public static String getServiceNameForList(ListInfo.DabListItem item) {
		return item.text;
	}
}
