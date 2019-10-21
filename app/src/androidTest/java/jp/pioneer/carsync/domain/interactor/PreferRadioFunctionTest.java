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

import jp.pioneer.carsync.domain.component.RadioFunctionSettingUpdater;
import jp.pioneer.carsync.domain.model.FMTunerSetting;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TASetting;
import jp.pioneer.carsync.domain.model.TunerFunctionSetting;
import jp.pioneer.carsync.domain.model.TunerFunctionSettingStatus;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/01.
 */
public class PreferRadioFunctionTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PreferRadioFunction mPreferRadioFunction;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock RadioFunctionSettingUpdater mUpdater;

    TunerFunctionSettingStatus mTunerFunctionSettingStatus;
    TunerFunctionSetting mTunerFunctionSetting;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

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

        mTunerFunctionSettingStatus = new TunerFunctionSettingStatus();
        when(mStatusHolder.getTunerFunctionSettingStatus()).thenReturn(mTunerFunctionSettingStatus);
        mTunerFunctionSetting = new TunerFunctionSetting();
        when(mStatusHolder.getTunerFunctionSetting()).thenReturn(mTunerFunctionSetting);
    }

    @Test
    public void setLocal() throws Exception {
        // setup
        mTunerFunctionSettingStatus.localSettingEnabled = true;
        LocalSetting setting = LocalSetting.LEVEL1;

        // exercise
        mPreferRadioFunction.setLocal(setting);
        mSignal.await();

        // verify
        verify(mUpdater).setLocal(setting);
    }

    @Test
    public void setFmTuner() throws Exception {
        // setup
        mTunerFunctionSettingStatus.fmSettingEnabled = true;
        FMTunerSetting setting = FMTunerSetting.MUSIC;

        // exercise
        mPreferRadioFunction.setFmTuner(setting);
        mSignal.await();

        // verify
        verify(mUpdater).setFmTuner(setting);
    }

    @Test
    public void setReg() throws Exception {
        // setup
        mTunerFunctionSettingStatus.regSettingEnabled = true;

        // exercise
        mPreferRadioFunction.setReg(true);
        mSignal.await();

        // verify
        verify(mUpdater).setReg(true);
    }

    @Test
    public void setTa() throws Exception {
        // setup
        mTunerFunctionSettingStatus.taSettingEnabled = true;
        TASetting setting = TASetting.RDS_TA_ON;

        // exercise
        mPreferRadioFunction.setTa(setting);
        mSignal.await();

        // verify
        verify(mUpdater).setTa(setting);
    }

    @Test
    public void setAf() throws Exception {
        // setup
        mTunerFunctionSettingStatus.afSettingEnabled = true;

        // exercise
        mPreferRadioFunction.setAf(true);
        mSignal.await();

        // verify
        verify(mUpdater).setAf(true);
    }

    @Test
    public void setNews() throws Exception {
        // setup
        mTunerFunctionSettingStatus.newsSettingEnabled = true;

        // exercise
        mPreferRadioFunction.setNews(true);
        mSignal.await();

        // verify
        verify(mUpdater).setNews(true);
    }

    @Test
    public void setAlarm() throws Exception {
        // setup
        mTunerFunctionSettingStatus.alarmSettingEnabled = true;

        // exercise
        mPreferRadioFunction.setAlarm(true);
        mSignal.await();

        // verify
        verify(mUpdater).setAlarm(true);
    }

    @Test
    public void togglePchManual_ToManual() throws Exception {
        // setup
        mTunerFunctionSettingStatus.pchManualEnabled = true;
        mTunerFunctionSetting.pchManualSetting = PCHManualSetting.PCH;

        // exercise
        mPreferRadioFunction.togglePchManual();
        mSignal.await();

        // verify
        verify(mUpdater).setPchManual(PCHManualSetting.MANUAL);
    }

    @Test
    public void togglePchManual_ToPch() throws Exception {
        // setup
        mTunerFunctionSettingStatus.pchManualEnabled = true;
        mTunerFunctionSetting.pchManualSetting = PCHManualSetting.MANUAL;

        // exercise
        mPreferRadioFunction.togglePchManual();
        mSignal.await();

        // verify
        verify(mUpdater).setPchManual(PCHManualSetting.PCH);
    }

    @Test
    public void setLocal_Disabled() throws Exception {
        // setup
        mTunerFunctionSettingStatus.localSettingEnabled = false;
        LocalSetting setting = LocalSetting.LEVEL2;

        // exercise
        mPreferRadioFunction.setLocal(setting);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setLocal(any(LocalSetting.class));
    }

    @Test
    public void setFmTuner_Disabled() throws Exception {
        // setup
        mTunerFunctionSettingStatus.fmSettingEnabled = false;
        FMTunerSetting setting = FMTunerSetting.STANDARD;

        // exercise
        mPreferRadioFunction.setFmTuner(setting);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setFmTuner(any(FMTunerSetting.class));
    }

    @Test
    public void setReg_Disabled() throws Exception {
        // setup
        mTunerFunctionSettingStatus.regSettingEnabled = false;

        // exercise
        mPreferRadioFunction.setReg(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setReg(anyBoolean());
    }

    @Test
    public void setTa_Disabled() throws Exception {
        // setup
        mTunerFunctionSettingStatus.taSettingEnabled = false;
        TASetting setting = TASetting.DAB_RDS_TA_ON;

        // exercise
        mPreferRadioFunction.setTa(setting);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setTa(any(TASetting.class));
    }

    @Test
    public void setAf_Disabled() throws Exception {
        // setup
        mTunerFunctionSettingStatus.afSettingEnabled = false;

        // exercise
        mPreferRadioFunction.setAf(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setAf(anyBoolean());
    }

    @Test
    public void setNews_Disabled() throws Exception {
        // setup
        mTunerFunctionSettingStatus.newsSettingEnabled = false;

        // exercise
        mPreferRadioFunction.setNews(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setNews(anyBoolean());
    }

    @Test
    public void setAlarm_Disabled() throws Exception {
        // setup
        mTunerFunctionSettingStatus.alarmSettingEnabled = false;

        // exercise
        mPreferRadioFunction.setAlarm(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setAlarm(anyBoolean());
    }

    @Test
    public void setPchManual_Disabled() throws Exception {
        // setup
        mTunerFunctionSettingStatus.pchManualEnabled = false;

        // exercise
        mPreferRadioFunction.togglePchManual();
        mSignal.await();

        // verify
        verify(mUpdater, never()).setPchManual(any(PCHManualSetting.class));
    }

    @Test(expected = NullPointerException.class)
    public void setLocal_ArgNull() throws Exception {
        // exercise
        mPreferRadioFunction.setLocal(null);
    }

    @Test(expected = NullPointerException.class)
    public void setFmTuner_ArgNull() throws Exception {
        // exercise
        mPreferRadioFunction.setFmTuner(null);
    }

    @Test(expected = NullPointerException.class)
    public void setTa_ArgNull() throws Exception {
        // exercise
        mPreferRadioFunction.setTa(null);
    }
}