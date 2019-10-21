package jp.pioneer.carsync.infrastructure.crp.handler.settingcommand;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.event.CrpPhoneSearchCommandCompleteEvent;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * サーチコマンド完了通知パケットハンドラ.
 */
public class PhoneSearchCommandCompleteNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public PhoneSearchCommandCompleteNotificationPacketHandler(@NonNull CarRemoteSession session) {
        super(session);
        mStatusHolder = checkNotNull(session).getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OutgoingPacket doHandle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, DATA_LENGTH);

            // D1:結果
            ResponseCode result = ResponseCode.valueOf(data[1]);

            CrpPhoneSearchCommandCompleteEvent ev = new CrpPhoneSearchCommandCompleteEvent(result);
            Timber.d("doHandle() " + ev);
            getSession().publishEvent(ev);
            return getPacketBuilder().createPhoneSearchCompleteNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
