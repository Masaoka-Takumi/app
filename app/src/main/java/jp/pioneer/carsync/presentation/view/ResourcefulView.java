package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;

/**
 * Created by NSW00_008316 on 2017/04/17.
 */

public interface ResourcefulView {

    /**
     * Foregroundへ
     */
    void startForeground(String message);

    /**
     * Backgroundへ
     */
    void stopForeground();

/// MARK - ショートカットキー

    /**
     * APPキー押下
     */
    void dispatchAppKey();

    /**
     * Naviキー押下
     */
    void dispatchNaviKey(String packageName);

    /**
     * Naviキー押下(Marin)
     */
    void dispatchNaviMarinKey(String packageName);

    /**
     * Messageキー押下
     */
    void dispatchMessageKey();

    /**
     * Phoneキー押下
     */
    void dispatchPhoneKey();

    /**
     * ソースキー押下
     */
    void dispatchAvKey();

    /**
     * 音声認識キー押下
     */
    void dispatchVoiceKey();

    /**
     * リスト入場
     */
    void dispatchEnterList();

    void dispatchPermissionRequest();

/// MARK - 衝突検知

    /**
     * 事故検知画面表示
     */
    void showAccidentDetect();

/// MARK - ADAS警告

    /**
     * ADAS警告画面表示
     */
    void showAdasWarning();

/// MARK - パーキングセンサー

    /**
     * パーキングセンサー画面表示
     */
    void showParkingSensor();

    /**
     * 車載器エラー画面表示
     *
     * @param errorText エラー文章
     */
    void showCarDeviceError(String errorTag,String errorTitle,String errorText);

    /**
     * Subscription Update画面表示
     */
    void showSubscriptionUpdating();

    /**
     * 楽曲切り替わり通知の表示
     *
     * @param mediaInfo 楽曲切り替わりの通知内容
     */
    void showSongNotification(AndroidMusicMediaInfo mediaInfo);

    /**
     * エラー内容の表示
     *
     * @param str エラー内容
     */
    void showError(String str);

    /**
     * 楽曲切り替わり通知非表示
     */
    void hideSongNotification();

    /**
     * アプリケーションの起動.
     *
     * @param packageName 起動するアプリケーションのパッケージ名
     */
    void startApplication(String packageName);

    /**
     * 読み上げ開始
     */
    void readMessage();

    /**
     * アンドロイド設定表示
     * @param action アクション
     */
    void onShowAndroidSettings(String action);

    /**
     * ナビ目的地設定
     */
    void setNaviDestination(Double latitude, Double longitude, String name);

}
