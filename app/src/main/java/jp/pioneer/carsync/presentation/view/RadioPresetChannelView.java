package jp.pioneer.carsync.presentation.view;

import android.util.SparseArray;

/**
 * ラジオプリセット選択画面のinterface
 */

public interface RadioPresetChannelView {

    /**
     * プリセットのバンド表示
     *
     * @param band バンド名
     */
    void setPresetBand(String band);

    /**
     * プリセットの番組名表示
     *
     * @param titles 番組名
     */
    void setPresetTitles(SparseArray titles);
}
