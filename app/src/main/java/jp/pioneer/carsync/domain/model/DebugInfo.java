package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * デバッグ情報.
 */
public class DebugInfo extends SerialVersion {
    /** クラシックBT規格のデバイスリスト. */
    private PairingDeviceList classicBtDeviceList = new PairingDeviceList(PairingSpecType.CLASSIC_BT);
    /** BLE規格のデバイスリスト. */
    private PairingDeviceList bleDeviceList = new PairingDeviceList(PairingSpecType.BLE);

    /**
     * コンストラクタ.
     */
    public DebugInfo() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        classicBtDeviceList.reset();
        bleDeviceList.reset();
        updateVersion();
    }

    /**
     * ペアリングデバイスリスト取得
     *
     * @param type ペアリング規格種別
     * @return ペアリングデバイスリスト
     * @throws NullPointerException {@code type}がnull
     */
    public PairingDeviceList getDeviceList(@NonNull PairingSpecType type){
        if(checkNotNull(type) == PairingSpecType.CLASSIC_BT){
            return classicBtDeviceList;
        } else {
            return bleDeviceList;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("classicBtDeviceList", classicBtDeviceList)
                .add("bleDeviceList", bleDeviceList)
                .toString();
    }
}