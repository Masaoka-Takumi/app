package jp.pioneer.carsync.domain.content;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import jp.pioneer.carsync.domain.util.QueryUtil;

import static android.provider.CallLog.Calls.*;
import static android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.content.ContactsContract.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * ContactsContractのテスト.
 */
@RunWith(Enclosed.class)
public class ContactsContractTest {
    private static final String EMPTY = ("EMPTY").replace("EMPTY","");

    public static class QueryParamsBuilderTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        }

        @Test
        public void createContacts() throws Exception {
            // exercise
            QueryParams queryParams = QueryParamsBuilder.createContacts();

            // verify
            assertThat(queryParams,is(new QueryParams(
                    Contacts.CONTENT_URI,
                    Contact.PROJECTION,
                    "((" + Contacts.DISPLAY_NAME + " NOTNULL)" +
                            " AND (" + Contacts.DISPLAY_NAME + " != '')" +
                            " AND (" + Contacts.LOOKUP_KEY + " NOTNULL)" +
                            " AND (" + Contacts.LOOKUP_KEY + " != '')" +
                            " AND (" + Contacts.HAS_PHONE_NUMBER + " = 1))",
                    null,
                    Contact.SORT_ORDER,
                    null
            )));
        }

        @Test
        public void createContact() throws Exception {
            // exercise
            QueryParams queryParams = QueryParamsBuilder.createContact("TEST");

            // verify
            assertThat(queryParams,is(new QueryParams(
                    Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, "TEST"),
                    Contact.PROJECTION,
                    "((" + Contacts.DISPLAY_NAME + " NOTNULL)" +
                            " AND (" + Contacts.DISPLAY_NAME + " != '')" +
                            " AND (" + Contacts.LOOKUP_KEY + " NOTNULL)" +
                            " AND (" + Contacts.LOOKUP_KEY + " != '')" +
                            " AND (" + Contacts.HAS_PHONE_NUMBER + " = 1))",
                    null,
                    null,
                    null
            )));
        }

        @Test(expected = NullPointerException.class)
        public void createContactArgNull() throws Exception {
            // exercise
            String lookupKey = null;
            QueryParams queryParams = QueryParamsBuilder.createContact(lookupKey);
        }

        @Test(expected = IllegalArgumentException.class)
        public void createContactArgEmpty() throws Exception {
            // exercise
            String lookupKey = EMPTY;
            QueryParams queryParams = QueryParamsBuilder.createContact(lookupKey);
        }

        @Test
        public void createContactArgMinLength() throws Exception {
            // exercise
            String lookupKey = "1";
            QueryParams queryParams = QueryParamsBuilder.createContact(lookupKey);
        }

        @Test
        public void createContactsByKeywords() throws Exception {
            // setup
            String[] keywords = {"TEST","TEST"};

            // exercise
            QueryParams queryParams = QueryParamsBuilder.createContactsByKeywords(keywords);

            // verify
            assertThat(queryParams,is(new QueryParams(
                    Contacts.CONTENT_URI,
                    Contact.PROJECTION,
                    "(" + QueryUtil.makeLikeSelection(Contacts.DISPLAY_NAME, keywords.length) +
                            " AND (" + Contacts.LOOKUP_KEY + " NOTNULL)" +
                            " AND (" + Contacts.LOOKUP_KEY + " != '')" +
                            " AND (" + Contacts.HAS_PHONE_NUMBER + " = 1))",
                    QueryUtil.makeLikeSelectionArgs(keywords),
                    Contact.SORT_ORDER,
                    null
            )));
        }

        @Test(expected = NullPointerException.class)
        public void createContactsByKeywordsArgNull() throws Exception {
            // exercise
            String[] keywords = null;
            QueryParams queryParams = QueryParamsBuilder.createContactsByKeywords(keywords);

        }

        @Test(expected = IllegalArgumentException.class)
        public void createContactsByKeywordsArgEmpty() throws Exception {
            // exercise
            String[] keywords = {};
            QueryParams queryParams = QueryParamsBuilder.createContactsByKeywords(keywords);

        }

        @Test
        public void createContactsByKeywordsArgSizeMin() throws Exception {
            // exercise
            String[] keywords = {"TEST"};
            QueryParams queryParams = QueryParamsBuilder.createContactsByKeywords(keywords);

        }

        @Test
        public void createFavoriteContacts() throws Exception {
            // exercise
            QueryParams queryParams = QueryParamsBuilder.createFavoriteContacts();

            // verify
            assertThat(queryParams,is(new QueryParams(
                    Contacts.CONTENT_URI,
                    Contact.PROJECTION,
                    "((" + Contacts.DISPLAY_NAME + " NOTNULL)" +
                            " AND (" + Contacts.DISPLAY_NAME + " != '')" +
                            " AND (" + Contacts.LOOKUP_KEY + " NOTNULL)" +
                            " AND (" + Contacts.LOOKUP_KEY + " != '')" +
                            " AND (" + Contacts.HAS_PHONE_NUMBER + " = 1)" +
                            " AND (" + Contacts.STARRED + " = 1))",
                    null,
                    Contact.SORT_ORDER,
                    null
            )));

        }

        @Test
        public void createPhones() throws Exception {
            // exercise
            QueryParams queryParams = QueryParamsBuilder.createPhones(1l);

            // verify
            assertThat(queryParams,is(new QueryParams(
                    CommonDataKinds.Phone.CONTENT_URI,
                    Phone.PROJECTION,
                    "((" + CommonDataKinds.Phone.CONTACT_ID + " = ?)" +
                            " AND (" + CommonDataKinds.Phone.NUMBER + " NOTNULL)" +
                            " AND (" + CommonDataKinds.Phone.NUMBER + " != ''))",
                    new String[] { String.valueOf(1l) },
                    Phone.SORT_ORDER,
                    null
            )));
        }

        @Test
        public void createPhone() throws Exception {
            // exercise
            QueryParams queryParams = QueryParamsBuilder.createPhone(1l);

            // verify
            assertThat(queryParams,is(new QueryParams(
                    CommonDataKinds.Phone.CONTENT_URI,
                    Phone.PROJECTION,
                    "((" + CommonDataKinds.Phone._ID + " = ?)" +
                            " AND (" + CommonDataKinds.Phone.NUMBER + " NOTNULL)" +
                            " AND (" + CommonDataKinds.Phone.NUMBER + " != ''))",
                    new String[] { String.valueOf(1l) },
                    null,
                    null
            )));
        }

        @Test
        public void createCalls() throws Exception {
            // exercise
            QueryParams queryParams = QueryParamsBuilder.createCalls();

            // verify
            assertThat(queryParams,is(new QueryParams(
                    CallLog.Calls.CONTENT_URI,
                    Call.PROJECTION,
                    CallLog.Calls.TYPE + " IN (" + INCOMING_TYPE + "," + OUTGOING_TYPE + "," + MISSED_TYPE + ")",
                    null,
                    Call.SORT_ORDER,
                    null
            )));

        }

    }

    public static class UpdateParamsBuilderTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        }

        @Test
        public void createContact() throws Exception {
            // exercise
            UpdateParams updateParams = UpdateParamsBuilder.createContact("TEST", new ContentValues());

            // verify
            assertThat(updateParams,is(new UpdateParams(
                    Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, "TEST"),
                    new ContentValues(),
                    null,
                    null
            )));
        }

        @Test(expected = NullPointerException.class)
        public void createContactArgNull() throws Exception {
            // verify
            String lookupKey = null;
            UpdateParams updateParams = UpdateParamsBuilder.createContact(lookupKey,new ContentValues());

        }

        @Test(expected = IllegalArgumentException.class)
        public void createContactArgEmpty() throws Exception {
            // verify
            String lookupKey = EMPTY;
            UpdateParams updateParams = UpdateParamsBuilder.createContact(lookupKey,new ContentValues());

        }

        @Test
        public void createContactArgMinLength() throws Exception {
            // verify
            String lookupKey = "1";
            UpdateParams updateParams = UpdateParamsBuilder.createContact(lookupKey,new ContentValues());

        }

    }

    public static class ContactTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            String[] projection = new String[] {
                    Contacts._ID,
                    Contacts.LOOKUP_KEY,
                    Contacts.PHOTO_URI,
                    Contacts.DISPLAY_NAME,
                    Contacts.STARRED
            };
            MatrixCursor cursor = new MatrixCursor(projection);
            cursor.addRow(new Object[] { 1, "lookup1", "content://com.android.contacts/display_photo/1", "name1", 1 });
            cursor.addRow(new Object[] { 2, "lookup2", "content://com.android.contacts/display_photo/2", "name2", 0 });
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() throws Exception {
            // exercise
            long actual = Contact.getId(mCursor);

            // verify
            assertThat(actual, is(1L));
        }

        @Test(expected = NullPointerException.class)
        public void getIdArgNull() throws Exception {
            // exercise
            Contact.getId(null);
        }

        @Test
        public void getLookupKey() throws Exception {
            // exercise
            String actual = Contact.getLookupKey(mCursor);

            // verify
            assertThat(actual, is("lookup1"));
        }

        @Test(expected = NullPointerException.class)
        public void getLookupKeyArgNull() throws Exception {
            // exercise
            Contact.getLookupKey(null);
        }

        @Test
        public void getPhotoUri() throws Exception {
            // exercise
            Uri actual = Contact.getPhotoUri(mCursor);

            // verify
            assertThat(actual, is(Uri.parse("content://com.android.contacts/display_photo/1")));
        }

        @Test(expected = NullPointerException.class)
        public void getPhotoUriArgNull() throws Exception {
            // exercise
            Contact.getPhotoUri(null);
        }

        @Test
        public void getDisplayName() throws Exception {
            // exercise
            String actual = Contact.getDisplayName(mCursor);

            // verify
            assertThat(actual,is ("name1"));
        }

        @Test(expected = NullPointerException.class)
        public void getDisplayNameArgNull() throws Exception {
            // exercise
            Contact.getDisplayName(null);
        }

        @Test
        public void isStarred() throws Exception {
            // exercise
            boolean actual1 = Contact.isStarred(mCursor);
            mCursor.moveToNext();
            boolean actual2 = Contact.isStarred(mCursor);

            // verify
            assertThat(actual1, is(true));
            assertThat(actual2, is(false));
        }

        @Test
        public void isStarredFalse() throws Exception {
            // setup
            mCursor.moveToNext();

            // exercise
            boolean actual = Contact.isStarred(mCursor);

            // verify
            assertThat(actual, is(false));
        }

        @Test(expected = NullPointerException.class)
        public void isStarredArgNull() throws Exception {
            // exercise
            Contact.isStarred(null);
        }

        @Test
        public void setStarredTrue() throws Exception {
            // exercise
            ContentValues actual = Contact.setStarred(new ContentValues() ,true);

            // verify
            assertThat(actual.getAsInteger(Contacts.STARRED), is(1));
        }

        @Test
        public void setStarredFalse() throws Exception {
            // exercise
            ContentValues actual = Contact.setStarred(new ContentValues() ,false);

            // verify
            assertThat(actual.getAsInteger(Contacts.STARRED), is(0));
        }

        @Test(expected = NullPointerException.class)
        public void setStarredArgNull() throws Exception {
            // exercise
            Contact.setStarred(null, true);
        }
    }

    public static class PhoneTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            String[] projection = new String[] {
                    CommonDataKinds.Phone._ID,
                    CommonDataKinds.Phone.LOOKUP_KEY,
                    CommonDataKinds.Phone.NUMBER,
                    CommonDataKinds.Phone.TYPE
            };
            MatrixCursor cursor = new MatrixCursor(projection);
            cursor.addRow(new Object[] { 1, "lookup1", "03-1234-5678", TYPE_HOME });
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() throws Exception {
            // exercise
            long actual = Phone.getId(mCursor);

            // verify
            assertThat(actual,is(1L));
        }

        @Test(expected = NullPointerException.class)
        public void getIdArgNull() throws Exception {
            // exercise
            Phone.getId(null);
        }

        @Test
        public void getLookupKey() throws Exception {
            // exercise
            String actual = Phone.getLookupKey(mCursor);

            // verify
            assertThat(actual,is("lookup1"));
        }

        @Test(expected = NullPointerException.class)
        public void getLookupKeyArgNull() throws Exception {
            // exercise
            Phone.getLookupKey(null);
        }

        @Test
        public void getNumber() throws Exception {
            // exercise
            String actual = Phone.getNumber(mCursor);

            // verify
            assertThat(actual,is("03-1234-5678"));
        }

        @Test(expected = NullPointerException.class)
        public void getNumberArgNull() throws Exception {
            // exercise
            Phone.getNumber(null);
        }

        @Test
        public void getNumberType() throws Exception {
            // exercise
            int actual = Phone.getNumberType(mCursor);

            // verify
            assertThat(actual, is(TYPE_HOME));
        }

        @Test(expected = NullPointerException.class)
        public void getNumberTypeArgNull() throws Exception {
            // exercise
            Phone.getNumberType(null);
        }
    }

    public static class CallTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            String[] projection = new String[] {
                    CallLog.Calls._ID,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.CACHED_NUMBER_TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.TYPE,
            };
            MatrixCursor cursor = new MatrixCursor(projection);
            DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            dtf.setTimeZone(TimeZone.getTimeZone("UTC"));
            cursor.addRow(new Object[] { 1, "03-1234-5678", "name1", TYPE_HOME, dtf.parse("2017-01-01T12:34:56.789Z").getTime(), INCOMING_TYPE });
            cursor.addRow(new Object[] { 2, "134567890", "name2", null, dtf.parse("1999-12-31T23:59:59.999Z").getTime(), OUTGOING_TYPE });
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() throws Exception {
            // exercise
            long actual = Call.getId(mCursor);

            // verify
            assertThat(actual, is(1L));
        }

        @Test(expected = NullPointerException.class)
        public void getIdArgNull() throws Exception {
            // exercise
            Call.getId(null);
        }

        @Test
        public void getNumber() throws Exception {
            // exercise
            String actual = Call.getNumber(mCursor);

            // verify
            assertThat(actual, is("03-1234-5678"));
        }

        @Test(expected = NullPointerException.class)
        public void getNumberArgNull() throws Exception {
            // exercise
            String displayName = Call.getNumber(null);
        }

        @Test
        public void getDisplayName() throws Exception {
            // exercise
            String actual = Call.getDisplayName(mCursor);

            // verify
            assertThat(actual, is("name1"));
        }

        @Test(expected = NullPointerException.class)
        public void getDisplayNameArgNull() throws Exception {
            // exercise
            Call.getDisplayName(null);
        }

        @Test
        public void getNumberType() throws Exception {
            // exercise
            Integer actual = Call.getNumberType(mCursor);

            // verify
            assertThat(actual, is(TYPE_HOME));
        }

        @Test
        public void getNumberTypeNull() throws Exception {
            // exercise
            mCursor.moveToNext();
            Integer actual = Call.getNumberType(mCursor);

            // verify
            assertThat(actual, is(nullValue()));
        }

        @Test(expected = NullPointerException.class)
        public void getNumberTypeArgNull() throws Exception {
            // exercise
            Call.getNumberType(null);
        }

        @Test
        public void getDate() throws Exception {
            // exercise
            Date actual = Call.getDate(mCursor);

            // verify
            DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            dtf.setTimeZone(TimeZone.getTimeZone("UTC"));
            assertThat(actual, is(dtf.parse("2017-01-01T12:34:56.789Z")));
        }

        @Test(expected = NullPointerException.class)
        public void getDateArgNull() throws Exception {
            // exercise
            Call.getDate(null);
        }

        @Test
        public void getType() throws Exception {
            // exercise
            int actual = Call.getType(mCursor);

            // verify
            assertThat(actual, is(INCOMING_TYPE));
        }

        @Test(expected = NullPointerException.class)
        public void getTypeArgNull() throws Exception {
            // exercise
            Call.getType(null);
        }
    }
}