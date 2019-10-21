package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.carsync.presentation.model.LiveSimulationItem;
import jp.pioneer.carsync.presentation.model.VisualEffectItem;

/**
 * LiveSimulation画面の抽象クラス
 */

public interface LiveSimulationSettingView {

    /**
     * LiveSimulationリストアダプター設定
     *
     * @param items    リストアイテム
     */
    void setLiveSimulationAdapter(ArrayList<LiveSimulationItem> items);

    /**
     * VisualEffect有効無効設定
     *
     * @param isEnabled    有効無効
     */
    void setVisualEffectEnabled(boolean isEnabled);

    /**
     * VisualEffectリストアダプター設定
     *
     * @param items    リストアイテム
     */
    void setVisualEffectAdapter(List<VisualEffectItem> items);

    /**
     * VisualEffect選択位置設定
     *
     * @param position 現在選択位置
     */
    void setVisualEffectSelectedIndex(int position);

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    void setColor(@ColorRes int color);

    /**
     * 選択位置の設定
     *
     * @param position  位置
     */
    void setNextItem(int position);

    /**
     * 選択位置（カーソル）の設定
     *
     * @param position  位置
     */
    void setCurrentPosition(int position);

    /**
     * 設定無効状態の設定
     *
     * @param isEnabled 有効/無効
     */
    void setEnable(boolean isEnabled);
}
