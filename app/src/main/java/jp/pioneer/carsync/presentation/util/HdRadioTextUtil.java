package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;

import java.util.Locale;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.HdRadioDigitalAudioStatus;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.TunerStatus;

/**
 * Created by tsuyosh on 2015/04/14.
 */
public class HdRadioTextUtil {
	private static final String EMPTY = "";

	public static CharSequence getFrequencyForPlayer(Context context, HdRadioInfo info, float proportion) {
		if (info.frequencyUnit != null) {
			SpannableStringBuilder ssb = new SpannableStringBuilder();
			ssb.append(FrequencyUtil.toString(context, info.currentFrequency, info.frequencyUnit, false));
			int start = ssb.length();
			ssb.append(context.getString(info.frequencyUnit.label));
			int end = ssb.length();
			ssb.setSpan(new RelativeSizeSpan(proportion), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			return ssb;
		} else {
			return null;
		}
	}

	public static String getStationInfo(HdRadioInfo info) {
		return info.stationInfo;
	}

	public static String getStationInfoForPlayer(Context context, CarDeviceStatus status, HdRadioInfo info) {
		String text;
		if (info.tunerStatus != null && info.tunerStatus == TunerStatus.SEEK) {
			text = context.getString(R.string.ply_030);
		} else if (info.tunerStatus != null && info.tunerStatus == TunerStatus.BSM) {
			text = context.getString(R.string.ply_012);
		} else {
			if(info.hdRadioDigitalAudioStatus!=null) {
				if (info.hdRadioDigitalAudioStatus == HdRadioDigitalAudioStatus.RECEIVING) {
					text = info.stationInfo;
				}else{
					text = !TextUtils.isEmpty(info.stationInfo) ? info.stationInfo : EMPTY;
				}
			}else{
				text = context.getString(R.string.ply_086);
			}
		}
		return text;
	}

	public static String getSongTitleForPlayer(Context context, HdRadioInfo info) {
		if (info.isSearchStatus()) {
			return EMPTY;
		} else {
			if (TextUtils.isEmpty(info.songTitle)) {
				return context.getString(R.string.ply_024);
			} else {
				return info.songTitle;
			}
		}

	}

	public static String getArtistNameForPlayer(Context context, HdRadioInfo info) {
		if (info.isSearchStatus()) {
			return EMPTY;
		} else {
			if (TextUtils.isEmpty(info.artistName)) {
				return context.getString(R.string.ply_021);
			} else {
				return info.artistName;
			}
		}
	}

	public static String getMulticastProgramNumber(Context context, HdRadioInfo info) {
		if(info.hdRadioDigitalAudioStatus!=null) {
			switch (info.hdRadioDigitalAudioStatus) {
				case RECEIVING:
					if(!TextUtils.isEmpty(info.multicastProgramNumber)) {
						return context.getString(R.string.ply_085) + info.multicastProgramNumber;
					}else{
						return EMPTY;
					}
				default:
					// アナログ時非表示 #451
					return EMPTY;
			}
		}
		return EMPTY;
	}

	public static String getBandName(Context context, HdRadioInfo info) {
		return info.band != null ? context.getString(info.band.label) : EMPTY;
	}

	public static String getDigitalAudioStatus(Context context, HdRadioInfo info) {
		String text;
		if (info.isSearchStatus()) {
			text = EMPTY;
		} else if(info.hdRadioDigitalAudioStatus!=null){
			switch (info.hdRadioDigitalAudioStatus) {
				case RECEIVING:
					text = context.getString(R.string.ply_087);
					break;
				case NOT_RECEIVING:
					text = context.getString(R.string.ply_088);
					break;
				default:
					text = EMPTY;
			}
		}else{
			text = EMPTY;
		}
		return text;
	}

	public static String getFavoriteName(Context context, HdRadioInfo info) {
		return TextUtils.isEmpty(info.stationInfo) ?
				context.getString(R.string.ply_024) : info.stationInfo;
	}

	public static String getFavoriteDescription(Context context, HdRadioInfo info) {
		return String.format(Locale.ENGLISH, "%s %s",
				info.band.label, FrequencyUtil.toString(context, info.currentFrequency, info.frequencyUnit)
		);
	}

	public static String getMiniPlayerPrimaryText(Context context, HdRadioInfo info) {
		if (info == null) return EMPTY;
		return String.format(Locale.ENGLISH, "%s %s",
				info.band.getLabel(),
				TextUtils.isEmpty(info.stationInfo) ?
						FrequencyUtil.toString(context, info.currentFrequency, info.frequencyUnit) :
						info.stationInfo
		);
	}
}
