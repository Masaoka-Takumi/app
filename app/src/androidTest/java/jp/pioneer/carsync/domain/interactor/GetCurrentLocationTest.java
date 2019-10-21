package jp.pioneer.carsync.domain.interactor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.component.ImpactDetector;
import jp.pioneer.carsync.domain.component.LocationProvider;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/20.
 */
public class GetCurrentLocationTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks GetCurrentLocation mGetCurrentLocation;
    @Mock LocationProvider mLocationProvider;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void execute() throws Exception {
        // setup
        LocationProvider.Callback callback = mock(LocationProvider.Callback.class);

        // exercise
        mGetCurrentLocation.execute(callback);

        // verify
        verify(mLocationProvider).startGetCurrentLocation(LocationProvider.Priority.HIGH_ACCURACY,callback, LocationProvider.GetType.SINGLE);

    }

    @Test(expected = NullPointerException.class)
    public void executeArgCallbackNull() throws Exception {
        // setup
        LocationProvider.Callback callback = null;

        // exercise
        mGetCurrentLocation.execute(callback);

    }

}