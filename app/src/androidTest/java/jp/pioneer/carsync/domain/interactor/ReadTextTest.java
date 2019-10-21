package jp.pioneer.carsync.domain.interactor;

import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.component.TextToSpeechController;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by NSW00_008320 on 2017/12/01.
 */
public class ReadTextTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ReadText mReadText;
    @Mock TextToSpeechController mTextToSpeechController;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void initialize() throws Exception {
        // setup
        TextToSpeechController.Callback callback = mock(TextToSpeechController.Callback.class);

        // exercise
        mReadText.initialize(callback);

        // verify
        verify(mTextToSpeechController).initialize(callback);
    }

    @Test(expected = NullPointerException.class)
    public void initializeArgNull() throws Exception {
        // setup
        TextToSpeechController.Callback callback = null;

        // exercise
        mReadText.initialize(callback);

    }

    @Test
    public void startReading() throws Exception {
        // setup
        Context context = getTargetContext();

        // exercise
        mReadText.startReading("TEST");

        // verify
        verify(mTextToSpeechController).speak("TEST");

    }

    @Test(expected = NullPointerException.class)
    public void startReadingArgNull() throws Exception {
        // exercise
        mReadText.startReading(null);

    }

    @Test
    public void stopReading() throws Exception {
        // exercise
        mReadText.stopReading();

        // verify
        verify(mTextToSpeechController).stop();

    }

    @Test
    public void terminate() throws Exception {
        // exercise
        mReadText.terminate();

        // verify
        verify(mTextToSpeechController).terminate();

    }


}