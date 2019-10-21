package jp.pioneer.carsync.application.event;

import android.support.annotation.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * アプリケーション状態変更イベント.
 */
public class AppStateChangeEvent {
    /** アプリケーション状態. */
    public AppState appState;

    /**
     * コンストラクタ.
     *
     * @param state アプリケーション状態
     * @throws NullPointerException {@code state}がnull
     */
    public AppStateChangeEvent(@NonNull AppState state) {
        appState = checkNotNull(state);
    }

    /**
     * アプリケーション状態.
     */
    public enum AppState {
        /**
         * 開始した.
         * <p>
         * アプリケーション内のActivityが1つStart状態になった
         */
        STARTED,
        /**
         * 再開した.
         * <p>
         * アプリケーション内のActivityが1つResume状態になった
         */
        RESUMED,
        /**
         * 休止した.
         * <p>
         * アプリケーション内のActivityが1つもResume状態に無い
         */
        PAUSED,
        /**
         * 停止した.
         * <p>
         * アプリケーション内のActivityが1つもStart状態に無い
         */
        STOPPED
    }
}
