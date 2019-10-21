package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.ListeningPosition;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;

/**
 * mallCarTa設定画面の抽象クラス
 */

public interface SmallCarTaSettingView {
    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    void setAdapter(ArrayList<SmallCarTaSettingType> types);

    /**
     * 選択中TA設定
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
     * シートタイプの設定有効/無効
     * @param isEnabled 設定有効/無効
     */
    void setSeatTypeSettingEnabled(boolean isEnabled);

    /**
     * シートタイプの設定
     * @param position シートのポジション
     */
    void setSeatType(ListeningPosition position);

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
