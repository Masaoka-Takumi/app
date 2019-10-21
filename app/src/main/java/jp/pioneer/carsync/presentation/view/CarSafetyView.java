package jp.pioneer.carsync.presentation.view;

/**
 * CarSafety設定抽象クラス
 */

public interface CarSafetyView {

    /**
     * パーキングセンサー設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     */
    void setParkingSensorSetting(boolean isSupported, boolean isEnabled);

    /**
     * 衝突検知設定.
     *
     * @param isSupported 対応しているか否か
     */
    void setImpactDetectionSetting(boolean isSupported);

    /**
     * ADAS設定.
     *
     * @param isSupported 対応しているか否か
     */
    void setAdasSetting(boolean isSupported);

    /**
     * ADAS未課金アイコン設定.
     *
     * @param isPurchased 課金しているか否か
     */
    void setPurchaseIcon(boolean isPurchased);
}
