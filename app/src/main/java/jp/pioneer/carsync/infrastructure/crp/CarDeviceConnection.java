package jp.pioneer.carsync.infrastructure.crp;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.pioneer.carsync.application.ServiceControlManager;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.application.event.AppStateChangeEvent;
import jp.pioneer.carsync.application.event.AppStateChangeEvent.AppState;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TransportStatus;
import jp.pioneer.carsync.infrastructure.crp.event.CrpStatusUpdateEvent;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.SendTask;
import jp.pioneer.carsync.infrastructure.crp.transport.BluetoothTransport;
import jp.pioneer.carsync.infrastructure.crp.transport.Transport;
import jp.pioneer.carsync.infrastructure.crp.transport.UsbTransport;
import jp.pioneer.carsync.presentation.event.DeviceConnectionSuppressEvent;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.transport.BluetoothTransport.SERVICE_NAME;
import static jp.pioneer.carsync.infrastructure.crp.transport.BluetoothTransport.UUID_SPP;

/**
 * 車載機との接続.
 * <p>
 * {@link CarRemoteSession}を管理する。
 * 通信を行う利用者のプロキシとして動作する。
 */
@SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
@Singleton
public class CarDeviceConnection {
    @Inject Context mContext;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject UsbManager mUsbManager;
    @Inject EventBus mEventBus;
    @Inject @ForInfrastructure Handler mHandler;
    @Inject ServiceControlManager mServiceControlManager;
    @Inject AppSharedPreference mPreference;
    private CarRemoteSession mCarRemoteSession;
    private AcceptThread mAcceptThread;
    private UsbAccessoryConnectTask mUsbAccessoryConnectTask;
    private String mUsbAccessoryPermissionAction;
    private Class<? extends BroadcastReceiver> mActionReceiver;

    /**
     * コンストラクタ.
     */
    @Inject
    public CarDeviceConnection() {
    }

    /**
     * 初期化.
     *
     * @param usbAccessoryPermissionAction USBアクセサリの許可結果をブロードキャストする際のIntentアクション
     * @param actionReceiver USBアクセサリの許可結果のブロードキャストのレシーバー
     * @throws NullPointerException {@code usbAccessoryPermissionAction}、または、{@code actionReceiver}がnull
     */
    public void initialize(@NonNull String usbAccessoryPermissionAction, @NonNull Class<? extends BroadcastReceiver> actionReceiver) {
        mUsbAccessoryPermissionAction = checkNotNull(usbAccessoryPermissionAction);
        mActionReceiver = checkNotNull(actionReceiver);
        mEventBus.register(this);
    }

    /**
     * USBアクセサリに接続.
     * <p>
     * USBアクセサリとなっている車載機がいれば接続する。
     */
    public synchronized void connectToUsbAccessory() {
        Timber.i("connectToUsbAccessory()");

        if (mCarRemoteSession != null) {
            Timber.d("connectToUsbAccessory() Already exists session.");
            return;
        }

        if (mStatusHolder.getAppStatus().deviceConnectionSuppress) {
            return;
        }

        if (mUsbAccessoryConnectTask != null) {
            mUsbAccessoryConnectTask.cancel();
        }

        mUsbAccessoryConnectTask = new UsbAccessoryConnectTask();
        mHandler.post(mUsbAccessoryConnectTask);
    }

    /**
     * Bluetooth Socketのリスン.
     * <p>
     * 車載機からのSPP接続を待ち合わせる。
     */
    public synchronized void listenBluetoothSocket() {
        Timber.i("listenBluetoothSocket()");

        if (mCarRemoteSession != null) {
            Timber.d("listenBluetoothSocket() Already exists session.");
            return;
        }

        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
            mStatusHolder.setTransportStatus(TransportStatus.BLUETOOTH_LISTENING);
            mEventBus.post(new CrpStatusUpdateEvent());
        }
    }

    /**
     * パケットダイレクト送信.
     * <p>
     * 未接続の場合、無視する。
     *
     * @param packet 送信パケット
     * @throws NullPointerException {@code packet}がnull
     */
    public synchronized void sendPacketDirect(@NonNull OutgoingPacket packet) {
        Timber.i("sendPacketDirect()");

        if (mCarRemoteSession == null) {
            Timber.w("sendPacketDirect() Not connected.");
            return;
        } else if (mStatusHolder.getSessionStatus() == SessionStatus.PENDING) {
            return;
        }

        // 引数チェックはCarRemoteSession任せ
        mCarRemoteSession.sendPacketDirect(packet);
    }

    /**
     * パケット送信.
     * <p>
     * 未接続の場合、無視する。
     *
     * @param packet 送信パケット
     * @throws NullPointerException {@code packet}がnull
     */
    public synchronized void sendPacket(@NonNull OutgoingPacket packet) {
        Timber.i("sendPacket()");

        if (mCarRemoteSession == null) {
            Timber.w("sendPacket() Not connected.");
            return;
        } else if (mStatusHolder.getSessionStatus() == SessionStatus.PENDING) {
            return;
        }

        // 引数チェックはCarRemoteSession任せ
        mCarRemoteSession.sendPacket(packet);
    }

    /**
     * 要求パケット送信.
     * <p>
     * 応答があるパケットを送信する。
     * 結果の型は、応答パケットの受信パケットハンドラクラスの定義を参照。
     * 未接続の場合、無視する。
     *
     * @param packet 送信パケット
     * @param callback コールバック
     * @param <T> 結果の型
     * @return タスクのFuture。実行出来ない状態の場合null。
     * @throws NullPointerException {@code packet}、または、{@code callback}がnull
     * @throws IllegalArgumentException {@code packet}が応答がないパケット
     */
    public synchronized <T> Future<?> sendRequestPacket(
            @NonNull OutgoingPacket packet,
            @NonNull RequestTask.Callback<T> callback) {
        Timber.i("sendRequestPacket()");

        if (mCarRemoteSession == null) {
            Timber.w("sendRequestPacket() Not connected.");
            return null;
        } else if (mStatusHolder.getSessionStatus() == SessionStatus.PENDING) {
            return null;
        }

        // 引数チェックはCarRemoteSession任せ
        return mCarRemoteSession.sendRequestPacket(packet, callback);
    }

    /**
     * 送信タスク実行.
     * <p>
     * {@link SendTask#inject(CarRemoteSessionComponent)}の呼び出しは、本メソッドにて行う。
     * 利用者はタスクのインスタンス生成まで行い、本メソッドに引き渡す。
     *
     * @param sendTask 送信タスク
     * @return タスクのFuture。実行出来ない状態の場合null。
     * @throws NullPointerException {@code sendTask}がnull
     */
    public synchronized Future<?> executeSendTask(@NonNull SendTask sendTask) {
        Timber.i("executeSendTask()");
        checkNotNull(sendTask);

        if (mCarRemoteSession == null) {
            Timber.w("executeSendTask() Not connected.");
            return null;
        } else if (mStatusHolder.getSessionStatus() == SessionStatus.PENDING) {
            return null;
        }

        sendTask.inject(mCarRemoteSession.getSessionComponent());
        return mCarRemoteSession.executeSendTask(sendTask);
    }

    /**
     * Bluetooth Socketのクローズ.
     * <p>
     * Bluetoothが無効になったら呼び出す。
     */
    public synchronized void closeBluetoothSocket() {
        Timber.i("closeBluetoothSocket()");

        // 実際の所、Bluetoothが無効になった時点でaccept()がIOExceptionとなり、AcceptThreadは終了している。
        if (mAcceptThread != null) {
            mAcceptThread.quit();
            mAcceptThread = null;
            if (mStatusHolder.getTransportStatus() != TransportStatus.USB_CONNECTING) {
                mStatusHolder.setTransportStatus(TransportStatus.UNUSED);
                mEventBus.post(new CrpStatusUpdateEvent());
            }
        }
        //TODO:mAcceptThreadがnull時も接続statusを更新する
    }

    /**
     * 連携解除.
     */
    public synchronized void sessionStop(){
        if(mCarRemoteSession!=null) {
            mCarRemoteSession.stop();
        }
    }

    /**
     * デバッグログ出力.
     *
     * @param format フォーマット
     * @param logText 内容
     */
    public void outputDebugLog(String format, Object[] logText){
        if (mCarRemoteSession != null){
            mCarRemoteSession.outputDebugLog(format, logText);
        }
    }

    /**
     * StatusHolder更新ハンドラ.
     *
     * @param ev CrpStatusUpdateEvent
     */
    @Subscribe
    public synchronized void onCrpStatusUpdateEvent(CrpStatusUpdateEvent ev) {
        if (mCarRemoteSession != null && mStatusHolder.getSessionStatus() == SessionStatus.STOPPED) {
            mCarRemoteSession = null;
            if (mAcceptThread != null) {
                mStatusHolder.setTransportStatus(TransportStatus.BLUETOOTH_LISTENING);
            } else {
                mStatusHolder.setTransportStatus(TransportStatus.UNUSED);
            }
            //mServiceControlManager.stopService();
            mEventBus.post(new CrpStatusUpdateEvent());
        }
    }

    /**
     * アプリケーション状態変更イベントハンドラ.
     *
     * @param ev アプリケーション状態イベント
     */
    @Subscribe
    public void onAppStateChangedEvent(AppStateChangeEvent ev) {

        if(!mPreference.isAppServiceResident()&&ev.appState == AppState.STARTED){
            if(mStatusHolder.getSessionStatus() != SessionStatus.STARTED) {
                mServiceControlManager.initialize();
            }
        }
        if (ev.appState != AppState.STARTED) {
            return;
        }

        BluetoothAdapter adapter = getDefaultBluetoothAdapter();
        if (adapter != null && adapter.getState() == BluetoothAdapter.STATE_ON) {
            listenBluetoothSocket();
        }

        connectToUsbAccessory();
        if (mCarRemoteSession != null) {
            mCarRemoteSession.startSessionIfNeeded();
        }
    }

    /**
     * 連携抑制状態更新イベント.
     *
     * @param event DeviceConnectionSuppressEvent
     */
    @Subscribe
    public void onDeviceConnectionSuppressEvent(DeviceConnectionSuppressEvent event) {
        connectToUsbAccessory();
        if (mCarRemoteSession != null) {
            mCarRemoteSession.startSessionIfNeeded();
        }
    }

    private synchronized boolean connect(Transport transport, TransportStatus status) {
        if (mCarRemoteSession != null) {
            Timber.w("connect() Already exists session.");
            return true;
        }

        mCarRemoteSession = new CarRemoteSession(mContext, transport, mStatusHolder);
        try {
            if(!mPreference.isAppServiceResident()){
                //アプリ常駐設定OFFで接続直後にServiceが停止した場合、連携状態ではServiceを再起動できなくなるため、ここで起動する
                Timber.d("mServiceControlManager.initialize()");
                mServiceControlManager.initialize();
            }
            mCarRemoteSession.start();
            mStatusHolder.setTransportStatus(status);
            mEventBus.post(new CrpStatusUpdateEvent());
            return true;
        } catch (IOException e) {
            Timber.e("connect() " + e.getMessage());
            transport.disconnect();
            mCarRemoteSession = null;
            if (mAcceptThread != null) {
                mStatusHolder.setTransportStatus(TransportStatus.BLUETOOTH_LISTENING);
            } else {
                mStatusHolder.setTransportStatus(TransportStatus.UNUSED);
            }
            mEventBus.post(new CrpStatusUpdateEvent());
            return false;
        }
    }

    @VisibleForTesting
    BluetoothAdapter getDefaultBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    class AcceptThread extends Thread {
        private BluetoothServerSocket mServerSocket;
        private boolean mCanceled;

        @Override
        public void run() {
            Timber.i("run()");

            while (!mCanceled) {
                try {
                    BluetoothAdapter adapter = getDefaultBluetoothAdapter();
                    if (adapter == null) {
                        Thread.sleep(500);
                        Timber.d("run() Failed to bluetooth adapter initialize.");
                        continue;
                    }
                    mServerSocket = adapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, UUID_SPP);
                    Timber.d("run() Accept...");
                    BluetoothSocket socket = mServerSocket.accept();
                    Timber.d("run() Accepted.");
                    connect(new BluetoothTransport(socket), TransportStatus.BLUETOOTH_CONNECTING);
                } catch (IOException e) {
                    Timber.d("run() Failed to accept socket.");
                    close();
                } catch (NullPointerException e) {
                    Timber.d("run() Failed to bluetooth adapter initialize.");
                    close();
                } catch (InterruptedException e) {
                    Timber.d("run() Failed to sleep.");
                }
            }

            close(mServerSocket);
            Timber.d("run() AcceptThread finished.");
        }

        void quit() {
            Timber.i("quit()");
            mCanceled = true;
        }

        private void close() {
            Timber.i("close()");
            close(mServerSocket);
            mServerSocket = null;
        }

        private void close(Closeable closeable) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    Timber.w(e, "close()");
                }
            }
            //TODO:mAcceptThreadをnullにしないとスレッドを再開できない
            //mAcceptThread=null;
        }
    }

    class UsbAccessoryConnectTask implements Runnable {
        private static final int REQUEST_USB_ACCESSORY_PERMISSION = 1;
        private static final long DELAY_TIME = 1000;
        private static final int MAX_TRY_COUNT = 5;
        private int mCount;
        private boolean mIsCanceled;

        UsbAccessoryConnectTask() {
            mCount = MAX_TRY_COUNT;
        }

        @Override
        public void run() {
            Timber.i("run()");

            if (mIsCanceled) {
                return;
            }

            if (mStatusHolder.getAppStatus().deviceConnectionSuppress) {
                return;
            }

            UsbAccessory accessory = getTargetUsbAccessory();
            if (accessory != null) {
                if (!ensurePermission(accessory)) {
                    // 許可待ちなので終了
                    return;
                }

                // 許可を持っているので接続を開始する
                if (connect(new UsbTransport(mUsbManager, accessory), TransportStatus.USB_CONNECTING)) {
                    return;
                }
            }

            if (--mCount >= 0) {
                mHandler.postDelayed(this, DELAY_TIME);
            } else {
                Timber.d("run() Retry over.");
            }
        }

        void cancel() {
            mIsCanceled = true;
        }

        private boolean ensurePermission(UsbAccessory accessory) {
            if (mUsbManager.hasPermission(accessory)) {
                return true;
            } else {
                // 許可を求める
                Intent intent = new Intent(mContext, mActionReceiver);
                intent.setAction(mUsbAccessoryPermissionAction);
                PendingIntent pi = PendingIntent.getBroadcast(mContext, REQUEST_USB_ACCESSORY_PERMISSION, intent, 0);
                mUsbManager.requestPermission(accessory, pi);
                return false;
            }
        }

        private boolean isTargetDevice(UsbAccessory accessory) {
            // res/xml/accessory_filter.xmlから取得した方が望ましいが…
            return "Pioneer".equals(accessory.getManufacturer()) && "99DREAM_ETC".equals(accessory.getModel());
        }

        private UsbAccessory getTargetUsbAccessory() {
            UsbAccessory[] accessories = mUsbManager.getAccessoryList();
            if (accessories == null) {
                return null;
            }

            for (UsbAccessory accessory : accessories) {
                if (isTargetDevice(accessory)) {
                    return accessory;
                }
            }

            return null;
        }
    }
}