package jp.pioneer.carsync.infrastructure.crp.transport;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import jp.pioneer.carsync.domain.model.ConnectionType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * SPPを用いたTransportの実装.
 */
public class BluetoothTransport implements Transport {
    public static final String SERVICE_NAME = "CarRemoteProtocol";
    public static final UUID UUID_SPP = UUID.fromString("87301616-C0F8-48BB-AA38-5D79FAF3DE92");
    private BluetoothSocket mSocket;
    private InputStream mIn;
    private OutputStream mOut;

    /**
     * コンストラクタ.
     *
     * @param socket {@link BluetoothServerSocket#accept()}で得たソケット
     * @throws NullPointerException {@code socket}がnull
     */
    public BluetoothTransport(@NonNull BluetoothSocket socket) {
        mSocket = checkNotNull(socket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(@NonNull StatusHolder statusHolder) throws IOException {
        checkNotNull(statusHolder);
        checkState(mSocket != null);
        checkState(mIn == null);

        statusHolder.setBtDeviceName(getBtDeviceName().trim());
        statusHolder.setConnectionType(ConnectionType.BLUETOOTH);
        mIn = mSocket.getInputStream();
        mOut = mSocket.getOutputStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        close(mOut);
        mOut = null;
        close(mIn);
        mIn = null;
        close(mSocket);
        mSocket = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return Optional.ofNullable(mSocket)
                .map(BluetoothSocket::isConnected)
                .orElse(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(@NonNull byte[] bytes) throws IOException {
        checkNotNull(bytes);
        checkState(mSocket != null);

        mOut.write(bytes);
        mOut.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(@NonNull byte[] bytes) throws IOException {
        checkNotNull(bytes);
        checkState(mSocket != null);

        return mIn.read(bytes);
    }

    private String getBtDeviceName() {
        return Optional.ofNullable(mSocket.getRemoteDevice())
                .map(BluetoothDevice::getName)
                .orElse(null);
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Timber.w(e, "close()");
            }
        }
    }
}
