package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
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
import jp.pioneer.carsync.domain.model.MusicCategory;
import jp.pioneer.carsync.domain.model.RotaryKeyAction;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.ListFocusEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.event.TopListEvent;
import jp.pioneer.carsync.presentation.model.AndroidMusicListType;
import jp.pioneer.carsync.presentation.view.PlaylistsView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static jp.pioneer.carsync.domain.content.AppMusicContract.PlayParams.createPlayParams;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createAllPlaylists;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createSongsForPlaylist;
import static jp.pioneer.carsync.presentation.model.AndroidMusicListType.PLAYLIST;

/**
 * プレイリスト画面のPresenter
 */
@PresenterLifeCycle
public class PlaylistsPresenter extends Presenter<PlaylistsView> implements LoaderManager.LoaderCallbacks<Cursor> {
    /** 楽曲リスト種別. */
    private static final AndroidMusicListType CURRENT_LIST_TYPE = PLAYLIST;
    private static final int DELAY_TIME = 500;
    @Inject EventBus mEventBus;
    @Inject QueryAppMusic mQueryCase;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    @Inject AppSharedPreference mPreference;
    @Inject Context mContext;
    @Inject ControlMediaList mControlMediaList;
    @Inject GetStatusHolder mGetStatusHolder;
    private static final int LOADER_ID_PLAYLIST = 4;
    private LoaderManager mLoaderManager;
    private int mSelectPosition = -1;
    private Handler mHandler = new Handler();
    @Inject
    public PlaylistsPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        //タブ選択で階層を戻る時、onResumeが走ってしまうため
        StatusHolder holder = mGetStatusHolder.execute();
        if(holder.getAppStatus().musicCategory!= MusicCategory.PLAYLIST)return;
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(mSelectPosition>=0) {
                view.setSelectPosition(mSelectPosition);
                Cursor cursor = view.getItem(mSelectPosition);
                if(cursor!=null) {
                    String text = AppMusicContract.Playlist.getName(cursor);
                    notifySelectListInfo(text);
                }
            }
        });
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
        Optional.ofNullable(getView()).ifPresent(view -> mSelectPosition = view.getSelectPosition());
    }

    public boolean isSphCarDevice() {
        return mGetStatusHolder.execute().getProtocolSpec().isSphCarDevice();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mQueryCase.execute(createAllPlaylists());
    }

    /**
     * LoaderManagerの設定
     *
     * @param loaderManager マネージャ
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        if (mGetStatusHolder.execute().getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC) {
            mLoaderManager = loaderManager;
            mLoaderManager.initLoader(LOADER_ID_PLAYLIST, Bundle.EMPTY, this);
        } else {
            mLoaderManager.destroyLoader(LOADER_ID_PLAYLIST);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        AppMusicCursorLoader load = (AppMusicCursorLoader) loader;
        Optional.ofNullable(getView()).ifPresent(view -> view.setPlaylistCursor(data, load.getExtras()));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setPlaylistCursor(null, Bundle.EMPTY));
    }

    /**
     * プレイリストがカード表記が有効か否か
     * @return boolean
     */
    public boolean isPlaylistCardEnabled() {
        return mPreference.isPlaylistCardEnabled();
    }

    /**
     * プレイリスト内楽曲再生
     * @param id プレイリスト
     */
    public void onPlaylistPlayAction(long id) {
        mControlMediaList.exitList();
        mControlAppMusicSource.play(createPlayParams(createSongsForPlaylist(id)));
    }

    /**
     * プレイリスト選択時のアクション
     * @param cursor プレイリストのカーソル
     * @param id プレイリストのID
     */
    public void onPlaylistSongListShowAction(Cursor cursor, long id, boolean isFocus) {
        MusicParams params = new MusicParams();
        params.changeDirectory(AppMusicContract.Playlist.getName(cursor));
        params.playlistId = id;
        params.isFocus = isFocus;
        mEventBus.post(new NavigateEvent(ScreenId.PLAYLIST_SONG_LIST, params.toBundle()));
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
        if (current == ListType.ABC_SEARCH_LIST && isPlaylistCardEnabled()) {
            mControlMediaList.enterList(ListType.LIST);
            return;
        }
        Optional.ofNullable(getView()).ifPresent(view -> {
            int value = 0;
            if(event.action== RotaryKeyAction.CLOCKWISE){
                value = event.value;
            }else if(event.action== RotaryKeyAction.COUNTERCLOCKWISE){
                value = -event.value;
            }
            Cursor cursor ;
            String text = "";
            int sectionIndex = view.getSectionIndex();
            int selectPosition = view.getSelectPosition();
            int maxSectionValue = view.getSectionCount();
            int maxPositionValue = view.getItemsCount();
            int newValue;
            switch (event.action){
                case PUSH:
                    if (current == ListType.ABC_SEARCH_LIST) {
                        mControlMediaList.enterList(ListType.LIST);
                    } else {
                        if (selectPosition < 0) {
                            if(maxPositionValue>0) {
                                cursor = view.getItem(0);
                                text = AppMusicContract.Playlist.getName(cursor);
                                view.setSelectPosition(0);
                            }
                            notifySelectListInfo(text);
                        } else {
                            long id = view.getItemId(selectPosition);
                            cursor = view.getItem(selectPosition);
                            if (id > 0) {
                                onPlaylistSongListShowAction(cursor, id,true);
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
                                text = AppMusicContract.Playlist.getName(cursor);
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
        if (current == ListType.ABC_SEARCH_LIST && isPlaylistCardEnabled()) {
            //時間差で送らないとリストタイプが変わらない
            mHandler.postDelayed(() -> {
                mControlMediaList.enterList(ListType.LIST);
            }, DELAY_TIME);
        }
        if (current == ListType.LIST) {
            Optional.ofNullable(getView()).ifPresent(view -> {
                int selectPosition = view.getSelectPosition();
                Cursor cursor = view.getItem(selectPosition);
                String text = AppMusicContract.Playlist.getName(cursor);

                notifySelectListInfo(text);
            });
        }
    }

    private void notifySelectListInfo(String text){
        mControlMediaList.notifySelectedListInfo(CURRENT_LIST_TYPE.hasParent, CURRENT_LIST_TYPE.hasChild, CURRENT_LIST_TYPE.position, CURRENT_LIST_TYPE.displayInfo, text);
    }

    /**
     * TopList遷移イベント.
     *
     * @param event TopListEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTopListEvent(TopListEvent event){
        mSelectPosition = -1;
        Optional.ofNullable(getView()).ifPresent(view -> view.setSelectPosition(-1));
    }

}
