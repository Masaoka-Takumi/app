package jp.pioneer.carsync.infrastructure.crp.handler.settingcommand;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandler;

/**
 * サーチコマンド通知応答パケットハンドラ.
 * <p>
 * 応答を知らせるのみの通知のため、{@link #getResult()}は常に{@link Boolean#TRUE}となる。
 */
public class PhoneSearchCommandResponsePacketHandler extends ResponsePacketHandler<Boolean> {
    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     */
    public PhoneSearchCommandResponsePacketHandler(@NonNull CarRemoteSession session) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        setResult(Boolean.TRUE);
    }
}
