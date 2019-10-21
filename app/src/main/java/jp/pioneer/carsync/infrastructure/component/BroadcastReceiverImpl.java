package jp.pioneer.carsync.infrastructure.component;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

import com.google.common.base.Objects;

import javax.inject.Inject;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import timber.log.Timber;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_POWER_CONNECTED;

/**
 * BroadcastReceiverの実装.
 * <p>
 * 車載機との接続、及び、切断の契機となるBroadcastを受信し、接続や切断のキックを行う。
 */
public class BroadcastReceiverImpl extends BroadcastReceiver {
    static final String ACTION_USB_ACCESSORY_PERMISSION = BuildConfig.APPLICATION_ID + ".action.USB_ACCESSORY_PERMISSION";
    private static final String INSTALL_REFERRER = "com.android.vending.INSTALL_REFERRER";
    @Inject CarDeviceConnection mCarDeviceConnection;

    /**
     * コンストラクタ.
     */
    public BroadcastReceiverImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        getAppComponent(context).inject(this);
        String action = intent.getAction();
        Timber.i("onReceive intent = " + action);
        if (Objects.equal(action, ACTION_POWER_CONNECTED)) {
            // 給電が始まったのでUSB接続されたとみなしてUSB接続を試す
            mCarDeviceConnection.connectToUsbAccessory();
        } else if (Objects.equal(action, BluetoothAdapter.ACTION_STATE_CHANGED)) {
            // Bluetoothの状態変更なのでBluetoothだけ
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            if (state == BluetoothAdapter.STATE_ON) {
                mCarDeviceConnection.listenBluetoothSocket();
            } else if (state == BluetoothAdapter.STATE_OFF) {
                mCarDeviceConnection.closeBluetoothSocket();
            }
        } else if (Objects.equal(action, ACTION_BOOT_COMPLETED)
                || Objects.equal(action, INSTALL_REFERRER)) {
            // 両方やってみる
            mCarDeviceConnection.connectToUsbAccessory();
            mCarDeviceConnection.listenBluetoothSocket();
        } else if (Objects.equal(action, ACTION_USB_ACCESSORY_PERMISSION)) {
            // 許可が下りた場合だけUSB接続を試す
            boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
            if (granted) {
                mCarDeviceConnection.connectToUsbAccessory();
            }
        }
    }

    private AppComponent getAppComponent(Context context) {
        return App.getApp(context).getAppComponent();
    }
}
