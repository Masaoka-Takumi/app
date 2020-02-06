package jp.pioneer.carsync.domain.model;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 通知.
 */
public interface Notification {
    /**
     * 通知読み上げ対象であるか否か取得.
     *
     * @return {@code true}:通知読み上げ対象. {@code false}:通知読み上げ未対象
     */
    boolean isReadTarget();

    /**
     * 通知情報 {@link android.app.Notification} からアイコン取得
     *
     * @return アイコン または null
     */
    @Nullable
    Drawable getNotificationIcon();

    /**
     * アプリケーション情報 {@link android.content.pm.ApplicationInfo} からアイコン取得.
     *
     * @return アイコン または null
     */
    @Nullable
    Drawable getApplicationIcon();

    /**
     * アプリケーション情報 {@link android.content.pm.ApplicationInfo} からアプリケーション名取得.
     *
     * @return アプリケーション名
     */
    @NonNull
    String getApplicationName();

    /**
     * アプリケーション情報 {@link android.content.pm.ApplicationInfo} からパッケージ名取得.
     *
     * @return パッケージ名
     */
    @NonNull
    String getPackageName();

    /**
     * 通知情報 {@link android.app.Notification} から通知タイトルを取得.
     *
     * @return 通知タイトル
     */
    @NonNull
    String getTitle();

    /**
     * 通知情報 {@link android.app.Notification} から通知内容を取得.
     *
     * @return 通知内容
     */
    @NonNull
    String getText();
}
