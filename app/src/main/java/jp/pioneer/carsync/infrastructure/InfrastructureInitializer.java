package jp.pioneer.carsync.infrastructure;

import javax.inject.Inject;
import javax.inject.Provider;

import jp.pioneer.carsync.domain.component.BtSettingController;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.ImpactDetector;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;
import jp.pioneer.carsync.domain.repository.SettingListRepository;
import timber.log.Timber;

/**
 * Infrastructure層の初期化者.
 * <p>
 * アプリケーション開始時にインスタンス化しておく必要があるものの処理。
 */
public class InfrastructureInitializer {
    @Inject Provider<CarDevice> mCarDeviceProvider;
    @Inject Provider<ImpactDetector> mImpactDetectorProvider;
    @Inject Provider<CarDeviceMediaRepository> mCarDeviceMediaRepository;
    @Inject Provider<BtSettingController> mBtSettingController;
    @Inject Provider<SettingListRepository> mSettingListRepository;

    /**
     * コンストラクタ.
     */
    @Inject
    public InfrastructureInitializer() {
    }

    /**
     * 初期化.
     */
    public void initialize() {
        Timber.i("initialize()");

        mCarDeviceProvider.get();
        mImpactDetectorProvider.get();
        mCarDeviceMediaRepository.get();
        mBtSettingController.get();
        mSettingListRepository.get();
    }
}
