package jp.pioneer.carsync.infrastructure.crp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Optional;

import java.io.IOException;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.CarRemoteSessionLifeCycle;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.transport.Transport;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * パケット受信スレッド.
 */
@CarRemoteSessionLifeCycle
public class PacketReaderThread extends Thread {
    @Inject Transport mTransport;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    private OnPacketReceivedListener mListener;
    private boolean mIsQuit;

    /**
     * コンストラクタ.
     */
    @Inject
    public PacketReaderThread() {
    }

    /**
     * パケット受信リスナー設定.
     *
     * @param listener リスナー。解除する場合null。
     */
    public void setOnPacketSendListener(@Nullable OnPacketReceivedListener listener) {
        mListener = listener;
    }

    /**
     * 終了.
     * <p>
     * パケット受信スレッドを終了する。
     * スレッドの終了は待たない。
     */
    public void quit() {
        Timber.i("quit()");

        mIsQuit = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Timber.i("run()");

        byte[] buf = new byte[1024];
        IncomingPacket packet = new IncomingPacket();
        while (!mIsQuit) {
            try {
                Timber.d("run() Reading...");
                int len = mTransport.read(buf);
                Timber.d("run() Read. len = %d", len);
                if (len == -1) {
                    Timber.d("run() EOF.");
                    break;
                }

                for (int i = 0; i < len; i++) {
                    try {
                        packet.appendByte(ubyteToInt(buf[i]));
                        if (packet.isCompleted()) {
                            Timber.d("run() Read completed.");
                            ProtocolVersion connectingVersion = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();
                            ProtocolVersion supportVersion = packet.getPacketIdType().supportVersion;
                            if (supportVersion != null && !connectingVersion.isGreaterThanOrEqual(supportVersion)) {
                                // サポートしていないパケットの場合は無視して次に進める
                                Timber.e("run() Unsupported packet.");
                                notifyPacketDropped(packet);
                            } else {
                                notifyPacketReceived(packet);
                            }

                            packet = new IncomingPacket();
                        }
                    } catch (IllegalArgumentException e) {
                        // パケットデータが異常な場合は無視して次に進める
                        Timber.e(e, "run() Invalid packet data.");
                        notifyPacketDropped(packet);
                        packet = new IncomingPacket();
                    }
                }
            } catch (IOException e) {
                // 読み込みが出来ないので終了
                Timber.d("run() Transport#read() failed.");
                notifyPacketReadFailed(e);
                break;
            }
        }

        mListener = null;

        Timber.d("run() PacketReaderThread finished.");
    }

    private void notifyPacketReceived(IncomingPacket packet) {
        Optional.ofNullable(mListener)
                .ifPresent(l -> l.onPacketReceived(packet));
    }

    private void notifyPacketDropped(IncomingPacket packet) {
        Optional.ofNullable(mListener)
                .ifPresent(l -> l.onPacketDropped(packet));
    }

    private void notifyPacketReadFailed(Throwable t) {
        Optional.ofNullable(mListener)
                .ifPresent(l -> l.onPacketReadFailed(t));
    }

    /**
     * パケット受信リスナー.
     */
    public interface OnPacketReceivedListener {
        /**
         * パケット受信ハンドラ.
         *
         * @param packet 受信パケット
         */
        void onPacketReceived(@NonNull IncomingPacket packet);

        /**
         * パケットドロップハンドラ.
         *
         * @param packet 受信パケット
         */
        void onPacketDropped(@NonNull IncomingPacket packet);

        /**
         * パケット読み込み失敗ハンドラ.
         *
         * @param t 失敗要因
         */
        void onPacketReadFailed(@NonNull Throwable t);
    }
}
