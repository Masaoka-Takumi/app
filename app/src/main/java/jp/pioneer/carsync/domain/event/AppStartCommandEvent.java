package jp.pioneer.carsync.domain.event;

import android.content.Intent;
import android.support.annotation.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * App起動コマンド通知.
 * <p>
 * 車載機からアプリを起動する場合に発生する。
 * 指定されたパッケージのMainActivityを起動する。（カテゴリ:{@link Intent#CATEGORY_LAUNCHER}、
 * アクション:{@link Intent#ACTION_MAIN}なActivity）
 */
public class AppStartCommandEvent {
    /** 起動するアプリのパッケージ名. */
    public final String packageName;

    /**
     * コンストラクタ.
     *
     * @param packageName 起動するアプリのパッケージ名
     * @throws NullPointerException {@code packageName}がnull
     */
    public AppStartCommandEvent(@NonNull String packageName) {
        this.packageName = checkNotNull(packageName);
    }
}
