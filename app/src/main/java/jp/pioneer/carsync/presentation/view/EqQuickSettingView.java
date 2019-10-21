package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

/**
 * EQ Quick Settingの抽象クラス
 */

public interface EqQuickSettingView {

    /**
     * Band値のSplineデータへの反映
     * @param bands 31Bandの配列
     */
    void setBandData(float[] bands);

    /**
     * Lowの設定値表示
     * @param value 設定値
     */
    void setLowValueText(int value);

    /**
     * Midの設定値表示
     * @param value 設定値
     */
    void setMidValueText(int value);

    /**
     * Hiの設定値表示
     * @param value 設定値
     */
    void setHiValueText(int value);

    /**
     * UIColorの設定
     * @param color 設定色
     */
    void setColor(@ColorRes int color);

    /**
     * 設定無効状態の設定
     *
     * @param isEnabled 有効/無効
     */
    void setEnable(boolean isEnabled);
}
