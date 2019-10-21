package jp.pioneer.carsync.infrastructure.content;

import android.app.Instrumentation;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.test.mock.MockContentResolver;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.content.QueryParams;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.content.AppMusicCursorLoader.SECTION_INDEXES;
import static jp.pioneer.carsync.domain.content.AppMusicCursorLoader.SECTION_STRINGS;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/10.
 */
public class AppMusicCursorLoaderImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    AppMusicCursorLoaderImpl mAppMusicCursorLoader;

    class MockImagesProvider extends ContentProvider {

        @Override
        public boolean onCreate() {
            return true;
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            return null;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                            String sortOrder) {
            return mCursor;
        }

        @Override
        public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
            return mCursor;
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            return 0;
        }

        @Override
        public String getType(Uri uri) {
            return null;
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            return 0;
        }
    }

    class MyMockContentResolver extends MockContentResolver {

        public MyMockContentResolver(Context context) {
            ContentProvider provider = new MockImagesProvider();
            ProviderInfo providerInfo = new ProviderInfo();
            providerInfo.authority = "media";
            providerInfo.enabled = true;
            providerInfo.isSyncable = false;
            providerInfo.packageName = MockImagesProvider.class.getPackage().getName();
            provider.attachInfo(context, providerInfo);
            super.addProvider("media", provider);
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

    static final String[] projection = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA,
    };

    Context mContext;
    Context mAppContext;
    Cursor mCursor;
    QueryParams mQueryParams;
    Handler mHandler;

    Instrumentation instr = InstrumentationRegistry.getInstrumentation();

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mContext = mock(Context.class);
        mAppContext = mock(Context.class);
        mHandler = new Handler(Looper.getMainLooper());

        when(mContext.getApplicationContext()).thenReturn(mAppContext);
        ContentResolver contentResolver = new MyMockContentResolver(mAppContext);
        when(mAppContext.getContentResolver()).thenReturn(contentResolver);

        MatrixCursor matrixCursor = new MatrixCursor(projection);
        matrixCursor.addRow(new Object[] { 1,"A_TITLE","E_ARTIST","I_ALBUM",5,9,"M_DATA"});
        matrixCursor.addRow(new Object[] { 2,"B_TITLE","F_ARTIST","J_ALBUM",6,10,"N_DATA"});
        matrixCursor.addRow(new Object[] { 3,"C_TITLE","G_ARTIST","K_ALBUM",7,11,"L_DATA"});
        matrixCursor.addRow(new Object[] { 4,"D_TITLE","H_ARTIST","L_ALBUM",8,12,"O_DATA"});
        mCursor = matrixCursor;
    }

    @Test
    public void getExtras_HappyPath() throws Exception {
        // setup
        mQueryParams = AppMusicContract.QueryParamsBuilder.createAllSongs();

        String[] expectedStringArray = {"A","B","C","D"};
        int[] expectedIntArray = {0,1,2,3};

        // exercise 
        instr.runOnMainSync(() -> {
            mAppMusicCursorLoader = new AppMusicCursorLoaderImpl(mContext, mQueryParams);
                });
        Cursor cursor = mAppMusicCursorLoader.loadInBackground();
        Bundle bundle = mAppMusicCursorLoader.getExtras();

        // verify
        Cursor actualCursor = ((CursorWrapper)cursor).getWrappedCursor();
        String[] actualSectionStringStringArray = bundle.getStringArray(SECTION_STRINGS);
        String[] actualSectionIndexStringArray = bundle.getStringArray(SECTION_INDEXES);
        int[] actualIntArray = bundle.getIntArray(SECTION_INDEXES);

        assertThat(actualCursor,is(mCursor));
        assertThat(actualSectionStringStringArray,is(expectedStringArray));
        assertThat(actualSectionIndexStringArray,is(nullValue()));
        assertThat(actualIntArray,is(expectedIntArray));
    }

    @Test
    public void getExtras_IndexColumnNull() throws Exception {
        // setup
        mQueryParams = AppMusicContract.QueryParamsBuilder.createGenresForAudioId(0);

        // exercise
        instr.runOnMainSync(() -> {
            mAppMusicCursorLoader = new AppMusicCursorLoaderImpl(mContext, mQueryParams);
                });
        Cursor cursor = mAppMusicCursorLoader.loadInBackground();
        Bundle bundle = mAppMusicCursorLoader.getExtras();

        // verify
        Cursor actualCursor = ((CursorWrapper)cursor).getWrappedCursor();
        String[] actualSectionStringStringArray = bundle.getStringArray(SECTION_STRINGS);
        String[] actualSectionIndexStringArray = bundle.getStringArray(SECTION_INDEXES);
        int[] actualIntArray = bundle.getIntArray(SECTION_INDEXES);

        assertThat(actualCursor,is(mCursor));
        assertThat(actualSectionStringStringArray,is(nullValue()));
        assertThat(actualSectionIndexStringArray,is(nullValue()));
        assertThat(actualIntArray,is(nullValue()));
    }

    @Test
    public void getExtras_CursorNull() throws Exception {
        // setup
        mCursor = null;
        mQueryParams = AppMusicContract.QueryParamsBuilder.createAllSongs();

        // exercise
        instr.runOnMainSync(() -> {
                    mAppMusicCursorLoader = new AppMusicCursorLoaderImpl(mContext, mQueryParams);
                });
        Cursor actualCursor = mAppMusicCursorLoader.loadInBackground();
        Bundle bundle = mAppMusicCursorLoader.getExtras();

        // verify
        String[] actualSectionStringStringArray = bundle.getStringArray(SECTION_STRINGS);
        String[] actualSectionIndexStringArray = bundle.getStringArray(SECTION_INDEXES);
        int[] actualIntArray = bundle.getIntArray(SECTION_INDEXES);

        assertThat(actualCursor,is(nullValue()));
        assertThat(actualSectionStringStringArray,is(nullValue()));
        assertThat(actualSectionIndexStringArray,is(nullValue()));
        assertThat(actualIntArray,is(nullValue()));
    }

    @Test
    public void getExtras_CursorCountZero() throws Exception {
        // setup
        MatrixCursor expectedCursor = new MatrixCursor(projection);
        mCursor = expectedCursor;
        mQueryParams = AppMusicContract.QueryParamsBuilder.createAllSongs();

        // exercise
        instr.runOnMainSync(() -> {
                    mAppMusicCursorLoader = new AppMusicCursorLoaderImpl(mContext, mQueryParams);
                });
        Cursor cursor = mAppMusicCursorLoader.loadInBackground();
        Bundle bundle = mAppMusicCursorLoader.getExtras();

        // verify
        Cursor actualCursor = ((CursorWrapper)cursor).getWrappedCursor();
        String[] actualSectionStringStringArray = bundle.getStringArray(SECTION_STRINGS);
        String[] actualSectionIndexStringArray = bundle.getStringArray(SECTION_INDEXES);
        int[] actualIntArray = bundle.getIntArray(SECTION_INDEXES);

        assertThat(actualCursor,is(mCursor));
        assertThat(actualSectionStringStringArray,is(nullValue()));
        assertThat(actualSectionIndexStringArray,is(nullValue()));
        assertThat(actualIntArray,is(nullValue()));
    }
}