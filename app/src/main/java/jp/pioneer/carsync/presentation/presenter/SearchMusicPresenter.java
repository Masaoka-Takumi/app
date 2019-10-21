package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.QueryAppMusic;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.SearchMusicView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.argument.SearchContentParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

import static jp.pioneer.carsync.domain.content.AppMusicContract.PlayParams.createPlayParams;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createSongsById;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createSongsForAlbum;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createSongsForArtist;

/**
 * 音楽検索画面のpresenter
 */
@PresenterLifeCycle
public class SearchMusicPresenter extends Presenter<SearchMusicView> implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID_ARTIST = 1;
    private static final int LOADER_ID_ALBUM = 2;
    private static final int LOADER_ID_MUSIC = 3;

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject QueryAppMusic mMusicCase;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    private LoaderManager mLoaderManager;
    private SearchContentParams mParams;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Optional.ofNullable(getView()).ifPresent(SearchMusicView::closeDialog);
            mEventBus.post(new NavigateEvent(ScreenId.PLAYER_CONTAINER, Bundle.EMPTY));
        }
    };
    /**
     * コンストラクタ
     */
    @Inject
    public SearchMusicPresenter() {
    }

    /**
     * 引き継ぎ情報設定
     *
     * @param args Bundle
     */
    public void setArguments(Bundle args) {
        mParams = SearchContentParams.from(args);
    }

    /**
     * LoaderManagerの設定
     *
     * @param loaderManager LoaderManager
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        switch (mParams.voiceCommand) {
            case ARTIST:
                mLoaderManager.initLoader(LOADER_ID_ARTIST, Bundle.EMPTY, this);
                break;
            case ALBUM:
                mLoaderManager.initLoader(LOADER_ID_ALBUM, Bundle.EMPTY, this);
                break;
            case SONG:
                mLoaderManager.initLoader(LOADER_ID_MUSIC, Bundle.EMPTY, this);
                break;
            default:
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID_ARTIST:
                return mMusicCase.execute(AppMusicContract.QueryParamsBuilder.createArtistsByKeywords(mParams.searchWords));
            case LOADER_ID_ALBUM:
                return mMusicCase.execute(AppMusicContract.QueryParamsBuilder.createAlbumsByKeywords(mParams.searchWords));
            case LOADER_ID_MUSIC:
                return mMusicCase.execute(AppMusicContract.QueryParamsBuilder.createSongsByKeywords(mParams.searchWords));
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            switch (loader.getId()) {
                case LOADER_ID_ARTIST:
                    view.setArtistCursor(data);
                    if(data.getCount() == 1){
                        data.moveToFirst();
                        mControlAppMusicSource.play(createPlayParams(createSongsForArtist(AppMusicContract.Artist.getId(data))));
                        mHandler.post(mRunnable);
                    }
                    break;
                case LOADER_ID_ALBUM:
                    view.setAlbumCursor(data);
                    if(data.getCount() == 1){
                        data.moveToFirst();
                        mControlAppMusicSource.play(createPlayParams(createSongsForAlbum(AppMusicContract.Album.getId(data))));
                        mHandler.post(mRunnable);
                    }
                    break;
                case LOADER_ID_MUSIC:
                    view.setMusicCursor(data);
                    if(data.getCount() == 1){
                        data.moveToFirst();
                        mControlAppMusicSource.play(createPlayParams(createSongsById(AppMusicContract.Song.getId(data)), AppMusicContract.Song.getId(data)));
                        mHandler.post(mRunnable);

                    }
                    break;
                default:
                    Timber.w("This case is impossible.");
                    break;
            }
        });

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            switch (loader.getId()) {
                case LOADER_ID_ARTIST:
                    view.setArtistCursor(null);
                    break;
                case LOADER_ID_ALBUM:
                    view.setAlbumCursor(null);
                    break;
                case LOADER_ID_MUSIC:
                    view.setMusicCursor(null);
                    break;
                default:
                    Timber.w("This case is impossible.");
                    break;
            }
        });
    }

    /**
     * アーティスト内楽曲再生
     *
     * @param id 選択アーティストID
     */
    public void onArtistPlayAction(long id) {
        mControlAppMusicSource.play(createPlayParams(createSongsForArtist(id)));
        mHandler.post(mRunnable);
    }

    /**
     * アルバム内楽曲再生
     *
     * @param id アルバムID
     */
    public void onAlbumPlayAction(long id) {
        mControlAppMusicSource.play(createPlayParams(createSongsForAlbum(id)));
        mHandler.post(mRunnable);
    }

    /**
     * アーティスト選択時処理
     *
     * @param cursor Cursor
     * @param id     アーティストID
     */
    public void onArtistAlbumListShowAction(Cursor cursor, long id) {
        MusicParams params = new MusicParams();
        params.changeDirectory(AppMusicContract.Artist.getArtist(cursor));
        params.artistId = id;
        mEventBus.post(new NavigateEvent(ScreenId.SEARCH_MUSIC_ARTIST_ALBUM_LIST, params.toBundle()));
    }

    /**
     * アルバム選択時処理
     *
     * @param cursor Cursor
     * @param id     アルバムID
     */
    public void onAlbumSongListShowAction(Cursor cursor, long id) {
        MusicParams params = new MusicParams();
        params.changeDirectory(AppMusicContract.Album.getAlbum(cursor));
        params.albumId = id;
        mEventBus.post(new NavigateEvent(ScreenId.SEARCH_MUSIC_ALBUM_SONG_LIST, params.toBundle()));
    }

    /**
     * 楽曲選択時処理
     *
     * @param id 楽曲ID
     */
    public void onSongPlayAction(long id) {
        mControlAppMusicSource.play(createPlayParams(createSongsById(id), id));
        mEventBus.post(new NavigateEvent(ScreenId.PLAYER_CONTAINER, Bundle.EMPTY));
    }
}
