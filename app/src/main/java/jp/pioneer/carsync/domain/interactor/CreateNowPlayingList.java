package jp.pioneer.carsync.domain.interactor;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.AppMusicSourceController;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.repository.NowPlayingListRepository;
import timber.log.Timber;

/**
 * 再生中プレイリストAppMusicCursorLoader生成.
 * <p>
 * 現在再生中のプレイリストを取得するための{@link AppMusicCursorLoader}を生成する.
 * 再生中の曲リストは{@link AppMusicSourceController#getPlaylistCursor()}を使用して取得する.
 */
public class CreateNowPlayingList {
    private AppMusicSourceController mAppMusicSourceController;
    @Inject NowPlayingListRepository mRepository;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public CreateNowPlayingList(CarDevice carDevice) {
        mAppMusicSourceController = (AppMusicSourceController) carDevice.getSourceController(MediaSourceType.APP_MUSIC);
    }

    /**
     * 実行.
     * <p>
     * ローカルコンテンツ再生がアクティブ状態の場合に現在再生中のプレイリストを生成する。
     *
     * @return {@link AppMusicCursorLoader} 非アクティブ状態の場合はnull
     */
    @Nullable
    public AppMusicCursorLoader execute(){
        if(mAppMusicSourceController.isActive()) {
            return mRepository.get(mAppMusicSourceController.getPlaylistCursor());
        } else {
            Timber.w("execute() not active.");
            return null;
        }
    }
}
