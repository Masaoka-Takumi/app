package jp.pioneer.carsync.domain.content;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * ソート順.
 * <p>
 * {@link ContentResolver#query(Uri, String[], String, String[], String)}のsortOrderに使用する
 * 文字列を構築するのが少し便利になるユーティリティクラス。
 */
public class SortOrder {
    /**
     * 順番.
     */
    public enum Order {
        /** 昇順. */
        ASC,
        /** 降順. */
        DESC
    }

    /**
     * 照合順序.
     * <p>
     * {@link SQLiteDatabase}の説明にあるように、AndroidではSQLiteを拡張して
     * <code>LOCALIZED</code>と<code>UNICODE</code>を照合順序に指定可能となっている。<br>
     * <code>UNICODE</code>はDUCETに基づいた照合が行われそうな記載に見えるが、
     * 実装が間違っているのでそうはならない。Android初期のバージョンからそうなっているので、
     * 互換性のためきっと修正されないと思われる。<br>
     * 従って、{@link Collate#UNICODE}の使用は推奨しない。
     */
    public enum Collate {
        /**
         * システムロケールに調整された照合順序.
         * <p>
         * {@link SQLiteDatabase}に具体的な説明がないので、ソースコードを確認したところ、
         * 強さ属性「PRIMARY」で照合を行う模様。
         */
        LOCALIZED,
        /**
         * （なんだか良く分からない）Unicode照合順序.
         * <p>
         * {@link SQLiteDatabase}に具体的な説明がないので、ソースコードを確認したところ、
         * デフォルトロケールで強さ属性「TERTIARY」で照合を行う模様。<br>
         * デフォルトロケールが何になるかは不明。エミュレータで確認したところ、Android4.4とAndroid7.0で
         * 異なる結果となった。
         */
        UNICODE
    }

    private List<Pair<String, String>> mOrders = new ArrayList<>();

    /**
     * コンストラクタ.
     */
    public SortOrder() {
    }

    /**
     * コンストラクタ.
     * <p>
     * 1個しか指定しない用向け。
     *
     * @param column 列名
     * @param order 順番
     * @see #add(String, Order)
     */
    public SortOrder(@NonNull String column, @NonNull Order order) {
        add(column, order);
    }

    /**
     * コンストラクタ.
     * <p>
     * 1個しか指定しない用向け。
     *
     * @param column 列名
     * @param collate 照合順序
     * @param order 順番
     * @see #add(String, Order)
     */
    public SortOrder(@NonNull String column, @NonNull Collate collate, @NonNull Order order) {
        add(column, collate, order);
    }

    /**
     * ソート指定追加.
     *
     * @param column 列名
     * @param order 順番
     * @return 本オブジェクト
     */
    public SortOrder add(@NonNull String column, @NonNull Order order) {
        mOrders.add(new Pair<>(column, order.name()));
        return this;
    }

    /**
     * ソート指定追加.
     *
     * @param column 列名
     * @param collate 照合順序
     * @param order 順番
     * @return 本オブジェクト
     */
    public SortOrder add(@NonNull String column, @NonNull Collate collate, @NonNull Order order) {
        mOrders.add(new Pair<>(column, "COLLATE " + collate.name() + " " + order.name()));
        return this;
    }

    /**
     * クエリー文字列化.
     *
     * @return {@link ContentResolver#query(Uri, String[], String, String[], String)}のsortOrderに使用可能な文字列。
     *          1個も指定していない場合、nullとなる。
     */
    @Nullable
    public String toQuery() {
        if (mOrders.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Pair<String, String> pair : mOrders) {
            if (!isFirst) {
                sb.append(",");
            }

            isFirst = false;
            sb.append(pair.first).append(" ").append(pair.second);
        }

        return sb.toString();
    }
}
