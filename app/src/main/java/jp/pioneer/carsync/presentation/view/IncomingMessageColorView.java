package jp.pioneer.carsync.presentation.view;

import java.util.ArrayList;

/**
 * Created by NSW00_008320 on 2018/03/14.
 */

public interface IncomingMessageColorView {
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
