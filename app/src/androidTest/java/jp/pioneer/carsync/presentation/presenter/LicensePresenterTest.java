package jp.pioneer.carsync.presentation.presenter;

import org.junit.After;
import org.junit.Before;

import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * Created by NSW00_906320 on 2017/06/01.
 */
public class LicensePresenterTest {
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @After
    public void tearDown() throws Exception {

    }
}