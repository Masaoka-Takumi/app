package jp.pioneer.carsync.infrastructure.crp.transport;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.pioneer.carsync.domain.model.ConnectionType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * AoAを用いたTransportの実装.
 */
public class UsbTransport implements Transport {
    private UsbManager mManager;
    private UsbAccessory mAccessory;
    private ParcelFileDescriptor mPfd;
    private FileDescriptor mFd;
    private FileInputStream mIn;
    private FileOutputStream mOut;

    /**
     * コンストラクタ.
     *
     * @param manager UsbManager
     * @param accessory 車載機のUsbAccessory
     * @throws NullPointerException {@code manager}、または、{@code accessory}がnull
     */
    public UsbTransport(@NonNull UsbManager manager, @NonNull UsbAccessory accessory) {
        mManager = checkNotNull(manager);
        mAccessory = checkNotNull(accessory);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("manufacturer", mAccessory.getManufacturer())
                .add("model", mAccessory.getModel())
                .add("version", mAccessory.getVersion())
                .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(@NonNull StatusHolder statusHolder) throws IOException {
        checkNotNull(statusHolder);
        checkState(mPfd == null);
        checkState(mFd == null);

        mPfd = mManager.openAccessory(mAccessory);
        if (mPfd == null) {
            throw new IOException("Accessory could not be opened.");
        }

        statusHolder.setConnectionType(ConnectionType.USB);
        mFd = mPfd.getFileDescriptor();
        mIn = new FileInputStream(mFd);
        mOut = new FileOutputStream(mFd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        close(mPfd);
        mPfd = null;
        mFd = null;
        mIn = null;
        mOut = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return (mPfd != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(@NonNull byte[] bytes) throws IOException {
        checkNotNull(bytes);
        checkState(mPfd != null);

        mOut.write(bytes);
        mOut.flush();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(@NonNull byte[] bytes) throws IOException {
        checkNotNull(bytes);
        checkState(mPfd != null);

        return mIn.read(bytes);
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
