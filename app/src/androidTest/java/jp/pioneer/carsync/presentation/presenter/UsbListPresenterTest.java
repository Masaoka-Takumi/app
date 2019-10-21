package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.CreateUsbList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.UsbListView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * USBリストのPresenterの
 */
public class UsbListPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks UsbListPresenter mPresenter = new UsbListPresenter();
    @Mock UsbListView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetCase;
    @Mock ControlMediaList mMediaCase;
    @Mock CreateUsbList mCreateUsbList;
    private static final int LOADER_ID_USB_LIST = -1;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void onResume() throws Exception {

    }

    @Test
    public void onPause() throws Exception {

    }

    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager mockLoader = mock(LoaderManager.class);

        mPresenter.setLoaderManager(mockLoader);

        verify(mockLoader).initLoader(eq(-1), eq(null), any(UsbListPresenter.class));
    }

    @Test
    public void testOnCreateLoader() throws Exception {

        mPresenter.onCreateLoader(-1, Bundle.EMPTY);

        verify(mCreateUsbList).execute();
    }

    @Test
    public void testOnLoadFinished() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockLoader.getId()).thenReturn(LOADER_ID_USB_LIST);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setCursor(mockCursor);
    }

    @Test
    public void testOnLoaderReset() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);

        mPresenter.onLoaderReset(mockLoader);

        //verify(mView).setCursor(null);
    }

    @Test
    public void onSelectListOnFile() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.listType = ListType.LIST;
        carDeviceStatus.sourceType = MediaSourceType.USB;
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        Cursor mockCursor = mock(Cursor.class);

        when(mockCursor.getColumnIndexOrThrow("enabled")).thenReturn(1);
        when(mockCursor.getInt(1)).thenReturn(1);
        when(mockCursor.getColumnIndexOrThrow("type")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0);
        mPresenter.onSelectList(1, mockCursor);

        verify(mMediaCase).selectListItem(1);
        verify(mMediaCase).exitList();
    }

    @Test
    public void testOnSelectListOnFolder() throws Exception {
        Cursor mockCursor = mock(Cursor.class);

        when(mockCursor.getColumnIndexOrThrow("type")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(1);
        when(mockCursor.getColumnIndexOrThrow("text")).thenReturn(1);
        when(mockCursor.getString(1)).thenReturn("Test Folder");
        mPresenter.onSelectList(1, mockCursor);

        verify(mMediaCase).selectListItem(1);
        verify(mView).setTitle("Test Folder");

        mPresenter.onBackAction();
        verify(mMediaCase).goBack();
        verify(mView).setTitle(mContext.getString(R.string.usb_list_title));

    }

    @Test
    public void testOnAddListItem() throws Exception {
        mPresenter.onAddListItem(1, 10);
        verify(mCreateUsbList, times(10)).addWantedListItemIndex(anyInt());
    }

    @Test
    public void testOnRemoveListItem() throws Exception {
        mPresenter.onRemoveListItem(1, 10);
        verify(mCreateUsbList, times(10)).removeWantedListItemIndex(anyInt());
    }

    @Test
    public void testOnBackAction() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.listType = ListType.LIST;
        carDeviceStatus.sourceType = MediaSourceType.USB;
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onBackAction();
        verify(mMediaCase).goBack();
        verify(mMediaCase).exitList();
    }

    @Test
    public void onCrpListUpdateEvent() throws Exception {

    }

}