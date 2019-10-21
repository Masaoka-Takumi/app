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

import java.util.ArrayList;
import java.util.Date;

import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.model.ContactsHistoryItem;
import jp.pioneer.carsync.presentation.view.ContactsHistoryView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ContactsHistoryPresenterのテスト
 */
public class ContactsHistoryPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ContactsHistoryPresenter mPresenter = new ContactsHistoryPresenter();
    @Mock ContactsHistoryView mView;
    @Mock EventBus mEventBus;
    @Mock QueryContact mContactCase;
    private static final int LOADER_ID_ARTIST = 0;
    private LoaderManager mLoaderManager;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * setLoaderManagerのテスト
     */
    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager loaderManager = mock(LoaderManager.class);
        mPresenter.setLoaderManager(loaderManager);
        verify(loaderManager).initLoader(LOADER_ID_ARTIST, Bundle.EMPTY, mPresenter);
    }

    /**
     * onCreateLoaderのテスト
     */
    @Test
    public void testOnCreateLoader() throws Exception {
        Bundle args = new Bundle();
        int id = LOADER_ID_ARTIST;
        CursorLoader cursorLoader = mock(CursorLoader.class);
        when(mContactCase.execute(ContactsContract.QueryParamsBuilder.createCalls())).thenReturn(cursorLoader);
        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);
        assertThat(loader, is(cursorLoader));
    }

    /**
     * onLoadFinishedのテスト
     */
    @Test
    public void testOnLoadFinished() throws Exception {
        CursorLoader loader = mock(CursorLoader.class);
        Cursor data = mock(Cursor.class);
        mPresenter.onLoadFinished(loader, data);
        verify(mView).setHistoryList(any(ArrayList.class));
    }

    /**
     * onLoaderResetのテスト
     */
    @Test
    public void testOnLoaderReset() throws Exception {
        CursorLoader loader = mock(CursorLoader.class);
        mPresenter.onLoaderReset(loader);
        verify(mView).setHistoryList(null);
    }

    /**
     * onContactsHistoryActionのテスト
     */
    @Test
    public void testOnContactsHistoryAction() throws Exception {
        ArgumentCaptor<Intent> argument = ArgumentCaptor.forClass(Intent.class);
        ContactsHistoryItem item = new ContactsHistoryItem(0,"satou", 1, new Date(),"11122223333");
        Uri uri = Uri.parse("tel:11122223333");
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        mPresenter.onContactsHistoryAction(item);
        //Intentの比較ができないため、ArgumentCaptureを使う
        verify(mView).dial(argument.capture());
        assertThat(argument.getValue().toString(), is(intent.toString()));
    }

}