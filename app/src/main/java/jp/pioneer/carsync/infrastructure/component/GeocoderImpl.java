package jp.pioneer.carsync.infrastructure.component;

import android.content.Context;
import android.location.Address;
import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.Geocoder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Geocoderの実装.
 */
public class GeocoderImpl implements Geocoder {
    @Inject Context mContext;
    @Inject Handler mHandler;

    /**
     * コンストラクタ.
     */
    @Inject
    public GeocoderImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getFromLocationName(@NonNull String locationName, @IntRange(from = 1) int maxResults, @NonNull Callback callback) {
        checkNotNull(locationName);
        checkArgument(maxResults >= 1);
        checkNotNull(callback);

        Timber.i("getFromLocationName() locationName = %s, maxResults = %d",
                locationName, maxResults);

        android.location.Geocoder geocoder = new android.location.Geocoder(mContext);
        try {
            List<Address> addresses = executeGeocoderGetFromLocationName(geocoder,locationName, maxResults);
            mHandler.post(() -> callback.onResult(addresses));
        } catch (IOException e) {
            Timber.e(e, "getFromLocationName() IOException");
            mHandler.post(() -> callback.onResult(null));
        }
    }

    /**
     * アドレス取得
     * <p>
     * UnitTest用
     *
     * @param geocoder ジオコーダー
     * @param locationName 場所の名前
     * @param maxResults 取得最大件数
     * @return アドレスリスト
     */
    @VisibleForTesting
    List<Address> executeGeocoderGetFromLocationName(android.location.Geocoder geocoder,String locationName, int maxResults) throws IOException{
        return geocoder.getFromLocationName(locationName, maxResults);
    }
}
