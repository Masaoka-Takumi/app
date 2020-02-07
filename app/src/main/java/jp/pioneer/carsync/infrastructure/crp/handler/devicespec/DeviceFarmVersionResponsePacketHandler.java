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

public class DeviceFarmVersionResponsePacketHandler extends DataResponsePacketHandler {
    private static final int MIN_DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceFarmVersionResponsePacketHandler(@NonNull CarRemoteSession session) {
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
            // D1:文字コード
            // D2-N:文字列
            carDeviceSpec.farmVersion = TextBytesUtil.extractText(data, 1);

            Timber.d("handle() farmVersion = " + carDeviceSpec.farmVersion);
            setResult(Boolean.TRUE);
        } catch (BadPacketException e) {
            Timber.e(e, "handle()");
            setResult(Boolean.FALSE);
        }
    }
}