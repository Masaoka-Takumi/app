package jp.pioneer.carsync.domain.model;

import java.util.Locale;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 設定リスト種別.
 */
public enum SettingListType {
    /** デバイスリスト. */
    DEVICE_LIST(0x00, 0x00),
    /** サーチリスト. */
    SEARCH_LIST(0x00, 0x01)
    ;

    /** 設定種別のプロトコルでの定義値. */
    public final int settingTypeCode;
    /** リスト種別のプロトコルでの定義値. */
    public final int listTypeCode;

    /**
     * コンストラクタ.
     *
     * @param settingTypeCode 設定種別のプロトコルでの定義値
     * @param listTypeCode リスト種別のプロトコルでの定義値
     */
    SettingListType(int settingTypeCode, int listTypeCode) {
        this.settingTypeCode = settingTypeCode;
        this.listTypeCode = listTypeCode;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param settingTypeCode 設定種別のプロトコルでの定義値
     * @param listTypeCode リスト種別のプロトコルでの定義値
     * @return プロトコルでの定義値に該当するSettingListType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SettingListType valueOf(byte settingTypeCode, byte listTypeCode) {
        for (SettingListType value : values()) {
            if (value.settingTypeCode == PacketUtil.ubyteToInt(settingTypeCode)
                    && value.listTypeCode == PacketUtil.ubyteToInt(listTypeCode)) {
                return value;
            }
        }

        throw new IllegalArgumentException(String.format(Locale.US, "invalid settingTypeCode: %d, listTypeCode: %d",
                settingTypeCode, listTypeCode));
    }
}
