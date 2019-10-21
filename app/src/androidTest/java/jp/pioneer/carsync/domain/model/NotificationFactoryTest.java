package jp.pioneer.carsync.domain.model;

import android.service.notification.StatusBarNotification;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Map;

import javax.inject.Provider;

import jp.pioneer.carsync.application.content.AppSharedPreference;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/14.
 */
public class NotificationFactoryTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks NotificationFactory mNotificationFactory;
    @Mock AppSharedPreference mPreference;
    @Mock Map<MessagingApp, Provider<? extends AbstractNotification>> mNotificationProviders;

    StatusBarNotification mStatusBarNotification;
    Provider mProvider;
    AbstractNotification mAbstractNotification;

    private static final String EXIST_APP_NAME = "com.facebook.katana";
    private static final String NOT_EXIST_APP_NAME = "TEST";
    private static final AppSharedPreference.Application[] EXIST_APPLICATIONS ={new AppSharedPreference.Application(EXIST_APP_NAME,"")};
    private static final AppSharedPreference.Application[] NOT_EXIST_APPLICATIONS ={new AppSharedPreference.Application(NOT_EXIST_APP_NAME,"")};

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        mStatusBarNotification = mock(StatusBarNotification.class);
        mProvider = mock(Provider.class);
        mAbstractNotification = mock(AbstractNotification.class);
    }

    @Test
    public void create_HappyPath() throws Exception {
        // setup
        AbstractNotification expect = mock(AbstractNotification.class);

        when(mStatusBarNotification.getPackageName()).thenReturn(EXIST_APP_NAME);
        when(mPreference.getReadNotificationApps()).thenReturn(EXIST_APPLICATIONS);
        when(mNotificationProviders.get(any(MessagingApp.class))).thenReturn(mProvider);
        when(mProvider.get()).thenReturn(mAbstractNotification);
        when(mAbstractNotification.setStatusBarNotification(mStatusBarNotification)).thenReturn(expect);

        when(expect.isReadTarget()).thenReturn(true);

        // exercise
        Notification actual = mNotificationFactory.create(mStatusBarNotification);

        // verify
        assertThat(actual,is(expect));

    }

    @Test
    public void create_isReadTargetReturnFalse() throws Exception {
        // setup
        AbstractNotification abstractNotification = mock(AbstractNotification.class);

        when(mStatusBarNotification.getPackageName()).thenReturn(EXIST_APP_NAME);
        when(mPreference.getReadNotificationApps()).thenReturn(EXIST_APPLICATIONS);
        when(mNotificationProviders.get(any(MessagingApp.class))).thenReturn(mProvider);
        when(mProvider.get()).thenReturn(mAbstractNotification);
        when(mAbstractNotification.setStatusBarNotification(mStatusBarNotification)).thenReturn(abstractNotification);

        when(abstractNotification.isReadTarget()).thenReturn(false);

        // exercise
        Notification actual = mNotificationFactory.create(mStatusBarNotification);

        // verify
        assertThat(actual,is(nullValue()));

    }

    @Test
    public void create_containsReturnFalse() throws Exception {
        // setup
        when(mStatusBarNotification.getPackageName()).thenReturn(EXIST_APP_NAME);
        when(mPreference.getReadNotificationApps()).thenReturn(NOT_EXIST_APPLICATIONS);

        // exercise
        Notification actual = mNotificationFactory.create(mStatusBarNotification);

        // verify
        assertThat(actual,is(nullValue()));

    }

    @Test
    public void create_fromPackageNameNoThrowReturnNull() throws Exception {
        // setup
        when(mStatusBarNotification.getPackageName()).thenReturn(NOT_EXIST_APP_NAME);

        // exercise
        Notification actual = mNotificationFactory.create(mStatusBarNotification);

        // verify
        assertThat(actual,is(nullValue()));

    }

    @Test(expected = NullPointerException.class)
    public void createArgNull() throws Exception {
        // setup
        StatusBarNotification statusBarNotification = null;

        // exercise
        Notification actual = mNotificationFactory.create(statusBarNotification);
    }

}