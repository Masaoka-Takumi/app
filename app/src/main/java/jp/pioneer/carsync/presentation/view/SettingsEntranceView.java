package jp.pioneer.carsync.presentation.view;

import java.util.ArrayList;

import jp.pioneer.carsync.presentation.model.SettingEntrance;

/**
 * 設定入口の抽象クラス
 */

public interface SettingsEntranceView {

    /**
     * アダプター設定
     * @param icons アイコン
     * @param titles タイトル
     * @param enable 有効か否か
     */
    void setAdapter(ArrayList<Integer> icons, ArrayList<SettingEntrance> titles, ArrayList<Boolean> enable);

    /**
     * メッセージ表示
     * @param message メッセージ
     */
    void onShowMessage(String message);

    /**
     * アンドロイド設定表示
     * @param action アクション
     */
    void onShowAndroidSettings(String action);


}
