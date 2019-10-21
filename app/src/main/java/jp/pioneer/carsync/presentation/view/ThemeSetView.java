package jp.pioneer.carsync.presentation.view;

import java.util.ArrayList;

import jp.pioneer.carsync.presentation.model.IlluminationColorModel;
import jp.pioneer.carsync.presentation.model.ThemeSelectItem;

/**
 * テーマ設定画面のinterface
 */

public interface ThemeSetView {

    /**
     * リストアイテムの設定
     *
     * @param items    リストアイテム
     */
    void setAdapter(ArrayList<ThemeSelectItem> items);

    /**
     * 選択位置の設定
     *
     * @param position  位置
     */
    void setCurrentItem(int position);
    /**
     * 選択中のカラー設定有効/無効
     *
     * @param enabled 有効/無効
     */
    void setDispColorSettingEnabled(boolean enabled);

    /**
     * 選択中のカラー設定有効/無効
     *
     * @param enabled  有効/無効
     */
    void setKeyColorSettingEnabled(boolean enabled);

    /**
     * 選択中のカラー設定(DISP)
     *
     * @param disp DISP
     */
    void setDispColor(IlluminationColorModel disp);

    /**
     * 選択中のカラー設定(KEY)
     *
     * @param key  KEY
     */
    void setKeyColor(IlluminationColorModel key);

    /**
     * UIカラー設定
     *
     * @param ui UIカラーID
     */
    void setUIColor(int ui);

    /**
     * テーマの設定
     *
     * @param theme テーマ
     */
    void setTheme(int theme);

    /**
     * カスタム表示の設定
     *
     * @param isCustom カスタム設定であるか否か
     */
    void setCustom(boolean isCustom);

    /**
     * 設定無効状態の設定
     *
     * @param isEnabled 有効/無効
     */
    void setEnable(boolean isEnabled);
}
