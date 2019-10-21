package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.v4.content.CursorLoader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/22.
 */
public class CreateUsbListTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks CreateUsbList mCreateUsbList;
    @Mock Handler mHandler;
    @Mock CarDeviceMediaRepository mCarDeviceMediaRepository;

    @Mock CursorLoader mCursorLoader;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mCarDeviceMediaRepository.getUsbList()).thenReturn(mCursorLoader);
    }

    @Test
    public void execute() throws Exception {
        // exercise
        CursorLoader actual = mCreateUsbList.execute();

        // verify
        assertThat(actual, is(mCursorLoader));
    }

    @Test
    public void addWantedListItemIndex() throws Exception {
        // exercise
        mCreateUsbList.addWantedListItemIndex(10);

        // verify
        verify(mCarDeviceMediaRepository).addWantedUsbListItemIndex(10);
    }

    @Test
    public void removeWantedListItemIndex() throws Exception {
        // exercise
        mCreateUsbList.removeWantedListItemIndex(10);

        // verify
        verify(mCarDeviceMediaRepository).removeWantedUsbListItemIndex(10);
    }

}