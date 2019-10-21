package jp.pioneer.carsync.domain.interactor;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.LocationProvider;
import jp.pioneer.carsync.domain.component.Resolver;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 現在の場所取得.
 * <p>
 * 衝突検知発生時の現在位置取得を想定。
 * 出来る限り高精度の位置情報を取得する。端末の設定が高精度になっていない場合
 * 設定の変更が必要となるが、{@link Resolver}を介して行うため、誘導の画面を
 * 用意したり、インテントを発行する必要はない。
 */
public class GetCurrentLocation {
    private static final LocationProvider.Priority PRIORITY = LocationProvider.Priority.HIGH_ACCURACY;

    @Inject LocationProvider mLocationProvider;

    /**
     * コンストラクタ.
     */
    @Inject
    public GetCurrentLocation() {
    }

    /**
     * 実行.
     *
     * @param callback コールバック
     * @throws NullPointerException {@code callback}がnull
     */
    public void execute(@NonNull LocationProvider.Callback callback) {
        checkNotNull(callback);
        mLocationProvider.startGetCurrentLocation(PRIORITY, callback, LocationProvider.GetType.SINGLE);
    }
}
