package jp.pioneer.carsync.domain.interactor;

import android.support.v4.content.CursorLoader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.content.QuerySettingListParams;
import jp.pioneer.carsync.domain.content.SettingListContract;
import jp.pioneer.carsync.domain.repository.SettingListRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/13.
 */
public class QuerySettingListTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks QuerySettingList mQuerySettingList;
    @Mock SettingListRepository mSettingListRepository;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void execute() throws Exception {
        // setup
        QuerySettingListParams params = SettingListContract.QuerySettingListParamsBuilder.createDeviceList();
        CursorLoader expected = mock(CursorLoader.class);
        when(mSettingListRepository.getSettingList(eq(params))).thenReturn(expected);

        // exercise
        CursorLoader actual = mQuerySettingList.execute(params);

        // verify
        assertThat(actual, is(expected));

    }

    @Test(expected = NullPointerException.class)
    public void execute_ArgNull() throws Exception {
        // exercise
        CursorLoader actual = mQuerySettingList.execute(null);
    }

}