package jp.pioneer.carsync.presentation.util;

import android.widget.TextView;

import jp.pioneer.carsync.presentation.view.widget.ScrollTextView;
import jp.pioneer.carsync.presentation.view.widget.SwitchTextView;

/**
 * TextView用Util
 */

public class TextViewUtil {
    /**
     * フォーカス設定
     * <p>
     * マーキー表示を行うため、Viewにフォーカスを設定する
     *
     * @param views 設定を行うView群
     */
    public static void setSelected(TextView... views) {
        for (TextView v : views) {
            v.setSelected(true);
        }
    }

    /**
     * テキスト設定
     * <p>
     * Viewテキストと新テキストが違う文章であればテキスト設定を行う
     *
     * @param view テキスト設定するView
     * @param text テキスト
     */
    public static void setTextIfChanged(TextView view, CharSequence text) {
        CharSequence oldText = view.getText();
        if (!oldText.equals(text)) {
            view.setText(text);
        }
    }

    /**
     * テキスト設定
     * <p>
     * Viewテキストと新テキストが違う文章であればテキスト設定を行う
     *
     * @param view テキスト設定するView
     * @param text テキスト
     */
    public static void setTextIfChanged(SwitchTextView view, CharSequence text) {
        CharSequence oldText = view.getText();
        if (!oldText.equals(text)) {
            view.setSingleText(text);
        }
    }

    /**
     * マーキー用テキスト設定
     * <p>
     * 英字のみのテキストをTextViewに設定すると、Marquee位置がリセットされる不具合が発生する
     * 対策としてテキストの前後に全角スペースを追加する
     * TODO 事象発生用確認 -> それまで追加しない
     *
     * @param view テキスト設定するView
     * @param text テキスト
     */
    public static void setMarqueeTextIfChanged(TextView view, CharSequence text) {
//        if (text != null) {
//            text = "　" + text.toString() + "　";
//        }
        setTextIfChanged(view, text);
    }

    /**
     * マーキー用テキスト設定
     * <p>ScrollTextViewを使用した場合
     *
     * @param view テキスト設定するView
     * @param text テキスト
     */
    public static void setMarqueeTextIfChanged(ScrollTextView view, CharSequence text) {
        CharSequence oldText = view.getText();
        if (!oldText.equals(text)) {
            view.setText(text);
            view.startScroll();
        }
    }
}
