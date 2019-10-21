package jp.pioneer.carsync.presentation.view;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.BtPhoneColor;

/**
 * Incoming Call Pattern設定画面の抽象クラス.
 */
public interface IncomingCallPatternView {
    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    void setAdapter(ArrayList<String> types);

    /**
     * 選択中アイテム設定.
     *
     * @param selected 選択中の設定
     */
    void setSelectedItem(BtPhoneColor selected);
}
