package jp.pioneer.carsync.infrastructure.component;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import javax.inject.Inject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.BluetoothHeadsetProvider;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.ConnectedDevicesCountStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.VoiceRecognizeMicType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * BroadcastReceiverの実装.
 */
@SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
public class BluetoothHeadsetProviderImpl extends BroadcastReceiver implements BluetoothHeadsetProvider {
    @Inject Context mContext;
    @Inject Handler mHandler;
    @Inject AudioManager mAudioManager;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject AppSharedPreference mPreference;
    private BluetoothScoConnectTask mBluetoothScoConnectTask = new BluetoothScoConnectTask();
    private BluetoothScoDisconnectTask mBluetoothScoDisconnectTask = new BluetoothScoDisconnectTask();
    private WeakReference<Callback> mCallback;
    private boolean isRegisterBroadcastReceiver;
    private boolean mIsStarting;
    private HeadsetRequestType mRequestType;

    /**
     * コンストラクタ.
     */
    @Inject
    public BluetoothHeadsetProviderImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void prepareBluetoothHeadset(@NonNull Callback callback) {
        Timber.d("prepareBluetoothHeadset");
        checkNotNull(callback);

        if (mCallback != null && mCallback.get() != null) {
            Timber.e("prepareBluetoothHeadset() multiple access.");
            return;
        }

        mCallback = new WeakReference<>(callback);

        if(mStatusHolder.getPhoneSettingStatus().hfDevicesCountStatus == ConnectedDevicesCountStatus.NONE){
            callbackComplete();
            return;
        }
        if(mPreference.getVoiceRecognitionMicType()== VoiceRecognizeMicType.PHONE){
            callbackComplete();
            return;
        }
        mRequestType = HeadsetRequestType.CONNECT;

        if (mAudioManager.isBluetoothScoAvailableOffCall()) {
            if (!isRegisterBroadcastReceiver) {
                registerBroadcastReceiver();
                isRegisterBroadcastReceiver = true;
            }

            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.setBluetoothScoOn(true);
            mBluetoothScoConnectTask.start();
            mIsStarting = true;
        } else {
            callbackError();
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void finishBluetoothHeadset(@NonNull Callback callback, boolean isRinging) {
        Timber.d("finishBluetoothHeadset");
        checkNotNull(callback);

        if (mCallback != null && mCallback.get() != null) {
            Timber.e("finishBluetoothHeadset() multiple access.");
            return;
        }

        mCallback = new WeakReference<>(callback);
        mRequestType = HeadsetRequestType.DISCONNECT;

        mAudioManager.setBluetoothScoOn(false);
        if(isRinging){
            mAudioManager.stopBluetoothSco();
            callbackComplete();
        } else {
            if (!isRegisterBroadcastReceiver) {
                registerBroadcastReceiver();
                isRegisterBroadcastReceiver = true;
            }

            mBluetoothScoDisconnectTask.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onReceive(Context context, Intent intent) {
        Timber.d("onReceive");
        String action = intent.getAction();
        if (Objects.equals(action, BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            if(mRequestType == HeadsetRequestType.CONNECT) {
                callbackError();
            } else {
                callbackComplete();
            }
        } else if (Objects.equals(action, AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR);

            if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                if (mIsStarting) {
                    mIsStarting = false;

                    if(mRequestType == HeadsetRequestType.CONNECT){
                        callbackComplete();
                    } else {
                        callbackError();
                    }
                }
            } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                if (!mIsStarting) {
                    if(mRequestType == HeadsetRequestType.CONNECT) {
                        callbackError();
                    } else {
                        callbackComplete();
                    }
                }
            }
        }
    }

    private void registerBroadcastReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        mContext.registerReceiver(this, filter);
    }

    private synchronized void callbackComplete() {
        Timber.d("complete");
        callback(mRequestType, Result.COMPLETE);
    }

    private synchronized void callbackError() {
        Timber.d("error");
        callback(mRequestType, Result.ERROR);
    }

    private synchronized void callback(HeadsetRequestType type, Result result){
        if(type == HeadsetRequestType.DISCONNECT) {
            mBluetoothScoDisconnectTask.stop();
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
        } else {
            mBluetoothScoConnectTask.stop();
        }

        Callback callback = getCallback(type);
        if (callback == null) {
            return;
        }

        initBroadcastReceiver();
        mHandler.post(() -> callback.onComplete(type, result));
    }

    @Nullable
    private Callback getCallback(HeadsetRequestType type) {
        if (mCallback == null) {
            Timber.w("getCallback() mStartCallback has been cleared.");
            return null;
        }

        Callback callback = mCallback.get();
        mCallback = null;
        if (callback == null) {
            Timber.w("getCallback() start callback has been cleared.");
            return null;
        }

        return callback;
    }

    private void initBroadcastReceiver() {
        if (isRegisterBroadcastReceiver) {
            mContext.unregisterReceiver(this);
            isRegisterBroadcastReceiver = false;
        }
    }

    /**
     * Bluetooth Scoに接続するためのタスク
     * <p>
     * 1秒に1回接続を実施し、2回リトライする
     * 接続(BroadcastReceiverで接続を検知)できなかった場合は、
     * エラーを返す。
     */
    class BluetoothScoConnectTask implements Runnable {
        private static final long DELAY_TIME = 1000;
        private static final int MAX_TRY_COUNT = 2;
        private int mCount;
        private boolean mIsStop;

        /**
         * コンストラクタ
         */
        BluetoothScoConnectTask() {
        }

        public void start(){
            mCount = MAX_TRY_COUNT;
            mIsStop = false;
            mHandler.post(this);
        }

        public void stop(){
            mIsStop = true;
            mHandler.removeCallbacks(this);
        }

        @Override
        public void run() {
            if(mIsStop){
                return;
            }

            if (--mCount >= 0) {
                mAudioManager.startBluetoothSco();
                mHandler.postDelayed(this, DELAY_TIME);
            } else {
                callbackError();
            }
        }
    }

    /**
     * Bluetooth Scoを切断するためのタスク
     * <p>
     * 1.5秒に1回切断を実施し、2回リトライする。
     * リトライが終了後切断(BroadcastReceiverで切断を検知)できなかった場合は、
     * 車載機側からSCOを切断するため車載機へOnHookを通知し、
     * エラーを返す。
     */
    class BluetoothScoDisconnectTask implements Runnable {
        private static final long DELAY_TIME = 1500;
        private static final int MAX_TRY_COUNT = 2;
        private int mCount;
        private boolean mIsStop;

        /**
         * コンストラクタ
         */
        BluetoothScoDisconnectTask() {
        }

        public void start(){
            mCount = MAX_TRY_COUNT;
            mIsStop = false;
            mHandler.post(this);
        }

        public void stop(){
            mIsStop = true;
            mHandler.removeCallbacks(this);
        }

        @Override
        public void run() {
            if(mIsStop){
                return;
            }

            if (--mCount >= 0) {
                mAudioManager.stopBluetoothSco();
                mHandler.postDelayed(this, DELAY_TIME);
            } else {
                requestOnHook();
                callbackError();
            }
        }
    }

    /**
     * ON HOOK通知.
     */
    private void requestOnHook(){
        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.ON_HOOK);
        mCarDeviceConnection.sendPacket(packet);
    }
}