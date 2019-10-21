package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.RadioSourceController;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

/**
 * TI操作.
 */
public class ControlTiSource {
    @Inject @ForInfrastructure Handler mHandler;
    private RadioSourceController mSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public ControlTiSource(CarDevice carDevice) {
        mSourceController = (RadioSourceController) carDevice.getSourceController(MediaSourceType.TI);
    }
    /**
     * チャンネルアップ.
     */
    public void channelUp() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.manualUp();
            } else {
                Timber.w("tiChannelUp() not active.");
            }
        });
    }

    /**
     * チャンネルダウン.
     */
    public void channelDown() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.manualDown();
            } else {
                Timber.w("tiChannelDown() not active.");
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
