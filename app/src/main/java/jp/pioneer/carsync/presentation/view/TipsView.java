package jp.pioneer.carsync.presentation.view;

import java.util.ArrayList;

import jp.pioneer.carsync.presentation.model.TipsItem;

/**
 * Created by NSW00_008320 on 2018/01/10.
 */

public interface TipsView {
    void setAdapter(ArrayList<TipsItem> items);
    void setSelectedTab(int position);
    int getSelectedTab();
    /**
     * エラー内容の表示
     *
     * @param str エラー内容
     */
    void showError(String str);
    void setDisabled(boolean disabled);
}
