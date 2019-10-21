package jp.pioneer.carsync.infrastructure.repository;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.test.mock.MockContentResolver;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.ProviderContract;
import jp.pioneer.carsync.domain.content.DeleteParams;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.content.UpdateParams;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.model.TunerSeekStep;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/29.
 */
public class FavoriteRepositoryImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks FavoriteRepositoryImpl mFavoriteRepository = new FavoriteRepositoryImpl(){

        @Override
        CursorLoader createCursorLoader(QueryParams params) {
            return mCursorLoader;
        }
    };
    @Mock Context mContext;
    @Mock CursorLoader mCursorLoader;
    @Mock Cursor mCursor;
    @Mock Resources mResources;
    ContentValues mContentValues;
    String mSelection;
    String[] mSelectionArgs;
    Uri mUri;


    class MockImagesProvider extends ContentProvider {

        @Override
        public boolean onCreate() {
            return true;
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            assertThat(uri,is(mUri));
            assertThat(values,is(mContentValues));

            return null;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                            String sortOrder) {
            assertThat(uri,is(mUri));
            assertThat(selection,is(mSelection));
            assertThat(selectionArgs,is(mSelectionArgs));

            return mCursor;
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            assertThat(uri,is(mUri));
            assertThat(selection,is(mSelection));
            assertThat(selectionArgs,is(mSelectionArgs));

            return 0;
        }

        @Override
        public String getType(Uri uri) {
            return null;
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            assertThat(uri,is(mUri));
            assertThat(values,is(mContentValues));
            assertThat(selection,is(mSelection));
            assertThat(selectionArgs,is(mSelectionArgs));

            return 0;
        }
    }

    class MyMockContentResolver extends MockContentResolver {

        public MyMockContentResolver(Context context) {
            ContentProvider provider = new MockImagesProvider();
            ProviderInfo providerInfo = new ProviderInfo();
            providerInfo.authority = "jp.pioneer.carsync.provider";
            providerInfo.enabled = true;
            providerInfo.isSyncable = false;
            providerInfo.packageName = ContactRepositoryImplTest.MockImagesProvider.class.getPackage().getName();
            provider.attachInfo(context, providerInfo);
            super.addProvider("jp.pioneer.carsync.provider", provider);
        }
    }

    class MyMockContext extends ContextWrapper {

        private ContentResolver contentResolver;

        public MyMockContext(Context context) {
            super(context);
            contentResolver = new MyMockContentResolver(context);
        }


        @Override
        public ContentResolver getContentResolver() {
            return contentResolver;
        }

    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mContentValues = new ContentValues();
        mContentValues.put(ProviderContract.Favorite.NAME, "TEST_NAME");
        mContentValues.put(ProviderContract.Favorite.DESCRIPTION, "TEST_DESCRIPTION");
        when(mContext.getResources()).thenReturn(mResources);
        when(mResources.getString(anyInt())).thenReturn("TEST");
    }

    @Test
    public void get() throws Exception {
        // exercise
        CursorLoader actual = mFavoriteRepository.get(mock(QueryParams.class));

        // verify
        assertThat(actual,is(mCursorLoader));
    }

    @Test
    public void upsert_Insert_CursorCountZero() throws Exception {
        // setup
        ContentResolver contentResolver = new MyMockContentResolver(mContext);
        RadioInfo radioInfo = new RadioInfo();
        radioInfo.ptyInfo = "TEST_NAME";
        radioInfo.currentFrequency = 100;
        radioInfo.frequencyUnit = TunerFrequencyUnit.KHZ;
        radioInfo.band = RadioBandType.FM1;
        radioInfo.index = 10;
        radioInfo.pi = 1;
        UpdateParams updateParams = TunerContract.FavoriteContract.UpdateParamsBuilder.createRadio(radioInfo, TunerSeekStep._10KHZ, mContext);

        when(mContext.getContentResolver()).thenReturn(contentResolver);
        when(mCursor.getCount()).thenReturn(0);

        mUri = updateParams.uri;
        mSelection = updateParams.where;
        mSelectionArgs = updateParams.selectionArgs;
        mContentValues = updateParams.values;

        // exercise
        mFavoriteRepository.upsert(updateParams);

        // verify
        verify(mCursor).close();

    }

    @Test
    public void upsert_Insert_CursorNull() throws Exception {
        // setup
        ContentResolver contentResolver = new MyMockContentResolver(mContext);
        RadioInfo radioInfo = new RadioInfo();
        radioInfo.ptyInfo = "TEST_NAME";
        radioInfo.currentFrequency = 100;
        radioInfo.frequencyUnit = TunerFrequencyUnit.KHZ;
        radioInfo.band = RadioBandType.FM1;
        radioInfo.index = 10;
        radioInfo.pi = 1;
        UpdateParams updateParams = TunerContract.FavoriteContract.UpdateParamsBuilder.createRadio(radioInfo, TunerSeekStep._10KHZ, mContext);

        when(mContext.getContentResolver()).thenReturn(contentResolver);
        mCursor = null;

        mUri = updateParams.uri;
        mSelection = updateParams.where;
        mSelectionArgs = updateParams.selectionArgs;
        mContentValues = updateParams.values;

        // exercise
        mFavoriteRepository.upsert(updateParams);

    }

    @Test
    public void upsert_Update() throws Exception {
        // setup
        ContentResolver contentResolver = new MyMockContentResolver(mContext);
        RadioInfo radioInfo = new RadioInfo();
        radioInfo.ptyInfo = "TEST_NAME";
        radioInfo.currentFrequency = 100;
        radioInfo.frequencyUnit = TunerFrequencyUnit.KHZ;
        radioInfo.band = RadioBandType.FM1;
        radioInfo.index = 10;
        radioInfo.pi = 1;
        UpdateParams updateParams = TunerContract.FavoriteContract.UpdateParamsBuilder.createRadio(radioInfo, TunerSeekStep._10KHZ, mContext);

        when(mContext.getContentResolver()).thenReturn(contentResolver);
        when(mCursor.getCount()).thenReturn(1);

        mUri = updateParams.uri;
        mSelection = updateParams.where;
        mSelectionArgs = updateParams.selectionArgs;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProviderContract.Favorite.NAME, updateParams.values.getAsString(ProviderContract.Favorite.NAME));
        contentValues.put(ProviderContract.Favorite.DESCRIPTION, updateParams.values.getAsString(ProviderContract.Favorite.DESCRIPTION));
        mContentValues = contentValues;

        // exercise
        mFavoriteRepository.upsert(updateParams);

        // verify
        verify(mCursor).close();
    }

    @Test
    public void delete() throws Exception {
        // setup
        ContentResolver contentResolver = new MyMockContentResolver(mContext);
        DeleteParams deleteParams = TunerContract.FavoriteContract.DeleteParamsBuilder.createParams(10);

        when(mContext.getContentResolver()).thenReturn(contentResolver);
        when(mCursor.getCount()).thenReturn(1);

        mUri = deleteParams.uri;
        mSelection = deleteParams.where;
        mSelectionArgs = deleteParams.selectionArgs;

        // exercise
        mFavoriteRepository.delete(deleteParams);

    }

}