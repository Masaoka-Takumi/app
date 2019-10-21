package jp.pioneer.carsync.presentation.presenter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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

import java.lang.reflect.Field;

import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.domain.interactor.UpdateContact;
import jp.pioneer.carsync.presentation.view.ContactsView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ContactsPresenterのテスト
 */
public class ContactsPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ContactsPresenter mPresenter = new ContactsPresenter();
    @Mock ContactsView mView;
    @Mock EventBus mEventBus;
    @Mock Context mContext;
    @Mock QueryContact mContactCase;
    @Mock UpdateContact mFavoriteCase;

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

    /**
     * onCreateLoaderのテスト 親項目取得
     */
    @Test
    public void testOnCreateLoaderCreateContacts() throws Exception {
        Bundle args = new Bundle();
        int id = LOADER_ID_CONTACT;
        CursorLoader cursorLoader = mock(CursorLoader.class);
        when(mContactCase.execute(ContactsContract.QueryParamsBuilder.createContacts())).thenReturn(cursorLoader);

        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);

        assertThat(loader, is(cursorLoader));
    }

    /**
     * onCreateLoaderのテスト 子項目取得
     */
    @Test
    public void testOnCreateLoaderCreatePhones() throws Exception {
        Bundle args = new Bundle();
        int id = 1;
        args.putLong(KEY_CONTACTS_ID, 1);
        CursorLoader cursorLoader = mock(CursorLoader.class);
        when(mContactCase.execute(ContactsContract.QueryParamsBuilder.createPhones(1))).thenReturn(cursorLoader);
        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);

        assertThat(loader, is(cursorLoader));
    }

    /**
     * setLoaderManagerのテスト
     */
    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager loaderManager = mock(LoaderManager.class);
        mPresenter.setLoaderManager(loaderManager);
        verify(loaderManager).initLoader(LOADER_ID_CONTACT, null, mPresenter);
    }

    /**
     * setGroupCursorのテスト
     */
    @Test
    public void testSetGroupCursor() throws Exception {
        ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
        LoaderManager loaderManager = mock(LoaderManager.class);
        Bundle args = new Bundle();
        args.putLong(KEY_CONTACTS_ID, 1);
        Cursor data = mock(Cursor.class);
        when(data.getPosition()).thenReturn(1);
        when(ContactsContract.Contact.getId(data)).thenReturn((long) 1);
        Field field = ContactsPresenter.class.getDeclaredField("mLoaderManager");
        field.setAccessible(true);
        field.set(mPresenter, loaderManager);

        mPresenter.setGroupCursor(data);

        //Bundleの比較ができないため、ArgumentCaptureを使う
        verify(loaderManager).restartLoader(anyInt(), argument.capture(), any(ContactsPresenter.class));
        assertThat(argument.getValue().getLong(KEY_CONTACTS_ID), is(1L));
    }

    /**
     * onLoadFinishedのテスト　親項目取得
     */
    @Test
    public void testOnLoadFinishedSetCursorSetGroupCursor() throws Exception {
        CursorLoader loader = mock(CursorLoader.class);
        Cursor data = mock(Cursor.class);
        when(loader.getId()).thenReturn(LOADER_ID_CONTACT);
        mPresenter.onLoadFinished(loader, data);

        verify(mView).setGroupCursor(data);
    }

    /**
     * onLoadFinishedのテスト　子項目取得
     */
    @Test
    public void testOnLoadFinishedSetCursorSetChildrenCursor() throws Exception {
        CursorLoader loader = mock(CursorLoader.class);
        Cursor data = mock(Cursor.class);
        when(loader.getId()).thenReturn(1);
        mPresenter.onLoadFinished(loader, data);

        verify(mView).setChildrenCursor(1, data);
    }

    /**
     * onLoaderResetのテスト
     */
    @Test
    public void testOnLoaderResetSetCursor() throws Exception {
        CursorLoader loader = mock(CursorLoader.class);
        Cursor data = mock(Cursor.class);
        when(loader.getId()).thenReturn(1);
        mPresenter.onLoaderReset(loader);

        verify(mView, times(0)).setChildrenCursor(anyInt(), eq(data));
    }

    /**
     * onGroupCollapseActionのテスト
     */
    @Test
    public void testOnGroupCollapseAction() throws Exception {
        LoaderManager loaderManager = mock(LoaderManager.class);
        Field field = ContactsPresenter.class.getDeclaredField("mLoaderManager");
        field.setAccessible(true);
        field.set(mPresenter, loaderManager);
        mPresenter.onGroupCollapseAction(1);
        verify(loaderManager).destroyLoader(1);
    }

    /**
     * onNumberActionのテスト
     */
    @Test
    public void testOnNumberAction() throws Exception {
        ArgumentCaptor<Intent> argument = ArgumentCaptor.forClass(Intent.class);
        Cursor cursor = mock(Cursor.class);
        when(ContactsContract.Phone.getNumber(cursor)).thenReturn("11122223333");
        Uri uri = Uri.parse("tel:11122223333");
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        mPresenter.onNumberAction(cursor);
        //Intentの比較ができないため、ArgumentCaptureを使う
        verify(mView).dial(argument.capture());
        assertThat(argument.getValue().toString(), is(intent.toString()));
    }

    /**
     * onFavoritesActionのテスト-お気に入りOn
     */
    @Test
    public void testOnFavoritesActionyToTrue() throws Exception {
        Cursor cursor = mock(Cursor.class);
        when(ContactsContract.Contact.getLookupKey(cursor)).thenReturn("satou");
        when(cursor.getInt(cursor.getColumnIndex(android.provider.ContactsContract.Contacts.STARRED))).thenReturn(0);
        mPresenter.onFavoritesAction(cursor);
        verify(mFavoriteCase).execute(ContactsContract.UpdateParamsBuilder.createContact("satou", ContactsContract.Contact.setStarred(new ContentValues(), true)));
    }

    /**
     * onFavoritesActionのテスト-お気に入りOff
     */
    @Test
    public void testOnFavoritesActionToFalse() throws Exception {
        Cursor cursor = mock(Cursor.class);
        when(ContactsContract.Contact.getLookupKey(cursor)).thenReturn("satou");
        when(cursor.getInt(cursor.getColumnIndex(android.provider.ContactsContract.Contacts.STARRED))).thenReturn(1);
        mPresenter.onFavoritesAction(cursor);
        verify(mFavoriteCase).execute(ContactsContract.UpdateParamsBuilder.createContact("satou", ContactsContract.Contact.setStarred(new ContentValues(), false)));
    }
}