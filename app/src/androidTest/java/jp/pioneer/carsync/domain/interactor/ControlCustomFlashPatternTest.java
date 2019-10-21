package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.matchers.Null;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CustomFlashRequestType;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.ProtocolSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/22.
 */
public class ControlCustomFlashPatternTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ControlCustomFlashPattern mControlCustomFlashPattern;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock AppSharedPreference mPreference;
    @Mock CarDevice mCarDevice;

    ProtocolSpec mProtocolSpec = new ProtocolSpec();
    IlluminationSettingStatus mIlluminationSettingStatus = new IlluminationSettingStatus();

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

        when(mStatusHolder.getProtocolSpec()).thenReturn(mProtocolSpec);
        when(mStatusHolder.getIlluminationSettingStatus()).thenReturn(mIlluminationSettingStatus);
        when(mPreference.isLightingEffectEnabled()).thenReturn(true);
    }

    @Test(expected = NullPointerException.class)
    public void start() throws Exception {
//        // setup
//        mSignal = new CountDownLatch(1);
//        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.SPH);
//        mIlluminationSettingStatus.customFlashPatternSettingEnabled = true;
//
//        // exercise
//        mControlCustomFlashPattern.start();
//        mSignal.await();
//
//        // verify
//        verify(mCarDevice).requestCustomFlash(CustomFlashRequestType.START);
    }

    @Test(expected = NullPointerException.class)
    public void start_NotSph() throws Exception {
//        // setup
//        mSignal = new CountDownLatch(1);
//        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.DEH);
//        mIlluminationSettingStatus.customFlashPatternSettingEnabled = true;
//
//        // exercise
//        mControlCustomFlashPattern.start();
//        mSignal.await();
//
//        // verify
//        verify(mCarDevice, never()).requestCustomFlash(any(CustomFlashRequestType.class));
    }

    @Test(expected = NullPointerException.class)
    public void start_CustomFlashPatternSettingDisabled() throws Exception {
//        // setup
//        mSignal = new CountDownLatch(1);
//        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.SPH);
//        mIlluminationSettingStatus.customFlashPatternSettingEnabled = false;
//
//        // exercise
//        mControlCustomFlashPattern.start();
//        mSignal.await();
//
//        // verify
//        verify(mCarDevice, never()).requestCustomFlash(any(CustomFlashRequestType.class));
    }

    @Test(expected = NullPointerException.class)
    public void start_LightingEffectSettingDisabled() throws Exception {
//        // setup
//        mSignal = new CountDownLatch(1);
//        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.SPH);
//        mIlluminationSettingStatus.customFlashPatternSettingEnabled = true;
//        when(mPreference.isLightingEffectEnabled()).thenReturn(false);
//
//        // exercise
//        mControlCustomFlashPattern.start();
//        mSignal.await();
//
//        // verify
//        verify(mCarDevice, never()).requestCustomFlash(any(CustomFlashRequestType.class));
    }

    @Test(expected = NullPointerException.class)
    public void finish() throws Exception {
//        // setup
//        mSignal = new CountDownLatch(1);
//        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.SPH);
//        mIlluminationSettingStatus.customFlashPatternSettingEnabled = true;
//
//        // exercise
//        mControlCustomFlashPattern.finish();
//        mSignal.await();
//
//        // verify
//        verify(mCarDevice).requestCustomFlash(CustomFlashRequestType.FINISH);
    }

    @Test(expected = NullPointerException.class)
    public void finish_NotSph() throws Exception {
//        // setup
//        mSignal = new CountDownLatch(1);
//        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.DEH);
//        mIlluminationSettingStatus.customFlashPatternSettingEnabled = true;
//
//        // exercise
//        mControlCustomFlashPattern.finish();
//        mSignal.await();
//
//        // verify
//        verify(mCarDevice, never()).requestCustomFlash(any(CustomFlashRequestType.class));
    }

    @Test(expected = NullPointerException.class)
    public void finish_CustomFlashPatternSettingDisabled() throws Exception {
//        // setup
//        mSignal = new CountDownLatch(1);
//        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.SPH);
//        mIlluminationSettingStatus.customFlashPatternSettingEnabled = false;
//
//        // exercise
//        mControlCustomFlashPattern.finish();
//        mSignal.await();
//
//        // verify
//        verify(mCarDevice, never()).requestCustomFlash(any(CustomFlashRequestType.class));
    }

}