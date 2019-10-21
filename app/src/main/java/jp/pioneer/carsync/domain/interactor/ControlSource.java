package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ソース制御.
 */
public class ControlSource {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject CarDevice mCarDevice;
    @Inject @ForDomain StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     */
    @Inject
    public ControlSource() {
    }

    /**
     * ソース選択.
     *
     * @param sourceType ソース種別
     * @throws NullPointerException {@code sourceType}がnull
     */
    public void selectSource(@NonNull MediaSourceType sourceType) {
        checkNotNull(sourceType);

        mHandler.post(() -> {
            CarDeviceStatus carDeviceStatus = mStatusHolder.getCarDeviceStatus();
            if (!carDeviceStatus.availableSourceTypes.contains(sourceType)) {
                Timber.w("execute() not available.");
                return;
            }

            if (carDeviceStatus.sourceType == sourceType && !mStatusHolder.isInterrupted()) {
                Timber.d("execute() no operation.");
                return;
            }

            mCarDevice.selectSource(sourceType);
        });
    }

    /**
     * 次のソースに変更.
     */
    public void changeNextSource() {
        mHandler.post(() -> mCarDevice.changeNextSource());
    }
}
