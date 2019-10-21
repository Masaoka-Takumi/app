package jp.pioneer.carsync.presentation.view;

import java.util.List;

/**
 * イルミネーションカラー設定画面のinterface
 */

public interface IlluminationColorSettingView {

    /**
     * 設定可能カラーの設定
     *
     * @param colors 設定可能カラーID
     */
    void setColor(List<Integer> colors);

    /**
     * 選択中カラーの設定
     *
     * @param position 選択位置
     */
    void setPosition(int position);

    /**
     * カスタムカラーの設定
     *
     * @param red   赤要素
     * @param green 緑要素
     * @param blue  青要素
     */
    void setCustomColor(int red, int green, int blue);

    /**
     * 設定無効状態の設定
     *
     * @param isEnabled 有効/無効
     */
    void setEnable(boolean isEnabled);
}
