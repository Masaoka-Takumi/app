package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;

/*
TODO[ver2.5][by 新堀メモ]
●Radioの再生画面のUI層のDABとHDRadio版を作る
 RadioFragment.java  画面(Fragment)
 RadioView.java 　PresenterとFragmentの橋渡し用抽象クラス
 RadioPresenter.java　画面の操作とDomain層とやり取りするクラス
 fragment_player_radio.xml　レイアウトファイル
 */
public interface HdRadioView {

    /**
     * 周波数の設定
     *
     * @param frequency 周波数
     */
    void setFrequency(String frequency);

    /**
     * 放送局の設定
     *
     * @param info 放送局情報
     */
    void setPsInformation(String info);

    /**
     * 楽曲情報の設定
     * @param psName 放送局情報
     * @param songTitle 曲名
     * @param artist 曲のアーティスト
     */
    void setMusicInfo(String psName,String songTitle,String artist);

    /**
     * プリセットチャンネルの設定
     *
     * @param pch プリセットチャンネル
     */
    void setPch(int pch);

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
    void setBand(HdRadioBandType band);

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
    void setStatus(TunerStatus status);

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
     * BSM、PTYSearch中のView表示/非表示
     *
     * @param isShow 表示/非表示
     * @param type タイプ
     */
    void showStatusView(boolean isShow, String type);

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

    void setSignalStatus(String info);
    void setMulticastNumber(String info);
    void setHdIcon(boolean visible);

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
