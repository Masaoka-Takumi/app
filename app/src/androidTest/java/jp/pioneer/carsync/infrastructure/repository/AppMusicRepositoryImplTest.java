package jp.pioneer.carsync.infrastructure.repository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.content.QueryParams;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by NSW00_008320 on 2017/05/10.
 */
public class AppMusicRepositoryImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks AppMusicRepositoryImpl mAppMusicRepository = new AppMusicRepositoryImpl(){
        @Override
        AppMusicCursorLoader createCursorLoader(QueryParams params) {
            assertThat(params,is(mQueryParams));

            return mAppMusicCursorLoader;
        }
    };

    AppMusicCursorLoader mAppMusicCursorLoader;
    QueryParams mQueryParams;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mAppMusicCursorLoader = mock(AppMusicCursorLoader.class);
        mQueryParams = mock(QueryParams.class);
    }

    @Test
    public void get() throws Exception {
        // exercise
        AppMusicCursorLoader actual = mAppMusicRepository.get(mQueryParams);

        // verify
        assertThat(actual, is(mAppMusicCursorLoader));
    }

    @Test(expected = NullPointerException.class)
    public void getArgNull() throws Exception {
        // setup
        QueryParams queryParams = null;

        // exercise
        AppMusicCursorLoader appMusicCursorLoader = mAppMusicRepository.get(queryParams);

    }
}