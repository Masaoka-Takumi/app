package jp.pioneer.carsync.infrastructure.crp.handler.devicespec;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.CarDeviceDestinationInfo;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.util.TextBytesUtil;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * 車載機型番応答パケットハンドラ.
 * <p>
 * {@link StatusHolder}の値を更新するが、タスクで更新イベントを発行することを想定。
 */
public class DeviceModelResponsePacketHandler extends DataResponsePacketHandler {
    private static final int MIN_DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceModelResponsePacketHandler(@NonNull CarRemoteSession session) {
        mStatusHolder = checkNotNull(session).getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, MIN_DATA_LENGTH);

            CarDeviceSpec carDeviceSpec = mStatusHolder.getCarDeviceSpec();
            // D1:仕向け情報
            carDeviceSpec.carDeviceDestinationInfo = CarDeviceDestinationInfo.valueOf(data[1]);
            // D2:文字コード
            // D3-N:文字列
            carDeviceSpec.modelName = TextBytesUtil.extractText(data, 2);

            Timber.d("handle() ModelName = " + carDeviceSpec.modelName);
            setResult(Boolean.TRUE);
        } catch (BadPacketException e) {
            Timber.e(e, "handle()");
            setResult(Boolean.FALSE);
        }
    }
}
