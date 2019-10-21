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
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/29.
 */
public class SelectRadioFavoriteTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    SelectRadioFavorite mSelectRadioFavorite;
    @Mock Handler mHandler;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    CarDevice mCarDevice;
    RadioSourceController mRadioSourceController;

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

        mCarDevice = mock(CarDevice.class);
        mRadioSourceController = mock(RadioSourceController.class);
        when(mCarDevice.getSourceController(eq(MediaSourceType.RADIO))).thenReturn(mRadioSourceController);

        mSelectRadioFavorite = new SelectRadioFavorite(mCarDevice);
        mSelectRadioFavorite.mHandler = mHandler;

        when(mRadioSourceController.isActive()).thenReturn(true);
    }

    @Test
    public void execute() throws Exception {
        // exercise
        mSelectRadioFavorite.execute(1, RadioBandType.AM,3);
        mSignal.await();

        // verify
        verify(mRadioSourceController).selectFavorite(1,RadioBandType.AM,3);
    }

    @Test
    public void execute_isActiveFalse() throws Exception {
        // setup
        when(mRadioSourceController.isActive()).thenReturn(false);

        // exercise
        mSelectRadioFavorite.execute(1,RadioBandType.AM,3);
        mSignal.await();

        // verify
        verify(mRadioSourceController,never()).selectFavorite(1,RadioBandType.AM,3);
    }

    @Test(expected = NullPointerException.class)
    public void execute_ArgNull() throws Exception {
        // exercise
        mSelectRadioFavorite.execute(1,null,3);
    }
}