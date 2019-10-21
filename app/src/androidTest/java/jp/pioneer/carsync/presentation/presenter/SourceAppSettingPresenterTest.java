package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseBooleanArray;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.PreferMusicApp;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.Property;
import jp.pioneer.carsync.presentation.model.SourceAppModel;
import jp.pioneer.carsync.presentation.view.SourceAppSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/10/24.
 */
public class SourceAppSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SourceAppSettingPresenter mPresenter = new SourceAppSettingPresenter();
    @Mock SourceAppSettingView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock SourceAppModel mModel;
    @Mock PreferMusicApp mPreferMusicApp;

    private List<ApplicationInfo> mInstallApplications;
    private List<ApplicationInfo> mCallbackApplications;
    private AppSharedPreference.Application[] mCheckedApplications;
    private SparseBooleanArray mCheckedItemPosition;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mModel.installedMessagingApps = mock(Property.class);
        mModel.checkedItemPositions = mock(Property.class);

        mInstallApplications = new ArrayList<>();
        mCallbackApplications = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ApplicationInfo info = new ApplicationInfo();
            info.packageName = "com.sample.app" + String.valueOf(i + 1);
            mInstallApplications.add(info);
            if (i == 1) mCallbackApplications.add(info);
        }
        mCheckedApplications = new AppSharedPreference.Application[]{
                new AppSharedPreference.Application("com.sample.app2", "com.sample.app2"),
        };
        mCheckedItemPosition = new SparseBooleanArray();
        mCheckedItemPosition.put(0, false);
        mCheckedItemPosition.put(1, true);
        mCheckedItemPosition.put(2, false);
    }

    @Test
    public void testOnTakeView() throws Exception {

        when(mPreferMusicApp.getInstalledTargetAppList()).thenReturn(mInstallApplications);
        when(mPreferMusicApp.getSelectedAppList()).thenReturn(mCheckedApplications);

        when(mModel.installedMessagingApps.isDirty()).thenReturn(true);
        when(mModel.installedMessagingApps.getValue()).thenReturn(mInstallApplications);
        when(mModel.checkedItemPositions.isDirty()).thenReturn(true);
        when(mModel.checkedItemPositions.getValue()).thenReturn(mCheckedItemPosition);

        /*
         * SparseBooleanArray#equalsがAPI LEVEL24未満の場合、
         * インスタンスでの比較を行っており新規インスタンスを作成すると、
         * 結果がFalseになるため、#verifyが使えない。
         * そのため、キャプチャを取る方法にする。
         */
        final ArgumentCaptor<SparseBooleanArray> cap = ArgumentCaptor.forClass(SparseBooleanArray.class);
        doNothing().when(mView).setCheckedItemPositions(cap.capture());

        mPresenter.takeView(mView);

        verify(mView).setInstalledMusicApps(mInstallApplications);

        SparseBooleanArray array = cap.getValue();
        assertThat(array.valueAt(1), is(true));
    }

    @Test
    public void testOnTakeViewError() throws Exception {
        AppSharedPreference.Application[] checked = new AppSharedPreference.Application[]{
                new AppSharedPreference.Application("", ""),
        };

        when(mPreferMusicApp.getInstalledTargetAppList()).thenReturn(mInstallApplications);
        when(mPreferMusicApp.getSelectedAppList()).thenReturn(checked);

        when(mModel.installedMessagingApps.isDirty()).thenReturn(false);
        when(mModel.checkedItemPositions.isDirty()).thenReturn(false);

        mPresenter.takeView(mView);

        verify(mView, Mockito.never()).setInstalledMusicApps(mInstallApplications);
        verify(mView, Mockito.never()).setCheckedItemPositions(mCheckedItemPosition);
    }

    @Test
    public void testOnResume() throws Exception {
        when(mPreferMusicApp.getInstalledTargetAppList()).thenReturn(mInstallApplications);
        when(mPreferMusicApp.getSelectedAppList()).thenReturn(mCheckedApplications);

        when(mModel.installedMessagingApps.isDirty()).thenReturn(true);
        when(mModel.installedMessagingApps.getValue()).thenReturn(mInstallApplications);
        when(mModel.checkedItemPositions.isDirty()).thenReturn(true);
        when(mModel.checkedItemPositions.getValue()).thenReturn(mCheckedItemPosition);

        /*
         * SparseBooleanArray#equalsがAPI LEVEL24未満の場合、
         * インスタンスでの比較を行っており新規インスタンスを作成すると、
         * 結果がFalseになるため、#verifyが使えない。
         * そのため、キャプチャを取る方法にする。
         */
        final ArgumentCaptor<SparseBooleanArray> cap = ArgumentCaptor.forClass(SparseBooleanArray.class);
        doNothing().when(mView).setCheckedItemPositions(cap.capture());

        mPresenter.onResume();

        verify(mView).setInstalledMusicApps(mInstallApplications);

        SparseBooleanArray array = cap.getValue();
        assertThat(array.valueAt(1), is(true));
    }

    @Test
    public void testOnSaveInstanceState() throws Exception {
        Bundle args = new Bundle();

        SourceAppSettingPresenter subPresenter = new SourceAppSettingPresenter();
        SourceAppSettingView mockView = mock(SourceAppSettingView.class);
        subPresenter.mModel = mock(SourceAppModel.class);
        subPresenter.mModel.installedMessagingApps = mock(Property.class);
        subPresenter.mModel.checkedItemPositions = mock(Property.class);
        subPresenter.mPreferMusicApp = mock(PreferMusicApp.class);

        when(mView.getCheckedItemPositions()).thenReturn(mCheckedItemPosition);
        when(subPresenter.mPreferMusicApp.getInstalledTargetAppList()).thenReturn(mInstallApplications);
        when(subPresenter.mPreferMusicApp.getSelectedAppList()).thenReturn(mCheckedApplications);

        when(subPresenter.mModel.installedMessagingApps.isDirty()).thenReturn(true);
        when(subPresenter.mModel.installedMessagingApps.getValue()).thenReturn(mInstallApplications);
        when(subPresenter.mModel.checkedItemPositions.isDirty()).thenReturn(true);
        when(subPresenter.mModel.checkedItemPositions.getValue()).thenReturn(mCheckedItemPosition);

        final ArgumentCaptor<SparseBooleanArray> cap = ArgumentCaptor.forClass(SparseBooleanArray.class);
        doNothing().when(mockView).setCheckedItemPositions(cap.capture());

        mPresenter.saveInstanceState(args);
        subPresenter.takeView(mockView);
        subPresenter.restoreInstanceState(args);

        /*
         * MessagePresenter#onTakeView
         * MessagePresenter#onRestoreInstanceState
         * 上記メソッドから呼ばれるため、2回呼ばれることを想定
         */

        verify(mockView, Mockito.times(2)).setInstalledMusicApps(mInstallApplications);

        SparseBooleanArray array = cap.getValue();
        assertThat(array.valueAt(1), is(true));
    }

    @Test
    public void testOnMusicAppDecided() throws Exception {
        when(mView.getCheckedItemPositions()).thenReturn(mCheckedItemPosition);
        when(mModel.installedMessagingApps.getValue()).thenReturn(mInstallApplications);
        ApplicationInfo app = new ApplicationInfo();
        PackageManager pm = mock(PackageManager.class);
        app.packageName = "com.sample.app1";
        when(mContext.getPackageManager()).thenReturn(pm);
        mPresenter.onMusicAppDecided();
        verify(mPreferMusicApp).setSelectedAppList(mCheckedApplications);

    }

    @Test
    public void onBackAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onBackAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.SOURCE_SELECT));
    }

}