package jp.pioneer.carsync.domain.interactor;

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

import jp.pioneer.carsync.domain.component.TextToSpeechController;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.component.TextToSpeechController.Error.FAILURE;
import static jp.pioneer.carsync.domain.component.TextToSpeechController.Error.LANG_MISSING_DATA;
import static jp.pioneer.carsync.domain.component.TextToSpeechController.Error.LANG_NOT_SUPPORTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by NSW00_008320 on 2017/04/14.
 */
@RunWith(Enclosed.class)
public class CheckAvailableTextToSpeechTest {

    public static class CheckAvailableTextToSpeechTest_OtherThan_onInitializeError {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        @InjectMocks CheckAvailableTextToSpeech mCheckAvailableTextToSpeech;
        @Mock TextToSpeechController mTextToSpeechController;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        }

        @Test
        public void execute() throws Exception {
            // exercise
            mCheckAvailableTextToSpeech.execute(mock(CheckAvailableTextToSpeech.Callback.class));

            // verify
            verify(mTextToSpeechController).initialize(mCheckAvailableTextToSpeech);
            verify(mock(CheckAvailableTextToSpeech.class),never()).onSpeakStart();
            verify(mock(CheckAvailableTextToSpeech.class),never()).onSpeakDone();
            verify(mock(CheckAvailableTextToSpeech.class),never()).onSpeakError(any(TextToSpeechController.Error.class));

        }

        @Test(expected = NullPointerException.class)
        public void executeArgNull() throws Exception {
            // setup
            CheckAvailableTextToSpeech.Callback callback = null;

            //exercise
            mCheckAvailableTextToSpeech.execute(callback);

        }

        @Test
        public void onInitializeSuccess() throws Exception {
            // setup
            CheckAvailableTextToSpeech.Callback callback = mock(CheckAvailableTextToSpeech.Callback.class);
            mCheckAvailableTextToSpeech.execute(callback);

            // exercise
            mCheckAvailableTextToSpeech.onInitializeSuccess();

            // verify
            verify(mTextToSpeechController).terminate();
            verify(callback).onResult(CheckAvailableTextToSpeech.Result.AVAILABLE);

        }

        @Test(expected = NullPointerException.class)
        public void onInitializeErrorArgNull() throws Exception {
            // setup
            TextToSpeechController.Error error = null;

            // exercise
            mCheckAvailableTextToSpeech.onInitializeError(error);

        }

        @Test
        public void onSpeakStart() throws Exception {
            // not test
        }

        @Test
        public void onSpeakDone() throws Exception {
            // not test
        }

        @Test
        public void onSpeakError() throws Exception {
            // not test
        }
    }

    @RunWith(Theories.class)
    public static class CheckAvailableTextToSpeechTest_onInitializeError {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        @InjectMocks CheckAvailableTextToSpeech mCheckAvailableTextToSpeech;
        @Mock TextToSpeechController mTextToSpeechController;

        static class Fixture {
            TextToSpeechController.Error error;
            CheckAvailableTextToSpeech.Result expected;

            Fixture(TextToSpeechController.Error error, CheckAvailableTextToSpeech.Result expected) {
                this.error = error;
                this.expected = expected;
            }
        }

        @DataPoints
        public static final Fixture[] FIXTURES = {
                new Fixture(LANG_MISSING_DATA, CheckAvailableTextToSpeech.Result.LANG_MISSING_DATA),
                new Fixture(LANG_NOT_SUPPORTED, CheckAvailableTextToSpeech.Result.LANG_NOT_SUPPORTED),
                new Fixture(FAILURE, CheckAvailableTextToSpeech.Result.MAY_NOT_DISABLED),
        };

        @Theory
        public void onInitializeError(Fixture fixture) throws Exception {
            // setup
            CheckAvailableTextToSpeech.Callback callback = mock(CheckAvailableTextToSpeech.Callback.class);
            mCheckAvailableTextToSpeech.execute(callback);

            // exercise
            mCheckAvailableTextToSpeech.onInitializeError(fixture.error);

            // verify
            verify(mTextToSpeechController).terminate();
            verify(callback).onResult(fixture.expected);
        }
    }
}