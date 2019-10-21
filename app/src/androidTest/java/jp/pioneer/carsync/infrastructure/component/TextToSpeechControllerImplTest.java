package jp.pioneer.carsync.infrastructure.component;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.TextToSpeechController;

import static android.speech.tts.TextToSpeech.ERROR_SERVICE;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TextToSpeechControllerImplのテスト.
 */
@SuppressWarnings("unchecked")
@RunWith(Theories.class)
public class TextToSpeechControllerImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks TextToSpeechControllerImpl mTextToSpeechControllerImpl = new TextToSpeechControllerImpl() {
        @Override
        TextToSpeech createTextToSpeech(TextToSpeech.OnInitListener listener) {
            mOnInitListener = listener;
            if (mIsCallOnInit) {
                mMainHandler.post(() -> mOnInitListener.onInit(mOnInitStatus));
            }
            return mTextToSpeech;
        }

        @Override
        Locale getCurrentLocale() {
            return mLocale;
        }
    };
    @Mock Context mContext;
    @Mock AudioManager mAudioManager;
    @Mock Handler mHandler;
    @Mock TextToSpeechController.Callback mCallback;
    TextToSpeech mTextToSpeech;
    TextToSpeech.OnInitListener mOnInitListener;
    Handler mMainHandler = new Handler(Looper.getMainLooper());
    Locale mLocale;
    CountDownLatch mSignal;
    int mOnInitStatus;
    UtteranceProgressListener mUtteranceProgressListener;
    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    boolean mIsCallOnInit;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        mLocale = Locale.JAPAN;
        mIsCallOnInit = true;
        mTextToSpeech = mock(TextToSpeech.class);
        // Handler#post(Runnable)がfinal methodなので、止むを得ずsendMessageAtTimeを使用
        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });
    }

    static class LanguageAvailableFixture {
        int result;

        LanguageAvailableFixture(int result) {
            this.result = result;
        }
    }

    static class LanguageUnavailableFixture {
        int result;
        TextToSpeechController.Error error;

        LanguageUnavailableFixture(int result, TextToSpeechController.Error error) {
            this.result = result;
            this.error = error;
        }
    }

    static class AudioFocusChangeLossFixture {
        int focusChange;

        AudioFocusChangeLossFixture(int focusChange) {
            this.focusChange = focusChange;
        }
    }

    static class AudioFocusChangeGainFixture {
        int focusChange;

        AudioFocusChangeGainFixture(int focusChange) {
            this.focusChange = focusChange;
        }
    }

    @DataPoints
    public static final LanguageAvailableFixture[] AVAILABLE_FIXTURES = new LanguageAvailableFixture[] {
            new LanguageAvailableFixture(TextToSpeech.LANG_AVAILABLE),
            new LanguageAvailableFixture(TextToSpeech.LANG_COUNTRY_AVAILABLE),
            new LanguageAvailableFixture(TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE)
    };

    @DataPoints
    public static final LanguageUnavailableFixture[] UNAVAILABLE_FIXTURES = new LanguageUnavailableFixture[] {
            new LanguageUnavailableFixture(TextToSpeech.LANG_MISSING_DATA, TextToSpeechController.Error.LANG_MISSING_DATA),
            new LanguageUnavailableFixture(TextToSpeech.LANG_NOT_SUPPORTED, TextToSpeechController.Error.LANG_NOT_SUPPORTED),
            new LanguageUnavailableFixture(12345678, TextToSpeechController.Error.FAILURE)
    };

    @DataPoints
    public static final AudioFocusChangeLossFixture[] LOSS_FIXTURES = new AudioFocusChangeLossFixture[] {
            new AudioFocusChangeLossFixture(AudioManager.AUDIOFOCUS_LOSS),
            new AudioFocusChangeLossFixture(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT),
            new AudioFocusChangeLossFixture(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
    };

    @DataPoints
    public static final AudioFocusChangeGainFixture[] GAIN_FIXTURES = new AudioFocusChangeGainFixture[] {
            new AudioFocusChangeGainFixture(AudioManager.AUDIOFOCUS_GAIN),
            new AudioFocusChangeGainFixture(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT),
            new AudioFocusChangeGainFixture(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE),
            new AudioFocusChangeGainFixture(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
    };

    @Theory
    public void initialize_HappyPath(LanguageAvailableFixture fixture) throws Exception {
        // setup
        mOnInitStatus = TextToSpeech.SUCCESS;
        mSignal = new CountDownLatch(1);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("engine");
        when(mTextToSpeech.isLanguageAvailable(Locale.JAPAN)).thenReturn(fixture.result);

        // exercise
        mTextToSpeechControllerImpl.initialize(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onInitializeSuccess();
        verify(mTextToSpeech).setLanguage(Locale.JAPAN);
        verify(mTextToSpeech).setOnUtteranceProgressListener(any(UtteranceProgressListener.class));
    }

    @Theory
    public void initialize_LanguageUnavailable(LanguageUnavailableFixture fixture) throws Exception {
        // setup
        mOnInitStatus = TextToSpeech.SUCCESS;
        mSignal = new CountDownLatch(1);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("engine");
        when(mTextToSpeech.isLanguageAvailable(Locale.JAPAN)).thenReturn(fixture.result);

        // exercise
        mTextToSpeechControllerImpl.initialize(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onInitializeError(fixture.error);
        verify(mTextToSpeech, never()).setLanguage(Locale.JAPAN);
        verify(mTextToSpeech, never()).setOnUtteranceProgressListener(any(UtteranceProgressListener.class));
    }

    @Test(expected = NullPointerException.class)
    public void initialize_ArgNull() throws Exception {
        // exercise
        mTextToSpeechControllerImpl.initialize(null);
    }

    @Test(expected = IllegalStateException.class)
    public void initialize_AlreadyInitialized() throws Exception {
        // setup
        transitionToInitialized();

        // exercise
        mTextToSpeechControllerImpl.initialize(mCallback);
    }

    @Test
    public void initialize_onInit_ERROR() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mOnInitStatus = TextToSpeech.ERROR;

        // exercise
        mTextToSpeechControllerImpl.initialize(mCallback);
        mSignal.await();

        // verify
        verify(mCallback).onInitializeError(TextToSpeechController.Error.FAILURE);
    }

    @Test
    public void speak_HappyPath() throws Exception {
        // setup
        transitionToInitialized();
        mSignal = new CountDownLatch(2);
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("engine");
        when(mAudioManager.requestAudioFocus(
                any(AudioManager.OnAudioFocusChangeListener.class),
                eq(AudioManager.STREAM_MUSIC),
                eq(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        when(mAudioManager.abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), any(HashMap.class)))
                    .then(invocationOnMock -> {
                        HashMap<String, String> params = (HashMap<String, String>) invocationOnMock.getArguments()[2];
                        assertThat(params, hasKey(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                        String utteranceId = params.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        mMainHandler.post(() -> mUtteranceProgressListener.onDone(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        } else {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), eq(null), any(String.class)))
                    .then(invocationOnMock -> {
                        String utteranceId = (String) invocationOnMock.getArguments()[3];
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        mMainHandler.post(() -> mUtteranceProgressListener.onDone(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        }

        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        // verify
        verify(mCallback).onSpeakStart();
        verify(mAudioManager).abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class));
        verify(mCallback).onSpeakDone();
    }

    @Theory
    public void speak_LanguageChanged_HappyPath(LanguageAvailableFixture fixture) throws Exception {
        // setup
        transitionToInitialized();
        mSignal = new CountDownLatch(1);
        mLocale = Locale.ITALY;
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("engine");
        when(mTextToSpeech.isLanguageAvailable(Locale.ITALY)).thenReturn(fixture.result);
        when(mAudioManager.requestAudioFocus(
                any(AudioManager.OnAudioFocusChangeListener.class),
                eq(AudioManager.STREAM_MUSIC),
                eq(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), any(HashMap.class)))
                    .then(invocationOnMock -> {
                        HashMap<String, String> params = (HashMap<String, String>) invocationOnMock.getArguments()[2];
                        assertThat(params, hasKey(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                        String utteranceId = params.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        } else {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), eq(null), any(String.class)))
                    .then(invocationOnMock -> {
                        String utteranceId = (String) invocationOnMock.getArguments()[3];
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        }

        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        // verify
        verify(mTextToSpeech).setLanguage(Locale.ITALY);
        verify(mCallback).onSpeakStart();
    }

    @Theory
    public void speak_LanguageChanged_Unavailable(LanguageUnavailableFixture fixture) throws Exception {
        // setup
        transitionToInitialized();
        mSignal = new CountDownLatch(1);
        mLocale = Locale.ITALY;
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("engine");
        when(mTextToSpeech.isLanguageAvailable(Locale.ITALY)).thenReturn(fixture.result);

        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        // verify
        verify(mTextToSpeech, never()).setLanguage(Locale.ITALY);
        verify(mCallback).onSpeakError(fixture.error);
    }

    @Test
    public void speak_EngineChanged_HappyPath() throws Exception {
        // setup
        transitionToInitialized();
        mSignal = new CountDownLatch(1);
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("super engine");
        when(mTextToSpeech.isLanguageAvailable(Locale.JAPAN)).thenReturn(TextToSpeech.LANG_AVAILABLE);
        when(mTextToSpeech.setOnUtteranceProgressListener(any(UtteranceProgressListener.class)))
                .then(invocationOnMock -> {
                    mUtteranceProgressListener = (UtteranceProgressListener) invocationOnMock.getArguments()[0];
                    return TextToSpeech.SUCCESS;
                });
        when(mAudioManager.requestAudioFocus(
                any(AudioManager.OnAudioFocusChangeListener.class),
                eq(AudioManager.STREAM_MUSIC),
                eq(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), any(HashMap.class)))
                    .then(invocationOnMock -> {
                        HashMap<String, String> params = (HashMap<String, String>) invocationOnMock.getArguments()[2];
                        assertThat(params, hasKey(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                        String utteranceId = params.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        } else {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), eq(null), any(String.class)))
                    .then(invocationOnMock -> {
                        String utteranceId = (String) invocationOnMock.getArguments()[3];
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        }

        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        // verify
        verify(mTextToSpeech).shutdown();
        verify(mCallback, never()).onInitializeSuccess();
        verify(mCallback).onSpeakStart();
    }

    @Test
    public void speak_EngineChanged_onInit_ERROR() throws Exception {
        // setup
        transitionToInitialized();
        mSignal = new CountDownLatch(1);
        mOnInitStatus = TextToSpeech.ERROR;
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("super engine");

        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        // verify
        verify(mTextToSpeech).shutdown();
        verify(mCallback, never()).onInitializeError(TextToSpeechController.Error.FAILURE);
        verify(mCallback).onSpeakError(TextToSpeechController.Error.FAILURE);
    }

    @Test
    public void speak_EngineChangeErrored() throws Exception {
        // setup
        transitionToEngineChangeErrored();
        mSignal = new CountDownLatch(1);
        mOnInitStatus = TextToSpeech.SUCCESS;
        when(mTextToSpeech.getDefaultEngine()).thenReturn("super engine");
        when(mTextToSpeech.isLanguageAvailable(Locale.JAPAN)).thenReturn(TextToSpeech.LANG_AVAILABLE);
        when(mTextToSpeech.setOnUtteranceProgressListener(any(UtteranceProgressListener.class)))
                .then(invocationOnMock -> {
                    mUtteranceProgressListener = (UtteranceProgressListener) invocationOnMock.getArguments()[0];
                    return TextToSpeech.SUCCESS;
                });
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mAudioManager.requestAudioFocus(
                any(AudioManager.OnAudioFocusChangeListener.class),
                eq(AudioManager.STREAM_MUSIC),
                eq(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), any(HashMap.class)))
                    .then(invocationOnMock -> {
                        HashMap<String, String> params = (HashMap<String, String>) invocationOnMock.getArguments()[2];
                        String utteranceId = params.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        } else {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), eq(null), any(String.class)))
                    .then(invocationOnMock -> {
                        String utteranceId = (String) invocationOnMock.getArguments()[3];
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        }

        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        // verify
        verify(mCallback, never()).onInitializeSuccess();
        verify(mCallback).onSpeakStart();
    }

    @Test
    public void speak_EngineLost() throws Exception {
        // setup
        transitionToInitialized();
        mSignal = new CountDownLatch(1);
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn(null);

        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        // verify
        verify(mCallback).onSpeakError(TextToSpeechController.Error.FAILURE);
    }

    @Test
    public void speak_EngineChanging() throws Exception {
        // setup
        transitionToEngineChanging();
        mSignal = new CountDownLatch(1);
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("super engine");
        when(mAudioManager.requestAudioFocus(
                any(AudioManager.OnAudioFocusChangeListener.class),
                eq(AudioManager.STREAM_MUSIC),
                eq(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            when(mTextToSpeech.speak(eq("bye bye."), eq(TextToSpeech.QUEUE_FLUSH), any(HashMap.class)))
                    .then(invocationOnMock -> {
                        HashMap<String, String> params = (HashMap<String, String>) invocationOnMock.getArguments()[2];
                        assertThat(params, hasKey(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                        String utteranceId = params.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        } else {
            when(mTextToSpeech.speak(eq("bye bye."), eq(TextToSpeech.QUEUE_FLUSH), eq(null), any(String.class)))
                    .then(invocationOnMock -> {
                        String utteranceId = (String) invocationOnMock.getArguments()[3];
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        }

        // exercise
        mTextToSpeechControllerImpl.speak("bye bye.");
        mMainHandler.post(() -> mOnInitListener.onInit(mOnInitStatus));
        mSignal.await();

        // verify
        verify(mCallback).onSpeakStart();
    }

    @Test
    public void speak_Speaking() throws Exception {
        // setup
        transitionToSpeaking();
        mSignal = new CountDownLatch(2);
        when(mTextToSpeech.isSpeaking()).thenReturn(true);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("engine");
        when(mAudioManager.abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), any(HashMap.class)))
                    .then(invocationOnMock -> {
                        HashMap<String, String> params = (HashMap<String, String>) invocationOnMock.getArguments()[2];
                        assertThat(params, hasKey(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                        String utteranceId = params.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        mMainHandler.post(() -> mUtteranceProgressListener.onDone(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        } else {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), eq(null), any(String.class)))
                    .then(invocationOnMock -> {
                        String utteranceId = (String) invocationOnMock.getArguments()[3];
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        mMainHandler.post(() -> mUtteranceProgressListener.onDone(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        }

        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        // verify
        verify(mTextToSpeech).stop();
        verify(mAudioManager, never()).requestAudioFocus(
                any(AudioManager.OnAudioFocusChangeListener.class),
                eq(AudioManager.STREAM_MUSIC),
                eq(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK));
        verify(mCallback).onSpeakStart();
        verify(mAudioManager).abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class));
        verify(mCallback).onSpeakDone();
    }

    @Test
    public void speak_requestAudioFocus_FAILED() throws Exception {
        // setup
        transitionToInitialized();
        mSignal = new CountDownLatch(1);
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("super engine");
        when(mAudioManager.requestAudioFocus(
                any(AudioManager.OnAudioFocusChangeListener.class),
                eq(AudioManager.STREAM_MUSIC),
                eq(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_FAILED);

        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        // verify
        verify(mCallback).onSpeakError(TextToSpeechController.Error.FAILURE);
    }

    @Test
    public void speak_onError() throws Exception {
        // setup
        transitionToInitialized();
        mSignal = new CountDownLatch(1);
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("engine");
        when(mAudioManager.requestAudioFocus(
                any(AudioManager.OnAudioFocusChangeListener.class),
                eq(AudioManager.STREAM_MUSIC),
                eq(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        when(mAudioManager.abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), any(HashMap.class)))
                    .then(invocationOnMock -> {
                        HashMap<String, String> params = (HashMap<String, String>) invocationOnMock.getArguments()[2];
                        assertThat(params, hasKey(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                        String utteranceId = params.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                        mMainHandler.post(() -> mUtteranceProgressListener.onError(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        } else {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), eq(null), any(String.class)))
                    .then(invocationOnMock -> {
                        String utteranceId = (String) invocationOnMock.getArguments()[3];
                        mMainHandler.post(() -> mUtteranceProgressListener.onError(utteranceId, ERROR_SERVICE));
                        return TextToSpeech.SUCCESS;
                    });
        }

        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        // verify
        verify(mAudioManager).abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class));
        verify(mCallback).onSpeakError(TextToSpeechController.Error.FAILURE);
    }

    @Test
    public void speak_ERROR() throws Exception {
        // setup
        transitionToInitialized();
        mSignal = new CountDownLatch(1);
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("engine");
        when(mAudioManager.requestAudioFocus(
                any(AudioManager.OnAudioFocusChangeListener.class),
                eq(AudioManager.STREAM_MUSIC),
                eq(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        when(mAudioManager.abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), any(HashMap.class)))
                    .thenReturn(TextToSpeech.ERROR);
        } else {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), eq(null), any(String.class)))
                    .thenReturn(TextToSpeech.ERROR);
        }

        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        // verify
        verify(mAudioManager).abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class));
        verify(mCallback).onSpeakError(TextToSpeechController.Error.FAILURE);
    }

    @Test(expected = NullPointerException.class)
    public void speak_ArgNull() throws Exception {
        // setup
        transitionToInitialized();

        // exercise
        mTextToSpeechControllerImpl.speak(null);
    }

    @Test(expected = IllegalStateException.class)
    public void speak_NotInitialized() throws Exception {
        // exercise
        mTextToSpeechControllerImpl.speak("Hello, world.");
    }

    @Test
    public void stop_Speaking() throws Exception {
        // setup
        transitionToSpeaking();
        when(mTextToSpeech.isSpeaking()).thenReturn(true);
        when(mAudioManager.abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

        // exercise
        mTextToSpeechControllerImpl.stop();

        // verify
        verify(mAudioManager).abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class));
        verify(mCallback, never()).onSpeakDone();
    }

    @Test
    public void stop_NotSpeaking() throws Exception {
        // setup
        transitionToInitialized();
        when(mTextToSpeech.isSpeaking()).thenReturn(false);

        // exercise
        mTextToSpeechControllerImpl.stop();

        // verify
        verify(mAudioManager, never()).abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class));
        verify(mCallback, never()).onSpeakDone();
    }

    @Test(expected = IllegalStateException.class)
    public void stop_NotInitialized() throws Exception {
        // exercise
        mTextToSpeechControllerImpl.stop();
    }

    @Test
    public void terminate_Speaking() throws Exception {
        // setup
        transitionToSpeaking();
        when(mTextToSpeech.isSpeaking()).thenReturn(true);
        when(mAudioManager.abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

        // exercise
        mTextToSpeechControllerImpl.terminate();

        // verify
        verify(mAudioManager).abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class));
        verify(mCallback, never()).onSpeakDone();
    }

    @Test
    public void terminate_NotSpeaking() throws Exception {
        // setup
        transitionToInitialized();
        when(mTextToSpeech.isSpeaking()).thenReturn(false);

        // exercise
        mTextToSpeechControllerImpl.terminate();

        // verify
        verify(mAudioManager, never()).abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class));
        verify(mCallback, never()).onSpeakDone();
    }

    @Test
    public void terminate_NotInitialize() throws Exception {
        // exercise
        mTextToSpeechControllerImpl.terminate();

        // verify
        verify(mTextToSpeech, never()).isSpeaking();
        verify(mTextToSpeech, never()).shutdown();
    }

    @Theory
    public void onAudioFocusChange_LOSS(AudioFocusChangeLossFixture fixture) throws Exception {
        // setup
        transitionToSpeaking();
        mSignal = new CountDownLatch(1);
        when(mTextToSpeech.isSpeaking()).thenReturn(true);
        when(mAudioManager.abandonAudioFocus(any(AudioManager.OnAudioFocusChangeListener.class)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

        // exercise
        mOnAudioFocusChangeListener.onAudioFocusChange(fixture.focusChange);
        mSignal.await();

        // verify
        verify(mTextToSpeech).stop();
        verify(mAudioManager).abandonAudioFocus(mOnAudioFocusChangeListener);
        verify(mCallback).onSpeakDone();
    }

    @Theory
    public void onAudioFocusChange_GAIN(AudioFocusChangeGainFixture fixture) throws Exception {
        // setup
        transitionToSpeaking();
        when(mTextToSpeech.isSpeaking()).thenReturn(true);

        // exercise
        mOnAudioFocusChangeListener.onAudioFocusChange(fixture.focusChange);

        // verify
        verify(mTextToSpeech, never()).stop();
        verify(mAudioManager, never()).abandonAudioFocus(mOnAudioFocusChangeListener);
        verify(mCallback, never()).onSpeakDone();
    }

    // for setup
    void transitionToInitialized() throws Exception {
        mSignal = new CountDownLatch(1);
        mOnInitStatus = TextToSpeech.SUCCESS;
        when(mTextToSpeech.getDefaultEngine()).thenReturn("engine");
        when(mTextToSpeech.isLanguageAvailable(Locale.JAPAN)).thenReturn(TextToSpeech.LANG_AVAILABLE);
        when(mTextToSpeech.setOnUtteranceProgressListener(any(UtteranceProgressListener.class)))
                .then(invocationOnMock -> {
                    mUtteranceProgressListener = (UtteranceProgressListener) invocationOnMock.getArguments()[0];
                    return TextToSpeech.SUCCESS;
                });

        mTextToSpeechControllerImpl.initialize(mCallback);
        mSignal.await();

        reset(mTextToSpeech);
        reset(mCallback);
    }

    void transitionToSpeaking() throws Exception {
        transitionToInitialized();
        mSignal = new CountDownLatch(1);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("engine");
        when(mAudioManager.requestAudioFocus(
                any(AudioManager.OnAudioFocusChangeListener.class),
                eq(AudioManager.STREAM_MUSIC),
                eq(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)))
                .then(invocationOnMock -> {
                    mOnAudioFocusChangeListener = (AudioManager.OnAudioFocusChangeListener) invocationOnMock.getArguments()[0];
                    return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
                });
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), any(HashMap.class)))
                    .then(invocationOnMock -> {
                        HashMap<String, String> params = (HashMap<String, String>) invocationOnMock.getArguments()[2];
                        String utteranceId = params.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        } else {
            when(mTextToSpeech.speak(eq("Hello, world."), eq(TextToSpeech.QUEUE_FLUSH), eq(null), any(String.class)))
                    .then(invocationOnMock -> {
                        String utteranceId = (String) invocationOnMock.getArguments()[3];
                        mMainHandler.post(() -> mUtteranceProgressListener.onStart(utteranceId));
                        return TextToSpeech.SUCCESS;
                    });
        }

        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();

        reset(mAudioManager);
        reset(mTextToSpeech);
        reset(mCallback);
    }

    void transitionToEngineChangeErrored() throws Exception {
        transitionToInitialized();
        mSignal = new CountDownLatch(1);
        mOnInitStatus = TextToSpeech.ERROR;
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("super engine");
        mTextToSpeechControllerImpl.speak("Hello, world.");
        mSignal.await();
    }

    void transitionToEngineChanging() throws Exception {
        transitionToInitialized();
        when(mTextToSpeech.isSpeaking()).thenReturn(false);
        when(mTextToSpeech.getDefaultEngine()).thenReturn("super engine");
        mIsCallOnInit = false;
        mTextToSpeechControllerImpl.speak("Hello, world.");

        reset(mTextToSpeech);
    }
}