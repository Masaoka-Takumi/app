package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.AuxSourceController;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

/**
 * Aux操作.
 */
public class ControlAuxSource {
    @Inject @ForInfrastructure Handler mHandler;
    private AuxSourceController mAuxSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public ControlAuxSource(CarDevice carDevice) {
        mAuxSourceController = (AuxSourceController) carDevice.getSourceController(MediaSourceType.AUX);
    }

    /**
     * ボリュームアップ.
     */
    public void volumeUp() {
        mHandler.post(() -> {
            if (mAuxSourceController.isActive()) {
                mAuxSourceController.volumeUp();
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
            if (mAuxSourceController.isActive()) {
                mAuxSourceController.volumeDown();
            } else {
                Timber.w("volumeDown() not active.");
            }
        });
    }
}
