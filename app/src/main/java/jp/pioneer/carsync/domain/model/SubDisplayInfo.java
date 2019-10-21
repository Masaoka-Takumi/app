package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * サブディスプレイ情報.
 */
public enum SubDisplayInfo {
    /** Playlists. */
    PLAYLISTS(0x00, R.string.sub_display_type_playlists),
    /** Genres. */
    GENRES(0x01, R.string.sub_display_type_genres),
    /** Artists. */
    ARTISTS(0x02, R.string.sub_display_type_artists),
    /** Albums. */
    ALBUMS(0x03, R.string.sub_display_type_albums),
    /** Songs. */
    SONGS(0x04, R.string.sub_display_type_songs),
    /** Composers. */
    COMPOSERS(0x05, R.string.sub_display_type_unused),
    /** Podcasts. */
    PODCASTS(0x06, R.string.sub_display_type_unused),
    /** Audio Books. */
    AUDIO_BOOKS(0x07, R.string.sub_display_type_unused),
    /** Top List. */
    TOP_LIST(0x08, R.string.sub_display_type_unused),
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
     *
     */
    SubDisplayInfo(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSubDisplayInfo
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SubDisplayInfo valueOf(byte code) {
        for (SubDisplayInfo value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
