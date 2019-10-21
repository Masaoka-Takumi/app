package jp.pioneer.carsync.domain.repository;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.PairingSpecType;

/**
 * ペアリングデバイスリストリポジトリ.
 * <p>
 * {@link #get(PairingSpecType, Callback)}が呼ばれた時点のリストを取得する
 */
public interface PairingDeviceListRepository {

    /**
     * PairingDeviceList.
     *
     * @param type ペアリング規格種別
     * @throws NullPointerException {@code type}がnull
     */
    void get(@NonNull PairingSpecType type,@NonNull Callback callback);

    /**
     * コールバック.
     */
    interface Callback{
        /**
         * 成功.
         *
         * @param type ペアリング規格種別
         */
        void onComplete(PairingSpecType type);
    }
}
