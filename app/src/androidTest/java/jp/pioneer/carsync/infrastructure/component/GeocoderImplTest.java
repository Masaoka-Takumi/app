package jp.pioneer.carsync.infrastructure.component;

import android.content.Context;
import android.location.Address;
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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.Geocoder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/11.
 */
public class GeocoderImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks GeocoderImpl mGeocoderImpl = new GeocoderImpl(){
        @Override
        List<Address> executeGeocoderGetFromLocationName(android.location.Geocoder geocoder, String locationName, int maxResults) throws IOException {
            assertThat(locationName,is(mLocationName));
            assertThat(maxResults,is(mMaxResults));

            if(isException){
                throw new IOException();
            }
            else {
                return mAddresses;
            }
        }
    };
    @Mock Context mContext;
    @Mock Handler mHandler;
    @Mock Geocoder.Callback mCallback;
    @Mock List<Address> mAddresses;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);
    String mLocationName = "TEST";
    int mMaxResults = 5;
    boolean isException;

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
    public void getFromLocationName() throws Exception {
        // setup
        isException = false;

        // exercise
        mGeocoderImpl.getFromLocationName(mLocationName,mMaxResults,mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onResult(mAddresses);

    }

    @Test
    public void getFromLocationNameCatchIOException() throws Exception {
        //setup
        isException = true;

        // exercise
        mGeocoderImpl.getFromLocationName(mLocationName,mMaxResults,mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onResult(null);

    }

    @Test(expected = NullPointerException.class)
    public void getFromLocationNameArgLocationNameNull() throws Exception {
        // setup
        String locationName = null;

        // exercise
        mGeocoderImpl.getFromLocationName(locationName,mMaxResults,mCallback);

    }

    @Test(expected = IllegalArgumentException.class)
    public void getFromLocationNameArgMaxResultSizeMin() throws Exception {
        // setup
        int maxResults = 0;

        // exercise
        mGeocoderImpl.getFromLocationName(mLocationName,maxResults,mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onResult(mAddresses);

    }

    @Test(expected = NullPointerException.class)
    public void getFromLocationNameArgCallbackNull() throws Exception {
        // setup
        Geocoder.Callback callback = null;

        // exercise
        mGeocoderImpl.getFromLocationName(mLocationName,mMaxResults,callback);
        mSignal.await();

        // verify
        verify(mCallback).onResult(mAddresses);

    }

}