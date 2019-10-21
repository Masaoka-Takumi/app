package jp.pioneer.carsync.presentation.view;

import java.util.ArrayList;

import jp.pioneer.carsync.presentation.model.CustomKeyItem;

/**
 * カスタムキー割当画面のinterface
 */
public interface CustomKeySettingDialogView {

    /**
     * アダプター設定
     *
     * @param customKeyItemArrayList タイプリスト
     */
    void setAdapter(ArrayList<CustomKeyItem> customKeyItemArrayList);

    /**
     * 選択中のカスタムキー設定position
     *
     * @param selectedItem リスト項目の位置
     */
    void setSelectedItem(int selectedItem);

    /**
     * ダイアログ終了
     */
    void callbackClose();
}
