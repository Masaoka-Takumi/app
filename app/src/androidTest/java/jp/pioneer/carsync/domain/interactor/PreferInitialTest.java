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

import jp.pioneer.carsync.domain.component.InitialSettingUpdater;
import jp.pioneer.carsync.domain.model.AmStep;
import jp.pioneer.carsync.domain.model.FmStep;
import jp.pioneer.carsync.domain.model.InitialSetting;
import jp.pioneer.carsync.domain.model.InitialSettingStatus;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.RearOutputPreoutOutputSetting;
import jp.pioneer.carsync.domain.model.RearOutputSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/25.
 */
public class PreferInitialTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PreferInitial mPreferInitial;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock InitialSettingUpdater mUpdater;

    InitialSetting mInitialSetting = new InitialSetting();
    InitialSettingStatus mInitialSettingStatus = new InitialSettingStatus();
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

        when(mStatusHolder.getInitialSetting()).thenReturn(mInitialSetting);
        when(mStatusHolder.getInitialSettingStatus()).thenReturn(mInitialSettingStatus);
    }

    @Test
    public void toggleFmStep() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mInitialSettingStatus.fmStepSettingEnabled = true;
        mInitialSetting.fmStep = FmStep._50KHZ;

        // exercise
        mPreferInitial.toggleFmStep();
        mSignal.await();

        // verify
        verify(mUpdater).setFmStep(FmStep._100KHZ);
    }

    @Test
    public void toggleFmStep_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mPreferInitial.toggleFmStep();
        mSignal.await();

        // verify
        verify(mUpdater, never()).setFmStep(any(FmStep.class));
    }

    @Test
    public void toggleAmStep() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mInitialSettingStatus.amStepSettingEnabled = true;
        mInitialSetting.amStep = AmStep._9KHZ;

        // exercise
        mPreferInitial.toggleAmStep();
        mSignal.await();

        // verify
        verify(mUpdater).setAmStep(AmStep._10KHZ);
    }

    @Test
    public void toggleAmStep_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mPreferInitial.toggleAmStep();
        mSignal.await();

        // verify
        verify(mUpdater, never()).setAmStep(any(AmStep.class));
    }

    @Test
    public void setRearOutputPreoutOutput() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mInitialSettingStatus.rearOutputPreoutOutputSettingEnabled = true;

        // exercise
        mPreferInitial.toggleRearOutputPreoutOutput();
        mSignal.await();

        // verify
        verify(mUpdater).setRearOutputPreoutOutput(RearOutputPreoutOutputSetting.SUBWOOFER_SUBWOOFER);
    }

    @Test
    public void setRearOutputPreoutOutput_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mPreferInitial.toggleRearOutput();
        mSignal.await();

        // verify
        verify(mUpdater, never()).setRearOutputPreoutOutput(any(RearOutputPreoutOutputSetting.class));
    }

    @Test
    public void toggleRearOutput() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mInitialSettingStatus.rearOutputSettingEnabled = true;
        mInitialSetting.rearOutputSetting = RearOutputSetting.REAR;

        // exercise
        mPreferInitial.toggleRearOutput();
        mSignal.await();

        // verify
        verify(mUpdater).setRearOutput(RearOutputSetting.SUBWOOFER);
    }

    @Test
    public void toggleRearOutput_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mPreferInitial.toggleRearOutput();
        mSignal.await();

        // verify
        verify(mUpdater, never()).setRearOutput(any(RearOutputSetting.class));
    }

    @Test
    public void setMenuDisplayLanguage() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mInitialSettingStatus.menuDisplayLanguageSettingEnabled = true;

        // exercise
        mPreferInitial.setMenuDisplayLanguage(MenuDisplayLanguageType.RUSSIAN);
        mSignal.await();

        // verify
        verify(mUpdater).setMenuDisplayLanguage(MenuDisplayLanguageType.RUSSIAN);
    }

    @Test
    public void setMenuDisplayLanguage_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mPreferInitial.setMenuDisplayLanguage(MenuDisplayLanguageType.RUSSIAN);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setMenuDisplayLanguage(any(MenuDisplayLanguageType.class));
    }

    @Test(expected = NullPointerException.class)
    public void setMenuDisplayLanguage_ArgNull() throws Exception {
        // exercise
        mPreferInitial.setMenuDisplayLanguage(null);
    }

}