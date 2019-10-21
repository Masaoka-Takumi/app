package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.CharSetType;
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
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacket;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * ペアリングリスト初期情報応答パケットハンドラ.
 */
public class PairingDeviceListInfoResponsePacketHandler extends DataResponsePacketHandler {
    private static final int MIN_DATA_LENGTH = 2;
    private static final int ITEM_LENGTH = 18;
    private CarRemoteSession mSession;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public PairingDeviceListInfoResponsePacketHandler(@NonNull CarRemoteSession session) {
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
            checkPacketDataLength(data, MIN_DATA_LENGTH);

            // D1:ペアリング規格種別
            PairingSpecType type = PairingSpecType.valueOf(data[1]);
            PairingDeviceList list = mStatusHolder.getDebugInfo().getDeviceList(type);
            list.reset();

            int pos = 2;
            checkPacket(((data.length - pos) % ITEM_LENGTH) == 0,
                    "data length invalid. list info = %d", data.length - pos);

            int count = (data.length - pos) / ITEM_LENGTH;
            for (int i = 0; i < count; i++, pos+=ITEM_LENGTH) {
                // offset[0]-offset[17]:BDアドレス
                list.getAddressList().add(TextBytesUtil.extractText(data, pos, CharSetType.UTF8));
            }

            mSession.publishStatusUpdateEvent(packet.getPacketIdType());
            Timber.d("handle() PairingDeviceList = " + list);
            setResult(Boolean.TRUE);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            setResult(Boolean.FALSE);
        }
    }
}
