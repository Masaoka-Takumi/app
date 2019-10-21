package jp.pioneer.carsync.domain.interactor;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.event.ShortcutKeyEvent;
import jp.pioneer.carsync.domain.model.ShortcutKey;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

/**
 * Created by NSW00_008320 on 2017/06/22.
 */
public class ActionSoftwareShortcutKeyTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ActionSoftwareShortcutKey mActionSoftwareShortcutKey;
    @Mock EventBus mEventBus;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void execute() throws Exception {
        // setup
        ArgumentCaptor<ShortcutKeyEvent> captor = ArgumentCaptor.forClass(ShortcutKeyEvent.class);

        // exercise
        mActionSoftwareShortcutKey.execute(ShortcutKey.APP);

        // verify
        verify(mEventBus).post(captor.capture());
        assertThat(captor.getValue().shortcutKey, is(ShortcutKey.APP));
    }

    @Test(expected = NullPointerException.class)
    public void execute_ArgNull() throws Exception {
        // exercise
        mActionSoftwareShortcutKey.execute(null);
    }

}