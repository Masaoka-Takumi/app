package jp.pioneer.carsync.presentation.presenter;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.telephony.SmsManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.component.LocationProvider;
import jp.pioneer.carsync.domain.component.Resolver;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.interactor.ImpactDetectionCountDown;
import jp.pioneer.carsync.domain.interactor.GetCurrentLocation;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.model.ImpactNotificationMethod;
import jp.pioneer.carsync.presentation.view.AccidentDetectDialogView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 事故通知ダイアログのテスト
 */
public class AccidentDetectDialogPresenterTest {

    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    AccidentDetectDialogPresenter mPresenter;
    @Mock AccidentDetectDialogView mView;
    @Mock Context mContext;
    @Mock AppSharedPreference mPreference;
    @Mock GetCurrentLocation mLocationCase;
    @Mock QueryContact mContactsCase;
    @Mock ImpactDetectionCountDown mCancelCountdownCase;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() ->{
            mPresenter = new AccidentDetectDialogPresenter();
            mPresenter.mContext = mContext;
            mPresenter.mPreference = mPreference;
            mPresenter.mLocationCase = mLocationCase;
            mPresenter.mContactsCase = mContactsCase;
            mPresenter.mCancelCountdownCase = mCancelCountdownCase;
        });
    }

    @Test
    public void testOnInitialize() throws Exception {
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.PHONE);

        mPresenter.takeView(mView);
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> mPresenter.initialize());

        verify(mView).initProgress(30000);
        verify(mContext).registerReceiver(any(AccidentDetectDialogPresenter.SMSSentBroadcastReceiver.class), any(IntentFilter.class));
    }

    @Test
    public void testOnRestoreInstanceState() throws Exception {
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.PHONE);

        mPresenter.takeView(mView);
        mPresenter.restoreInstanceState(new Bundle());

        verify(mView).initProgress(30000);
        verify(mContext).registerReceiver(any(AccidentDetectDialogPresenter.SMSSentBroadcastReceiver.class), any(IntentFilter.class));
    }

    @Test
    public void testOnRestoreInstanceStateSending() throws Exception {
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.SMS);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.setSendState(AccidentDetectDialogPresenter.SendState.SENDING);
        mPresenter.restoreInstanceState(new Bundle());

        verify(mContext).registerReceiver(any(AccidentDetectDialogPresenter.SMSSentBroadcastReceiver.class), any(IntentFilter.class));
    }

    @Test
    public void testOnRestoreInstanceStateLocationError() throws Exception {
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.SMS);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.setSendState(AccidentDetectDialogPresenter.SendState.LOCATION_ERROR);
        mPresenter.restoreInstanceState(new Bundle());

        verify(mContext).registerReceiver(any(AccidentDetectDialogPresenter.SMSSentBroadcastReceiver.class), any(IntentFilter.class));
    }

    @Test
    public void testOnRestoreInstanceStateSendError() throws Exception {
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.SMS);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.setSendState(AccidentDetectDialogPresenter.SendState.SEND_ERROR);
        mPresenter.restoreInstanceState(new Bundle());

        verify(mView).updateText(getTargetContext().getResources().getString(R.string.sms_failure_cannot_send), true);
        verify(mContext).registerReceiver(any(AccidentDetectDialogPresenter.SMSSentBroadcastReceiver.class), any(IntentFilter.class));
    }

    @Test
    public void testOnRestoreInstanceStateComplete() throws Exception {
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.SMS);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.setSendState(AccidentDetectDialogPresenter.SendState.COMPLETE);
        mPresenter.restoreInstanceState(new Bundle());

        verify(mView).updateText(getTargetContext().getResources().getString(R.string.sms_complete), false);
        verify(mContext).registerReceiver(any(AccidentDetectDialogPresenter.SMSSentBroadcastReceiver.class), any(IntentFilter.class));
    }

    @Test
    public void testOnDestroy() throws Exception {
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.PHONE);

        mPresenter.takeView(mView);
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> mPresenter.initialize());
        mPresenter.destroy();

        verify(mContext).unregisterReceiver(any(AccidentDetectDialogPresenter.SMSSentBroadcastReceiver.class));
    }

    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager mockManager = mock(LoaderManager.class);

        mPresenter.setLoaderManager(mockManager);

        verify(mockManager).initLoader(any(Integer.class), isNull(), any(LoaderManager.LoaderCallbacks.class));
    }

    @Test
    public void testOnCreateLoader() throws Exception {
        when(mPreference.getImpactNotificationContactNumberId()).thenReturn(1L);

        mPresenter.onCreateLoader(0, new Bundle());

        verify(mContactsCase).execute(any(QueryParams.class));
    }

    @Test
    public void testOnLoadFinished() throws Exception {
        Loader<Cursor> mockLoader = mock(Loader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockCursor.getColumnIndexOrThrow(any(String.class))).thenReturn(1);

        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mockCursor).moveToFirst();
        verify(mockCursor).getString(any(Integer.class));
    }

    @Test
    public void testOnLoadFinishedException() throws Exception {
        Loader<Cursor> mockLoader = mock(Loader.class);
        Cursor mockCursor = mock(Cursor.class);

        doThrow(new IllegalStateException()).when(mockCursor).moveToFirst();

        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mockCursor, never()).getString(any(Integer.class));
    }

    @Test
    public void testOnLoaderReset() throws Exception {
        Loader<Cursor> mockLoader = mock(Loader.class);

        mPresenter.onLoaderReset(mockLoader);

        // no verify
    }

    @Test
    public void testOnSuccess() throws Exception {
        Loader<Cursor> mockLoader = mock(Loader.class);
        Cursor mockCursor = mock(Cursor.class);
        Location mockLocation = mock(Location.class);

        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.PHONE);
        when(mockCursor.getColumnIndexOrThrow("data1")).thenReturn(1);
        when(mockCursor.getString(1)).thenReturn("08012345678");
        when(mockLocation.getLatitude()).thenReturn(0.12345);
        when(mockLocation.getLongitude()).thenReturn(0.6789);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);
        mPresenter.onSuccess(mockLocation);
        String text = mContext.getResources().getString(R.string.format_accident_text) + "\n"
                + String.format(mContext.getResources().getString(R.string.format_accident_position), 0.12345, 0.6789);
        verify(mView).sendSMS(any(String.class), eq(text));
    }

    @Test
    public void testOnError() throws Exception {
        Resolver mockResolver = mock(Resolver.class);
        Loader<Cursor> mockLoader = mock(Loader.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.PHONE);
        when(mockCursor.getColumnIndexOrThrow("data1")).thenReturn(1);
        when(mockCursor.getString(1)).thenReturn("08012345678");
        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);
        mPresenter.onError(LocationProvider.Error.NOT_AVAILABLE, mockResolver);
        String text = mContext.getResources().getString(R.string.format_accident_text);
        verify(mView).sendSMS(any(String.class), eq(text));
    }

    @Test
    public void testExecutePhone() throws Exception {
        Loader<Cursor> mockLoader = mock(Loader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.PHONE);
        when(mockCursor.getColumnIndexOrThrow("data1")).thenReturn(1);
        when(mockCursor.getString(1)).thenReturn("08012345678");
        when(mView.isShowDialog()).thenReturn(true);

        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);
        mPresenter.execute();

        verify(mView).callPhone(any(String.class));
        verify(mView).callbackClose();
    }

    @Test
    public void testExecuteSMS() throws Exception {
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.SMS);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.execute();

        verify(mLocationCase).execute(any(AccidentDetectDialogPresenter.class));
    }

    @Test
    public void testOnReceiveResultSuccess() throws Exception {
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.onReceiveResult(Activity.RESULT_OK);

        verify(mView).updateText(getTargetContext().getResources().getString(R.string.sms_complete), false);
    }

    @Test
    public void testOnReceiveResultFailure() throws Exception {
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.onReceiveResult(SmsManager.RESULT_ERROR_GENERIC_FAILURE);

        verify(mView).updateText(getTargetContext().getResources().getString(R.string.sms_failure_cannot_send), true);
    }

    @Test
    public void testOnCancelAction() throws Exception {

        mPresenter.takeView(mView);
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> mPresenter.initialize());
        mPresenter.onCancelAction();

        verify(mView).callbackClose();
    }
}