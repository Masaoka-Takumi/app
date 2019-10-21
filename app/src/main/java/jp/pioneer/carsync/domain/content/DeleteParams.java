package jp.pioneer.carsync.domain.content;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Arrays;

/**
 * 削除パラメータ.
 * <p>
 * {@link android.content.ContentResolver#delete(Uri, String, String[])}で指定するパラメータをまとめたもの.
 * */
public class DeleteParams {
    /** content:// スキームのURI. */
    public final Uri uri;
    /** 戻る行のフィルタ宣言. */
    public final String where;
    /** where中の?を置き換える引数. */
    public final String[] selectionArgs;

    /**
     * コンストラクタ.
     *
     * @param uri content:// スキームのURI
     * @param where 戻る行のフィルタ宣言
     * @param selectionArgs where中の?を置き換える引数
     */
    DeleteParams(@NonNull Uri uri, @Nullable String where, @Nullable String[] selectionArgs) {
        this.uri = uri;
        this.where = where;
        this.selectionArgs = selectionArgs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("uri", uri)
                .add("values", where)
                .add("selectionArgs", (selectionArgs == null) ? "null" : Joiner.on(',').join(selectionArgs))
                .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        DeleteParams other = (DeleteParams) obj;
        return Objects.equal(uri, other.uri)
                && Objects.equal(where, other.where)
                && Arrays.equals(selectionArgs, other.selectionArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(uri, where, selectionArgs);
    }
}
