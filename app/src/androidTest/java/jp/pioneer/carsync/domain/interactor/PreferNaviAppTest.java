package jp.pioneer.carsync.domain.interactor;

import android.content.pm.ApplicationInfo;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.NaviApp;
import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GetNaviApplicationInfoListのテスト.
 */
public class PreferNaviAppTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PreferNaviApp mPreferNaviApp;
    @Mock ApplicationInfoRepository mRepository;
    @Mock AppSharedPreference mPreference;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void getInstalledTargetAppList() throws Exception {
        // setup
        List<ApplicationInfo> expect = Lists.newArrayList(mock(ApplicationInfo.class), mock(ApplicationInfo.class));
        ArgumentCaptor<String[]> cap = ArgumentCaptor.forClass(String[].class);
        when(mRepository.get(cap.capture())).thenReturn(expect);

        // exercise
        List<ApplicationInfo> actual = mPreferNaviApp.getInstalledTargetAppList();

        // verify
        assertThat(actual, contains(expect.toArray()));

        List<String> packageNames = new ArrayList<>();
        for (NaviApp naviApp : NaviApp.values()) {
            packageNames.add(naviApp.getPackageName());
        }
        assertThat(Arrays.asList(cap.getValue()), containsInAnyOrder(packageNames.toArray()));
    }


    @Test
    public void getSelectedApp() throws Exception {
        // setup
        AppSharedPreference.Application expected = mock(AppSharedPreference.Application.class);
        when(mPreferNaviApp.getSelectedApp()).thenReturn(expected);

        // exercise
        AppSharedPreference.Application actual = mPreferNaviApp.getSelectedApp();

        // verify
        assertThat(actual, is(expected));
    }

    @Test
    public void setSelectedApp() throws Exception {
        // exercise
        mPreferNaviApp.setSelectedApp(new AppSharedPreference.Application("a", "b"));

        // verify
        verify(mPreference).setNavigationApp(new AppSharedPreference.Application("a", "b"));
    }

    @Test(expected = NullPointerException.class)
    public void setSelectedAppArgNull() throws Exception {
        // setup
        AppSharedPreference.Application application = null;

        // exercise
        mPreferNaviApp.setSelectedApp(application);
    }
}