package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.InitialSettingSpec;
import jp.pioneer.carsync.domain.model.InitialSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * 初期設定ステータス情報パケットプロセッサ.
 * <p>
 * 初期設定ステータス情報応答と通知で使用する。
 */
public class InitialSettingStatusPacketProcessor {
    private static final int DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public InitialSettingStatusPacketProcessor(@NonNull StatusHolder statusHolder) {
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

            InitialSettingSpec spec = mStatusHolder.getCarDeviceSpec().initialSettingSpec;
            InitialSettingStatus status = mStatusHolder.getInitialSettingStatus();
            byte b;
            // D1:有効初期設定1
            b = data[1];
            status.dabAntennaPowerEnabled = spec.dabAntennaPowerSupported ? isBitOn(b, 5) : false;
            status.menuDisplayLanguageSettingEnabled = spec.menuDisplayLanguageSettingSupported ? isBitOn(b, 4) : false;
            status.rearOutputSettingEnabled = spec.rearOutputSettingSupported ? isBitOn(b, 3) : false;
            status.rearOutputPreoutOutputSettingEnabled = spec.rearOutputPreoutOutputSettingSupported ? isBitOn(b, 2) : false;
            status.amStepSettingEnabled = spec.amStepSettingSupported ? isBitOn(b, 1) : false;
            status.fmStepSettingEnabled = spec.fmStepSettingSupported ? isBitOn(b, 0) : false;
            // D2:有効初期設定2
            //  (RESERVED)

            status.updateVersion();
            Timber.d("process() InitialSettingStatus = " + status);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
