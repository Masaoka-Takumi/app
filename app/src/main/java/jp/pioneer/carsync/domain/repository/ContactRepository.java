package jp.pioneer.carsync.domain.repository;

import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;

import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.content.UpdateParams;

/**
 * 連絡先リポジトリ.
 */
public interface ContactRepository {
    /**
     * 連絡先情報取得.
     * <p>
     * 指定されたクエリーパラメータの {@link CursorLoader} を作成する。
     *
     * @param params クエリーパラメータ
     * @return {@link CursorLoader}
     * @throws NullPointerException {@code params}がnull
     */
    @NonNull
    CursorLoader get(@NonNull QueryParams params);

    /**
     * 連絡先情報更新.
     * <p>
     * 指定された更新パラメータで連絡先情報を更新する。
     *
     * @param params 更新パラメータ
     * @return 更新された行数
     * @throws NullPointerException {@code params}がnull
     */
    int update(@NonNull UpdateParams params);
}
