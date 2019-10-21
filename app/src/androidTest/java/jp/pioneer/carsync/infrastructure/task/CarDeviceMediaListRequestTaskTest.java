package jp.pioneer.carsync.infrastructure.task;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.Future;

import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.model.ListType.PCH_LIST;
import static jp.pioneer.carsync.domain.model.MediaSourceType.RADIO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/31.
 */
public class CarDeviceMediaListRequestTaskTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks CarDeviceMediaListRequestTask mCarDeviceMediaListRequestTask = new CarDeviceMediaListRequestTask(){
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
    ListInfo mListInfo = new ListInfo();
    @Mock OutgoingPacket mInitialPacket;
    @Mock OutgoingPacket mListInfoPacket1;
    @Mock OutgoingPacket mListInfoPacket2;
    @Mock OutgoingPacket mListInfoPacket3;

    boolean isInterrupted = false;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mListInfo.transactionInfo.reset();
        when(mStatusHolder.getListInfo()).thenReturn(mListInfo);

        when(mPacketBuilder.createInitialListInfoRequest(RADIO, PCH_LIST)).thenReturn(mInitialPacket);
        when(mPacketBuilder.createListInfoRequest(1, RADIO, PCH_LIST, 1, 10)).thenReturn(mListInfoPacket1);
        when(mPacketBuilder.createListInfoRequest(2, RADIO, PCH_LIST, 11, 10)).thenReturn(mListInfoPacket2);
        when(mPacketBuilder.createListInfoRequest(3, RADIO, PCH_LIST, 21, 10)).thenReturn(mListInfoPacket3);

        when(mCarDeviceConnection.sendRequestPacket(same(mInitialPacket), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    mListInfo.transactionInfo.setInitialInfo(RADIO, PCH_LIST, 21, 10, 1, "TEST");
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.TRUE);
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket1), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.TRUE);
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket2), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.TRUE);
                    return mock(Future.class);
                });
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket3), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.TRUE);
                    return mock(Future.class);
                });
    }

    @Test
    public void run_HappyPath() throws Exception {
        // setup
        CarDeviceMediaListRequestTask.Callback callback = mock(CarDeviceMediaListRequestTask.Callback.class);

        // exercise
        mCarDeviceMediaListRequestTask = mCarDeviceMediaListRequestTask.setParams(RADIO, PCH_LIST, callback);
        mCarDeviceMediaListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(4)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(callback).onFinish();
    }

    @Test
    public void run_InitialListInfo_ResultFalse() throws Exception {
        // setup
        CarDeviceMediaListRequestTask.Callback callback = mock(CarDeviceMediaListRequestTask.Callback.class);
        when(mCarDeviceConnection.sendRequestPacket(same(mInitialPacket), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    mListInfo.transactionInfo.setInitialInfo(RADIO, PCH_LIST, 21, 10, 1,"TEST");
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.FALSE);
                    return null;
                });

        // exercise
        mCarDeviceMediaListRequestTask = mCarDeviceMediaListRequestTask.setParams(RADIO, PCH_LIST, callback);
        mCarDeviceMediaListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(1)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(callback, never()).onFinish();
    }

    @Test
    public void run_InitialListInfo_Error() throws Exception {
        // setup
        CarDeviceMediaListRequestTask.Callback callback = mock(CarDeviceMediaListRequestTask.Callback.class);
        when(mCarDeviceConnection.sendRequestPacket(same(mInitialPacket), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    mListInfo.transactionInfo.setInitialInfo(RADIO, PCH_LIST, 21, 10, 1,"TEST");
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onError();
                    return mock(Future.class);
                });

        // exercise
        mCarDeviceMediaListRequestTask = mCarDeviceMediaListRequestTask.setParams(RADIO, PCH_LIST, callback);
        mCarDeviceMediaListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(1)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(callback, never()).onFinish();
    }

    @Test
    public void run_istInfo_ResultFalse() throws Exception {
        // setup
        CarDeviceMediaListRequestTask.Callback callback = mock(CarDeviceMediaListRequestTask.Callback.class);
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket2), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(Boolean.FALSE);
                    return mock(Future.class);
                });

        // exercise
        mCarDeviceMediaListRequestTask = mCarDeviceMediaListRequestTask.setParams(RADIO, PCH_LIST, callback);
        mCarDeviceMediaListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(3)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(callback, never()).onFinish();
    }

    @Test
    public void run_ListInfo_Error() throws Exception {
        // setup
        CarDeviceMediaListRequestTask.Callback callback = mock(CarDeviceMediaListRequestTask.Callback.class);
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket2), any(RequestTask.Callback.class)))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    resultCallback.onError();
                    return mock(Future.class);
                });

        // exercise
        mCarDeviceMediaListRequestTask = mCarDeviceMediaListRequestTask.setParams(RADIO, PCH_LIST, callback);
        mCarDeviceMediaListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(3)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(callback, never()).onFinish();
    }

    @Test
    public void run_isInterrupted() throws Exception {
        // setup
        CarDeviceMediaListRequestTask.Callback callback = mock(CarDeviceMediaListRequestTask.Callback.class);
        isInterrupted = true;

        // exercise
        mCarDeviceMediaListRequestTask = mCarDeviceMediaListRequestTask.setParams(RADIO, PCH_LIST, callback);
        mCarDeviceMediaListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, never()).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(callback, never()).onFinish();
    }

    @Test
    public void run_InitialListInfoRequest_FailedSendRequestPacket() throws Exception {
        // setup
        CarDeviceMediaListRequestTask.Callback callback = mock(CarDeviceMediaListRequestTask.Callback.class);
        when(mCarDeviceConnection.sendRequestPacket(same(mInitialPacket), any(RequestTask.Callback.class)))
                .thenReturn(null);

        // exercise
        mCarDeviceMediaListRequestTask = mCarDeviceMediaListRequestTask.setParams(RADIO, PCH_LIST, callback);
        mCarDeviceMediaListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(1)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(callback, never()).onFinish();
    }

    @Test
    public void run_ListInfoRequest_FailedSendRequestPacket() throws Exception {
        // setup
        CarDeviceMediaListRequestTask.Callback callback = mock(CarDeviceMediaListRequestTask.Callback.class);
        when(mCarDeviceConnection.sendRequestPacket(same(mListInfoPacket2), any(RequestTask.Callback.class)))
                .thenReturn(null);

        // exercise
        mCarDeviceMediaListRequestTask = mCarDeviceMediaListRequestTask.setParams(RADIO, PCH_LIST, callback);
        mCarDeviceMediaListRequestTask.run();

        // verify
        verify(mCarDeviceConnection, times(3)).sendRequestPacket(any(OutgoingPacket.class), any(RequestTask.Callback.class));
        verify(callback, never()).onFinish();
    }
}
