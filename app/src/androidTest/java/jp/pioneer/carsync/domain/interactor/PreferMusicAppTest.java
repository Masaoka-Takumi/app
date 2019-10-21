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
import jp.pioneer.carsync.domain.model.MusicApp;
import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/07/27.
 */
public class PreferMusicAppTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PreferMusicApp mPreferMusicApp;
    @Mock ApplicationInfoRepository mRepository;
    @Mock AppSharedPreference mPreference;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void getInstalledTargetAppList() throws Exception {
        // setup
        List<ApplicationInfo> expected = Lists.newArrayList(mock(ApplicationInfo.class), mock(ApplicationInfo.class));
        ArgumentCaptor<String[]> cap = ArgumentCaptor.forClass(String[].class);
        when(mRepository.get(cap.capture())).thenReturn(expected);

        // exercise
        List<ApplicationInfo> actual = mPreferMusicApp.getInstalledTargetAppList();

        // verify
        assertThat(actual, contains(expected.toArray()));

        String[] packageNames = Stream.of(MusicApp.values())
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
        when(mPreference.getMusicApps()).thenReturn(expected);

        // exercise
        AppSharedPreference.Application[] actual = mPreferMusicApp.getSelectedAppList();

        // verify
        assertThat(actual, is(expected));
    }

    @Test
    public void setSelectedAppList() throws Exception {
        // exercise
        mPreferMusicApp.setSelectedAppList(new AppSharedPreference.Application[] {
                new AppSharedPreference.Application("a", "b"),
                new AppSharedPreference.Application("1", "2"),
        });

        // verify
        verify(mPreference).setMusicApps(new AppSharedPreference.Application[] {
                new AppSharedPreference.Application("a", "b"),
                new AppSharedPreference.Application("1", "2"),
        });
    }

}