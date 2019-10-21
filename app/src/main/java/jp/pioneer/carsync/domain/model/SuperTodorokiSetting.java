package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Super"轟"設定.
 */
public enum SuperTodorokiSetting {
    /** OFF. */
    OFF(0x00, R.string.com_001),
    /** LOW. */
    LOW(0x01, R.string.set_127),
    /** HIGH. */
    HIGH(0x02, R.string.set_088),
    /** SUPER HIGH */
    SUPER_HIGH(0x03, R.string.set_207),
    /**
     * LAST.
     *
     * 車載機で覚えているLAST値に設定するためのもので設定専用。
     */
    LAST(0x0F, R.string.unknown), // 画面には表示しないので適用なIDを使用しておく
    ;

    /** プロトコルでの定義値. */
    public final int code;
    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    SuperTodorokiSetting(int code, @StringRes int label) {
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
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSuperTodorokiSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SuperTodorokiSetting valueOf(byte code) {
        for (SuperTodorokiSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
