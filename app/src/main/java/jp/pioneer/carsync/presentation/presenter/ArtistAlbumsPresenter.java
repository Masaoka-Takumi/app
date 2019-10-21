package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.event.ListTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QueryAppMusic;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RotaryKeyAction;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.presentation.event.ListFocusEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AndroidMusicListType;
import jp.pioneer.carsync.presentation.view.AlbumsView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static jp.pioneer.carsync.domain.content.AppMusicContract.PlayParams.createPlayParams;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createAlbumsForArtist;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createSongsForArtist;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createSongsForArtistAlbum;
import static jp.pioneer.carsync.presentation.model.AndroidMusicListType.ARTIST_ALBUM;

/**
 * アーティストリスト内アルバムリスト画面のPresenter
 *
 * @see AlbumsView
 */
@PresenterLifeCycle
public class ArtistAlbumsPresenter extends Presenter<AlbumsView> implements LoaderManager.LoaderCallbacks<Cursor> {
    /** 楽曲リスト種別. */
    private static final AndroidMusicListType CURRENT_LIST_TYPE = ARTIST_ALBUM;

    @Inject EventBus mEventBus;
    @Inject QueryAppMusic mQueryCase;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    @Inject Context mContext;
    @Inject ControlMediaList mControlMediaList;
    @Inject GetStatusHolder mGetStatusHolder;
    private MusicParams mParams;

    private static final int LOADER_ID_ALBUM = 2;
    private LoaderManager mLoaderManager;

    @Inject
    public ArtistAlbumsPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (view.getItemsCount() > 0) {
                int selectPosition = view.getSelectPosition();
                Cursor cursor;
                String text;
                if (selectPosition < 0) {
                    cursor = view.getItem(0);
                    text = AppMusicContract.Album.getAlbum(cursor);
                    view.setSelectPosition(0);
                } else {
                    long id = view.getItemId(selectPosition);
                    cursor = view.getItem(selectPosition);
                    text = AppMusicContract.Album.getAlbum(cursor);
                }
                notifySelectListInfo(text);
            }
        });
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    public boolean isSphCarDevice() {
        return mGetStatusHolder.execute().getProtocolSpec().isSphCarDevice();
    }

    /**
     * パラメータの設定
     *
     * @param args バンドル
     */
    public void setArguments(Bundle args) {
        mParams = MusicParams.from(args);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mQueryCase.execute(createAlbumsForArtist(mParams.artistId));
    }

    /**
     * LoaderManagerの設定
     *
     * @param loaderManager マネージャ
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        if (mGetStatusHolder.execute().getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC) {
            mLoaderManager = loaderManager;
            mLoaderManager.initLoader(LOADER_ID_ALBUM, Bundle.EMPTY, this);
        } else {
            mLoaderManager.destroyLoader(LOADER_ID_ALBUM);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        AppMusicCursorLoader load = (AppMusicCursorLoader) loader;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setAlbumCursor(data, load.getExtras());
            if (data.getCount() > 0) {
                Cursor cursor = view.getItem(0);
                String text = AppMusicContract.Album.getAlbum(cursor);
                view.setSelectPosition(0);
                notifySelectListInfo(text);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setAlbumCursor(null, Bundle.EMPTY));
    }

    /**
     * アーティストリスト内アルバムリスト楽曲ランダム再生
     */
    public void onArtistAlbumShufflePlayAction() {
        mControlMediaList.exitList();
        mControlAppMusicSource.play(createPlayParams(createSongsForArtist(mParams.artistId), ShuffleMode.ON));
    }

    /**
     * アーティストリスト内アルバムリスト楽曲再生
     *
     * @param id アルバムID
     */
    public void onArtistAlbumPlayAction(long id) {
        mControlMediaList.exitList();
        mControlAppMusicSource.play(createPlayParams(createSongsForArtistAlbum(mParams.artistId, id)));
    }

    /**
     * アーティストリスト内アルバムリスト選択時のアクション
     *
     * @param cursor 選択アルバムのカーソル
     * @param id     選択アルバムID
     */
    public void onArtistAlbumSongListShowAction(Cursor cursor, long id, boolean isFocus) {
        mParams.changeDirectory(AppMusicContract.Album.getAlbum(cursor));
        mParams.albumId = id;
        mParams.isFocus = isFocus;
        mEventBus.post(new NavigateEvent(ScreenId.ARTIST_ALBUM_SONG_LIST, mParams.toBundle()));
    }

    /**
     * リストフォーカスイベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onListFocusAction(ListFocusEvent event) {
        CarDeviceStatus status = mGetStatusHolder.execute().getCarDeviceStatus();
        ListType current = status.listType;

        Optional.ofNullable(getView()).ifPresent(view -> {
            int value = 0;
            if (event.action == RotaryKeyAction.CLOCKWISE) {
                value = event.value;
            } else if (event.action == RotaryKeyAction.COUNTERCLOCKWISE) {
                value = -event.value;
            }
            Cursor cursor;
            String text = "";
            int sectionIndex = view.getSectionIndex();
            int selectPosition = view.getSelectPosition();
            int maxSectionValue = view.getSectionCount();
            int maxPositionValue = view.getItemsCount();
            int newValue;
            switch (event.action) {
                case PUSH:
                    if (current == ListType.ABC_SEARCH_LIST) {
                        mControlMediaList.enterList(ListType.LIST);
                    } else {
                        if (selectPosition < 0) {
                            if(maxPositionValue>0) {
                                cursor = view.getItem(0);
                                text = AppMusicContract.Album.getAlbum(cursor);
                                view.setSelectPosition(0);
                            }
                            notifySelectListInfo(text);
                        } else {
                            long id = view.getItemId(selectPosition);
                            cursor = view.getItem(selectPosition);
                            if (id > 0) {
                                onArtistAlbumSongListShowAction(cursor, id, true);
                            }
                        }
                    }
                    break;
                case CLOCKWISE:
                case COUNTERCLOCKWISE:
                    switch (current) {
                        case ABC_SEARCH_LIST:
                            if(maxSectionValue>0) {
                                newValue = sectionIndex + value;
                                if (newValue >= maxSectionValue) {
                                    newValue = newValue - maxSectionValue;
                                } else if (newValue < 0) {
                                    newValue = maxSectionValue + newValue;
                                }
                                text = view.getSectionString(newValue);
                                view.setSectionIndex(newValue);
                            }
                            break;
                        case LIST:
                            if(maxPositionValue>0) {
                                newValue = selectPosition + value;
                                if (newValue >= maxPositionValue) {
                                    newValue = newValue - maxPositionValue;
                                } else if (newValue < 0) {
                                    newValue = maxPositionValue + newValue;
                                }
                                cursor = view.getItem(newValue);
                                text = AppMusicContract.Album.getAlbum(cursor);
                                view.setSelectPosition(newValue);
                            }
                            break;
                        default:
                            break;
                    }
                    notifySelectListInfo(text);
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * リスト種別変更イベント.
     *
     * @param event ListTypeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onListTypeChangeEvent(ListTypeChangeEvent event) {
        CarDeviceStatus status = mGetStatusHolder.execute().getCarDeviceStatus();
        ListType current = status.listType;
        if (current == ListType.LIST) {
            Optional.ofNullable(getView()).ifPresent(view -> {
                int selectPosition = view.getSelectPosition();
                Cursor cursor = view.getItem(selectPosition);
                String text = AppMusicContract.Album.getAlbum(cursor);

                notifySelectListInfo(text);
            });
        }
    }

    private void notifySelectListInfo(String text){
        mControlMediaList.notifySelectedListInfo(CURRENT_LIST_TYPE.hasParent, CURRENT_LIST_TYPE.hasChild, CURRENT_LIST_TYPE.position, CURRENT_LIST_TYPE.displayInfo, text);
    }
}
