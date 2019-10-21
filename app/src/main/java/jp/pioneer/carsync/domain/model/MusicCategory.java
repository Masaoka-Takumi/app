package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 楽曲リストカテゴリ
 */

public enum MusicCategory {
    SONG(0x01),
    PLAYLIST(0x02),
    ALBUM(0x03),
    ARTIST(0x04),
    GENRE(0x05),;

    /** プロトコルでの定義値. */
    public final int code;

    MusicCategory(int code) {
        this.code = code;
    }

    /**
     * {@inheritDoc}
     */
    public int getCode() {
        return code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するRadioBandType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static MusicCategory valueOf(byte code) {
        for (MusicCategory value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    public static MusicCategory toggle(MusicCategory category, int value){
        int next = category.getCode() + value;
        if (next > GENRE.getCode()) {
            next =  next - GENRE.getCode();
        }else if(next < SONG.getCode()) {
            next = next + GENRE.getCode();
        }
        return MusicCategory.valueOf((byte)next);
    }
}
