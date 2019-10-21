package jp.pioneer.carsync.presentation.view;

import java.util.ArrayList;

import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;

/**
 * Created by NSW00_007906 on 2018/02/06.
 */

public interface TiView {
    /**
     * 周波数の設定
     *
     * @param frequency 周波数
     */
    void setFrequency(String frequency);

    /**
     * アンテナレベルの設定
     *
     * @param level アンテナレベル
     */
    void setAntennaLevel(float level);

    /**
     * ジェスチャー表示.
     *
     * @param type ジェスチャー種別
     */
    void showGesture(GestureType type);

    // MARK - EQ FX

    /**
     * 選択中EQの設定
     *
     * @param str 文字列リソース
     */
    void setEqButton(String str);

    /**
     * 選択中SoundFxの設定
     *
     * @param str 文字列リソース
     */
    void setFxButton(String str);

    /**
     * EQボタン、FXボタンの有効無効設定
     *
     * @param eqEnabled EQ設定有効/無効
     * @param fxEnabled FX設定有効/無効
     */
    void setEqFxButtonEnabled(boolean eqEnabled, boolean fxEnabled);

    /**
     * EQ,FX更新時メッセージの表示
     */
    void displayEqFxMessage(String str);

    /**
     * Adasアイコンの表示設定
     *
     * @param isEnabled 設定有効/無効
     */
    void setAdasEnabled(boolean isEnabled);

    /**
     * Adasアイコンの表示設定
     *
     * @param status ノーマル/検知中/エラー
     */
    void setAdasIcon(int status);

    /**
     * ShortCutKeyの設定
     *
     * @param keys ShortCutKeys
     */
    void setShortcutKeyItems(ArrayList<ShortcutKeyItem> keys);

    /**
     * ShortCutButtonの表示設定
     *
     * @param enabled 表示/非表示
     */
    void setShortCutButtonEnabled(boolean enabled);

    void setAlexaNotification(boolean notification);
}
