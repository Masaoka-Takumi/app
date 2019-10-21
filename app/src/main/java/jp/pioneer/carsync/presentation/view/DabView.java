package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;


public interface DabView {

    /**
     * FmLinkの設定
     *
     * @param fmLink FmLink
     */
    void setFmLink(String fmLink);

    /**
     * Service Component Labelの設定
     *
     * @param serviceName Service Component Label
     */
    void setServiceName(String serviceName);

    void setPlayPauseVisible(boolean isVisible);
    void setPlayPause(boolean isPlay,boolean timeShiftModeAvailable);
    void setTimeShiftVisible(boolean isVisible);
    void setTimeShift(boolean timeShiftMode,boolean timeShiftModeAvailable);
    /**
     * Dynamic Labelの設定
     *
     * @param info Dynamic Label
     */
    void setDynamicLabelText(String info);

    /**
     * ラジオ番組タイプの設定
     *
     * @param pty ラジオ番組タイプ
     */
    void setPtyName(String pty);

    /**
     * serviceNumberの設定
     *
     * @param serviceNumber serviceNumber
     */
    void setServiceNumber(String serviceNumber);

    /**
     * お気に入り状態の表示/非表示設定
     *
     * @param isVisible 表示/非表示
     */
    void setFavoriteVisible(boolean isVisible);

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
    void setBand(DabBandType band);

    /**
     * アンテナレベルの設定
     *
     * @param level アンテナレベル
     */
    void setAntennaLevel(float level);

    /**
     * TunerStatusの設定.
     *
     * @param status TunerStatus
     */
    void setStatus(PlaybackMode mode, TunerStatus status);

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

    void setBandList(ArrayList<BandType> bandTypes);
    void setViewPagerCurrentPage(BandType bandType);
    void setSelectedPreset(int pch);
}
