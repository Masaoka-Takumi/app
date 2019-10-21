package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
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

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.DirectCallSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DirectCallSettingPresenterのテスト
 */
public class DirectCallSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks DirectCallSettingPresenter mPresenter = new DirectCallSettingPresenter();
    @Mock DirectCallSettingView mView;
    @Mock Context mContext;
    @Mock AppSharedPreference mPreference;
    @Mock EventBus mEventBus;
    @Mock QueryContact mContactCase;

    private static final int LOADER_ID_NAME = 1;
    private static final int LOADER_ID_NUMBER = 2;
    private static final String KEY_NAME_ID = "name_id";
    private static final String KEY_NUMBER_ID = "number_id";

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void testSetLoaderManagerEnable() throws Exception {
        LoaderManager mockManager = mock(LoaderManager.class);

        when(mPreference.getDirectCallContactLookupKey()).thenReturn("TEST");
        when(mPreference.getDirectCallContactNumberId()).thenReturn((long) 1);
        mPresenter.setLoaderManager(mockManager);

        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(1) instanceof Bundle) {
                String actual = ((Bundle) invocationOnMock.getArgument(1)).getString(KEY_NAME_ID);
                assertThat(actual, is("TEST"));
            }
            return null;
        }).when(mockManager).restartLoader(eq(LOADER_ID_NAME), any(Bundle.class), any(LoaderManager.LoaderCallbacks.class));
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(1) instanceof Bundle) {
                long actual = ((Bundle) invocationOnMock.getArgument(1)).getLong(KEY_NUMBER_ID);
                assertThat(actual, is(1));
            }
            return null;
        }).when(mockManager).restartLoader(eq(LOADER_ID_NUMBER), any(Bundle.class), any(LoaderManager.LoaderCallbacks.class));
    }

    @Test
    public void testSetLoaderManagerDisableName() throws Exception {
        LoaderManager mockManager = mock(LoaderManager.class);
        when(mPreference.getDirectCallContactLookupKey()).thenReturn("");
        when(mPreference.getDirectCallContactNumberId()).thenReturn((long) 1);
        mPresenter.setLoaderManager(mockManager);
        verify(mView).setDisable();
    }

    @Test
    public void testSetLoaderManagerDisableNumber() throws Exception {
        LoaderManager mockManager = mock(LoaderManager.class);
        when(mPreference.getDirectCallContactLookupKey()).thenReturn("TEST");
        when(mPreference.getDirectCallContactNumberId()).thenReturn((long) -1);
        mPresenter.setLoaderManager(mockManager);
        verify(mView).setDisable();
    }

    @Test
    public void testOnCreateLoaderName() throws Exception {
        int id = LOADER_ID_NAME;
        Bundle args = new Bundle();
        args.putString(KEY_NAME_ID, "TEST");
        CursorLoader cursorLoader = mock(CursorLoader.class);
        QueryParams params = ContactsContract.QueryParamsBuilder.createContact("TEST");
        when(mContactCase.execute(params)).thenReturn(cursorLoader);
        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);
        assertThat(loader, is(cursorLoader));

    }

    @Test
    public void testOnCreateLoaderName2() throws Exception {
        int id = LOADER_ID_NAME;
        Bundle args = new Bundle();
        args.putLong(KEY_NUMBER_ID, 1);
        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);
        assertThat(loader, is(nullValue()));

    }

    @Test
    public void testOnCreateLoaderNumber() throws Exception {
        int id = LOADER_ID_NUMBER;
        Bundle args = new Bundle();
        args.putLong(KEY_NUMBER_ID, 1);
        CursorLoader cursorLoader = mock(CursorLoader.class);
        QueryParams params = ContactsContract.QueryParamsBuilder.createPhone(1);
        when(mContactCase.execute(params)).thenReturn(cursorLoader);
        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);
        assertThat(loader, is(cursorLoader));

    }

    @Test
    public void testOnCreateLoaderNumber2() throws Exception {
        int id = LOADER_ID_NUMBER;
        Bundle args = new Bundle();
        args.putString(KEY_NAME_ID, "TEST");
        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);
        assertThat(loader, is(nullValue()));

    }

    @Test
    public void testOnCreateLoaderElse() throws Exception {
        int id = 3;
        Bundle args = new Bundle();
        args.putLong(KEY_NUMBER_ID, 0);
        args.putString(KEY_NAME_ID, "");
        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);

        assertThat(loader, is(nullValue()));
    }

    @Test
    public void testOnLoadFinishedDataName() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        Cursor data = mock(Cursor.class);
        data.moveToFirst();
        when(cursorLoader.getId()).thenReturn(LOADER_ID_NAME);
        when(data.moveToFirst()).thenReturn(true);
        when(data.getCount()).thenReturn(1);
        when(data.getColumnIndexOrThrow(android.provider.ContactsContract.Contacts.DISPLAY_NAME)).thenReturn(0);
        when(data.getString(0)).thenReturn("TEST");
        when(data.getColumnIndexOrThrow(android.provider.ContactsContract.Contacts.PHOTO_URI)).thenReturn(1);
        when(data.getString(1)).thenReturn("test://motonami");
        mPresenter.onLoadFinished(cursorLoader, data);
        CursorLoader cursorLoader2 = mock(CursorLoader.class);
        Cursor data2 = mock(Cursor.class);
        data2.moveToFirst();;
        when(cursorLoader2.getId()).thenReturn(LOADER_ID_NUMBER);
        when(data2.isNull(1)).thenReturn(false);
        when(data2.getCount()).thenReturn(1);

        when(ContactsContract.Phone.getNumber(data2)).thenReturn("080-1111-2222");
        when(ContactsContract.Phone.getNumberType(data2)).thenReturn(1);
        mPresenter.onLoadFinished(cursorLoader2, data2);

        verify(mView).setContactItem("TEST", Uri.parse("test://motonami"));
        verify(mView).setPhoneItem("080-1111-2222", R.drawable.p0052_home);
    }

    @Test
    public void testOnLoadFinishedNoData() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        Cursor data = mock(Cursor.class);
        when(data.getCount()).thenReturn(0);
        mPresenter.onLoadFinished(cursorLoader, data);
        verify(mView).setDisable();
    }


    @Test
    public void testOnDeleteAction() throws Exception {
        LoaderManager mockManager = mock(LoaderManager.class);
        when(mPreference.getDirectCallContactLookupKey()).thenReturn("TEST");
        when(mPreference.getDirectCallContactNumberId()).thenReturn((long) 1);
        mPresenter.setLoaderManager(mockManager);

        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(1) instanceof Bundle) {
                String actual = ((Bundle) invocationOnMock.getArgument(1)).getString(KEY_NAME_ID);
                assertThat(actual, is("TEST"));
            }
            return null;
        }).when(mockManager).restartLoader(eq(LOADER_ID_NAME), any(Bundle.class), any(LoaderManager.LoaderCallbacks.class));
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(1) instanceof Bundle) {
                long actual = ((Bundle) invocationOnMock.getArgument(1)).getLong(KEY_NUMBER_ID);
                assertThat(actual, is(1));
            }
            return null;
        }).when(mockManager).restartLoader(eq(LOADER_ID_NUMBER), any(Bundle.class), any(LoaderManager.LoaderCallbacks.class));
        mPresenter.onDeleteAction();
        verify(mPreference).removeDirectCallContactLookupKey();
        verify(mockManager).destroyLoader(LOADER_ID_NAME);
        verify(mPreference).removeDirectCallContactNumberId();
        verify(mockManager).destroyLoader(LOADER_ID_NUMBER);
        verify(mView).setDisable();
    }

    @Test
    public void testOnRegisterAction() throws Exception {
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onRegisterAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.DIRECT_CALL_CONTACT_SETTING));
    }

}