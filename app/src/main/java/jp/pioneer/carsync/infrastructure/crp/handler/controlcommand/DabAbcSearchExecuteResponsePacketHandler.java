package jp.pioneer.carsync.infrastructure.crp.handler.controlcommand;

import android.support.annotation.NonNull;

import com.google.common.base.Objects;

import jp.pioneer.carsync.domain.event.CrpDabAbcSearchResultEvent;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.event.CrpListUpdateEvent;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;

import static com.google.common.base.Preconditions.checkNotNull;

public class DabAbcSearchExecuteResponsePacketHandler extends DataResponsePacketHandler {
    private CarRemoteSession mSession;
    private DabAbcSearchExecutePacketProcessor mPacketProcessor;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DabAbcSearchExecuteResponsePacketHandler(@NonNull CarRemoteSession session) {
        mSession = checkNotNull(session);
        mPacketProcessor = new DabAbcSearchExecutePacketProcessor(session.getStatusHolder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        setResult(mPacketProcessor.process(packet));
        if (Objects.equal(getResult(), Boolean.TRUE)) {
            //TODO：不要？
            //mSession.publishStatusUpdateEvent(packet.getPacketIdType());
            mSession.publishEvent(new CrpDabAbcSearchResultEvent());
        }
    }
}