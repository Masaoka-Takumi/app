package jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * DIMMER設定情報パケットプロセッサ.
 * <p>
 * DIMMER設定情報応答と通知で使用する。
 */
public class DimmerSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 6;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public DimmerSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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

            IlluminationSetting illuminationSetting = mStatusHolder.getIlluminationSetting();
            DimmerSetting setting = illuminationSetting.dimmerSetting;
            // D1:設定値
            DimmerSetting.Dimmer dimmer = DimmerSetting.Dimmer.valueOf(data[1]);
            // D2:開始時刻（Hour）
            // D3:開始時刻（Min）
            // D4:終了時刻（Hour）
            // D5:終了時刻（Min）
            setting.setValue(dimmer, ubyteToInt(data[2]), ubyteToInt(data[3]), ubyteToInt(data[4]), ubyteToInt(data[5]));

            illuminationSetting.updateVersion();
            Timber.d("process() DimmerSetting = " + setting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
