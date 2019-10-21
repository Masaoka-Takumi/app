package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.interactor.IsCapableOfImpactDetector;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.ImpactNotificationMethod;
import jp.pioneer.carsync.presentation.view.ImpactDetectionSettingsView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ImpactDetectionSettingsPresenterのテスト
 */
public class ImpactDetectionSettingsPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ImpactDetectionSettingsPresenter mPresenter = new ImpactDetectionSettingsPresenter();
    @Mock ImpactDetectionSettingsView mView;
    @Mock Context mContext;
    @Mock AppSharedPreference mPreference;
    @Mock EventBus mEventBus;
    @Mock QueryContact mContactCase;
    @Mock IsCapableOfImpactDetector mIsCapableOfImpactDetector;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * onInitializeから衝突検知機能無効端末設定のテスト
     */
    @Test
    public void testOnInitializeInvalidImpactDetection() throws Exception {
        //setup
        when(mIsCapableOfImpactDetector.execute()).thenReturn(false);
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.PHONE);

        mPresenter.onInitialize();

        verify(mView).invalidImpactDetection();
        verify(mView).setImpactNotificationMethod(ImpactNotificationMethod.PHONE);
    }

    /**
     * onRestoreInstanceStateから衝突検知機能有効端末設定のテスト
     */
    @Test
    public void testOnRestoreInstanceStateSetImpactDetectionEnabled() throws Exception {
        Bundle savedInstanceState = new Bundle();
        when(mIsCapableOfImpactDetector.execute()).thenReturn(true);
        when(mPreference.isImpactDetectionEnabled()).thenReturn(true);
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.SMS);

        mPresenter.onRestoreInstanceState(savedInstanceState);

        verify(mView).setImpactDetectionEnabled(true);
        verify(mView).setImpactNotificationMethod(ImpactNotificationMethod.SMS);
    }

    /**
     * setLoaderManagerのテスト
     */
    @Test
    public void testSetLoaderManagerRestartLoader() throws Exception {
        LoaderManager loaderManager = mock(LoaderManager.class);

        when(mPreference.getImpactNotificationContactLookupKey()).thenReturn("test");

        mPresenter.setLoaderManager(loaderManager);

        verify(loaderManager).restartLoader(0, null, mPresenter);
    }

    @Test
    public void testSetLoaderManagerEmpty() throws Exception {
        LoaderManager loaderManager = mock(LoaderManager.class);

        when(mPreference.getImpactNotificationContactLookupKey()).thenReturn("");

        mPresenter.setLoaderManager(loaderManager);

        verify(mView).setImpactNotificationContact("未設定");
        verify(loaderManager, never()).restartLoader(0, null, mPresenter);
    }

    /**
     * onCreateLoader連絡先未設定のテスト
     */
    @Test
    public void testOnCreateLoaderNull() throws Exception {
        Bundle args = new Bundle();
        int id = 1;

        CursorLoader cursorLoader = mock(CursorLoader.class);

        when(mPreference.getImpactNotificationContactLookupKey()).thenReturn("");
        QueryParams params = ContactsContract.QueryParamsBuilder.createContact("satou");
        when(mContactCase.execute(params)).thenReturn(cursorLoader);

        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);

        assertThat(loader, is(nullValue()));
    }

    /**
     * onCreateLoader連絡先設定済のテスト
     */
    @Test
    public void testOnCreateLoader() throws Exception {
        Bundle args = new Bundle();
        int id = 1;

        CursorLoader cursorLoader = mock(CursorLoader.class);

        when(mPreference.getImpactNotificationContactLookupKey()).thenReturn("satou");
        QueryParams params = ContactsContract.QueryParamsBuilder.createContact("satou");
        when(mContactCase.execute(params)).thenReturn(cursorLoader);

        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);

        assertThat(loader, is(cursorLoader));
    }

    /**
     * onLoadFinished正常系のテスト
     */
    @Test
    public void testOnLoadFinished() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        Cursor data = mock(Cursor.class);
        when(ContactsContract.Contact.getDisplayName(data)).thenReturn("satou");

        mPresenter.onLoadFinished(cursorLoader, data);

        verify(mView).setImpactNotificationContact("satou");
    }

    /**
     * onLoadFinished異常系のテスト
     */
    @Test
    public void testOnLoadFinishedException() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        Cursor data = mock(Cursor.class);
        //new Exception()ではChecked exception is invalid for this method!となる
        when(data.moveToFirst()).thenThrow(new RuntimeException());
        mPresenter.onLoadFinished(cursorLoader, data);

        verify(mView).setImpactNotificationContact("未設定");
    }

    /**
     * onLoaderResetのテスト（処理なし）
     */
    @Test
    public void testOnLoaderReset() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        mPresenter.onLoaderReset(cursorLoader);
    }

    /**
     * onImpactDetectionChangeのテスト
     */
    @Test
    public void testOnImpactDetectionChange() throws Exception {
        boolean newValue = true;
        mPresenter.onImpactDetectionChange(newValue);

        verify(mPreference).setImpactDetectionEnabled(newValue);
        verify(mView).setImpactDetectionEnabled(newValue);
    }

    /**
     * onImpactNotificationMethodChangeのテスト
     */
    @Test
    public void testOnImpactNotificationMethodChange() throws Exception {
        when(mPreference.getImpactNotificationMethod()).thenReturn(ImpactNotificationMethod.PHONE);

        mPresenter.onImpactNotificationMethodChange();
        verify(mPreference).setImpactNotificationMethod(ImpactNotificationMethod.SMS);
        verify(mView).setImpactNotificationMethod(ImpactNotificationMethod.SMS);
    }

    /**
     * onImpactNotificationContactActionのテスト
     */
    @Test
    public void testOnImpactNotificationContactAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onImpactNotificationContactAction();

        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.IMPACT_DETECTION_CONTACT_SETTING));

    }

}