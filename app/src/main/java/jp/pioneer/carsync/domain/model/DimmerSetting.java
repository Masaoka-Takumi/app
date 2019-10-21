package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * DIMMER設定.
 */
public class DimmerSetting {
    /** 設定値. */
    public Dimmer dimmer;
    /** 開始時刻(Hour). */
    public int startHour;
    /** 開始時刻(Minute). */
    public int startMinute;
    /** 終了時刻(Hour). */
    public int endHour;
    /** 終了時刻(Minute). */
    public int endMinute;

    /**
     * コンストラクタ.
     */
    public DimmerSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        dimmer = null;
        startHour = 0;
        startMinute = 0;
        endHour = 0;
        endMinute = 0;
    }

    /**
     * 値設定.
     *
     * @param dimmer 設定値
     * @param startHour 開始時刻(Hour)
     * @param startMinute 開始時刻(Minute)
     * @param endHour 終了時刻(Hour)
     * @param endMinute 終了時刻(Minute)
     */
    public void setValue(Dimmer dimmer, int startHour, int startMinute, int endHour, int endMinute) {
        this.dimmer = dimmer;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("dimmer", dimmer)
                .add("startHour", startHour)
                .add("startMinute", startMinute)
                .add("endHour", endHour)
                .add("endMinute", endMinute)
                .toString();
    }

    /**
     * DIMMER設定値.
     */
    public enum Dimmer {
        /** ILLUMI LINE連動. */
        ILLUMI_LINE(0x00),
        /** SYNC CLOCK連動設定. */
        SYNC_CLOCK(0x01),
        /** Dimmer ON. */
        ON(0x02),
        /** Dimmer OFF. */
        OFF(0x03),
        /** MANUAL. */
        MANUAL(0x04)
        ;

        /** プロトコルでの定義値. */
        public final int code;

        /**
         * コンストラクタ.
         *
         * @param code プロトコルでの定義値
         */
        Dimmer(int code) {
            this.code = code;
        }

        /**
         * プロトコルでの定義値から取得.
         *
         * @param code プロトコルでの定義値
         * @return プロトコルでの定義値に該当するDimmer
         * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
         */
        public static Dimmer valueOf(byte code) {
            for (Dimmer value : values()) {
                if (value.code == PacketUtil.ubyteToInt(code)) {
                    return value;
                }
            }

            throw new IllegalArgumentException("invalid code: " + code);
        }
    }
}
