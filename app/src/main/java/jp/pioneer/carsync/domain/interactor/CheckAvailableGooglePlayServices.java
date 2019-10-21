package jp.pioneer.carsync.domain.interactor;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.GooglePlayServicesAvailabilityChecker;

/**
 * GooglePlay開発者サービスが利用可能かチェック.
 * <p>
 * 一部の機能はGooglePlay開発者サービスを利用して実装しているため、GooglePlay
 * 開発者サービスが利用可能か確認するために使用する。<br>
 * 開発者サービスが利用できないケースは以下。
 * <ul>
 *      <li>GooglePlay開発者サービスがインストールされていない</li>
 *      <li>GooglePlay開発者サービスのバージョンアップが必要</li>
 *      <li>GooglePlay開発者サービスが無効になっている</li>
 *      <li>GooglePlay開発者サービス更新中</li>
 *      <li>Playストアがインストールされていない</li>
 *      <li>GooglePlay開発者サービス、または、Playストアの署名が不正</li>
 * </ul>
 *
 * @see GetCurrentLocation
 */
public class CheckAvailableGooglePlayServices {
    @Inject GooglePlayServicesAvailabilityChecker mGooglePlayServicesAvailabilityChecker;

    /**
     * コンストラクタ.
     */
    @Inject
    public CheckAvailableGooglePlayServices() {
    }

    /**
     * 実行.
     *
     * @return チェック結果
     */
    public GooglePlayServicesAvailabilityChecker.Result execute() {
        return mGooglePlayServicesAvailabilityChecker.doCheck();
    }
}
