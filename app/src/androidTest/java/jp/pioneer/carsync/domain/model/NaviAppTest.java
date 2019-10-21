package jp.pioneer.carsync.domain.model;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static jp.pioneer.carsync.domain.model.NaviApp.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * NaviAppのテスト.
 */
@RunWith(Enclosed.class)
public class NaviAppTest {
    // fromPackageNameNoThrowもテストする
    @RunWith(Enclosed.class)
    public static class FromPackageNameTest {
        @RunWith(Theories.class)
        public static class ValidData {
            static class Fixture {
                String packageName;
                NaviApp expected;

                Fixture(String packageName, NaviApp expected) {
                    this.packageName = packageName;
                    this.expected = expected;
                }
            }

            @DataPoints
            public static final Fixture[] FIXTURES = {
                    new Fixture("com.google.android.apps.maps", GOOGLE_MAP),
                    new Fixture("com.here.app.maps", HERE_WE_GO),
                    new Fixture("inrix.android.ui", INRIX),
                    new Fixture("jp.co.incrementp.mapfan.navi", MAP_FAN),
                    new Fixture("com.navitime.local.navitimedrive", NAVI_TIME),
                    new Fixture("com.sygic.aura", SYGIC),
                    new Fixture("com.waze", WAZE),
                    new Fixture("jp.co.yahoo.android.apps.navi", YAHOO_CAR_NAVI),
                    new Fixture("ru.yandex.yandexnavi", YANDEX),
            };

            @Theory
            public void fromPackageName(Fixture fixture) throws Exception {
                assertThat(NaviApp.fromPackageName(fixture.packageName), is(fixture.expected));
            }

            @Theory
            public void fromPackageNameNoThrow(Fixture fixture) throws Exception {
                assertThat(NaviApp.fromPackageNameNoThrow(fixture.packageName), is(fixture.expected));
            }
        }

        @RunWith(Theories.class)
        public static class InvalidData {
            @Rule
            public ExpectedException mExpectedException = ExpectedException.none();

            @DataPoints
            public static final String[] DATA = {
                    "com.google.android.apps.mapss",
                    "hoge",
                    ""
            };

            @Theory
            public void fromPackageName(String packageName) throws Exception {
                mExpectedException.expect(IllegalArgumentException.class);
                NaviApp.fromPackageName(packageName);
            }

            @Theory
            public void fromPackageNameNoThrow(String packageName) throws Exception {
                assertThat(NaviApp.fromPackageNameNoThrow(packageName), is(nullValue()));
            }
        }
    }

    public static class CreateMainIntentTest {
        @Test
        public void GOOGLE_MAP() {
            // exercise
            Intent actual = GOOGLE_MAP.createMainIntent();

            // verify
            assertThat(actual.getAction(), is(GOOGLE_MAP.getAction()));
            assertThat(actual.getPackage(), is(GOOGLE_MAP.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void HERE_WE_GO() {
            // exercise
            Intent actual = HERE_WE_GO.createMainIntent();

            // verify
            assertThat(actual.getAction(), is(HERE_WE_GO.getAction()));
            assertThat(actual.getPackage(), is(HERE_WE_GO.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void INRIX() {
            // exercise
            Intent actual = INRIX.createMainIntent();

            // verify
            assertThat(actual.getAction(), is(INRIX.getAction()));
            assertThat(actual.getPackage(), is(INRIX.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void MAP_FAN() {
            // exercise
            Intent actual = MAP_FAN.createMainIntent();

            // verify
            assertThat(actual.getAction(), is(MAP_FAN.getAction()));
            assertThat(actual.getComponent(),
                    is(new ComponentName(MAP_FAN.getPackageName(), "md5e55881333aafba1b96e9db1c06fdbb3f.SplashActivity")));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void NAVI_TIME() {
            // exercise
            Intent actual = NAVI_TIME.createMainIntent();

            // verify
            assertThat(actual.getAction(), is(NAVI_TIME.getAction()));
            assertThat(actual.getPackage(), is(NAVI_TIME.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void SYGIC() {
            // exercise
            Intent actual = SYGIC.createMainIntent();

            // verify
            assertThat(actual.getAction(), is(SYGIC.getAction()));
            assertThat(actual.getPackage(), is(SYGIC.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void WAZE() {
            // exercise
            Intent actual = WAZE.createMainIntent();

            // verify
            assertThat(actual.getAction(), is(WAZE.getAction()));
            assertThat(actual.getPackage(), is(WAZE.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void YAHOO_CAR_NAVI() {
            // exercise
            Intent actual = YAHOO_CAR_NAVI.createMainIntent();

            // verify
            assertThat(actual.getAction(), is(YAHOO_CAR_NAVI.getAction()));
            assertThat(actual.getPackage(), is(YAHOO_CAR_NAVI.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void YANDEX() {
            // exercise
            Intent actual = YANDEX.createMainIntent();

            // verify
            assertThat(actual.getAction(), is(YANDEX.getAction()));
            assertThat(actual.getPackage(), is(YANDEX.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    public static class CreateNavigationIntentTest {
        private static final double LATITUDE = 123.5f;
        private static final double LONGITUDE = 1.234f;

        @Test
        public void GOOGLE_MAP() {
            // exercise
            Intent actual = GOOGLE_MAP.createNavigationIntent(LATITUDE, LONGITUDE);

            // verify
            assertThat(actual.getAction(), is(GOOGLE_MAP.getAction()));
            assertThat(actual.getData(), is(Uri.parse("google.navigation:q=" + LATITUDE + "," + LONGITUDE + "&mode=d")));
            assertThat(actual.getPackage(), is(GOOGLE_MAP.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void HERE_WE_GO() {
            // exercise
            Intent actual = HERE_WE_GO.createNavigationIntent(LATITUDE, LONGITUDE);

            // verify
            assertThat(actual.getAction(), is(HERE_WE_GO.getAction()));
            assertThat(actual.getData(), is(Uri.parse("geo:" + LATITUDE + "," + LONGITUDE)));
            assertThat(actual.getPackage(), is(HERE_WE_GO.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void INRIX() {
            // exercise
            Intent actual = INRIX.createNavigationIntent(LATITUDE, LONGITUDE);

            // verify
            assertThat(actual.getAction(), is(INRIX.getAction()));
            assertThat(actual.getData(), is(Uri.parse("geo:" + LATITUDE + "," + LONGITUDE)));
            assertThat(actual.getPackage(), is(INRIX.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void MAP_FAN() {
            // exercise
            Intent actual = MAP_FAN.createNavigationIntent(LATITUDE, LONGITUDE);

            // verify
            assertThat(actual.getAction(), is(MAP_FAN.getAction()));
            assertThat(actual.getComponent(),
                    is(new ComponentName(MAP_FAN.getPackageName(), "md5e55881333aafba1b96e9db1c06fdbb3f.SplashActivity")));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void NAVI_TIME() {
            // exercise
            Intent actual = NAVI_TIME.createNavigationIntent(LATITUDE, LONGITUDE);

            // verify
            assertThat(actual.getAction(), is(NAVI_TIME.getAction()));
            assertThat(actual.getData(), is(Uri.parse("geo:" + LATITUDE + "," + LONGITUDE)));
            assertThat(actual.getPackage(), is(NAVI_TIME.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void SYGIC() {
            // exercise
            Intent actual = SYGIC.createNavigationIntent(LATITUDE, LONGITUDE);

            // verify
            assertThat(actual.getAction(), is(SYGIC.getAction()));
            assertThat(actual.getData(),
                    is(Uri.parse("com.sygic.aura://coordinate|" + LONGITUDE + "|" + LATITUDE + "|drive")));
            assertThat(actual.getPackage(), is(SYGIC.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void WAZE() {
            // exercise
            Intent actual = WAZE.createNavigationIntent(LATITUDE, LONGITUDE);

            // verify
            assertThat(actual.getAction(), is(WAZE.getAction()));
            assertThat(actual.getData(),
                    is(Uri.parse("waze://?ll=" + LATITUDE + "," + LONGITUDE + "&navigate=yes")));
            assertThat(actual.getPackage(), is(WAZE.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void YAHOO_CAR_NAVI() {
            // exercise
            Intent actual = YAHOO_CAR_NAVI.createNavigationIntent(LATITUDE, LONGITUDE);

            // verify
            assertThat(actual.getAction(), is(YAHOO_CAR_NAVI.getAction()));
            assertThat(actual.getData(), is(Uri.parse("geo:" + LATITUDE + "," + LONGITUDE)));
            assertThat(actual.getPackage(), is(YAHOO_CAR_NAVI.getPackageName()));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        @Test
        public void YANDEX() {
            // exercise
            Intent actual = YANDEX.createNavigationIntent(LATITUDE, LONGITUDE);

            // verify
            assertThat(actual.getAction(), is(YANDEX.getAction()));
            assertThat(actual.getPackage(), is(YANDEX.getPackageName()));
            assertThat(actual.getDoubleExtra("lat_to", 0), is(LATITUDE));
            assertThat(actual.getDoubleExtra("lon_to", 0), is(LONGITUDE));
            assertThat(actual.getFlags(), is(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}