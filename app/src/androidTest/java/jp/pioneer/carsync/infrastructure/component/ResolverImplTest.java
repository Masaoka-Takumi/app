package jp.pioneer.carsync.infrastructure.component;

import android.app.Activity;
import android.content.IntentSender;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by NSW00_008320 on 2017/04/20.
 */
public class ResolverImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ResolverImpl mResolverImpl = new ResolverImpl(new Status(CommonStatusCodes.RESOLUTION_REQUIRED));

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test(expected = NullPointerException.class)
    public void ResolverImplArgNull() throws Exception {
        // setup
        Status status = null;

        // exercise
        ResolverImpl resolver = new ResolverImpl(status);

    }

    @Test
    public void startResolutionForResult() throws Exception {
        // setup
        Activity activityForComparison = mock(Activity.class);
        ResolverImpl resolver = new ResolverImpl(new Status(CommonStatusCodes.RESOLUTION_REQUIRED)){
            @Override
            void callStartResolutionForResult(Activity activity, int requestCode) throws IntentSender.SendIntentException {
                assertThat(activity,is(activityForComparison));
                assertThat(requestCode,is(10));
            }
        };

        // exercise
        resolver.startResolutionForResult(activityForComparison,10);

    }

    @Test(expected = NullPointerException.class)
    public void startResolutionForResultArgNull() throws Exception {
        // setup
        Activity activity = null;

        // exercise
        mResolverImpl.startResolutionForResult(activity,10);

    }

}