package jp.pioneer.carsync.infrastructure.repository;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ApplicationInfoRepositoryImplのテスト.
 */
public class ApplicationInfoRepositoryImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ApplicationInfoRepositoryImpl mApplicationInfoRepositoryImpl;
    @Mock PackageManager mPackageManager;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void getsOk() throws Exception {
        // setup
        List<ApplicationInfo> expected = Lists.newArrayList(mock(ApplicationInfo.class));
        String[] packageNames = { "package1", "package2" };
        when(mPackageManager.getApplicationInfo("package1", PackageManager.GET_META_DATA))
                .thenThrow(new PackageManager.NameNotFoundException("package1"));
        when(mPackageManager.getApplicationInfo("package2", PackageManager.GET_META_DATA))
                .thenReturn(expected.get(0));

        // exercise
        List<ApplicationInfo> actual = mApplicationInfoRepositoryImpl.get(packageNames);

        // verify
        assertThat(actual, contains(expected.toArray()));
    }

    @Test(expected = NullPointerException.class)
    public void getsArgNull() throws Exception {
        // setup
        String[] packageNames = null;

        // exercise
        mApplicationInfoRepositoryImpl.get(packageNames);
    }

    @Test(expected = NullPointerException.class)
    public void getsArgContainNull() throws Exception {
        // setup
        String[] packageNames = { "package1", null };
        when(mPackageManager.getApplicationInfo("package1", PackageManager.GET_META_DATA))
                .thenThrow(new PackageManager.NameNotFoundException("package1"));

        // exercise
        mApplicationInfoRepositoryImpl.get(packageNames);
    }


    @Test
    public void getOk() throws Exception {
        // setup
        String packageName = "package1";
        ApplicationInfo expected = mock(ApplicationInfo.class);
        when(mPackageManager.getApplicationInfo("package1", PackageManager.GET_META_DATA))
                .thenReturn(expected);

        // exercise
        ApplicationInfo actual = mApplicationInfoRepositoryImpl.get(packageName);

        // verify
        assertThat(actual, is(expected));
    }

    @Test
    public void getNotFound() throws Exception {
        // setup
        String packageName = "package1";
        when(mPackageManager.getApplicationInfo("package1", PackageManager.GET_META_DATA))
                .thenThrow(new PackageManager.NameNotFoundException("package1"));

        // exercise
        ApplicationInfo actual = mApplicationInfoRepositoryImpl.get(packageName);

        // verify
        assertThat(actual, is(nullValue()));
    }

    @Test(expected = NullPointerException.class)
    public void getArgNull() throws Exception {
        // setup
        String packageName = null;

        // exercise
        mApplicationInfoRepositoryImpl.get(packageName);
    }
}