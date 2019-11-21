package jp.pioneer.carsync.infrastructure.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.CursorLoader;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.ProviderContract.*;
import jp.pioneer.carsync.domain.content.DeleteParams;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.content.UpdateParams;
import jp.pioneer.carsync.domain.repository.FavoriteRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * FavoriteRepositoryの実装.
 */
public class FavoriteRepositoryImpl implements FavoriteRepository {
    @Inject Context mContext;

    /**
     * コンストラクタ
     */
    @Inject
    public FavoriteRepositoryImpl(){

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
    public void upsert(@NonNull UpdateParams params) {
        checkNotNull(params);

        Cursor cursor = null;
        ContentResolver resolver = mContext.getContentResolver();

        try {
            cursor = resolver.query(params.uri, null, params.where, params.selectionArgs, null);

            if ((cursor != null) && (cursor.getCount() > 0)) {
                ContentValues contentValues = new ContentValues();

                // 更新時はname,descriptionのみ更新
                if(params.values != null) {
                    contentValues.put(Favorite.NAME, params.values.getAsString(Favorite.NAME));
                    contentValues.put(Favorite.DESCRIPTION, params.values.getAsString(Favorite.DESCRIPTION));
                    //Presetキーの場合他も更新
                    if(params.values.getAsInteger(Favorite.TUNER_CHANNEL_KEY2)!=null){
                        contentValues.put(Favorite.TUNER_CHANNEL_KEY1, params.values.getAsLong(Favorite.TUNER_CHANNEL_KEY1));
                        contentValues.put(Favorite.TUNER_FREQUENCY_INDEX, params.values.getAsInteger(Favorite.TUNER_FREQUENCY_INDEX));
                        contentValues.put(Favorite.TUNER_BAND, params.values.getAsInteger(Favorite.TUNER_BAND));
                        contentValues.put(Favorite.TUNER_PARAM1, params.values.getAsInteger(Favorite.TUNER_PARAM1));
                        contentValues.put(Favorite.TUNER_PARAM2, params.values.getAsInteger(Favorite.TUNER_PARAM2));
                        contentValues.put(Favorite.TUNER_PARAM3, params.values.getAsInteger(Favorite.TUNER_PARAM3));
		                contentValues.put(Favorite.CREATE_DATE, params.values.getAsInteger(Favorite.CREATE_DATE));
                    }
                    resolver.update(params.uri, contentValues, params.where, params.selectionArgs);
                }
            } else {
                resolver.insert(params.uri, params.values);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(@NonNull DeleteParams params) {
        checkNotNull(params);

        ContentResolver resolver = mContext.getContentResolver();
        resolver.delete(params.uri, params.where, params.selectionArgs);
    }

    @VisibleForTesting
    CursorLoader createCursorLoader(QueryParams params) {
        return new CursorLoader(mContext, params.uri, params.projection, params.selection,
                params.selectionArgs, params.sortOrder);
    }
}
