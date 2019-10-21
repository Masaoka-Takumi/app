package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.SettingListTransaction;
import jp.pioneer.carsync.domain.model.SettingListType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * 設定リスト初期情報応答ハンドラ.
 */
public class InitialSettingListInfoResponsePacketHandler extends DataResponsePacketHandler {
    private static final int DATA_LENGTH = 5;
    private CarRemoteSession mSession;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public InitialSettingListInfoResponsePacketHandler(@NonNull CarRemoteSession session) {
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

            // D1:設定種別
            // D2:リスト種別
            SettingListType listType = SettingListType.valueOf(data[1], data[2]);
            // D3-D4:件数
            int total = PacketUtil.ushortToInt(data, 3);

            SettingListTransaction transaction = mStatusHolder.getSettingListInfoMap().getTransaction(listType);
            transaction.setInitialInfo(listType, total);

            mSession.publishStatusUpdateEvent(packet.getPacketIdType());
            Timber.d("handle() SettingListTransaction = " + transaction);
            setResult(Boolean.TRUE);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            setResult(Boolean.FALSE);
        }
    }
}
