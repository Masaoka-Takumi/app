package jp.pioneer.carsync.infrastructure.repository;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.repository.AppMusicRepository;
import jp.pioneer.carsync.infrastructure.content.AppMusicCursorLoaderImpl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * AppMusicRepositoryの実装.
 */
public class AppMusicRepositoryImpl implements AppMusicRepository {
    @Inject Context mContext;

    /**
     * コンストラクタ
     */
    @Inject
    public AppMusicRepositoryImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppMusicCursorLoader get(@NonNull QueryParams params) {
        checkNotNull(params);

        return createCursorLoader(params);
    }

    /**
     * {@link AppMusicCursorLoader} 生成.
     *
     * @param params クエリーパラメータ
     * @return {@link AppMusicCursorLoader}
     */
    AppMusicCursorLoader createCursorLoader(QueryParams params) {
        return new AppMusicCursorLoaderImpl(mContext, params);
    }
}
