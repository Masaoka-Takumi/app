package jp.pioneer.carsync.presentation.view;

/**
 * 事故通知ダイアログのinterface
 */

public interface AccidentDetectDialogView {

    /**
     * 発信.
     *
     * @param number 電話番号
     */
    void callPhone(String number);

    /**
     * SMS送信
     *
     * @param number 電話番号
     * @param text   本文
     */
    void sendSMS(String number, String text);

    /**
     * プログレス初期設定
     *
     * @param maxValue カウント時間
     */
    void initProgress(int maxValue);

    /**
     * プログレス更新
     *
     * @param millisUntilFinished 残り時間
     */
    void updateProgress(long millisUntilFinished);

    /**
     * テキスト更新
     *
     * @param state   表示文言
     * @param isRetry リトライ中
     */
    void updateText(String state, boolean isRetry);

    /**
     * ダイアログ終了
     */
    void callbackClose();

    boolean isShowDialog();

    /**
     * タイマーカウント終了
     */
    void callbackTimerFinished();
}
