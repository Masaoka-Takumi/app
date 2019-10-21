package jp.pioneer.carsync.domain.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static jp.pioneer.carsync.domain.model.MessagingApp.FACEBOOK_MESSENGER;
import static jp.pioneer.carsync.domain.model.MessagingApp.HANGOUTS;
import static jp.pioneer.carsync.domain.model.MessagingApp.LINE;
import static jp.pioneer.carsync.domain.model.MessagingApp.MESSENGER;
import static jp.pioneer.carsync.domain.model.MessagingApp.VIBER;
import static jp.pioneer.carsync.domain.model.MessagingApp.VK;
import static jp.pioneer.carsync.domain.model.MessagingApp.WE_CHAT;
import static jp.pioneer.carsync.domain.model.MessagingApp.WHATS_APP_MESSENGER;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

/**
 * MessagingAppのテスト.
 */
@RunWith(Enclosed.class)
public class MessagingAppTest {
    @RunWith(Theories.class)
    public static class ValidData {
        static class Fixture {
            String packageName;
            MessagingApp expected;

            Fixture(String packageName, MessagingApp expected) {
                this.packageName = packageName;
                this.expected = expected;
            }
        }

        @DataPoints
        public static final Fixture[] FIXTURES = {
                new Fixture("com.facebook.katana", FACEBOOK_MESSENGER),
                new Fixture("com.google.android.talk", HANGOUTS),
                new Fixture("jp.naver.line.android", LINE),
                new Fixture("com.facebook.orca", MESSENGER),
                new Fixture("com.vkontakte.android", VK),
                new Fixture("com.viber.voip", VIBER),
                new Fixture("com.tencent.mm", WE_CHAT),
                new Fixture("com.whatsapp", WHATS_APP_MESSENGER)
        };

        @Theory
        public void fromPackageName(Fixture fixture) throws Exception {
            assertThat(MessagingApp.fromPackageName(fixture.packageName), is(fixture.expected));
        }

        @Theory
        public void fromPackageNameNoThrow(Fixture fixture) throws Exception {
            assertThat(MessagingApp.fromPackageName(fixture.packageName), is(fixture.expected));
        }
    }

    @RunWith(Theories.class)
    public static class InvalidData {
        @Rule
        public ExpectedException mExpectedException = ExpectedException.none();

        @DataPoints
        public static final String[] DATA = {
                "com.facebook.katanaa",
                "hoge",
                ""
        };

        @Theory
        public void fromPackageName(String packageName) throws Exception {
            mExpectedException.expect(IllegalArgumentException.class);
            MessagingApp.fromPackageName(packageName);
        }

        @Theory
        public void fromPackageNameNoThrow(String packageName) throws Exception {
            assertThat(MessagingApp.fromPackageNameNoThrow(packageName), is(nullValue()));
        }
    }

    public static class NullData {
        @Test(expected = NullPointerException.class)
        public void fromPackageName() throws Exception {
            MessagingApp.fromPackageName(null);
        }

        @Test(expected = NullPointerException.class)
        public void fromPackageNameNoThrow() throws Exception {
            assertThat(MessagingApp.fromPackageNameNoThrow(null), is(nullValue()));
        }
    }
}