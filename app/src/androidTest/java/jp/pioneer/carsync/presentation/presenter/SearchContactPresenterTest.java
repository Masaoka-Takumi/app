package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.interactor.CheckAvailableTextToSpeech;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.domain.interactor.ReadText;
import jp.pioneer.carsync.domain.model.VoiceCommand;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.SearchContactView;
import jp.pioneer.carsync.presentation.view.argument.SearchContentParams;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 連絡先検索画面のpresenterテスト
 */
public class SearchContactPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SearchContactPresenter mPresenter = new SearchContactPresenter();
    @Mock SearchContactView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock QueryContact mContactCase;
    @Mock CheckAvailableTextToSpeech mCheckTtsCase;
    @Mock ReadText mReadText;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void testOnCreateLoaderCreateContacts() throws Exception {
        Bundle args = new Bundle();
        String[] words = new String[]{"a", "i"};
        Bundle params = SearchContentParams.toBundle(VoiceCommand.PHONE, words);
        CursorLoader cursorLoader = mock(CursorLoader.class);

        when(mContactCase.execute(any(QueryParams.class))).thenReturn(cursorLoader);

        mPresenter.setArguments(params);
        Loader<Cursor> loader = mPresenter.onCreateLoader(-1, args);

        assertThat(loader, is(cursorLoader));
    }

    @Test
    public void testOnCreateLoaderCreatePhones() throws Exception {
        Bundle args = new Bundle();
        args.putLong("contacts_id", 1);
        CursorLoader cursorLoader = mock(CursorLoader.class);

        when(mContactCase.execute(any(QueryParams.class))).thenReturn(cursorLoader);

        Loader<Cursor> loader = mPresenter.onCreateLoader(1, args);

        assertThat(loader, is(cursorLoader));
    }

    @Test
    public void testOnSelectGroup() throws Exception {
        LoaderManager mockManager = mock(LoaderManager.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockCursor.getPosition()).thenReturn(1);
        when(mockCursor.getColumnIndexOrThrow(any(String.class))).thenReturn(1);
        when(mockCursor.getLong(any(Integer.class))).thenReturn(1L);

        ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);

        mPresenter.setLoaderManager(mockManager);
        mPresenter.onSelectGroup(mockCursor);

        verify(mockManager).restartLoader(anyInt(), argument.capture(), any(SearchContactPresenter.class));
        assertThat(argument.getValue().getLong("contacts_id"), is(1L));
    }

    @Test
    public void testOnGroupCollapseAction() throws Exception {
        LoaderManager mockManager = mock(LoaderManager.class);

        mPresenter.setLoaderManager(mockManager);
        mPresenter.onGroupCollapseAction(1);

        verify(mockManager).destroyLoader(1);
    }

    @Test
    public void testOnLoadFinishedForContacts() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockLoader.getId()).thenReturn(-1);
        when(mockCursor.getCount()).thenReturn(2);
        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setGroupCursor(any(Cursor.class));
    }

    @Test
    public void testOnLoadFinishedForContactsZero() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockLoader.getId()).thenReturn(-1);
        when(mockCursor.getCount()).thenReturn(0);

        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof CheckAvailableTextToSpeech.Callback) {
                ((CheckAvailableTextToSpeech.Callback) invocationOnMock.getArgument(0))
                        .onResult(CheckAvailableTextToSpeech.Result.AVAILABLE);
            }
            return null;
        }).when(mCheckTtsCase).execute(any(CheckAvailableTextToSpeech.Callback.class));
        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setGroupCursor(any(Cursor.class));
        verify(mReadText).startReading(mContext.getString(R.string.vr_search_not_found));
    }

    @Test
    public void testOnLoadFinishedForContactsOne() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        LoaderManager mockManager = mock(LoaderManager.class);
        when(mockLoader.getId()).thenReturn(-1);
        when(mockCursor.getCount()).thenReturn(1);
        mPresenter.takeView(mView);
        mPresenter.setLoaderManager(mockManager);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setGroupCursor(any(Cursor.class));
        verify(mockManager).restartLoader(anyInt(), any(Bundle.class), any(SearchContactPresenter.class));
    }

    @Test
    public void testOnLoadFinishedForPhones() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockLoader.getId()).thenReturn(1);
        when(mockCursor.isClosed()).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setChildrenCursor(eq(1), any(Cursor.class));
    }

    @Test
    public void testOnLoaderReset() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);

        when(mockLoader.getId()).thenReturn(1);

        mPresenter.takeView(mView);
        mPresenter.onLoaderReset(mockLoader);

        verify(mView, never()).setChildrenCursor(eq(1), any(Cursor.class));
    }

    @Test
    public void testOnNumberAction() throws Exception {
        Cursor mockCursor = mock(Cursor.class);

        when(mockCursor.getColumnIndexOrThrow(any(String.class))).thenReturn(1);
        when(mockCursor.getString(any(Integer.class))).thenReturn("08012345678");

        mPresenter.takeView(mView);
        mPresenter.onNumberAction(mockCursor);

        verify(mView).dial(eq("08012345678"));
        verify(mEventBus).post(any(GoBackEvent.class));
    }
}