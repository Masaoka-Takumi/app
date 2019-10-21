package jp.pioneer.carsync.infrastructure.crp.handler;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * シンブルな応答パケットハンドラ.
 * <p>
 * D1に{@link ResponseCode}が設定されるのみのシンブルな構成の応答パケットで使用する。
 * パケットが不正な場合、{@link #getResult()}は{@link ResponseCode#NG}となる。
 */
public class SimpleResponsePacketHandler extends ResponsePacketHandler<ResponseCode> {
    private static final int DATA_LENGTH = 2;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     */
    public SimpleResponsePacketHandler(@NonNull CarRemoteSession session) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, DATA_LENGTH);

            // D1:結果
            setResult(ResponseCode.valueOf(data[1]));

            Timber.d("handle() %s Result = %s", packet.getPacketIdType(), getResult());
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "handle()");
            setResult(ResponseCode.NG);
        }
    }
}
