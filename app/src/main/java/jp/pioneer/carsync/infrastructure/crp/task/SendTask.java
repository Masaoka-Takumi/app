package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.PacketSenderThread;
import jp.pioneer.carsync.infrastructure.crp.SendTimeoutException;
import jp.pioneer.carsync.infrastructure.crp.SessionConfig;
import jp.pioneer.carsync.infrastructure.crp.UnexpectedPacketException;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 送信タスク.
 * <p>
 * パケットを送信する際に使用する基底クラス。
 * リトライやRequest-Response方式のパケット処理を実装している。
 * インスタンス生成後に{@link #inject(CarRemoteSessionComponent)}を
 * 呼び出すこと。
 */
public abstract class SendTask implements Runnable {
    @Inject ReentrantLock mLock;
    @Inject SessionConfig mSessionConfig;
    @Inject PacketSenderThread mPacketSenderThread;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject TaskStatusMonitor mStatusMonitor;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;
    private Condition mResponseReceivedCond;
    private OutgoingPacket mOutgoingPacket;

    /**
     * インジェクト.
     * <p>
     * {@link CarRemoteSessionComponent}にサブクラス用のinjectメソッドを定義しインジェクションを行う。
     *
     * @param component コンポーネント
     * @return 本オブジェクト
     * @throws NullPointerException {@code component}がnull
     */
    public abstract SendTask inject(@NonNull CarRemoteSessionComponent component);

    /**
     * 送信タスクID取得.
     *
     * @return 送信タスクID
     */
    @NonNull
    public abstract SendTaskId getSendTaskId();

    /**
     * 受信パケットハンドラ.
     * <p>
     * 応答パケットを処理する。
     *
     * @param packet 受信パケット
     * @return {@code true}:受信パケットを処理した。{@code false}:それ以外。
     * @throws NullPointerException {@code packet}がnull
     * @throws Exception 何らかの例外発生
     */
    public boolean handlePacket(@NonNull IncomingPacket packet) throws Exception {
        Timber.i("handlePacket()");

        checkNotNull(packet);

        if (packet.getPacketIdType() != mOutgoingPacket.packetIdType.responsePacketIdType) {
            Timber.d("handlePacket() Unexpected packet.");
            return false;
        }

        mLock.lock();
        try {
            boolean isContinue;
            do {
                try {
                    isContinue = false;
                    doResponsePacket(packet);
                } catch (UnexpectedPacketException e) {
                    Timber.w("handlePacket() Unexpected packet. " + e.getMessage());
                    isContinue = true;
                }
            } while (isContinue);

            mResponseReceivedCond.signal();
            return true;
        } catch (Exception e) {
            mResponseReceivedCond.signal();
            throw e;
        } finally {
            mLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Timber.i("run()");

        try {
            mResponseReceivedCond = mLock.newCondition();
            mStatusMonitor.onTaskStarted(this);
            doTask();
            mStatusMonitor.onTaskFinished(this);
        } catch (InterruptedException e) {
            mStatusMonitor.onTaskCanceled(this);
        } catch (Exception e) {
            mStatusMonitor.onTaskFailed(this, e);
        }
    }

    /**
     * 要求.
     * <p>
     * 応答があるパケットの送信時に使用する。
     *
     * @param packet 送信パケット
     * @return {@code true}:応答を受け取れた。{@code false}:それ以外。
     * @throws NullPointerException {@code packet}がnull
     * @throws InterruptedException 待機中に割り込み発生
     * @throws SendTimeoutException 送信タイムアウト発生（応答無し）
     */
    boolean request(@NonNull OutgoingPacket packet) throws InterruptedException, SendTimeoutException {
        Timber.i("request()");

        return requestInternal(packet);
    }

    /**
     * 要求（送信タイムアウト発生しない版）.
     * <p>
     * 応答があるパケットの送信時に使用する。
     * {@link #request(OutgoingPacket)}と違い、応答が無くても{@link SendTimeoutException}が発生しない。
     * 応答が無くても処理を進めることが出来る場合に使用する。（設定要求前の最新状態取得等）
     *
     * @param packet 送信パケット
     * @return {@code true}:応答を受け取れた。{@code false}:それ以外。
     * @throws NullPointerException {@code packet}がnull
     * @throws InterruptedException 割り込み（キャンセル）発生
     */
    boolean requestNoSendTimeout(@NonNull OutgoingPacket packet) throws InterruptedException {
        Timber.i("requestNoSendTimeout()");
        try {
            return requestInternal(packet);
        } catch (SendTimeoutException e) {
            Timber.w(e, "requestNoSendTimeout()");
            return false;
        }
    }

    /**
     * 通知.
     * <p>
     * 応答がないタイプ（車載機への応答もこのタイプ）のパケットの送信時に使用する。
     *
     * @param packet 送信パケット
     * @return {@code true}:成功。{@code false}:失敗
     * @throws InterruptedException 待機中に割り込み（キャンセル）が発生した
     */
    boolean post(@NonNull OutgoingPacket packet) throws InterruptedException {
        Timber.i("post()");

        SyncPacketSendListener listener = new SyncPacketSendListener();
        if (mPacketSenderThread.send(packet, listener)) {
            return false;
        }

        return listener.isSent();
    }

    /**
     * 送信パケットビルダー取得.
     * <p>
     * パッケージプライベートなので直接フィールドを参照出来るが、見なかったことにして
     * 本メソッドを使用すること。
     *
     * @return 送信パケットビルダー
     */
    @NonNull
    OutgoingPacketBuilder getPacketBuilder() {
        return mPacketBuilder;
    }

    /**
     * 応答パケットハンドラファクトリ取得.
     * <p>
     * パッケージプライベートなので直接フィールドを参照出来るが、見なかったことにして
     * 本メソッドを使用すること。
     *
     * @return 応答パケットハンドラファクトリ
     */
    @NonNull
    ResponsePacketHandlerFactory getResponsePacketHandlerFactory() {
        return mHandlerFactory;
    }

    /**
     * タスク実施.
     *
     * @throws Exception 何らかの例外発生
     */
    abstract void doTask() throws Exception;

    /**
     * 応答パケット処理.
     * <p>
     * 応答を受け取るサブクラスは本メソッドをオーバーライドすること。
     *
     * @param packet 応答パケット
     * @throws Exception 何らかの例外発生
     */
    void doResponsePacket(@NonNull IncomingPacket packet) throws Exception {
        throw new UnsupportedOperationException("require override.");
    }

    private boolean requestInternal(@NonNull OutgoingPacket packet)
            throws InterruptedException, SendTimeoutException {
        mLock.lock();
        try {
            mOutgoingPacket = packet;
            for (int i = 0; i <= mSessionConfig.getSendRetryCount(); i++) {
                SyncPacketSendListener listener = new SyncPacketSendListener();
                if (!mPacketSenderThread.send(packet, listener)) {
                    return false;
                }

                Timber.d("await...");
                boolean responseReceived = mResponseReceivedCond.await(
                        mSessionConfig.getSendRetryInterval(), TimeUnit.MILLISECONDS);
                Timber.d("awaken. responseReceived = %s", responseReceived);
                if (responseReceived) {
                    return true;
                }
            }

            throw new SendTimeoutException(packet);
        } finally {
            mLock.unlock();
        }
    }
}
