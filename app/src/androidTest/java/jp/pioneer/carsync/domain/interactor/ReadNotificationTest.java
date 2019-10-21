package jp.pioneer.carsync.domain.interactor;

import android.content.Context;
import android.content.res.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.component.TextToSpeechController;
import jp.pioneer.carsync.domain.model.Notification;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/17.
 */
public class ReadNotificationTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ReadNotification mReadNotification;
    @Mock Context mContext;
    @Mock TextToSpeechController mTextToSpeechController;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        Resources resources = mock(Resources.class);
        when(resources.getString(eq(R.string.reading_text), eq("TEST"))).thenReturn("TEST");
        when(mContext.getResources()).thenReturn(resources);
    }

    @Test
    public void initialize() throws Exception {
        // setup
        TextToSpeechController.Callback callback = mock(TextToSpeechController.Callback.class);

        // exercise
        mReadNotification.initialize(callback);

        // verify
        verify(mTextToSpeechController).initialize(callback);
    }

    @Test(expected = NullPointerException.class)
    public void initializeArgNull() throws Exception {
        // setup
        TextToSpeechController.Callback callback = null;

        // exercise
        mReadNotification.initialize(callback);

    }

    @Test
    public void startReading() throws Exception {
        // setup
        Notification notification = mock(Notification.class);
        when(notification.getText()).thenReturn("TEST");
        when(notification.getTitle()).thenReturn("TEST");

        StringBuilder expectedArgString = new StringBuilder();
        expectedArgString.append("TEST");
        expectedArgString.append("\n");
        expectedArgString.append("TEST");

        // exercise
        mReadNotification.startReading(notification);

        // verify
        verify(notification).getText();
        verify(notification).getTitle();
        verify(mTextToSpeechController).speak(expectedArgString.toString());

    }

    @Test(expected = NullPointerException.class)
    public void startReadingArgNull() throws Exception {
        // exercise
        mReadNotification.startReading(null);

    }

    @Test
    public void stopReading() throws Exception {
        // exercise
        mReadNotification.stopReading();

        // verify
        verify(mTextToSpeechController).stop();

    }

    @Test
    public void terminate() throws Exception {
        // exercise
        mReadNotification.terminate();

        // verify
        verify(mTextToSpeechController).terminate();

    }

}