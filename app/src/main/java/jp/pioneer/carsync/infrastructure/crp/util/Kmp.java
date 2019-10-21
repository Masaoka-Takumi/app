package jp.pioneer.carsync.infrastructure.crp.util;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * KMP法（Knuth–Morris–Pratt algorithm）.
 * <p>
 * 文字列検索アルゴリズム。
 */
public class Kmp {
    /**
     * patternがdata内で最初に出現するインデックス取得.
     *
     * @param data 検索対象データ
     * @param pattern 検索パターン
     * @return 最初に出現するインデックス。見つからない場合-1。
     * @throws NullPointerException {@code data}、または、{@code pattern}がnull
     * @throws IllegalArgumentException {@code pattern}の要素数が1以上ではない
     */
    public static int indexOf(@NonNull byte[] data, @NonNull @Size(min = 1) byte[] pattern) {
        return indexOf(data, 0, data.length, pattern);
    }

    /**
     * patternがdata内で最初に出現するインデックス取得.
     *
     * @param data 検索対象データ
     * @param start 検索開始位置
     * @param stop 検索終了位置
     * @param pattern 検索パターン
     * @return 最初に出現するインデックス。見つからない場合-1。
     * @throws NullPointerException {@code data}、または、{@code pattern}がnull
     * @throws IllegalArgumentException {@code pattern}の要素数が1以上ではない
     */
    public static int indexOf(@NonNull byte[] data, int start, int stop, @NonNull @Size(min = 1) byte[] pattern) {
        checkNotNull(data);
        checkArgument(checkNotNull(pattern).length >= 1);


        int[] failure = computeFailure(pattern);
        int j = 0;
        for(int i = start; i < stop; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }

            if (pattern[j] == data[i]) {
                j++;
            }

            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }

            if (pattern[j] == pattern[i]) {
                j++;
            }

            failure[i] = j;
        }

        return failure;
    }
}
