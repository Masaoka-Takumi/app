package jp.pioneer.carsync.presentation.presenter;

import android.os.Bundle;

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


import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.ContactsContainerView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * ContactsContainerPresenterのテスト
 */
public class ContactsContainerPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ContactsContainerPresenter mPresenter = new ContactsContainerPresenter();
    @Mock ContactsContainerView mView;
    @Mock EventBus mEventBus;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        mPresenter.mTab = ContactsContainerPresenter.ContactsTab.CONTACTS;
    }
    @After
    public void tearDown() throws Exception {

    }

    /**
     * onInitializeのテスト
     */
    @Test
    public void testOnInitialize() throws Exception {
        mPresenter.onInitialize();

        verify(mView).onNavigate(ScreenId.CONTACTS_LIST, Bundle.EMPTY);
        verify(mView).setCurrentTab(ContactsContainerPresenter.ContactsTab.CONTACTS);
        assertThat(mPresenter.mTab, is(ContactsContainerPresenter.ContactsTab.CONTACTS));
    }

    /**
     * onSaveInstanceStateのテスト
     */
    @Test
    public void testOnSaveInstanceState() throws Exception {
        Bundle outState = new Bundle();
        mPresenter.onSaveInstanceState(outState);
        assertThat(outState.getString("screen"), is(ContactsContainerPresenter.ContactsTab.CONTACTS.name()));
    }

    /**
     * onRestoreInstanceStateのテスト
     */
    @Test
    public void testOnRestoreInstanceState() throws Exception {
        Bundle savedInstanceState = new Bundle();
        savedInstanceState.putString("screen", ContactsContainerPresenter.ContactsTab.CONTACTS.name());
        mPresenter.onRestoreInstanceState(savedInstanceState);
        assertThat(mPresenter.mTab, is(ContactsContainerPresenter.ContactsTab.CONTACTS));
        verify(mView).setCurrentTab(ContactsContainerPresenter.ContactsTab.CONTACTS);
    }

    /**
     * onTabActionのテスト Historyタブの場合
     */
    @Test
    public void testOnTabActionHistory() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onTabAction(ContactsContainerPresenter.ContactsTab.HISTORY);
        verify(mView).setCurrentTab(ContactsContainerPresenter.ContactsTab.HISTORY);
        assertThat(mPresenter.mTab, is(ContactsContainerPresenter.ContactsTab.HISTORY));
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.CONTACTS_HISTORY));
    }

    /**
     * onTabActionのテスト Historyタブの場合（遷移しない）
     */
    @Test
    public void testOnTabActionHistoryNo() throws Exception {
        mPresenter.mTab = ContactsContainerPresenter.ContactsTab.HISTORY;
        mPresenter.onTabAction(ContactsContainerPresenter.ContactsTab.HISTORY);
        verify(mView,times(0)).setCurrentTab(ContactsContainerPresenter.ContactsTab.HISTORY);
        assertThat(mPresenter.mTab, is(ContactsContainerPresenter.ContactsTab.HISTORY));
        verify(mEventBus,times(0)).post(any(NavigateEvent.class));
    }

    /**
     * onTabActionのテスト Favoritesタブの場合
     */
    @Test
    public void testOnTabActionFavorite() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onTabAction(ContactsContainerPresenter.ContactsTab.FAVORITE);
        verify(mView).setCurrentTab(ContactsContainerPresenter.ContactsTab.FAVORITE);
        assertThat(mPresenter.mTab, is(ContactsContainerPresenter.ContactsTab.FAVORITE));
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.CONTACTS_FAVORITE));
    }

    /**
     * onTabActionのテスト Favoritesタブの場合（遷移しない）
     */
    @Test
    public void testOnTabActionFavoriteNoMove() throws Exception {
        mPresenter.mTab = ContactsContainerPresenter.ContactsTab.FAVORITE;
        mPresenter.onTabAction(ContactsContainerPresenter.ContactsTab.FAVORITE);
        verify(mView,times(0)).setCurrentTab(ContactsContainerPresenter.ContactsTab.FAVORITE);
        assertThat(mPresenter.mTab, is(ContactsContainerPresenter.ContactsTab.FAVORITE));
        verify(mEventBus,times(0)).post(any(NavigateEvent.class));
    }

    /**
     * onTabActionのテスト Contactsタブの場合
     */
    @Test
    public void testOnTabActionContacts() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.mTab = ContactsContainerPresenter.ContactsTab.HISTORY;
        mPresenter.onTabAction(ContactsContainerPresenter.ContactsTab.CONTACTS);
        verify(mView).setCurrentTab(ContactsContainerPresenter.ContactsTab.CONTACTS);
        assertThat(mPresenter.mTab, is(ContactsContainerPresenter.ContactsTab.CONTACTS));
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.CONTACTS_LIST));
    }

    /**
     * onTabActionのテスト Contactsタブの場合（遷移しない）
     */
    @Test
    public void testOnTabActionContactsNoMove() throws Exception {
        mPresenter.onTabAction(ContactsContainerPresenter.ContactsTab.CONTACTS);
        verify(mView,times(0)).setCurrentTab(ContactsContainerPresenter.ContactsTab.CONTACTS);
        assertThat(mPresenter.mTab, is(ContactsContainerPresenter.ContactsTab.CONTACTS));
        verify(mEventBus,times(0)).post(any(NavigateEvent.class));
    }
    /**
     * onBackActionのテスト
     */
    //@Test
    public void testOnBackAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onBackAction();
        verify(mEventBus).post(argument.capture());
        assertThat(argument.getValue(), is(instanceOf(GoBackEvent.class)));
    }

}