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
    SUPER_BASS(AudioSettingEqualizerType.SUPER_BASS.code, R.string.val_086,"Superbass"),
    /** POWERFUL. */
    POWERFUL(AudioSettingEqualizerType.POWERFUL.code, R.string.val_087,"Powerful"),
    /** NATURAL. */
    NATURAL(AudioSettingEqualizerType.NATURAL.code, R.string.val_088,"Natural"),
    /** VOCAL. */
    VOCAL(AudioSettingEqualizerType.VOCAL.code, R.string.val_089,"Vocal"),
    /** TODOROKI. */
    TODOROKI(AudioSettingEqualizerType.TODOROKI.code, R.string.val_245,"Todoroki"),
    /** POP_ROCK. */
    POP_ROCK(AudioSettingEqualizerType.POP_ROCK.code, R.string.val_228,"Pop Rock"),
    /** ELECTRONICA. */
    ELECTRONICA(AudioSettingEqualizerType.ELECTRONICA.code, R.string.val_229,"Eletronica"),
    /** EQ_SAMBA. */
    EQ_SAMBA(AudioSettingEqualizerType.EQ_SAMBA.code, R.string.val_230,"Samba"),
    /** SERTANEJO. */
    SERTANEJO(AudioSettingEqualizerType.SERTANEJO.code, R.string.val_231,"Sertanejo"),
    /** PRO. */
    PRO(AudioSettingEqualizerType.PRO.code, R.string.val_232,"Pro"),
    /** FLAT. */
    FLAT(AudioSettingEqualizerType.FLAT.code, R.string.val_085,"Flat"),
    /** COMMON_CUSTOM. */
    COMMON_CUSTOM(AudioSettingEqualizerType.COMMON_CUSTOM.code, R.string.val_090,"Custom1"),
    /** COMMON_CUSTOM_2ND. */
    COMMON_CUSTOM_2ND(AudioSettingEqualizerType.COMMON_CUSTOM_2ND.code, R.string.val_091,"Custom2"),
    /** CLEAR. */
    CLEAR(AudioSettingEqualizerType.CLEAR.code, R.string.val_233,"Clear"),
    /** VIVID. */
    VIVID(AudioSettingEqualizerType.VIVID.code, R.string.val_235,"Vivid"),
    /** DYNAMIC. */
    DYNAMIC(AudioSettingEqualizerType.DYNAMIC.code, R.string.val_236,"Dynamic"),
    /** JAZZ. */
    JAZZ(AudioSettingEqualizerType.JAZZ.code, R.string.val_237,"Jazz"),
    /** FORRO. */
    FORRO(AudioSettingEqualizerType.FORRO.code, R.string.val_238,"Forro"),

    // SpecialEqualizer
    SPECIAL_DEBUG_1((1 << 8) | 0xF0, R.string.val_246,"Special EQ 1"),
    SPECIAL_DEBUG_2((1 << 8) | 0xF1, R.string.val_247,"Special EQ 2"),

    // SpecialEQ種別が不明だった場合
    UNKNOWN((1 << 8) | 0xFF, R.string.unknown,"Unknown")
    ;

    /** プロトコルでの定義値. */
    public final int code;
    /** 表示用文字列リソースID. */
    @StringRes public final int label;
    /** Analytics用文字列. */
    public final String strValue;
    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param label 表示用文字列リソースID
     */
    SoundFxSettingEqualizerType(int code, @StringRes int label, String strValue){
        this.code = code;
        this.label = label;
        this.strValue = strValue;
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
     * Analytics用文字列取得.
     */
    public String getAnalyticsStr() {
        return strValue;
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
