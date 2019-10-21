package jp.pioneer.carsync.infrastructure.crp.handler.controlcommand;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * 連携切断通知パケットハンドラ.
 */
public class DisconnectNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 2;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DisconnectNotificationPacketHandler(@NonNull CarRemoteSession session) {
        super(session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OutgoingPacket doHandle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, DATA_LENGTH);

            // 本来は戻り値で応答パケットを返すが、切断をここで行うため応答を送信してnullを返す。
            getSession().sendPacketDirect(getPacketBuilder().createDisconnectNotificationResponse());
            // 直ぐに切断すると通知が届かないかもしれないので少し待機する
            Thread.sleep(1000);
            // 切断
            getSession().stop();
            return null;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
