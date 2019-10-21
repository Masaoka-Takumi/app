package jp.pioneer.carsync.presentation.view;

import android.net.Uri;
import android.support.annotation.ColorRes;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;

/**
 * Androidローカルコンテンツ再生の抽象クラス
 */

public interface AndroidMusicView {

    void setNowPlayingEnabled(boolean isEnabled);

    /**
     * 楽曲タイトルの設定
     * @param title 曲のタイトル
     */
    void setMusicTitle(String title);

    /**
     * 楽曲情報の設定
     * @param artist 曲のアーティスト
     * @param albumName アルバム名
     * @param genre ジャンル
     */
    void setMusicInfo(String title,String artist,String albumName,String genre);

    /**
     * アルバムアートの設定
     * @param uri アルバムアURI
     */
    void setMusicAlbumArt(Uri uri);

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
    void setRepeatImage(SmartPhoneRepeatMode mode);

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

    boolean isShowPlayerTabContainer();

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
    void setAudioMode(AudioMode audioMode);
    void setAmazonMusicLayout();
    void setAmazonMusicInfo(RenderPlayerInfoItem playerInfoItem);
    void setControlEnable(boolean isEnable);
    void setAlexaNotification(boolean notification);

}
