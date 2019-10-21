package jp.pioneer.carsync.infrastructure.component;

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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import jp.pioneer.carsync.domain.component.VoiceRecognitionRequester;
import jp.pioneer.carsync.domain.model.VoiceRecognitionRequestType;
import jp.pioneer.carsync.domain.model.VoiceRecognitionResponseType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.entity.VoiceRecognitionResponse;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/11/30.
 */
public class VoiceRecognitionRequesterImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks VoiceRecognitionRequesterImpl mVoiceRecognitionRequester;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock Handler mHandler;

    VoiceRecognitionRequester.Callback mCallback;
    OutgoingPacket mOutgoingPacket;
    VoiceRecognitionResponse mVoiceRecognitionResponse;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal;

    boolean mIsOnError;
    boolean mIsReturnNull;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mIsOnError = false;
        mIsReturnNull = false;
        mCallback = mock(VoiceRecognitionRequester.Callback.class);
        mOutgoingPacket = mock(OutgoingPacket.class);
        mVoiceRecognitionResponse = new VoiceRecognitionResponse(VoiceRecognitionResponseType.START, ResponseCode.OK);
        when(mPacketBuilder.createVoiceRecognitionCommand(VoiceRecognitionRequestType.START)).thenReturn(mOutgoingPacket);

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });

        when(mCarDeviceConnection.sendRequestPacket(mOutgoingPacket, mVoiceRecognitionRequester))
                .then(invocationOnMock -> {
                    RequestTask.Callback resultCallback = (RequestTask.Callback) invocationOnMock.getArguments()[1];
                    if(mIsOnError) {
                        resultCallback.onError();
                    } else {
                        resultCallback.onResult(mVoiceRecognitionResponse);
                    }

                    if(mIsReturnNull){
                        return null;
                    } else {
                        return mock(Future.class);
                    }
                });
    }

    @Test
    public void startRequest() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mVoiceRecognitionRequester.startRequest(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onSuccess();
    }

    @Test
    public void startRequest_ResponseNG() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mVoiceRecognitionResponse = new VoiceRecognitionResponse(VoiceRecognitionResponseType.START, ResponseCode.NG);

        // exercise
        mVoiceRecognitionRequester.startRequest(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onError();
    }

    @Test
    public void startRequest_ResponseFinish() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mVoiceRecognitionResponse = new VoiceRecognitionResponse(VoiceRecognitionResponseType.FINISH, ResponseCode.OK);

        // exercise
        mVoiceRecognitionRequester.startRequest(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onError();
    }

    @Test
    public void startRequest_onError() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mIsOnError = true;

        // exercise
        mVoiceRecognitionRequester.startRequest(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onError();
    }

    @Test
    public void startRequest_sendRequestError() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mIsReturnNull = true;

        // exercise
        mVoiceRecognitionRequester.startRequest(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onError();
    }

    @Test(expected = NullPointerException.class)
    public void startRequest_ArgNull() throws Exception {
        // exercise
        mVoiceRecognitionRequester.startRequest(null);
    }

    @Test
    public void finishRequest() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mVoiceRecognitionResponse = new VoiceRecognitionResponse(VoiceRecognitionResponseType.FINISH, ResponseCode.OK);
        when(mPacketBuilder.createVoiceRecognitionCommand(VoiceRecognitionRequestType.FINISH)).thenReturn(mOutgoingPacket);

        // exercise
        mVoiceRecognitionRequester.finishRequest(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onSuccess();
    }
}