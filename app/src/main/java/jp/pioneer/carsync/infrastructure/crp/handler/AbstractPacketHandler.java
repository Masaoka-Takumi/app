package jp.pioneer.carsync.infrastructure.crp.handler;

import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 応答以外のパケットハンドラ.
 * <p>
 * 車載機からの通知や要求を扱う基本クラス。
 */
public abstract class AbstractPacketHandler implements PacketHandler {
    private CarRemoteSession mSession;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    protected AbstractPacketHandler(@NonNull CarRemoteSession session) {
        mSession = checkNotNull(session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        Optional.ofNullable(doHandle(packet))
                .ifPresent(response -> mSession.sendPacketDirect(response));
    }

    /**
     * ハンドル.
     *
     * @param packet 受信パケット
     * @return 車載機への応答パケット。不要な場合はnull。
     * @throws Exception 何らかの例外発生
     */
    protected abstract OutgoingPacket doHandle(@NonNull IncomingPacket packet) throws Exception;

    /**
     * CarRemoteSession取得.
     *
     * @return CarRemoteSession
     */
    @NonNull
    protected CarRemoteSession getSession() {
        return mSession;
    }

    /**
     * 送信パケットビルダー取得.
     * <p>
     * {@code getSession().getOutgoingPacketBuilder()}と同じ。
     *
     * @return OutgoingPacketBuilder
     */
    @NonNull
    protected OutgoingPacketBuilder getPacketBuilder() {
        return mSession.getOutgoingPacketBuilder();
    }
}

