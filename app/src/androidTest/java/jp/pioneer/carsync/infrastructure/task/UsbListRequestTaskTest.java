package jp.pioneer.carsync.infrastructure.task;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.Future;

import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/25.
 */
public class UsbListRequestTaskTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks UsbListRequestTask mUsbListRequestTask = new UsbListRequestTask(){
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
    @Mock OutgoingPacket mListInfoPacket;
    @Mock OutgoingPacket mItemInfoPacket;
    private UsbListRequestTask.Callback mCallback = mock(UsbListRequestTask.Callback.class);
    private ListInfo mListInfo = new ListInfo();

    boolean isInterrupted = false;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mStatusHolder.getListInfo()).thenReturn(mListInfo);
    }

    @Test
    public void run_ListInfo_HappyPath() throws Exception {
        // setup
        when(mPacketBuilder.createInitialListInfoRequest(MediaSourceType.USB, ListType.LIST)).thenReturn(mListInfoPacket);
        when(mCarDeviceConnection.sendRequestPacket(mListInfoPacket, mUsbListRequestTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.TRUE);
                    return mock(Future.class);
                });

        // exercise
        mUsbListRequestTask = mUsbListRequestTask.setParams(mCallback, UsbListRequestTask.RequestType.LIST_INFO);
        mUsbListRequestTask.run();

        // verify
        verify(mCallback).onFinish(UsbListRequestTask.RequestType.LIST_INFO);
    }

    @Test
    public void run_Info_Interrupted() throws Exception {
        // setup
        isInterrupted = true;

        // exercise
        mUsbListRequestTask = mUsbListRequestTask.setParams(mCallback, UsbListRequestTask.RequestType.LIST_INFO);
        mUsbListRequestTask.run();

        // verify
        verify(mCallback, never()).onFinish(any(UsbListRequestTask.RequestType.class));
    }

    @Test
    public void run_ListInfo_RequestError() throws Exception {
        // setup
        when(mPacketBuilder.createInitialListInfoRequest(MediaSourceType.USB, ListType.LIST)).thenReturn(mListInfoPacket);
        when(mCarDeviceConnection.sendRequestPacket(mListInfoPacket, mUsbListRequestTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onError();
                    return mock(Future.class);
                });

        // exercise
        mUsbListRequestTask = mUsbListRequestTask.setParams(mCallback, UsbListRequestTask.RequestType.LIST_INFO);
        mUsbListRequestTask.run();

        // verify
        verify(mCallback, never()).onFinish(any(UsbListRequestTask.RequestType.class));
    }

    @Test
    public void run_ListInfo_ResultNG() throws Exception {
        // setup
        when(mPacketBuilder.createInitialListInfoRequest(MediaSourceType.USB, ListType.LIST)).thenReturn(mListInfoPacket);
        when(mCarDeviceConnection.sendRequestPacket(mListInfoPacket, mUsbListRequestTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.FALSE);
                    return mock(Future.class);
                });

        // exercise
        mUsbListRequestTask = mUsbListRequestTask.setParams(mCallback, UsbListRequestTask.RequestType.LIST_INFO);
        mUsbListRequestTask.run();

        // verify
        verify(mCallback, never()).onFinish(any(UsbListRequestTask.RequestType.class));
    }

    @Test
    public void run_ItemInfo_HappyPath() throws Exception {
        // setup
        when(mPacketBuilder.createListInfoRequest(1, MediaSourceType.USB, ListType.LIST, 5, 1)).thenReturn(mItemInfoPacket);
        mListInfo.transactionInfo.setInitialInfo(MediaSourceType.USB, ListType.LIST, 100, 10, 5, "TEST");
        mListInfo.transactionInfo.listIndex = 5;
        mListInfo.transactionInfo.id++;
        when(mCarDeviceConnection.sendRequestPacket(mItemInfoPacket, mUsbListRequestTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.TRUE);
                    return mock(Future.class);
                });

        // exercise
        mUsbListRequestTask = mUsbListRequestTask.setParams(mCallback, UsbListRequestTask.RequestType.ITEM_INFO);
        mUsbListRequestTask.run();

        // verify
        verify(mCallback).onFinish(UsbListRequestTask.RequestType.ITEM_INFO);
    }

    @Test
    public void run_ItemInfo_Interrupted() throws Exception {
        // setup
        isInterrupted = true;

        // exercise
        mUsbListRequestTask = mUsbListRequestTask.setParams(mCallback, UsbListRequestTask.RequestType.ITEM_INFO);
        mUsbListRequestTask.run();

        // verify
        verify(mCallback, never()).onFinish(any(UsbListRequestTask.RequestType.class));
    }

    @Test
    public void run_ItemInfo_RequestError() throws Exception {
        // setup
        when(mPacketBuilder.createListInfoRequest(1, MediaSourceType.USB, ListType.LIST, 5, 1)).thenReturn(mItemInfoPacket);
        mListInfo.transactionInfo.setInitialInfo(MediaSourceType.USB, ListType.LIST, 100, 10, 5, "TEST");
        mListInfo.transactionInfo.listIndex = 5;
        mListInfo.transactionInfo.id++;
        when(mCarDeviceConnection.sendRequestPacket(mItemInfoPacket, mUsbListRequestTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onError();
                    return mock(Future.class);
                });

        // exercise
        mUsbListRequestTask = mUsbListRequestTask.setParams(mCallback, UsbListRequestTask.RequestType.ITEM_INFO);
        mUsbListRequestTask.run();

        // verify
        verify(mCallback, never()).onFinish(any(UsbListRequestTask.RequestType.class));
    }

    @Test
    public void run_ItemInfo_ResultNG() throws Exception {
        // setup
        when(mPacketBuilder.createListInfoRequest(1, MediaSourceType.USB, ListType.LIST, 5, 1)).thenReturn(mItemInfoPacket);
        mListInfo.transactionInfo.setInitialInfo(MediaSourceType.USB, ListType.LIST, 100, 10, 5, "TEST");
        mListInfo.transactionInfo.listIndex = 5;
        mListInfo.transactionInfo.id++;
        when(mCarDeviceConnection.sendRequestPacket(mItemInfoPacket, mUsbListRequestTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.FALSE);
                    return mock(Future.class);
                });

        // exercise
        mUsbListRequestTask = mUsbListRequestTask.setParams(mCallback, UsbListRequestTask.RequestType.ITEM_INFO);
        mUsbListRequestTask.run();

        // verify
        verify(mCallback, never()).onFinish(any(UsbListRequestTask.RequestType.class));
    }

    @Test(expected = NullPointerException.class)
    public void setParams_ArgCallbackNull() throws Exception {
        // exercise
        mUsbListRequestTask = mUsbListRequestTask.setParams(null, UsbListRequestTask.RequestType.LIST_INFO);
    }

    @Test(expected = NullPointerException.class)
    public void setParams_ArgRequestTypeNull() throws Exception {
        // exercise
        mUsbListRequestTask = mUsbListRequestTask.setParams(mCallback, null);
    }
}