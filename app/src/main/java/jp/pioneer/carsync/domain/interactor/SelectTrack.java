package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.AppMusicSourceController;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

/**
 * トラック選択.
 */
public class SelectTrack {
    @Inject @ForInfrastructure Handler mHandler;
    private AppMusicSourceController mAppMusicSourceController;

    /**
     * コンストラクタ.
     *
     * @param carDevice CarDevice
     */
    @Inject
    public SelectTrack(CarDevice carDevice) {
        mAppMusicSourceController = (AppMusicSourceController) carDevice.getSourceController(MediaSourceType.APP_MUSIC);
    }

    /**
     * 実行.
     * <p>
     * ローカルコンテンツ再生がアクティブ状態の場合に指定されたトラック番号の曲を再生する。
     *
     * @param trackNo トラック番号
     */
    public void execute(int trackNo) {
        mHandler.post(() -> {
            if (mAppMusicSourceController.isActive()) {
                mAppMusicSourceController.selectTrack(trackNo);
            } else {
                Timber.w("execute() not active.");
            }
        });
    }
}
