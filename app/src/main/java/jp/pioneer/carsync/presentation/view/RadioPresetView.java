package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import java.util.ArrayList;

import jp.pioneer.carsync.presentation.model.AbstractPresetItem;

/**
 * ラジオプリセットリスト画面のinterface
 */

public interface RadioPresetView {

    /**
     * プリセットリストの設定
     *
     * @param presetList プリセットリスト
     */
    void setPresetList(ArrayList<AbstractPresetItem> presetList);

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    void setColor(@ColorRes int color);

    /**
     * 選択中PCHの設定
     *
     * @param position 選択中ポジション
     */
    void setSelectedPosition(int position);

}
