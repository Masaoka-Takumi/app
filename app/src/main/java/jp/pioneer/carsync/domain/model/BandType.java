package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

/**
 * バンド種別.
 */
public interface BandType {
    /**
     * プロトコルでの定義値取得.
     *
     * @return プロトコルでの定義値
     */
    int getCode();

    /**
     * 表示用文字列リソースID取得.
     *
     * @return 表示用文字列リソースID
     */
    @StringRes
    int getLabel();
}
