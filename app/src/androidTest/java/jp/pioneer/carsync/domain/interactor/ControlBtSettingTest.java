package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.BtSettingController;
import jp.pioneer.carsync.domain.model.ConnectServiceType;
import jp.pioneer.carsync.domain.model.DeviceListItem;
import jp.pioneer.carsync.domain.model.PhoneConnectRequestType;
import jp.pioneer.carsync.domain.model.SearchListItem;
import jp.pioneer.carsync.domain.model.SettingListType;
import jp.pioneer.carsync.domain.repository.SettingListRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/13.
 */
public class ControlBtSettingTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ControlBtSetting mControlBtSetting;
    @Mock Handler mHandler;
    @Mock SettingListRepository mSettingListRepository;
    @Mock BtSettingController mBtSettingController;

    private static final String BD_ADDRESS = "00:11:22:33:44:55";
    private static final EnumSet<ConnectServiceType> SERVICE_TYPES = EnumSet.of(ConnectServiceType.AUDIO, ConnectServiceType.PHONE);

    DeviceListItem mDeviceListItem;
    SearchListItem mSearchListItem;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mDeviceListItem = new DeviceListItem.Builder()
                .audioConnected(false)
                .build();
        mSearchListItem = new SearchListItem.Builder()
                .build();

        when(mSettingListRepository.findByBdAddress(eq(BD_ADDRESS), eq(SettingListType.DEVICE_LIST))).thenReturn(mDeviceListItem);
        when(mSettingListRepository.findByBdAddress(eq(BD_ADDRESS), eq(SettingListType.SEARCH_LIST))).thenReturn(mSearchListItem);

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });
    }

    @Test
    public void switchAudioDevice() throws Exception {
        // exercise
        mControlBtSetting.switchAudioDevice(BD_ADDRESS);
        mSignal.await();

        // verify
        verify(mBtSettingController).switchAudioDevice(mDeviceListItem);
    }

    @Test
    public void switchAudioDevice_deviceNotFound() throws Exception {
        // setup
        when(mSettingListRepository.findByBdAddress(eq(BD_ADDRESS), eq(SettingListType.DEVICE_LIST))).thenReturn(null);

        // exercise
        mControlBtSetting.switchAudioDevice(BD_ADDRESS);
        mSignal.await();

        // verify
        verify(mBtSettingController, never()).switchAudioDevice(any(DeviceListItem.class));
    }

    @Test
    public void switchAudioDevice_deviceAlreadyConnected() throws Exception {
        // setup
        DeviceListItem deviceListItem = new DeviceListItem.Builder().audioConnected(true).build();
        when(mSettingListRepository.findByBdAddress(eq(BD_ADDRESS), eq(SettingListType.DEVICE_LIST))).thenReturn(deviceListItem);

        // exercise
        mControlBtSetting.switchAudioDevice(BD_ADDRESS);
        mSignal.await();

        // verify
        verify(mBtSettingController, never()).switchAudioDevice(any(DeviceListItem.class));
    }

    @Test
    public void connectPhoneServiceCommand() throws Exception {
        // exercise
        mControlBtSetting.connectPhoneServiceCommand(BD_ADDRESS, SERVICE_TYPES);
        mSignal.await();

        // verify
        verify(mBtSettingController).sendPhoneServiceCommand(mDeviceListItem, SERVICE_TYPES, PhoneConnectRequestType.CONNECT);
    }

    @Test
    public void connectPhoneServiceCommand_deviceNotFound() throws Exception {
        // setup
        when(mSettingListRepository.findByBdAddress(eq(BD_ADDRESS), eq(SettingListType.DEVICE_LIST))).thenReturn(null);

        // exercise
        mControlBtSetting.connectPhoneServiceCommand(BD_ADDRESS, SERVICE_TYPES);
        mSignal.await();

        // verify
        verify(mBtSettingController, never()).sendPhoneServiceCommand(any(DeviceListItem.class), any(EnumSet.class), any(PhoneConnectRequestType.class));
    }

    @Test
    public void disconnectPhoneServiceCommand() throws Exception {
        // exercise
        mControlBtSetting.disconnectPhoneServiceCommand(BD_ADDRESS, SERVICE_TYPES);
        mSignal.await();

        // verify
        verify(mBtSettingController).sendPhoneServiceCommand(mDeviceListItem, SERVICE_TYPES, PhoneConnectRequestType.DISCONNECT);
    }

    @Test
    public void disconnectPhoneServiceCommand_deviceNotFound() throws Exception {
        // setup
        when(mSettingListRepository.findByBdAddress(eq(BD_ADDRESS), eq(SettingListType.DEVICE_LIST))).thenReturn(null);

        // exercise
        mControlBtSetting.disconnectPhoneServiceCommand(BD_ADDRESS, SERVICE_TYPES);
        mSignal.await();

        // verify
        verify(mBtSettingController, never()).sendPhoneServiceCommand(any(DeviceListItem.class), any(EnumSet.class), any(PhoneConnectRequestType.class));
    }

    @Test
    public void pairingDevice() throws Exception {
        // setup
        when(mSettingListRepository.findByBdAddress(eq(BD_ADDRESS), eq(SettingListType.DEVICE_LIST))).thenReturn(null);

        // exercise
        mControlBtSetting.pairingDevice(BD_ADDRESS, SERVICE_TYPES);
        mSignal.await();

        // verify
        verify(mBtSettingController).pairingDevice(mSearchListItem, SERVICE_TYPES);
    }

    @Test
    public void pairingDevice_deviceNotFound() throws Exception {
        // setup
        when(mSettingListRepository.findByBdAddress(eq(BD_ADDRESS), eq(SettingListType.SEARCH_LIST))).thenReturn(null);
        when(mSettingListRepository.findByBdAddress(eq(BD_ADDRESS), eq(SettingListType.DEVICE_LIST))).thenReturn(null);

        // exercise
        mControlBtSetting.pairingDevice(BD_ADDRESS, SERVICE_TYPES);
        mSignal.await();

        // verify
        verify(mBtSettingController, never()).pairingDevice(any(SearchListItem.class), any(EnumSet.class));
    }

    @Test
    public void pairingDevice_targetDeviceAlreadyPairing() throws Exception {
        // setup
        when(mSettingListRepository.findByBdAddress(eq(BD_ADDRESS), eq(SettingListType.DEVICE_LIST))).thenReturn(mock(DeviceListItem.class));

        // exercise
        mControlBtSetting.pairingDevice(BD_ADDRESS, SERVICE_TYPES);

        // verify
        verify(mBtSettingController, never()).pairingDevice(any(SearchListItem.class), any(EnumSet.class));
    }

    @Test
    public void deleteDevice() throws Exception {
        // exercise
        mControlBtSetting.deleteDevice(BD_ADDRESS);
        mSignal.await();

        // verify
        verify(mBtSettingController).deleteDevice(mDeviceListItem);
    }

    @Test
    public void deleteDevice_deviceNotFound() throws Exception {
        // setup
        when(mSettingListRepository.findByBdAddress(eq(BD_ADDRESS), eq(SettingListType.DEVICE_LIST))).thenReturn(null);

        // exercise
        mControlBtSetting.deleteDevice(BD_ADDRESS);
        mSignal.await();

        // verify
        verify(mBtSettingController, never()).deleteDevice(any(DeviceListItem.class));
    }

    @Test
    public void startPhoneSearch() throws Exception {
        // setup
        when(mSettingListRepository.getAudioConnectedDevice()).thenReturn(mDeviceListItem);

        // exercise
        mControlBtSetting.startPhoneSearch();
        mSignal.await();

        // verify
        verify(mBtSettingController).startPhoneSearch(mDeviceListItem);
    }

    @Test
    public void startPhoneSearch_notConnected() throws Exception {
        // setup
        when(mSettingListRepository.getAudioConnectedDevice()).thenReturn(null);

        // exercise
        mControlBtSetting.startPhoneSearch();
        mSignal.await();

        // verify
        verify(mBtSettingController).startPhoneSearch(null);
    }

    @Test
    public void stopPhoneSearch() throws Exception {
        // exercise
        mControlBtSetting.stopPhoneSearch();
        mSignal.await();

        // verify
        verify(mBtSettingController).stopPhoneSearch(false);
    }

    @Test
    public void stopPhoneSearchAndReconnectA2dp() throws Exception {
        // exercise
        mControlBtSetting.stopPhoneSearchAndReconnectA2dp();
        mSignal.await();

        // verify
        verify(mBtSettingController).stopPhoneSearch(true);
    }

}