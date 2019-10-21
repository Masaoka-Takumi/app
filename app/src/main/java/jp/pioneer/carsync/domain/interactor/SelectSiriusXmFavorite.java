package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.SiriusXmSourceController;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.SxmBandType;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * お気に入り選択.
 * <p>
 * Sirius XMのお気に入りリストから選局する場合に使用する。
 */
public class SelectSiriusXmFavorite {
    @Inject @ForInfrastructure Handler mHandler;
    private SiriusXmSourceController mSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public SelectSiriusXmFavorite(CarDevice carDevice) {
        mSourceController = (SiriusXmSourceController) carDevice.getSourceController(MediaSourceType.SIRIUS_XM);
    }

    /**
     * 実行.
     *
     * @param channelNo チャンネルNo.
     * @param bandType  バンドコード.
     * @param sId       SID.
     * @throws NullPointerException {@code bandType} がnull.
     */
    public void execute(int channelNo, @NonNull SxmBandType bandType, int sId) {
        checkNotNull(bandType);

        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.selectFavorite(channelNo, bandType, sId);
            } else {
                Timber.w("execute() not active.");
            }
        });
    }
}
