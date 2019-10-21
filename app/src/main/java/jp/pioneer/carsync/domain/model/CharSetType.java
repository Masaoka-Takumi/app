package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 文字コード種別.
 */
public enum CharSetType {
    /** UTF-8. */
    UTF8(0x00, "UTF-8"),
    /** ASCII. */
    ASCII(0x01, "US-ASCII"),
    /** ISO-8859-1. */
    ISO88591(0x02, "ISO-8859-1"),
    /** UTF-16（BigEndian）. */
    UTF16BE(0x03, "UTF-16BE"),
    /** Shift_JIS. */
    SHIFT_JIS(0x04, "MS932"),
    /** EBU COMPLETE（パイオニア独自仕様の文字コード）. */
    EBU_COMPLETE(0x05, null),
    /** TITLE TEXT STD EURO（パイオニア独自仕様の文字コード）. */
    TITLE_TEXT_STD_EURO(0x06, null),
    /** 無効値（車載機割り込み情報通知で使用される）. */
    INVALID(0xFF, null)
    ;

    /** プロトコルでの定義値. */
    public final int code;
    /** 文字セット（JavaのStringで変換出来る場合）. */
    public final String charset;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値.
     * @param charset 文字セット。JavaのStringで変換出来ない場合はnull。
     */
    CharSetType(int code, String charset) {
        this.code = code;
        this.charset = charset;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するCharSetType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static CharSetType valueOf(byte code) {
        for (CharSetType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
