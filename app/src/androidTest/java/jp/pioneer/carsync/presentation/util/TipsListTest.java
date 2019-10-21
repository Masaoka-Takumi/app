package jp.pioneer.carsync.presentation.util;

import android.graphics.Bitmap;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.InvalidParameterException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.presentation.model.TipsCategory;
import jp.pioneer.carsync.presentation.model.TipsContentsEndpoint;
import jp.pioneer.carsync.presentation.model.TipsItem;
import jp.pioneer.carsync.presentation.model.TipsTag;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2018/01/23.
 */
public class TipsListTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks TipsList mTipsList = new TipsList(){
        @Override
        public HttpURLConnection createHttpUrlConnection(String url) throws Exception {
            String listUrl = TipsContentsEndpoint.PRODUCTION.endpoint + "ja/TEST_CAR_DEVICE/list.json";
            if(listUrl.equals(url)){
                return mTipsListConnection;
            } else if ("TEST_THUMBNAIL_URL".equals(url)){
                return mThumbnailConnection;
            } else if ("TEST_ICON_URL".equals(url)){
                return mIconConnection;
            }

            throw new InvalidParameterException();
        }

        @Override
        public BufferedReader createBufferedReader(InputStream stream) {
            if(stream == mTipsListInputStream) {
                return mBufferedReader;
            }
            throw new InvalidParameterException();
        }

        @Override
        public TipsItem[] buildTipsItems(BufferedReader reader) throws Exception {
            if(reader == mBufferedReader) {
                return mItems;
            }
            throw new InvalidParameterException();
        }

        @Override
        public Bitmap createBitmap(InputStream stream) throws Exception {
            if(stream == mThumbnailInputStream){
                return mThumbnail;
            } else if(stream == mIconInputStream){
                return mIcon;
            }
            throw new InvalidParameterException();
        }
    };
    @Mock AppSharedPreference mPreference;
    @Mock EventBus mEventBus;
    @Mock HttpURLConnection mTipsListConnection;
    @Mock HttpURLConnection mThumbnailConnection;
    @Mock HttpURLConnection mIconConnection;
    @Mock InputStream mTipsListInputStream;
    @Mock InputStream mThumbnailInputStream;
    @Mock InputStream mIconInputStream;
    @Mock ExecutorService mTaskExecutor;
    @Mock BufferedReader mBufferedReader;

    private Future mTaskFuture = mock(Future.class);;
    private TipsItem[] mItems;
    private Bitmap mThumbnail = Bitmap.createBitmap(10, 10, Bitmap.Config.ALPHA_8);
    private Bitmap mIcon = Bitmap.createBitmap(10, 10, Bitmap.Config.ALPHA_8);


    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mItems = new TipsItem[3];
        for (int i = 0; i < mItems.length; i++) {
            mItems[i] = new TipsItem(
                    i + 1,
                    "TEST_LINK_" + (i + 1),
                    "TEST_TITLE_" + (i + 1),
                    "TEST_THUMBNAIL_URL",
                    new TipsCategory[]{
                            new TipsCategory(
                                    1,
                                    "TEST_NAME_1",
                                    "TEST_SLUG_1"
                            ),
                            new TipsCategory(
                                    2,
                                    "TEST_NAME_2",
                                    "TEST_SLUG_2"
                            )
                    },
                    new TipsTag[]{
                            new TipsTag(
                                    1,
                                    "TEST_NAME_1",
                                    "TEST_SLUG_1",
                                    "TEST_ICON_URL"
                            ),
                            new TipsTag(
                                    2,
                                    "TEST_NAME_2",
                                    "TEST_SLUG_2",
                                    "TEST_ICON_URL"
                            )
                    },
                    "TEST_DESCRIPTION_" + (i + 1)
            );
        }

        when(mPreference.getTipsListEndpoint()).thenReturn(TipsContentsEndpoint.PRODUCTION);
        when(mPreference.getLastConnectedCarDeviceModel()).thenReturn("TEST_CAR_DEVICE");
        when(mTipsListConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mThumbnailConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mIconConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mTipsListConnection.getInputStream()).thenReturn(mTipsListInputStream);
        when(mThumbnailConnection.getInputStream()).thenReturn(mThumbnailInputStream);
        when(mIconConnection.getInputStream()).thenReturn(mIconInputStream);
        when(mTaskExecutor.submit(any(Runnable.class))).then(invocation -> {
            Runnable task = (Runnable) invocation.getArguments()[0];
            task.run();
            return mTaskFuture;
        });
    }

    @Test
    public void update() throws Exception {
        // exercise
        mTipsList.update();

        // verify
        assertThat(mTipsList.items[2].id, is(1));
        assertThat(mTipsList.items[2].link, is("TEST_LINK_1"));
        assertThat(mTipsList.items[2].title, is("TEST_TITLE_1"));
        assertThat(mTipsList.items[2].thumbUrl, is("TEST_THUMBNAIL_URL"));
        assertThat(mTipsList.items[2].thumbImage, is(mThumbnail));
        assertThat(mTipsList.items[2].categories[0].id, is(1));
        assertThat(mTipsList.items[2].categories[0].name, is("TEST_NAME_1"));
        assertThat(mTipsList.items[2].categories[0].slug, is("TEST_SLUG_1"));
        assertThat(mTipsList.items[2].categories[1].id, is(2));
        assertThat(mTipsList.items[2].categories[1].name, is("TEST_NAME_2"));
        assertThat(mTipsList.items[2].categories[1].slug, is("TEST_SLUG_2"));
        assertThat(mTipsList.items[2].tags[0].id, is(1));
        assertThat(mTipsList.items[2].tags[0].name, is("TEST_NAME_1"));
        assertThat(mTipsList.items[2].tags[0].slug, is("TEST_SLUG_1"));
        assertThat(mTipsList.items[2].tags[0].iconUrl, is("TEST_ICON_URL"));
        assertThat(mTipsList.items[2].tags[0].iconImage, is(mIcon));
        assertThat(mTipsList.items[2].tags[1].id, is(2));
        assertThat(mTipsList.items[2].tags[1].name, is("TEST_NAME_2"));
        assertThat(mTipsList.items[2].tags[1].slug, is("TEST_SLUG_2"));
        assertThat(mTipsList.items[2].tags[1].iconUrl, is("TEST_ICON_URL"));
        assertThat(mTipsList.items[2].tags[1].iconImage, is(mIcon));
        assertThat(mTipsList.items[2].description, is("TEST_DESCRIPTION_1"));
        assertThat(mTipsList.items[1].id, is(2));
        assertThat(mTipsList.items[1].link, is("TEST_LINK_2"));
        assertThat(mTipsList.items[1].title, is("TEST_TITLE_2"));
        assertThat(mTipsList.items[1].thumbUrl, is("TEST_THUMBNAIL_URL"));
        assertThat(mTipsList.items[1].thumbImage, is(mThumbnail));
        assertThat(mTipsList.items[1].categories[0].id, is(1));
        assertThat(mTipsList.items[1].categories[0].name, is("TEST_NAME_1"));
        assertThat(mTipsList.items[1].categories[0].slug, is("TEST_SLUG_1"));
        assertThat(mTipsList.items[1].categories[1].id, is(2));
        assertThat(mTipsList.items[1].categories[1].name, is("TEST_NAME_2"));
        assertThat(mTipsList.items[1].categories[1].slug, is("TEST_SLUG_2"));
        assertThat(mTipsList.items[1].tags[0].id, is(1));
        assertThat(mTipsList.items[1].tags[0].name, is("TEST_NAME_1"));
        assertThat(mTipsList.items[1].tags[0].slug, is("TEST_SLUG_1"));
        assertThat(mTipsList.items[1].tags[0].iconUrl, is("TEST_ICON_URL"));
        assertThat(mTipsList.items[1].tags[0].iconImage, is(mIcon));
        assertThat(mTipsList.items[1].tags[1].id, is(2));
        assertThat(mTipsList.items[1].tags[1].name, is("TEST_NAME_2"));
        assertThat(mTipsList.items[1].tags[1].slug, is("TEST_SLUG_2"));
        assertThat(mTipsList.items[1].tags[1].iconUrl, is("TEST_ICON_URL"));
        assertThat(mTipsList.items[1].tags[1].iconImage, is(mIcon));
        assertThat(mTipsList.items[1].description, is("TEST_DESCRIPTION_2"));
        assertThat(mTipsList.items[0].id, is(3));
        assertThat(mTipsList.items[0].link, is("TEST_LINK_3"));
        assertThat(mTipsList.items[0].title, is("TEST_TITLE_3"));
        assertThat(mTipsList.items[0].thumbUrl, is("TEST_THUMBNAIL_URL"));
        assertThat(mTipsList.items[0].thumbImage, is(mThumbnail));
        assertThat(mTipsList.items[0].categories[0].id, is(1));
        assertThat(mTipsList.items[0].categories[0].name, is("TEST_NAME_1"));
        assertThat(mTipsList.items[0].categories[0].slug, is("TEST_SLUG_1"));
        assertThat(mTipsList.items[0].categories[1].id, is(2));
        assertThat(mTipsList.items[0].categories[1].name, is("TEST_NAME_2"));
        assertThat(mTipsList.items[0].categories[1].slug, is("TEST_SLUG_2"));
        assertThat(mTipsList.items[0].tags[0].id, is(1));
        assertThat(mTipsList.items[0].tags[0].name, is("TEST_NAME_1"));
        assertThat(mTipsList.items[0].tags[0].slug, is("TEST_SLUG_1"));
        assertThat(mTipsList.items[0].tags[0].iconUrl, is("TEST_ICON_URL"));
        assertThat(mTipsList.items[0].tags[0].iconImage, is(mIcon));
        assertThat(mTipsList.items[0].tags[1].id, is(2));
        assertThat(mTipsList.items[0].tags[1].name, is("TEST_NAME_2"));
        assertThat(mTipsList.items[0].tags[1].slug, is("TEST_SLUG_2"));
        assertThat(mTipsList.items[0].tags[1].iconUrl, is("TEST_ICON_URL"));
        assertThat(mTipsList.items[0].tags[1].iconImage, is(mIcon));
        assertThat(mTipsList.items[0].description, is("TEST_DESCRIPTION_3"));

    }

    @Test
    public void update_getListFailed() throws Exception {
        // setup
        when(mTipsListConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);

        // exercise
        mTipsList.update();

        // verify
        assertThat(mTipsList.items.length, is(0));
        assertThat(mTipsList.isError, is(true));
    }

    @Test
    public void update_getThumbImageFailed() throws Exception {
        // setup
        when(mThumbnailConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);

        // exercise
        mTipsList.update();

        // verify
        assertThat(mTipsList.items[0].thumbImage, is(nullValue()));
        assertThat(mTipsList.items[0].tags[0].iconImage, is(mIcon));
        assertThat(mTipsList.items[0].tags[1].iconImage, is(mIcon));
        assertThat(mTipsList.items[1].thumbImage, is(nullValue()));
        assertThat(mTipsList.items[1].tags[0].iconImage, is(mIcon));
        assertThat(mTipsList.items[1].tags[1].iconImage, is(mIcon));
        assertThat(mTipsList.items[2].thumbImage, is(nullValue()));
        assertThat(mTipsList.items[2].tags[0].iconImage, is(mIcon));
        assertThat(mTipsList.items[2].tags[1].iconImage, is(mIcon));
    }

    @Test
    public void update_getIconImageFailed() throws Exception {
        // setup
        when(mIconConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);

        // exercise
        mTipsList.update();

        // verify
        assertThat(mTipsList.items[0].thumbImage, is(mThumbnail));
        assertThat(mTipsList.items[0].tags[0].iconImage, is(nullValue()));
        assertThat(mTipsList.items[0].tags[1].iconImage, is(nullValue()));
        assertThat(mTipsList.items[1].thumbImage, is(mThumbnail));
        assertThat(mTipsList.items[1].tags[0].iconImage, is(nullValue()));
        assertThat(mTipsList.items[1].tags[1].iconImage, is(nullValue()));
        assertThat(mTipsList.items[2].thumbImage, is(mThumbnail));
        assertThat(mTipsList.items[2].tags[0].iconImage, is(nullValue()));
        assertThat(mTipsList.items[2].tags[1].iconImage, is(nullValue()));
    }
}