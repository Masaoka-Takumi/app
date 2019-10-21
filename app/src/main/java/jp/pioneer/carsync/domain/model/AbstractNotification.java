package jp.pioneer.carsync.domain.model;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;

import static android.support.v4.app.NotificationCompat.EXTRA_LARGE_ICON;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Notificationの基本的な実装.
 * <p>
 * どのNotificationであっても変わらないはずの部分の実装。
 */
public abstract class AbstractNotification implements Notification {
    @Inject Resources mResources;
    @Inject ApplicationInfoRepository mAppInfoRepository;
    @Inject PackageManager mPackageManager;
    private StatusBarNotification mSbn;
    private ApplicationInfo mAppInfo;

    /**
     * 通知情報設定.
     *
     * @param sbn 通知情報
     * @return 通知情報 {@code sbn} が設定された {@link AbstractNotification}
     * @throws NullPointerException {@code sbn}がnull
     * @throws IllegalArgumentException パッケージ名に該当するものがない
     */
    public AbstractNotification setStatusBarNotification(@NonNull StatusBarNotification sbn) {
        mSbn = checkNotNull(sbn);
        mAppInfo = mAppInfoRepository.get(mSbn.getPackageName());
        checkArgument(mAppInfo != null);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    public Drawable getApplicationIcon() {
        return mAppInfo.loadIcon(mPackageManager);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    public String getApplicationName() {
        return mAppInfo.loadLabel(mPackageManager).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    public Drawable getNotificationIcon() {
        Bitmap bitmap = getNotification().extras.getParcelable(EXTRA_LARGE_ICON);
        if (bitmap == null) {
            return null;
        }

        return new BitmapDrawable(mResources, bitmap);
    }

    /**
     * 通知情報取得.
     *
     * @return 通知情報 {@link StatusBarNotification}
     * @throws NullPointerException {@code mSbn}がnull
     */
    @NonNull
    StatusBarNotification getStatusBarNotification() {
        return checkNotNull(mSbn);
    }

    /**
     * 通知情報から {@link Notification} 生成.
     *
     * @return 通知情報 {@link Notification}
     */
    @NonNull
    android.app.Notification getNotification() {
        return getStatusBarNotification().getNotification();
    }
}
