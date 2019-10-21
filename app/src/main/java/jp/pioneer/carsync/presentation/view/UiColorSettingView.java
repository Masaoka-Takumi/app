package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import java.util.List;

import jp.pioneer.carsync.presentation.model.UiColor;

/**
 * UIカラー設定画面のinterface
 */

public interface UiColorSettingView {
    /**
     * 設定可能カラーの設定
     *
     * @param colors 設定可能カラー
     */
    void setColor(List<UiColor> colors);

    /**
     * 選択カラーの設定
     *
     * @param position 選択位置
     * @param color 設定色
     */
    void setPosition(int position, @ColorRes int color);

    /**
     * テーマの設定
     *
     * @param theme テーマ
     */
    void setTheme(int theme);
}
