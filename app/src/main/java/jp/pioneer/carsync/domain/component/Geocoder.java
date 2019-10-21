package jp.pioneer.carsync.domain.component;

import android.location.Address;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.List;

/**
 * ジオコーダー.
 */
public interface Geocoder {

    /**
     * アドレス取得
     * <p>
     * 実行した結果が、{@link Callback#onResult(List)} によって返却される。
     *
     * @param locationName 場所の名前
     * @param maxResults 返却されるアドレスリストの最大件数
     * @param callback コールバック
     * @throws NullPointerException {@code locationName} がnull
     * @throws IllegalArgumentException {@code locationName} の値が1未満
     * @throws NullPointerException {@code callback} がnull
     */
    void getFromLocationName(@NonNull String locationName, @IntRange(from = 1) int maxResults, @NonNull Callback callback);

    /**
     * コールバック.
     */
    interface Callback {

        /**
         * {@link #getFromLocationName(String, int, Callback)} を実行した結果
         *
         * @param addresses アドレスのリスト
         */
        @UiThread
        void onResult(@Nullable List<Address> addresses);
    }
}
