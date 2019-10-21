package jp.pioneer.carsync.domain.interactor;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.model.PairingSpecType;
import jp.pioneer.carsync.domain.repository.PairingDeviceListRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ペアリングデバイスリスト取得.
 * <p>
 * 取得に成功した場合は{@link PairingDeviceListRepository.Callback#onComplete(PairingSpecType)}が呼ばれる
 */
public class GetPairingDeviceList {
    @Inject PairingDeviceListRepository mRepository;

    /**
     * コンストラクタ.
     */
    @Inject
    public GetPairingDeviceList() {

    }

    /**
     * 実行.
     *
     * @param type     ペアリング規格種別
     * @param callback コールバック
     */
    public void execute(@NonNull PairingSpecType type, @NonNull PairingDeviceListRepository.Callback callback) {
        checkNotNull(type);
        checkNotNull(callback);

        mRepository.get(type, callback);
    }
}
