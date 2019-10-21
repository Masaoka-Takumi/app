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

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.BluetoothHeadsetProvider;
import jp.pioneer.carsync.domain.component.VoiceRecognitionRequester;
import jp.pioneer.carsync.domain.model.ConnectedDevicesCountStatus;
import jp.pioneer.carsync.domain.model.PhoneSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/11.
 */
public class PrepareSpeechRecognizerTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PrepareSpeechRecognizer mPrepareSpeechRecognizer;
    @Mock StatusHolder mStatusHolder;
    @Mock BluetoothHeadsetProvider mBluetoothHeadsetProvider;
    @Mock Handler mHandler;
    @Mock VoiceRecognitionRequester mRequester;

    PrepareSpeechRecognizer.PrepareCallback mCallback;
    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mCallback = mock(PrepareSpeechRecognizer.PrepareCallback.class);

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });

        PhoneSettingStatus status = new PhoneSettingStatus();
        status.hfDevicesCountStatus = ConnectedDevicesCountStatus.FULL;
        when(mStatusHolder.getPhoneSettingStatus()).thenReturn(status);
    }

    @Test
    public void executeCompleteDeviceHeadset() throws Exception {
        // setup
        doAnswer(
                invocationOnMock -> {
                    BluetoothHeadsetProvider.Callback Callback = (BluetoothHeadsetProvider.Callback)invocationOnMock.getArguments()[0];
                    Callback.onComplete(BluetoothHeadsetProvider.HeadsetRequestType.CONNECT, BluetoothHeadsetProvider.Result.COMPLETE);
                    return null;
                }).when(mBluetoothHeadsetProvider).prepareBluetoothHeadset(any(BluetoothHeadsetProvider.Callback.class));
        doAnswer(
                invocationOnMock -> {
                    VoiceRecognitionRequester.Callback Callback = (VoiceRecognitionRequester.Callback)invocationOnMock.getArguments()[0];
                    Callback.onSuccess();
                    return null;
                }).when(mRequester).startRequest(mPrepareSpeechRecognizer);

        // exercise
        mPrepareSpeechRecognizer.execute(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onComplete(eq(PrepareSpeechRecognizer.Device.HEADSET),any(PrepareSpeechRecognizer.FinishBluetoothHeadset.class));
        verify(mBluetoothHeadsetProvider).prepareBluetoothHeadset(any(BluetoothHeadsetProvider.Callback.class));
        verify(mRequester).startRequest(any(VoiceRecognitionRequester.Callback.class));
    }

    @Test
    public void executeCompleteDevicePhone() throws Exception {
        // setup
        doAnswer(
                invocationOnMock -> {
                    BluetoothHeadsetProvider.Callback Callback = (BluetoothHeadsetProvider.Callback)invocationOnMock.getArguments()[0];
                    Callback.onComplete(BluetoothHeadsetProvider.HeadsetRequestType.CONNECT, BluetoothHeadsetProvider.Result.ERROR);
                    return null;
                }).when(mBluetoothHeadsetProvider).prepareBluetoothHeadset(any(BluetoothHeadsetProvider.Callback.class));
        doAnswer(
                invocationOnMock -> {
                    VoiceRecognitionRequester.Callback Callback = (VoiceRecognitionRequester.Callback)invocationOnMock.getArguments()[0];
                    Callback.onSuccess();
                    return null;
                }).when(mRequester).startRequest(any(VoiceRecognitionRequester.Callback.class));

        // exercise
        mPrepareSpeechRecognizer.execute(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onComplete(eq(PrepareSpeechRecognizer.Device.PHONE),any(PrepareSpeechRecognizer.FinishBluetoothHeadset.class));
        verify(mBluetoothHeadsetProvider).prepareBluetoothHeadset(any(BluetoothHeadsetProvider.Callback.class));
        verify(mRequester).startRequest(any(VoiceRecognitionRequester.Callback.class));
    }

    @Test
    public void executeRequestNG() throws Exception {
        // setup
        doAnswer(
                invocationOnMock -> {
                    VoiceRecognitionRequester.Callback Callback = (VoiceRecognitionRequester.Callback)invocationOnMock.getArguments()[0];
                    Callback.onError();
                    return null;
                }).when(mRequester).startRequest(any(VoiceRecognitionRequester.Callback.class));

        // exercise
        mPrepareSpeechRecognizer.execute(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onError();
    }

    @Test(expected = NullPointerException.class)
    public void executeCompleteArgNull() throws Exception {
        // exercise
        mPrepareSpeechRecognizer.execute(null);
    }

    @Test
    public void executeFinishBluetoothHeadset() throws Exception {

        // setup
        PrepareSpeechRecognizer.FinishCallback finishCallback = mock(PrepareSpeechRecognizer.FinishCallback.class);
        CountDownLatch mSignal = new CountDownLatch(1);
        BluetoothHeadsetProvider.Callback callback = mock(BluetoothHeadsetProvider.Callback.class);
        doAnswer(
                invocationOnMock -> {
                    BluetoothHeadsetProvider.Callback Callback = (BluetoothHeadsetProvider.Callback)invocationOnMock.getArguments()[0];
                    Callback.onComplete(BluetoothHeadsetProvider.HeadsetRequestType.CONNECT, BluetoothHeadsetProvider.Result.COMPLETE);
                    return null;
                }).when(mBluetoothHeadsetProvider).prepareBluetoothHeadset(any(BluetoothHeadsetProvider.Callback.class));
        doAnswer(
                invocationOnMock -> {
                    BluetoothHeadsetProvider.Callback Callback = (BluetoothHeadsetProvider.Callback)invocationOnMock.getArguments()[0];
                    Callback.onComplete(BluetoothHeadsetProvider.HeadsetRequestType.DISCONNECT, BluetoothHeadsetProvider.Result.COMPLETE);
                    return null;
                }).when(mBluetoothHeadsetProvider).finishBluetoothHeadset(any(BluetoothHeadsetProvider.Callback.class));
        doAnswer(
                invocationOnMock -> {
                    VoiceRecognitionRequester.Callback Callback = (VoiceRecognitionRequester.Callback)invocationOnMock.getArguments()[0];
                    Callback.onSuccess();
                    return null;
                }).when(mRequester).startRequest(mPrepareSpeechRecognizer);
        doAnswer(
                invocationOnMock -> {
                    VoiceRecognitionRequester.Callback Callback = (VoiceRecognitionRequester.Callback)invocationOnMock.getArguments()[0];
                    Callback.onSuccess();
                    mSignal.countDown();
                    return null;
                }).when(mRequester).finishRequest(any(VoiceRecognitionRequester.Callback.class));

        // exercise
        mPrepareSpeechRecognizer.execute(new PrepareSpeechRecognizer.PrepareCallback() {
            @Override
            public void onComplete(PrepareSpeechRecognizer.Device device, PrepareSpeechRecognizer.FinishBluetoothHeadset finishBluetoothHeadset) {
                finishBluetoothHeadset.execute(finishCallback);
            }

            @Override
            public void onError() {
                mSignal.countDown();
            }
        });
        mSignal.await();

        // verify
        verify(finishCallback).onComplete();
    }
}