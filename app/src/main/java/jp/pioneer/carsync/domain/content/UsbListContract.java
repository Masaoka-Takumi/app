package jp.pioneer.carsync.domain.content;

import android.database.Cursor;
import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.UsbInfoType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * USBリストコントラクト.
 */
public class UsbListContract {
    /**
     * 行のユニークID.
     * <p>
     * <P>Type: long</P>
     */
    public static final String _ID = "_id";

    /**
     * リストインデックス.
     * <p>
     * <P>Type: int</P>
     */
    public static final String LIST_INDEX = "list_index";

    /**
     * 文字列.
     * <p>
     * <P>Type: String</P>
     */
    public static final String TEXT = "text";

    /**
     * 情報種別.
     * <p>
     * <P>Type: int</P>
     */
    public static final String TYPE = "type";

    /**
     * データが有効か.
     * <p>
     * <P>Type: int</P>
     */
    public static final String DATA_ENABLED = "enabled";

    /**
     * ID取得.
     *
     * @param cursor USBリストのCursor
     * @return ID
     * @throws NullPointerException {@code cursor}がnull
     */
    public static long getId(@NonNull Cursor cursor) {
        checkNotNull(cursor);

        return cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
    }

    /**
     * リストインデックス取得.
     *
     * @param cursor USBリストのCursor
     * @return リストインデックス
     * @throws NullPointerException {@code cursor}がnull
     */
    public static int getListIndex(@NonNull Cursor cursor) {
        checkNotNull(cursor);

        return cursor.getInt(cursor.getColumnIndexOrThrow(LIST_INDEX));
    }

    /**
     * 文字列取得.
     *
     * @param cursor USBリストのCursor
     * @return 文字列（{@link #TEXT}）
     * @throws NullPointerException {@code cursor}がnull
     */
    public static String getText(@NonNull Cursor cursor) {
        checkNotNull(cursor);

        return cursor.getString(cursor.getColumnIndexOrThrow(TEXT));
    }

    /**
     * 情報種別取得.
     * <p>
     * データが無効な場合は使用しないこと
     *
     * @param cursor USBリストのCursor
     * @return 情報種別
     * @throws NullPointerException {@code cursor}がnull
     */
    public static UsbInfoType getInfoType(@NonNull Cursor cursor) {
        checkNotNull(cursor);

        Integer usbInfoType = cursor.getInt(cursor.getColumnIndexOrThrow(TYPE));
        return UsbInfoType.valueOf(usbInfoType.byteValue());
    }

    /**
     * データが有効か否か取得.
     * <p>
     * 空のデータかどうかを判断するために使用する
     *
     * @param cursor USBリストのCursor
     * @return データが有効か否か
     * @throws NullPointerException {@code cursor}がnull
     */
    public static boolean getDataEnabled(@NonNull Cursor cursor) {
        checkNotNull(cursor);

        return cursor.getInt(cursor.getColumnIndexOrThrow(DATA_ENABLED)) == 1;
    }
}
