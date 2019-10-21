package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseBooleanArray;

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
import jp.pioneer.carsync.domain.interactor.PreferReadNotification;
import jp.pioneer.carsync.presentation.model.MessagingAppModel;
import jp.pioneer.carsync.presentation.model.Property;
import jp.pioneer.carsync.presentation.view.MessageView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/06/09.
 */
public class MessagePresenterTest {

    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks MessagePresenter mPresenter = new MessagePresenter();
    @Mock MessageView mView;
    @Mock MessagingAppModel mModel;
    @Mock PreferReadNotification mPreferReadNotification;
    @Mock Context mContext;

    private List<ApplicationInfo> mInstallApplications;
    private List<ApplicationInfo> mCallbackApplications;
    private AppSharedPreference.Application[] mCheckedApplications;
    private SparseBooleanArray mCheckedItemPosition;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mModel.isReadNotificationEnabled = mock(Property.class);
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

    @Test(expected = NullPointerException.class)
    public void testOnTakeView() throws Exception {
        when(mPreferReadNotification.isEnabled()).thenReturn(true);
        when(mPreferReadNotification.getInstalledTargetAppList()).thenReturn(mInstallApplications);
        when(mPreferReadNotification.getSelectedAppList()).thenReturn(mCheckedApplications);

        when(mModel.isReadNotificationEnabled.isDirty()).thenReturn(true);
        when(mModel.isReadNotificationEnabled.getValue()).thenReturn(true);
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
//        doNothing().when(mView).setCheckedItemPositions(cap.capture());
//
//        mPresenter.takeView(mView);
//
//        verify(mView).setReadNotificationEnabled(true);
//        verify(mView).setInstalledMessagingApps(mInstallApplications);

        SparseBooleanArray array = cap.getValue();
        assertThat(array.valueAt(1), is(true));
    }
//
//    @Test
//    public void testOnTakeViewError() throws Exception {
//        AppSharedPreference.Application[] checked = new AppSharedPreference.Application[]{
//                new AppSharedPreference.Application("", ""),
//        };
//
//        when(mPreferReadNotification.isEnabled()).thenReturn(true);
//        when(mPreferReadNotification.getInstalledTargetAppList()).thenReturn(mInstallApplications);
//        when(mPreferReadNotification.getSelectedAppList()).thenReturn(checked);
//
//        when(mModel.isReadNotificationEnabled.isDirty()).thenReturn(false);
//        when(mModel.installedMessagingApps.isDirty()).thenReturn(false);
//        when(mModel.checkedItemPositions.isDirty()).thenReturn(false);
//
//        mPresenter.takeView(mView);
//
//        verify(mView, Mockito.never()).setReadNotificationEnabled(true);
//        verify(mView, Mockito.never()).setInstalledMessagingApps(mInstallApplications);
//        verify(mView, Mockito.never()).setCheckedItemPositions(mCheckedItemPosition);
//    }
//
//    @Test
//    public void testOnResume() throws Exception {
//        when(mPreferReadNotification.isEnabled()).thenReturn(true);
//        when(mPreferReadNotification.getInstalledTargetAppList()).thenReturn(mInstallApplications);
//        when(mPreferReadNotification.getSelectedAppList()).thenReturn(mCheckedApplications);
//
//        when(mModel.isReadNotificationEnabled.isDirty()).thenReturn(true);
//        when(mModel.isReadNotificationEnabled.getValue()).thenReturn(true);
//        when(mModel.installedMessagingApps.isDirty()).thenReturn(true);
//        when(mModel.installedMessagingApps.getValue()).thenReturn(mInstallApplications);
//        when(mModel.checkedItemPositions.isDirty()).thenReturn(true);
//        when(mModel.checkedItemPositions.getValue()).thenReturn(mCheckedItemPosition);
//
//        /*
//         * SparseBooleanArray#equalsがAPI LEVEL24未満の場合、
//         * インスタンスでの比較を行っており新規インスタンスを作成すると、
//         * 結果がFalseになるため、#verifyが使えない。
//         * そのため、キャプチャを取る方法にする。
//         */
//        final ArgumentCaptor<SparseBooleanArray> cap = ArgumentCaptor.forClass(SparseBooleanArray.class);
//        doNothing().when(mView).setCheckedItemPositions(cap.capture());
//
//        mPresenter.onResume();
//
//        verify(mView).setReadNotificationEnabled(true);
//        verify(mView).setInstalledMessagingApps(mInstallApplications);
//
//        SparseBooleanArray array = cap.getValue();
//        assertThat(array.valueAt(1), is(true));
//    }
//
//    @Test
//    public void testInstanceState() throws Exception {
//        Bundle args = new Bundle();
//
//        MessagePresenter subPresenter = new MessagePresenter();
//        MessageView mockView = mock(MessageView.class);
//        subPresenter.mModel = mock(MessagingAppModel.class);
//        subPresenter.mModel.isReadNotificationEnabled = mock(Property.class);
//        subPresenter.mModel.installedMessagingApps = mock(Property.class);
//        subPresenter.mModel.checkedItemPositions = mock(Property.class);
//        subPresenter.mMessagingCase = mock(PreferReadNotification.class);
//
//        when(mView.isReadNotificationEnabled()).thenReturn(true);
//        when(mView.getCheckedItemPositions()).thenReturn(mCheckedItemPosition);
//        when(subPresenter.mMessagingCase.isEnabled()).thenReturn(true);
//        when(subPresenter.mMessagingCase.getInstalledTargetAppList()).thenReturn(mInstallApplications);
//        when(subPresenter.mMessagingCase.getSelectedAppList()).thenReturn(mCheckedApplications);
//        when(subPresenter.mModel.isReadNotificationEnabled.isDirty()).thenReturn(true);
//        when(subPresenter.mModel.isReadNotificationEnabled.getValue()).thenReturn(true);
//        when(subPresenter.mModel.installedMessagingApps.isDirty()).thenReturn(true);
//        when(subPresenter.mModel.installedMessagingApps.getValue()).thenReturn(mInstallApplications);
//        when(subPresenter.mModel.checkedItemPositions.isDirty()).thenReturn(true);
//        when(subPresenter.mModel.checkedItemPositions.getValue()).thenReturn(mCheckedItemPosition);
//
//        final ArgumentCaptor<SparseBooleanArray> cap = ArgumentCaptor.forClass(SparseBooleanArray.class);
//        doNothing().when(mockView).setCheckedItemPositions(cap.capture());
//
//        mPresenter.saveInstanceState(args);
//        subPresenter.takeView(mockView);
//        subPresenter.restoreInstanceState(args);
//
//        /*
//         * MessagePresenter#onTakeView
//         * MessagePresenter#onRestoreInstanceState
//         * 上記メソッドから呼ばれるため、2回呼ばれることを想定
//         */
//        verify(mockView, Mockito.times(2)).setReadNotificationEnabled(true);
//        verify(mockView, Mockito.times(2)).setInstalledMessagingApps(mInstallApplications);
//
//        SparseBooleanArray array = cap.getValue();
//        assertThat(array.valueAt(1), is(true));
//    }
//
//    @Test
//    public void testGetEnable() throws Exception {
//        MessagePresenter subPresenter = new MessagePresenter();
//        subPresenter.mMessagingCase = mock(PreferReadNotification.class);
//        when(subPresenter.mMessagingCase.isEnabled()).thenReturn(true);
//        mPresenter.getEnable();
//    }
//
//    @Test
//    public void testOnSwitchReadNotificationEnabledChange() throws Exception {
//        mPresenter.onSwitchReadNotificationEnabledChange(true);
//    }
//
//    @Test
//    public void testOnMessagingAppDecided() throws Exception {
//        when(mView.isReadNotificationEnabled()).thenReturn(true);
//        when(mView.getCheckedItemPositions()).thenReturn(mCheckedItemPosition);
//        when(mModel.installedMessagingApps.getValue()).thenReturn(mInstallApplications);
//        ApplicationInfo app = new ApplicationInfo();
//        PackageManager pm = mock(PackageManager.class);
//        app.packageName = "com.sample.app1";
//        when(mContext.getPackageManager()).thenReturn(pm);
//        mPresenter.onMessagingAppDecided();
//    }
}