package jp.pioneer.carsync.domain.interactor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.repository.StatusHolderRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/24.
 */
public class GetStatusHolderTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks GetStatusHolder mGetStatusHolder;
    @Mock StatusHolderRepository mStatusHolderRepository;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void execute() throws Exception {
        // setup
        StatusHolder expected = mock(StatusHolder.class);
        when(mStatusHolderRepository.get()).thenReturn(expected);

        // exercise
        StatusHolder actual = mGetStatusHolder.execute();

        // verify
        assertThat(actual,is(expected));

    }

}