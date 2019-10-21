package jp.pioneer.carsync.presentation.view.activity;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.di.component.ActivityComponent;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.di.module.ActivityModule;
import jp.pioneer.carsync.application.di.module.AppModule;
import jp.pioneer.carsync.application.di.module.DomainModule;
import jp.pioneer.carsync.application.di.module.InfrastructureBindsModule;
import jp.pioneer.carsync.application.di.module.InfrastructureModule;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.presentation.controller.MainFragmentController;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.presenter.MainPresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static jp.pioneer.carsync.presentation.view.activity.MainActivity.ACTION_VOICE_COMMAND;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MainActivityのテスト
 */
public class MainActivityTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class, true, false);
    @Mock ComponentFactory mComponentFactory;
    @Mock MainPresenter mPresenter;
    @Mock MainFragmentController mFragmentController;
    @Mock AppSharedPreference mPreference;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        TestPresenterComponent presenterComponent(TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = {TestPresenterModule.class})
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public MainPresenter provideTestPresenter() {
            return mPresenter;
        }

        @Provides
        public MainFragmentController provideMainFragmentController() {
            return mFragmentController;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerMainActivityTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        ActivityComponent activityComponent = presenterComponent.activityComponent(new ActivityModule());
        when(mComponentFactory.getPresenterComponent(appComponent, MainActivity.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createActivityComponent(any(TestPresenterComponent.class), any(MainActivity.class))).thenReturn(activityComponent);

        when(mPresenter.getUiColor()).thenReturn(UiColor.RED);
    }

    private TestApp getTestApp() {
        return (TestApp) InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext()
                .getApplicationContext();
    }

    @Test
    public void testLifeCycle() throws Exception {
        mActivityRule.launchActivity(null);
        mActivityRule.getActivity().finish();

        Thread.sleep(200);

        verify(mFragmentController).setContainerViewId(any(Integer.class));
    }

    @Test
    public void testAccidentDetectDialog() throws Exception {
        Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_SHOW_ACCIDENT_DETECT);
        intent.putExtra("test", "TEST");
        mActivityRule.launchActivity(intent);

        verify(mPresenter).onShowAccidentDetectAction(any(Bundle.class));
    }

    @Test
    public void testAccidentDetectDialogFromBackground() throws Exception {
        mActivityRule.launchActivity(null);

        Intent intent = new Intent(getTargetContext(), MainActivity.class);
        intent.setAction(MainActivity.ACTION_SHOW_ACCIDENT_DETECT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("test", "TEST");
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> getTargetContext().startActivity(intent));

        Thread.sleep(500);

        verify(mPresenter).onShowAccidentDetectAction(any(Bundle.class));
    }

    @Test
    public void testShowAccidentDetectDialog() throws Exception {
        mActivityRule.launchActivity(null);

        when(mFragmentController.isShowAccidentDetect()).thenReturn(false);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            if (!activity.isShowAccidentDetect()) {
                activity.showAccidentDetect(Bundle.EMPTY);
            }
        });

        verify(mFragmentController).showAccidentDetect(any(Bundle.class));
    }

    @Test
    public void testAdasWarningDialog() throws Exception {
        Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_SHOW_ADAS_WARNING);
        intent.putExtra("test", "TEST");
        mActivityRule.launchActivity(intent);

        verify(mPresenter).onShowAdasWarningAction(any(Bundle.class));
    }

    @Test
    public void testAdasWarningDialogFromBackground() throws Exception {
        mActivityRule.launchActivity(null);

        Intent intent = new Intent(getTargetContext(), MainActivity.class);
        intent.setAction(MainActivity.ACTION_SHOW_ADAS_WARNING);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("test", "TEST");
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> getTargetContext().startActivity(intent));

        Thread.sleep(500);

        verify(mPresenter).onShowAdasWarningAction(any(Bundle.class));
    }

    @Test
    public void testShowAdasWarningDialog() throws Exception {
        mActivityRule.launchActivity(null);

        when(mFragmentController.isShowAdasWarning()).thenReturn(false);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            if (!activity.isShowAccidentDetect()) {
                activity.showAdasWarning(Bundle.EMPTY);
            }
        });

        verify(mFragmentController).showAdasWarning(any(Bundle.class));
    }

    @Test
    public void testParkingSensorDialog() throws Exception {
        Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_SHOW_PARKING_SENSOR);
        intent.putExtra("test", "TEST");
        mActivityRule.launchActivity(intent);

        verify(mPresenter).onShowParkingSensorAction(any(Bundle.class));
    }

    @Test
    public void testParkingSensorDialogFromBackground() throws Exception {
        mActivityRule.launchActivity(null);

        Intent intent = new Intent(getTargetContext(), MainActivity.class);
        intent.setAction(MainActivity.ACTION_SHOW_PARKING_SENSOR);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("test", "TEST");
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> getTargetContext().startActivity(intent));

        Thread.sleep(500);

        verify(mPresenter).onShowParkingSensorAction(any(Bundle.class));
    }

    @Test
    public void testShowParkingSensorDialog() throws Exception {
        mActivityRule.launchActivity(null);

        when(mFragmentController.isShowParkingSensor()).thenReturn(false);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            if (!activity.isShowParkingSensor()) {
                activity.showParkingSensor(Bundle.EMPTY);
            }
        });

        verify(mFragmentController).showParkingSensor(any(Bundle.class));
    }

    @Test
    public void testRecognizeSpeechDialogFromBackground() throws Exception {
        mActivityRule.launchActivity(null);
        /*
         * 音声認識に関しては、テスト中音声入力ができないため、
         * 実際のシーケンス通りにメソッドを確認する。
         */
        Intent mockIntent = mock(Intent.class);
        ArrayList mockList = mock(ArrayList.class);
        Bundle results = new Bundle();
        //when(mockIntent.getStringArrayListExtra(any(String.class))).thenReturn(mockList);
        when(mockIntent.getAction()).thenReturn(ACTION_VOICE_COMMAND);
        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            activity.onNewIntent(mockIntent);
            //activity.onActivityResult(1, -1, mockIntent);
            //activity.onResume();
            activity.startRecognizer();
            activity.onResults(results);
        });
        verify(mPresenter).prepareRecognizer();
        verify(mPresenter).onRecognizeResults(any());
    }

    @Test
    public void testRecognizeSpeechNoMatch() throws Exception {
        mActivityRule.launchActivity(null);

        Intent mockIntent = mock(Intent.class);

        ArrayList mockList = mock(ArrayList.class);

        when(mockIntent.getAction()).thenReturn(ACTION_VOICE_COMMAND);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            activity.onNewIntent(mockIntent);
            activity.startRecognizer();
            activity.onError(SpeechRecognizer.ERROR_NO_MATCH);
        });
        verify(mPresenter).prepareRecognizer();
        verify(mPresenter).restartRecognizer();
    }

    @Test
    public void testRecognizeSpeechFailure() throws Exception {
        mActivityRule.launchActivity(null);

        Intent mockIntent = mock(Intent.class);

        ArrayList mockList = mock(ArrayList.class);

        when(mockIntent.getAction()).thenReturn(ACTION_VOICE_COMMAND);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            activity.onNewIntent(mockIntent);
            activity.startRecognizer();
            activity.onError(SpeechRecognizer.ERROR_NETWORK);
        });
        verify(mPresenter).prepareRecognizer();
        verify(mPresenter).finishRecognizer();
        verify(mPresenter, never()).onRecognizeResults(any(ArrayList.class));
    }

    @Test
    public void testNavigate() throws Exception {
        mActivityRule.launchActivity(null);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> activity.navigate(ScreenId.HOME_CONTAINER, Bundle.EMPTY));

        verify(mFragmentController).navigate(eq(ScreenId.HOME_CONTAINER), any(Bundle.class));
    }

    @Test
    public void testGoBack() throws Exception {
        mActivityRule.launchActivity(null);

        when(mFragmentController.goBack()).thenReturn(false);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> activity.goBack());

        verify(mFragmentController).goBack();
    }

    @Test
    public void testChangeBackgroundBlurOn() throws Exception {
        mActivityRule.launchActivity(null);


        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> activity.changeBackgroundBlur(true));

        onView(withId(R.id.blurView)).check(matches(isEnabled()));
    }

    @Test
    public void testChangeBackgroundBlurOff() throws Exception {
        mActivityRule.launchActivity(null);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> activity.changeBackgroundBlur(false));

        onView(withId(R.id.blurView)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testChangeBackgroundBlurOn2() throws Exception {
        mActivityRule.launchActivity(null);

        Uri uri = Uri.parse("android.resource://" + getTargetContext().getPackageName() + "/" + R.raw.bgv_001);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            activity.changeBackgroundVideo(uri);
            activity.setCaptureImage(true, uri);
        });

        onView(withId(R.id.blurView)).check(matches(isEnabled()));
    }

    @Test
    public void testChangeBackgroundBlurOff2() throws Exception {
        mActivityRule.launchActivity(null);

        Uri uri = Uri.parse("TEST");

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> activity.setCaptureImage(false, uri));

        onView(withId(R.id.blurView)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testChangeBackgroundTypeVideo() throws Exception {
        mActivityRule.launchActivity(null);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> activity.changeBackgroundType(true));

        onView(withId(R.id.background_video_view)).check(matches(isEnabled()));
        onView(withId(R.id.background_image_view)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testChangeBackgroundTypePicture() throws Exception {
        mActivityRule.launchActivity(null);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> activity.changeBackgroundType(false));

        onView(withId(R.id.background_video_view)).check(matches(not(isDisplayed())));
        onView(withId(R.id.background_image_view)).check(matches(isEnabled()));
    }

    @Test
    public void testChangeBackgroundImage() throws Exception {
        mActivityRule.launchActivity(null);

        when(mFragmentController.isShowCaution()).thenReturn(true);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> activity.changeBackgroundImage(0));

        onView(withId(R.id.blurView)).check(matches(isEnabled()));
    }

    @Test
    public void testChangeBackgroundVideo() throws Exception {
        mActivityRule.launchActivity(null);
        Uri uri = Uri.parse("TEST");

        when(mFragmentController.isShowCaution()).thenReturn(true);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> activity.changeBackgroundVideo(uri));

        onView(withId(R.id.blurView)).check(matches(isEnabled()));
    }

    @Test
    public void testShowCaution() throws Exception {
        mActivityRule.launchActivity(null);

        when(mFragmentController.isShowCaution()).thenReturn(false);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            if (!activity.isShowCaution()) {
                activity.showCaution(Bundle.EMPTY);
            }
        });

        verify(mFragmentController).showCaution(any(Bundle.class));
    }

    @Test
    public void testShowSessionStopped() throws Exception {
        mActivityRule.launchActivity(null);

        when(mFragmentController.isShowSessionStopped()).thenReturn(false);

        MainActivity activity = mActivityRule.getActivity();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            if (!activity.isShowSessionStopped()) {
                activity.showSessionStopped(Bundle.EMPTY);
            }
        });

        verify(mFragmentController).showSessionStopped(any(Bundle.class));
    }
}