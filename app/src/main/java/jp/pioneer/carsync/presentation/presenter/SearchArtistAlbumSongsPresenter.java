package jp.pioneer.carsync.presentation.presenter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QueryAppMusic;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.SongsView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static jp.pioneer.carsync.domain.content.AppMusicContract.PlayParams.createPlayParams;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createSongsForArtistAlbum;

/**
 * アーティスト指定アルバム内楽曲リスト画面（検索用）のpresenter
 */
@PresenterLifeCycle
public class SearchArtistAlbumSongsPresenter extends Presenter<SongsView> implements LoaderManager.LoaderCallbacks<Cursor> {
    @Inject EventBus mEventBus;
    @Inject QueryAppMusic mQueryCase;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    private MusicParams mParams;

    private static final int LOADER_ID_MUSIC = 3;
    private LoaderManager mLoaderManager;

    /**
     * コンストラクタ
     */
    @Inject
    public SearchArtistAlbumSongsPresenter() {
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
        return mQueryCase.execute(createSongsForArtistAlbum(mParams.artistId, mParams.albumId));
    }

    /**
     * LoaderManagerの設定
     *
     * @param loaderManager LoaderManager
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(LOADER_ID_MUSIC, Bundle.EMPTY, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        AppMusicCursorLoader load = (AppMusicCursorLoader) loader;
        Optional.ofNullable(getView()).ifPresent(view -> view.setSongCursor(data, load.getExtras()));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setSongCursor(null, Bundle.EMPTY));
    }

    /**
     * 楽曲選択時処理
     *
     * @param id 楽曲ID
     */
    public void onArtistAlbumSongPlayAction(long id) {
        mControlAppMusicSource.play(createPlayParams(createSongsForArtistAlbum(mParams.artistId, mParams.albumId), id));
        mEventBus.post(new NavigateEvent(ScreenId.PLAYER_CONTAINER, Bundle.EMPTY));
    }
}
