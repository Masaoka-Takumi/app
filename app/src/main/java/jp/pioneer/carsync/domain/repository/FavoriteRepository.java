package jp.pioneer.carsync.domain.repository;

import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;

import jp.pioneer.carsync.domain.content.DeleteParams;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.content.UpdateParams;

/**
 * お気に入りリポジトリ.
 */
public interface FavoriteRepository {

    /**
     * お気に入り情報取得.
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
     * お気に入り情報登録又は更新.
     * <p>
     * 指定された更新パラメータで登録されていない場合は登録、
     * 指定された更新パラメータで登録されている場合は更新、
     * を実施する。
     *
     * @param params 更新パラメータ
     * @throws NullPointerException {@code params}がnull
     */
    void upsert(@NonNull UpdateParams params);

    /**
     * お気に入り情報削除
     * <p>
     * 指定された削除パラメータでお気に入り情報を削除する。
     *
     * @param params 削除パラメータ
     * @throws NullPointerException {@code params}がnull
     */
    void delete(@NonNull DeleteParams params);
}
