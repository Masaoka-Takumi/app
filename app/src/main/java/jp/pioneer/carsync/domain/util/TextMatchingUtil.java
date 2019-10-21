package jp.pioneer.carsync.domain.util;

import android.support.annotation.NonNull;

/**
 * テキストマッチングユーティリティ.
 */
public class TextMatchingUtil {
    /**
     * 一致するか否か取得.
     * <p>
     * テキスト情報で送信される車載機ステータス情報の文字列マッチング処理で使用する。
     *
     * @param subject 評価対象の文字列
     * @param pattern 文字列パターン
     * @return {@code true}:一致する。{@code false}:それ以外。
     */
    public static boolean equals(String subject, @NonNull String pattern) {
        if (subject == null) {
            return false;
        }

        return subject.trim().equalsIgnoreCase(pattern);
    }
}
