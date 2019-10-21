package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.content.DeleteParams;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.content.UpdateParams;
import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;
import jp.pioneer.carsync.domain.repository.FavoriteRepository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * チューナーのクエリーアイテム.
 * <p>
 * チューナー系ソースのプリセット情報の取得と
 * お気に入り情報の取得・登録・削除をする際に使用する。
 */
public class QueryTunerItem {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject FavoriteRepository mFavoriteRepository;
    @Inject CarDeviceMediaRepository mCarDeviceMediaRepository;

    /**
     * コンストラクタ
     */
    @Inject
    public QueryTunerItem() {
    }

    /**
     * プリセットリスト取得.
     *
     * @param sourceType 取得したいメディアの種別
     * @param bandType   バンド種別 nullの場合は全て取得。 not nullの場合は指定のバンド種別の情報を取得。
     * @return {@link CursorLoader}
     * @throws NullPointerException {@code params}がnull
     * @throws IllegalArgumentException {@code sourceType}がプリセットリスト非対応
     */
    public CursorLoader getPresetList(@NonNull MediaSourceType sourceType, @Nullable BandType bandType) {
        checkArgument(checkNotNull(sourceType).isPchListSupported());

        return mCarDeviceMediaRepository.getPresetChannelList(sourceType, bandType);
    }

    /**
     * お気に入りリスト取得.
     *
     * @param params クエリーパラメータ
     *               {@link TunerContract.FavoriteContract.QueryParamsBuilder}のメソッドを使用して生成する
     * @return {@link CursorLoader}
     * @throws NullPointerException {@code params}がnull
     */
    public CursorLoader getFavoriteList(@NonNull QueryParams params) {
        checkNotNull(params);

        return mFavoriteRepository.get(params);
    }

    /**
     * お気に入り登録.
     *
     * @param params 更新パラメータ
     *               {@link TunerContract.FavoriteContract.UpdateParamsBuilder}のメソッドを使用して生成する
     * @throws NullPointerException {@code params}がnull
     */
    public void registerFavorite(@NonNull UpdateParams params) {
        checkNotNull(params);

        mHandler.post(() -> mFavoriteRepository.upsert(params));
    }

    /**
     * お気に入り解除.
     *
     * @param params 削除パラメータ
     *               {@link TunerContract.FavoriteContract.DeleteParamsBuilder}のメソッドを使用して生成する
     * @throws NullPointerException {@code params}がnull
     */
    public void unregisterFavorite(@NonNull DeleteParams params) {
        checkNotNull(params);

        mHandler.post(() -> mFavoriteRepository.delete(params));
    }
}
