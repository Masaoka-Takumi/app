package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.BtAudioSourceController;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

/**
 * BtAudio操作.
 */
public class ControlBtAudioSource {
    @Inject @ForInfrastructure Handler mHandler;
    private BtAudioSourceController mSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public ControlBtAudioSource(CarDevice carDevice) {
        mSourceController = (BtAudioSourceController) carDevice.getSourceController(MediaSourceType.BT_AUDIO);
    }

    /**
     * 再生状態変更
     */
    public void togglePlay() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.togglePlay();
            } else {
                Timber.w("togglePlay() not active.");
            }
        });
    }

    /**
     * 次の楽曲再生
     */
    public void skipNextTrack() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.skipNextTrack();
            } else {
                Timber.w("skipNextTrack() not active.");
            }
        });
    }

    /**
     * 前の楽曲再生
     */
    public void skipPreviousTrack() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.skipPreviousTrack();
            } else {
                Timber.w("skipPreviousTrack() not active.");
            }
        });
    }

    /**
     * ボリュームアップ.
     */
    public void volumeUp() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.volumeUp();
            } else {
                Timber.w("volumeUp() not active.");
            }
        });
    }

    /**
     * ボリュームダウン.
     */
    public void volumeDown() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.volumeDown();
            } else {
                Timber.w("volumeDown() not active.");
            }
        });
    }
}
