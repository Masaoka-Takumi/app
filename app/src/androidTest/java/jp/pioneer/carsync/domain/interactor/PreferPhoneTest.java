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

import jp.pioneer.carsync.domain.component.PhoneSettingUpdater;
import jp.pioneer.carsync.domain.model.PhoneSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/19.
 */
public class PreferPhoneTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PreferPhone mPreferPhone;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock PhoneSettingUpdater mUpdater;

    PhoneSettingStatus mPhoneSettingStatus = new PhoneSettingStatus();
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

        when(mStatusHolder.getPhoneSettingStatus()).thenReturn(mPhoneSettingStatus);
    }

    @Test
    public void setAutoAnswer() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mPhoneSettingStatus.autoAnswerSettingEnabled = true;

        // exercise
        mPreferPhone.setAutoAnswer(true);
        mSignal.await();

        // verify
        verify(mUpdater).setAutoAnswer(true);
    }

    @Test
    public void setAutoAnswer_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mPhoneSettingStatus.autoAnswerSettingEnabled = false;

        // exercise
        mPreferPhone.setAutoAnswer(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setAutoAnswer(anyBoolean());
    }

    @Test
    public void setAutoPairing() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mPhoneSettingStatus.autoPairingSettingEnabled = true;

        // exercise
        mPreferPhone.setAutoPairing(true);
        mSignal.await();

        // verify
        verify(mUpdater).setAutoPairing(true);
    }

    @Test
    public void setAutoPairing_SettingDisabled() throws Exception {
// setup
        mSignal = new CountDownLatch(1);
        mPhoneSettingStatus.autoPairingSettingEnabled = false;

        // exercise
        mPreferPhone.setAutoPairing(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setAutoPairing(anyBoolean());
    }

}