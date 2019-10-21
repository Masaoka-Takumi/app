package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CustomFlashRequestType;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

/**
 * CUSTOM発光パターン操作.
 */
public class ControlCustomFlashPattern {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject AppSharedPreference mPreference;
    @Inject CarDevice mCarDevice;

    /**
     * コンストラクタ.
     */
    @Inject
    public ControlCustomFlashPattern() {
    }

    /**
     * 開始.
     * <p>
     * SPH(専用機)以外の場合は何もしない
     * CUSTOM発光パターンが無効な場合は何もしない
     * ライティングエフェクトが無効な場合は何もしない
     */
    public void start() {
        mHandler.post(() -> {
            if (!mStatusHolder.getProtocolSpec().isSphCarDevice()) {
                Timber.w("start() car device is not sph.");
                return;
            }

            if (!mStatusHolder.getIlluminationSettingStatus().customFlashPatternSettingEnabled) {
                Timber.w("start() custom flash pattern setting disabled.");
                return;
            }

            if (mStatusHolder.getIlluminationSetting().customFlashPatternRequestStatus != RequestStatus.SENT_COMPLETE) {
                Timber.w("start() custom flash pattern not sent.");
                return;
            }

            if (!mPreference.isLightingEffectEnabled()) {
                Timber.w("start() lighting effect disabled.");
                return;
            }

            mCarDevice.requestCustomFlash(CustomFlashRequestType.START);
        });
    }

    /**
     * 終了.
     * <p>
     * SPH(専用機)以外の場合は何もしない
     * CUSTOM発光パターンが無効な場合は何もしない
     */
    public void finish() {
        mHandler.post(() -> {
            if (mStatusHolder.getProtocolSpec().getCarDeviceClassId() != CarDeviceClassId.SPH) {
                Timber.w("start() car device is not sph.");
                return;
            }

            if (!mStatusHolder.getIlluminationSettingStatus().customFlashPatternSettingEnabled) {
                Timber.w("finish() custom flash pattern setting disabled.");
                return;
            }

            mCarDevice.requestCustomFlash(CustomFlashRequestType.FINISH);
        });
    }
}
