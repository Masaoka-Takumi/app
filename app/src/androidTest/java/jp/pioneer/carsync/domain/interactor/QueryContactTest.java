package jp.pioneer.carsync.domain.interactor;

import android.support.v4.content.CursorLoader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.repository.ContactRepository;


import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.is;

/**
 * QueryContactのテスト.
 */
public class QueryContactTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks QueryContact mQueryContact;
    @Mock ContactRepository mContactRepository;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void execute() throws Exception {
        // setup
        CursorLoader expected = mock(CursorLoader.class);
        when(mContactRepository.get(any(QueryParams.class))).thenReturn(expected);

        // exercise
        CursorLoader actual = mQueryContact.execute(mock(QueryParams.class));

        // verify
        assertThat(actual,is(expected));

    }

    @Test(expected = NullPointerException.class)
    public  void executeArgNull() throws  Exception{
        // exercise
        QueryParams queryParams = null;
        CursorLoader actual = mQueryContact.execute(queryParams);
    }

}