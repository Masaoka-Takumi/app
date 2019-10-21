package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import java.util.List;

import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;

/**
 * PresetEQ設定の抽象クラス
 */

public interface EqSettingView {
    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    void setAdapter(List<SoundFxSettingEqualizerType> types);

    /**
     * 選択中EQ設定
     *
     * @param selected 選択中インデックス
     */
    void setSelectedItem(int selected);

    /**
     * Presetイメージ設定
     *
     * @param resId リソース
     */
    void setPresetView(int resId);

    /**
     * CustomView設定
     *
     * @param bands Band配列
     */
    void setCustomView(float[] bands);

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
