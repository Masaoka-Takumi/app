package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.presentation.model.TipsContentsEndpoint;

/**
 * デバッグ設定の抽象クラス
 */

public interface DebugSettingView {
    /**
     * ログ出力有効無効の設定.
     *
     * @param isEnabled {@code true}:設定有効 {@code false}:設定無効
     */
    void setLogEnabled(boolean isEnabled);

    /**
     * 衝突検知デバッグモード有効無効の設定.
     *
     * @param isEnabled {@code true}:設定有効 {@code false}:設定無効
     */
    void setImpactDetectionDebugModeEnabled(boolean isEnabled);

    /**
     * Special EQ デバッグモード有効無効の設定.
     *
     * @param isEnabled {@code true}:設定有効 {@code false}:設定無効
     */
    void setSpecialEqDebugModeEnabled(boolean isEnabled);

    /**
     * Tipsサーバー設定.
     *
     * @param endpoint Tipsサーバーのエンドポイント
     */
    void setTipsServer(TipsContentsEndpoint endpoint);

    /**
     * バージョン1.1機能有効無効の設定.
     *
     * @param isEnabled {@code true}:設定有効 {@code false}:設定無効
     */
    void setVersion1_1FunctionEnabled(boolean isEnabled);

    /**
     * Adas SIM判定有無の設定.
     *
     * @param off {@code true}:無 {@code false}:有
     */
    void setAdasSimJudgement(boolean off);

    /**
     * Adas購入状態の設定.
     *
     * @param purchased {@code true}:購入済 {@code false}:未購入
     */
    void setAdasPurchased(boolean purchased);

    /**
     * Adas初期設定状態の設定.
     *
     * @param configured {@code true}:設定済 {@code false}:未設定
     */
    void setAdasConfigured(boolean configured);

    /**
     * Adas用擬似連携設定.
     *
     * @param enabled {@code true}:有効 {@code false}:無効
     */
    void setAdasPseudoCooperation(boolean enabled);

    void setVRDelayTime(int value);

    /**
     * 方位・走行状態取得.
     *
     * @param enabled {@code true}:有効 {@code false}:無効
     */
    void setDebugRunningStatus(boolean enabled);
    void setHomeCenterView(boolean value);
    /**
     * Adas車速の設定.
     *
     *  @param speed 車速
     */
    void setAdasCarSpeed(int speed);

    void setAdasLdwMinSpeed(int value);
    void setAdasLdwMaxSpeed(int value);
    void setAdasPcwMinSpeed(int value);
    void setAdasPcwMaxSpeed(int value);
    void setAdasFcwMinSpeed(int value);
    void setAdasFcwMaxSpeed(int value);
    void setAdasAccelerateY(float value);
    void setAdasAccelerateZMin(float value);
    void setAdasAccelerateZMax(float value);
    void setAdasFps(int value);
    void setAdasCameraPreview(boolean enabled);
    void setAlexaSimJudgement(boolean value);
    void recheckSim();
    void setSmartPhoneControlComand(boolean value);
}
