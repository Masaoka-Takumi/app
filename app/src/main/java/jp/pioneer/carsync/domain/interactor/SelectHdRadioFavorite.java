package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.HdRadioSourceController;
import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * お気に入り選択.
 * <p>
 * HD Radioのお気に入りリストから選局する場合に使用する。
 */
public class SelectHdRadioFavorite {
    @Inject @ForInfrastructure Handler mHandler;
    private HdRadioSourceController mSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public SelectHdRadioFavorite(CarDevice carDevice) {
        mSourceController = (HdRadioSourceController) carDevice.getSourceController(MediaSourceType.HD_RADIO);
    }

    /**
     * 実行.
     *
     * @param index 周波数インデックス
     * @param bandType バンド種別.
     * @param multicastChannelNumber マルチキャストCH番号
     * @throws NullPointerException {@code bandType} がnull.
     */
    public void selectFavorite(int index, @NonNull HdRadioBandType bandType, int multicastChannelNumber) {
        checkNotNull(bandType);

        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.selectFavorite(index, bandType, multicastChannelNumber);
            } else {
                Timber.w("execute() not active.");
            }
        });
    }
}
