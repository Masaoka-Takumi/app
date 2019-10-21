package jp.pioneer.carsync.domain.component;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

/**
 * 位置プロバイダー.
 */
public interface LocationProvider {
    /**
     * 現在位置取得開始.
     * <p>
     * 取得に成功した場合,{@link Callback#onSuccess(Location)}が呼ばれる.
     * 取得に失敗した場合,{@link Callback#onError(Error, Resolver)}が呼ばれる.
     * <p>
     * 取得種別により、{@link Callback#onSuccess(Location)}が呼ばれる回数が変わる
     * {@link GetType#SINGLE}の場合：一度
     * {@link GetType#CONTINUOUS}の場合：終了するまで
     *
     * @param priority 検出精度
     * @param callback コールバック
     * @param type     取得種別
     * @throws NullPointerException {@code priority} または {@code callback} または {@code type} がnull
     */
    void startGetCurrentLocation(@NonNull Priority priority, @NonNull Callback callback, @NonNull GetType type);

    /**
     * 現在位置取得終了.
     */
    void finishGetCurrentLocation();

    /**
     * 最後に取得した位置情報取得
     *
     * @return 位置情報
     */
    @Nullable
    Location getLastLocation();

    /**
     * 位置情報検出精度
     *
     * @see LocationProvider#startGetCurrentLocation(Priority, Callback, GetType)
     */
    enum Priority {
        HIGH_ACCURACY,
        BALANCED_POWER_ACCURACY,
        LOW_POWER,
        NO_POWER,
    }

    /**
     * エラー
     *
     * @see Callback#onError(Error, Resolver)
     */
    enum Error {
        RESOLUTION_REQUIRED,
        MULTIPLE_ACCESS,
        NOT_AVAILABLE,
    }

    /**
     * 取得種別.
     *
     * @see LocationProvider#startGetCurrentLocation(Priority, Callback, GetType)
     */
    enum GetType {
        /** 単発 */
        SINGLE(1000,5000),
        /** 連続 */
        CONTINUOUS(1000,1000);

        /** インターバル[ms] */
        public final int interval;
        /** ファストインターバル[ms] */
        public final int fastInterval;

        GetType(int interval, int fastInterval){
            this.interval = interval;
            this.fastInterval = fastInterval;
        }
    }

    /**
     * コールバック
     */
    interface Callback {
        /**
         * {@link #startGetCurrentLocation(Priority, Callback, GetType)} が成功した場合に呼ばれるハンドラ
         *
         * @param location 現在位置情報
         */
        @UiThread
        void onSuccess(@NonNull Location location);

        /**
         * {@link #startGetCurrentLocation(Priority, Callback, GetType)} が失敗した場合に呼ばれるハンドラ
         *
         * @param error    エラー内容
         * @param resolver エラー解決
         */
        @UiThread
        void onError(@NonNull Error error, @Nullable Resolver resolver);
    }
}
