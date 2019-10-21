package jp.pioneer.carsync.domain.interactor;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.repository.AppMusicRepository;
import jp.pioneer.carsync.domain.content.AppMusicContract;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * AppMusicのクエリ
 * <p>
 * 音楽に関する情報を取得する際に使用する
 */
public class QueryAppMusic {
    @Inject AppMusicRepository mRepository;

    /**
     * コンストラクタ.
     */
    @Inject
    public QueryAppMusic() {
    }

    /**
     * 実行.
     *
     * @param params クエリーパラメータ
     *               {@link AppMusicContract.QueryParamsBuilder}のメソッドを使用して生成する
     * @return {@link AppMusicCursorLoader}
     * @throws NullPointerException {@code params}がnull
     */
    public AppMusicCursorLoader execute(@NonNull QueryParams params) {
        checkNotNull(params);

        return mRepository.get(params);
    }
}
