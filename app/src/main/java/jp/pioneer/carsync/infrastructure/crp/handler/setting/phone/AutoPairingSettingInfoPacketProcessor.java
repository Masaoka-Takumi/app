package jp.pioneer.carsync.infrastructure.crp.handler.setting.phone;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.PhoneSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * AUTO PAIRING設定情報パケットプロセッサ.
 * <p>
 * AUTO PAIRING設定情報応答と通知で使用する。
 */
public class AutoPairingSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public AutoPairingSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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

            PhoneSetting phoneSetting = mStatusHolder.getPhoneSetting();
            // D1:AUTO PAIRING設定
            phoneSetting.autoPairingSetting = (ubyteToInt(data[1]) == 0x01);

            phoneSetting.updateVersion();
            Timber.d("process() AutoPairingSetting = " + phoneSetting.autoPairingSetting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
