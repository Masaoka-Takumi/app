package jp.pioneer.carsync.infrastructure.crp.handler.settingcommand;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * CUSTOM発光パターン設定通知応答パケットハンドラ.
 */
public class CustomFlashPatternNotificationResponsePacketHandler extends ResponsePacketHandler<Integer> {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     */
    public CustomFlashPatternNotificationResponsePacketHandler(@NonNull CarRemoteSession session) {
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

            // D1:トランザクションID
            int transactionId = ubyteToInt(data[1]);

            setResult(transactionId);
            Timber.d("handle() Result = " + getResult());
        } catch (BadPacketException | IllegalArgumentException e) {
            setResult(-1);
            Timber.e(e, "handle()");
        }
    }
}
