package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;

/**
 * SiriusXM再生画面のinterface
 */

public interface SxmView {

    /**
     * チャンネル名の設定
     *
     * @param channel チャンネル名
     */
    void setChannelName(String channel);

    /**
     * 楽曲情報の設定
     * @param title 楽曲名
     * @param artist 曲のアーティスト
     * @param category カテゴリー名
     */
    void setMusicInfo(String title,String artist,String category);

    /**
     * CHANNEL NUMBERの設定
     *
     * @param channelNumber CHANNEL NUMBER
     */
    void setChannelNumber(String channelNumber);

    /**
     * プリセットチャンネルの設定
     *
     * @param pch プリセットチャンネル
     */
    void setPch(int pch);

    /**
     * お気に入り状態の設定
     *
     * @param isFavorite お気に入りかどうか
     */
    void setFavorite(boolean isFavorite);

    /**
     * 選択中BANDの設定
     *
     * @param band BAND
     */
    void setBand(String band);

    /**
     * 視聴モード選択
     *
     * @param isReplayMode True : replay / False : Channel
     */
    void setReplayMode(boolean isReplayMode);


    /**
     * アンテナレベルの設定
     *
     * @param level アンテナレベル
     */
    void setAntennaLevel(float level);

    /**
     * Sxmアイコンの設定
     *
     * @param visible 表示/非表示
     */
    void setSxmIcon(boolean visible);

    /**
     * プログレスバーの最大値の設定
     *
     * @param max プログレスバーの最大値
     */
    void setMaxProgress(int max);

    /**
     * プログレスバーの現在値の設定
     *
     * @param curr プログレスバーの現在値
     */
    void setCurrentProgress(int curr);

    /**
     * プログレスバーのセカンド値の設定
     *
     * @param curr プログレスバーのセカンド値
     */
    void setSecondaryProgress(int curr);

    /**
     * PlayBackModeの設定
     *
     * @param mode PlayBackMode
     */
    void setPlaybackMode(PlaybackMode mode);

    /**
     * TuneMixの設定
     *
     * @param inTuneMix 設定
     */
    void setTuneMix(boolean inTuneMix);

    /**
     * Tune Mixの有効設定.
     *
     * @param enabled 有効か否か
     */
    void setTuneMixEnabled(boolean enabled);

    /**
     * Replayモード移行ボタンの有効設定.
     *
     * @param enabled 有効か否か
     */
    void setReplayButtonEnabled(boolean enabled);

    /**
     * Chボタンの有効設定
     *
     * @param enabled 有効か否か
     */
    void setChButtonEnabled(boolean enabled);

    /**
     * Liveボタンの有効設定.
     *
     * @param enabled 有効か否か
     */
    void setLiveButtonEnabled(boolean enabled);

    /**
     * TuneMixの表示設定
     *
     * @param visible 表示するか否か
     */
    void setTuneMixVisibility(boolean visible);

    /**
     * Replayモード移行ボタンの表示設定
     *
     * @param visible 表示するか否か
     */
    void setReplayButtonVisibility(boolean visible);

    /**
     * お気に入り表示設定.
     *
     * @param visible 表示するか否か
     */
    void setFavoriteVisibility(boolean visible);

    /**
     * ジェスチャー表示.
     *
     * @param type ジェスチャー種別
     */
    void showGesture(GestureType type);

    /**
     * リスト有効設定.
     *
     * @param isEnabled 有効か否か
     */
    void setListEnabled(boolean isEnabled);

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

    boolean isShowRadioTabContainer();

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
