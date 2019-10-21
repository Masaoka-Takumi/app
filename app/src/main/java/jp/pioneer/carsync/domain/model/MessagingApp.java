package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * メッセージングアプリ.
 * <p>
 * 本アプリと連携するメッセージングアプリの定義。
 */
public enum MessagingApp {
    /** Facebook. */
    FACEBOOK_MESSENGER("com.facebook.katana"),
    /** Hangouts. */
    HANGOUTS("com.google.android.talk"),
    /** LINE. */
    LINE("jp.naver.line.android"),
    /** Messenger. */
    MESSENGER("com.facebook.orca"),
    /** VK. */
    VK("com.vkontakte.android"),
    /** Viber. */
    VIBER("com.viber.voip"),
    /** WeChat. */
    WE_CHAT("com.tencent.mm"),
    /** WhatsApp Messenger. */
    WHATS_APP_MESSENGER("com.whatsapp")
    ;
    private String mPackageName;

    /**
     * コンストラクタ.
     *
     * @param packageName パッケージ名
     */
    MessagingApp(String packageName) {
        mPackageName = packageName;
    }

    /**
     * パッケージ名から{@link MessagingApp}生成.
     *
     * @param packageName パッケージ名
     * @return パッケージ名に該当する {@link MessagingApp}
     * @throws NullPointerException {@code packageName}がnull
     * @throws IllegalArgumentException パッケージ名に該当するものがない
     * @see #fromPackageNameNoThrow(String)
     */
    @NonNull
    public static MessagingApp fromPackageName(@NonNull String packageName) {
        MessagingApp messagingApp = fromPackageNameNoThrow(packageName);
        if (messagingApp == null) {
            throw new IllegalArgumentException("invalid packageName: " + packageName);
        }

        return messagingApp;
    }

    /**
     * パッケージ名から{@link MessagingApp}生成.
     * <p>
     * パッケージ名に該当するものがない場合にnullを返してほしい場合に使用する。
     *
     * @param packageName パッケージ名
     * @return パッケージ名に該当する {@link MessagingApp}。該当するものがない場合はnull。
     * @throws NullPointerException {@code packageName}がnull
     * @see #fromPackageName(String)
     */
    @Nullable
    public static MessagingApp fromPackageNameNoThrow(@NonNull String packageName) {
        checkNotNull(packageName);

        for (MessagingApp messagingApp : MessagingApp.values()) {
            if (packageName.equals(messagingApp.getPackageName())) {
                return messagingApp;
            }
        }

        return null;
    }

    /**
     * パッケージ名取得.
     *
     * @return パッケージ名
     */
    @NonNull
    public String getPackageName() {
        return mPackageName;
    }
}
