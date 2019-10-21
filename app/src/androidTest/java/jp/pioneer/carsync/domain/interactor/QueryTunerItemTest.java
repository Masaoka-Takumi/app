package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.CursorLoader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.content.DeleteParams;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.content.UpdateParams;
import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;
import jp.pioneer.carsync.domain.repository.FavoriteRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/26.
 */
public class QueryTunerItemTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks QueryTunerItem mQueryTunerItem;
    @Mock Handler mHandler;
    @Mock FavoriteRepository mFavoriteRepository;
    @Mock CarDeviceMediaRepository mCarDeviceMediaRepository;

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
    public void getPresetList() throws Exception {
        // setup
        MediaSourceType mediaSourceType = MediaSourceType.RADIO;
        BandType bandType = mock(BandType.class);
        CursorLoader expected = mock(CursorLoader.class);
        when(mCarDeviceMediaRepository.getPresetChannelList(eq(mediaSourceType),eq(bandType))).thenReturn(expected);

        // exercise
        CursorLoader actual =  mQueryTunerItem.getPresetList(mediaSourceType,bandType);

        // verify
        assertThat(actual,is(expected));
    }

    @Test(expected = NullPointerException.class)
    public void getPresetList_ArgSourceTypeNull() throws Exception {
        // setup
        MediaSourceType mediaSourceType = null;
        BandType bandType = mock(BandType.class);

        // exercise
        CursorLoader cursorLoader =  mQueryTunerItem.getPresetList(mediaSourceType,bandType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPresetList_ArgSourceType_NotTunerSource() throws Exception {
        // setup
        MediaSourceType mediaSourceType = MediaSourceType.APP_MUSIC;
        BandType bandType = mock(BandType.class);
        CursorLoader expected = mock(CursorLoader.class);
        when(mCarDeviceMediaRepository.getPresetChannelList(eq(mediaSourceType),eq(bandType))).thenReturn(expected);

        // exercise
        CursorLoader actual =  mQueryTunerItem.getPresetList(mediaSourceType,bandType);
    }

    @Test
    public void getFavoriteList() throws Exception {
        // setup
        QueryParams queryParams = mock(QueryParams.class);
        CursorLoader expected = mock(CursorLoader.class);
        when(mFavoriteRepository.get(eq(queryParams))).thenReturn(expected);

        // exercise
        CursorLoader actual =  mQueryTunerItem.getFavoriteList(queryParams);

        // verify
        assertThat(actual,is(expected));
    }

    @Test(expected = NullPointerException.class)
    public void getFavoriteList_ArgNull() throws Exception {
        // setup
        QueryParams queryParams = null;

        // exercise
        CursorLoader cursorLoader =  mQueryTunerItem.getFavoriteList(queryParams);
    }

    @Test
    public void registerFavorite() throws Exception {
        // setup
        UpdateParams updateParams = mock(UpdateParams.class);

        // exercise
        mQueryTunerItem.registerFavorite(updateParams);
        mSignal.await();

        // verify
        verify(mFavoriteRepository).upsert(updateParams);
    }

    @Test(expected = NullPointerException.class)
    public void registerFavorite_ArgNull() throws Exception {
        // setup
        UpdateParams updateParams = null;

        // exercise
        mQueryTunerItem.registerFavorite(updateParams);
    }

    @Test
    public void unregisterFavorite() throws Exception {
        // setup
        DeleteParams deleteParams = mock(DeleteParams.class);

        // exercise
        mQueryTunerItem.unregisterFavorite(deleteParams);
        mSignal.await();

        // verify
        verify(mFavoriteRepository).delete(deleteParams);
    }

    @Test(expected = NullPointerException.class)
    public void unregisterFavorite_ArgNull() throws Exception {
        // setup
        DeleteParams deleteParams = null;

        // exercise
        mQueryTunerItem.unregisterFavorite(deleteParams);
    }

}