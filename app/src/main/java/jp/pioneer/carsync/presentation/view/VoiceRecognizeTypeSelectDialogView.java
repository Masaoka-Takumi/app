package jp.pioneer.carsync.presentation.view;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.model.CustomKeyItem;

public interface VoiceRecognizeTypeSelectDialogView {

    /**
     * アダプター設定
     *
     * @param itemList タイプリスト
     */
    void setAdapter(ArrayList<VoiceRecognizeType> itemList);

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
