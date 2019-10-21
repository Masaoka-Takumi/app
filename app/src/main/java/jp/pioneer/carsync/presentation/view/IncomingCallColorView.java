package jp.pioneer.carsync.presentation.view;

import java.util.ArrayList;

/**
 * Incoming Call Color設定画面の抽象クラス.
 */
public interface IncomingCallColorView {
    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    void setAdapter(ArrayList<String> types);

    /**
     * 選択中アイテム設定.
     *
     * @param position 選択中の設定
     */
    void setSelectedItem(int position);
}
