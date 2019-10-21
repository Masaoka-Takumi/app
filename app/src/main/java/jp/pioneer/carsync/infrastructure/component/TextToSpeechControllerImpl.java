package jp.pioneer.carsync.infrastructure.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.annimon.stream.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Locale;

import javax.inject.Inject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.util.AppUtil;
import jp.pioneer.carsync.domain.component.TextToSpeechController;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * TextToSpeechControllerの実装.
 */
@SuppressFBWarnings(
        value = "IS2_INCONSISTENT_SYNC",
        justification = "DIされるフィールドは同期化出来ない")
public class TextToSpeechControllerImpl extends UtteranceProgressListener
        implements TextToSpeechController, TextToSpeech.OnInitListener, AudioManager.OnAudioFocusChangeListener {
    @Inject Context mContext;
    @Inject Handler mHandler;
    @Inject AudioManager mAudioManager;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    private static final String UTTERANCE_ID = "message_id_%d";
    private static final HashMap<String, String> sParams = Maps.newHashMap(
            ImmutableMap.<String, String>builder()
                    .put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID)
                    .build());
    private Callback mCallback;
    private TextToSpeech mTts;
    private String mEngine;
    private Locale mLocale;
    private String mPendingText;
    private boolean mHasAudioFocus;
    private boolean mInitialized;
    private boolean mEngineChanging;
    private String mSpeakingId;
    private long mTransactionId;
    private AudioAttributes mPlaybackAttributes;
    private AudioFocusRequest mFocusRequest;
    /**
     * AudioFocus.
     *
     * @see #stopIfNecessary
     */
    enum AudioFocus {
        /** 維持. */
        KEEP,
        /** 放棄. */
        ABANDON
    }

    /**
     * SpeakDone呼び出し.
     *
     * @see #stopIfNecessary
     */
    enum SpeakDone {
        /** 呼び出す. */
        CALL,
        /** 呼び出さない. */
        NOT_CALL
    }

    /**
     * コンストラクタ.
     */
    @Inject
    public TextToSpeechControllerImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void initialize(@NonNull Callback callback) {
        checkNotNull(callback);
        if(mInitialized){
            Timber.i("already initialized");
            return;
        }

        Timber.i("initialize()");

        mCallback = callback;
        mTts = createTextToSpeech(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void speak(@NonNull String text) {
        checkNotNull(text);
        checkState(mInitialized);
        Timber.i("speak()");

        if (mTts == null) {
            // エンジン変更時の初期化で失敗したので再度初期化を試みる
            Timber.d("speak() pending.(engine change)");
            mEngineChanging = true;
            mPendingText = text;
            mTts = createTextToSpeech(this);
            return;
        }

        stopIfNecessary(AudioFocus.KEEP, SpeakDone.NOT_CALL);
        if (!checkEngine(text) || !checkLanguage()) {
            return;
        }

        if (!requestAudioFocus()) {
            callbackSpeakError(Error.FAILURE);
            return;
        }

        int result;
        mSpeakingId = String.format(UTTERANCE_ID, ++mTransactionId);
        result = mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, mSpeakingId);

        if (result != TextToSpeech.SUCCESS) {
            Timber.e("speak() TextToSpeech#speak() failed.");
            abandonAudioFocus();
            callbackSpeakError(Error.FAILURE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stop() {
        checkState(mInitialized);

        Timber.i("stop()");
        stopIfNecessary(AudioFocus.ABANDON, SpeakDone.NOT_CALL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void terminate() {
        Timber.i("terminate()");

        mPendingText = null;
        mCallback = null;
        mInitialized = false;
        mEngineChanging = false;
        stopIfNecessary(AudioFocus.ABANDON, SpeakDone.NOT_CALL);
        if (mTts != null) {
            mTts.shutdown();
            mTts = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onStart(String utteranceId) {
        Timber.i("onStart()");
        callbackSpeakStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onDone(String utteranceId) {
        Timber.i("onDone()");
        if(utteranceId.equals(mSpeakingId)) {
            abandonAudioFocus();
            callbackSpeakDone();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public synchronized void onError(String utteranceId) {
        Timber.e("onError()");
        abandonAudioFocus();
        callbackSpeakError(Error.FAILURE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onError(String utteranceId, int errorCode) {
        Timber.e("onError() errorCode = " + errorCode);
        abandonAudioFocus();
        callbackSpeakError(Error.FAILURE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onInit(int status) {
        mEngineChanging = false;
        if (status == TextToSpeech.SUCCESS) {
            mEngine = mTts.getDefaultEngine();
            if (!checkLanguage()) {
                return;
            }

            if (!mInitialized) {
                mInitialized = true;
                callbackInitializeSuccess();
            }

            mTts.setOnUtteranceProgressListener(this);
            if (mPendingText != null) {
                String text = mPendingText;
                mPendingText = null;
                speak(text);
            }
        } else {
            Timber.e("onInit() failed. status = " + status);
            mTts = null;
            if (!mInitialized) {
                callbackInitializeError(Error.FAILURE);
            } else if (mPendingText != null) {
                mPendingText = null;
                callbackSpeakError(Error.FAILURE);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * 一時停止の手段はないので、AudioFocusが失われたら放棄する。
     * Speak終了の契機が無くなるので、読み上げ中でも{@link Callback#onSpeakDone()} を呼び出す。
     */
    @Override
    public synchronized void onAudioFocusChange(int focusChange) {
        Timber.i("onAudioFocusChange() focusChange = %d",focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                Timber.i("onAudioFocusChange() focusChange = AUDIOFOCUS_LOSS");
                stopIfNecessary(AudioFocus.ABANDON, SpeakDone.CALL);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Timber.i("onAudioFocusChange() focusChange = AUDIOFOCUS_LOSS_TRANSIENT");
                stopIfNecessary(AudioFocus.ABANDON, SpeakDone.CALL);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Timber.i("onAudioFocusChange() focusChange = AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                stopIfNecessary(AudioFocus.ABANDON, SpeakDone.CALL);
                break;
            default:
                // GAIN系は何もしない
        }
    }

    private boolean checkLanguage() {
        Locale locale = AppUtil.getCurrentLocale(mContext);
        if (locale.equals(mLocale)) {
            return true;
        }

        Error error;
        int code = mTts.isLanguageAvailable(locale);
        switch (code) {
            case TextToSpeech.LANG_AVAILABLE:
            case TextToSpeech.LANG_COUNTRY_AVAILABLE:
            case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
                setCurrentLanguageIsTtsSupported(true);
                mLocale = locale;
                mTts.setLanguage(locale);
                return true;
            case TextToSpeech.LANG_MISSING_DATA:
                Timber.e("checkLanguage() current language TextToSpeech.LANG_MISSING_DATA.");
                error = Error.LANG_MISSING_DATA;
                break;
            case TextToSpeech.LANG_NOT_SUPPORTED:
            {// デフォルトLocaleでチェック
                locale = AppUtil.getDefaultLocale();
                if (locale.equals(mLocale)) {
                    setCurrentLanguageIsTtsSupported(false);
                    return true;
                }
                code = mTts.isLanguageAvailable(locale);
                switch (code) {
                    case TextToSpeech.LANG_AVAILABLE:
                    case TextToSpeech.LANG_COUNTRY_AVAILABLE:
                    case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
                        setCurrentLanguageIsTtsSupported(false);
                        mLocale = locale;
                        mTts.setLanguage(locale);
                        return true;
                    case TextToSpeech.LANG_MISSING_DATA:
                        Timber.e("checkLanguage() default language TextToSpeech.LANG_MISSING_DATA.");
                        error = Error.LANG_MISSING_DATA;
                        break;
                    case TextToSpeech.LANG_NOT_SUPPORTED:
                        Timber.e("checkLanguage() default language TextToSpeech.LANG_NOT_SUPPORTED.");
                        error = Error.LANG_NOT_SUPPORTED;
                        break;
                    default:
                        Timber.e("checkLanguage() default language can't happen. code = " + code);
                        error = Error.FAILURE;
                }
                break;
            }
            default:
                Timber.e("checkLanguage() current language can't happen. code = " + code);
                error = Error.FAILURE;
        }

        mLocale = null;
        mPendingText = null;

        if (mInitialized) {
            callbackSpeakError(error);
        } else {
            callbackInitializeError(error);
        }

        return false;
    }

    private void setCurrentLanguageIsTtsSupported(boolean isSupported){
        mStatusHolder.getAppStatus().isTtsSupportedCurrentLocale = isSupported;
    }

    private boolean checkEngine(@NonNull String text) {
        if (mEngineChanging) {
            Timber.d("checkEngine() pending.");
            mPendingText = text;
            return false;
        }

        String engine = mTts.getDefaultEngine();
        if (engine == null) {
            Timber.e("checkEngine() TextToSpeech#getDefaultEngine() failed.");
            callbackSpeakError(Error.FAILURE);
            return false;
        }

        if (!engine.equals(mEngine)) {
            Timber.d("checkEngine() pending.(engine change)");
            mTts.shutdown();
            mEngineChanging = true;
            mPendingText = text;
            mTts = createTextToSpeech(this);
            return false;
        }

        return true;
    }
    @SuppressLint("WrongConstant")
    @SuppressWarnings("deprecation")
    private boolean requestAudioFocus() {
        if (mHasAudioFocus) {
            return true;
        }
        int result;
        if(Build.VERSION.SDK_INT>=26){
            mPlaybackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(mPlaybackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, mHandler)
                    .build();
            result = mAudioManager.requestAudioFocus(mFocusRequest);
        }else{
            result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Timber.e("requestAudioFocus() result = " + result);
            return false;
        }

        mHasAudioFocus = true;
        return true;
    }
    @SuppressWarnings("deprecation")
    private void abandonAudioFocus() {
        if (!mHasAudioFocus) {
            return;
        }

        int result;
        if(Build.VERSION.SDK_INT>=26) {
            result = mAudioManager.abandonAudioFocusRequest(mFocusRequest);
        }else{
            result = mAudioManager.abandonAudioFocus(this);
        }
        // 失敗してもリスナーは登録解除されるので、AudioFocusは持っていないことにする
        mHasAudioFocus = false;
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Timber.e("abandonAudioFocus() result = " + result);
        }
    }

    private void stopIfNecessary(AudioFocus audioFocus, SpeakDone speakDone) {
        if (mTts != null && mTts.isSpeaking()) {
            mTts.stop();
            if (audioFocus == AudioFocus.ABANDON) {
                abandonAudioFocus();
            }
            if (speakDone == SpeakDone.CALL) {
                callbackSpeakDone();
            }
        }
    }

    private void callbackInitializeSuccess() {
        mHandler.post(() -> Optional.ofNullable(mCallback)
                .ifPresent(Callback::onInitializeSuccess));
    }

    private void callbackInitializeError(Error error) {
        mHandler.post(() -> Optional.ofNullable(mCallback)
                .ifPresent(callback -> callback.onInitializeError(error)));
    }

    private void callbackSpeakStart() {
        mHandler.post(() -> Optional.ofNullable(mCallback)
                .ifPresent(Callback::onSpeakStart));
    }

    private void callbackSpeakDone() {
        mHandler.postDelayed(() -> Optional.ofNullable(mCallback)
                .ifPresent(Callback::onSpeakDone), 500);
    }

    private void callbackSpeakError(Error error) {
        mHandler.post(() -> Optional.ofNullable(mCallback)
                .ifPresent(callback -> callback.onSpeakError(error)));
    }

    /**
     * TextToSpeech生成.
     * <p>
     * UnitTest用
     *
     * @param listener リスナー
     * @return TextToSpeech
     */
    @VisibleForTesting
    TextToSpeech createTextToSpeech(TextToSpeech.OnInitListener listener) {
        return new TextToSpeech(mContext, this);
    }
}
