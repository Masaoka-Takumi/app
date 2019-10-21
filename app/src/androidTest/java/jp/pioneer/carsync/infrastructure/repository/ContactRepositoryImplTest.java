package jp.pioneer.carsync.infrastructure.repository;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.test.mock.MockContentResolver;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.content.UpdateParams;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/13.
 */
public class ContactRepositoryImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks TestContactRepositoryImpl mTestContactRepositoryImpl = new TestContactRepositoryImpl();
    @Mock Context mContext;
    @Mock CursorLoader mCursorLoader;

    class TestContactRepositoryImpl extends ContactRepositoryImpl {
        public TestContactRepositoryImpl() {
        }

        @Override
        CursorLoader createCursorLoader(QueryParams params) {
            return mCursorLoader;
        }
    }

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
            return null;
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
            providerInfo.authority = "com.android.contacts";
            providerInfo.enabled = true;
            providerInfo.isSyncable = false;
            providerInfo.packageName = MockImagesProvider.class.getPackage().getName();
            provider.attachInfo(context, providerInfo);
            super.addProvider("com.android.contacts", provider);
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
    }

    @Test
    public void get() throws Exception {
        // exercise
        CursorLoader actual = mTestContactRepositoryImpl.get(mock(QueryParams.class));

        // verify
        assertThat(actual,is(mCursorLoader));

    }

    @Test(expected = NullPointerException.class)
    public void getArgNull() throws Exception {
        // setup
        QueryParams queryParams = null;

        // exercise
        CursorLoader actual = mTestContactRepositoryImpl.get(queryParams);

    }

    @Test
    public void update() throws Exception {
        // setup
        ContentResolver contentResolver = new MyMockContentResolver(mContext);
        UpdateParams updateParams = ContactsContract.UpdateParamsBuilder.createContact("contacts",new ContentValues());
        when(mContext.getContentResolver()).thenReturn(contentResolver);

        // exercise
        int actual = mTestContactRepositoryImpl.update(updateParams);

        // verify
        assertThat(actual,is(0));
    }

    @Test(expected = NullPointerException.class)
    public void updateArgNull() throws Exception {
        // setup
        UpdateParams updateParams = null;

        // exercise
        int actual = mTestContactRepositoryImpl.update(updateParams);

    }

}