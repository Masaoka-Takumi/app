package jp.pioneer.carsync.domain.model;

import android.content.Context;
import android.test.InstrumentationTestCase;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Created by NSW00_008316 on 2017/11/17.
 */

public class ThemeTypeTest extends InstrumentationTestCase {

    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }

    @Test
    public void testVideoPattern() throws Exception {
        ThemeType type = ThemeType.VIDEO_PATTERN1;

        assertThat(type.isVideo(), is(true));

        List<ZoneFrameInfo> datum = type.getFlashPattern().get(getApplicationContext());
        for (ZoneFrameInfo info : datum) {
//            assertThat(info.zone1.red, is(60));
//            assertThat(info.zone1.green, is(60));
//            assertThat(info.zone1.blue, is(60));
//
//            assertThat(info.zone2.red, is(60));
//            assertThat(info.zone2.green, is(60));
//            assertThat(info.zone2.blue, is(60));
//
//            assertThat(info.duration, is(10));
        }
    }
}
