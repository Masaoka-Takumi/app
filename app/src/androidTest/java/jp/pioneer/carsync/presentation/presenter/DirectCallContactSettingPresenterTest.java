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
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.DirectCallContactSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DirectCallContactSettingPresenterのテスト
 */
public class DirectCallContactSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks DirectCallContactSettingPresenter mPresenter = new DirectCallContactSettingPresenter();
    @Mock DirectCallContactSettingView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock AppSharedPreference mPreference;
    @Mock QueryContact mContactCase;

    private static final int LOADER_ID_CONTACT = -1;
    private static final String KEY_CONTACTS_ID = "contacts_id";
    private LoaderManager mLoaderManager;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOnInitialize() throws Exception {
        when(mPreference.getDirectCallContactLookupKey()).thenReturn("satou");
        mPresenter.onInitialize();
        verify(mView).setTargetContact("satou");
    }

    @Test
    public void testOnRestoreInstanceState() throws Exception {
        Bundle args = new Bundle();
        when(mPreference.getDirectCallContactLookupKey()).thenReturn("tanaka");
        mPresenter.onRestoreInstanceState(args);
        verify(mView).setTargetContact("tanaka");
    }

    @Test
    public void testOnCreateLoaderCreateContacts() throws Exception {
        Bundle args = new Bundle();
        int id = LOADER_ID_CONTACT;
        CursorLoader cursorLoader = mock(CursorLoader.class);
        QueryParams params = ContactsContract.QueryParamsBuilder.createContacts();
        when(mContactCase.execute(params)).thenReturn(cursorLoader);

        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);

        assertThat(loader, is(cursorLoader));
    }

    @Test
    public void testOnCreateLoaderCreatePhones() throws Exception {
        Bundle args = new Bundle();
        int id = 1;
        CursorLoader cursorLoader = mock(CursorLoader.class);
        args.putLong(KEY_CONTACTS_ID, 1);
        QueryParams params = ContactsContract.QueryParamsBuilder.createPhones(1);
        when(mContactCase.execute(params)).thenReturn(cursorLoader);
        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);

        assertThat(loader, is(cursorLoader));
    }

    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager loaderManager = mock(LoaderManager.class);
        mLoaderManager = loaderManager;
        mPresenter.setLoaderManager(loaderManager);
        verify(mLoaderManager).initLoader(LOADER_ID_CONTACT, null, mPresenter);
    }

    @Test
    public void onSelectGroup() throws Exception {
        ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
        LoaderManager loaderManager = mock(LoaderManager.class);

        Cursor cursor = mock(Cursor.class);
        when(cursor.getPosition()).thenReturn(1);
        when(cursor.getLong(anyInt())).thenReturn((long) 1);
        //Bundle args = new Bundle();
        //args.putLong(KEY_CONTACTS_ID, (long)1);
        mPresenter.setLoaderManager(loaderManager);

        loaderManager.initLoader(LOADER_ID_CONTACT, null, mPresenter);
        mPresenter.onSelectGroup(cursor);
        //Bundleの比較ができないため、ArgumentCaptureを使う
        verify(loaderManager).restartLoader(anyInt(), argument.capture(), any(DirectCallContactSettingPresenter.class));
        assertThat(argument.getValue().getLong(KEY_CONTACTS_ID), is(1L));
    }

    //リストグループ表示//setLoaderManagerから
    @Test
    public void testOnLoadFinishedSetGroupCursor() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        Cursor cursor = mock(Cursor.class);
        when(cursorLoader.getId()).thenReturn(LOADER_ID_CONTACT);
        //cursorが閉じていない
        when(cursor.isClosed()).thenReturn(false);
        mPresenter.onLoadFinished(cursorLoader, cursor);
        verify(mView).setGroupCursor(cursor);
    }

    //リスト子要素表示//onSelectGroupから
    @Test
    public void testOnLoadFinishedSetChildrenCursor() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        Cursor cursor = mock(Cursor.class);
        when(cursorLoader.getId()).thenReturn(1);
        when(cursor.isClosed()).thenReturn(false);
        mPresenter.onLoadFinished(cursorLoader, cursor);
        verify(mView).setChildrenCursor(1, cursor);
    }

    //リスト子要素表示例外//onSelectGroupから
    @Test
    public void testOnLoadFinishedSetChildrenCursorException() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        Cursor cursor = mock(Cursor.class);
        NullPointerException e = mock(NullPointerException.class);
        ;
        when(cursorLoader.getId()).thenReturn(1);
        when(cursor.isClosed()).thenReturn(false);
        when(cursor.getColumnIndex("_id")).thenThrow(new NullPointerException());
        mPresenter.onLoadFinished(cursorLoader, cursor);
    }

    @Test
    public void testOnLoaderReset() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        Cursor cursor = mock(Cursor.class);
        when(cursorLoader.getId()).thenReturn(1);
        when(cursor.isClosed()).thenReturn(false);
        mPresenter.onLoaderReset(cursorLoader);
        verify(mView, times(0)).setGroupCursor(cursor);
        verify(mView, times(0)).setChildrenCursor(1, cursor);
    }

    @Test
    public void testOnGroupCollapseAction() throws Exception {
        int position = -1;
        LoaderManager loaderManager = mock(LoaderManager.class);
        mLoaderManager = loaderManager;
        mPresenter.setLoaderManager(loaderManager);
        mPresenter.onGroupCollapseAction(position);
        verify(mLoaderManager).destroyLoader(position);
    }

    //子項目選択時の処理
    @Test
    public void testOnNumberAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        Cursor cursor = mock(Cursor.class);

        mPresenter.onNumberAction(cursor);
        verify(mPreference).setDirectCallContactLookupKey(ContactsContract.Phone.getLookupKey(cursor));
        verify(mPreference).setDirectCallContactNumberId(ContactsContract.Phone.getId(cursor));
        verify(mEventBus).post(argument.capture());
        assertThat(argument.getValue(), is(instanceOf(GoBackEvent.class)));
    }

}