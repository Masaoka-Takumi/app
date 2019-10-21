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

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.content.UpdateParams;
import jp.pioneer.carsync.domain.repository.ContactRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/27.
 */
public class UpdateContactTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks UpdateContact mUpdateContact;
    @Mock @ForInfrastructure Handler mHandler;
    @Mock ContactRepository mRepository;

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
    }

    @Test
    public void execute() throws Exception {
        // setup
        UpdateParams updateParams = mock(UpdateParams.class);

        // exercise
        mUpdateContact.execute(updateParams);
        mSignal.await();

        // verify
        verify(mRepository).update(updateParams);
    }

    @Test(expected = NullPointerException.class)
    public void executeArgNull() throws Exception {
        // setup
        UpdateParams updateParams = null;

        // exercise
        mUpdateContact.execute(updateParams);
    }

}