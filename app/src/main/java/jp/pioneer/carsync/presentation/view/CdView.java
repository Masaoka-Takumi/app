package jp.pioneer.carsync.presentation.view;


import android.support.annotation.ColorRes;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.CarDeviceRepeatMode;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;

/**
 * CD再生画面のinterface
 */

public interface CdView {
    /**
     * 楽曲タイトルの設定
     * @param title 曲のタイトル
     */
    void setMusicTitle(String title);
    /**
     * 楽曲情報の設定
     * @param artist 曲のアーティスト
     * @param albumName アルバム名
     */
    void setMusicInfo(String title,String artist,String albumName);

    /**
     * プログレスバーの最大値の設定
     * @param max プログレスバーの最大値
     */
    void setMaxProgress(int max);

    /**
     * プログレスバーの現在値の設定
     * @param curr プログレスバーの現在値
     */
    void setCurrentProgress(int curr);

    /**
     * リピートアイコンの設定
     * @param mode リピート状態
     */
    void setRepeatImage(CarDeviceRepeatMode mode);

    /**
     * シャッフルアイコンの設定
     * @param mode シャッフル状態
     */
    void setShuffleImage(ShuffleMode mode);

    /**
     * PlayBackModeの設定
     *
     * @param mode PlayBackMode
     */
    void setPlaybackMode(PlaybackMode mode);

    /**
     * ジェスチャー表示.
     *
     * @param type ジェスチャー種別
     */
    void showGesture(GestureType type);

    // MARK - color

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    void setColor(@ColorRes int color);

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
