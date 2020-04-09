package jp.pioneer.carsync.presentation.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import jp.pioneer.carsync.presentation.controller.MainFragmentController;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SpeechRecognizerDialogFragment;

/**
 * MainActivityのinterface
 */

public interface MainView {
    /**
     * 画面遷移
     *
     * @param screenId 遷移先画面ID
     * @param args     Bundle
     */
    void navigate(ScreenId screenId, Bundle args);

    /**
     * 画面差し戻し
     */
    void goBack();

    /**
     * 設定画面解除
     */
    void exitSetting();

    /**
     * ダイアログクローズ
     */
    void closeDialog(ScreenId screenId);

    /**
     * 表示画面IDの取得
     *
     * @return ScreenId
     */
    ScreenId getScreenId();
    /**
     * コンテナに表示中のFragmentの取得
     *
     * @return Fragment
     */
    Fragment getScreenInContainer();
    /**
     * 背景再読み込み
     */
    void reloadBackground();

    /**
     * 背景差し替え (静止画の場合)
     *
     * @param isBlur ブラーの有効無効
     */
    void changeBackgroundBlur(boolean isBlur);

    /**
     * キャプチャイメージの設定
     * <p>
     * 背景がBGVの場合、キャプチャ画像を背景として設定する。
     *
     * @param isEnabled キャプチャの有効無効
     * @param uri       背景動画のUri
     */
    void setCaptureImage(boolean isEnabled, Uri uri);

    /**
     * 背景種別の変更
     *
     * @param isVideo 動画か否か
     *                True: 動画 / False: 静止画
     */
    void changeBackgroundType(boolean isVideo);

    /**
     * 背景静止画の変更
     *
     * @param id 静止画のリソースID
     */
    void changeBackgroundImage(int id);
    void changeBackgroundBitmap(Bitmap img);
    /**
     * 背景動画の変更
     *
     * @param uri 動画のUri
     */
    void changeBackgroundVideo(Uri uri);
    void startMarin(Intent intent);
    /**
     * ナビ起動
     *
     * @param intent Intent
     */
    void startNavigation(Intent intent);

    /**
     * 注意喚起表示
     *
     * @param args Bundle
     */
    void showCaution(Bundle args);

    /**
     * 注意喚起非表示
     */
    void dismissCaution();

    /**
     * 注意喚起が表示されているか否か
     *
     * @return boolean
     */
    boolean isShowCaution();

    /**
     * 検索コンテナ表示
     *
     * @param args 引き継ぎ情報
     */
    void showSearchContainer(Bundle args);

    /**
     * 検索コンテナ非表示
     */
    void dismissSearchContainer();

    /**
     * 検索コンテナ表示中か
     *
     * @return 表示中か否か
     */
    boolean isShowSearchContainer();

    /**
     * 電話帳コンテナ表示
     *
     * @param args 引き継ぎ情報
     */
    void showContactContainer(Bundle args);

    /**
     * 電話帳コンテナ非表示
     */
    void dismissContactContainer();

    /**
     * 電話帳コンテナ表示中か
     *
     * @return 表示中か否か
     */
    boolean isShowContactContainer();

    /**
     * 事故通知ダイアログの表示
     *
     * @param args Bundle
     */
    void showAccidentDetect(Bundle args);

    /**
     * 事故通知ダイアログの非表示
     */
    void dismissAccidentDetect();

    /**
     * 事故通知ダイアログが表示されているか否か
     *
     * @return boolean
     */
    boolean isShowAccidentDetect();

    /**
     * パーキングセンサー画面の表示
     *
     * @param args Bundle
     */
    void showParkingSensor(Bundle args);

    /**
     * パーキングセンサー画面の非表示
     */
    void dismissParkingSensor();

    /**
     * パーキングセンサー画面が表示されているか否か
     *
     * @return boolean
     */
    boolean isShowParkingSensor();

    /**
     * MainFragmentController取得.
     *
     * @return MainFragmentController
     */
    MainFragmentController getController();

    /**
     * 車載機切断ダイアログ表示.
     *
     * @param args Bundle
     */
    void showSessionStopped(Bundle args);

    /**
     * ADAS警報画面の非表示
     */
    void dismissAdasWarning();

     /**
     * 車載機切断ダイアログが表示されているか否か.
      *
     * @return boolean
     */
    boolean isShowSessionStopped();

    // MARK - 音声認識

    /**
     * 音声認識開始
     */
    void startRecognizer();

    /**
     * 音声認識ダイアログ表示
     *
     * @param args 引き継ぎ情報
     */
    void showSpeechRecognizerDialog(Bundle args);

    /**
     * 音声認識ダイアログ非表示
     */
    void dismissSpeechRecognizerDialog();

    /**
     * 音声認識ダイアログ表示中か
     *
     * @return 表示中か否か
     */
    boolean isShowSpeechRecognizerDialog();

    /**
     * 音声認識処理終了.
     */
    void finishSpeechRecognizer();

    /**
     * rsm dB変更.
     *
     * @param rmsdB rsm dB
     */
    void changeRsm(float rmsdB);

    /**
     * SpeechRecognizerText設定
     *
     * @param text 文字列
     */
    void setSpeechRecognizerText(String text);

    // MARK - ADAS

    /**
     * ADAS警報画面の表示
     *
     * @param args Bundle
     */
    void showAdasWarning(Bundle args);

    /**
     * ADAS警報画面が表示されているか否か
     *
     * @return boolean
     */
    boolean isShowAdasWarning();

    /**
     * ADAS起動.
     */
    void startAdas();

    /**
     * ADAS停止.
     */
    void stopAdas();

    /**
     * ADAS設定更新
     */
    void updateAdas();

    // MARK - other

    void showToast(String text);

    /**
     * 通知読み上げダイアログ表示
     *
     * @param args 引き継ぎ情報
     */
    void showReadMessageDialog(Bundle args);

    /**
     * 通知読み上げダイアログ非表示
     */
    void dismissReadMessageDialog();

    /**
     * 通知読み上げダイアログ表示中か
     *
     * @return 表示中か否か
     */
    boolean isShowReadMessageDialog();

    /**
     * Alexaダイアログ非表示
     */
    void dismissAlexaDialog();

    /**
     * Alexaダイアログ表示中か
     *
     * @return 表示中か否か
     */
    boolean isShowAlexaDialog();

    /**
     * AlexaDisplayCardダイアログ非表示
     */
    void dismissAlexaDisplayCardDialog();

    /**
     * AlexaDisplayCardダイアログ表示中か
     *
     * @return 表示中か否か
     */
    boolean isShowAlexaDisplayCardDialog();

    /**
     * 車載器エラーダイアログ/SubscriptionUpdateダイアログ表示
     *
     * @param args Bundle
     * @param tag タグ
     */
    void showCarDeviceErrorDialog(Bundle args, String tag);

    /**
     * 車載器エラーダイアログ/SubscriptionUpdateダイアログ非表示
     *
     * @param tag タグ
     */
    void dismissCarDeviceErrorDialog(String tag);

    /**
     * 全ての車載器エラーダイアログ/SubscriptionUpdateダイアログ非表示
     */
    void dismissCarDeviceErrorDialog();

    /**
     * 車載器エラーダイアログ/SubscriptionUpdateダイアログ表示中か
     *
     * @param tag タグ
     */
    boolean isShowCarDeviceErrorDialog(String tag);

    /**
     * 全ての車載器エラーダイアログ/SubscriptionUpdateダイアログ表示中か
     */
    boolean isShowCarDeviceErrorDialog();

    void showPromptAuthorityPermissionDialog(Bundle args);
    void dismissPromptAuthorityPermissionDialog();
    boolean isShowPromptAuthorityPermissionDialog();
    void setupBillingHelper();
    void startAlexaConnection();
    void finishAlexaConnection();
    void setAlexaCapabilities();
    void manageDrawOverlayPermission();

    void setSpeechRecognizerState(SpeechRecognizerDialogFragment.StateType state);
    void checkSim();
    void getWriteExternalPermission();
}
