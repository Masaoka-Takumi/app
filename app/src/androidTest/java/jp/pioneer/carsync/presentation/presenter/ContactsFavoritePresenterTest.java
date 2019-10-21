package jp.pioneer.carsync.presentation.presenter;

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

import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.view.ContactsFavoriteView;

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
public class ContactsFavoritePresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ContactsFavoritePresenter mPresenter = new ContactsFavoritePresenter();
    @Mock ContactsFavoriteView mView;
    @Mock EventBus mEventBus;
    @Mock QueryContact mContactCase;
    private static final int LOADER_ID_CONTACT = -1;
    private static final String KEY_CONTACTS_ID = "contacts_id";
    @Mock LoaderManager mLoaderManager;

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
    public void testOnCreateLoaderCreateFavoriteContacts() throws Exception {
        Bundle args = new Bundle();
        int id = LOADER_ID_CONTACT;
        CursorLoader cursorLoader = mock(CursorLoader.class);
        when(mContactCase.execute(ContactsContract.QueryParamsBuilder.createFavoriteContacts())).thenReturn(cursorLoader);

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
        args.putLong(KEY_CONTACTS_ID,1);
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
        mPresenter.setLoaderManager(mLoaderManager);
        verify(mLoaderManager).initLoader(LOADER_ID_CONTACT, null, mPresenter);
    }

    /**
     * setGroupCursorのテスト
     */
    @Test
    public void testSetGroupCursor() throws Exception {
        ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
        Bundle args = new Bundle();
        args.putLong(KEY_CONTACTS_ID, 1);
        Cursor data = mock(Cursor.class);
        when(data.getPosition()).thenReturn(1);
        when(ContactsContract.Contact.getId(data)).thenReturn(1L);

        mPresenter.setGroupCursor(data);

        //Bundleの比較ができないため、ArgumentCaptureを使う
        verify(mLoaderManager).restartLoader(anyInt(), argument.capture(), any(ContactsFavoritePresenter.class));
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
        mPresenter.onLoadFinished(loader,data);

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
        mPresenter.onLoadFinished(loader,data);

        verify(mView).setChildrenCursor(1,data);
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

        verify(mView,times(0)).setChildrenCursor(anyInt(),eq(data));
    }

    /**
     * onGroupCollapseActionのテスト
     */
    @Test
    public void testOnGroupCollapseAction() throws Exception {
        mPresenter.onGroupCollapseAction(1);
        verify(mLoaderManager).destroyLoader(1);
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
        Intent intent =new Intent(Intent.ACTION_DIAL,uri);
        mPresenter.onNumberAction(cursor);
        //Intentの比較ができないため、ArgumentCaptureを使う
        verify(mView).dial(argument.capture());
        assertThat(intent.toString(), is(argument.getValue().toString()));
    }

}