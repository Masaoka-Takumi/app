package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.CharSetType;
import jp.pioneer.carsync.domain.model.PairingDeviceInfo;
import jp.pioneer.carsync.domain.model.PairingDeviceList;
import jp.pioneer.carsync.domain.model.PairingSpecType;
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
 * ペアリングデバイス情報応答パケットハンドラ.
 */
public class PairingDeviceInfoResponsePacketHandler extends DataResponsePacketHandler {
    private static final int DATA_LENGTH = 53;
    private CarRemoteSession mSession;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public PairingDeviceInfoResponsePacketHandler(@NonNull CarRemoteSession session) {
        mSession = checkNotNull(session);
        mStatusHolder = session.getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, DATA_LENGTH);

            // D1:規格種別
            PairingSpecType type = PairingSpecType.valueOf(data[1]);
            // D2-D19:BDアドレス
            String address = TextBytesUtil.extractText(data, 2, CharSetType.UTF8);
            // D20-D52:BT Link KEY
            String linkKey = TextBytesUtil.extractText(data, 20, CharSetType.UTF8);

            PairingDeviceInfo info = new PairingDeviceInfo(address, linkKey);
            PairingDeviceList list = mStatusHolder.getDebugInfo().getDeviceList(type);
            list.pairingDeviceList.add(info);

            mSession.publishStatusUpdateEvent(packet.getPacketIdType());
            Timber.d("handle() PairingDeviceList = " + list);
            setResult(Boolean.TRUE);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            setResult(Boolean.FALSE);
        }
    }
}