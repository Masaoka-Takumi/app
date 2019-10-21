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

import jp.pioneer.carsync.domain.component.NaviGuideVoiceSettingUpdater;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/25.
 */
public class PreferNaviGuideVoiceTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PreferNaviGuideVoice mPreferNaviGuideVoice;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock NaviGuideVoiceSettingUpdater mUpdater;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);
    CarDeviceStatus mCarDeviceStatus = new CarDeviceStatus();

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
    }

    @Test
    public void setNaviGuideVoice() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mCarDeviceStatus.naviGuideVoiceSettingEnabled = true;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mPreferNaviGuideVoice.setNaviGuideVoice(true);
        mSignal.await();

        // verify
        verify(mUpdater).setNaviGuideVoice(true);
    }

    @Test
    public void setNaviGuideVoice_settingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mCarDeviceStatus.naviGuideVoiceSettingEnabled = false;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mPreferNaviGuideVoice.setNaviGuideVoice(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setNaviGuideVoice(anyBoolean());
    }

    @Test
    public void setNaviGuideVoiceVolume() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mCarDeviceStatus.naviGuideVoiceSettingEnabled = true;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mPreferNaviGuideVoice.setNaviGuideVoiceVolume(NaviGuideVoiceVolumeSetting.MAXIMUM);
        mSignal.await();

        // verify
        verify(mUpdater).setNaviGuideVoiceVolume(NaviGuideVoiceVolumeSetting.MAXIMUM);
    }

    @Test
    public void setNaviGuideVoiceVolume_settingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mCarDeviceStatus.naviGuideVoiceSettingEnabled = false;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mPreferNaviGuideVoice.setNaviGuideVoiceVolume(NaviGuideVoiceVolumeSetting.MAXIMUM);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setNaviGuideVoiceVolume(any(NaviGuideVoiceVolumeSetting.class));
    }

    @Test(expected = NullPointerException.class)
    public void setNaviGuideVoiceVolume_ArgNull() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mPreferNaviGuideVoice.setNaviGuideVoiceVolume(null);
    }

}