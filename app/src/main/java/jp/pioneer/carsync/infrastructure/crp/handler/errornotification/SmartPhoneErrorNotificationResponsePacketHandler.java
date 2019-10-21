package jp.pioneer.carsync.infrastructure.crp.handler.errornotification;

import android.support.annotation.NonNull;

import com.google.common.base.Objects;

import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import jp.pioneer.carsync.infrastructure.crp.handler.SimpleResponsePacketHandler;

/**
 * SmartPhoneエラー通知応答パケットハンドラ.
 */
public class SmartPhoneErrorNotificationResponsePacketHandler extends SimpleResponsePacketHandler {
    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     *
     */
    public SmartPhoneErrorNotificationResponsePacketHandler(@NonNull CarRemoteSession session) {
        super(session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        super.handle(packet);

        // ResponseCode.OKのみの応答しかないので、一応判定しておく
        if (Objects.equal(getResult(), ResponseCode.NG)) {
            throw new IllegalArgumentException("invalid code: " + ResponseCode.NG);
        }
    }
}
