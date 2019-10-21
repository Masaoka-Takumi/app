package jp.pioneer.carsync.presentation.presenter;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.event.ShowCautionEvent;
import jp.pioneer.carsync.presentation.view.OpeningPrivacyPolicyView;
import jp.pioneer.carsync.presentation.view.argument.PermissionParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FlurryDataAnalysisPermissionのテスト
 */
public class FlurryDataAnalysisPermissionPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks OpeningPrivacyPolicyPresenter mPresenter = new OpeningPrivacyPolicyPresenter();
    @Mock EventBus mEventBus;
    @Mock OpeningPrivacyPolicyView mView;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock Context mContext;
    @Mock AppSharedPreference mPreference;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET
    };
    private PermissionParams mParams;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        int pid = android.os.Process.myPid();
        int uid = Process.myUid();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when(mContext.checkPermission(Manifest.permission.READ_CONTACTS, pid, uid)).thenReturn(PERMISSION_GRANTED);
            when(mContext.checkPermission(Manifest.permission.WRITE_CONTACTS, pid, uid)).thenReturn(PERMISSION_GRANTED);
            when(mContext.checkPermission(Manifest.permission.READ_CALL_LOG, pid, uid)).thenReturn(PERMISSION_GRANTED);
            when(mContext.checkPermission(Manifest.permission.CALL_PHONE, pid, uid)).thenReturn(PERMISSION_GRANTED);
            when(mContext.checkPermission(Manifest.permission.SEND_SMS, pid, uid)).thenReturn(PERMISSION_GRANTED);
            when(mContext.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, pid, uid)).thenReturn(PERMISSION_GRANTED);
            when(mContext.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, pid, uid)).thenReturn(PERMISSION_GRANTED);
            when(mContext.checkPermission(Manifest.permission.INTERNET, pid, uid)).thenReturn(PERMISSION_GRANTED);

        }
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void onAcceptAction_SessionStopped() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STOPPED);
        doAnswer(
                invocationOnMock -> {
                    NavigateEvent event = (NavigateEvent) invocationOnMock.getArguments()[0];
                    assertThat(event.screenId, is(ScreenId.UNCONNECTED_CONTAINER));
                    return null;
                }).when(mEventBus).post(any(NavigateEvent.class));

        mPresenter.onAcceptAction();

        verify(mPreference).setAgreedEulaPrivacyPolicy(true);
        verify(mEventBus).post(any(ShowCautionEvent.class));
    }

    @Test
    public void onAcceptAction_SessionStarted() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);

        mPresenter.onAcceptAction();

        verify(mPreference).setAgreedEulaPrivacyPolicy(true);
    }

    @Test
    public void onAcceptAction_SomePermissionDenied() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STOPPED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when(mContext.checkPermission(Manifest.permission.READ_CONTACTS, android.os.Process.myPid(), Process.myUid()))
                    .thenReturn(PERMISSION_DENIED);
        }

        mPresenter.onAcceptAction();

        verify(mPreference).setAgreedEulaPrivacyPolicy(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArgumentCaptor<String> argumentPermissions = ArgumentCaptor.forClass(String.class);
            assertThat(argumentPermissions.getAllValues(), is(contains(Manifest.permission.READ_CONTACTS)));
        }
    }

    @Test
    public void onAcceptAction_AllPermissionDenied() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STOPPED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when(mContext.checkPermission(anyString(), anyInt(), anyInt()))
                    .thenReturn(PERMISSION_DENIED);
        }

        mPresenter.onAcceptAction();

        verify(mPreference).setAgreedEulaPrivacyPolicy(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArgumentCaptor<String> argumentPermissions = ArgumentCaptor.forClass(String.class);
            assertThat(argumentPermissions.getAllValues(), is(contains(PERMISSIONS)));
        }
    }
}