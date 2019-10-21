package jp.pioneer.carsync.domain.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Tuner系ステータス種別.
 */
public enum TunerStatus {
    /** 通常状態. */
    NORMAL(0x00, MediaSourceType.RADIO, MediaSourceType.DAB, MediaSourceType.HD_RADIO, MediaSourceType.SIRIUS_XM),
    /** SEEK. */
    SEEK(0x01, MediaSourceType.RADIO, MediaSourceType.DAB, MediaSourceType.HD_RADIO),
    /** SCAN中. */
    SCAN(0x01, MediaSourceType.SIRIUS_XM),
    /** BSM. */
    BSM(0x02, MediaSourceType.RADIO, MediaSourceType.HD_RADIO),
    /** LIST UPDATE. */
    LIST_UPDATE(0x02, MediaSourceType.DAB),
    /** PI SEARCH. */
    PI_SEARCH(0x03, MediaSourceType.RADIO),
    /** FM LINK. */
    FM_LINK(0x03, MediaSourceType.DAB),
    /** ERROR. */
    ERROR(0x04, MediaSourceType.RADIO, MediaSourceType.DAB, MediaSourceType.HD_RADIO),
    /** PTY SEARCH. */
    PTY_SEARCH(0x05, MediaSourceType.RADIO)
    ;

    /** 該当のステータスを持つソース群. */
    public final Set<MediaSourceType> supportedSources;
    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param sources 該当のステータスを持つソース群
     */
    TunerStatus(int code, MediaSourceType... sources) {
        this.code = code;
        this.supportedSources = EnumSet.noneOf(MediaSourceType.class);
        Collections.addAll(this.supportedSources, sources);
    }

    /**
     * MediaSourceとcodeからTunerStatus取得.
     *
     * @param source ソース
     * @param code プロトコルでの定義値
     * @return {@code source}と{@code code}に該当するTunerStatus
     * @throws IllegalArgumentException 未定義の{@code source}と{@code code}
     */
    public static TunerStatus fromMediaSourceAndCode(MediaSourceType source, int code) {
        for (TunerStatus status : values()) {
            if (status.supportedSources.contains(source) && status.code == code) {
                return status;
            }
        }

        throw new IllegalArgumentException(String.format(Locale.US, "invalid source(%s) or code(%d).", source, code));
    }
}
