package jp.pioneer.carsync.domain.component;

/**
 * GooglePlay開発者サービス利用可能チェッカー.
 */
public interface GooglePlayServicesAvailabilityChecker {
    /**
     * チェック実施.
     *
     * @return 実施結果
     */
    Result doCheck();

    /**
     * 実施結果.
     */
    interface Result {
        /**
         * 利用可能か否か取得.
         *
         * @return {@code true}:利用可能。{@code false}:利用不可。
         */
        boolean isAvailable();

        /**
         * 解決.
         * <p>
         * {@link #isAvailable()}が{@code false}の場合に使用する。
         * 利用可能するための解決策があれば通知領域に表示する。
         *
         * @return {@code true}:解決策がある（通知領域に表示）。{@code false}:解決策がない。
         */
        boolean resolve();
    }
}
