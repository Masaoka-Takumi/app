package jp.pioneer.carsync.domain.content;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;

/**
 * AppMusic用のCursorLoaderクラス.
 */
public abstract class AppMusicCursorLoader extends CursorLoader {
    public static final String SECTION_STRINGS = "section_strings";
    public static final String SECTION_INDEXES = "section_indexes";

    /**
     * コンストラクタ.
     *
     * @param context context
     * @param uri uri
     * @param projection projection
     * @param selection selection
     * @param selectionArgs selectionArg
     * @param sortOrder sortOrder
     */
    public AppMusicCursorLoader(Context context, Uri uri, String[] projection, String selection,
                                String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * コンストラクタ.
     *
     * @param context context
     */
    public AppMusicCursorLoader(Context context) {
        super(context);
    }

    /**
     * データ受け取り.
     *
     * @return {@link Bundle}
     */
    public abstract Bundle getExtras();
}
