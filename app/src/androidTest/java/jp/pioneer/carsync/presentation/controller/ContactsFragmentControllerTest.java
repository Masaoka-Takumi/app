package jp.pioneer.carsync.presentation.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.contacts.ContactsContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.contacts.ContactsFavoriteFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.contacts.ContactsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.contacts.ContactsHistoryFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.home.HomeContainerFragment;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ContactsFragmentController のテスト
 */
public class ContactsFragmentControllerTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks TestContactsFragmentController mFragmentController = new TestContactsFragmentController();
    @Mock FragmentManager mFragmentManager;
    @Mock ContactsFragment mContactsFragment;
    @Mock ContactsHistoryFragment mContactsHistoryFragment;
    @Mock ContactsFavoriteFragment mContactsFavoriteFragment;
    @Mock View mView;

    class TestContactsFragmentController extends ContactsFragmentController {
        @Override
        Fragment createContactsFragment(Bundle args) {
            assertThat(args, is(Bundle.EMPTY));
            return mContactsFragment;
        }

        @Override
        Fragment createContactsHistoryFragment(Bundle args) {
            assertThat(args, is(Bundle.EMPTY));
            return mContactsHistoryFragment;
        }

        @Override
        Fragment createContactsFavoriteFragment(Bundle args) {
            assertThat(args, is(Bundle.EMPTY));
            return mContactsFavoriteFragment;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * getScreenIdInContainerのテスト
     */
    @Test
    public void testGetScreenIdInContainer() throws Exception {
        AbstractScreenFragment mockFragment = mock(AbstractScreenFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.getScreenId()).thenReturn(ScreenId.CONTACTS_LIST);

        mFragmentController.setContainerViewId(mView.getId());

        assertThat(mFragmentController.getScreenIdInContainer(), is(ScreenId.CONTACTS_LIST));
    }

    /**
     * getScreenIdInContainerのテスト fragment==nullの場合
     */
    @Test
    public void testGetScreenIdInContainerIsNull() throws Exception {
        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(null);

        mFragmentController.setContainerViewId(mView.getId());

        assertThat(mFragmentController.getScreenIdInContainer(), is(nullValue()));
    }

    /**
     * navigateのテスト
     */
    @Test
    public void testNavigate() throws Exception {
        FragmentTransaction mockTransaction = mock(FragmentTransaction.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(null);
        when(mFragmentManager.beginTransaction()).thenReturn(mockTransaction);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.CONTACTS_LIST, Bundle.EMPTY), is(true));
        assertThat(mFragmentController.navigate(ScreenId.CONTACTS_HISTORY, Bundle.EMPTY), is(true));
        assertThat(mFragmentController.navigate(ScreenId.CONTACTS_FAVORITE, Bundle.EMPTY), is(true));
        assertThat(mFragmentController.navigate(ScreenId.ANDROID_MUSIC, Bundle.EMPTY), is(false));

        verify(mockTransaction).replace(mView.getId(), mContactsFragment);
        verify(mockTransaction).replace(mView.getId(), mContactsHistoryFragment);
        verify(mockTransaction).replace(mView.getId(), mContactsFavoriteFragment);
    }

    /**
     * navigateのテスト
     * if (fragment instanceof OnNavigateListener)をはじく場合
     */
    @Test
    public void testNavigate2() throws Exception {
        FragmentTransaction mockTransaction = mock(FragmentTransaction.class);
        Fragment mockFragment = mock(Fragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mFragmentManager.beginTransaction()).thenReturn(mockTransaction);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.CONTACTS_LIST, Bundle.EMPTY), is(true));
        verify(mockTransaction).replace(mView.getId(), mContactsFragment);
    }

    /**
     * navigateのテスト
     * if (((OnNavigateListener) fragment).onNavigate(screenId, args))をはじく場合
     */
    @Test
    public void testNavigate3() throws Exception {
        FragmentTransaction mockTransaction = mock(FragmentTransaction.class);
        ContactsContainerFragment mockFragment = mock(ContactsContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onNavigate(ScreenId.CONTACTS_LIST, Bundle.EMPTY)).thenReturn(false);
        when(mFragmentManager.beginTransaction()).thenReturn(mockTransaction);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.CONTACTS_LIST, Bundle.EMPTY), is(true));
        verify(mockTransaction).replace(mView.getId(), mContactsFragment);
    }

    /**
     * navigateのテスト
     * if (((OnNavigateListener) fragment).onNavigate(screenId, args))の中に入る場合
     */
    @Test
    public void testNavigateInOther() throws Exception {
        ContactsContainerFragment mockFragment = mock(ContactsContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onNavigate(ScreenId.CONTACTS_LIST, Bundle.EMPTY)).thenReturn(true);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.CONTACTS_LIST, Bundle.EMPTY), is(true));
    }

    /**
     * goBackのテスト
     * fragmentがnullの場合
     */
    @Test
    public void testGoBack() throws Exception {
        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(null);

        assertThat(mFragmentController.goBack(), is(false));
    }

    /**
     * goBackのテスト
     * if (fragment instanceof OnGoBackListener)をはじく場合
     */
    @Test
    public void testGoBack2() throws Exception {
        Fragment mockFragment = mock(Fragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);

        assertThat(mFragmentController.goBack(), is(false));
    }

    /**
     * goBackのテスト
     * if (((OnGoBackListener) fragment).onGoBack())をはじく場合
     */
    @Test
    public void testGoBack3() throws Exception {
        HomeContainerFragment mockFragment = mock(HomeContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onGoBack()).thenReturn(false);

        assertThat(mFragmentController.goBack(), is(false));
    }

    /**
     * goBackのテスト
     * if (((OnGoBackListener) fragment).onGoBack())の中に入る場合
     */
    @Test
    public void testGoBackInOther() throws Exception {
        HomeContainerFragment mockFragment = mock(HomeContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onGoBack()).thenReturn(true);

        assertThat(mFragmentController.goBack(), is(true));
    }

    /**
     * goBackのテスト
     * if (mFragmentManager.getBackStackEntryCount() > 0)の中に入る場合
     */
    @Test
    public void testGoBackStack() throws Exception {
        ContactsContainerFragment mockFragment = mock(ContactsContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mFragmentManager.getBackStackEntryCount()).thenReturn(1);

        assertThat(mFragmentController.goBack(), is(true));
        verify(mFragmentManager).popBackStackImmediate();
    }

}