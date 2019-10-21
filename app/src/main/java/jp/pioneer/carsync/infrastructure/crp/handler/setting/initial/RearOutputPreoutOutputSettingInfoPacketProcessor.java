package jp.pioneer.carsync.infrastructure.crp.handler.setting.initial;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.InitialSetting;
import jp.pioneer.carsync.domain.model.RearOutputPreoutOutputSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * REAR出力設定/PREOUT出力設定情報パケットプロセッサ.
 * <p>
 * REAR出力設定/PREOUT出力設定情報応答と通知で使用する。
 */
public class RearOutputPreoutOutputSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public RearOutputPreoutOutputSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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

            InitialSetting initialSetting = mStatusHolder.getInitialSetting();
            // D1:REAR出力設定/PREOUT出力設定
            initialSetting.rearOutputPreoutOutputSetting = RearOutputPreoutOutputSetting.valueOf(data[1]);

            initialSetting.updateVersion();
            Timber.d("process() RearOutputPreoutOutputSetting = " + initialSetting.rearOutputPreoutOutputSetting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
