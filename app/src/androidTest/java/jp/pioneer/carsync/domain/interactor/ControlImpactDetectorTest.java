package jp.pioneer.carsync.domain.interactor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.component.ImpactDetector;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/20.
 */
public class ControlImpactDetectorTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ControlImpactDetector mControlImpactDetector;
    @Mock ImpactDetector mImpactDetector;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void startDetectionTrue() throws Exception {
        // setup
        when(mImpactDetector.startDetection(anyFloat(),anyFloat())).thenReturn(true);

        // verify
        assertThat(mControlImpactDetector.startDetection(),is(true));

    }

    @Test
    public void startDetectionFalse() throws Exception {
        // setup
        when(mImpactDetector.startDetection(anyFloat(),anyFloat())).thenReturn(false);

        // verify
        assertThat(mControlImpactDetector.startDetection(),is(false));

    }

    @Test
    public void stopDetection() throws Exception {
        // exercise
        mControlImpactDetector.stopDetection();

        // verify
        verify(mImpactDetector).stopDetection();
    }

}