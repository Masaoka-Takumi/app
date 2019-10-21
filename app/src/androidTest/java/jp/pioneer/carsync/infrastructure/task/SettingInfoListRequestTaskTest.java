package jp.pioneer.carsync.infrastructure.task;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.Future;

import jp.pioneer.carsync.domain.model.PhoneSettingStatus;
import jp.pioneer.carsync.domain.model.SettingListInfo;
import jp.pioneer.carsync.domain.model.SettingListInfoMap;
import jp.pioneer.carsync.domain.model.SettingListItem;
import jp.pioneer.carsync.domain.model.SettingListType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;
import jp.pioneer.carsync.infrastructure.repository.SettingListRepositoryImpl;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/21.
 */
public class SettingInfoListRequestTaskTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SettingInfoListRequestTask mSettingInfoListRequestTask = new SettingInfoListRequestTask(){
        @Override
        void checkInterrupted() throws InterruptedException {
            if(isInterrupted){
                throw new InterruptedException();
            }
        }
    };
    @Mock StatusHolder mStatusHolder;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mSettingStatusPacket;
    @Mock OutgoingPacket mInitialPacket;
    @Mock OutgoingPacket mListInfoPacket1;
    @Mock OutgoingPacket mListInfoPacket2;
    @Mock OutgoingPacket mListInfoPacket3;
    @Mock SettingListItem mSettingListItem;
    @Mock SettingInfoListRequestTask.Callback mCallback;

    SettingListInfoMap mSettingListInfoMap = null;
    boolean isInterrupted = false;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mSettingListInfoMap = new SettingListInfoMap();

        when(mStatusHolder.getSettingListInfoMap()).thenReturn(mSettingListInfoMap);
        when(mStatusHolder.isSettingListEnabled(SettingListType.DEVICE_LIST)).thenReturn(true);

        when(mPacketBuilder.createPhoneSettingStatusRequest()).thenReturn(mSettingStatusPacket);
        when(mPacketBuilder.createInitialSettingListInfoRequest(SettingListType.DEVICE_LIST)).thenReturn(mInitialPacket);
        when(mPacketBuilder.createSettingListInfoRequest(anyInt(), eq(SettingListType.DEVICE_LIST), eq(1))).thenReturn(mListInfoPacket1);
        when(mPacketBuilder.createSettingListInfoRequest(anyInt(), eq(SettingListType.DEVICE_LIST), eq(2))).thenReturn(mListInfoPacket2);
        when(mPacketBuilder.createSettingListInfoRequest(anyInt(), eq(SettingListType.DEVICE_LIST), eq(3))).thenReturn(mListInfoPacket3);

        when(mCarDeviceConnection.sendRequestPacket(same(mSettingStatusPacket), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.TRUE);
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mInitialPacket), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    mSettingListInfoMap.getTransaction(SettingListType.DEVICE_LIST).setInitialInfo(SettingListType.DEVICE_LIST, 3);
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.TRUE);
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket1), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    mSettingListInfoMap.getTransaction(SettingListType.DEVICE_LIST).items.put(1, mSettingListItem);
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.TRUE);
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket2), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    mSettingListInfoMap.getTransaction(SettingListType.DEVICE_LIST).items.put(2, mSettingListItem);
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.TRUE);
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket3), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    mSettingListInfoMap.getTransaction(SettingListType.DEVICE_LIST).items.put(3, mSettingListItem);
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.TRUE);
                    return mock(Future.class);
                });
    }

    @Test
    public void run_HappyPath() throws Exception {
        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(5)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback).onGetItem(SettingListType.DEVICE_LIST, mSettingListItem, 1);
        verify(mCallback).onGetItem(SettingListType.DEVICE_LIST, mSettingListItem, 2);
        verify(mCallback).onGetItem(SettingListType.DEVICE_LIST, mSettingListItem, 3);
    }

    @Test
    public void run_IsInterrupted() throws Exception {
        // setup
        isInterrupted = true;

        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, never()).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback, never()).onGetItem(any(SettingListType.class), any(SettingListItem.class), anyInt());
    }

    @Test
    public void run_PhoneSettingStatusRequestResultFalse() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(same(mSettingStatusPacket), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.FALSE);
                    return mock(Future.class);
                });

        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(1)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback, never()).onGetItem(any(SettingListType.class), any(SettingListItem.class), anyInt());
    }

    @Test
    public void run_PhoneSettingStatusRequestError() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(same(mSettingStatusPacket), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onError();
                    return mock(Future.class);
                });

        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(1)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback, never()).onGetItem(any(SettingListType.class), any(SettingListItem.class), anyInt());
    }

    @Test
    public void run_SettingListDisabled() throws Exception {
        // setup
        when(mStatusHolder.isSettingListEnabled(SettingListType.DEVICE_LIST)).thenReturn(false);

        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(1)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback, never()).onGetItem(any(SettingListType.class), any(SettingListItem.class), anyInt());
    }

    @Test
    public void run_InitialSettingListRequestResultFalse() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(same(mInitialPacket), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    mSettingListInfoMap.getTransaction(SettingListType.DEVICE_LIST).setInitialInfo(SettingListType.DEVICE_LIST, 3);
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.FALSE);
                    return mock(Future.class);
                });

        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(2)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback, never()).onGetItem(any(SettingListType.class), any(SettingListItem.class), anyInt());
    }

    @Test
    public void run_InitialSettingListRequestError() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(same(mInitialPacket), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    mSettingListInfoMap.getTransaction(SettingListType.DEVICE_LIST).setInitialInfo(SettingListType.DEVICE_LIST, 3);
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onError();
                    return mock(Future.class);
                });

        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(2)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback, never()).onGetItem(any(SettingListType.class), any(SettingListItem.class), anyInt());
    }

    @Test
    public void run_SettingListRequestResultFalse() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket2), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    mSettingListInfoMap.getTransaction(SettingListType.DEVICE_LIST).items.put(1, mSettingListItem);
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.FALSE);
                    return mock(Future.class);
                });

        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(4)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback, times(1)).onGetItem(any(SettingListType.class), any(SettingListItem.class), anyInt());
        verify(mCallback).onGetItem(SettingListType.DEVICE_LIST, mSettingListItem, 1);
    }

    @Test
    public void run_SettingListRequestError() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket2), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    mSettingListInfoMap.getTransaction(SettingListType.DEVICE_LIST).items.put(1, mSettingListItem);
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onError();
                    return mock(Future.class);
                });

        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(4)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback, times(1)).onGetItem(any(SettingListType.class), any(SettingListItem.class), anyInt());
        verify(mCallback).onGetItem(SettingListType.DEVICE_LIST, mSettingListItem, 1);
    }

    @Test
    public void run_PhoneSettingStatusRequest_FailedSendPacket() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(same(mSettingStatusPacket), any(RequestTask.Callback.class))).thenReturn(null);

        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(1)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback, never()).onGetItem(any(SettingListType.class), any(SettingListItem.class), anyInt());
    }

    @Test
    public void run_InitialSettingListRequest_FailedSendPacket() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(same(mInitialPacket), any(RequestTask.Callback.class))).thenReturn(null);

        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(2)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback, never()).onGetItem(any(SettingListType.class), any(SettingListItem.class), anyInt());
    }

    @Test
    public void run_SettingListRequest_FailedSendPacket() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket2), any(RequestTask.Callback.class))).thenReturn(null);

        // exercise
        mSettingInfoListRequestTask = mSettingInfoListRequestTask.setParams(SettingListType.DEVICE_LIST, mCallback);
        mSettingInfoListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(4)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(mCallback, times(1)).onGetItem(any(SettingListType.class), any(SettingListItem.class), anyInt());
        verify(mCallback).onGetItem(SettingListType.DEVICE_LIST, mSettingListItem, 1);
    }

}