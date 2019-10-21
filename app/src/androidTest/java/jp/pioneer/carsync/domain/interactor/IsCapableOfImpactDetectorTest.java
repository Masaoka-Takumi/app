package jp.pioneer.carsync.domain.interactor;

import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.component.ImpactDetector;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * IsCapableOfImpactDetectorのテスト.
 */
@RunWith(Theories.class)
public class IsCapableOfImpactDetectorTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks IsCapableOfImpactDetector mIsCapableOfImpactDetector;
    @Mock ImpactDetector mImpactDetector;

    static class Fixture {
        float maximumRange;
        boolean expected;

        Fixture(float maximumRange, boolean expected) {
            this.maximumRange = maximumRange;
            this.expected = expected;
        }
    }

    @DataPoints
    public static final Fixture[] FIXTURES = new Fixture[] {
            new Fixture(29.9f, false),
            new Fixture(30.0f, true),
            new Fixture(30.1f, true)
    };

    @Theory
    public void execute(Fixture fixture) throws Exception {
        // setup
        when(mImpactDetector.getMaximumRange()).thenReturn(fixture.maximumRange);

        // exercise
        boolean actual = mIsCapableOfImpactDetector.execute();

        // verify
        assertThat(actual, is(fixture.expected));
    }
}