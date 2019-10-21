package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.PandoraSourceController;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

/**
 * Pandora操作.
 */
public class ControlPandoraSource {
    @Inject @ForInfrastructure Handler mHandler;
    private PandoraSourceController mSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public ControlPandoraSource(CarDevice carDevice) {
        mSourceController = (PandoraSourceController) carDevice.getSourceController(MediaSourceType.PANDORA);
    }

    /**
     * 再生状態変更.
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
     * 次のトラック再生.
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

    /**
     * ThumbUp設定.
     */
    public void setThumbUp() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.setThumbUp();
            } else {
                Timber.w("setThumbUp() not active.");
            }
        });
    }

    /**
     * ThumbDown設定.
     */
    public void setThumbDown() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.setThumbDown();
            } else {
                Timber.w("setThumbDown() not active.");
            }
        });
    }
}
