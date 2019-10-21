package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.Arrays;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

import static jp.pioneer.mle.pmg.player.data.FilterStatus.SoundFieldMode;


/**
 * SoundFieldControl設定種別.
 */
public enum SoundFieldControlSettingType {
    OFF(0x00, R.string.val_119, SoundFieldMode.OFF,
            SoundEffectSettingType.OFF),
    CLUB(0x01, R.string.val_094, SoundFieldMode.LIVE_REC,
            SoundEffectSettingType.OFF, SoundEffectSettingType.CLUB_F, SoundEffectSettingType.CLUB_M),
    CAFE(0x02, R.string.val_093, SoundFieldMode.LIVE,
            SoundEffectSettingType.OFF, SoundEffectSettingType.HALL_F, SoundEffectSettingType.HALL_M),
    CONCERT_HALL(0x05, R.string.val_097, SoundFieldMode.DOME,
            SoundEffectSettingType.OFF, SoundEffectSettingType.ARENA_F, SoundEffectSettingType.ARENA_M),
    OPEN_AIR(0x06, R.string.val_098, SoundFieldMode.STADIUM,
            SoundEffectSettingType.OFF, SoundEffectSettingType.ARENA_F, SoundEffectSettingType.ARENA_M)
    ;

    /** プロトコルでの定義値. */
    public final int code;
    /** 表示用文字列リソースID. */
    @StringRes public final int label;
    /** MLEでの定義値 */
    public final SoundFieldMode mode;

    /** 本設定種別に対して設定可能な{@link SoundEffectSettingType}の組み合わせ([0]:OFF, [1]:FEMALE, [2]:MALE). */
    public final ArrayList<SoundEffectSettingType> types;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param types {@link #code}になり得るソース種別群
     */
    SoundFieldControlSettingType(int code, @StringRes int label, SoundFieldMode mode, SoundEffectSettingType... types) {
        this.code = code;
        this.label = label;
        this.mode = mode;
        this.types = new ArrayList<>();
        if (types != null) {
            this.types.addAll(Arrays.asList(types));
        }
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
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSoundFieldSettingType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SoundFieldControlSettingType valueOf(byte code) {
        for (SoundFieldControlSettingType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * 有効なSound Effect設定種別取得.
     *
     * @param type Sound Effect種別
     * @return 有効なSoundEffect設定種別
     */
    public SoundEffectSettingType getEnableSoundEffectSettingType(SoundEffectType type){
        if(this == OFF){
            return SoundEffectSettingType.OFF;
        } else {
            switch(type){
                case OFF:
                    return this.types.get(0);
                case FEMALE:
                    return this.types.get(1);
                case MALE:
                    return this.types.get(2);
            }
        }

        return SoundEffectSettingType.OFF;
    }
}
