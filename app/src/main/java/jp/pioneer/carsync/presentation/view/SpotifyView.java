package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.CarDeviceRepeatMode;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.ThumbStatus;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;

/**
 * Spotify再生の抽象クラス
 */

public interface SpotifyView {

    /**
     * 楽曲タイトルの設定
     * @param title 曲のタイトル
     */
    void setMusicTitle(String title);

    /**
     * 楽曲情報の設定
     * @param artist 曲のアーティスト
     * @param albumName アルバム名
     * @param playingTrackSource 再生元
     */
    void setMusicInfo(String title,String artist,String albumName,String playingTrackSource);

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
     * Thumbアイコンの設定
     * @param thumbStatus Thumbアイコンの状態
     */
    void setThumbStatus(ThumbStatus thumbStatus);

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
     * Browsing Mode/Radio Modeの切り替え
     * @param radioPlaying true == Radio Mode
     */
    void setModeView(boolean radioPlaying);

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
