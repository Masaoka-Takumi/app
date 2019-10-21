package jp.pioneer.carsync.infrastructure.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.CursorLoader;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.content.UpdateParams;
import jp.pioneer.carsync.domain.repository.ContactRepository;
import jp.pioneer.carsync.infrastructure.content.ContactsCursorLoaderImpl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ContactRepositoryの実装.
 */
public class ContactRepositoryImpl implements ContactRepository {
    @Inject Context mContext;

    /**
     * コンストラクタ.
     */
    @Inject
    public ContactRepositoryImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CursorLoader get(@NonNull QueryParams params) {
        checkNotNull(params);

        return createCursorLoader(params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(@NonNull UpdateParams params) {
        checkNotNull(params);

        ContentResolver resolver = mContext.getContentResolver();
        return resolver.update(params.uri, params.values, params.where, params.selectionArgs);
    }

    @VisibleForTesting
    CursorLoader createCursorLoader(QueryParams params) {
        return new ContactsCursorLoaderImpl(mContext, params);
    }
}
