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
    FACEBOOK_MESSENGER("com.facebook.katana",5,"Facebook"),
    /** Hangouts. */
    HANGOUTS("com.google.android.talk",6,"Hangouts"),
    /** LINE. */
    LINE("jp.naver.line.android",8,"LINE"),
    /** Messenger. */
    MESSENGER("com.facebook.orca",4,"Messenger"),
    /** VK. */
    VK("com.vkontakte.android",3,"VK"),
    /** Viber. */
    VIBER("com.viber.voip",2,"Viber"),
    /** WeChat. */
    WE_CHAT("com.tencent.mm",7,"WeChat"),
    /** WhatsApp Messenger. */
    WHATS_APP_MESSENGER("com.whatsapp",1,"WhatsApp Messenger")
    ;
    private String mPackageName;
    private int mNumber;
    private String mAppName;

    /**
     * コンストラクタ.
     *
     * @param packageName パッケージ名
     */
    MessagingApp(String packageName, int number, @NonNull String appName) {
        mPackageName = packageName;
        mNumber = number;
        mAppName = appName;
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

    /**
     * App名取得.
     *
     * @return App名
     */
    @NonNull
    public String getAppName() {
        return mAppName;
    }

    public int getNumber() {
        return mNumber;
    }
}
