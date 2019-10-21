package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.RadioSourceController;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * お気に入り選択.
 * <p>
 * ラジオのお気に入りリストから選局する場合に使用する。
 */
public class SelectRadioFavorite {
    @Inject @ForInfrastructure Handler mHandler;
    private RadioSourceController mSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public SelectRadioFavorite(CarDevice carDevice) {
        mSourceController = (RadioSourceController) carDevice.getSourceController(MediaSourceType.RADIO);
    }

    /**
     * 実行.
     *
     * @param index    周波数index.
     * @param bandType バンドコード.
     * @param pi       PI.
     * @throws NullPointerException {@code bandType} がnull.
     */
    public void execute(int index, @NonNull RadioBandType bandType, int pi) {
        checkNotNull(bandType);

        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.selectFavorite(index, bandType, pi);
            } else {
                Timber.w("execute() not active.");
            }
        });
    }
}
