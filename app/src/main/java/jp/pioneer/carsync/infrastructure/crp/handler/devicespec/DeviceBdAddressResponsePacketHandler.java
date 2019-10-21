package jp.pioneer.carsync.infrastructure.crp.handler.devicespec;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CharSetType;
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
 * 車載機BDアドレス応答パケットハンドラ.
 * <p>
 * {@link StatusHolder}の値を更新するが、タスクで更新イベントを発行することを想定。
 */
public class DeviceBdAddressResponsePacketHandler extends DataResponsePacketHandler {
    private static final int DATA_LENGTH = 19;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceBdAddressResponsePacketHandler(@NonNull CarRemoteSession session) {
        mStatusHolder = checkNotNull(session).getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, DATA_LENGTH);

            CarDeviceSpec carDeviceSpec = mStatusHolder.getCarDeviceSpec();
            // D1-D18:BDアドレス
            carDeviceSpec.bdAddress = TextBytesUtil.extractText(data, 1, CharSetType.UTF8);

            Timber.d("handle() BDAddress = " + carDeviceSpec.bdAddress);
            setResult(Boolean.TRUE);
        } catch (BadPacketException e) {
            Timber.e(e, "handle()");
            setResult(Boolean.FALSE);
        }
    }
}
