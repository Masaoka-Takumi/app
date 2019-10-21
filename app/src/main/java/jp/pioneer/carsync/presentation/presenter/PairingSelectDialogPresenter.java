package jp.pioneer.carsync.presentation.presenter;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.presentation.view.PairingSelectDialogView;

import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED;
import static android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED;
import static android.bluetooth.BluetoothDevice.ACTION_FOUND;
import static android.bluetooth.BluetoothDevice.ACTION_NAME_CHANGED;

/**
 * PairingSelectDialogPresenter
 */
public class PairingSelectDialogPresenter extends Presenter<PairingSelectDialogView> {
    @Inject Context mContext;
    private ArrayAdapter<String> mDeviceAdapter;
    private ArrayList<BluetoothDevice> mDevices = new ArrayList<>();
    private BluetoothAdapter mBtAdapter;
    private BluetoothHeadset mProxyHeadset;
    private BluetoothA2dp mProxyA2dp;
    private boolean mIsRegisterBroadcastReceiver;

    /** ServiceListener. */
    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                mProxyHeadset = (BluetoothHeadset) proxy;
            } else if (profile == BluetoothProfile.A2DP) {
                mProxyA2dp = (BluetoothA2dp) proxy;
            }

            if (mProxyHeadset != null && mProxyA2dp != null) {
                //インテントフィルターとBroadcastReceiverの登録
                if(!mIsRegisterBroadcastReceiver) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(ACTION_DISCOVERY_STARTED);
                    filter.addAction(ACTION_FOUND);
                    filter.addAction(ACTION_NAME_CHANGED);
                    filter.addAction(ACTION_DISCOVERY_FINISHED);
                    filter.addAction(ACTION_BOND_STATE_CHANGED);
                    mContext.registerReceiver(mDeviceFoundReceiver, filter);

                    mIsRegisterBroadcastReceiver = true;
                }

                searchDevice();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            mProxyA2dp = null;
            mProxyHeadset = null;
        }
    };

    /** BroadcastReceiver. */
    private final BroadcastReceiver mDeviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                BluetoothDevice foundDevice;
                switch (action) {
                    case ACTION_DISCOVERY_STARTED: // スキャン開始
                    case ACTION_DISCOVERY_FINISHED: // スキャン終了
                        break;
                    case ACTION_FOUND: // デバイスが検出された
                    case ACTION_NAME_CHANGED: // 名前が検出された
                        foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if ((foundDevice.getName()) != null) {
                            if (foundDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                                // 接続したことのないデバイスのみアダプタに詰める
                                if (!mDevices.contains(foundDevice)) {
                                    mDeviceAdapter.add(foundDevice.getName());
                                    mDevices.add(foundDevice);
                                }
                            }
                        }
                        break;
                    case ACTION_BOND_STATE_CHANGED:
                        break;
                }
            }
        }
    };

    /**
     * コンストラクタ
     */
    @Inject
    public PairingSelectDialogPresenter() {
    }

    public void search(ArrayAdapter<String> adapter) {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
            if(getView()!=null)getView().bluetoothDisabled();
            return;
        }
        mBtAdapter.getProfileProxy(mContext, mProfileListener, BluetoothProfile.HEADSET);
        mBtAdapter.getProfileProxy(mContext, mProfileListener, BluetoothProfile.A2DP);
        mDeviceAdapter = adapter;
    }

    public void pairing(int itemIndex) {
        BluetoothDevice device = mDevices.get(itemIndex);
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            device.createBond();
        }
    }

    public void stop() {
        if (mBtAdapter != null) {
            if (mProxyHeadset != null) {
                mBtAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mProxyHeadset);
                mProxyHeadset = null;
            }
            if (mProxyA2dp != null) {
                mBtAdapter.closeProfileProxy(BluetoothProfile.A2DP, mProxyA2dp);
                mProxyA2dp = null;
            }
            if (mBtAdapter.isDiscovering()) {
                // 検索中の場合は検出をキャンセルする
                mBtAdapter.cancelDiscovery();
            }
            mBtAdapter = null;
            mDevices.clear();
        }

        if(mIsRegisterBroadcastReceiver){
            mContext.unregisterReceiver(mDeviceFoundReceiver);
            mIsRegisterBroadcastReceiver = false;
        }
    }

    private void searchDevice() {
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();
    }
}