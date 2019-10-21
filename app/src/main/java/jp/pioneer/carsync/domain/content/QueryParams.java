package jp.pioneer.carsync.domain.content;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.SectionIndexer;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Arrays;

/**
 * クエリーパラメータ.
 * <p>
 * {@link ContentResolver#query(Uri, String[], String, String[], String)}で指定するパラメータと、
 * {@link SectionIndexer}のセクションに使用するカラム指定をまとめたもの。
 */
public class QueryParams {
    /** content:// スキームのURI. */
    public final Uri uri;
    /** 戻る列のリスト. */
    public final String[] projection;
    /** 戻る行のフィルタ宣言. */
    public final String selection;
    /** selection中の?を置き換える引数. */
    public final String[] selectionArgs;
    /** 戻る行の順番. */
    public final String sortOrder;
    /** */
    public final String indexColumn;

    /**
     * コンストラクタ.
     *
     * @param uri content:// スキームのURI
     * @param projection 戻る列のリスト
     * @param selection 戻る行のフィルタ宣言
     * @param selectionArgs selection中の?を置き換える引数
     * @param sortOrder 戻る行の順番
     * @param indexColumn {@link SectionIndexer}のセクションに使用するカラム
     * @see ContentResolver#query(Uri, String[], String, String[], String)
     */
    QueryParams(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                @Nullable String[] selectionArgs, @Nullable String sortOrder, @Nullable String indexColumn) {
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
        this.indexColumn = indexColumn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("uri", uri)
                .add("projection", (projection == null) ? "null" : Joiner.on(',').join(projection))
                .add("selection", selection)
                .add("selectionArgs", (selectionArgs == null) ? "null" : Joiner.on(',').join(selectionArgs))
                .add("sortOrder", sortOrder)
                .add("indexColumn", indexColumn)
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

        QueryParams other = (QueryParams) obj;
        return Objects.equal(uri, other.uri)
                && Arrays.equals(projection, other.projection)
                && Objects.equal(selection, other.selection)
                && Arrays.equals(selectionArgs, other.selectionArgs)
                && Objects.equal(sortOrder, other.sortOrder)
                && Objects.equal(indexColumn, other.indexColumn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(uri, projection, selection, selectionArgs, sortOrder, indexColumn);
    }
}
