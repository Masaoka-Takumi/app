package jp.pioneer.carsync.presentation.presenter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QueryAppMusic;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AlbumsView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static jp.pioneer.carsync.domain.content.AppMusicContract.PlayParams.createPlayParams;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createAlbumsForArtist;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createSongsForArtist;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createSongsForArtistAlbum;

/**
 * アーティスト指定アルバムリスト画面（検索用）のpresenter
 */
@PresenterLifeCycle
public class SearchArtistAlbumsPresenter extends Presenter<AlbumsView> implements LoaderManager.LoaderCallbacks<Cursor> {
    @Inject EventBus mEventBus;
    @Inject QueryAppMusic mQueryCase;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    @Inject GetStatusHolder mGetStatusHolder;
    private MusicParams mParams;

    private static final int LOADER_ID_ALBUM = 2;
    private LoaderManager mLoaderManager;

    /**
     * コンストラクタ
     */
    @Inject
    public SearchArtistAlbumsPresenter() {
    }

    /**
     * 引き継ぎ情報の設定
     *
     * @param args Bundle
     */
    public void setArguments(Bundle args) {
        mParams = MusicParams.from(args);
    }

    public boolean isSphCarDevice() {
        return mGetStatusHolder.execute().getProtocolSpec().isSphCarDevice();
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mQueryCase.execute(createAlbumsForArtist(mParams.artistId));
    }

    /**
     * LoaderManagerの設定
     *
     * @param loaderManager LoaderManager
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(LOADER_ID_ALBUM, Bundle.EMPTY, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        AppMusicCursorLoader load = (AppMusicCursorLoader) loader;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAlbumCursor(data, load.getExtras()));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setAlbumCursor(null, Bundle.EMPTY));
    }

    /**
     * アルバム内シャッフル再生処理
     */
    public void onArtistAlbumShufflePlayAction() {
        mControlAppMusicSource.play(createPlayParams(createSongsForArtist(mParams.artistId), ShuffleMode.ON));
        mEventBus.post(new NavigateEvent(ScreenId.PLAYER_CONTAINER, Bundle.EMPTY));
    }

    /**
     * アルバム内順序再生処理
     *
     * @param id アルバムID
     */
    public void onArtistAlbumPlayAction(long id) {
        mControlAppMusicSource.play(createPlayParams(createSongsForArtistAlbum(mParams.artistId, id)));
        mEventBus.post(new NavigateEvent(ScreenId.PLAYER_CONTAINER, Bundle.EMPTY));
    }

    /**
     * アルバム選択時処理
     *
     * @param cursor Cursor
     * @param id     アルバムID
     */
    public void onArtistAlbumSongListShowAction(Cursor cursor, long id) {
        mParams.changeDirectory(AppMusicContract.Album.getAlbum(cursor));
        mParams.albumId = id;
        mEventBus.post(new NavigateEvent(ScreenId.SEARCH_MUSIC_ARTIST_ALBUM_SONG_LIST, mParams.toBundle()));
    }
}
