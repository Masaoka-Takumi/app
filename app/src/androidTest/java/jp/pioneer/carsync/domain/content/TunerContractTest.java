package jp.pioneer.carsync.domain.content;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Date;
import java.util.Locale;

import jp.pioneer.carsync.application.content.ProviderContract;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.model.TunerSeekStep;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.content.TunerContract.FavoriteContract.Radio;
import static jp.pioneer.carsync.domain.content.TunerContract.FavoriteContract.SiriusXm;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/29.
 */
@RunWith(Enclosed.class)
public class TunerContractTest {
    @RunWith(Enclosed.class)
    public static class FavoriteContractTest {

        public static class QueryParamsBuilderTest {
            @Rule public MockitoRule mMockito = MockitoJUnit.rule();

            @Before
            public void setUp() throws Exception {
                System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
            }

            @Test
            public void createRadio() {
                // exercise
                QueryParams queryParams = TunerContract.FavoriteContract.QueryParamsBuilder.createRadio();

                // verify
                assertThat(queryParams, is(new QueryParams(
                        ProviderContract.Favorite.CONTENT_URI,
                        TunerContract.FavoriteContract.Radio.PROJECTION,
                        "(" + ProviderContract.Favorite.SOURCE_ID + " = ?)",
                        new String[]{String.valueOf(MediaSourceType.RADIO.code)},
                        Radio.SORT_ORDER,
                        null
                )));
            }

            @Test
            public void createSiriusXm() {
                // exercise
                QueryParams queryParams = TunerContract.FavoriteContract.QueryParamsBuilder.createSiriusXm();

                // verify
                assertThat(queryParams, is(new QueryParams(
                        ProviderContract.Favorite.CONTENT_URI,
                        TunerContract.FavoriteContract.SiriusXm.PROJECTION,
                        "(" + ProviderContract.Favorite.SOURCE_ID + " = ?)",
                        new String[]{String.valueOf(MediaSourceType.SIRIUS_XM.code)},
                        TunerContract.FavoriteContract.SiriusXm.SORT_ORDER,
                        null
                )));
            }
        }

        public static class UpdateParamsBuilderTest {
            @Rule public MockitoRule mMockito = MockitoJUnit.rule();
            @Mock Context mContext;
            @Mock Resources mResources;

            @Before
            public void setUp() throws Exception {
                System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
                when(mContext.getResources()).thenReturn(mResources);
                when(mResources.getString(anyInt())).thenReturn("TEST");
            }

            @Test
            public void createRadio_isFM() {
                // setup
                RadioInfo radioInfo = new RadioInfo();
                radioInfo.ptyInfo = "TEST_NAME";
                radioInfo.currentFrequency = 100;
                radioInfo.frequencyUnit = TunerFrequencyUnit.KHZ;
                radioInfo.band = RadioBandType.FM1;
                radioInfo.index = 10;
                radioInfo.pi = 1;

                TunerSeekStep tunerSeekStep = TunerSeekStep._10KHZ;

                float value = radioInfo.currentFrequency / ((float) radioInfo.frequencyUnit.divide);
                String format = "%." + radioInfo.frequencyUnit.fraction + "f%s";
                String frequency = String.format(Locale.ENGLISH, format, value, "TEST");
                String description = String.format(Locale.ENGLISH, "%s %s", "TEST", frequency);

                ContentValues values = new ContentValues();
                values.put(ProviderContract.Favorite.SOURCE_ID, MediaSourceType.RADIO.code);
                values.put(ProviderContract.Favorite.NAME, radioInfo.ptyInfo);
                values.put(ProviderContract.Favorite.DESCRIPTION, description);
                values.put(ProviderContract.Favorite.TUNER_CHANNEL_KEY1, radioInfo.currentFrequency);
                values.put(ProviderContract.Favorite.TUNER_FREQUENCY_INDEX, radioInfo.index);
                values.put(ProviderContract.Favorite.TUNER_BAND, radioInfo.getBand().code);
                values.put(ProviderContract.Favorite.TUNER_PARAM1, radioInfo.pi);
                values.put(ProviderContract.Favorite.CREATE_DATE, System.currentTimeMillis());

                StringBuilder selection = new StringBuilder();
                selection.append(ProviderContract.Favorite.SOURCE_ID).append(" = ?");
                selection.append(" AND ").append(ProviderContract.Favorite.TUNER_CHANNEL_KEY1 + " = ?");
                selection.append(" AND ").append(ProviderContract.Favorite.TUNER_CHANNEL_KEY2 + " IS NULL");

                String[] selectionArgs = new String[]{
                        String.valueOf(MediaSourceType.RADIO.code),
                        String.valueOf(radioInfo.currentFrequency)
                };

                // exercise
                UpdateParams actual = TunerContract.FavoriteContract.UpdateParamsBuilder.createRadio(radioInfo, tunerSeekStep, mContext);
                UpdateParams expected = new UpdateParams(ProviderContract.Favorite.CONTENT_URI, values, selection.toString(),selectionArgs);

                // verify
                actual.values.remove(ProviderContract.Favorite.CREATE_DATE);
                expected.values.remove(ProviderContract.Favorite.CREATE_DATE);

                assertThat(actual, is(expected));
                verify(mResources).getString(radioInfo.frequencyUnit.label);
                verify(mResources).getString(radioInfo.getBand().label);
            }

            @Test
            public void createRadio_isAM() {
                // setup
                RadioInfo radioInfo = new RadioInfo();
                radioInfo.ptyInfo = "TEST_NAME";
                radioInfo.currentFrequency = 100;
                radioInfo.frequencyUnit = TunerFrequencyUnit.KHZ;
                radioInfo.band = RadioBandType.AM;
                radioInfo.index = 10;
                radioInfo.pi = 1;

                TunerSeekStep tunerSeekStep = TunerSeekStep._10KHZ;

                float value = radioInfo.currentFrequency / ((float) radioInfo.frequencyUnit.divide);
                String format = "%." + radioInfo.frequencyUnit.fraction + "f%s";
                String frequency = String.format(Locale.ENGLISH, format, value, "TEST");
                String description = String.format(Locale.ENGLISH, "%s %s", "TEST", frequency);

                ContentValues values = new ContentValues();
                values.put(ProviderContract.Favorite.SOURCE_ID, MediaSourceType.RADIO.code);
                values.put(ProviderContract.Favorite.NAME, radioInfo.ptyInfo);
                values.put(ProviderContract.Favorite.DESCRIPTION, description);
                values.put(ProviderContract.Favorite.TUNER_CHANNEL_KEY1, radioInfo.currentFrequency);
                values.put(ProviderContract.Favorite.TUNER_FREQUENCY_INDEX, radioInfo.index);
                values.put(ProviderContract.Favorite.TUNER_BAND, radioInfo.getBand().code);
                values.put(ProviderContract.Favorite.TUNER_PARAM1, radioInfo.pi);
                values.put(ProviderContract.Favorite.TUNER_PARAM2, tunerSeekStep.code);
                values.put(ProviderContract.Favorite.CREATE_DATE, System.currentTimeMillis());

                StringBuilder selection = new StringBuilder();
                selection.append(ProviderContract.Favorite.SOURCE_ID).append(" = ?");
                selection.append(" AND ").append(ProviderContract.Favorite.TUNER_CHANNEL_KEY1 + " = ?");
                selection.append(" AND ").append(ProviderContract.Favorite.TUNER_CHANNEL_KEY2 + " IS NULL");

                String[] selectionArgs = new String[]{
                        String.valueOf(MediaSourceType.RADIO.code),
                        String.valueOf(radioInfo.currentFrequency)
                };

                // exercise
                UpdateParams actual = TunerContract.FavoriteContract.UpdateParamsBuilder.createRadio(radioInfo, tunerSeekStep, mContext);
                UpdateParams expected = new UpdateParams(ProviderContract.Favorite.CONTENT_URI, values, selection.toString(),selectionArgs);

                // verify
                actual.values.remove(ProviderContract.Favorite.CREATE_DATE);
                expected.values.remove(ProviderContract.Favorite.CREATE_DATE);

                assertThat(actual, is(expected));
                verify(mResources).getString(radioInfo.frequencyUnit.label);
                verify(mResources).getString(radioInfo.getBand().label);
            }

            @Test
            public void createSiriusXm() {
                // setup
                SxmMediaInfo sxmInfo = new SxmMediaInfo();
                sxmInfo.artistNameOrContentInfo = "TEST_NAME";
                sxmInfo.currentChannelNumber = 1;
                sxmInfo.sid = 100;
                sxmInfo.band = SxmBandType.SXM1;

                ContentValues values = new ContentValues();
                values.put(ProviderContract.Favorite.SOURCE_ID, MediaSourceType.SIRIUS_XM.code);
                values.put(ProviderContract.Favorite.NAME, sxmInfo.artistNameOrContentInfo);
                values.put(ProviderContract.Favorite.DESCRIPTION, String.format(Locale.ENGLISH, "%s %s", "TEST", sxmInfo.currentChannelNumber));
                values.put(ProviderContract.Favorite.TUNER_CHANNEL_KEY1, sxmInfo.sid);
                values.put(ProviderContract.Favorite.TUNER_BAND, sxmInfo.getBand().code);
                values.put(ProviderContract.Favorite.TUNER_PARAM1, sxmInfo.currentChannelNumber);
                values.put(ProviderContract.Favorite.CREATE_DATE, System.currentTimeMillis());

                StringBuilder selection = new StringBuilder();
                selection.append(ProviderContract.Favorite.SOURCE_ID).append(" = ?");
                selection.append(" AND ").append(ProviderContract.Favorite.TUNER_CHANNEL_KEY1 + " = ?");
                selection.append(" AND ").append(ProviderContract.Favorite.TUNER_CHANNEL_KEY2 + " IS NULL");

                String[] selectionArgs = new String[]{
                        String.valueOf(MediaSourceType.SIRIUS_XM.code),
                        String.valueOf(sxmInfo.sid)
                };

                // exercise
                UpdateParams actual = TunerContract.FavoriteContract.UpdateParamsBuilder.createSiriusXm(sxmInfo, mContext);
                UpdateParams expected = new UpdateParams(ProviderContract.Favorite.CONTENT_URI, values, selection.toString(), selectionArgs);

                // verify
                actual.values.remove(ProviderContract.Favorite.CREATE_DATE);
                expected.values.remove(ProviderContract.Favorite.CREATE_DATE);

                assertThat(actual, is(expected));
                verify(mResources).getString(sxmInfo.getBand().label);
            }
        }
    }

    public static class DeleteParamsBuilderTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        }

        @Test
        public void createParams() {
            // exercise
            DeleteParams deleteParams = TunerContract.FavoriteContract.DeleteParamsBuilder.createParams(1);

            // verify
            assertThat(deleteParams, is( new DeleteParams(
                    ProviderContract.Favorite.CONTENT_URI,
                    "(" + ProviderContract.Favorite._ID + " = ?)",
                    new String[]{String.valueOf(1)}
            )));
        }
    }

    public static class RadioTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;
        Date mDate = new Date(System.currentTimeMillis());

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            MatrixCursor cursor = new MatrixCursor(Radio.PROJECTION);
            cursor.addRow(new Object[] { 1, "TEST_NAME_1", "TEST_DESCRIPTION_1", 1000, 1 , RadioBandType.AM.getCode() , 10, TunerSeekStep._9KHZ.code , mDate.getTime()});
            cursor.addRow(new Object[] { 2, "TEST_NAME_2", "TEST_DESCRIPTION_2", 2000, 2 , RadioBandType.FM1.getCode(), 20, TunerSeekStep._10KHZ.code, mDate.getTime()});
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() {
            // exercise
            long actual = TunerContract.FavoriteContract.Radio.getId(mCursor);

            // verify
            assertThat(actual, is(1L));
        }

        @Test
        public void getName() {
            // exercise
            String actual = TunerContract.FavoriteContract.Radio.getName(mCursor);

            // verify
            assertThat(actual, is("TEST_NAME_1"));
        }

        @Test
        public void getDescription() {
            // exercise
            String actual = TunerContract.FavoriteContract.Radio.getDescription(mCursor);

            // verify
            assertThat(actual, is("TEST_DESCRIPTION_1"));
        }

        @Test
        public void getFrequency() {
            // exercise
            long actual = TunerContract.FavoriteContract.Radio.getFrequency(mCursor);

            // verify
            assertThat(actual, is(1000L));
        }

        @Test
        public void getIndex() {
            // exercise
            long actual = TunerContract.FavoriteContract.Radio.getIndex(mCursor);

            // verify
            assertThat(actual, is(1L));
        }

        @Test
        public void getRadioBandType() {
            // exercise
            RadioBandType actual = TunerContract.FavoriteContract.Radio.getRadioBandType(mCursor);

            // verify
            assertThat(actual, is(RadioBandType.AM));
        }

        @Test
        public void getPi() {
            // exercise
            long actual = TunerContract.FavoriteContract.Radio.getPi(mCursor);

            // verify
            assertThat(actual, is(10L));
        }

        @Test
        public void getTunerSeekStep() {
            // exercise
            TunerSeekStep actual = TunerContract.FavoriteContract.Radio.getTunerSeekStep(mCursor);

            // verify
            assertThat(actual, is(TunerSeekStep._9KHZ));
        }

        @Test
        public void getCreateDate() {
            // exercise
            Date actual = TunerContract.FavoriteContract.Radio.getCreateDate(mCursor);

            // verify
            assertThat(actual, is(mDate));
        }

        @Test(expected = NullPointerException.class)
        public void getIdArgNull() {
            // exercise
            TunerContract.FavoriteContract.Radio.getId(null);
        }

        @Test(expected = NullPointerException.class)
        public void getNameArgNull() {
            // exercise
            TunerContract.FavoriteContract.Radio.getName(null);
        }

        @Test(expected = NullPointerException.class)
        public void getDescriptionArgNull() {
            // exercise
            TunerContract.FavoriteContract.Radio.getDescription(null);
        }

        @Test(expected = NullPointerException.class)
        public void getFrequencyArgNull() {
            // exercise
            TunerContract.FavoriteContract.Radio.getFrequency(null);
        }

        @Test(expected = NullPointerException.class)
        public void getIndexArgNull() {
            // exercise
            TunerContract.FavoriteContract.Radio.getIndex(null);
        }

        @Test(expected = NullPointerException.class)
        public void getRadioBandTypeArgNull() {
            // exercise
            TunerContract.FavoriteContract.Radio.getRadioBandType(null);
        }

        @Test(expected = NullPointerException.class)
        public void getPiArgNull() {
            // exercise
            TunerContract.FavoriteContract.Radio.getPi(null);
        }

        @Test(expected = NullPointerException.class)
        public void getTunerSeekStepArgNull() {
            // exercise
            TunerContract.FavoriteContract.Radio.getTunerSeekStep(null);
        }

        @Test(expected = NullPointerException.class)
        public void getCreateDateArgNull() {
            // exercise
            TunerContract.FavoriteContract.Radio.getCreateDate(null);
        }
    }

    public static class SiriusXmTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;
        Date mDate = new Date(System.currentTimeMillis());

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            MatrixCursor cursor = new MatrixCursor(SiriusXm.PROJECTION);
            cursor.addRow(new Object[] { 1, "TEST_NAME_1", "TEST_DESCRIPTION_1", 1000, SxmBandType.SXM1.getCode() , 10, mDate.getTime()});
            cursor.addRow(new Object[] { 2, "TEST_NAME_2", "TEST_DESCRIPTION_2", 2000, SxmBandType.SXM2.getCode(), 20, mDate.getTime()});
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() {
            // exercise
            long actual = TunerContract.FavoriteContract.SiriusXm.getId(mCursor);

            // verify
            assertThat(actual, is(1L));
        }

        @Test
        public void getName() {
            // exercise
            String actual = TunerContract.FavoriteContract.SiriusXm.getName(mCursor);

            // verify
            assertThat(actual, is("TEST_NAME_1"));
        }

        @Test
        public void getDescription() {
            // exercise
            String actual = TunerContract.FavoriteContract.SiriusXm.getDescription(mCursor);

            // verify
            assertThat(actual, is("TEST_DESCRIPTION_1"));
        }

        @Test
        public void getSid() {
            // exercise
            int actual = TunerContract.FavoriteContract.SiriusXm.getSid(mCursor);

            // verify
            assertThat(actual, is(1000));
        }

        @Test
        public void getSxmBandType() {
            // exercise
            SxmBandType actual = TunerContract.FavoriteContract.SiriusXm.getSxmBandType(mCursor);

            // verify
            assertThat(actual, is(SxmBandType.SXM1));
        }

        @Test
        public void getChannelNo() {
            // exercise
            int actual = TunerContract.FavoriteContract.SiriusXm.getChannelNo(mCursor);

            // verify
            assertThat(actual, is(10));
        }

        @Test
        public void getCreateDate() {
            // exercise
            Date actual = TunerContract.FavoriteContract.SiriusXm.getCreateDate(mCursor);

            // verify
            assertThat(actual, is(mDate));
        }

        @Test(expected = NullPointerException.class)
        public void getIdArgNull() {
            // exercise
            TunerContract.FavoriteContract.SiriusXm.getId(null);
        }

        @Test(expected = NullPointerException.class)
        public void getNameArgNull() {
            // exercise
            TunerContract.FavoriteContract.SiriusXm.getName(null);
        }

        @Test(expected = NullPointerException.class)
        public void getDescriptionArgNull() {
            // exercise
            TunerContract.FavoriteContract.SiriusXm.getDescription(null);
        }

        @Test(expected = NullPointerException.class)
        public void getSidArgNull() {
            // exercise
            TunerContract.FavoriteContract.SiriusXm.getSid(null);
        }

        @Test(expected = NullPointerException.class)
        public void getRadioBandTypeArgNull() {
            // exercise
            TunerContract.FavoriteContract.SiriusXm.getSxmBandType(null);
        }

        @Test(expected = NullPointerException.class)
        public void getChannelNoArgNull() {
            // exercise
            TunerContract.FavoriteContract.SiriusXm.getChannelNo(null);
        }

        @Test(expected = NullPointerException.class)
        public void getCreateDateArgNull() {
            // exercise
            TunerContract.FavoriteContract.SiriusXm.getCreateDate(null);
        }
    }
}
