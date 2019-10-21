package jp.pioneer.carsync.domain.interactor;

import android.service.notification.StatusBarNotification;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.component.NotificationProvider;
import jp.pioneer.carsync.domain.model.Notification;
import jp.pioneer.carsync.domain.model.NotificationFactory;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/17.
 */
public class GetReadNotificationListTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks GetReadNotificationList mGetReadNotificationList;
    @Mock NotificationProvider mInteractor;
    @Mock NotificationFactory mFactory;
    @Mock AppSharedPreference mPreference;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void execute_isReadNotificationEnabledReturnTrue() throws Exception {
        // setup
        Notification expect = mock(Notification.class);
        StatusBarNotification statusBarNotification_1 = mock(StatusBarNotification.class);
        StatusBarNotification statusBarNotification_2 = mock(StatusBarNotification.class);

        StatusBarNotification[] statusBarNotifications = {statusBarNotification_1,statusBarNotification_2};

        when(mPreference.isReadNotificationEnabled()).thenReturn(true);
        when(mInteractor.getStatusBarNotifications(any(Comparator.class))).thenReturn(statusBarNotifications);
        when(mFactory.create(any(StatusBarNotification.class))).thenReturn(expect);

        // exercise
        List<Notification> actual = mGetReadNotificationList.execute();

        // verify
        assertThat(actual.get(0),is(expect));

    }

    @Test
    public void execute_isReadNotificationEnabledReturnFalse() throws Exception {
        // setup
        List<Notification> expect = new ArrayList<>();
        when(mPreference.isReadNotificationEnabled()).thenReturn(false);

        // exercise
        List<Notification> actual = mGetReadNotificationList.execute();

        // verify
        verify(mInteractor,never()).getStatusBarNotifications(any(Comparator.class));
        verify(mFactory,never()).create(any(StatusBarNotification.class));
        assertThat(actual,is(expect));

    }

}