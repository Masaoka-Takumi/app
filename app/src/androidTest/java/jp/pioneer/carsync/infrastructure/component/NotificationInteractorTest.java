package jp.pioneer.carsync.infrastructure.component;

import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.Comparator;

import jp.pioneer.carsync.domain.component.NotificationProvider;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/18.
 */
public class NotificationInteractorTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();

    @InjectMocks NotificationInteractor mNotificationInteractor = new NotificationInteractor(){
        @Override
        String getEnabledNotificationListeners() {
            return mEnabledNotificationListeners;
        }
    };

    @Mock Context mContext;

    private String mEnabledNotificationListeners;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test(expected = NullPointerException.class)
    public void attachNotificationListenerServiceArgNul() throws Exception {
        // setup
        NotificationListenerService notificationListenerService = null;

        // exercise
        mNotificationInteractor.attachNotificationListenerService(notificationListenerService);
    }

    @Test(expected = NullPointerException.class)
    public void onNotificationPostedArgNull() throws Exception {
        // setup
        StatusBarNotification statusBarNotification = null;

        // exercise
        mNotificationInteractor.onNotificationPosted(statusBarNotification);
    }

    @Test(expected = NullPointerException.class)
    public void onNotificationRemovedArgNull() throws Exception {
        // setup
        StatusBarNotification statusBarNotification = null;

        // exercise
        mNotificationInteractor.onNotificationRemoved(statusBarNotification);
    }

    @Test
    public void isGrantReadNotification_MultiListener_StartWithTrue() throws Exception {
        // setup
        mEnabledNotificationListeners = "jp.pioneer.carsync/jp.pioneer.carsync.infrastructure.component.NotificationListenerServiceImpl" +
                ":jp.pioneer.carsync/jp.pioneer.carsync.infrastructure.component.NotificationListenerServiceImpl";
        when(mContext.getPackageName()).thenReturn("jp.pioneer.carsync");

        // verify
        assertThat(mNotificationInteractor.isGrantReadNotification(),is(true));
        verify(mContext).getPackageName();

    }

    @Test
    public void isGrantReadNotification_MultiListener_StartWithFalse() throws Exception {
        mEnabledNotificationListeners = "jp.pioneer.Test/jp.pioneer.carsync.infrastructure.component.NotificationListenerServiceImpl" +
                ":jp.pioneer.Test/jp.pioneer.carsync.infrastructure.component.NotificationListenerServiceImpl";
        when(mContext.getPackageName()).thenReturn("jp.pioneer.carsync");

        // verify
        assertThat(mNotificationInteractor.isGrantReadNotification(),is(false));
        verify(mContext).getPackageName();

    }

    @Test
    public void isGrantReadNotification_SingleListener_StartWithTrue() throws Exception {
        mEnabledNotificationListeners = "jp.pioneer.carsync/jp.pioneer.carsync.infrastructure.component.NotificationListenerServiceImpl";
        when(mContext.getPackageName()).thenReturn("jp.pioneer.carsync");

        // verify
        assertThat(mNotificationInteractor.isGrantReadNotification(),is(true));
        verify(mContext).getPackageName();

    }

    @Test
    public void isGrantReadNotification_SingleListener_StartWithFalse() throws Exception {
        mEnabledNotificationListeners = "jp.pioneer.Test/jp.pioneer.carsync.infrastructure.component.NotificationListenerServiceImpl";
        when(mContext.getPackageName()).thenReturn("jp.pioneer.carsync");

        // verify
        assertThat(mNotificationInteractor.isGrantReadNotification(),is(false));
        verify(mContext).getPackageName();

    }

    @Test
    public void isGrantReadNotification_NonListener() throws Exception {
        mEnabledNotificationListeners = "";
        when(mContext.getPackageName()).thenReturn("jp.pioneer.carsync");

        // verify
        assertThat(mNotificationInteractor.isGrantReadNotification(),is(false));
        verify(mContext,never()).getPackageName();

    }

    @Test
    public void getStatusBarNotifications_ServiceNotNull_ComparatorNotNull() throws Exception {
        // setup
        StatusBarNotification statusBarNotification_1 = mock(StatusBarNotification.class);
        StatusBarNotification statusBarNotification_2 = mock(StatusBarNotification.class);
        StatusBarNotification statusBarNotification_3 = mock(StatusBarNotification.class);
        when(statusBarNotification_1.getPostTime()).thenReturn(10000l); // 1
        when(statusBarNotification_2.getPostTime()).thenReturn(100l);   // 3
        when(statusBarNotification_3.getPostTime()).thenReturn(1000l);  // 2

        StatusBarNotification[] expected = {
                statusBarNotification_1,
                statusBarNotification_3,
                statusBarNotification_2,
        };

        StatusBarNotification[] statusBarNotifications = {
                statusBarNotification_1,
                statusBarNotification_2,
                statusBarNotification_3,
        };

        NotificationListenerService notificationListenerService = mock(NotificationListenerService.class);
        when(notificationListenerService.getActiveNotifications()).thenReturn(statusBarNotifications);
        Comparator<StatusBarNotification> comparator = (lhs, rhs) -> (int) (rhs.getPostTime() - lhs.getPostTime());
        mNotificationInteractor.attachNotificationListenerService(notificationListenerService);

        // exercise
        StatusBarNotification[] actual = mNotificationInteractor.getStatusBarNotifications(comparator);

        // verify
        // ソートされていることを確認
        assertThat(actual[0],is(expected[0]));
        assertThat(actual[1],is(expected[1]));
        assertThat(actual[2],is(expected[2]));
    }

    @Test
    public void getStatusBarNotifications_ServiceNotNull_ComparatorNull() throws Exception {
        // setup
        StatusBarNotification statusBarNotification_1 = mock(StatusBarNotification.class);
        StatusBarNotification statusBarNotification_2 = mock(StatusBarNotification.class);
        StatusBarNotification statusBarNotification_3 = mock(StatusBarNotification.class);
        when(statusBarNotification_1.getPostTime()).thenReturn(10000l); // 1
        when(statusBarNotification_2.getPostTime()).thenReturn(100l);   // 3
        when(statusBarNotification_3.getPostTime()).thenReturn(1000l);  // 2

        StatusBarNotification[] expected = {
                statusBarNotification_1,
                statusBarNotification_2,
                statusBarNotification_3,
        };

        NotificationListenerService notificationListenerService = mock(NotificationListenerService.class);
        when(notificationListenerService.getActiveNotifications()).thenReturn(expected);
        Comparator<StatusBarNotification> comparator = null;
        mNotificationInteractor.attachNotificationListenerService(notificationListenerService);

        // exercise
        StatusBarNotification[] actual = mNotificationInteractor.getStatusBarNotifications(comparator);

        // verify
        // ソートされていないことを確認
        assertThat(actual[0],is(expected[0]));
        assertThat(actual[1],is(expected[1]));
        assertThat(actual[2],is(expected[2]));
    }

    @Test
    public void getStatusBarNotifications_ServiceNull() throws Exception {
        // setup
        Comparator<StatusBarNotification> comparator = null;
        mNotificationInteractor.detachNotificationListenerService();

        // exercise
        StatusBarNotification[] actual = mNotificationInteractor.getStatusBarNotifications(comparator);

        // verify
        assertThat(Arrays.asList(actual), containsInAnyOrder(new StatusBarNotification[0]));
    }

    @Test
    public void setOnPostedListenerArgNotNull() throws Exception {
        // setup
        NotificationProvider.OnPostedListener onPostedListener_Mock = mock(NotificationProvider.OnPostedListener.class);
        StatusBarNotification statusBarNotification = mock(StatusBarNotification.class);

        // exercise
        mNotificationInteractor.setOnPostedListener(onPostedListener_Mock);
        mNotificationInteractor.onNotificationPosted(statusBarNotification);

        // verify
        verify(onPostedListener_Mock).onPosted(statusBarNotification);
    }

    @Test
    public void setOnPostedListenerArgNull() throws Exception {
        // setup
        NotificationProvider.OnPostedListener onPostedListener_Mock = mock(NotificationProvider.OnPostedListener.class);
        NotificationProvider.OnPostedListener onPostedListener_Null = null;
        StatusBarNotification statusBarNotification = mock(StatusBarNotification.class);

        // exercise
        mNotificationInteractor.setOnPostedListener(onPostedListener_Mock);
        mNotificationInteractor.setOnPostedListener(onPostedListener_Null);
        mNotificationInteractor.onNotificationPosted(statusBarNotification);

        // verify
        verify(onPostedListener_Mock,never()).onPosted(statusBarNotification);

    }

    @Test
    public void setOnRemovedListenerArgNotNull() throws Exception {
        // setup
        NotificationProvider.OnRemovedListener onRemovedListener_Mock = mock(NotificationProvider.OnRemovedListener.class);
        StatusBarNotification statusBarNotification = mock(StatusBarNotification.class);

        // exercise
        mNotificationInteractor.setOnRemovedListener(onRemovedListener_Mock);
        mNotificationInteractor.onNotificationRemoved(statusBarNotification);

        // verify
        verify(onRemovedListener_Mock).onRemoved(statusBarNotification);
    }

    @Test
    public void setOnRemovedListenerArgNull() throws Exception {
        // setup
        NotificationProvider.OnRemovedListener onRemovedListener_Mock = mock(NotificationProvider.OnRemovedListener.class);
        NotificationProvider.OnRemovedListener onRemovedListener_Null = null;
        StatusBarNotification statusBarNotification = mock(StatusBarNotification.class);

        // exercise
        mNotificationInteractor.setOnRemovedListener(onRemovedListener_Mock);
        mNotificationInteractor.setOnRemovedListener(onRemovedListener_Null);
        mNotificationInteractor.onNotificationRemoved(statusBarNotification);

        // verify
        verify(onRemovedListener_Mock,never()).onRemoved(statusBarNotification);
    }

}