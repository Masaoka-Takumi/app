package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.BleServiceConnectStatus;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.ConnectedDevicesCountStatus;
import jp.pioneer.carsync.domain.model.PhoneSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.getBitsValue;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * Phone設定ステータス情報パケットプロセッサ.
 * <p>
 * Phone設定ステータス情報応答と通知で使用する。
 */
public class PhoneSettingStatusPacketProcessor {
    private static final int DATA_LENGTH = 6;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public PhoneSettingStatusPacketProcessor(@NonNull StatusHolder statusHolder) {
        mStatusHolder = checkNotNull(statusHolder);
    }

    /**
     * 処理.
     *
     * @param packet 受信パケット
     * @return {@link Boolean#TRUE}:成功。{@link Boolean#FALSE}:それ以外。
     * @throws NullPointerException {@code packet}がnull
     */
    public Boolean process(@NonNull IncomingPacket packet) {
        try {
            byte[] data = checkNotNull(packet).getData();
            checkPacketDataLength(data, DATA_LENGTH);

            int majorVer = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion().major;
            CarDeviceSpec spec = mStatusHolder.getCarDeviceSpec();
            PhoneSettingStatus status = mStatusHolder.getPhoneSettingStatus();
            byte b;
            // D1:有効Phone設定1
            b = data[1];
            // 個別の対応有無が無いので、CarDeviceSpec#phoneSettingSupportedで判定している
            status.pairingClearEnabled = spec.phoneSettingSupported ? isBitOn(b, 3) : false;
            status.pairingAddEnabled = spec.phoneSettingSupported ? isBitOn(b, 2) : false;
            status.inquiryEnabled = spec.phoneSettingSupported ? isBitOn(b, 1) : false;
            status.deviceListEnabled = spec.phoneSettingSupported ? isBitOn(b, 0) : false;
            // D2:有効Phone設定2
            //  (RESERVED)
            // D3:サービス接続/切断 有効状態
            b = data[3];
            // 個別の対応有無が無いので、CarDeviceSpec#phoneSettingSupportedで判定している
            status.audioServiceEnabled = spec.phoneSettingSupported ? isBitOn(b, 1) : false;
            status.phoneServiceEnabled = spec.phoneSettingSupported ? isBitOn(b, 0) : false;
            // D4:接続数状態
            b = data[4];
            status.hfDevicesCountStatus = ConnectedDevicesCountStatus.valueOf(getBitsValue(b, 2, 2));
            status.pairingDevicesCountStatus = ConnectedDevicesCountStatus.valueOf(getBitsValue(b, 0, 2));

            if (majorVer >= 4) {
                v4(data, status);
            }

            status.updateVersion();
            Timber.d("process() PhoneSettingStatus = " + status);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }

    private void v4(byte[] data, PhoneSettingStatus status) {
        byte b;
        CarDeviceSpec spec = mStatusHolder.getCarDeviceSpec();
        // D1:有効Phone設定1
        b = data[1];
        status.autoPairingSettingEnabled = spec.phoneSettingSupported ? isBitOn(b, 5) : false;
        status.autoAnswerSettingEnabled = spec.phoneSettingSupported ? isBitOn(b, 4) : false;
        // D5:接続状態(BLE Service)
        b = data[5];
        status.ancsRelayServiceConnectStatus = BleServiceConnectStatus.valueOf(getBitsValue(b, 4, 4));
        status.ancsServiceConnectStatus = BleServiceConnectStatus.valueOf(getBitsValue(b, 0, 4));
    }
}
