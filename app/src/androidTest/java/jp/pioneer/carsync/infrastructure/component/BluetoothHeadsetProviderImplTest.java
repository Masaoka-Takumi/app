package jp.pioneer.carsync.infrastructure.component;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.test.espresso.core.deps.guava.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.BluetoothHeadsetProvider;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/12.
 */
public class BluetoothHeadsetProviderImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks BluetoothHeadsetProviderImpl mBluetoothHeadsetProvider = new BluetoothHeadsetProviderImpl(){
        @Override
        BluetoothAdapterHolder createBluetoothAdapterHolder(BluetoothAdapter bluetoothAdapter) {
            return mBluetoothAdapterHolder;
        }

        @Override
        void removeCallbacks() {
        }
    };
    @Mock Context mContext;
    @Mock Handler mHandler;
    @Mock AudioManager mAudioManager;

    BluetoothHeadsetProviderImpl.BluetoothAdapterHolder mBluetoothAdapterHolder = new BluetoothHeadsetProviderImpl.BluetoothAdapterHolder(null){
        @Override
        boolean isEnabled() {
            return mIsEnabledAdapter;
        }

        @Override
        boolean isNull() {
            return mIsNullAdapter;
        }
    };

    BluetoothHeadsetProvider.Callback mCallback;
    BroadcastReceiver mBroadcastReceiver;
    Intent mBroadcastReceiverIntent;
    boolean mIsEnabledAdapter;
    boolean mIsNullAdapter;
    boolean mIsCallOnReceive;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });

        doAnswer(
                invocationOnMock -> {
                    if(mIsCallOnReceive) {
                        mMainHandler.post(() -> mBroadcastReceiver.onReceive(mock(Context.class), mBroadcastReceiverIntent));
                    }
                    return null;
                }).when(mAudioManager).startBluetoothSco();

        doAnswer(
                invocationOnMock -> {
                    if(mIsCallOnReceive) {
                        mMainHandler.post(() -> mBroadcastReceiver.onReceive(mock(Context.class), mBroadcastReceiverIntent));
                    }
                    return null;
                }).when(mAudioManager).stopBluetoothSco();

        mCallback = mock(BluetoothHeadsetProvider.Callback.class);
        mBroadcastReceiver = mBluetoothHeadsetProvider;
        mBroadcastReceiverIntent = mock(Intent.class);
        mIsEnabledAdapter = true;
        mIsNullAdapter = false;
        mIsCallOnReceive = true;

        when(mBroadcastReceiverIntent.getAction()).thenReturn(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        when(mBroadcastReceiverIntent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE,AudioManager.SCO_AUDIO_STATE_ERROR)).thenReturn(AudioManager.SCO_AUDIO_STATE_CONNECTED);
        when(mAudioManager.isBluetoothScoOn()).thenReturn(false);
        when(mAudioManager.isBluetoothScoAvailableOffCall()).thenReturn(true);
    }

    // MARK - expected = Callback.onSuccess
    @Test
    public void prepareBluetoothHeadset_HappyPath() throws Exception {
        // setup
        mSignal = new CountDownLatch(3);

        // exercise
        mBluetoothHeadsetProvider.prepareBluetoothHeadset(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onComplete(BluetoothHeadsetProvider.HeadsetRequestType.CONNECT, BluetoothHeadsetProvider.Result.COMPLETE);
    }

    @Test
    public void prepareBluetoothHeadset_AlreadyScoOn() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        when(mAudioManager.isBluetoothScoOn()).thenReturn(true);

        // exercise
        mBluetoothHeadsetProvider.prepareBluetoothHeadset(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onComplete(BluetoothHeadsetProvider.HeadsetRequestType.CONNECT, BluetoothHeadsetProvider.Result.COMPLETE);
    }

    // MARK - expected = Callback.onError

    @Test
    public void prepareBluetoothHeadset_AdapterIsNull() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mIsNullAdapter = true;

        // exercise
        mBluetoothHeadsetProvider.prepareBluetoothHeadset(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onComplete(BluetoothHeadsetProvider.HeadsetRequestType.CONNECT, BluetoothHeadsetProvider.Result.ERROR);
    }

    @Test
    public void prepareBluetoothHeadset_AdapterDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mIsEnabledAdapter = false;

        // exercise
        mBluetoothHeadsetProvider.prepareBluetoothHeadset(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onComplete(BluetoothHeadsetProvider.HeadsetRequestType.CONNECT, BluetoothHeadsetProvider.Result.ERROR);
    }

    @Test
    public void prepareBluetoothHeadset_UnavailableScoOn() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        when(mAudioManager.isBluetoothScoAvailableOffCall()).thenReturn(false);

        // exercise
        mBluetoothHeadsetProvider.prepareBluetoothHeadset(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onComplete(BluetoothHeadsetProvider.HeadsetRequestType.CONNECT, BluetoothHeadsetProvider.Result.ERROR);
    }

    @Test
    public void prepareBluetoothHeadset_AclDisconnected() throws Exception {
        // setup
        mSignal = new CountDownLatch(3);
        when(mBroadcastReceiverIntent.getAction()).thenReturn(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        // exercise
        mBluetoothHeadsetProvider.prepareBluetoothHeadset(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onComplete(BluetoothHeadsetProvider.HeadsetRequestType.CONNECT, BluetoothHeadsetProvider.Result.ERROR);
    }

    @Test
    public void prepareBluetoothHeadset_ScoConnectTaskRetryOver() throws Exception {
        // setup
        mSignal = new CountDownLatch(7);
        mIsCallOnReceive = false;

        // exercise
        mBluetoothHeadsetProvider.prepareBluetoothHeadset(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onComplete(BluetoothHeadsetProvider.HeadsetRequestType.CONNECT, BluetoothHeadsetProvider.Result.ERROR);
    }

    // MARK - Arg null

    @Test(expected = NullPointerException.class)
    public void prepareBluetoothHeadset_ArgNull() throws Exception {
        // exercise
        mBluetoothHeadsetProvider.prepareBluetoothHeadset(null);
    }

    // MARK - finishBluetoothHeadset

    @Test
    public void finishBluetoothHeadset() throws Exception {
        // setup
        mSignal = new CountDownLatch(3);
        when(mAudioManager.isBluetoothScoOn()).thenReturn(true);
        when(mAudioManager.isBluetoothScoAvailableOffCall()).thenReturn(true);
        when(mBroadcastReceiverIntent.getAction()).thenReturn(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        when(mBroadcastReceiverIntent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE,AudioManager.SCO_AUDIO_STATE_ERROR)).thenReturn(AudioManager.SCO_AUDIO_STATE_DISCONNECTED);

        // exercise
        mBluetoothHeadsetProvider.mIsStarting = false;
        mBluetoothHeadsetProvider.finishBluetoothHeadset(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onComplete(BluetoothHeadsetProvider.HeadsetRequestType.DISCONNECT, BluetoothHeadsetProvider.Result.COMPLETE);
    }
}