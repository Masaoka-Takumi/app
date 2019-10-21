package jp.pioneer.carsync.domain.interactor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.repository.AppMusicRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/09.
 */
public class QueryAppMusicTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks QueryAppMusic mQueryAppMusic;
    @Mock AppMusicRepository mRepository;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void execute() throws Exception {
        // setup
        QueryParams queryParams = mock(QueryParams.class);
        AppMusicCursorLoader expected = mock(AppMusicCursorLoader.class);
        when(mRepository.get(eq(queryParams))).thenReturn(expected);

        // exercise
        AppMusicCursorLoader actual = mQueryAppMusic.execute(queryParams);

        // verify
        assertThat(actual,is(expected));
    }

    @Test(expected = NullPointerException.class)
    public void executeArgNull() throws Exception {
        // setup
        QueryParams queryParams = null;

        // exercise
        AppMusicCursorLoader actual = mQueryAppMusic.execute(queryParams);
    }

}