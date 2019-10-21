package jp.pioneer.carsync.infrastructure.component;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import jp.pioneer.carsync.domain.content.SettingListContract.DeviceList;
import jp.pioneer.carsync.domain.event.SettingListCommandStatusChangeEvent;
import jp.pioneer.carsync.domain.model.AudioDeviceSwitchRequest;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.ConnectServiceType;
import jp.pioneer.carsync.domain.model.ConnectedDevicesCountStatus;
import jp.pioneer.carsync.domain.model.DeviceDeleteRequest;
import jp.pioneer.carsync.domain.model.DeviceListItem;
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

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/19.
 */
public class BtSettingControllerImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks BtSettingControllerImpl mBtSettingController = new BtSettingControllerImpl(){
        // AudioStatus取得時にリクエストがnullの場合の値が確認できないため下記のように実装
        @Override
        AudioDeviceSwitchRequest createAudioDeviceSwitchRequest(DeviceListItem targetDevice) {
            mIsCalledCreateAudioDeviceSwitchRequest = true;
            return super.createAudioDeviceSwitchRequest(targetDevice);
        }

        // PhoneStatus取得時にリクエストがnullの場合の値が確認できないため下記のように実装
        @Override
        PhoneServiceRequest createPhoneServiceRequest(DeviceListItem targetDevice, PhoneConnectRequestType connectType, EnumSet<ConnectServiceType> serviceTypes) {
            mIsCalledCreatePhoneServiceRequest = true;
            return super.createPhoneServiceRequest(targetDevice, connectType, serviceTypes);
        }

        // DeleteStatus取得時にリクエストがnullの場合の値が確認できないため下記のように実装
        @Override
        DeviceDeleteRequest createDeviceDeleteRequest(DeviceListItem targetDevice) {
            mIsCalledCreateDeviceDeleteRequest = true;
            return super.createDeviceDeleteRequest(targetDevice);
        }

        @Override
        boolean checkBluetoothAddress(String bdAddress) {
            mIsCalledCheckBluetoothAddress = true;
            return bdAddress.equals(BD_ADDRESS);
        }

        @Override
        BluetoothDeviceHolder createBluetoothDeviceHolder(BluetoothDevice bluetoothDevice) {
            return mBluetoothDeviceHolder;
        }

        @Override
        BluetoothAdapterHolder createBluetoothAdapterHolder(BluetoothAdapter bluetoothAdapter) {
            return mBluetoothAdapterHolder;
        }
    };
    @Mock StatusHolder mStatusHolder;
    @Mock EventBus mEventBus;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock ReentrantLock mLock;

    @Mock OutgoingPacket mOutgoingPacket;
    @Mock Condition mCondition;

    SettingListInfoMap mSettingListInfoMap;
    PhoneSettingStatus mPhoneSettingStatus;
    CarDeviceSpec mCarDeviceSpec;
    BtSettingControllerImpl.BluetoothAdapterHolder mBluetoothAdapterHolder = new BtSettingControllerImpl.BluetoothAdapterHolder(null){
        @Override
        BluetoothDevice getRemoteDevice(String bdAddress) {
            return null;
        }

        @Override
        boolean isEnabled() {
            mIsCalledBluetoothAdapterIsEnabled = true;
            return mIsBluetoothAdapterEnabled;
        }

        @Override
        boolean isNull() {
            mIsCalledBluetoothAdapterIsNull = true;
            return mIsBluetoothAdapterNull;
        }
    };
    BtSettingControllerImpl.BluetoothDeviceHolder mBluetoothDeviceHolder = new BtSettingControllerImpl.BluetoothDeviceHolder(null){
        @Override
        int getBondState() {
            mIsCalledBluetoothDeviceGetBondState = true;
            return mBluetoothDeviceBondState;
        }

        @Override
        boolean createBond() {
            mIsCalledBluetoothDeviceCreateBond = true;
            return mIsBluetoothDeviceCreateBond;
        }
    };

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal;
    ArgumentCaptor<SettingListCommandStatusChangeEvent> mCaptor = ArgumentCaptor.forClass(SettingListCommandStatusChangeEvent.class);
    boolean mIsStopSearch;
    boolean mIsBluetoothAdapterEnabled;
    boolean mIsBluetoothAdapterNull;
    boolean mIsBluetoothDeviceCreateBond;
    int mBluetoothDeviceBondState;

    boolean mIsCalledCreateAudioDeviceSwitchRequest;
    boolean mIsCalledCreatePhoneServiceRequest;
    boolean mIsCalledCreateDeviceDeleteRequest;
    boolean mIsCalledCheckBluetoothAddress;
    boolean mIsCalledBluetoothAdapterIsEnabled;
    boolean mIsCalledBluetoothAdapterIsNull;
    boolean mIsCalledBluetoothDeviceGetBondState;
    boolean mIsCalledBluetoothDeviceCreateBond;

    static final String BD_ADDRESS = "BD_ADDRESS";
    static final String WRONG_BD_ADDRESS = "WRONG_BD_ADDRESS";

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mSignal = new CountDownLatch(1);
        mIsStopSearch = false;
        mIsBluetoothAdapterEnabled = true;
        mIsBluetoothAdapterNull = false;
        mIsBluetoothDeviceCreateBond = true;
        mBluetoothDeviceBondState = BluetoothDevice.BOND_NONE;

        mIsCalledCreateAudioDeviceSwitchRequest = false;
        mIsCalledCreatePhoneServiceRequest = false;
        mIsCalledCreateDeviceDeleteRequest = false;
        mIsCalledCheckBluetoothAddress = false;
        mIsCalledBluetoothAdapterIsEnabled = false;
        mIsCalledBluetoothAdapterIsNull = false;
        mIsCalledBluetoothDeviceGetBondState = false;
        mIsCalledBluetoothDeviceCreateBond = false;

        mSettingListInfoMap = new SettingListInfoMap();
        mSettingListInfoMap.audioDeviceSwitchStatus = SettingListInfoMap.CommandStatus.SUCCESS;
        mSettingListInfoMap.phoneServiceStatus = SettingListInfoMap.CommandStatus.SUCCESS;
        mSettingListInfoMap.devicePairingStatus = SettingListInfoMap.CommandStatus.SUCCESS;
        mSettingListInfoMap.deviceDeleteStatus = SettingListInfoMap.CommandStatus.SUCCESS;
        mSettingListInfoMap.deviceSearchStatus = DeviceSearchStatus.NONE;
        mPhoneSettingStatus = new PhoneSettingStatus();
        mPhoneSettingStatus.pairingClearEnabled = true;
        mPhoneSettingStatus.pairingAddEnabled = true;
        mPhoneSettingStatus.inquiryEnabled = true;
        mPhoneSettingStatus.deviceListEnabled = true;
        mPhoneSettingStatus.audioServiceEnabled = true;
        mPhoneSettingStatus.phoneServiceEnabled = true;
        mCarDeviceSpec = new CarDeviceSpec();
        mCarDeviceSpec.bdAddress = "bd_address";

        when(mStatusHolder.getSettingListInfoMap()).thenReturn(mSettingListInfoMap);
        when(mStatusHolder.getPhoneSettingStatus()).thenReturn(mPhoneSettingStatus);
        when(mStatusHolder.getCarDeviceSpec()).thenReturn(mCarDeviceSpec);
        when(mStatusHolder.isAutoPainingEnabled()).thenReturn(true);
        when(mLock.newCondition()).thenReturn(mCondition);
    }

    @Test
    public void initialize() throws Exception {
        // exercise
        mBtSettingController.initialize();

        // verify
        verify(mEventBus, times(1)).register(any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class));
        verify(mEventBus, times(1)).register(any(BtSettingControllerImpl.SendPhoneServiceCallback.class));
        verify(mEventBus, times(1)).register(any(BtSettingControllerImpl.PairingDeviceCallback.class));
        verify(mEventBus, times(1)).register(any(BtSettingControllerImpl.DeleteDeviceCallback.class));
        verify(mEventBus, times(1)).register(any(BtSettingControllerImpl.PhoneSearchCallback.class));
        verify(mEventBus).register(mBtSettingController);
    }

    @Test
    public void switchAudioDevice_HappyPath() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpAudioDeviceSwitchCompleteEvent(new CrpAudioDeviceSwitchCompleteEvent(ResponseCode.OK, BD_ADDRESS));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.SUCCESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));

    }

    @Test
    public void switchAudioDevice_AlreadyExecution() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 完了せずに進む
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();
        mBtSettingController.switchAudioDevice(deviceListItem);

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));
        verify(mPacketBuilder, times(1)).createAudioDeviceSwitchCommand(anyString());
        verify(mCarDeviceConnection, times(1)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));

    }

    @Test
    public void switchAudioDevice_SendRequestSessionStopped() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class))).thenReturn(null);

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(nullValue()));

    }

    @Test
    public void switchAudioDevice_ResponseResultFalse() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.FALSE);
                        // 応答がFALSEのためステータスが失敗状態であることの確認
                        assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));

    }

    @Test
    public void switchAudioDevice_ResponseError() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onError();
                        // 応答がErrorのためステータスが失敗状態であることの確認
                        assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));
    }

    @Test
    public void switchAudioDevice_CompleteWrongRequest() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpAudioDeviceSwitchCompleteEvent(new CrpAudioDeviceSwitchCompleteEvent(ResponseCode.OK, WRONG_BD_ADDRESS));
                            // 完了通知が期待したリクエストのものではないためステータスが変更されていないことの確認
                            assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));

    }

    @Test
    public void switchAudioDevice_CompleteSessionStopped() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            mSettingListInfoMap.audioDeviceSwitchStatus = null;
                            resultCallback.onCrpAudioDeviceSwitchCompleteEvent(new CrpAudioDeviceSwitchCompleteEvent(ResponseCode.OK, BD_ADDRESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));

    }

    @Test
    public void switchAudioDevice_CompleteNG() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpAudioDeviceSwitchCompleteEvent(new CrpAudioDeviceSwitchCompleteEvent(ResponseCode.NG, BD_ADDRESS));
                            // 完了がNGのためステータスが失敗状態であることの確認
                            assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));

    }

    @Test(expected = NullPointerException.class)
    public void switchAudioDevice_ArgNull() throws Exception {
        // exercise
        mBtSettingController.switchAudioDevice(null);

    }

    @Test
    public void sendPhoneServiceCommand_HappyPath() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.OK, BD_ADDRESS, PhoneConnectRequestType.CONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.SUCCESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SERVICE_CONNECT));

    }

    @Test
    public void sendPhoneServiceCommand_PhoneServiceDisabled() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        mPhoneSettingStatus.phoneServiceEnabled = false;

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        verify(mPacketBuilder, never()).createPhoneServiceConnectCommand(anyString(), any(PhoneConnectRequestType.class), any(EnumSet.class));
        verify(mCarDeviceConnection, never()).sendRequestPacket(any(OutgoingPacket.class), any(BtSettingControllerImpl.SendPhoneServiceCallback.class));

    }

    @Test
    public void sendPhoneServiceCommand_AlreadyExecution() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 完了せずに進む
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SERVICE_CONNECT));
        verify(mPacketBuilder, times(1)).createPhoneServiceConnectCommand(anyString(), any(PhoneConnectRequestType.class), any(EnumSet.class));
        verify(mCarDeviceConnection, times(1)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));

    }

    @Test
    public void sendPhoneServiceCommand_ConnectPhoneAndAudioConnect() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpAudioDeviceSwitchCompleteEvent(new CrpAudioDeviceSwitchCompleteEvent(ResponseCode.OK, BD_ADDRESS));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.SUCCESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE, ConnectServiceType.AUDIO), PhoneConnectRequestType.CONNECT);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));
    }

    @Test
    public void sendPhoneServiceCommand_PhoneAlreadyConnecting() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .phone1Connected(true)
                .build();

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        verify(mPacketBuilder, never()).createPhoneServiceConnectCommand(anyString(), any(PhoneConnectRequestType.class), any(EnumSet.class));
        verify(mCarDeviceConnection, never()).sendRequestPacket(any(OutgoingPacket.class), any(BtSettingControllerImpl.SendPhoneServiceCallback.class));
    }

    @Test
    public void sendPhoneServiceCommand_PhoneAlreadyDisconnecting() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .phone1Connected(false)
                .phone2Connected(false)
                .build();

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.DISCONNECT);

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        verify(mPacketBuilder, never()).createPhoneServiceConnectCommand(anyString(), any(PhoneConnectRequestType.class), any(EnumSet.class));
        verify(mCarDeviceConnection, never()).sendRequestPacket(any(OutgoingPacket.class), any(BtSettingControllerImpl.SendPhoneServiceCallback.class));
    }

    @Test
    public void sendPhoneServiceCommand_PhoneHfConnectLimit() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        mPhoneSettingStatus.hfDevicesCountStatus = ConnectedDevicesCountStatus.FULL;

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        verify(mPacketBuilder, never()).createPhoneServiceConnectCommand(anyString(), any(PhoneConnectRequestType.class), any(EnumSet.class));
        verify(mCarDeviceConnection, never()).sendRequestPacket(any(OutgoingPacket.class), any(BtSettingControllerImpl.SendPhoneServiceCallback.class));
    }

    @Test
    public void sendPhoneServiceCommand_AudioConnect() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpAudioDeviceSwitchCompleteEvent(new CrpAudioDeviceSwitchCompleteEvent(ResponseCode.OK, BD_ADDRESS));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.SUCCESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.AUDIO), PhoneConnectRequestType.CONNECT);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SWITCH_DEVICE));
    }

    @Test
    public void sendPhoneServiceCommand_SendRequestSessionStopped() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class))).thenReturn(null);

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        assertThat(mSettingListInfoMap.phoneServiceStatus, is(nullValue()));

    }

    @Test
    public void sendPhoneServiceCommand_ResponseResultFalse() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.FALSE);
                        // 応答がFALSEのためステータスが失敗状態であることの確認
                        assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SERVICE_CONNECT));

    }

    @Test
    public void sendPhoneServiceCommand_ResponseError() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onError();
                        // 応答がErrorのためステータスが失敗状態であることの確認
                        assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SERVICE_CONNECT));
    }

    @Test
    public void sendPhoneServiceCommand_CompleteWrongRequest() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.OK, WRONG_BD_ADDRESS, PhoneConnectRequestType.CONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            // 完了通知が期待したリクエストのものではないためステータスが変更されていないことの確認
                            assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SERVICE_CONNECT));

    }

    @Test
    public void sendPhoneServiceCommand_CompleteSessionStopped() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            mSettingListInfoMap.phoneServiceStatus = null;
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.OK, BD_ADDRESS, PhoneConnectRequestType.CONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SERVICE_CONNECT));

    }

    @Test
    public void sendPhoneServiceCommand_CompleteNG() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.NG, BD_ADDRESS, PhoneConnectRequestType.CONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            // 完了がNGのためステータスが失敗状態であることの確認
                            assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SERVICE_CONNECT));

    }

    @Test(expected = NullPointerException.class)
    public void sendPhoneServiceCommand_ArgDeviceNull() throws Exception {
        // exercise
        mBtSettingController.sendPhoneServiceCommand(null, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);

    }

    @Test(expected = NullPointerException.class)
    public void sendPhoneServiceCommand_ArgServiceTypeNull() throws Exception {
        // exercise
        mBtSettingController.sendPhoneServiceCommand(mock(DeviceListItem.class), null, PhoneConnectRequestType.CONNECT);

    }

    @Test(expected = NullPointerException.class)
    public void sendPhoneServiceCommand_ArgConnectTypeNull() throws Exception {
        // exercise
        mBtSettingController.sendPhoneServiceCommand(mock(DeviceListItem.class), EnumSet.of(ConnectServiceType.PHONE), null);

    }

    @Test
    public void startPhoneSearch_HappyPath() throws Exception {
        // setup
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneSearchCommandCompleteEvent(new CrpPhoneSearchCommandCompleteEvent(ResponseCode.OK));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.COMPLETED));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));

    }

    @Test
    public void startPhoneSearch_HappyPath_AudioDisconnect() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.DISCONNECT), eq(EnumSet.of(ConnectServiceType.AUDIO)))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.OK, BD_ADDRESS, PhoneConnectRequestType.CONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.SUCCESS));
                        });
                    });
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneSearchCommandCompleteEvent(new CrpPhoneSearchCommandCompleteEvent(ResponseCode.OK));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.COMPLETED));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(4)).post(any(Object.class));
    }

    @Test
    public void startPhoneSearch_InquiryDisabled() throws Exception {
        // setup
        mPhoneSettingStatus.inquiryEnabled = false;

        // exercise
        mBtSettingController.startPhoneSearch(null);

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        verify(mPacketBuilder, never()).createPhoneSearchCommand(any(PhoneSearchRequestType.class));
        verify(mCarDeviceConnection, never()).sendRequestPacket(any(OutgoingPacket.class), any(BtSettingControllerImpl.PhoneSearchCallback.class));
    }

    @Test
    public void startPhoneSearch_AlreadyExecution() throws Exception {
        // setup
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                        // 完了せずに進む
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();
        mBtSettingController.startPhoneSearch(null);

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));

    }

    @Test
    public void startPhoneSearch_AudioDisconnectFailed() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.DISCONNECT), eq(EnumSet.of(ConnectServiceType.AUDIO)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.FALSE);
                        // 応答がFALSEのためステータスが失敗状態であることの確認
                        assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                        mSignal.countDown();

                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(any(Object.class));
    }

    @Test
    public void startPhoneSearch_SendRequestSessionStopped() throws Exception {
        // setup
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class))).thenReturn(null);

        // exercise
        mBtSettingController.startPhoneSearch(null);

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.NONE));
    }

    @Test
    public void startPhoneSearch_ResponseResultFalse() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.FALSE);
                        // 応答がFALSEのためステータスが失敗状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.FAILED));
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));

    }

    @Test
    public void startPhoneSearch_ResponseError() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onError();
                        // 応答がErrorのためステータスが失敗状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.FAILED));
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));
    }

    @Test
    public void startPhoneSearch_CompleteNG() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneSearchCommandCompleteEvent(new CrpPhoneSearchCommandCompleteEvent(ResponseCode.NG));
                            // 完了がNGのためステータスが失敗状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.FAILED));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));
    }

    @Test
    public void stopPhoneSearch_HappyPath() throws Exception {
        // setup
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.STOP))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    if(mIsStopSearch){
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.STOP_COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが状態なしであることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.NONE));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneSearchCommandCompleteEvent(new CrpPhoneSearchCommandCompleteEvent(ResponseCode.OK));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.COMPLETED));
                            mSignal.countDown();
                        });
                    });
                    }else{
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.TRUE);
                            // 応答がTRUEのためステータスが処理中状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                            // 完了せずに進む
                            mSignal.countDown();
                        });
                    }
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();
        mIsStopSearch = true;

        mSignal = new CountDownLatch(1);
        mBtSettingController.stopPhoneSearch(false);
        mSignal.await();

        // verify
        verify(mEventBus, times(3)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));
    }

    @Test
    public void stopPhoneSearch_HappyPath_ReconnectA2dp() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.DISCONNECT), eq(EnumSet.of(ConnectServiceType.AUDIO)))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.STOP))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpAudioDeviceSwitchCompleteEvent(new CrpAudioDeviceSwitchCompleteEvent(ResponseCode.OK, BD_ADDRESS));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.audioDeviceSwitchStatus, is(SettingListInfoMap.CommandStatus.SUCCESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.OK, BD_ADDRESS, PhoneConnectRequestType.CONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.phoneServiceStatus, is(SettingListInfoMap.CommandStatus.SUCCESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    if(mIsStopSearch){
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.STOP_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.TRUE);
                            // 応答がTRUEのためステータスが状態なしであることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.NONE));

                            // 送信したリクエストの完了ハンドラ
                            mMainHandler.post(() -> {
                                resultCallback.onCrpPhoneSearchCommandCompleteEvent(new CrpPhoneSearchCommandCompleteEvent(ResponseCode.OK));
                                // 完了がOKのためステータスが成功状態であることの確認
                                assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.COMPLETED));
                                mSignal.countDown();
                            });
                        });
                    }else{
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.TRUE);
                            // 応答がTRUEのためステータスが処理中状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                            // 完了せずに進む
                            mSignal.countDown();
                        });
                    }
                    return mock(Future.class);
                });

        // exercise
        mSignal = new CountDownLatch(2);
        mBtSettingController.startPhoneSearch(deviceListItem);
        mSignal.await();
        mIsStopSearch = true;

        mSignal = new CountDownLatch(2);
        mBtSettingController.stopPhoneSearch(true);
        mSignal.await();

        // verify
        verify(mEventBus, times(7)).post(any(Object.class));
    }

    @Test
    public void stopPhoneSearch_InquiryDisabled() throws Exception {
        // setup
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                        // 完了せずに進む
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();
        mPhoneSettingStatus.inquiryEnabled = false;

        mBtSettingController.stopPhoneSearch(false);

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));
    }

    @Test
    public void stopPhoneSearch_CompletedSearchDevice() throws Exception {
        // exercise
        mBtSettingController.stopPhoneSearch(false);

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        verify(mPacketBuilder, never()).createPhoneSearchCommand(any(PhoneSearchRequestType.class));
        verify(mCarDeviceConnection, never()).sendRequestPacket(any(OutgoingPacket.class), any(BtSettingControllerImpl.PhoneSearchCallback.class));
    }

    @Test
    public void stopPhoneSearch_SendRequestSessionStopped() throws Exception {
        // setup
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.STOP))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                        // 完了せずに進む
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class))).thenReturn(null);

        mBtSettingController.stopPhoneSearch(false);

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));
    }

    @Test
    public void stopPhoneSearch_ResponseResultFalse() throws Exception {
        // setup
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.STOP))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    if(mIsStopSearch){
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.STOP_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.FALSE);
                            // 応答がFALSEのためステータスが失敗状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.FAILED));
                            mSignal.countDown();
                        });
                    }else{
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.TRUE);
                            // 応答がTRUEのためステータスが処理中状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                            // 完了せずに進む
                            mSignal.countDown();
                        });
                    }
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();
        mIsStopSearch = true;

        mSignal = new CountDownLatch(1);
        mBtSettingController.stopPhoneSearch(false);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));

    }

    @Test
    public void stopPhoneSearch_ResponseError() throws Exception {
        // setup
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.STOP))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    if(mIsStopSearch){
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.STOP_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onError();
                            // 応答がErrorのためステータスが失敗状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.FAILED));
                            mSignal.countDown();
                        });
                    }else{
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.TRUE);
                            // 応答がTRUEのためステータスが処理中状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                            // 完了せずに進む
                            mSignal.countDown();
                        });
                    }
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();
        mIsStopSearch = true;

        mSignal = new CountDownLatch(1);
        mBtSettingController.stopPhoneSearch(false);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));
    }

    @Test
    public void stopPhoneSearch_CompleteNG() throws Exception {
        // setup
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.STOP))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    if(mIsStopSearch){
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.STOP_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.TRUE);
                            // 応答がTRUEのためステータスが状態なしであることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.NONE));

                            // 送信したリクエストの完了ハンドラ
                            mMainHandler.post(() -> {
                                resultCallback.onCrpPhoneSearchCommandCompleteEvent(new CrpPhoneSearchCommandCompleteEvent(ResponseCode.NG));
                                // 完了がNGのためステータスが失敗状態であることの確認
                                assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.FAILED));
                                mSignal.countDown();
                            });
                        });
                    }else{
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.TRUE);
                            // 応答がTRUEのためステータスが処理中状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                            // 完了せずに進む
                            mSignal.countDown();
                        });
                    }
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();
        mIsStopSearch = true;

        mSignal = new CountDownLatch(1);
        mBtSettingController.stopPhoneSearch(false);
        mSignal.await();

        // verify
        verify(mEventBus, times(3)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));
    }

    @Test
    public void stopPhoneSearch_NotReconnectA2dp() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.DISCONNECT), eq(EnumSet.of(ConnectServiceType.AUDIO)))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.STOP))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {
                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.OK, BD_ADDRESS, PhoneConnectRequestType.CONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    if(mIsStopSearch){
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.STOP_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.TRUE);
                            // 応答がTRUEのためステータスが状態なしであることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.NONE));

                            // 送信したリクエストの完了ハンドラ
                            mMainHandler.post(() -> {
                                resultCallback.onCrpPhoneSearchCommandCompleteEvent(new CrpPhoneSearchCommandCompleteEvent(ResponseCode.OK));
                                // 完了がOKのためステータスが成功状態であることの確認
                                assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.COMPLETED));
                                mSignal.countDown();
                            });
                        });
                    }else{
                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.TRUE);

                            // 完了せずに進む
                            mSignal.countDown();
                        });
                    }
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(deviceListItem);
        mSignal.await();
        mIsStopSearch = true;

        mSignal = new CountDownLatch(2);
        mBtSettingController.stopPhoneSearch(true);
        mSignal.await();

        // verify
        verify(mEventBus, times(5)).post(any(Object.class));
    }

    @Test
    public void pairingDevice_HappyPath() throws Exception {
        // setup
        SearchListItem searchListItem = new SearchListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingAddCommand(eq(BD_ADDRESS), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PairingDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PairingDeviceCallback resultCallback = (BtSettingControllerImpl.PairingDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPairingAddCommandCompleteEvent(new CrpPairingAddCommandCompleteEvent(ResponseCode.OK, BD_ADDRESS));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.SUCCESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE));

    }

    @Test
    public void pairingDevice_HappyPath_SearchStop() throws Exception {
        // setup
        SearchListItem searchListItem = new SearchListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.START))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPhoneSearchCommand(eq(PhoneSearchRequestType.STOP))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createPairingAddCommand(eq(BD_ADDRESS), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PhoneSearchCallback.class)))
                .then(invocationOnMock -> {
                    if(mIsStopSearch){
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.STOP_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.TRUE);
                            // 応答がTRUEのためステータスが状態なしであることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.NONE));

                            // 送信したリクエストの完了ハンドラ
                            mMainHandler.post(() -> {
                                resultCallback.onCrpPhoneSearchCommandCompleteEvent(new CrpPhoneSearchCommandCompleteEvent(ResponseCode.OK));
                                // 完了がOKのためステータスが成功状態であることの確認
                                assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.COMPLETED));
                                mSignal.countDown();
                            });
                        });
                    }else{
                        // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                        assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.START_COMMAND_SENT));

                        // 送信したリクエストの応答ハンドラ
                        mMainHandler.post(() -> {
                            BtSettingControllerImpl.PhoneSearchCallback resultCallback = (BtSettingControllerImpl.PhoneSearchCallback) invocationOnMock.getArguments()[1];
                            resultCallback.onResult(Boolean.TRUE);
                            // 応答がTRUEのためステータスが処理中状態であることの確認
                            assertThat(mSettingListInfoMap.deviceSearchStatus, is(DeviceSearchStatus.SEARCHING));

                            // 完了せずに進む
                            mSignal.countDown();
                        });
                    }
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PairingDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PairingDeviceCallback resultCallback = (BtSettingControllerImpl.PairingDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPairingAddCommandCompleteEvent(new CrpPairingAddCommandCompleteEvent(ResponseCode.OK, BD_ADDRESS));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.SUCCESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.startPhoneSearch(null);
        mSignal.await();
        mIsStopSearch = true;
        mSignal = new CountDownLatch(2);

        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));
        mSignal.await();

        // verify
        verify(mEventBus, times(5)).post(mCaptor.capture());

    }

    @Test
    public void pairingDevice_PairingAddDisabled() throws Exception {
        // setup
        SearchListItem searchListItem = new SearchListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        mPhoneSettingStatus.pairingAddEnabled = false;

        // exercise
        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        verify(mPacketBuilder, never()).createPairingAddCommand(anyString(), any(EnumSet.class));
        verify(mCarDeviceConnection, never()).sendRequestPacket(any(OutgoingPacket.class), any(BtSettingControllerImpl.PairingDeviceCallback.class));
    }

    @Test
    public void pairingDevice_PairingDeviceCountFull() throws Exception {
        // setup
        SearchListItem searchListItem = new SearchListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        mPhoneSettingStatus.pairingDevicesCountStatus = ConnectedDevicesCountStatus.FULL;

        // exercise
        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        verify(mPacketBuilder, never()).createPairingAddCommand(anyString(), any(EnumSet.class));
        verify(mCarDeviceConnection, never()).sendRequestPacket(any(OutgoingPacket.class), any(BtSettingControllerImpl.PairingDeviceCallback.class));
    }

    @Test
    public void pairingDevice_AlreadyExecution() throws Exception {
        // setup
        SearchListItem searchListItem = new SearchListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingAddCommand(eq(BD_ADDRESS), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PairingDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PairingDeviceCallback resultCallback = (BtSettingControllerImpl.PairingDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 完了せず進む
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));
        mSignal.await();
        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE));
    }

    @Test
    public void pairingDevice_SendRequestSessionStopped() throws Exception {
        // setup
        SearchListItem searchListItem = new SearchListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingAddCommand(eq(BD_ADDRESS), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PairingDeviceCallback.class))).thenReturn(null);

        // exercise
        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        assertThat(mSettingListInfoMap.devicePairingStatus, is(nullValue()));
    }

    @Test
    public void pairingDevice_ResponseResultFalse() throws Exception {
        // setup
        SearchListItem searchListItem = new SearchListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingAddCommand(eq(BD_ADDRESS), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PairingDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PairingDeviceCallback resultCallback = (BtSettingControllerImpl.PairingDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.FALSE);
                        // 応答がFALSEのためステータスが失敗状態であることの確認
                        assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE));
    }

    @Test
    public void pairingDevice_ResponseError() throws Exception {
        // setup
        SearchListItem searchListItem = new SearchListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingAddCommand(eq(BD_ADDRESS), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PairingDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PairingDeviceCallback resultCallback = (BtSettingControllerImpl.PairingDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onError();
                        // 応答がErrorのためステータスが失敗状態であることの確認
                        assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE));
    }

    @Test
    public void pairingDevice_CompleteWrongRequest() throws Exception {
        // setup
        SearchListItem searchListItem = new SearchListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingAddCommand(eq(BD_ADDRESS), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PairingDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PairingDeviceCallback resultCallback = (BtSettingControllerImpl.PairingDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPairingAddCommandCompleteEvent(new CrpPairingAddCommandCompleteEvent(ResponseCode.OK, WRONG_BD_ADDRESS));
                            // 完了通知が期待したリクエストのものではないためステータスが変更されていないことの確認
                            assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE));
    }

    @Test
    public void pairingDevice_CompleteSessionStopped() throws Exception {
        // setup
        SearchListItem searchListItem = new SearchListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingAddCommand(eq(BD_ADDRESS), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PairingDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PairingDeviceCallback resultCallback = (BtSettingControllerImpl.PairingDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            mSettingListInfoMap.audioDeviceSwitchStatus = null;
                            resultCallback.onCrpPairingAddCommandCompleteEvent(new CrpPairingAddCommandCompleteEvent(ResponseCode.OK, WRONG_BD_ADDRESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE));
    }

    @Test
    public void pairingDevice_CompleteNG() throws Exception {
        // setup
        SearchListItem searchListItem = new SearchListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingAddCommand(eq(BD_ADDRESS), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.PairingDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.PairingDeviceCallback resultCallback = (BtSettingControllerImpl.PairingDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPairingAddCommandCompleteEvent(new CrpPairingAddCommandCompleteEvent(ResponseCode.NG, BD_ADDRESS));
                            // 完了がNGのためステータスが失敗状態であることの確認
                            assertThat(mSettingListInfoMap.devicePairingStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.pairingDevice(searchListItem, EnumSet.of(ConnectServiceType.PHONE));
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE));
    }

    @Test(expected = NullPointerException.class)
    public void pairingDevice_ArgDeviceNull() throws Exception {
        // exercise
        mBtSettingController.pairingDevice(null, EnumSet.of(ConnectServiceType.PHONE));
    }

    @Test(expected = NullPointerException.class)
    public void pairingDevice_ArgServiceTypeNull() throws Exception {
        // exercise
        mBtSettingController.pairingDevice(mock(SearchListItem.class), null);
    }

    @Test
    public void deleteDevice_HappyPath() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPairingDeleteCommandCompleteEvent(new CrpPairingDeleteCommandCompleteEvent(ResponseCode.OK, BD_ADDRESS));
                            // 完了がOKのためステータスが成功状態であることの確認
                            assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.SUCCESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.DELETE_DEVICE));
    }

    @Test
    public void deleteDevice_PairingClearDisabled() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        mPhoneSettingStatus.phoneServiceEnabled = false;

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        verify(mPacketBuilder, never()).createPhoneServiceConnectCommand(anyString(), any(PhoneConnectRequestType.class), any(EnumSet.class));
        verify(mCarDeviceConnection, never()).sendRequestPacket(any(OutgoingPacket.class), any(BtSettingControllerImpl.SendPhoneServiceCallback.class));
    }

    @Test
    public void deleteDevice_AlreadyExecution() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 完了せず進む
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();
        mBtSettingController.deleteDevice(deviceListItem);

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.DELETE_DEVICE));
    }

    @Test
    public void deleteDevice_SendRequestSessionStopped() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class))).thenReturn(null);

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);

        // verify
        verify(mEventBus, never()).post(any(Object.class));
        assertThat(mSettingListInfoMap.deviceDeleteStatus, is(nullValue()));
    }

    @Test
    public void deleteDevice_ResponseResultFalse() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.FALSE);
                        // 応答がFALSEのためステータスが失敗状態であることの確認
                        assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.DELETE_DEVICE));
    }

    @Test
    public void deleteDevice_ResponseError() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onError();
                        // 応答がErrorのためステータスが失敗状態であることの確認
                        assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.DELETE_DEVICE));
    }

    @Test
    public void deleteDevice_CompleteWrongRequest() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPairingDeleteCommandCompleteEvent(new CrpPairingDeleteCommandCompleteEvent(ResponseCode.OK, WRONG_BD_ADDRESS));
                            // 完了通知が期待したリクエストのものではないためステータスが変更されていないことの確認
                            assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.DELETE_DEVICE));
    }

    @Test
    public void deleteDevice_CompleteSessionStopped() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            mSettingListInfoMap.deviceDeleteStatus = null;
                            resultCallback.onCrpPairingDeleteCommandCompleteEvent(new CrpPairingDeleteCommandCompleteEvent(ResponseCode.NG, BD_ADDRESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.DELETE_DEVICE));
    }

    @Test
    public void deleteDevice_CompleteNG() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // リクエスト送信直後はステータスがコマンド送信状態であることの確認
                    assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.COMMAND_SENT));

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 応答がTRUEのためステータスが処理中状態であることの確認
                        assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.PROCESSING));

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPairingDeleteCommandCompleteEvent(new CrpPairingDeleteCommandCompleteEvent(ResponseCode.NG, BD_ADDRESS));
                            // 完了がNGのためステータスが失敗状態であることの確認
                            assertThat(mSettingListInfoMap.deviceDeleteStatus, is(SettingListInfoMap.CommandStatus.FAILED));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();

        // verify
        verify(mEventBus, times(2)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.DELETE_DEVICE));
    }

    @Test(expected = NullPointerException.class)
    public void deleteDevice_ArgNull() throws Exception {
        // exercise
        mBtSettingController.deleteDevice(null);
    }

    @Test
    public void getAudioConnectStatus_Connecting() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();
        DeviceList.AudioConnectStatus actual = mBtSettingController.getAudioConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.AudioConnectStatus.STATUS_CONNECTING));
        assertThat(mIsCalledCreateAudioDeviceSwitchRequest, is(true));
    }

    @Test
    public void getAudioConnectStatus_Connected() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpAudioDeviceSwitchCompleteEvent(new CrpAudioDeviceSwitchCompleteEvent(ResponseCode.OK, BD_ADDRESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();
        DeviceList.AudioConnectStatus actual = mBtSettingController.getAudioConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.AudioConnectStatus.STATUS_CONNECTED));
        assertThat(mIsCalledCreateAudioDeviceSwitchRequest, is(true));
    }

    @Test
    public void getAudioConnectStatus_Response_ConnectFailed() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.FALSE);
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();
        DeviceList.AudioConnectStatus actual = mBtSettingController.getAudioConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.AudioConnectStatus.STATUS_CONNECT_FAILED));
        assertThat(mIsCalledCreateAudioDeviceSwitchRequest, is(true));
    }

    @Test
    public void getAudioConnectStatus_Complete_ConnectFailed() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpAudioDeviceSwitchCompleteEvent(new CrpAudioDeviceSwitchCompleteEvent(ResponseCode.NG, BD_ADDRESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();
        DeviceList.AudioConnectStatus actual = mBtSettingController.getAudioConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.AudioConnectStatus.STATUS_CONNECT_FAILED));
        assertThat(mIsCalledCreateAudioDeviceSwitchRequest, is(true));
    }

    @Test
    public void getAudioConnectStatus_WrongAddress() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createAudioDeviceSwitchCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SwitchAudioDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SwitchAudioDeviceCallback resultCallback = (BtSettingControllerImpl.SwitchAudioDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpAudioDeviceSwitchCompleteEvent(new CrpAudioDeviceSwitchCompleteEvent(ResponseCode.OK, BD_ADDRESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.switchAudioDevice(deviceListItem);
        mSignal.await();
        DeviceList.AudioConnectStatus actual = mBtSettingController.getAudioConnectStatus(WRONG_BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.AudioConnectStatus.STATUS_DEFAULT));
        assertThat(mIsCalledCreateAudioDeviceSwitchRequest, is(true));
    }

    @Test
    public void getAudioConnectStatus_RequestNull() throws Exception {
        // exercise
        DeviceList.AudioConnectStatus actual = mBtSettingController.getAudioConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.AudioConnectStatus.STATUS_DEFAULT));
        assertThat(mIsCalledCreateAudioDeviceSwitchRequest, is(false));
    }

    @Test
    public void getPhoneConnectStatus_Connecting() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();
        DeviceList.PhoneConnectStatus actual = mBtSettingController.getPhoneConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.PhoneConnectStatus.STATUS_CONNECTING));
        assertThat(mIsCalledCreatePhoneServiceRequest, is(true));
    }

    @Test
    public void getPhoneConnectStatus_Connected() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.OK, BD_ADDRESS, PhoneConnectRequestType.CONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();
        DeviceList.PhoneConnectStatus actual = mBtSettingController.getPhoneConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.PhoneConnectStatus.STATUS_CONNECTED));
        assertThat(mIsCalledCreatePhoneServiceRequest, is(true));
    }

    @Test
    public void getPhoneConnectStatus_Response_ConnectFailed() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.FALSE);
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();
        DeviceList.PhoneConnectStatus actual = mBtSettingController.getPhoneConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.PhoneConnectStatus.STATUS_CONNECT_FAILED));
        assertThat(mIsCalledCreatePhoneServiceRequest, is(true));
    }

    @Test
    public void getPhoneConnectStatus_Complete_ConnectFailed() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.NG, BD_ADDRESS, PhoneConnectRequestType.CONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();
        DeviceList.PhoneConnectStatus actual = mBtSettingController.getPhoneConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.PhoneConnectStatus.STATUS_CONNECT_FAILED));
        assertThat(mIsCalledCreatePhoneServiceRequest, is(true));
    }

    @Test
    public void getPhoneConnectStatus_Disconnecting() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .phone1Connected(true)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.DISCONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.DISCONNECT);
        mSignal.await();
        DeviceList.PhoneConnectStatus actual = mBtSettingController.getPhoneConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.PhoneConnectStatus.STATUS_DISCONNECTING));
        assertThat(mIsCalledCreatePhoneServiceRequest, is(true));
    }

    @Test
    public void getPhoneConnectStatus_Disconnected() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .phone1Connected(true)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.DISCONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.OK, BD_ADDRESS, PhoneConnectRequestType.DISCONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.DISCONNECT);
        mSignal.await();
        DeviceList.PhoneConnectStatus actual = mBtSettingController.getPhoneConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.PhoneConnectStatus.STATUS_DISCONNECTED));
        assertThat(mIsCalledCreatePhoneServiceRequest, is(true));
    }

    @Test
    public void getPhoneConnectStatus_Response_DisconnectFailed() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .phone1Connected(true)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.DISCONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.FALSE);
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.DISCONNECT);
        mSignal.await();
        DeviceList.PhoneConnectStatus actual = mBtSettingController.getPhoneConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.PhoneConnectStatus.STATUS_DISCONNECT_FAILED));
        assertThat(mIsCalledCreatePhoneServiceRequest, is(true));
    }

    @Test
    public void getPhoneConnectStatus_Complete_DisconnectFailed() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .phone1Connected(true)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.DISCONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.NG, BD_ADDRESS, PhoneConnectRequestType.DISCONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.DISCONNECT);
        mSignal.await();
        DeviceList.PhoneConnectStatus actual = mBtSettingController.getPhoneConnectStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.PhoneConnectStatus.STATUS_DISCONNECT_FAILED));
        assertThat(mIsCalledCreatePhoneServiceRequest, is(true));
    }

    @Test
    public void getPhoneConnectStatus_WrongAddress() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPhoneServiceConnectCommand(eq(BD_ADDRESS), eq(PhoneConnectRequestType.CONNECT), eq(EnumSet.of(ConnectServiceType.PHONE)))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.SendPhoneServiceCallback.class)))
                .then(invocationOnMock -> {

                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.SendPhoneServiceCallback resultCallback = (BtSettingControllerImpl.SendPhoneServiceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPhoneServiceConnectCompleteEvent(new CrpPhoneServiceConnectCompleteEvent(ResponseCode.OK, BD_ADDRESS, PhoneConnectRequestType.CONNECT, EnumSet.of(ConnectServiceType.PHONE)));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.sendPhoneServiceCommand(deviceListItem, EnumSet.of(ConnectServiceType.PHONE), PhoneConnectRequestType.CONNECT);
        mSignal.await();
        DeviceList.PhoneConnectStatus actual = mBtSettingController.getPhoneConnectStatus(WRONG_BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.PhoneConnectStatus.STATUS_DEFAULT));
        assertThat(mIsCalledCreatePhoneServiceRequest, is(true));
    }

    @Test
    public void getPhoneConnectStatus_RequestNull() throws Exception {
        // exercise
        DeviceList.PhoneConnectStatus actual = mBtSettingController.getPhoneConnectStatus("ADDRESS");

        // verify
        assertThat(actual, is(DeviceList.PhoneConnectStatus.STATUS_DEFAULT));
        assertThat(mIsCalledCreatePhoneServiceRequest, is(false));
    }

    @Test
    public void getDeleteStatus_Deleting() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();
        DeviceList.DeleteStatus actual = mBtSettingController.getDeleteStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.DeleteStatus.STATUS_DELETING));
        assertThat(mIsCalledCreateDeviceDeleteRequest, is(true));
    }

    @Test
    public void getDeleteStatus_Deleted() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPairingDeleteCommandCompleteEvent(new CrpPairingDeleteCommandCompleteEvent(ResponseCode.OK, BD_ADDRESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();
        DeviceList.DeleteStatus actual = mBtSettingController.getDeleteStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.DeleteStatus.STATUS_DELETED));
        assertThat(mIsCalledCreateDeviceDeleteRequest, is(true));
    }

    @Test
    public void getDeleteStatus_Response_DeleteFailed() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.FALSE);
                        mSignal.countDown();
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();
        DeviceList.DeleteStatus actual = mBtSettingController.getDeleteStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.DeleteStatus.STATUS_DELETE_FAILED));
        assertThat(mIsCalledCreateDeviceDeleteRequest, is(true));
    }

    @Test
    public void getDeleteStatus_Complete_DeleteFailed() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPairingDeleteCommandCompleteEvent(new CrpPairingDeleteCommandCompleteEvent(ResponseCode.NG, BD_ADDRESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();
        DeviceList.DeleteStatus actual = mBtSettingController.getDeleteStatus(BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.DeleteStatus.STATUS_DELETE_FAILED));
        assertThat(mIsCalledCreateDeviceDeleteRequest, is(true));
    }

    @Test
    public void getDeleteStatus_WrongAddress() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress(BD_ADDRESS)
                .build();
        when(mPacketBuilder.createPairingDeleteCommand(eq(BD_ADDRESS))).thenReturn(mOutgoingPacket);
        when(mCarDeviceConnection.sendRequestPacket(same(mOutgoingPacket), any(BtSettingControllerImpl.DeleteDeviceCallback.class)))
                .then(invocationOnMock -> {
                    // 送信したリクエストの応答ハンドラ
                    mMainHandler.post(() -> {
                        BtSettingControllerImpl.DeleteDeviceCallback resultCallback = (BtSettingControllerImpl.DeleteDeviceCallback) invocationOnMock.getArguments()[1];
                        resultCallback.onResult(Boolean.TRUE);

                        // 送信したリクエストの完了ハンドラ
                        mMainHandler.post(() -> {
                            resultCallback.onCrpPairingDeleteCommandCompleteEvent(new CrpPairingDeleteCommandCompleteEvent(ResponseCode.NG, BD_ADDRESS));
                            mSignal.countDown();
                        });
                    });
                    return mock(Future.class);
                });

        // exercise
        mBtSettingController.deleteDevice(deviceListItem);
        mSignal.await();
        DeviceList.DeleteStatus actual = mBtSettingController.getDeleteStatus(WRONG_BD_ADDRESS);

        // verify
        assertThat(actual, is(DeviceList.DeleteStatus.STATUS_DEFAULT));
        assertThat(mIsCalledCreateDeviceDeleteRequest, is(true));
    }

    @Test
    public void getDeleteStatus_RequestNull() throws Exception {
        // exercise
        DeviceList.DeleteStatus actual = mBtSettingController.getDeleteStatus("ADDRESS");

        // verify
        assertThat(actual, is(DeviceList.DeleteStatus.STATUS_DEFAULT));
        assertThat(mIsCalledCreateDeviceDeleteRequest, is(false));
    }

    @Test
    public void onCrpSessionStartedEvent_HappyPath() throws Exception {
        // exercise
        mBtSettingController.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        // verify
        assertThat(mIsCalledCheckBluetoothAddress, is(true));
        assertThat(mIsCalledBluetoothAdapterIsNull, is(true));
        assertThat(mIsCalledBluetoothAdapterIsEnabled, is(true));
        assertThat(mIsCalledBluetoothDeviceGetBondState, is(true));
        assertThat(mIsCalledBluetoothDeviceCreateBond, is(true));
    }

    @Test
    public void onCrpSessionStartedEvent_IsAutoPairingDisabled() throws Exception {
        // setup
        when(mStatusHolder.isAutoPainingEnabled()).thenReturn(false);

        // exercise
        mBtSettingController.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        // verify
        assertThat(mIsCalledCheckBluetoothAddress, is(false));
        assertThat(mIsCalledBluetoothAdapterIsNull, is(false));
        assertThat(mIsCalledBluetoothAdapterIsEnabled, is(false));
        assertThat(mIsCalledBluetoothDeviceGetBondState, is(false));
        assertThat(mIsCalledBluetoothDeviceCreateBond, is(false));
    }

    @Test
    public void onCrpSessionStartedEvent_BdAddressNotFound() throws Exception {
        // setup
        mCarDeviceSpec.bdAddress = "address";

        // exercise
        mBtSettingController.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        // verify
        assertThat(mIsCalledCheckBluetoothAddress, is(true));
        assertThat(mIsCalledBluetoothAdapterIsNull, is(false));
        assertThat(mIsCalledBluetoothAdapterIsEnabled, is(false));
        assertThat(mIsCalledBluetoothDeviceGetBondState, is(false));
        assertThat(mIsCalledBluetoothDeviceCreateBond, is(false));
    }

    @Test
    public void onCrpSessionStartedEvent_DeviceNotSupportBluetooth() throws Exception {
        // setup
        mIsBluetoothAdapterNull = true;

        // exercise
        mBtSettingController.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        // verify
        assertThat(mIsCalledCheckBluetoothAddress, is(true));
        assertThat(mIsCalledBluetoothAdapterIsNull, is(true));
        assertThat(mIsCalledBluetoothAdapterIsEnabled, is(false));
        assertThat(mIsCalledBluetoothDeviceGetBondState, is(false));
        assertThat(mIsCalledBluetoothDeviceCreateBond, is(false));
    }

    @Test
    public void onCrpSessionStartedEvent_BluetoothDisabled() throws Exception {
        // setup
        mIsBluetoothAdapterEnabled = false;

        // exercise
        mBtSettingController.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        // verify
        assertThat(mIsCalledCheckBluetoothAddress, is(true));
        assertThat(mIsCalledBluetoothAdapterIsNull, is(true));
        assertThat(mIsCalledBluetoothAdapterIsEnabled, is(true));
        assertThat(mIsCalledBluetoothDeviceGetBondState, is(false));
        assertThat(mIsCalledBluetoothDeviceCreateBond, is(false));
    }

    @Test
    public void onCrpSessionStartedEvent_AlreadyPairing() throws Exception {
        // setup
        mBluetoothDeviceBondState = BluetoothDevice.BOND_BONDING;

        // exercise
        mBtSettingController.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        // verify
        assertThat(mIsCalledCheckBluetoothAddress, is(true));
        assertThat(mIsCalledBluetoothAdapterIsNull, is(true));
        assertThat(mIsCalledBluetoothAdapterIsEnabled, is(true));
        assertThat(mIsCalledBluetoothDeviceGetBondState, is(true));
        assertThat(mIsCalledBluetoothDeviceCreateBond, is(false));
    }

    @Test
    public void onCrpSessionStoppedEvent() throws Exception {
        // setup
        SettingListInfoMap settingListInfoMap = mock(SettingListInfoMap.class);
        when(mStatusHolder.getSettingListInfoMap()).thenReturn(settingListInfoMap);

        // exercise
        mBtSettingController.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());

        // verify
        verify(settingListInfoMap).resetStatus();
        verify(mEventBus, times(1)).post(mCaptor.capture());
        assertThat(mCaptor.getValue().statusType, is(SettingListCommandStatusChangeEvent.CommandStatusType.ALL));
    }

}