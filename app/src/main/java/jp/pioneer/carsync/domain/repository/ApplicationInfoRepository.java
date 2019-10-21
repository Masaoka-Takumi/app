package jp.pioneer.carsync.domain.repository;

import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * アプリケーション情報リポジトリ.
 * <p>
 * {@link ApplicationInfo}を取得するためのリポジトリ。
 */
public interface ApplicationInfoRepository {
    /**
     * アプリ情報一覧取得.
     * <p>
     * 指定されたパッケージ名の {@link ApplicationInfo} を取得する。
     *
     * @param packageNames パッケージ名群
     * @return インストールされているアプリに関する {@link ApplicationInfo} のリスト
     * @throws NullPointerException {@code packageNames}がnull、または、{@code packageNames}の要素がnull
     */
    @NonNull
    List<ApplicationInfo> get(@NonNull String[] packageNames);

    /**
     * アプリ情報取得.
     * <p>
     * 指定されたパッケージ名の {@link ApplicationInfo} を取得する。
     *
     * @param packageName パッケージ名
     * @return インストールされているアプリに関する {@link ApplicationInfo}。インストールされていない場合はnull。
     * @throws NullPointerException {@code packageName}がnull
     */
    @Nullable
    ApplicationInfo get(@NonNull String packageName);
}
