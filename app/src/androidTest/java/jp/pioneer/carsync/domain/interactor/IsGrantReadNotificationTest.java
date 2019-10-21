package jp.pioneer.carsync.domain.interactor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.component.NotificationProvider;

import static android.support.test.InstrumentationRegistry.getTargetContext;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/14.
 */
public class IsGrantReadNotificationTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks IsGrantReadNotification mIsGrantReadNotification;
    @Mock NotificationProvider mNotificationProvider;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void executeTrue() throws Exception {
        // setup
        when(mNotificationProvider.isGrantReadNotification()).thenReturn(true);

        // exercise
        boolean actual = mIsGrantReadNotification.execute();

        // verify
        assertThat(actual,is(true));

    }

    @Test
    public void executeFalse() throws Exception {
        // setup
        when(mNotificationProvider.isGrantReadNotification()).thenReturn(false);

        // exercise
        boolean actual = mIsGrantReadNotification.execute();

        // verify
        assertThat(actual,is(false));

    }

}