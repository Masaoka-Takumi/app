package jp.pioneer.carsync.domain.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Arrays;

/**
 * 更新パラメータ.
 * <p>
 * {@link ContentResolver#update(Uri, ContentValues, String, String[])}で指定するパラメータを
 * まとめたもの。
 */
public class UpdateParams {
    /** content:// スキームのURI. */
    public final Uri uri;
    /** 新しいフィールド値. */
    public final ContentValues values;
    /** 更新する行のフィルタ宣言. */
    public final String where;
    /** where?を置き換える引数. */
    public final String[] selectionArgs;

    /**
     * コンストラクタ.
     *
     * @param uri content:// スキームのURI
     * @param values 新しいフィールド値
     * @param where 更新する行のフィルタ宣言
     * @param selectionArgs where?を置き換える引数
     */
    UpdateParams(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String where, @Nullable String[] selectionArgs) {
        this.uri = uri;
        this.values = values;
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
                .add("values", values)
                .add("where", where)
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

        UpdateParams other = (UpdateParams) obj;
        return Objects.equal(uri, other.uri)
                && Objects.equal(values, other.values)
                && Objects.equal(where, other.where)
                && Arrays.equals(selectionArgs, other.selectionArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(uri, values,where,selectionArgs);
    }
}
