package jp.pioneer.carsync.infrastructure.crp.handler.setting.audio;

import android.support.annotation.NonNull;

import com.google.common.base.Objects;

import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SPEAKER LEVEL情報応答パケットハンドラ.
 */
public class SpeakerLevelSettingInfoResponsePacketHandler extends DataResponsePacketHandler {
    private CarRemoteSession mSession;
    private SpeakerLevelSettingInfoPacketProcessor mPacketProcessor;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public SpeakerLevelSettingInfoResponsePacketHandler(@NonNull CarRemoteSession session) {
        mSession = checkNotNull(session);
        mPacketProcessor = new SpeakerLevelSettingInfoPacketProcessor(session.getStatusHolder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        setResult(mPacketProcessor.process(packet));
        if (Objects.equal(getResult(), Boolean.TRUE)) {
            mSession.publishStatusUpdateEvent(packet.getPacketIdType());
        }
    }
}
