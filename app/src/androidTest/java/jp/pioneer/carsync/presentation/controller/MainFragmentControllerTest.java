package jp.pioneer.carsync.presentation.controller;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AccidentDetectDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.CautionDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SessionStoppedDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.contacts.ContactsContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.home.HomeContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.PlayerContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchContainerFragment;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MainFragmentControllerのテスト
 */
public class MainFragmentControllerTest {

    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks TestMainFragmentController mFragmentController = new TestMainFragmentController();
    @Mock FragmentManager mFragmentManager;
    @Mock HomeContainerFragment mHomeContainerFragment;
    @Mock PlayerContainerFragment mPlayerContainerFragment;
    @Mock ContactsContainerFragment mContactsContainerFragment;
    @Mock SearchContainerFragment mSearchContainerFragment;
    @Mock CautionDialogFragment mCautionDialogFragment;
    @Mock AccidentDetectDialogFragment mAccidentDetectDialogFragment;
    @Mock SessionStoppedDialogFragment mSessionStopDialogFragment;
    @Mock View mView;

    class TestMainFragmentController extends MainFragmentController {
        @Override
        Fragment createHomeContainerFragment(Bundle args) {
            assertThat(args, is(Bundle.EMPTY));
            return mHomeContainerFragment;
        }

        @Override
        Fragment createPlayerContainerFragment(Bundle args) {
            assertThat(args, is(Bundle.EMPTY));
            return mPlayerContainerFragment;
        }

        @Override
        Fragment createContactsContainerFragment(Bundle args) {
            assertThat(args, is(Bundle.EMPTY));
            return mContactsContainerFragment;
        }

//        @Override
//        Fragment createSearchContainerFragment(Bundle args) {
//            assertThat(args, is(Bundle.EMPTY));
//            return mSearchContainerFragment;
//        }

        @Override
        DialogFragment createCautionDialogFragment(Bundle args) {
            assertThat(args, is(Bundle.EMPTY));
            return mCautionDialogFragment;
        }

        @Override
        DialogFragment createAccidentDetectDialogFragment(Bundle args) {
            assertThat(args, is(Bundle.EMPTY));
            return mAccidentDetectDialogFragment;
        }

        @Override
        DialogFragment createSessionStopDialogFragment(Bundle args) {
            return mSessionStopDialogFragment;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void testGetScreenIdInContainer() throws Exception {
        AbstractScreenFragment mockFragment = mock(AbstractScreenFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.getScreenId()).thenReturn(ScreenId.HOME_CONTAINER);

        mFragmentController.setContainerViewId(mView.getId());

        assertThat(mFragmentController.getScreenIdInContainer(), is(ScreenId.HOME_CONTAINER));
    }

    @Test
    public void testGetScreenIdInContainerIsNull() throws Exception {
        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(null);

        mFragmentController.setContainerViewId(mView.getId());

        assertThat(mFragmentController.getScreenIdInContainer(), is(nullValue()));
    }

    @Test
    public void testNavigate() throws Exception {
        FragmentTransaction mockTransaction = mock(FragmentTransaction.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(null);
        when(mFragmentManager.beginTransaction()).thenReturn(mockTransaction);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.HOME_CONTAINER, Bundle.EMPTY), is(true));
        assertThat(mFragmentController.navigate(ScreenId.PLAYER_CONTAINER, Bundle.EMPTY), is(true));
        assertThat(mFragmentController.navigate(ScreenId.CONTACTS_CONTAINER, Bundle.EMPTY), is(true));
//        assertThat(mFragmentController.navigate(ScreenId.SEARCH_CONTAINER, Bundle.EMPTY), is(true));
        assertThat(mFragmentController.navigate(ScreenId.ANDROID_MUSIC, Bundle.EMPTY), is(false));

        verify(mockTransaction).replace(mView.getId(), mHomeContainerFragment);
        verify(mockTransaction).replace(mView.getId(), mPlayerContainerFragment);
        verify(mockTransaction).replace(mView.getId(), mContactsContainerFragment);
//        verify(mockTransaction).replace(mView.getId(), mSearchContainerFragment);
    }

    @Test
    public void testNavigate2() throws Exception {
        FragmentTransaction mockTransaction = mock(FragmentTransaction.class);
        Fragment mockFragment = mock(Fragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mFragmentManager.beginTransaction()).thenReturn(mockTransaction);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.HOME_CONTAINER, Bundle.EMPTY), is(true));
        verify(mockTransaction).replace(mView.getId(), mHomeContainerFragment);
    }

    @Test
    public void testNavigate3() throws Exception {
        FragmentTransaction mockTransaction = mock(FragmentTransaction.class);
        HomeContainerFragment mockFragment = mock(HomeContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onNavigate(ScreenId.HOME, Bundle.EMPTY)).thenReturn(false);
        when(mFragmentManager.beginTransaction()).thenReturn(mockTransaction);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.HOME_CONTAINER, Bundle.EMPTY), is(true));
        verify(mockTransaction).replace(mView.getId(), mHomeContainerFragment);
    }

    @Test
    public void testNavigateInOther() throws Exception {
        HomeContainerFragment mockFragment = mock(HomeContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onNavigate(ScreenId.HOME, Bundle.EMPTY)).thenReturn(true);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.HOME, Bundle.EMPTY), is(true));
    }

    @Test
    public void testGoBack() throws Exception {
        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(null);

        assertThat(mFragmentController.goBack(), is(false));
    }

    @Test
    public void testGoBack2() throws Exception {
        Fragment mockFragment = mock(Fragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);

        assertThat(mFragmentController.goBack(), is(false));
    }

    @Test
    public void testGoBack3() throws Exception {
        HomeContainerFragment mockFragment = mock(HomeContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onGoBack()).thenReturn(false);

        assertThat(mFragmentController.goBack(), is(false));
    }

    @Test
    public void testGoBackInOther() throws Exception {
        HomeContainerFragment mockFragment = mock(HomeContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onGoBack()).thenReturn(true);

        assertThat(mFragmentController.goBack(), is(true));
    }

    @Test
    public void testShowCaution() throws Exception {
        mFragmentController.showCaution(Bundle.EMPTY);
        verify(mFragmentManager).executePendingTransactions();
    }

    @Test
    public void testIsShowCaution() throws Exception {
        when(mFragmentManager.findFragmentByTag("caution")).thenReturn(new Fragment());
        assertThat(mFragmentController.isShowCaution(), is(true));
    }

    @Test
    public void testIsNotShowCaution() throws Exception {
        when(mFragmentManager.findFragmentByTag("caution")).thenReturn(null);
        assertThat(mFragmentController.isShowCaution(), is(false));
    }

    @Test
    public void testShowAccidentDetect() throws Exception {
        mFragmentController.showAccidentDetect(Bundle.EMPTY);
        verify(mFragmentManager).executePendingTransactions();
    }

    @Test
    public void testIsShowAccidentDetect() throws Exception {
        when(mFragmentManager.findFragmentByTag("accident_detect")).thenReturn(new Fragment());
        assertThat(mFragmentController.isShowAccidentDetect(), is(true));
    }

    @Test
    public void testIsNotShowAccidentDetect() throws Exception {
        when(mFragmentManager.findFragmentByTag("accident_detect")).thenReturn(null);
        assertThat(mFragmentController.isShowAccidentDetect(), is(false));
    }

    @Test
    public void testShowSessionStopped() throws Exception {
        mFragmentController.showSessionStopped(Bundle.EMPTY);
        verify(mFragmentManager).executePendingTransactions();
    }

    @Test
    public void testIsShowSessionStopped() throws Exception {
        when(mFragmentManager.findFragmentByTag("session_stop")).thenReturn(new Fragment());
        assertThat(mFragmentController.isShowSessionStopped(), is(true));
    }

    @Test
    public void testIsNotShowSessionStopped() throws Exception {
        when(mFragmentManager.findFragmentByTag("session_stop")).thenReturn(null);
        assertThat(mFragmentController.isShowSessionStopped(), is(false));
    }
}