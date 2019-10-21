package jp.pioneer.carsync.domain.interactor;

import android.location.Address;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.pioneer.carsync.domain.component.Geocoder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/11.
 */
public class GetAddressFromLocationNameTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks GetAddressFromLocationName mGetAddressFromLocationName;
    @Mock Geocoder mGeocoder;

    @Mock List<Address> mAddresses;
    GetAddressFromLocationName.Callback mCallback;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mCallback = mock(GetAddressFromLocationName.Callback.class);
    }

    @Test
    public void executeOnSuccess() throws Exception {
        // setup
        Address address = mock(Address.class);
        when(mAddresses.isEmpty()).thenReturn(false);
        when(mAddresses.get(0)).thenReturn(address);

        doAnswer(
                invocationOnMock -> {
                    Geocoder.Callback Callback = (Geocoder.Callback)invocationOnMock.getArguments()[2];
                    Callback.onResult(mAddresses);
                    return null;
                }).when(mGeocoder).getFromLocationName(anyString(), anyInt(), any(Geocoder.Callback.class));

        // exercise
        mGetAddressFromLocationName.execute("TEST", mCallback);

        // verify
        verify(mGeocoder).getFromLocationName(eq("TEST"),eq(1),any(Geocoder.Callback.class));
        verify(mCallback).onSuccess(address);
    }

    @Test
    public void executeOnErrorNotAvailable() throws Exception {
        // setup
        List<Address> addresses = null;

        doAnswer(
                invocationOnMock -> {
                    Geocoder.Callback Callback = (Geocoder.Callback)invocationOnMock.getArguments()[2];
                    Callback.onResult(addresses);
                    return null;
                }).when(mGeocoder).getFromLocationName(anyString(), anyInt(), any(Geocoder.Callback.class));

        // exercise
        mGetAddressFromLocationName.execute("TEST", mCallback);

        // verify
        verify(mGeocoder).getFromLocationName(eq("TEST"),eq(1),any(Geocoder.Callback.class));
        verify(mCallback).onError(GetAddressFromLocationName.Error.NOT_AVAILABLE);
    }

    @Test
    public void executeOnErrorNotFound() throws Exception {
        // setup
        Address address = mock(Address.class);
        when(mAddresses.isEmpty()).thenReturn(true);

        doAnswer(
                invocationOnMock -> {
                    Geocoder.Callback Callback = (Geocoder.Callback)invocationOnMock.getArguments()[2];
                    Callback.onResult(mAddresses);
                    return null;
                }).when(mGeocoder).getFromLocationName(anyString(), anyInt(), any(Geocoder.Callback.class));

        // exercise
        mGetAddressFromLocationName.execute("TEST", mCallback);

        // verify
        verify(mGeocoder).getFromLocationName(eq("TEST"),eq(1),any(Geocoder.Callback.class));
        verify(mCallback).onError(GetAddressFromLocationName.Error.NOT_FOUND);
    }

    @Test(expected = NullPointerException.class)
    public void executeArgNullLocationName() throws Exception {
        // setup
        String locationName = null;

        // exercise
        mGetAddressFromLocationName.execute(locationName, mCallback);
    }

    @Test(expected = NullPointerException.class)
    public void executeArgNullCallback() throws Exception {
        // setup
        GetAddressFromLocationName.Callback callback = null;

        // exercise
        mGetAddressFromLocationName.execute("TEST", callback);
    }

}