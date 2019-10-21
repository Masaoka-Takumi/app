package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.SiriusXmSourceController;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.TunerStatus;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/13.
 */
public class ControlSiriusXmSourceTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    ControlSiriusXmSource mControlSiriusXmSource;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock CarDevice mCarDevice;
    @Mock SiriusXmSourceController mSiriusXmSourceController;

    SxmMediaInfo mSxmMediaInfo = new SxmMediaInfo();
    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    enum EXCEPTION_VALUE {
        MIN(0), MAX(7);
        int value;

        EXCEPTION_VALUE(int value) {
            this.value = value;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mCarDevice.getSourceController(eq(MediaSourceType.SIRIUS_XM))).thenReturn(mSiriusXmSourceController);
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(new CarDeviceMediaInfoHolder() {{ sxmMediaInfo = mSxmMediaInfo; }});
        when(mSiriusXmSourceController.isActive()).thenReturn(true);
        mSxmMediaInfo.reset();
        mControlSiriusXmSource = new ControlSiriusXmSource(mCarDevice);
        mControlSiriusXmSource.mHandler = mHandler;
        mControlSiriusXmSource.mStatusHolder = mStatusHolder;

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
    public void toggleBand() throws Exception {
        // exercise
        mControlSiriusXmSource.toggleBand();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController).toggleBand();
    }

    @Test
    public void toggleBand_isActiveFalse() throws Exception {
        // setup
        when(mSiriusXmSourceController.isActive()).thenReturn(false);

        // exercise
        mControlSiriusXmSource.toggleBand();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).toggleBand();
    }

    @Test
    public void callPreset() throws Exception {
        // exercise
        mControlSiriusXmSource.callPreset(1);
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController).callPreset(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void callPreset_presetNoValueMin() throws Exception {
        // exercise
        mControlSiriusXmSource.callPreset(ControlRadioSourceTest.EXCEPTION_VALUE.MIN.value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void callPreset_presetNoValueMax() throws Exception {
        // exercise
        mControlSiriusXmSource.callPreset(ControlRadioSourceTest.EXCEPTION_VALUE.MAX.value);
    }

    @Test
    public void callPreset_isActiveFalse() throws Exception {
        // setup
        when(mSiriusXmSourceController.isActive()).thenReturn(false);

        // exercise
        mControlSiriusXmSource.callPreset(1);
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).callPreset(anyInt());
    }

    @Test
    public void presetUp() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        mSxmMediaInfo.subscriptionUpdatingShowing = false;
        mSxmMediaInfo.inReplayMode = false;
        mSxmMediaInfo.playbackMode = PlaybackMode.PLAY;

        // exercise
        mControlSiriusXmSource.presetUp();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController).presetUp();
    }

    @Test
    public void presetUp_isActiveFalse() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        mSxmMediaInfo.subscriptionUpdatingShowing = false;
        mSxmMediaInfo.inReplayMode = false;
        mSxmMediaInfo.playbackMode = PlaybackMode.PLAY;
        when(mSiriusXmSourceController.isActive()).thenReturn(false);

        // exercise
        mControlSiriusXmSource.presetUp();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetUp();
    }

    @Test
    public void presetUp_runningBsm() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.BSM;
        mSxmMediaInfo.subscriptionUpdatingShowing = false;
        mSxmMediaInfo.inReplayMode = false;
        mSxmMediaInfo.playbackMode = PlaybackMode.PLAY;

        // exercise
        mControlSiriusXmSource.presetUp();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetUp();
    }

    @Test
    public void presetUp_showingSubscription() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        mSxmMediaInfo.subscriptionUpdatingShowing = true;
        mSxmMediaInfo.inReplayMode = false;
        mSxmMediaInfo.playbackMode = PlaybackMode.PLAY;

        // exercise
        mControlSiriusXmSource.presetUp();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetUp();
    }

    @Test
    public void presetUp_isReplayMode() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        mSxmMediaInfo.subscriptionUpdatingShowing = false;
        mSxmMediaInfo.inReplayMode = true;
        mSxmMediaInfo.playbackMode = PlaybackMode.PLAY;

        // exercise
        mControlSiriusXmSource.presetUp();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetUp();
    }

    @Test
    public void presetUp_isPause() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        mSxmMediaInfo.subscriptionUpdatingShowing = false;
        mSxmMediaInfo.inReplayMode = false;
        mSxmMediaInfo.playbackMode = PlaybackMode.PAUSE;

        // exercise
        mControlSiriusXmSource.presetUp();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetUp();
    }

    @Test
    public void presetDown() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        mSxmMediaInfo.subscriptionUpdatingShowing = false;
        mSxmMediaInfo.inReplayMode = false;
        mSxmMediaInfo.playbackMode = PlaybackMode.PLAY;

        // exercise
        mControlSiriusXmSource.presetDown();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController).presetDown();
    }

    @Test
    public void presetDown_isActiveFalse() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        mSxmMediaInfo.subscriptionUpdatingShowing = false;
        mSxmMediaInfo.inReplayMode = false;
        mSxmMediaInfo.playbackMode = PlaybackMode.PLAY;
        when(mSiriusXmSourceController.isActive()).thenReturn(false);

        // exercise
        mControlSiriusXmSource.presetDown();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void presetDown_runningBsm() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.BSM;
        mSxmMediaInfo.subscriptionUpdatingShowing = false;
        mSxmMediaInfo.inReplayMode = false;
        mSxmMediaInfo.playbackMode = PlaybackMode.PLAY;

        // exercise
        mControlSiriusXmSource.presetDown();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void presetDown_showingSubscription() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        mSxmMediaInfo.subscriptionUpdatingShowing = true;
        mSxmMediaInfo.inReplayMode = false;
        mSxmMediaInfo.playbackMode = PlaybackMode.PLAY;

        // exercise
        mControlSiriusXmSource.presetDown();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void presetDown_isReplayMode() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        mSxmMediaInfo.subscriptionUpdatingShowing = false;
        mSxmMediaInfo.inReplayMode = true;
        mSxmMediaInfo.playbackMode = PlaybackMode.PLAY;

        // exercise
        mControlSiriusXmSource.presetDown();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void presetDown_isPause() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        mSxmMediaInfo.subscriptionUpdatingShowing = false;
        mSxmMediaInfo.inReplayMode = false;
        mSxmMediaInfo.playbackMode = PlaybackMode.PAUSE;

        // exercise
        mControlSiriusXmSource.presetDown();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void toggleLiveMode() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;

        // exercise
        mControlSiriusXmSource.toggleLiveMode();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void toggleLiveMode_Scanning() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.SCAN;

        // exercise
        mControlSiriusXmSource.toggleLiveMode();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void toggleLiveMode_isActiveFalse() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        when(mSiriusXmSourceController.isActive()).thenReturn(false);

        // exercise
        mControlSiriusXmSource.toggleLiveMode();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void toggleChannelMode() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;

        // exercise
        mControlSiriusXmSource.toggleChannelMode();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void toggleChannelMode_Scanning() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.SCAN;

        // exercise
        mControlSiriusXmSource.toggleChannelMode();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void toggleChannelMode_isActiveFalse() throws Exception {
        // setup
        mSxmMediaInfo.tunerStatus = TunerStatus.NORMAL;
        when(mSiriusXmSourceController.isActive()).thenReturn(false);

        // exercise
        mControlSiriusXmSource.toggleChannelMode();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void toggleTuneMix() throws Exception {
        // exercise
        mControlSiriusXmSource.toggleTuneMix();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void toggleTuneMix_isActiveFalse() throws Exception {
        // setup
        when(mSiriusXmSourceController.isActive()).thenReturn(false);

        // exercise
        mControlSiriusXmSource.toggleTuneMix();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void toggleReplayMode() throws Exception {
        // exercise
        mControlSiriusXmSource.toggleReplayMode();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void toggleReplayMode_isActiveFalse() throws Exception {
        // setup
        when(mSiriusXmSourceController.isActive()).thenReturn(false);

        // exercise
        mControlSiriusXmSource.toggleReplayMode();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void releaseSubscriptionUpdating() throws Exception {
        // exercise
        mControlSiriusXmSource.releaseSubscriptionUpdating();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }

    @Test
    public void releaseSubscriptionUpdating_isActiveFalse() throws Exception {
        // setup
        when(mSiriusXmSourceController.isActive()).thenReturn(false);

        // exercise
        mControlSiriusXmSource.releaseSubscriptionUpdating();
        mSignal.await();

        // verify
        verify(mSiriusXmSourceController,never()).presetDown();
    }
}