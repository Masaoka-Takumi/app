package jp.pioneer.carsync.domain.util;

import android.content.ContentResolver;
import android.net.Uri;

import java.util.ArrayList;

/**
 * クエリーユーティリティ.
 */
public class QueryUtil {
    private static final String LIKE_ESCAPER = "$";

    /**
     * likeを使用したselectionの生成.
     * <p>
     * {@link ContentResolver#query(Uri, String[], String, String[], String)}のselectionに
     * SQLのWHERE句（WHERE自身は除く）の文字列を指定するが、部分一致検索のためにlikeを使用する際に
     * 検索ワードを内の%をエスケープするために使用する。
     * selectionArgsは、{@link #makeLikeSelectionArgs(String[])}を使用する。
     *
     * @param column 列名
     * @param count 部分一致検索を行う数（検索ワード数）
     * @return selection用の文字列
     */
    public static String makeLikeSelection(String column, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i >= 1) {
                sb.append(" OR ");
            }
            sb.append(column).append(" like ? escape ?");
        }

        return sb.toString();
    }

    /**
     * likeを使用したselectionArgsの生成.
     * <p>
     * {@link #makeLikeSelection(String, int)}でselectionの文字列を作成した場合に使用する。
     *
     * @param values 検索ワード群
     * @return selectionArgs用の文字列
     */
    public static String[] makeLikeSelectionArgs(String[] values) {
        ArrayList<String> result = new ArrayList<>();
        for (String value : values) {
            result.add("%" + escapeForLike(value) + "%");
            result.add(QueryUtil.LIKE_ESCAPER);
        }

        return result.toArray(new String[0]);
    }

    /**
     * inを使用したselectionの生成.
     * <p>
     * {@link ContentResolver#query(Uri, String[], String, String[], String)}のselectionに
     * SQLのWHERE句（WHERE自身は除く）の文字列を指定するが、比較検索のためにinを使用する際に
     * 検索ワードを内の?をエスケープするために使用する。
     *
     * @param values 検索ワード群
     * @return selection用の文字列
     */
    public static String makeInSelection(String column, String[] values) {
        StringBuilder sb = new StringBuilder();
        sb.append(column).append(" IN(");
        for (int i = 0; i < values.length; i++) {
            sb.append(" ?");
            if(i != values.length -1) {
                sb.append(",");
            }
        }
        sb.append(")");

        return sb.toString();
    }

    private static String escapeForLike(String s) {
        return s.replaceAll("%", "\\" + LIKE_ESCAPER + "%").replaceAll("_", "\\" + LIKE_ESCAPER + "_");
    }
}
