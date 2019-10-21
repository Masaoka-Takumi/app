package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.DabSourceController;
import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * お気に入り選択.
 * <p>
 * DABのお気に入りリストから選局する場合に使用する。
 */
public class SelectDabFavorite {
    @Inject @ForInfrastructure Handler mHandler;
    private DabSourceController mSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public SelectDabFavorite(CarDevice carDevice) {
        mSourceController = (DabSourceController) carDevice.getSourceController(MediaSourceType.DAB);
    }

    /**
     * 実行.
     *
     * @param index 周波数インデックス
     * @param bandType バンド種別.
     * @param eid EID
     * @param sid SID
     * @param scids SCIdS
     * @throws NullPointerException {@code bandType} がnull.
     */
    public void selectFavorite(int index, @NonNull DabBandType bandType, int eid, long sid, int scids) {
        checkNotNull(bandType);

        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.selectFavorite(index, bandType, eid, sid, scids);
            } else {
                Timber.w("execute() not active.");
            }
        });
    }
}
