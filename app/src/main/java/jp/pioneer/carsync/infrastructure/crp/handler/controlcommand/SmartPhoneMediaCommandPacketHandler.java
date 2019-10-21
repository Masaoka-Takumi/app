package jp.pioneer.carsync.infrastructure.crp.handler.controlcommand;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.infrastructure.crp.event.CrpSmartPhoneMediaCommandEvent;
import jp.pioneer.carsync.infrastructure.crp.entity.SmartPhoneMediaCommand;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * メディアコマンド通知パケットハンドラ.
 */
public class SmartPhoneMediaCommandPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public SmartPhoneMediaCommandPacketHandler(@NonNull CarRemoteSession session) {
        super(session);
        mStatusHolder = session.getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OutgoingPacket doHandle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, DATA_LENGTH);

            // D1:メディアコマンド
            SmartPhoneMediaCommand command = SmartPhoneMediaCommand.valueOf(data[1]);

            getSession().publishEvent(new CrpSmartPhoneMediaCommandEvent(command));
            return null;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
