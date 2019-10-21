package jp.pioneer.carsync.infrastructure.crp;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.pioneer.carsync.application.di.CarRemoteSessionLifeCycle;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.transport.Transport;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * パケット送信スレッド.
 * <p>
 * 送信間隔の調整しか行わないので、リトライやRequest-Response方式のパケット処理が必要な場合は利用者側で面倒見ること。
 */
@CarRemoteSessionLifeCycle
public class PacketSenderThread extends Thread {
    @Inject Transport mTransport;
    @Inject OutgoingPacketRegulator mPacketRegulator;
    @Inject SessionConfig mSessionConfig;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    private OnPacketSendListener mListener;
    private boolean mIsQuit;
    private long mLastSentTime;
    private int mSeqNumber;
    private SparseArrayCompat<OnPacketSendListener> mListeners = new SparseArrayCompat<>();
    private BlockingQueue<OutgoingPacket> mOutgoingPacketQueue= new ArrayBlockingQueue<>(100);

    /**
     * コンストラクタ.
     */
    @Inject
    public PacketSenderThread() {
    }

    /**
     * パケット送信リスナー設定.
     * <p>
     * 全ての送信パケットに関してコールバックされる特別なリスナー。
     *
     * @param listener リスナー。解除する場合null。
     */
    @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
    public void setOnPacketSendListener(@Nullable OnPacketSendListener listener) {
        mListener = listener;
    }

    /**
     * 送信.
     *
     * @param packet 送信パケット
     * @param listener リスナー
     * @return {@code true}:送信キューへの追加成功。{@code false}:それ以外。
     * @throws NullPointerException {@code packet}、または、{@code listener}がnull
     */
    public synchronized boolean send(@NonNull OutgoingPacket packet, @NonNull OnPacketSendListener listener) {
        Timber.i("send()");

        checkNotNull(packet);
        checkNotNull(listener);

        packet.seqNumber = getNextSequenceNumber();
        mListeners.put(packet.seqNumber, listener);
        ProtocolVersion connectingVersion = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();
        ProtocolVersion supportVersion = packet.packetIdType.supportVersion;
        if (supportVersion != null && !connectingVersion.isGreaterThanOrEqual(supportVersion)) {
            Timber.w("send() Unsupported packet. packetIdType = " + packet.packetIdType);
            mListeners.remove(packet.seqNumber);
            return false;
        } else if (!mOutgoingPacketQueue.offer(packet)) {
            Timber.w("send() OutgoingPacketQueue is full.");
            mListeners.remove(packet.seqNumber);
            return false;
        }

        return true;
    }

    /**
     * 終了.
     * <p>
     * パケット送信スレッドを終了する。
     * スレッドの終了は待たない。
     */
    public void quit() {
        Timber.i("quit()");

        mIsQuit = true;
        interrupt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Timber.i("run()");

        try {
            while (!mIsQuit) {
                OutgoingPacket packet = mOutgoingPacketQueue.poll(1000, TimeUnit.MILLISECONDS);
                if (packet == null) {
                    continue;
                }

                if (!mPacketRegulator.canSend(packet)) {
                    notifyPacketDropped(packet);
                    continue;
                }

                try {
                    waitUntilSendTime();
                    notifyPacketSending(packet);
                    mTransport.send(packet.toByteArray());
                    mLastSentTime = SystemClock.elapsedRealtime();
                    notifyPacketSent(packet);
                } catch (IOException e) {
                    notifyPacketSendFailed(packet, e);
                }
            }
        } catch (InterruptedException e) {
            Timber.w("run() Interrupted.");
            Stream.of(mOutgoingPacketQueue)
                    .forEach(this::notifyPacketDropped);
        }

        mOutgoingPacketQueue.clear();
        mListeners.clear();
        mListener = null;

        Timber.d("run() PacketSenderThread finished.");
    }

    /**
     * 送信可能時刻まで待機.
     * <p>
     * 送信間隔を一定以上空けるためにスリープする。
     *
     * @throws InterruptedException 待機中に割り込み発生
     */
    private void waitUntilSendTime() throws InterruptedException {
        long now = SystemClock.elapsedRealtime();
        long waitUntil = mLastSentTime + mSessionConfig.getSendWaitTime();
        if (now < waitUntil) {
            long sleepTime = waitUntil - now;
            Timber.d("waitUntilSendTime() Sleeping %s ms.", sleepTime);
            Thread.sleep(sleepTime);
        }
    }

    private synchronized void notifyPacketDropped(OutgoingPacket packet) {
        mListeners.get(packet.seqNumber).onPacketDropped(packet);
        mListeners.remove(packet.seqNumber);
        Optional.ofNullable(mListener)
                .ifPresent(listener -> listener.onPacketDropped(packet));
    }

    private synchronized void notifyPacketSending(OutgoingPacket packet) {
        mListeners.get(packet.seqNumber).onPacketSending(packet);
        // ここでのmListenersからの削除は不要
        Optional.ofNullable(mListener)
                .ifPresent(listener -> listener.onPacketSending(packet));
    }

    private synchronized void notifyPacketSent(OutgoingPacket packet) {
        mListeners.get(packet.seqNumber).onPacketSent(packet);
        mListeners.remove(packet.seqNumber);
        Optional.ofNullable(mListener)
                .ifPresent(listener -> listener.onPacketSent(packet));
    }

    private synchronized void notifyPacketSendFailed(OutgoingPacket packet, Throwable t) {
        mListeners.get(packet.seqNumber).onPacketSendFailed(packet, t);
        mListeners.remove(packet.seqNumber);
        Optional.ofNullable(mListener)
                .ifPresent(listener -> listener.onPacketSendFailed(packet, t));
    }

    private int getNextSequenceNumber() {
        if (mSeqNumber == Integer.MAX_VALUE) {
            mSeqNumber = 0;
        } else {
            ++mSeqNumber;
        }

        return mSeqNumber;
    }

    /**
     * パケット送信リスナー.
     */
    public interface OnPacketSendListener {
        /**
         * パケット送信開始ハンドラ.
         *
         * @param packet 送信パケット
         */
        void onPacketSending(@NonNull OutgoingPacket packet);

        /**
         * パケット送信完了ハンドラ.
         *
         * @param packet 送信パケット
         */
        void onPacketSent(@NonNull OutgoingPacket packet);

        /**
         * パケットドロップハンドラ.
         *
         * @param packet 送信パケット
         */
        void onPacketDropped(@NonNull OutgoingPacket packet);

        /**
         * パケット送信失敗ハンドラ.
         *
         * @param packet 送信パケット
         * @param t 失敗要因
         */
        void onPacketSendFailed(@NonNull OutgoingPacket packet, @NonNull Throwable t);
    }
}
