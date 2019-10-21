package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

/**
 * Created by NSW00_906320 on 2017/07/20.
 */

public interface FaderBalanceSettingView {
    /**
     * ステータス更新
     */
    void onStatusUpdated();

    /**
     * UIColorの設定
     *
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
