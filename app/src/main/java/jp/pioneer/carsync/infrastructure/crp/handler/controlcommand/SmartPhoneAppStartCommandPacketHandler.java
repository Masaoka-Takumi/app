package jp.pioneer.carsync.infrastructure.crp.handler.controlcommand;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.event.AppStartCommandEvent;
import jp.pioneer.carsync.domain.model.CharSetType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.util.TextBytesUtil;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * App起動コマンド通知パケットハンドラ.
 */
public class SmartPhoneAppStartCommandPacketHandler extends AbstractPacketHandler {
    private static final int MIN_DATA_LENGTH = 2; // 2はNUL文字のみ（＝空文字列）であり現実的にはエラーであるがここでは良しとしておく
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public SmartPhoneAppStartCommandPacketHandler(@NonNull CarRemoteSession session) {
        super(session);
        mStatusHolder = session.getStatusHolder();
    }

    @Override
    protected OutgoingPacket doHandle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, MIN_DATA_LENGTH);

            // D1-DN:起動情報
            String packageName = TextBytesUtil.extractText(data, 1, CharSetType.UTF8);

            getSession().publishEvent(new AppStartCommandEvent(packageName));

            Timber.d("doHandle() packageName = " + packageName);
            return null;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
