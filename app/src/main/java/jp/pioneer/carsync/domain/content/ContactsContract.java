package jp.pioneer.carsync.domain.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import java.util.Date;

import jp.pioneer.carsync.domain.content.SortOrder.Order;
import jp.pioneer.carsync.domain.util.QueryUtil;

import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.provider.CallLog.Calls.INCOMING_TYPE;
import static android.provider.CallLog.Calls.MISSED_TYPE;
import static android.provider.CallLog.Calls.OUTGOING_TYPE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 連絡先コントラクト.
 */
public class ContactsContract {

    /**
     * {@link QueryParams} のビルダー.
     */
    public static class QueryParamsBuilder {

        /**
         * 連絡先情報を取得する {@link QueryParams} 生成.
         *
         * @return 連絡先情報を取得するクエリーパラメータ
         * @see Contact
         */
        @NonNull
        public static QueryParams createContacts() {
            //Uri uri = Contacts.CONTENT_URI;
                    //.buildUpon()
                    //.appendQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true")
                    //         .build();
            return new QueryParams(
                    Contacts.CONTENT_URI,
                    Contact.PROJECTION,
                    "((" + Contacts.DISPLAY_NAME + " NOTNULL)" +
                            " AND (" + Contacts.DISPLAY_NAME + " != '')" +
                            " AND (" + Contacts.LOOKUP_KEY + " NOTNULL)" +
                            " AND (" + Contacts.LOOKUP_KEY + " != '')" +
                            " AND (" + Contacts.HAS_PHONE_NUMBER + " = 1))",
                    null,
                    Contact.SORT_ORDER,
                    Contacts.DISPLAY_NAME
            );
        }

        /**
         * LookUpKeyから連絡先情報を取得する {@link QueryParams} 生成.
         *
         * @param lookupKey LookUpKey
         * @return LookUpKeyから連絡先情報を取得するクエリ―パラメータ
         * @throws NullPointerException {@code lookupKey}がnull
         * @throws IllegalArgumentException {@code lookupKey}が空文字
         * @see Contact
         */
        @NonNull
        public static QueryParams createContact(@NonNull @Size(min = 1) String lookupKey) {
            checkNotNull(lookupKey);
            checkArgument(lookupKey.length() >= 1);

            return new QueryParams(
                    Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey),
                    Contact.PROJECTION,
                    "((" + Contacts.DISPLAY_NAME + " NOTNULL)" +
                            " AND (" + Contacts.DISPLAY_NAME + " != '')" +
                            " AND (" + Contacts.LOOKUP_KEY + " NOTNULL)" +
                            " AND (" + Contacts.LOOKUP_KEY + " != '')" +
                            " AND (" + Contacts.HAS_PHONE_NUMBER + " = 1))",
                    null,
                    null,
                    null
            );
        }

        /**
         * 部分一致検索によって連絡先情報を取得する {@link QueryParams} 生成.
         *
         * @param keywords 部分一致検索キーワード群
         * @return 部分一致検索によって連絡先情報を取得するクエリパラメータ
         * @throws NullPointerException {@code keywords}がnull
         * @throws IllegalArgumentException {@code keywords}の要素数が0
         * @see Contact
         */
        @NonNull
        public static QueryParams createContactsByKeywords(@NonNull @Size(min = 1) String[] keywords) {
            checkNotNull(keywords);
            checkArgument(keywords.length >= 1);

            return new QueryParams(
                    Contacts.CONTENT_URI,
                    Contact.PROJECTION,
                    "(" + QueryUtil.makeLikeSelection(Contacts.DISPLAY_NAME, keywords.length) +
                            " AND (" + Contacts.LOOKUP_KEY + " NOTNULL)" +
                            " AND (" + Contacts.LOOKUP_KEY + " != '')" +
                            " AND (" + Contacts.HAS_PHONE_NUMBER + " = 1))",
                    QueryUtil.makeLikeSelectionArgs(keywords),
                    Contact.SORT_ORDER,
                    null
            );
        }

        /**
         * お気に入りの連絡先情報を取得する {@link QueryParams} 生成.
         *
         * @return お気に入りの連絡先情報を取得するクエリーパラメータ
         * @see Contact
         */
        @NonNull
        public static QueryParams createFavoriteContacts() {
            return new QueryParams(
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
                    Contacts.DISPLAY_NAME
            );
        }

        /**
         * 連絡先IDに該当する電話番号情報を取得する {@link QueryParams} 生成.
         *
         * @param contactId 連絡先ID
         * @return ID検索によって電話番号情報を取得するクエリーパラメータ
         * @see Contact#getId(Cursor)
         * @see Phone
         */
        @NonNull
        public static QueryParams createPhones(long contactId) {
            return new QueryParams(
                    CommonDataKinds.Phone.CONTENT_URI,
                    Phone.PROJECTION,
                    "((" + CommonDataKinds.Phone.CONTACT_ID + " = ?)" +
                            " AND (" + CommonDataKinds.Phone.NUMBER + " NOTNULL)" +
                            " AND (" + CommonDataKinds.Phone.NUMBER + " != ''))",
                    new String[] { String.valueOf(contactId) },
                    Phone.SORT_ORDER,
                    null
            );
        }

        /**
         * 電話番号IDに該当する電話番号情報を取得する {@link QueryParams} 生成.
         *
         * @param phoneId 電話番号ID
         * @return 電話番号IDによって電話番号情報を取得するクエリーパラメータ
         * @see Phone#getId(Cursor)
         * @see Phone
         */
        @NonNull
        public static QueryParams createPhone(long phoneId) {
            return new QueryParams(
                    CommonDataKinds.Phone.CONTENT_URI,
                    Phone.PROJECTION,
                    "((" + CommonDataKinds.Phone._ID + " = ?)" +
                            " AND (" + CommonDataKinds.Phone.NUMBER + " NOTNULL)" +
                            " AND (" + CommonDataKinds.Phone.NUMBER + " != ''))",
                    new String[] { String.valueOf(phoneId) },
                    null,
                    null
            );
        }

        /**
         * 電話番号に該当する電話番号情報を取得する {@link QueryParams} 生成.
         *
         * @param phoneNumber 電話番号
         * @return 電話番号によって電話番号情報を取得するクエリーパラメータ
         * @see Phone#getId(Cursor)
         * @see Phone
         */
        @NonNull
        public static QueryParams createPhone(String phoneNumber) {
            return new QueryParams(
                    CommonDataKinds.Phone.CONTENT_URI,
                    Phone.PROJECTION,
                    "((" + CommonDataKinds.Phone.NUMBER + " = ?)" +
                            " AND (" + CommonDataKinds.Phone.NUMBER + " NOTNULL)" +
                            " AND (" + CommonDataKinds.Phone.NUMBER + " != ''))",
                    new String[] { phoneNumber },
                    null,
                    null
            );
        }

        /**
         * 通話履歴情報を取得する {@link QueryParams} 生成.
         *
         * @return 通話履歴情報を取得するクエリーパラメータ
         * @see Call
         */
        @NonNull
        public static QueryParams createCalls() {
            return new QueryParams(
                    CallLog.Calls.CONTENT_URI,
                    Call.PROJECTION,
                    CallLog.Calls.TYPE + " IN (" + INCOMING_TYPE + "," + OUTGOING_TYPE + "," + MISSED_TYPE + ")",
                    null,
                    Call.SORT_ORDER,
                    null
            );
        }
    }

    /**
     * {@link UpdateParams} のビルダー.
     */
    public static class UpdateParamsBuilder {

        /**
         * 該当のLookUpKeyの連絡先情報を更新する {@link UpdateParams} 生成.
         *
         * @param lookupKey LookUpKey
         * @param values 更新内容
         * @return 該当のLookUpKeyの連絡先情報を更新する更新パラメータ
         * @throws NullPointerException {@code lookupKey}がnull
         * @throws IllegalArgumentException {@code lookupKey}の要素数が0
         */
        @NonNull
        public static UpdateParams createContact(@NonNull @Size(min = 1) String lookupKey, ContentValues values) {
            checkNotNull(lookupKey);
            checkArgument(lookupKey.length() >= 1);

            return new UpdateParams(
                    Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey),
                    values,
                    null,
                    null
            );
        }
    }

    /**
     * 連絡先情報.
     */
    public static class Contact {
        static final String[] PROJECTION = new String[] {
                Contacts._ID,
                Contacts.LOOKUP_KEY,
                Contacts.PHOTO_URI,
                Contacts.DISPLAY_NAME,
                Contacts.STARRED
        };

        static final String SORT_ORDER = new SortOrder(Contacts.DISPLAY_NAME, SortOrder.Collate.LOCALIZED, Order.ASC).toQuery();
        static final String INDEX_COLUMN = Contacts.DISPLAY_NAME;
        /**
         * 連絡先情報 {@code cursor} からIDを取得する.
         *
         * @param cursor 連絡先情報
         * @return 連絡先ID。連絡先を統合するとIDが変わるため、永続データに使用しないこと。
         *          永続データには{@link #getLookupKey(Cursor)}を使用すること。
         * @throws NullPointerException {@code cursor}がnull
         */
        public static long getId(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getLong(cursor.getColumnIndexOrThrow(Contacts._ID));
        }

        /**
         * 連絡先情報 {@code cursor} からLookUpKeyを取得する.
         * <p>
         * 個人に紐づくキーとして使用出来る。
         * 永続データには、{@link #getId(Cursor)}ではなく本メソッドの値を使用すること。
         *
         * @param cursor 連絡先情報
         * @return LookUpKey
         * @throws NullPointerException {@code cursor}がnull
         */
        @NonNull
        public static String getLookupKey(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(Contacts.LOOKUP_KEY));
        }

        /**
         * 連絡先情報 {@code cursor} からPhotoUriを取得する.
         *
         * @param cursor 連絡先情報
         * @return PhotoUri
         * @throws NullPointerException {@code cursor}がnull
         */
        @Nullable
        public static Uri getPhotoUri(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            String uriString = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.PHOTO_URI));
            return (uriString != null) ? Uri.parse(uriString) : null;
        }

        /**
         * 連絡先情報 {@code cursor} から名前を取得する.
         *
         * @param cursor 連絡先情報
         * @return 名前
         * @throws NullPointerException {@code cursor}がnull
         */
        @NonNull
        public static String getDisplayName(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
        }

        /**
         * 連絡先情報 {@code cursor} からお気に入りかどうかを取得する.
         *
         * @param cursor 連絡先情報
         * @return {@code true}:お気に入りである. {@code false}:お気に入りではない.
         * @throws NullPointerException {@code cursor}がnull
         */
        public static boolean isStarred(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return (cursor.getInt(cursor.getColumnIndex(Contacts.STARRED)) == 1);
        }

        /**
         * お気に入り情報設定.
         *
         * @param values 更新内容
         * @param isStarred {@code true}:お気に入りに設定する. {@code false}:お気に入りの設定を解除する.
         * @return お気に入り情報設定後の {@link ContentValues}
         * @throws NullPointerException {@code cursor}がnull
         */
        public static ContentValues setStarred(@NonNull ContentValues values, boolean isStarred) {
            checkNotNull(values);

            values.put(Contacts.STARRED, isStarred ? 1 : 0);
            return values;
        }
    }

    /**
     * 電話番号情報.
     */
    public static class Phone {
        static final String[] PROJECTION = new String[] {
                CommonDataKinds.Phone._ID,
                CommonDataKinds.Phone.LOOKUP_KEY,
                CommonDataKinds.Phone.NUMBER,
                CommonDataKinds.Phone.TYPE
        };

        static final String SORT_ORDER = new SortOrder(CommonDataKinds.Phone.RAW_CONTACT_ID, Order.ASC).toQuery();
        /**
         * 電話番号情報 {@code cursor} からIDを取得する.
         * <p>
         * 永続データとして保存する場合、本IDを保存すること。
         *
         * @param cursor 電話番号情報
         * @return 電話ID
         * @throws NullPointerException {@code cursor}がnull
         */
        public static long getId(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getLong(cursor.getColumnIndexOrThrow(CommonDataKinds.Phone._ID));
        }

        /**
         * 電話番号情報 {@code cursor} から連絡先のLookUpKeyを取得する.
         *
         * @param cursor 電話番号情報
         * @return 連絡先のLookUpKey
         * @throws NullPointerException {@code cursor}がnull
         */
        @NonNull
        public static String getLookupKey(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(CommonDataKinds.Phone.LOOKUP_KEY));
        }

        /**
         * 電話番号情報 {@code cursor} から電話番号を取得する.
         *
         * @param cursor 電話番号情報
         * @return 電話番号
         * @throws NullPointerException {@code cursor}がnull
         */
        @NonNull
        public static String getNumber(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(CommonDataKinds.Phone.NUMBER));
        }

        /**
         * 電話番号情報 {@code cursor} から電話の種別を取得する.
         *
         * @param cursor 電話番号情報
         * @return 電話の種別
         * @throws NullPointerException {@code cursor}がnull
         */
        public static int getNumberType(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getInt(cursor.getColumnIndexOrThrow(CommonDataKinds.Phone.TYPE));
        }
    }

    /**
     * 通話履歴情報.
     */
    public static class Call {
        static final String[] PROJECTION = new String[] {
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.CACHED_NUMBER_TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.TYPE,
        };

        static final String SORT_ORDER = new SortOrder(CallLog.Calls.DATE, Order.DESC).toQuery();
        /**
         * 通話履歴情報 {@code cursor} からIDを取得する.
         *
         * @param cursor 通話履歴情報
         * @return 通話履歴ID
         * @throws NullPointerException {@code cursor}がnull
         */
        public static long getId(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls._ID));
        }

        /**
         * 通話履歴情報 {@code cursor} から電話番号を取得する.
         *
         * @param cursor 通話履歴情報
         * @return 電話番号
         * @throws NullPointerException {@code cursor}がnull
         */
        @NonNull
        public static String getNumber(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
        }

        /**
         * 通話履歴情報 {@code cursor} から名前を取得する.
         *
         * @param cursor 通話履歴情報
         * @return 名前
         * @throws NullPointerException {@code cursor}がnull
         */
        @Nullable
        public static String getDisplayName(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
        }

        /**
         * 通話履歴情報 {@code cursor} から発着信の種別を取得する.
         *
         * @param cursor 通話履歴情報
         * @return 発着信の種別。取得出来ない場合はnull\。
         * @throws NullPointerException {@code cursor}がnull
         * @see android.provider.CallLog.Calls#CACHED_NUMBER_TYPE
         */
        @Nullable
        public static Integer getNumberType(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            int index = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NUMBER_TYPE);
            if (cursor.getType(index) == FIELD_TYPE_NULL) {
                return null;
            }

            return cursor.getInt(index);
        }

        /**
         * 通話履歴情報 {@code cursor} から日時を取得する.
         *
         * @param cursor 通話履歴情報
         * @return 日時
         * @throws NullPointerException {@code cursor}がnull
         */
        @NonNull
        public static Date getDate(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return new Date(cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)));
        }

        /**
         * 通話履歴情報 {@code cursor} から通話履歴の種別を取得する.
         *
         * @param cursor 通話履歴情報
         * @return 通話履歴の種別
         * @throws NullPointerException {@code cursor}がnull
         * @see android.provider.CallLog.Calls#TYPE
         */
        public static int getType(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
        }
    }
}
