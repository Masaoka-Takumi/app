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
import jp.pioneer.carsync.domain.component.RadioSourceController;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.PtySearchSetting;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerFunctionSetting;
import jp.pioneer.carsync.domain.model.TunerFunctionSettingStatus;
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
 * Created by NSW00_008320 on 2017/05/26.
 */
public class ControlRadioSourceTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    ControlRadioSource mControlRadioSource;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock CarDevice mCarDevice;
    @Mock RadioSourceController mRadioSourceController;

    RadioInfo mRadioInfo = new RadioInfo();
    TunerFunctionSetting mTunerFunctionSetting = new TunerFunctionSetting();
    TunerFunctionSettingStatus mTunerFunctionSettingStatus = new TunerFunctionSettingStatus();
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

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });

        when(mCarDevice.getSourceController(eq(MediaSourceType.RADIO))).thenReturn(mRadioSourceController);
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(new CarDeviceMediaInfoHolder() {{ radioInfo = mRadioInfo; }});
        when(mStatusHolder.getTunerFunctionSetting()).thenReturn(mTunerFunctionSetting);
        when(mStatusHolder.getTunerFunctionSettingStatus()).thenReturn(mTunerFunctionSettingStatus);
        when(mRadioSourceController.isActive()).thenReturn(true);
        mRadioInfo.reset();
        mControlRadioSource = new ControlRadioSource(mCarDevice);
        mControlRadioSource.mHandler = mHandler;
        mControlRadioSource.mStatusHolder = mStatusHolder;

        mSignal = new CountDownLatch(1);
    }

    @Test
    public void toggleBand() throws Exception {
        // exercise
        mControlRadioSource.toggleBand();
        mSignal.await();

        // verify
        verify(mRadioSourceController).toggleBand();
    }

    @Test
    public void toggleBand_isActiveFalse() throws Exception {
        // setup
        when(mRadioSourceController.isActive()).thenReturn(false);

        // exercise
        mControlRadioSource.toggleBand();
        mSignal.await();

        // verify
        verify(mRadioSourceController,never()).toggleBand();
    }

    @Test
    public void callPreset() throws Exception {
        // exercise
        mControlRadioSource.callPreset(1);
        mSignal.await();

        // verify
        verify(mRadioSourceController).callPreset(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void callPreset_presetNoValueMin() throws Exception {
        // exercise
        mControlRadioSource.callPreset(EXCEPTION_VALUE.MIN.value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void callPreset_presetNoValueMax() throws Exception {
        // exercise
        mControlRadioSource.callPreset(EXCEPTION_VALUE.MAX.value);
    }

    @Test
    public void callPreset_isActiveFalse() throws Exception {
        // setup
        when(mRadioSourceController.isActive()).thenReturn(false);

        // exercise
        mControlRadioSource.callPreset(1);
        mSignal.await();

        // verify
        verify(mRadioSourceController,never()).callPreset(anyInt());
    }

    @Test
    public void channelUp_Manual() throws Exception {
        // setup
        mRadioInfo.tunerStatus = TunerStatus.NORMAL;
        mTunerFunctionSetting.pchManualSetting = PCHManualSetting.MANUAL;

        // exercise
        mControlRadioSource.channelUp();
        mSignal.await();

        // verify
        verify(mRadioSourceController).manualUp();
    }

    @Test
    public void channelUp_Pch() throws Exception {
        // setup
        mRadioInfo.tunerStatus = TunerStatus.NORMAL;
        mTunerFunctionSetting.pchManualSetting = PCHManualSetting.PCH;

        // exercise
        mControlRadioSource.channelUp();
        mSignal.await();

        // verify
        verify(mRadioSourceController).manualUp();
    }

    @Test
    public void channelUp_isActiveFalse() throws Exception {
        // setup
        mRadioInfo.tunerStatus = TunerStatus.NORMAL;
        when(mRadioSourceController.isActive()).thenReturn(false);

        // exercise
        mControlRadioSource.channelUp();
        mSignal.await();

        // verify
        verify(mRadioSourceController,never()).manualUp();
    }

    @Test
    public void channelUp_runningBsm() throws Exception {
        // setup
        mRadioInfo.tunerStatus = TunerStatus.BSM;
        when(mRadioSourceController.isActive()).thenReturn(false);

        // exercise
        mControlRadioSource.channelUp();
        mSignal.await();

        // verify
        verify(mRadioSourceController,never()).manualUp();
    }

    @Test
    public void channelDown_Manual() throws Exception {
        // setup
        mRadioInfo.tunerStatus = TunerStatus.NORMAL;
        mTunerFunctionSetting.pchManualSetting = PCHManualSetting.MANUAL;

        // exercise
        mControlRadioSource.channelDown();
        mSignal.await();

        // verify
        verify(mRadioSourceController).manualDown();
    }

    @Test
    public void channelDown_Pch() throws Exception {
        // setup
        mRadioInfo.tunerStatus = TunerStatus.NORMAL;
        mTunerFunctionSetting.pchManualSetting = PCHManualSetting.PCH;

        // exercise
        mControlRadioSource.channelDown();
        mSignal.await();

        // verify
        verify(mRadioSourceController).manualDown();
    }

    @Test
    public void channelDown_isActiveFalse() throws Exception {
        // setup
        mRadioInfo.tunerStatus = TunerStatus.NORMAL;
        when(mRadioSourceController.isActive()).thenReturn(false);

        // exercise
        mControlRadioSource.channelDown();
        mSignal.await();

        // verify
        verify(mRadioSourceController,never()).manualDown();
    }

    @Test
    public void channelDown_runningBsm() throws Exception {
        // setup
        mRadioInfo.tunerStatus = TunerStatus.BSM;
        when(mRadioSourceController.isActive()).thenReturn(false);

        // exercise
        mControlRadioSource.channelDown();
        mSignal.await();

        // verify
        verify(mRadioSourceController,never()).manualDown();
    }

    @Test
    public void startBsm() throws Exception {
        // setup
        mTunerFunctionSettingStatus.bsmSettingEnabled = true;

        // exercise
        mControlRadioSource.startBsm();
        mSignal.await();

        // verify
        verify(mRadioSourceController).startBsm();
    }

    @Test
    public void startBsm_SettingDisabled() throws Exception {
        // setup
        mTunerFunctionSettingStatus.bsmSettingEnabled = false;

        // exercise
        mControlRadioSource.startBsm();
        mSignal.await();

        // verify
        verify(mRadioSourceController, never()).startBsm();
    }

    @Test
    public void startPtySearch() throws Exception {
        // setup
        mTunerFunctionSettingStatus.ptySearchSettingEnabled = true;

        // exercise
        mControlRadioSource.startPtySearch(PtySearchSetting.NEWS_INFO);
        mSignal.await();

        // verify
        verify(mRadioSourceController).startPtySearch(PtySearchSetting.NEWS_INFO);
    }

    @Test
    public void startPtySearch_SettingDisabled() throws Exception {
        // setup
        mTunerFunctionSettingStatus.bsmSettingEnabled = false;

        // exercise
        mControlRadioSource.startPtySearch(PtySearchSetting.NEWS_INFO);
        mSignal.await();

        // verify
        verify(mRadioSourceController, never()).startPtySearch(any(PtySearchSetting.class));
    }

    @Test(expected = NullPointerException.class)
    public void startPtySearch_ArgNull() throws Exception {
        // exercise
        mControlRadioSource.startPtySearch(null);
    }
}