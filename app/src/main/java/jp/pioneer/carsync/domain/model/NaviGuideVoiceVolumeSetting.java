package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * ナビガイド音声ボリューム設定.
 */
public enum NaviGuideVoiceVolumeSetting {
    /** 極小. */
    MINIMUM(0x00, R.string.val_223),
    /** 小. */
    SMALL(0x01, R.string.val_224),
    /** 中. */
    MEDIUM(0x02, R.string.val_225),
    /** 大. */
    LARGE(0x03, R.string.val_226),
    /** 極大. */
    MAXIMUM(0x04, R.string.val_227);

    /** プロトコルでの定義値. */
    public final int code;

    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param label 表示用文字列リソースID
     */
    NaviGuideVoiceVolumeSetting(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するNaviGuideVolumeSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static NaviGuideVoiceVolumeSetting valueOf(byte code) {
        for (NaviGuideVoiceVolumeSetting value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
