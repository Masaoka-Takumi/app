package jp.pioneer.carsync.domain.internal;

import android.service.notification.StatusBarNotification;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.component.NotificationProvider;
import jp.pioneer.carsync.domain.event.ReadNotificationPostedEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationRemovedEvent;
import jp.pioneer.carsync.domain.model.Notification;
import jp.pioneer.carsync.domain.model.NotificationFactory;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/14.
 */
public class ReadNotificationListenerTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ReadNotificationListener mReadNotificationListener;
    @Mock EventBus mEventBus;
    @Mock NotificationProvider mProvider;
    @Mock NotificationFactory mFactory;
    @Mock AppSharedPreference mPreference;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void initialize() throws Exception {
        // exercise
        mReadNotificationListener.initialize();

        // verify
        verify(mProvider).setOnPostedListener(mReadNotificationListener);
        verify(mProvider).setOnRemovedListener(mReadNotificationListener);
    }

    @Test
    public void onPosted_isReadNotificationEnabledReturnTrue_createReturnNotNullNotification() throws Exception {
        // setup
        when(mPreference.isReadNotificationEnabled()).thenReturn(true);
        when(mFactory.create(any(StatusBarNotification.class))).thenReturn(mock(Notification.class));

        // exercise
        mReadNotificationListener.onPosted(mock(StatusBarNotification.class));

        // verify
        verify(mPreference).isReadNotificationEnabled();
        verify(mFactory).create(any(StatusBarNotification.class));
        verify(mEventBus).post(any(ReadNotificationPostedEvent.class));

    }

    @Test
    public void onPosted_isReadNotificationEnabledReturnTrue_createReturnNullNotification() throws Exception {
        // setup
        when(mPreference.isReadNotificationEnabled()).thenReturn(true);
        when(mFactory.create(any(StatusBarNotification.class))).thenReturn(null);

        // exercise
        mReadNotificationListener.onPosted(mock(StatusBarNotification.class));

        // verify
        verify(mPreference).isReadNotificationEnabled();
        verify(mFactory).create(any(StatusBarNotification.class));
        verify(mEventBus,never()).post(any(ReadNotificationPostedEvent.class));

    }

    @Test
    public void onPosted_isReadNotificationEnabledReturnFalse() throws Exception {
        // setup
        when(mPreference.isReadNotificationEnabled()).thenReturn(false);

        // exercise
        mReadNotificationListener.onPosted(mock(StatusBarNotification.class));

        // verify
        verify(mPreference).isReadNotificationEnabled();
        verify(mFactory,never()).create(any(StatusBarNotification.class));
        verify(mEventBus,never()).post(any(ReadNotificationPostedEvent.class));

    }

    @Test(expected = NullPointerException.class)
    public void onPostedArgNull() throws Exception {
        // setup
        StatusBarNotification statusBarNotification = null;

        // exercise
        mReadNotificationListener.onPosted(statusBarNotification);

    }

    @Test
    public void onRemoved_isReadNotificationEnabledReturnTrue_createReturnNotNullNotification() throws Exception {
        // setup
        when(mPreference.isReadNotificationEnabled()).thenReturn(true);
        when(mFactory.create(any(StatusBarNotification.class))).thenReturn(mock(Notification.class));

        // exercise
        mReadNotificationListener.onRemoved(mock(StatusBarNotification.class));

        // verify
        verify(mPreference).isReadNotificationEnabled();
        verify(mFactory).create(any(StatusBarNotification.class));
        verify(mEventBus).post(any(ReadNotificationRemovedEvent.class));

    }

    @Test
    public void onRemoved_isReadNotificationEnabledReturnTrue_createReturnNullNotification() throws Exception {
        // setup
        when(mPreference.isReadNotificationEnabled()).thenReturn(true);
        when(mFactory.create(any(StatusBarNotification.class))).thenReturn(null);

        // exercise
        mReadNotificationListener.onRemoved(mock(StatusBarNotification.class));

        // verify
        verify(mPreference).isReadNotificationEnabled();
        verify(mFactory).create(any(StatusBarNotification.class));
        verify(mEventBus,never()).post(any(ReadNotificationPostedEvent.class));

    }

    @Test
    public void onRemoved_isReadNotificationEnabledReturnFalse() throws Exception {
        // setup
        when(mPreference.isReadNotificationEnabled()).thenReturn(false);

        // exercise
        mReadNotificationListener.onRemoved(mock(StatusBarNotification.class));

        // verify
        verify(mPreference).isReadNotificationEnabled();
        verify(mFactory,never()).create(any(StatusBarNotification.class));
        verify(mEventBus,never()).post(any(ReadNotificationPostedEvent.class));

    }

    @Test(expected = NullPointerException.class)
    public void onRemovedArgNull() throws Exception {
        // setup
        StatusBarNotification statusBarNotification = null;

        // exercise
        mReadNotificationListener.onRemoved(statusBarNotification);

    }

}