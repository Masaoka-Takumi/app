package jp.pioneer.carsync.presentation.presenter;

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
import jp.pioneer.carsync.domain.event.AppMusicPlaybackModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicTrackChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.CreateNowPlayingList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.SelectTrack;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.NowPlayingListView;

/**
 * NowPlayingListのPresenter
 */
@PresenterLifeCycle
public class NowPlayingListPresenter extends Presenter<NowPlayingListView> implements LoaderManager.LoaderCallbacks<Cursor>{
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetCase;
    @Inject CreateNowPlayingList mCreateList;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    @Inject SelectTrack mSelectTrack;
    private static final int LOADER_ID_NOW_PLAY = 1;
    private LoaderManager mLoaderManager;
    private long  mNowPlaySongId = 0;
    /**
     * コンストラクタ
     */
    @Inject
    public NowPlayingListPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        setNowPlayingSong();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mCreateList.execute();
    }

    /**
     * LoaderManagerの設定
     *
     * @param loaderManager マネージャ
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(LOADER_ID_NOW_PLAY, Bundle.EMPTY, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        AppMusicCursorLoader load = (AppMusicCursorLoader) loader;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setSongCursor(data, load.getExtras());
            boolean isEof = data.moveToFirst();
            int position = 0;
            while (isEof) {
                if (AppMusicContract.Song.getId(data) == mNowPlaySongId) {
                    view.setSelection(position);
                    break;
                }
                position++;
                isEof = data.moveToNext();
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setSongCursor(null, Bundle.EMPTY));
    }

    private AndroidMusicMediaInfo getAndroidMusicMediaInfo() {
        StatusHolder holder = mGetCase.execute();
        CarDeviceMediaInfoHolder mediaHolder = holder.getCarDeviceMediaInfoHolder();
        return mediaHolder.androidMusicMediaInfo;
    }

    private SmartPhoneStatus getSmartPhoneStatus() {
        StatusHolder holder = mGetCase.execute();
        return holder.getSmartPhoneStatus();
    }

    /**
     * プレイヤー状態の更新イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackChangeAction(AppMusicTrackChangeEvent event) {
        setNowPlayingSong();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppMusicPlaybackModeChangeEvent(AppMusicPlaybackModeChangeEvent event) {
        setNowPlayingSong();
    }

    private void setNowPlayingSong(){
        SmartPhoneStatus status = getSmartPhoneStatus();
        AndroidMusicMediaInfo info = getAndroidMusicMediaInfo();
        mNowPlaySongId = info.mediaId;
        Optional.ofNullable(getView()).ifPresent(view -> view.setNowPlaySong(info.trackNumber, info.mediaId, status.playbackMode));
    }

    /**
     * 楽曲再生アクション
     * @param trackNo トラックNo
     */
    public void onSongPlayAction(int trackNo) {
        mSelectTrack.execute(trackNo);
    }

}
