package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

/**
 * EQ Pro Setting Zoomの抽象クラス
 */

public interface EqProSettingZoomView {
    /**
     * Band値のSplineデータへの反映
     * @param bands 31Bandの配列
     */
    void setBandData(float[] bands);

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
