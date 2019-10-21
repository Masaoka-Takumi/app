package jp.pioneer.carsync.domain.repository;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.content.QueryParams;

/**
 * AppMusicリポジトリ.
 */
public interface AppMusicRepository {
    /**
     * AppMusic
     * <p>
     * 指定されたクエリーパラメータの {@link AppMusicCursorLoader} を作成する
     *
     * @param params クエリーパラメータ
     * @return {@link AppMusicCursorLoader}
     * @throws NullPointerException {@code params} がnull
     */
    AppMusicCursorLoader get(@NonNull QueryParams params);
}
