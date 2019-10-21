package jp.pioneer.carsync.domain.interactor;

import android.location.Address;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.Geocoder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 緯度経度取得.
 * <p>
 * 場所の名前からアドレスを取得する.
 */
public class GetAddressFromLocationName {
    @Inject Geocoder mGeocoder;

    /**
     * コンストラクタ.
     */
    @Inject
    public GetAddressFromLocationName() {
    }

    /**
     * 実行.
     *
     * @param locationName 場所の名前
     * @param callback コールバック
     * @throws NullPointerException {@code locationName} がnull
     * @throws NullPointerException {@code callback} がnull
     */
    public void execute(@NonNull String locationName, @NonNull Callback callback) {
        checkNotNull(locationName);
        checkNotNull(callback);

        mGeocoder.getFromLocationName(locationName, 1, (addresses) -> {
            if (addresses == null) {
                Timber.e("execute() addresses is null.");
                callback.onError(Error.NOT_AVAILABLE);
            } else if (addresses.isEmpty()) {
                Timber.e("execute() addresses is empty.");
                callback.onError(Error.NOT_FOUND);
            } else {
                callback.onSuccess(addresses.get(0));
            }
        });
    }

    /**
     * エラー.
     *
     * @see Callback#onError(Error)
     */
    public enum Error {
        /** 利用可能なGeocoderが見つからなかった場合. */
        NOT_FOUND,
        /** 検索する場所の名前に一致するアドレスが見つからなかった場合. */
        NOT_AVAILABLE,
    }

    /**
     * コールバック.
     */
    public interface Callback {

        /**
         * {@link Geocoder#getFromLocationName(String, int, Geocoder.Callback)} に成功した場合に呼ばれるハンドラ.
         *
         * @param addresses アドレス
         */
        @UiThread
        void onSuccess(@NonNull Address addresses);

        /**
         * {@link Geocoder#getFromLocationName(String, int, Geocoder.Callback)} に失敗した場合に呼ばれるハンドラ.
         *
         * @param error エラー
         */
        @UiThread
        void onError(@NonNull Error error);
    }
}
