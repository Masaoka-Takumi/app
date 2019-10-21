package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * EQ設定.
 * <p>
 * UI層向け
 * SPECIALはUIに必要ないためUI向けに定義
 */
public enum SoundFxSettingEqualizerType {
    /** SUPER_BASS. */
    SUPER_BASS(AudioSettingEqualizerType.SUPER_BASS.code, R.string.val_086),
    /** POWERFUL. */
    POWERFUL(AudioSettingEqualizerType.POWERFUL.code, R.string.val_087),
    /** NATURAL. */
    NATURAL(AudioSettingEqualizerType.NATURAL.code, R.string.val_088),
    /** VOCAL. */
    VOCAL(AudioSettingEqualizerType.VOCAL.code, R.string.val_089),
    /** TODOROKI. */
    TODOROKI(AudioSettingEqualizerType.TODOROKI.code, R.string.val_245),
    /** POP_ROCK. */
    POP_ROCK(AudioSettingEqualizerType.POP_ROCK.code, R.string.val_228),
    /** ELECTRONICA. */
    ELECTRONICA(AudioSettingEqualizerType.ELECTRONICA.code, R.string.val_229),
    /** EQ_SAMBA. */
    EQ_SAMBA(AudioSettingEqualizerType.EQ_SAMBA.code, R.string.val_230),
    /** SERTANEJO. */
    SERTANEJO(AudioSettingEqualizerType.SERTANEJO.code, R.string.val_231),
    /** PRO. */
    PRO(AudioSettingEqualizerType.PRO.code, R.string.val_232),
    /** FLAT. */
    FLAT(AudioSettingEqualizerType.FLAT.code, R.string.val_085),
    /** COMMON_CUSTOM. */
    COMMON_CUSTOM(AudioSettingEqualizerType.COMMON_CUSTOM.code, R.string.val_090),
    /** COMMON_CUSTOM_2ND. */
    COMMON_CUSTOM_2ND(AudioSettingEqualizerType.COMMON_CUSTOM_2ND.code, R.string.val_091),
    /** CLEAR. */
    CLEAR(AudioSettingEqualizerType.CLEAR.code, R.string.val_233),
    /** VIVID. */
    VIVID(AudioSettingEqualizerType.VIVID.code, R.string.val_235),
    /** DYNAMIC. */
    DYNAMIC(AudioSettingEqualizerType.DYNAMIC.code, R.string.val_236),
    /** JAZZ. */
    JAZZ(AudioSettingEqualizerType.JAZZ.code, R.string.val_237),
    /** FORRO. */
    FORRO(AudioSettingEqualizerType.FORRO.code, R.string.val_238),

    // SpecialEqualizer
    SPECIAL_DEBUG_1((1 << 8) | 0xF0, R.string.val_246),
    SPECIAL_DEBUG_2((1 << 8) | 0xF1, R.string.val_247),

    // SpecialEQ種別が不明だった場合
    UNKNOWN((1 << 8) | 0xFF, R.string.unknown)
    ;

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
    SoundFxSettingEqualizerType(int code, @StringRes int label){
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値取得.
     *
     * @return プロトコルでの定義値
     */
    public int getCode() {
        return code;
    }

    /**
     * 表示用文字列リソースID取得.
     *
     * @return 表示用文字列リソースID
     */
    @StringRes
    public int getLabel() {
        return label;
    }

    /**
     * CustomEQか否か.
     */
    public boolean isCustomEq(){
        return this == COMMON_CUSTOM || this == COMMON_CUSTOM_2ND;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSoundFxSettingEqualizerType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SoundFxSettingEqualizerType valueOfEq(byte code) {
        for (SoundFxSettingEqualizerType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSoundFxSettingEqualizerType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SoundFxSettingEqualizerType valueOfSpecialEq(byte code) {
        for (SoundFxSettingEqualizerType value : values()) {
            if(value.code >= (1 << 8)){
                if ((value.code & 0xFF) == PacketUtil.ubyteToInt(code)) {
                    return value;
                }
            }
        }

        return UNKNOWN;
    }
}
