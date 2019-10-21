package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.PacketSenderThread;

/**
 * 同期パケット送信リスナー.
 * <p>
 * パケットの送信処理が行われるまで待ち合わせる際に使用するリスナー。
 */
public class SyncPacketSendListener implements PacketSenderThread.OnPacketSendListener {
    private CountDownLatch mCountDownLatch = new CountDownLatch(1);
    private boolean mIsSent;

    /**
     * パケットが送信されたか否か取得.
     * <p>
     * パケットの送信処理が行われるまで待機する。
     *
     * @return {@code true}:送信された。{@code false}:それ以外。
     * @throws InterruptedException 待機中に割り込み発生
     */
    public boolean isSent() throws InterruptedException {
        mCountDownLatch.await();
        return mIsSent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketSending(@NonNull OutgoingPacket packet) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketSent(@NonNull OutgoingPacket packet) {
        mIsSent = true;
        mCountDownLatch.countDown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketDropped(@NonNull OutgoingPacket packet) {
        mCountDownLatch.countDown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketSendFailed(@NonNull OutgoingPacket packet, @NonNull Throwable t) {
        mCountDownLatch.countDown();
    }
}
