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
import jp.pioneer.carsync.domain.model.RotaryKeyAction;
import jp.pioneer.carsync.presentation.event.ListFocusEvent;
import jp.pioneer.carsync.presentation.model.AndroidMusicListType;
import jp.pioneer.carsync.presentation.view.SongsView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;

import static jp.pioneer.carsync.domain.content.AppMusicContract.PlayParams.createPlayParams;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createSongsForAlbum;
import static jp.pioneer.carsync.presentation.model.AndroidMusicListType.ALBUM_SONG;

/**
 * アルバムリスト内楽曲リスト画面のPresenter
 *
 * @see SongsView
 */
@PresenterLifeCycle
public class AlbumSongsPresenter extends Presenter<SongsView> implements LoaderManager.LoaderCallbacks<Cursor> {
    /** 楽曲リスト種別. */
    private static final AndroidMusicListType CURRENT_LIST_TYPE = ALBUM_SONG;
    private static final int DELAY_TIME = 500;
    @Inject EventBus mEventBus;
    @Inject QueryAppMusic mQueryCase;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    @Inject Context mContext;
    @Inject ControlMediaList mControlMediaList;
    @Inject GetStatusHolder mGetStatusHolder;
    private MusicParams mParams;
    private static final int LOADER_ID_MUSIC = 3;
    private LoaderManager mLoaderManager;
    private Handler mHandler = new Handler();
    @Inject
    public AlbumSongsPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
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
        return mQueryCase.execute(createSongsForAlbum(mParams.albumId));
    }

    /**
     * LoaderManagerの設定
     *
     * @param loaderManager マネージャ
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(LOADER_ID_MUSIC, Bundle.EMPTY, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        AppMusicCursorLoader load = (AppMusicCursorLoader) loader;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setSongCursor(data, load.getExtras());
            if (data.getCount() > 0) {
                Cursor cursor = view.getItem(0);
                String text = AppMusicContract.Song.getTitle(cursor);
                view.setSelectPosition(0);
                notifySelectListInfo(text);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setSongCursor(null, Bundle.EMPTY));
    }

    /**
     * アルバムリスト内楽曲再生アクション
     *
     * @param id 選択楽曲ID
     */
    public void onAlbumSongPlayAction(long id) {
        mControlMediaList.exitList();
        mControlAppMusicSource.play(createPlayParams(createSongsForAlbum(mParams.albumId), id));
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
        if (current == ListType.ABC_SEARCH_LIST) {
            mControlMediaList.enterList(ListType.LIST);
            return;
        }
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
                                text = AppMusicContract.Song.getTitle(cursor);
                                view.setSelectPosition(0);
                            }
                            notifySelectListInfo(text);
                        } else {
                            long id = view.getItemId(selectPosition);
                            if (id > 0)
                                onAlbumSongPlayAction(id);
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
                                text = AppMusicContract.Song.getTitle(cursor);
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
        if (current == ListType.ABC_SEARCH_LIST) {
            //時間差で送らないとリストタイプが変わらない
            mHandler.postDelayed(() -> {
                mControlMediaList.enterList(ListType.LIST);
            }, DELAY_TIME);
        }
        if (current == ListType.LIST) {
            Optional.ofNullable(getView()).ifPresent(view -> {
                int selectPosition = view.getSelectPosition();
                Cursor cursor = view.getItem(selectPosition);
                String text = AppMusicContract.Song.getTitle(cursor);
                notifySelectListInfo(text);
            });
        }
    }

    private void notifySelectListInfo(String text){
        mControlMediaList.notifySelectedListInfo(CURRENT_LIST_TYPE.hasParent, CURRENT_LIST_TYPE.hasChild, CURRENT_LIST_TYPE.position, CURRENT_LIST_TYPE.displayInfo, text);
    }

}
