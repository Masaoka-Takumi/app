package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.PreferNaviApp;
import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;
import jp.pioneer.carsync.presentation.view.NavigationView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/06/02.
 */
public class NavigationPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks NavigationPresenter mPresenter = new NavigationPresenter();
    @Mock NavigationView mView;
    @Mock PreferNaviApp mNaviCase;
    @Mock AppSharedPreference mPreference;
    @Mock ApplicationInfoRepository mRepository;
    @Mock Context mContext;

    private List<ApplicationInfo> mInstallApplications;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        mInstallApplications = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ApplicationInfo info = new ApplicationInfo();
            info.packageName = "com.sample.app" + String.valueOf(i + 1);
            mInstallApplications.add(info);
        }
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOnTakeView() throws Exception {
        when(mNaviCase.getInstalledTargetAppList()).thenReturn(mInstallApplications);
        AppSharedPreference.Application app = mock(AppSharedPreference.Application.class);
        when(mPreference.getNavigationApp()).thenReturn(app);
        mPreference.getNavigationApp().packageName = "com.sample.app1";
        mPresenter.onTakeView();
    }

    @Test
    public void testOnTakeView2() throws Exception {
        when(mNaviCase.getInstalledTargetAppList()).thenReturn(mInstallApplications);
        AppSharedPreference.Application app = mock(AppSharedPreference.Application.class);
        when(mPreference.getNavigationApp()).thenReturn(app);
        mPreference.getNavigationApp().packageName = "com.sample.app4";
        mPresenter.onTakeView();
    }

    @Test
    public void testOnResume() throws Exception {
        when(mNaviCase.getInstalledTargetAppList()).thenReturn(mInstallApplications);
        AppSharedPreference.Application app = mock(AppSharedPreference.Application.class);
        when(mPreference.getNavigationApp()).thenReturn(app);
        mPreference.getNavigationApp().packageName = "com.sample.app1";
        mPresenter.onResume();
    }

    @Test
    public void testOnNaviAppSelected() throws Exception {
        ApplicationInfo app = new ApplicationInfo();
        PackageManager pm = mock(PackageManager.class);
        app.packageName = "com.sample.app1";
        when(mContext.getPackageManager()).thenReturn(pm);
        AppSharedPreference.Application naviApp = new AppSharedPreference.Application("com.sample.app1", "");
        when(mPreference.getNavigationApp()).thenReturn(naviApp);
        mPresenter.onNavigationAppSelectedAction(app);
    }
}