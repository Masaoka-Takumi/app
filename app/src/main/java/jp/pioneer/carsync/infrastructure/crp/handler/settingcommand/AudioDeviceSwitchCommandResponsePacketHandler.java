package jp.pioneer.carsync.infrastructure.crp.handler.settingcommand;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandler;

/**
 * Audio Device 切替コマンド通知応答パケットハンドラ.
 * <p>
 * 応答を知らせるのみの通知のため、{@link #getResult()}は常に{@link Boolean#TRUE}となる。
 */
public class AudioDeviceSwitchCommandResponsePacketHandler extends ResponsePacketHandler<Boolean> {
    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     */
    public AudioDeviceSwitchCommandResponsePacketHandler(@NonNull CarRemoteSession session) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        setResult(Boolean.TRUE);
    }
}
