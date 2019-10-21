package jp.pioneer.carsync.infrastructure.component;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Objects;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.BtSettingController;
import jp.pioneer.carsync.domain.content.SettingListContract.DeviceList;
import jp.pioneer.carsync.domain.event.DeviceSearchStartEvent;
import jp.pioneer.carsync.domain.event.SettingListCommandStatusChangeEvent;
import jp.pioneer.carsync.domain.model.AudioDeviceSwitchRequest;
import jp.pioneer.carsync.domain.model.ConnectServiceType;
import jp.pioneer.carsync.domain.model.ConnectedDevicesCountStatus;
import jp.pioneer.carsync.domain.model.DeviceDeleteRequest;
import jp.pioneer.carsync.domain.model.DeviceListItem;
import jp.pioneer.carsync.domain.model.DevicePairingRequest;
import jp.pioneer.carsync.domain.model.DeviceSearchStatus;
import jp.pioneer.carsync.domain.model.PhoneConnectRequestType;
import jp.pioneer.carsync.domain.model.PhoneSearchRequestType;
import jp.pioneer.carsync.domain.model.PhoneServiceRequest;
import jp.pioneer.carsync.domain.model.PhoneSettingStatus;
import jp.pioneer.carsync.domain.model.SearchListItem;
import jp.pioneer.carsync.domain.model.SettingListInfoMap;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.event.CrpAudioDeviceSwitchCompleteEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpPairingAddCommandCompleteEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpPairingDeleteCommandCompleteEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpPhoneSearchCommandCompleteEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpPhoneServiceConnectCompleteEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStartedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;
import timber.log.Timber;

import static dagger.internal.Preconditions.checkNotNull;

/**
 * BtSettingControllerの実装.
 */
@SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
public class BtSettingControllerImpl implements BtSettingController {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject @ForInfrastructure ReentrantLock mLock;
    @Inject EventBus mEventBus;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    private AudioDeviceSwitchRequest mLastAudioDeviceSwitchRequest;
    private PhoneServiceRequest mLastPhoneServiceRequest;
    private DevicePairingRequest mLastDevicePairingRequest;
    private DeviceDeleteRequest mLastDeviceDeleteRequest;

    /** デバイス切り替えCondition */
    private Condition mSwitchDeviceCond;
    /**
     * デバイス切り替えコールバック.
     * <p>
     * デバイス切り替えの応答通知(RequestTask.Callback)と、
     * 完了通知(CrpAudioDeviceSwitchCompleteEvent)を処理する。
     * 結果によってステータスを更新し、
     * 応答通知を受けた場合は{@link Condition#signal()}を実行する。
     */
    private SwitchAudioDeviceCallback mSwitchAudioDeviceCallback = new SwitchAudioDeviceCallback();

    class SwitchAudioDeviceCallback implements RequestTask.Callback<Boolean> {
        @Override
        public void onResult(Boolean result) {
            mLock.lock();
            try {
                SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

                if (Objects.equal(result, Boolean.TRUE)) {
                    settingListInfoMap.audioDeviceSwitchStatus = SettingListInfoMap.CommandStatus.PROCESSING;
                } else {
                    settingListInfoMap.audioDeviceSwitchStatus = SettingListInfoMap.CommandStatus.FAILED;
                }
                mSwitchDeviceCond.signal();

                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));
                Timber.d("onResult() AudioDeviceSwitchStatus = " + settingListInfoMap.audioDeviceSwitchStatus);
            } finally {
                mLock.unlock();
            }
        }

        @Override
        public void onError() {
            mLock.lock();
            try {
                SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

                settingListInfoMap.audioDeviceSwitchStatus = SettingListInfoMap.CommandStatus.FAILED;
                mSwitchDeviceCond.signal();

                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));
                Timber.d("onError() AudioDeviceSwitchStatus = " + settingListInfoMap.audioDeviceSwitchStatus);
            } finally {
                mLock.unlock();
            }
        }

        /**
         * デバイス切り替え完了イベントハンドラ
         *
         * @param ev デバイス切り替え完了イベント
         */
        @Subscribe
        public synchronized void onCrpAudioDeviceSwitchCompleteEvent(CrpAudioDeviceSwitchCompleteEvent ev) {
            if (mLastAudioDeviceSwitchRequest == null || !ev.bdAddress.equals(mLastAudioDeviceSwitchRequest.targetDevice.bdAddress)) {
                return;
            }

            SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();
            if (settingListInfoMap.audioDeviceSwitchStatus == null) {
                return;
            }

            if (ev.result == ResponseCode.OK) {
                settingListInfoMap.audioDeviceSwitchStatus = SettingListInfoMap.CommandStatus.SUCCESS;
            } else {
                settingListInfoMap.audioDeviceSwitchStatus = SettingListInfoMap.CommandStatus.FAILED;
            }

            mEventBus.post(new SettingListCommandStatusChangeEvent(
                    SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));
            Timber.d("onCrpAudioDeviceSwitchCompleteEvent() AudioDeviceSwitchStatus = " + settingListInfoMap.audioDeviceSwitchStatus);
        }
    }

    /** サービスコネクトCondition */
    private Condition mSendPhoneServiceCond;
    /**
     * サービスコネクトコールバック.
     * <p>
     * サービスコネクトの応答通知(RequestTask.Callback)と、
     * 完了通知(CrpPhoneServiceConnectCompleteEvent)を処理する。
     * 結果によってステータスを更新し、
     * 応答通知を受けた場合は{@link Condition#signal()}を実行する。
     */
    private SendPhoneServiceCallback mSendPhoneServiceCallback = new SendPhoneServiceCallback();

    class SendPhoneServiceCallback implements RequestTask.Callback<Boolean> {
        @Override
        public void onResult(Boolean result) {
            mLock.lock();
            try {
                SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

                if (Objects.equal(result, Boolean.TRUE)) {
                    settingListInfoMap.phoneServiceStatus = SettingListInfoMap.CommandStatus.PROCESSING;
                } else {
                    settingListInfoMap.phoneServiceStatus = SettingListInfoMap.CommandStatus.FAILED;
                }
                mSendPhoneServiceCond.signal();

                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.SERVICE_CONNECT));
                Timber.d("onResult() PhoneServiceStatus = " + settingListInfoMap.phoneServiceStatus);
            } finally {
                mLock.unlock();
            }
        }

        @Override
        public void onError() {
            mLock.lock();
            try {
                SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

                settingListInfoMap.phoneServiceStatus = SettingListInfoMap.CommandStatus.FAILED;
                mSendPhoneServiceCond.signal();

                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.SERVICE_CONNECT));
                Timber.d("onError() PhoneServiceStatus = " + settingListInfoMap.phoneServiceStatus);
            } finally {
                mLock.unlock();
            }
        }

        /**
         * サービスコネクト完了イベントハンドラ
         *
         * @param ev サービスコネクト完了イベント
         */
        @Subscribe
        public synchronized void onCrpPhoneServiceConnectCompleteEvent(CrpPhoneServiceConnectCompleteEvent ev) {
            if (mLastPhoneServiceRequest == null || !ev.bdAddress.equals(mLastPhoneServiceRequest.targetDevice.bdAddress)) {
                return;
            }

            SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();
            if (settingListInfoMap.phoneServiceStatus == null) {
                return;
            }

            if (ev.result == ResponseCode.OK) {
                settingListInfoMap.phoneServiceStatus = SettingListInfoMap.CommandStatus.SUCCESS;
            } else {
                settingListInfoMap.phoneServiceStatus = SettingListInfoMap.CommandStatus.FAILED;
            }

            mEventBus.post(new SettingListCommandStatusChangeEvent(
                    SettingListCommandStatusChangeEvent.CommandStatusType.SERVICE_CONNECT));
            Timber.d("onCrpPhoneServiceConnectCompleteEvent() PhoneServiceStatus = " + settingListInfoMap.phoneServiceStatus);
        }
    }

    /** BluetoothデバイスサーチCondition */
    private Condition mPhoneSearchCond;
    /**
     * Bluetoothデバイスサーチコールバック.
     * <p>
     * Bluetoothデバイスサーチの応答通知(RequestTask.Callback)と、
     * 完了通知(CrpPhoneSearchCommandCompleteEvent)を処理する。
     * 結果によってステータスを更新し、
     * 応答通知を受けた場合は{@link Condition#signal()}を実行する。
     */
    private PhoneSearchCallback mPhoneSearchCallback = new PhoneSearchCallback();

    class PhoneSearchCallback implements RequestTask.Callback<Boolean> {
        @Override
        public void onResult(Boolean result) {
            mLock.lock();
            try {
                SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

                if (Objects.equal(result, Boolean.TRUE)) {
                    if (settingListInfoMap.deviceSearchStatus != DeviceSearchStatus.STOP_COMMAND_SENT) {
                        settingListInfoMap.deviceSearchStatus = DeviceSearchStatus.SEARCHING;
                    } else {
                        settingListInfoMap.deviceSearchStatus = DeviceSearchStatus.NONE;
                    }
                } else {
                    settingListInfoMap.deviceSearchStatus = DeviceSearchStatus.FAILED;
                }
                mPhoneSearchCond.signal();

                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));
                Timber.d("onResult() DeviceSearchStatus = " + settingListInfoMap.deviceSearchStatus);
            } finally {
                mLock.unlock();
            }
        }

        @Override
        public void onError() {
            mLock.lock();
            try {
                SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

                settingListInfoMap.deviceSearchStatus = DeviceSearchStatus.FAILED;
                mPhoneSearchCond.signal();

                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));
                Timber.d("onError() DeviceSearchStatus = " + settingListInfoMap.deviceSearchStatus);
            } finally {
                mLock.unlock();
            }
        }

        /**
         * デバイスサーチ完了イベントハンドラ
         *
         * @param ev デバイスサーチ完了イベント
         */
        @Subscribe
        public synchronized void onCrpPhoneSearchCommandCompleteEvent(CrpPhoneSearchCommandCompleteEvent ev) {
            SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

            if (ev.result == ResponseCode.OK) {
                settingListInfoMap.deviceSearchStatus = DeviceSearchStatus.COMPLETED;
            } else {
                settingListInfoMap.deviceSearchStatus = DeviceSearchStatus.FAILED;
            }

            mEventBus.post(new SettingListCommandStatusChangeEvent(
                    SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));
            Timber.d("onCrpPhoneSearchCommandCompleteEvent() DeviceSearchStatus = " + settingListInfoMap.deviceSearchStatus);
        }
    }

    /** ペアリングCondition */
    private Condition mPairingDeviceCond;
    /**
     * ペアリングコールバック.
     * <p>
     * ペアリングの応答通知(RequestTask.Callback)と、
     * 完了通知(CrpPairingAddCommandCompleteEvent)を処理する。
     * 結果によってステータスを更新し、
     * 応答通知を受けた場合は{@link Condition#signal()}を実行する。
     */
    private PairingDeviceCallback mPairingDeviceCallback = new PairingDeviceCallback();

    class PairingDeviceCallback implements RequestTask.Callback<Boolean> {
        @Override
        public void onResult(Boolean result) {
            mLock.lock();
            try {
                SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

                if (Objects.equal(result, Boolean.TRUE)) {
                    settingListInfoMap.devicePairingStatus = SettingListInfoMap.CommandStatus.PROCESSING;
                } else {
                    settingListInfoMap.devicePairingStatus = SettingListInfoMap.CommandStatus.FAILED;

                }
                mPairingDeviceCond.signal();

                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE));
                Timber.d("onResult() DevicePairingStatus = " + settingListInfoMap.devicePairingStatus);
            } finally {
                mLock.unlock();
            }
        }

        @Override
        public void onError() {
            mLock.lock();
            try {
                SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

                settingListInfoMap.devicePairingStatus = SettingListInfoMap.CommandStatus.FAILED;
                mPairingDeviceCond.signal();

                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE));
                Timber.d("onError() DevicePairingStatus = " + settingListInfoMap.devicePairingStatus);
            } finally {
                mLock.unlock();
            }
        }

        /**
         * ペアリング完了イベントハンドラ
         *
         * @param ev ペアリング完了イベント
         */
        @Subscribe
        public synchronized void onCrpPairingAddCommandCompleteEvent(CrpPairingAddCommandCompleteEvent ev) {
            if (mLastDevicePairingRequest == null || !ev.bdAddress.equals(mLastDevicePairingRequest.targetDevice.bdAddress)) {
                return;
            }

            SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();
            if (settingListInfoMap.devicePairingStatus == null) {
                return;
            }

            if (ev.result == ResponseCode.OK) {
                settingListInfoMap.devicePairingStatus = SettingListInfoMap.CommandStatus.SUCCESS;
            } else {
                settingListInfoMap.devicePairingStatus = SettingListInfoMap.CommandStatus.FAILED;
            }

            mEventBus.post(new SettingListCommandStatusChangeEvent(
                    SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE));
            Timber.d("onCrpPairingAddCommandCompleteEvent() DevicePairingStatus = " + settingListInfoMap.devicePairingStatus);
        }
    }

    /** ペアリング解除Condition */
    private Condition mDeleteDeviceCond;
    /**
     * ペアリング解除コールバック.
     * <p>
     * ペアリング解除の応答通知(RequestTask.Callback)と、
     * 完了通知(CrpPairingDeleteCommandCompleteEvent)を処理する。
     * 結果によってステータスを更新し、
     * 応答通知を受けた場合は{@link Condition#signal()}を実行する。
     */
    private DeleteDeviceCallback mDeleteDeviceCallback = new DeleteDeviceCallback();

    class DeleteDeviceCallback implements RequestTask.Callback<Boolean> {
        @Override
        public void onResult(Boolean result) {
            mLock.lock();
            try {
                SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

                if (Objects.equal(result, Boolean.TRUE)) {
                    settingListInfoMap.deviceDeleteStatus = SettingListInfoMap.CommandStatus.PROCESSING;
                } else {
                    settingListInfoMap.deviceDeleteStatus = SettingListInfoMap.CommandStatus.FAILED;
                }
                mDeleteDeviceCond.signal();

                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.DELETE_DEVICE));
                Timber.d("onResult() DeviceDeleteStatus = " + settingListInfoMap.deviceDeleteStatus);
            } finally {
                mLock.unlock();
            }
        }

        @Override
        public void onError() {
            mLock.lock();
            try {
                SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

                settingListInfoMap.deviceDeleteStatus = SettingListInfoMap.CommandStatus.FAILED;
                mDeleteDeviceCond.signal();

                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.DELETE_DEVICE));
                Timber.d("onError() DeviceDeleteStatus = " + settingListInfoMap.deviceDeleteStatus);
            } finally {
                mLock.unlock();
            }
        }

        /**
         * ペアリング解除完了イベントハンドラ.
         *
         * @param ev ペアリング解除完了イベント
         */
        @Subscribe
        public synchronized void onCrpPairingDeleteCommandCompleteEvent(CrpPairingDeleteCommandCompleteEvent ev) {
            if (mLastDeviceDeleteRequest == null || !ev.bdAddress.equals(mLastDeviceDeleteRequest.targetDevice.bdAddress)) {
                return;
            }

            SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();
            if (settingListInfoMap.deviceDeleteStatus == null) {
                return;
            }

            if (ev.result == ResponseCode.OK) {
                settingListInfoMap.deviceDeleteStatus = SettingListInfoMap.CommandStatus.SUCCESS;
            } else {
                settingListInfoMap.deviceDeleteStatus = SettingListInfoMap.CommandStatus.FAILED;
            }

            mEventBus.post(new SettingListCommandStatusChangeEvent(
                    SettingListCommandStatusChangeEvent.CommandStatusType.DELETE_DEVICE));
            Timber.d("onCrpPairingDeleteCommandCompleteEvent() DeviceDeleteStatus = " + settingListInfoMap.deviceDeleteStatus);
        }
    }

    /**
     * コンストラクタ
     */
    @Inject
    public BtSettingControllerImpl() {

    }

    /**
     * 初期化
     */
    public void initialize() {
        mEventBus.register(mSwitchAudioDeviceCallback);
        mEventBus.register(mSendPhoneServiceCallback);
        mEventBus.register(mPairingDeviceCallback);
        mEventBus.register(mDeleteDeviceCallback);
        mEventBus.register(mPhoneSearchCallback);
        mEventBus.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void switchAudioDevice(@NonNull DeviceListItem targetDevice) {
        Timber.i("sendPhoneServiceCommand() targetDevice = %s", targetDevice);
        checkNotNull(targetDevice);

        SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();
        if (mLastAudioDeviceSwitchRequest != null && !settingListInfoMap.audioDeviceSwitchStatus.isAvailable()) {
            // 既に実行中
            return;
        }

        mLock.lock();
        try {
            OutgoingPacket packet = mPacketBuilder.createAudioDeviceSwitchCommand(targetDevice.bdAddress);

            mSwitchDeviceCond = mLock.newCondition();
            mLastAudioDeviceSwitchRequest = createAudioDeviceSwitchRequest(targetDevice);
            settingListInfoMap.audioDeviceSwitchStatus = SettingListInfoMap.CommandStatus.COMMAND_SENT;

            if (mCarDeviceConnection.sendRequestPacket(packet, mSwitchAudioDeviceCallback) == null) {
                // セッションが停止している
                mLastAudioDeviceSwitchRequest = null;
                settingListInfoMap.audioDeviceSwitchStatus = null;
                return;
            }

            while (mStatusHolder.getSettingListInfoMap().audioDeviceSwitchStatus == SettingListInfoMap.CommandStatus.COMMAND_SENT) {
                mSwitchDeviceCond.await();
            }

            if (mLastAudioDeviceSwitchRequest != null) {
                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));
            }
        } catch (InterruptedException e) {
            Timber.d(e, "switchAudioDevice()");
        } finally {
            mLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void sendPhoneServiceCommand(@NonNull DeviceListItem targetDevice, @NonNull EnumSet<ConnectServiceType> serviceTypes, @NonNull PhoneConnectRequestType connectType) {
        Timber.i("sendPhoneServiceCommand() targetDevice = %s, serviceTypes = %s, connectType = %s", targetDevice, serviceTypes, connectType);
        checkNotNull(targetDevice);
        checkNotNull(serviceTypes);
        checkNotNull(connectType);

        PhoneSettingStatus phoneSettingStatus = mStatusHolder.getPhoneSettingStatus();
        if (!phoneSettingStatus.phoneServiceEnabled) {
            Timber.w("connectPhoneServiceCommand() Service is disable.");
            return;
        }

        SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();
        if (mLastPhoneServiceRequest != null && !settingListInfoMap.phoneServiceStatus.isAvailable()) {
            // 既に実行中
            return;
        }

        if (serviceTypes.size() == ConnectServiceType.values().length) {
            if (connectType == PhoneConnectRequestType.CONNECT) {
                // 両方接続が未サポート(Audio接続未サポート)のため、A2DP切り替えを実行
                switchAudioDevice(targetDevice);
            }
            return;
        } else if (serviceTypes.contains(ConnectServiceType.PHONE)) {
            boolean connected = targetDevice.phone1Connected || targetDevice.phone2Connected;
            if (connectType == PhoneConnectRequestType.CONNECT && connected) {
                // 対象が既に接続中
                return;
            } else if (connectType == PhoneConnectRequestType.DISCONNECT && !connected) {
                // 対象が既に切断中
                return;
            } else if (connectType == PhoneConnectRequestType.CONNECT && phoneSettingStatus.hfDevicesCountStatus == ConnectedDevicesCountStatus.FULL) {
                // HF接続が最大
                return;
            }
        } else if (serviceTypes.contains(ConnectServiceType.AUDIO)) {
            if (connectType == PhoneConnectRequestType.CONNECT) {
                // Audio接続が未サポートのため、A2DP切り替えを実行
                switchAudioDevice(targetDevice);
                return;
            }
        }

        mLock.lock();
        try {
            OutgoingPacket packet = mPacketBuilder.createPhoneServiceConnectCommand(
                    targetDevice.bdAddress,
                    connectType,
                    serviceTypes
            );

            mSendPhoneServiceCond = mLock.newCondition();
            mLastPhoneServiceRequest = createPhoneServiceRequest(targetDevice, connectType, serviceTypes);
            settingListInfoMap.phoneServiceStatus = SettingListInfoMap.CommandStatus.COMMAND_SENT;

            if (mCarDeviceConnection.sendRequestPacket(packet, mSendPhoneServiceCallback) == null) {
                // セッションが停止している
                mLastPhoneServiceRequest = null;
                settingListInfoMap.phoneServiceStatus = null;
                return;
            }

            while (mStatusHolder.getSettingListInfoMap().phoneServiceStatus == SettingListInfoMap.CommandStatus.COMMAND_SENT) {
                mSendPhoneServiceCond.await();
            }

            if (mLastPhoneServiceRequest != null) {
                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.SERVICE_CONNECT));
            }

        } catch (InterruptedException e) {
            Timber.d(e, "sendPhoneServiceCommand()");
        } finally {
            mLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void startPhoneSearch(@Nullable DeviceListItem audioConnectDevice) {
        Timber.i("startPhoneSearch() audioConnectDevice = %s", audioConnectDevice);

        PhoneSettingStatus phoneSettingStatus = mStatusHolder.getPhoneSettingStatus();
        if (!phoneSettingStatus.inquiryEnabled) {
            Timber.w("startPhoneSearch() Inquiry disable.");
            return;
        }

        SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();
        if (settingListInfoMap.deviceSearchStatus != null && !settingListInfoMap.deviceSearchStatus.startable) {
            // 既に実行中
            return;
        }

        mLock.lock();
        try {
            OutgoingPacket packet = mPacketBuilder.createPhoneSearchCommand(PhoneSearchRequestType.START);
            mPhoneSearchCond = mLock.newCondition();
            settingListInfoMap.deviceSearchStatus = DeviceSearchStatus.START_COMMAND_SENT;

            mEventBus.post(new DeviceSearchStartEvent());

            if (mCarDeviceConnection.sendRequestPacket(packet, mPhoneSearchCallback) == null) {
                // セッションが停止している
                settingListInfoMap.deviceSearchStatus = DeviceSearchStatus.NONE;
                return;
            }

            while (mStatusHolder.getSettingListInfoMap().deviceSearchStatus == DeviceSearchStatus.START_COMMAND_SENT) {
                mPhoneSearchCond.await();
            }

            mEventBus.post(new SettingListCommandStatusChangeEvent(
                    SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));

        } catch (InterruptedException e) {
            Timber.d(e, "startPhoneSearch()");
        } finally {
            mLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stopPhoneSearch(boolean isReconnectA2dp) {
        Timber.i("stopPhoneSearch()");

        mLock.lock();
        try {
            PhoneSettingStatus phoneSettingStatus = mStatusHolder.getPhoneSettingStatus();
            SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();

            if (phoneSettingStatus.inquiryEnabled && (settingListInfoMap.deviceSearchStatus == null || settingListInfoMap.deviceSearchStatus.stoppable)) {
                OutgoingPacket packet = mPacketBuilder.createPhoneSearchCommand(PhoneSearchRequestType.STOP);
                mPhoneSearchCond = mLock.newCondition();
                settingListInfoMap.deviceSearchStatus = DeviceSearchStatus.STOP_COMMAND_SENT;

                if (mCarDeviceConnection.sendRequestPacket(packet, mPhoneSearchCallback) == null) {
                    // セッションが停止している
                    settingListInfoMap.deviceSearchStatus = DeviceSearchStatus.NONE;
                    return;
                }

                while (mStatusHolder.getSettingListInfoMap().deviceSearchStatus == DeviceSearchStatus.STOP_COMMAND_SENT) {
                    mPhoneSearchCond.await();
                }

                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));
            }
        } catch (InterruptedException e) {
            Timber.d(e, "stopPhoneSearch()");
        } finally {
            mLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void pairingDevice(@NonNull SearchListItem targetDevice, @NonNull EnumSet<ConnectServiceType> serviceTypes) {
        Timber.i("pairingDevice() targetDevice = %s, serviceTypes = %s", targetDevice, serviceTypes);
        checkNotNull(targetDevice);
        checkNotNull(serviceTypes);

        PhoneSettingStatus phoneSettingStatus = mStatusHolder.getPhoneSettingStatus();
        if (!phoneSettingStatus.pairingAddEnabled) {
            Timber.w("pairingDevice() Pairing device is not enabled.");
            return;
        } else if (phoneSettingStatus.pairingDevicesCountStatus == ConnectedDevicesCountStatus.FULL) {
            Timber.w("pairingDevice() Pairing device is full.");
            return;
        }

        SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();
        if (mLastDevicePairingRequest != null && !settingListInfoMap.devicePairingStatus.isAvailable()) {
            // 既に実行中
            return;
        }

        // サーチ停止
        stopPhoneSearch(false);

        mLock.lock();
        try {
            OutgoingPacket packet = mPacketBuilder.createPairingAddCommand(targetDevice.bdAddress, serviceTypes);
            mPairingDeviceCond = mLock.newCondition();
            mLastDevicePairingRequest = new DevicePairingRequest(targetDevice);
            settingListInfoMap.devicePairingStatus = SettingListInfoMap.CommandStatus.COMMAND_SENT;

            if (mCarDeviceConnection.sendRequestPacket(packet, mPairingDeviceCallback) == null) {
                // セッションが停止している
                mLastDevicePairingRequest = null;
                settingListInfoMap.devicePairingStatus = null;
                return;
            }

            while (mStatusHolder.getSettingListInfoMap().devicePairingStatus == SettingListInfoMap.CommandStatus.COMMAND_SENT) {
                mPairingDeviceCond.await();
            }

            if (mLastDevicePairingRequest != null) {
                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE));
            }
        } catch (InterruptedException e) {
            Timber.d(e, "pairingDevice()");
        } finally {
            mLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deleteDevice(@NonNull DeviceListItem targetDevice) {
        Timber.i("deleteDevice() targetDevice = %s", targetDevice);
        checkNotNull(targetDevice);

        PhoneSettingStatus phoneSettingStatus = mStatusHolder.getPhoneSettingStatus();
        if (!phoneSettingStatus.pairingClearEnabled) {
            Timber.w("deleteDevice() Pairing delete is disable.");
            return;
        }

        SettingListInfoMap settingListInfoMap = mStatusHolder.getSettingListInfoMap();
        if (mLastDeviceDeleteRequest != null && !settingListInfoMap.deviceDeleteStatus.isAvailable()) {
            // 既に実行中
            return;
        }

        mLock.lock();
        try {
            OutgoingPacket packet = mPacketBuilder.createPairingDeleteCommand(targetDevice.bdAddress);
            mDeleteDeviceCond = mLock.newCondition();
            mLastDeviceDeleteRequest = createDeviceDeleteRequest(targetDevice);
            settingListInfoMap.deviceDeleteStatus = SettingListInfoMap.CommandStatus.COMMAND_SENT;

            if (mCarDeviceConnection.sendRequestPacket(packet, mDeleteDeviceCallback) == null) {
                // セッションが停止している
                mLastDeviceDeleteRequest = null;
                settingListInfoMap.deviceDeleteStatus = null;
                return;
            }

            while (mStatusHolder.getSettingListInfoMap().deviceDeleteStatus == SettingListInfoMap.CommandStatus.COMMAND_SENT) {
                mDeleteDeviceCond.await();
            }

            if (mLastDeviceDeleteRequest != null) {
                mEventBus.post(new SettingListCommandStatusChangeEvent(
                        SettingListCommandStatusChangeEvent.CommandStatusType.DELETE_DEVICE));
            }

        } catch (InterruptedException e) {
            Timber.d(e, "deleteDevice()");
        } finally {
            mLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized DeviceList.AudioConnectStatus getAudioConnectStatus(@NonNull String bdAddress) {
        Timber.i("getAudioConnectStatus() bdAddress = %s", bdAddress);
        checkNotNull(bdAddress);

        DeviceList.AudioConnectStatus status = DeviceList.AudioConnectStatus.STATUS_DEFAULT;
        if (mLastAudioDeviceSwitchRequest != null) {
            status = mLastAudioDeviceSwitchRequest.getAudioConnectStatus(bdAddress, mStatusHolder);
        }
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized DeviceList.PhoneConnectStatus getPhoneConnectStatus(@NonNull String bdAddress) {
        Timber.i("getPhoneConnectStatus() bdAddress = %s", bdAddress);
        checkNotNull(bdAddress);

        DeviceList.PhoneConnectStatus status = DeviceList.PhoneConnectStatus.STATUS_DEFAULT;
        if (mLastPhoneServiceRequest != null) {
            status = mLastPhoneServiceRequest.getPhoneConnectStatus(bdAddress, mStatusHolder);
        }
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized DeviceList.DeleteStatus getDeleteStatus(@NonNull String bdAddress) {
        Timber.i("getPhoneConnectStatus() bdAddress = %s", bdAddress);
        checkNotNull(bdAddress);

        DeviceList.DeleteStatus status = DeviceList.DeleteStatus.STATUS_DEFAULT;
        if (mLastDeviceDeleteRequest != null) {
            status = mLastDeviceDeleteRequest.getDeleteStatus(bdAddress, mStatusHolder);
        }
        return status;
    }

    /**
     * セッション開始イベントハンドラ
     * <p>
     * オートペアリング機能が有効の場合に、
     * オートペアリングを実施する。
     *
     * @param ev セッションスタートイベント
     */
    @Subscribe
    public synchronized void onCrpSessionStartedEvent(CrpSessionStartedEvent ev) {
        resetRequest();

        Timber.i("onCrpSessionStartedEvent() controller.");
        if (!mStatusHolder.isAutoPainingEnabled()) {
            // オートペアリング設定が無効
            Timber.d("onCrpSessionStartedEvent() Auto Pairing is not enabled.");
            return;
        }

        String bdAddress = mStatusHolder.getCarDeviceSpec().bdAddress.toUpperCase(Locale.ENGLISH);
        if (!checkBluetoothAddress(bdAddress)) {
            // 文字列がBDアドレスの形式となっていない
            Timber.w("onCrpSessionStartedEvent() 'bdAddress' is not in bluetooth address format:%s.", bdAddress);
            return;
        }

        BluetoothAdapterHolder adapter = createBluetoothAdapterHolder(BluetoothAdapter.getDefaultAdapter());
        if (adapter.isNull()) {
            // Bluetooth未対応端末
            Timber.d("onCrpSessionStartedEvent() This device does not support Bluetooth");
            return;

        }
        if (!adapter.isEnabled()) {
            // Bluetooth設定が無効
            Timber.d("onCrpSessionStartedEvent() Bluetooth adapter is not enabled.");
            return;
        }

        BluetoothDeviceHolder targetDevice = createBluetoothDeviceHolder(adapter.getRemoteDevice(bdAddress));
        if (targetDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            boolean callResult = targetDevice.createBond();
            if (callResult) {
                // BOND(ペアリング)成功
                Timber.d("onCrpSessionStartedEvent() createBond succeeded .bdAddress=%s", bdAddress);
            } else {
                // BOND(ペアリング)失敗
                Timber.w("onCrpSessionStartedEvent() createBond failed. bdAddress=%s", bdAddress);
            }
        } else {
            // 既にBOND(ペアリング)しているか、している最中
            Timber.d("onCrpSessionStartedEvent() Target device is bonded or now bonding. bdAddress=%s, bond state=%s", bdAddress, targetDevice.getBondState());
        }
    }

    /**
     * セッション停止イベントハンドラ
     * <p>
     * セッション停止した場合はリクエスト及びステータスの初期化と、
     * 各処理の終了を行うために処理を待つためのConditionを抜ける。
     *
     * @param ev セッションストップイベント
     */
    @Subscribe
    public void onCrpSessionStoppedEvent(CrpSessionStoppedEvent ev) {
        mLock.lock();
        try {
            if (mSwitchDeviceCond != null) {
                mSwitchDeviceCond.signalAll();
            }
            if (mSendPhoneServiceCond != null) {
                mSendPhoneServiceCond.signalAll();
            }
            if (mPhoneSearchCond != null) {
                mPhoneSearchCond.signalAll();
            }
            if (mPairingDeviceCond != null) {
                mPairingDeviceCond.signalAll();
            }
            if (mDeleteDeviceCond != null) {
                mDeleteDeviceCond.signalAll();
            }
        } finally {
            mLock.unlock();
        }

        synchronized (this) {
            resetRequest();

            mDeleteDeviceCond = null;
            mPairingDeviceCond = null;
            mPhoneSearchCond = null;
            mSendPhoneServiceCond = null;
            mSwitchDeviceCond = null;
        }
    }

    /**
     * リクエスト初期化
     */
    private void resetRequest() {
        mLastAudioDeviceSwitchRequest = null;
        mLastPhoneServiceRequest = null;
        mLastDevicePairingRequest = null;
        mLastDeviceDeleteRequest = null;

        mStatusHolder.getSettingListInfoMap().resetStatus();
        mEventBus.post(new SettingListCommandStatusChangeEvent(
                SettingListCommandStatusChangeEvent.CommandStatusType.ALL));
    }

    /**
     * AudioDeviceSwitchRequest生成.
     * <p>
     * UnitTest用
     *
     * @param targetDevice デバイス情報
     * @return AudioDeviceSwitchRequest
     */
    @VisibleForTesting
    AudioDeviceSwitchRequest createAudioDeviceSwitchRequest(DeviceListItem targetDevice) {
        return new AudioDeviceSwitchRequest(targetDevice);
    }

    /**
     * PhoneServiceRequest生成.
     * <p>
     * UnitTest用
     *
     * @param targetDevice デバイス情報
     * @param connectType  接続リクエスト種別
     * @param serviceTypes 接続サービス種別
     * @return PhoneServiceRequest
     */
    @VisibleForTesting
    PhoneServiceRequest createPhoneServiceRequest(DeviceListItem targetDevice, PhoneConnectRequestType connectType, EnumSet<ConnectServiceType> serviceTypes) {
        return new PhoneServiceRequest(targetDevice, connectType, serviceTypes);
    }

    /**
     * DeviceDeleteRequest生成
     * <p>
     * UnitTest用
     *
     * @param targetDevice デバイス情報
     * @return DeviceDeleteRequest
     */
    @VisibleForTesting
    DeviceDeleteRequest createDeviceDeleteRequest(DeviceListItem targetDevice){
        return new DeviceDeleteRequest(targetDevice);
    }

    /**
     * BluetoothAddressチェック.
     * <p>
     * UnitTest用
     *
     * @param bdAddress チェック対象のBDアドレス
     * @return 結果 {@code true}:文字列がBDアドレスの形式となっている {@code false}:文字列がBDアドレスの形式となっていない
     */
    @VisibleForTesting
    boolean checkBluetoothAddress(String bdAddress) {
        return BluetoothAdapter.checkBluetoothAddress(bdAddress);
    }

    /**
     * BluetoothAdapterHolder生成.
     * <p>
     * UnitTest用
     *
     * @param bluetoothAdapter 保持対象のBluetoothAdapter
     * @return BluetoothAdapterHolder
     */
    @VisibleForTesting
    BluetoothAdapterHolder createBluetoothAdapterHolder(BluetoothAdapter bluetoothAdapter) {
        return new BluetoothAdapterHolder(bluetoothAdapter);
    }

    /**
     * BluetoothDeviceHolder生成.
     * <p>
     * UnitTest用
     *
     * @param bluetoothDevice 保持対象のBluetoothDevice
     * @return BluetoothDeviceHolder
     */
    @VisibleForTesting
    BluetoothDeviceHolder createBluetoothDeviceHolder(BluetoothDevice bluetoothDevice) {
        return new BluetoothDeviceHolder(bluetoothDevice);
    }

    /**
     * BluetoothAdapter保持クラス.
     * <p>
     * UnitTest用
     */
    static class BluetoothAdapterHolder {
        BluetoothAdapter mBluetoothAdapter;

        /**
         * コンストラクタ.
         *
         * @param adapter 保持したいBluetoothAdapter
         */
        BluetoothAdapterHolder(BluetoothAdapter adapter) {
            mBluetoothAdapter = adapter;
        }

        /**
         * 取得.
         *
         * @return 保持しているBluetoothAdapter
         */
        BluetoothAdapter get() {
            return mBluetoothAdapter;
        }

        /**
         * BluetoothDevice取得.
         *
         * @param bdAddress BDアドレス
         * @return BluetoothDevice
         */
        BluetoothDevice getRemoteDevice(String bdAddress) {
            return mBluetoothAdapter.getRemoteDevice(bdAddress);
        }

        /**
         * Bluetooth有効判定
         *
         * @return 有効かどうか {@code true}:有効 {@code false}:無効
         */
        boolean isEnabled() {
            return mBluetoothAdapter.isEnabled();
        }

        /**
         * null判定
         *
         * @return 結果 {@code true}:null {@code false}:not null
         */
        boolean isNull() {
            return mBluetoothAdapter == null;
        }
    }

    /**
     * BluetoothDevice保持クラス.
     * <p>
     * UnitTest用
     */
    static class BluetoothDeviceHolder {
        BluetoothDevice mBluetoothDevice;

        /**
         * コンストラクタ.
         *
         * @param device 保持したいBluetoothDevice
         */
        BluetoothDeviceHolder(BluetoothDevice device) {
            mBluetoothDevice = device;
        }

        /**
         * 取得.
         *
         * @return 保持しているBluetoothAdapter
         */
        BluetoothDevice get() {
            return mBluetoothDevice;
        }

        /**
         * Bond状態取得.
         *
         * @return 状態
         */
        int getBondState() {
            return mBluetoothDevice.getBondState();
        }

        /**
         * Bond生成.
         *
         * @return 結果 {@code true}:成功 {@code false}:失敗
         */
        boolean createBond() {
            return mBluetoothDevice.createBond();
        }
    }
}
