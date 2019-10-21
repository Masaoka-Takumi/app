package jp.pioneer.carsync.domain.model;

import android.app.Notification;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;

import static android.app.Notification.EXTRA_TEXT;
import static android.app.Notification.EXTRA_TITLE;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.v4.app.NotificationCompat.EXTRA_LARGE_ICON;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/17.
 */
@RunWith(Enclosed.class)
public class ApplicationNotificationTest{

    @RunWith(Theories.class)
    public static class ApplicationNotificationTest_DefaultNotificationImpl {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        @Mock ApplicationInfoRepository mAppInfoRepository;
        @Mock PackageManager mPackageManager;
        @Mock Resources mResources;
        @Mock StatusBarNotification mStatusBarNotification;
        @Mock Notification mNotification;
        @Mock ApplicationInfo mApplicationInfo;

        private void SetMocks(DefaultNotificationImpl fixture){
            fixture.mResources = mResources;
            fixture.mAppInfoRepository = mAppInfoRepository;
            fixture.mPackageManager = mPackageManager;
        }

        @DataPoints
        public static DefaultNotificationImpl[] FIXTURES = new DefaultNotificationImpl[]{
                new FacebookMessengerNotification(),
                new MessengerNotification(),
                new VKNotification(),
                new WhatsAppMessengerNotification(),
        };

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            when(mStatusBarNotification.getPackageName()).thenReturn("TEST");
            when(mStatusBarNotification.getNotification()).thenReturn(mNotification);
        }

        @Theory
        public void isReadTarget(DefaultNotificationImpl fixture) {
            // verify
            assertThat(fixture.isReadTarget(),is(true));
        }

        @Theory
        public void getTitle(DefaultNotificationImpl fixture) {
            // setup
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TITLE,"TEST");
            mNotification.extras = bundle;
            SetMocks(fixture);
            fixture.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = fixture.getTitle();

            // verify
            assertThat(actual,is("TEST"));

        }

        @Theory
        public void getText(DefaultNotificationImpl fixture) {
            // setup
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TEXT,"TEST");
            mNotification.extras = bundle;
            SetMocks(fixture);
            fixture.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = fixture.getText();

            // verify
            assertThat(actual,is("TEST"));

        }

        @Theory
        public void setStatusBarNotification(DefaultNotificationImpl fixture) throws Exception {
            // setup
            SetMocks(fixture);

            // exercise
            AbstractNotification actual = fixture.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(actual,is(fixture));

        }

        @Theory
        @Test(expected = NullPointerException.class)
        public void setStatusBarNotificationArgNull(DefaultNotificationImpl fixture) throws Exception {
            // setup
            StatusBarNotification statusBarNotification = null;

            // exercise
            AbstractNotification actual = fixture.setStatusBarNotification(statusBarNotification);

        }

        @Theory
        @Test(expected = IllegalArgumentException.class)
        public void setStatusBarNotificationAppInfoNull(DefaultNotificationImpl fixture) throws Exception {
            // setup
            when(mAppInfoRepository.get("TEST")).thenReturn(null);
            SetMocks(fixture);

            // exercise
            AbstractNotification actual =  fixture.setStatusBarNotification(mStatusBarNotification);

        }

        @Theory
        public void getApplicationIcon(DefaultNotificationImpl fixture) throws Exception {
            // setup
            Drawable expected = mock(Drawable.class);
            when(mApplicationInfo.loadIcon(any(PackageManager.class))).thenReturn(expected);
            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            SetMocks(fixture);
            fixture.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = fixture.getApplicationIcon();

            // verify
            assertThat(actual,is(expected));

        }

        @Theory
        public void getApplicationName(DefaultNotificationImpl fixture) throws Exception {
            // setup
            String expected = "TEST";
            when(mApplicationInfo.loadLabel(mPackageManager)).thenReturn(expected);
            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            SetMocks(fixture);
            fixture.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = fixture.getApplicationName();

            // verify
            assertThat(actual,is(expected));

        }

        @Theory
        public void getNotificationIconReturnNotNull(DefaultNotificationImpl fixture) throws Exception {
            // setup
            Bundle bundle = new Bundle();
            Bitmap bitmap = Bitmap.createBitmap(10,10,Bitmap.Config.ALPHA_8);
            Resources resources = Resources.getSystem();
            Notification notification = new Notification();
            bundle.putParcelable(EXTRA_LARGE_ICON,bitmap);
            notification.extras = bundle;
            when(mStatusBarNotification.getNotification()).thenReturn(notification);
            SetMocks(fixture);
            fixture.mResources = resources;
            fixture.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = fixture.getNotificationIcon();

            // verify
            assertThat(actual,instanceOf(Drawable.class));

        }

        @Theory
        public void getNotificationIconReturnNull(DefaultNotificationImpl fixture) throws Exception {
            // setup
            Bundle bundle = new Bundle();
            Bitmap bitmap = null;
            Notification notification = new Notification();
            bundle.putParcelable(EXTRA_LARGE_ICON,bitmap);
            notification.extras = bundle;
            when(mStatusBarNotification.getNotification()).thenReturn(notification);
            SetMocks(fixture);
            fixture.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = fixture.getNotificationIcon();

            // verify
            assertThat(actual,is(nullValue()));

        }

        @Theory
        public void getStatusBarNotification(DefaultNotificationImpl fixture) throws Exception {
            // setup
            StatusBarNotification expected = mock(StatusBarNotification.class);
            when(expected.getPackageName()).thenReturn("TEST");
            SetMocks(fixture);
            fixture.setStatusBarNotification(expected);

            // exercise
            StatusBarNotification actual = fixture.getStatusBarNotification();

            // verify
            assertThat(actual,is(expected));

        }

        @Theory
        public void getNotification(DefaultNotificationImpl fixture) throws Exception {
            // setup
            StatusBarNotification statusBarNotification = mock(StatusBarNotification.class);
            Notification expected = mock(Notification.class);
            when(mStatusBarNotification.getNotification()).thenReturn(expected);
            SetMocks(fixture);
            fixture.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Notification actual = fixture.getNotification();

            // verify
            assertThat(actual,is(expected));

        }

    }

    public static class LineNotificationTest{
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        @InjectMocks LineNotification mLineNotification;
        @Mock ApplicationInfoRepository mAppInfoRepository;
        @Mock PackageManager mPackageManager;
        @Mock Resources mResources;
        @Mock StatusBarNotification mStatusBarNotification;
        @Mock Notification mNotification;
        @Mock ApplicationInfo mApplicationInfo;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            when(mStatusBarNotification.getPackageName()).thenReturn("TEST");
            when(mStatusBarNotification.getNotification()).thenReturn(mNotification);
        }

        @Test
        public void isReadTarget_LineEqualsTitleFalse_MatchesFalse_ContainsKeyFalse() {
            // setup
            Bundle bundleForEqualsTitle = new Bundle(); // false
            bundleForEqualsTitle.putString(EXTRA_TITLE,"line");
            mNotification.extras = bundleForEqualsTitle;
            CharSequence charSequenceForMatches = "jp.naver.line.android"; // false
            mNotification.tickerText = charSequenceForMatches;
            Bundle bundleForContainsKey = new Bundle(); // false
            bundleForContainsKey.putString("line","");
            mNotification.extras = bundleForContainsKey;
            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(mLineNotification.isReadTarget(),is(true));
        }

        @Test
        public void isReadTarget_LineEqualsTrue_MatchesFalse_ContainsKeyFalse() {
            // setup
            Bundle bundleForEqualsTitle = new Bundle(); // true
            bundleForEqualsTitle.putString(EXTRA_TITLE,"LINE");
            mNotification.extras = bundleForEqualsTitle;
            CharSequence charSequenceForMatches = "jp.naver.line.android";  // false
            mNotification.tickerText = charSequenceForMatches;
            Bundle bundleForContainsKey = new Bundle(); // false
            bundleForContainsKey.putString("line","");
            mNotification.extras = bundleForContainsKey;
            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(mLineNotification.isReadTarget(),is(true));
        }

        @Test
        public void isReadTarget_LineEqualsFalse_MatchesTrue_ContainsKeyFalse() {
            // setup
            Bundle bundleForEqualsTitle = new Bundle(); // false
            bundleForEqualsTitle.putString(EXTRA_TITLE,"line");
            mNotification.extras = bundleForEqualsTitle;
            CharSequence charSequenceForMatches = ".test:.test";  // true
            mNotification.tickerText = charSequenceForMatches;
            Bundle bundleForContainsKey = new Bundle(); // false
            bundleForContainsKey.putString("line","");
            mNotification.extras = bundleForContainsKey;
            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(mLineNotification.isReadTarget(),is(true));
        }

        @Test
        public void isReadTarget_LineEqualsFalse_MatchesFalse_ContainsKeyTrue() {
            // setup
            Bundle bundleForEqualsTitle = new Bundle(); // false
            bundleForEqualsTitle.putString(EXTRA_TITLE,"line");
            mNotification.extras = bundleForEqualsTitle;
            CharSequence charSequenceForMatches = "jp.naver.line.android";  // false
            mNotification.tickerText = charSequenceForMatches;
            Bundle bundleForContainsKey = new Bundle(); // true
            bundleForContainsKey.putString("line.chat.id","");
            mNotification.extras = bundleForContainsKey;

            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(mLineNotification.isReadTarget(),is(false));
        }

        @Test
        public void isReadTarget_LineEqualsTrue_MatchesFalse_ContainsKeyTrue() {
            // setup
            Bundle bundleForEqualsTitle = new Bundle(); // true
            bundleForEqualsTitle.putString(EXTRA_TITLE,"LINE");
            mNotification.extras = bundleForEqualsTitle;
            CharSequence charSequenceForMatches = "jp.naver.line.android"; // false
            mNotification.tickerText = charSequenceForMatches;
            Bundle bundleForContainsKey = new Bundle();
            bundleForContainsKey.putString("line.chat.id","");  // true
            mNotification.extras = bundleForContainsKey;

            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(mLineNotification.isReadTarget(),is(false));
        }

        @Test
        public void isReadTarget_LineEqualsFalse_MatchesTrue_ContainsKeyTrue() {
            // setup
            Bundle bundleForEqualsTitle = new Bundle(); // false
            bundleForEqualsTitle.putString(EXTRA_TITLE,"line");
            mNotification.extras = bundleForEqualsTitle;
            CharSequence charSequenceForMatches = ".test:.test";    // true
            mNotification.tickerText = charSequenceForMatches;
            Bundle bundleForContainsKey = new Bundle();
            bundleForContainsKey.putString("line.chat.id","");  // true
            mNotification.extras = bundleForContainsKey;
            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(mLineNotification.isReadTarget(),is(false));
        }

        @Test
        public void isReadTarget_LineEqualsTrue_MatchesTrue() {
            // setup
            Bundle bundleForEqualsTitle = new Bundle(); // true
            bundleForEqualsTitle.putString(EXTRA_TITLE,"LINE");
            mNotification.extras = bundleForEqualsTitle;
            CharSequence charSequenceForMatches = ".test:.test";    // true
            mNotification.tickerText = charSequenceForMatches;
            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(mLineNotification.isReadTarget(),is(false));
        }

        @Test
        public void getTitle() {
            // setup
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TITLE,"TEST");
            mNotification.extras = bundle;
            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mLineNotification.getTitle();

            // verify
            assertThat(actual,is("TEST"));

        }

        @Test
        public void getText() {
            // setup
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TEXT,"TEST");
            mNotification.extras = bundle;
            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mLineNotification.getText();

            // verify
            assertThat(actual,is("TEST"));

        }

        @Test
        public void setStatusBarNotification() throws Exception {
            // exercise
            AbstractNotification actual = mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(actual,is(mLineNotification));

        }

        @Test(expected = NullPointerException.class)
        public void setStatusBarNotificationArgNull() throws Exception {
            // setup
            StatusBarNotification statusBarNotification = null;

            // exercise
            AbstractNotification actual = mLineNotification.setStatusBarNotification(statusBarNotification);

        }

        @Test(expected = IllegalArgumentException.class)
        public void setStatusBarNotificationAppInfoNull() throws Exception {
            // setup
            when(mAppInfoRepository.get("TEST")).thenReturn(null);

            // exercise
            AbstractNotification actual =  mLineNotification.setStatusBarNotification(mStatusBarNotification);

        }

        @Test
        public void getApplicationIcon() throws Exception {
            // setup
            Drawable expected = mock(Drawable.class);
            when(mApplicationInfo.loadIcon(any(PackageManager.class))).thenReturn(expected);
            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mLineNotification.getApplicationIcon();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getApplicationName() throws Exception {
            // setup
            String expected = "TEST";
            when(mApplicationInfo.loadLabel(mPackageManager)).thenReturn(expected);
            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mLineNotification.getApplicationName();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getNotificationIconReturnNotNull() throws Exception {
            // setup
            Bundle bundle = new Bundle();
            Bitmap bitmap = Bitmap.createBitmap(10,10,Bitmap.Config.ALPHA_8);
            Resources resources = Resources.getSystem();
            Notification notification = new Notification();

            bundle.putParcelable(EXTRA_LARGE_ICON,bitmap);
            notification.extras = bundle;

            when(mStatusBarNotification.getNotification()).thenReturn(notification);

            mLineNotification.mResources = resources;
            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mLineNotification.getNotificationIcon();

            // verify
            assertThat(actual,instanceOf(Drawable.class));

        }

        @Test
        public void getNotificationIconReturnNull() throws Exception {
            // setup
            Bundle bundle = new Bundle();
            Bitmap bitmap = null;
            Notification notification = new Notification();
            bundle.putParcelable(EXTRA_LARGE_ICON,bitmap);
            notification.extras = bundle;

            when(mStatusBarNotification.getNotification()).thenReturn(notification);

            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mLineNotification.getNotificationIcon();

            // verify
            assertThat(actual,is(nullValue()));

        }

        @Test
        public void getStatusBarNotification() throws Exception {
            // setup
            StatusBarNotification expected = mock(StatusBarNotification.class);
            when(expected.getPackageName()).thenReturn("TEST");
            mLineNotification.setStatusBarNotification(expected);

            // exercise
            StatusBarNotification actual = mLineNotification.getStatusBarNotification();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getNotification() throws Exception {
            // setup
            StatusBarNotification statusBarNotification = mock(StatusBarNotification.class);
            Notification expected = mock(Notification.class);
            when(mStatusBarNotification.getNotification()).thenReturn(expected);
            mLineNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Notification actual = mLineNotification.getNotification();

            // verify
            assertThat(actual,is(expected));

        }
    }

    public static class HangoutsNotificationTest{
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        @InjectMocks HangoutsNotification mHangoutsNotification;
        @Mock ApplicationInfoRepository mAppInfoRepository;
        @Mock PackageManager mPackageManager;
        @Mock Resources mResources;
        @Mock StatusBarNotification mStatusBarNotification;
        @Mock Notification mNotification;
        @Mock ApplicationInfo mApplicationInfo;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            when(mStatusBarNotification.getPackageName()).thenReturn("TEST");
            when(mStatusBarNotification.getNotification()).thenReturn(mNotification);
        }

        @Test
        public void isReadTarget() {
            // verify
            assertThat(mHangoutsNotification.isReadTarget(),is(true));
        }

        @Test
        public void getTitle() {
            // setup
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TITLE,"TEST");
            mNotification.extras = bundle;
            mHangoutsNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mHangoutsNotification.getTitle();

            // verify
            assertThat(actual,is("TEST"));

        }

        @Test
        public void getText() {
            // setup
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TEXT,"TEST");
            mNotification.extras = bundle;
            mHangoutsNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mHangoutsNotification.getText();

            // verify
            assertThat(actual,is("TEST"));

        }

        @Test
        public void getTextEmpty() {
            // setup
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TEXT,null);
            mNotification.extras = bundle;
            mHangoutsNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mHangoutsNotification.getText();

            // verify
            assertThat(actual,is(""));

        }

        @Test
        public void setStatusBarNotification() throws Exception {
            // exercise
            AbstractNotification actual = mHangoutsNotification.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(actual,is(mHangoutsNotification));

        }

        @Test(expected = NullPointerException.class)
        public void setStatusBarNotificationArgNull() throws Exception {
            // setup
            StatusBarNotification statusBarNotification = null;

            // exercise
            AbstractNotification actual = mHangoutsNotification.setStatusBarNotification(statusBarNotification);

        }

        @Test(expected = IllegalArgumentException.class)
        public void setStatusBarNotificationAppInfoNull() throws Exception {
            // setup
            when(mAppInfoRepository.get("TEST")).thenReturn(null);

            // exercise
            AbstractNotification actual =  mHangoutsNotification.setStatusBarNotification(mStatusBarNotification);

        }

        @Test
        public void getApplicationIcon() throws Exception {
            // setup
            Drawable expected = mock(Drawable.class);
            when(mApplicationInfo.loadIcon(any(PackageManager.class))).thenReturn(expected);
            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            mHangoutsNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mHangoutsNotification.getApplicationIcon();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getApplicationName() throws Exception {
            // setup
            String expected = "TEST";
            when(mApplicationInfo.loadLabel(mPackageManager)).thenReturn(expected);
            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            mHangoutsNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mHangoutsNotification.getApplicationName();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getNotificationIconReturnNotNull() throws Exception {
            // setup
            Bundle bundle = new Bundle();
            Bitmap bitmap = Bitmap.createBitmap(10,10,Bitmap.Config.ALPHA_8);
            Resources resources = Resources.getSystem();
            Notification notification = new Notification();
            bundle.putParcelable(EXTRA_LARGE_ICON,bitmap);
            notification.extras = bundle;
            when(mStatusBarNotification.getNotification()).thenReturn(notification);
            mHangoutsNotification.mResources = resources;
            mHangoutsNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mHangoutsNotification.getNotificationIcon();

            // verify
            assertThat(actual,instanceOf(Drawable.class));

        }

        @Test
        public void getNotificationIconReturnNull() throws Exception {
            // setup
            Bundle bundle = new Bundle();
            Bitmap bitmap = null;
            Notification notification = new Notification();
            bundle.putParcelable(EXTRA_LARGE_ICON,bitmap);
            notification.extras = bundle;
            when(mStatusBarNotification.getNotification()).thenReturn(notification);
            mHangoutsNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mHangoutsNotification.getNotificationIcon();

            // verify
            assertThat(actual,is(nullValue()));

        }

        @Test
        public void getStatusBarNotification() throws Exception {
            // setup
            StatusBarNotification expected = mock(StatusBarNotification.class);
            when(expected.getPackageName()).thenReturn("TEST");
            mHangoutsNotification.setStatusBarNotification(expected);

            // exercise
            StatusBarNotification actual = mHangoutsNotification.getStatusBarNotification();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getNotification() throws Exception {
            // setup
            StatusBarNotification statusBarNotification = mock(StatusBarNotification.class);
            Notification expected = mock(Notification.class);
            when(mStatusBarNotification.getNotification()).thenReturn(expected);
            mHangoutsNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Notification actual = mHangoutsNotification.getNotification();

            // verify
            assertThat(actual,is(expected));

        }
    }

    public static class ViberNotificationTest{
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        @InjectMocks ViberNotification mViberNotification;
        @Mock ApplicationInfoRepository mAppInfoRepository;
        @Mock PackageManager mPackageManager;
        @Mock Resources mResources;
        @Mock StatusBarNotification mStatusBarNotification;
        @Mock Notification mNotification;
        @Mock ApplicationInfo mApplicationInfo;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            when(mStatusBarNotification.getPackageName()).thenReturn("TEST");
            when(mStatusBarNotification.getNotification()).thenReturn(mNotification);
        }

        @Test
        public void isReadTargetTrue() {
            // setup
            when(mStatusBarNotification.getTag()).thenReturn("");

            // verify
            assertThat(mViberNotification.isReadTarget(),is(true));
        }

        @Test
        public void isReadTargetFalse() {
            // setup
            when(mStatusBarNotification.getTag()).thenReturn("missed_call");

            // verify
            assertThat(mViberNotification.isReadTarget(),is(false));
        }

        @Test
        public void getTitle() {
            // setup
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TITLE,"TEST");
            mNotification.extras = bundle;

            mViberNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mViberNotification.getTitle();

            // verify
            assertThat(actual,is("TEST"));

        }

        @Test
        public void getText() {
            // setup
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TEXT,"TEST");
            mNotification.extras = bundle;
            mViberNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mViberNotification.getText();

            // verify
            assertThat(actual,is("TEST"));

        }

        @Test
        public void setStatusBarNotification() throws Exception {
            // exercise
            AbstractNotification actual = mViberNotification.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(actual,is(mViberNotification));

        }

        @Test(expected = NullPointerException.class)
        public void setStatusBarNotificationArgNull() throws Exception {
            // setup
            StatusBarNotification statusBarNotification = null;

            // exercise
            AbstractNotification actual = mViberNotification.setStatusBarNotification(statusBarNotification);

        }

        @Test(expected = IllegalArgumentException.class)
        public void setStatusBarNotificationAppInfoNull() throws Exception {
            // setup
            when(mAppInfoRepository.get("TEST")).thenReturn(null);

            // exercise
            AbstractNotification actual =  mViberNotification.setStatusBarNotification(mStatusBarNotification);

        }

        @Test
        public void getApplicationIcon() throws Exception {
            // setup
            Drawable expected = mock(Drawable.class);
            when(mApplicationInfo.loadIcon(any(PackageManager.class))).thenReturn(expected);
            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            mViberNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mViberNotification.getApplicationIcon();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getApplicationName() throws Exception {
            // setup
            String expected = "TEST";
            when(mApplicationInfo.loadLabel(mPackageManager)).thenReturn(expected);
            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            mViberNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mViberNotification.getApplicationName();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getNotificationIconReturnNotNull() throws Exception {
            // setup
            Bundle bundle = new Bundle();
            Bitmap bitmap = Bitmap.createBitmap(10,10,Bitmap.Config.ALPHA_8);
            Resources resources = Resources.getSystem();
            Notification notification = new Notification();
            bundle.putParcelable(EXTRA_LARGE_ICON,bitmap);
            notification.extras = bundle;
            when(mStatusBarNotification.getNotification()).thenReturn(notification);
            mViberNotification.mResources = resources;
            mViberNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mViberNotification.getNotificationIcon();

            // verify
            assertThat(actual,instanceOf(Drawable.class));

        }

        @Test
        public void getNotificationIconReturnNull() throws Exception {
            // setup
            Bundle bundle = new Bundle();
            Bitmap bitmap = null;
            Notification notification = new Notification();
            bundle.putParcelable(EXTRA_LARGE_ICON,bitmap);
            notification.extras = bundle;
            when(mStatusBarNotification.getNotification()).thenReturn(notification);
            mViberNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mViberNotification.getNotificationIcon();

            // verify
            assertThat(actual,is(nullValue()));

        }

        @Test
        public void getStatusBarNotification() throws Exception {
            // setup
            StatusBarNotification expected = mock(StatusBarNotification.class);
            when(expected.getPackageName()).thenReturn("TEST");
            mViberNotification.setStatusBarNotification(expected);

            // exercise
            StatusBarNotification actual = mViberNotification.getStatusBarNotification();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getNotification() throws Exception {
            // setup
            StatusBarNotification statusBarNotification = mock(StatusBarNotification.class);
            Notification expected = mock(Notification.class);
            when(mStatusBarNotification.getNotification()).thenReturn(expected);
            mViberNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Notification actual = mViberNotification.getNotification();

            // verify
            assertThat(actual,is(expected));

        }
    }

    public static class WeChatNotificationTest{
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        @InjectMocks WeChatNotification mWeChatNotification;
        @Mock ApplicationInfoRepository mAppInfoRepository;
        @Mock PackageManager mPackageManager;
        @Mock Resources mResources;
        @Mock StatusBarNotification mStatusBarNotification;
        @Mock Notification mNotification;
        @Mock ApplicationInfo mApplicationInfo;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            when(mStatusBarNotification.getPackageName()).thenReturn("TEST");
            when(mStatusBarNotification.getNotification()).thenReturn(mNotification);
        }

        @Test
        public void isReadTargetTrue() {
            // setup
            CharSequence charSequenceForMatches = "";
            mNotification.tickerText = charSequenceForMatches;

            // verify
            assertThat(mWeChatNotification.isReadTarget(),is(true));
        }

        @Test
        public void isReadTargetFalse() {
            // setup
            CharSequence charSequenceForMatches = "TEST: [TEST]";
            mNotification.tickerText = charSequenceForMatches;

            // verify
            assertThat(mWeChatNotification.isReadTarget(),is(false));
        }

        @Test
        public void getTitle() {
            // setup
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TITLE,"TEST");
            mNotification.extras = bundle;
            mWeChatNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mWeChatNotification.getTitle();

            // verify
            assertThat(actual,is("TEST"));

        }

        @Test
        public void getText() {
            // setup
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TEXT,"TEST");
            mNotification.extras = bundle;
            mWeChatNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mWeChatNotification.getText();

            // verify
            assertThat(actual,is("TEST"));

        }

        @Test
        public void setStatusBarNotification() throws Exception {
            // exercise
            AbstractNotification actual = mWeChatNotification.setStatusBarNotification(mStatusBarNotification);

            // verify
            assertThat(actual,is(mWeChatNotification));

        }

        @Test(expected = NullPointerException.class)
        public void setStatusBarNotificationArgNull() throws Exception {
            // setup
            StatusBarNotification statusBarNotification = null;

            // exercise
            AbstractNotification actual = mWeChatNotification.setStatusBarNotification(statusBarNotification);

        }

        @Test(expected = IllegalArgumentException.class)
        public void setStatusBarNotificationAppInfoNull() throws Exception {
            // setup
            when(mAppInfoRepository.get("TEST")).thenReturn(null);

            // exercise
            AbstractNotification actual =  mWeChatNotification.setStatusBarNotification(mStatusBarNotification);

        }

        @Test
        public void getApplicationIcon() throws Exception {
            // setup
            Drawable expected = mock(Drawable.class);
            when(mApplicationInfo.loadIcon(any(PackageManager.class))).thenReturn(expected);
            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            mWeChatNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mWeChatNotification.getApplicationIcon();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getApplicationName() throws Exception {
            // setup
            String expected = "TEST";
            when(mApplicationInfo.loadLabel(mPackageManager)).thenReturn(expected);
            when(mAppInfoRepository.get("TEST")).thenReturn(mApplicationInfo);
            mWeChatNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            String actual = mWeChatNotification.getApplicationName();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getNotificationIconReturnNotNull() throws Exception {
            // setup
            Bundle bundle = new Bundle();
            Bitmap bitmap = Bitmap.createBitmap(10,10,Bitmap.Config.ALPHA_8);
            Resources resources = Resources.getSystem();
            Notification notification = new Notification();
            bundle.putParcelable(EXTRA_LARGE_ICON,bitmap);
            notification.extras = bundle;
            when(mStatusBarNotification.getNotification()).thenReturn(notification);
            mWeChatNotification.mResources = resources;
            mWeChatNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mWeChatNotification.getNotificationIcon();

            // verify
            assertThat(actual,instanceOf(Drawable.class));

        }

        @Test
        public void getNotificationIconReturnNull() throws Exception {
            // setup
            Bundle bundle = new Bundle();
            Bitmap bitmap = null;
            Notification notification = new Notification();
            bundle.putParcelable(EXTRA_LARGE_ICON,bitmap);
            notification.extras = bundle;
            when(mStatusBarNotification.getNotification()).thenReturn(notification);
            mWeChatNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Drawable actual = mWeChatNotification.getNotificationIcon();

            // verify
            assertThat(actual,is(nullValue()));

        }

        @Test
        public void getStatusBarNotification() throws Exception {
            // setup
            StatusBarNotification expected = mock(StatusBarNotification.class);
            when(expected.getPackageName()).thenReturn("TEST");
            mWeChatNotification.setStatusBarNotification(expected);

            // exercise
            StatusBarNotification actual = mWeChatNotification.getStatusBarNotification();

            // verify
            assertThat(actual,is(expected));

        }

        @Test
        public void getNotification() throws Exception {
            // setup
            StatusBarNotification statusBarNotification = mock(StatusBarNotification.class);
            Notification expected = mock(Notification.class);
            when(mStatusBarNotification.getNotification()).thenReturn(expected);
            mWeChatNotification.setStatusBarNotification(mStatusBarNotification);

            // exercise
            Notification actual = mWeChatNotification.getNotification();

            // verify
            assertThat(actual,is(expected));

        }
    }

}
