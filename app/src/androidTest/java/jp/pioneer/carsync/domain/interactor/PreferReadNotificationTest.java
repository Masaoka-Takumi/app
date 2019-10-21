package jp.pioneer.carsync.domain.interactor;

import android.content.pm.ApplicationInfo;

import com.annimon.stream.Stream;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.List;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.MessagingApp;
import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

/**
 * PreferReadNotificationのテスト.
 */
public class PreferReadNotificationTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PreferReadNotification mPreferReadNotification;
    @Mock ApplicationInfoRepository mRepository;
    @Mock AppSharedPreference mPreference;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void isEnabledTrue() throws Exception {
        // setup
        when(mPreference.isReadNotificationEnabled()).thenReturn(true);

        // exercise
        boolean actual = mPreferReadNotification.isEnabled();

        // verify
        assertThat(actual, is(true));
    }

    @Test
    public void isEnabledFalse() throws Exception {
        // setup
        when(mPreference.isReadNotificationEnabled()).thenReturn(false);

        // exercise
        boolean actual = mPreferReadNotification.isEnabled();

        // verify
        assertThat(actual, is(false));
    }

    @Test
    public void setEnabledTrue() throws Exception {
        // exercise
        mPreferReadNotification.setEnabled(true);

        // verify
        verify(mPreference).setReadNotificationEnabled(true);
    }

    @Test
    public void setEnabledFalse() throws Exception {
        // exercise
        mPreferReadNotification.setEnabled(false);

        // verify
        verify(mPreference).setReadNotificationEnabled(false);
    }

    @Test
    public void getInstalledTargetAppList() throws Exception {
        // setup
        List<ApplicationInfo> expected = Lists.newArrayList(mock(ApplicationInfo.class), mock(ApplicationInfo.class));
        ArgumentCaptor<String[]> cap = ArgumentCaptor.forClass(String[].class);
        when(mRepository.get(cap.capture())).thenReturn(expected);

        // exercise
        List<ApplicationInfo> actual = mPreferReadNotification.getInstalledTargetAppList();

        // verify
        assertThat(actual, contains(expected.toArray()));

        String[] packageNames = Stream.of(MessagingApp.values())
                .map(app -> app.getPackageName())
                .toArray(String[]::new);
        assertThat(Arrays.asList(cap.getValue()), containsInAnyOrder(packageNames));
    }

    @Test
    public void getSelectedAppList() throws Exception {
        // setup
        AppSharedPreference.Application[] expected = new AppSharedPreference.Application[] {
                mock(AppSharedPreference.Application.class),
                mock(AppSharedPreference.Application.class)
        };
        when(mPreference.getReadNotificationApps()).thenReturn(expected);

        // exercise
        AppSharedPreference.Application[] actual = mPreferReadNotification.getSelectedAppList();

        // verify
        assertThat(actual, is(expected));
    }

    @Test
    public void setSelectedAppList() throws Exception {
        // exercise
        mPreferReadNotification.setSelectedAppList(new AppSharedPreference.Application[] {
                new AppSharedPreference.Application("a", "b"),
                new AppSharedPreference.Application("1", "2"),
        });

        // verify
        verify(mPreference).setReadNotificationApps(new AppSharedPreference.Application[] {
                new AppSharedPreference.Application("a", "b"),
                new AppSharedPreference.Application("1", "2"),
        });
    }
}