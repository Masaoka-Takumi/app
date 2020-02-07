package jp.pioneer.carsync.presentation.view;

import android.content.Intent;

/**
 * Information画面の抽象クラス
 */

public interface InformationView {

    /**
     * デバイス情報設定
     * @param deviceInformation デバイス情報
     */
    void setDeviceInformation(String deviceInformation);

    /**
     * デバイスバージョン設定
     * @param isVisible 表示/非表示
     * @param deviceFarmVersion デバイスバージョン
     */
    void setDeviceFarmVersion(boolean isVisible, String deviceFarmVersion);

    /**
     * アプリバージョン設定
     * @param appVersion アプリバージョン
     */
    void setAppVersion(String appVersion);

    /**
     * ブラウザー起動
     * @param intent インテント
     */
    void startBrowser(Intent intent);
}
