package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.presentation.view.fragment.dialog.SpeechRecognizerDialogFragment;

/**
 * Created by NSW00_007906 on 2017/11/27.
 */

public interface SpeechRecognizerDialogView {
    /**
     * SpeechRecognizerText設定
     *
     * @param text 文字列
     */
    void setText(String text);

    /**
     * 表示中テキスト取得
     */
    String getText();
    void setState(SpeechRecognizerDialogFragment.StateType state);
}
