package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.presentation.model.ImpactNotificationMethod;

/**
 * 衝突検知機能設定の抽象クラス
 */

public interface ImpactDetectionSettingsView {

    /**
     * 衝突検知機能無効端末設定
     */
    void invalidImpactDetection();

    /**
     * 衝突検知機能有効/無効設定
     * @param enabled 有効/無効
     */
    void setImpactDetectionEnabled(boolean enabled);

    /**
     * 衝突検知機能通知方法設定
     * @param method 通知方法
     */
    void setImpactNotificationMethod(ImpactNotificationMethod method);
    void checkPermission();
}
