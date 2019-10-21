package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;
import jp.pioneer.mle.pmg.player.data.FilterStatus.ApplauseEffectMode;

/**
 * SoundEffect設定種別.
 * <p>
 * 車載機で定義されている種別
 */
public enum SoundEffectSettingType {
    /** OFF. */
    OFF(0x00, SoundEffectType.OFF, ApplauseEffectMode.ApplauseModeOff),
    /** ARENA_F. */
    ARENA_F(0x01, SoundEffectType.FEMALE, ApplauseEffectMode.ApplauseModeArena_F),
    /** ARENA_M. */
    ARENA_M(0x02, SoundEffectType.MALE, ApplauseEffectMode.ApplauseModeArena_M),
    /** HALL_F. */
    HALL_F(0x03, SoundEffectType.FEMALE, ApplauseEffectMode.ApplauseModeHall_F),
    /** HALL_M. */
    HALL_M(0x04, SoundEffectType.MALE, ApplauseEffectMode.ApplauseModeHall_M),
    /** CLUB_F. */
    CLUB_F(0x05, SoundEffectType.FEMALE, ApplauseEffectMode.ApplauseModeClub_F),
    /** CLUB_M. */
    CLUB_M(0x06, SoundEffectType.MALE, ApplauseEffectMode.ApplauseModeClub_M)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /** UI向けのSound Effect種別 */
    public final SoundEffectType type;

    /** MLEでの定義値 */
    public final ApplauseEffectMode mode;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param type UI向けのSound Effect種別
     * @param mode MLEでの定義値
     */
    SoundEffectSettingType(int code, SoundEffectType type, ApplauseEffectMode mode) {
        this.code = code;
        this.type = type;
        this.mode = mode;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSoundEffectSettingType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SoundEffectSettingType valueOf(byte code) {
        for (SoundEffectSettingType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
