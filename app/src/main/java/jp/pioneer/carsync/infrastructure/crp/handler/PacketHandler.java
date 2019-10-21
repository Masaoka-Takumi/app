package jp.pioneer.carsync.infrastructure.crp.handler;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;

/**
 * 受信パケットハンドラ.
 */
public interface PacketHandler {
    /**
     * ハンドル.
     *
     * @param packet 受信パケット
     * @throws NullPointerException {@code packet}がnull
     * @throws Exception 何らかの例外発生
     */
    void handle(@NonNull IncomingPacket packet) throws Exception;
}
